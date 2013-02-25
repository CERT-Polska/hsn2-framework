/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.workflow.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;
import java.util.Set;

import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.activiti.ExtendedExecutionImpl;
import pl.nask.hsn2.activiti.behavior.HSNBehavior;
import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.api.TimeoutException;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnectorException;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.ObjectData;
import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.suppressor.JobSuppressorHelper;
import pl.nask.hsn2.framework.workflow.engine.ProcessBasedWorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;
import pl.nask.hsn2.framework.workflow.job.WorkflowJob;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;

public class ActivitiJob implements WorkflowJob, WorkflowJobInfo {
    private final static Logger LOGGER = LoggerFactory.getLogger(ActivitiJob.class);

    private final ProcessBasedWorkflowDescriptor<PvmProcessDefinition> workflowDefinitionDescriptor;

    private final PvmProcessDefinition processDefinition;
    private PvmProcessInstance processInstance;
    private long jobId;

    private TaskErrorReasonType failureReason;
    private boolean running = false;
    private Map<String, Integer> errorMessages = new ConcurrentHashMap<>();

    private Map<String, Properties> userConfig;

    private long startTime = 0;
    private long endTime = 0;

    private String lastActiveStepName;
    private long objectDataId;
    private DefaultTasksStatistics stats = new DefaultTasksStatistics();

	public ActivitiJob(PvmProcessDefinition processDefinition,
			ProcessBasedWorkflowDescriptor<PvmProcessDefinition> workflowDefinitionDescriptorImpl,
			Map<String, Properties> workflowConfig) {
        this.processDefinition = processDefinition;
        this.workflowDefinitionDescriptor = workflowDefinitionDescriptorImpl;
        this.userConfig = workflowConfig;
    }

    @Override
    public synchronized void start(long jobId, JobSuppressorHelper jobSuppressorHelper) {
        if (processInstance != null) {
            throw new IllegalStateException("Job already started");
        } else {
            processInstance = processDefinition.createProcessInstance();
            
            startTime = System.currentTimeMillis();
            this.jobId = jobId;
            
            try{
	            this.objectDataId = createInitialObject(jobId);
	            ExecutionWrapper utils = new ExecutionWrapper(processInstance);
	            utils.initProcessState(jobId, objectDataId, userConfig, workflowDefinitionDescriptor, stats, jobSuppressorHelper);
	            processInstance.start();
	            this.running = true;
	            ((FrameworkBus)BusManager.getBus()).jobStarted(jobId);
	            LOGGER.info("Job started (jobId={}, userConfig={}, workflowDefinition={}, initialObjectId={}", new Object[] {jobId, userConfig, workflowDefinitionDescriptor, objectDataId});
            }
            catch(ObjectStoreConnectorException e){
            	failureReason = TaskErrorReasonType.OBJ_STORE;
            	addErrorMessage("Problem with connection to objectStore: " + e.getMessage());
            	endTime = System.currentTimeMillis();
            }
        }
    }

    private long createInitialObject(long jobId) throws ObjectStoreConnectorException {
    	return ((FrameworkBus)BusManager.getBus()).getObjectStoreConnector().sendObjectStoreData(jobId, new ObjectData());
    }

    @Override
    public boolean isEnded() {
        return !running;
    }

    @Override
    public synchronized JobStatus getStatus() {
        if (isFailed()) {
            return JobStatus.FAILED;
        } else if (isAborted()) {
            return JobStatus.CANCELLED;
        } else if (isEnded()) {
            return JobStatus.COMPLETED;
        } else {
            return JobStatus.PROCESSING;
        }
    }

    private boolean isAborted() {
        return false;
    }

    private boolean isFailed() {
        return failureReason != null;
    }

    @Override
    public synchronized void markTaskAsAccepted(int requestId) {
        if (running) {
            ExecutionWrapper execution = findExecutionForTaskId(requestId);
            if (execution == null) {
                LOGGER.debug("Cannot find execution for taskId={}. The task may be already completed", requestId);
            } else {
                //  don't care, if the task was accepted previously.
                execution.markTaskAsAccepted();
            }
        } else {
        	// Send reminder that job is already finished.
        	((FrameworkBus) BusManager.getBus()).jobFinishedReminder(jobId, getStatus(), requestId);

            LOGGER.debug("Job (id={}) is not running. Can not mark task (id={}) as accepted", jobId, requestId);
        }
    }

    private ExecutionWrapper findExecutionForTaskId(int requestId) {
        long searchStartTime = System.currentTimeMillis();
        ExtendedExecutionImpl extendedProcessInstance = (ExtendedExecutionImpl) processInstance;
        PvmExecution execution = extendedProcessInstance.findExecuionWithTaskId(requestId);
        if (execution != null) {
            ExecutionWrapper wrapper = new ExecutionWrapper(execution);
            // verify taskId
            Integer taskId = wrapper.getTaskId();
            if (taskId != null && taskId.equals(requestId)) {
                LOGGER.debug("Execution (jobId={}, taskId={}) found in {} ms", new Object[] {jobId, requestId, System.currentTimeMillis() - searchStartTime});
                return wrapper;
            }
        }
        LOGGER.debug("Execution (jobId={}, taskId={}) not found after {} ms", new Object[] {jobId, requestId, System.currentTimeMillis() - searchStartTime});
        return null;
    }

    @Override
    public synchronized void markTaskAsCompleted(int requestId, Set<Long> newObjects) {
        if (running) {
            ExecutionWrapper execution = findExecutionForTaskId(requestId);
            if (execution == null) {
            	LOGGER.warn("Execution for taskId={} cannot be found. The task may be already completed.", requestId);
            } else {
                if (newObjects != null) {
                    for (Long objectId: newObjects) {
                        execution.subprocess(workflowDefinitionDescriptor, objectId);
                    }
                    resume();
                }
                try {
                	execution.signal("completeTask", requestId);
                } catch (Exception e) {
                	 LOGGER.error("Error processing job", e);
                     markTaskAsFailed(requestId, TaskErrorReasonType.DEFUNCT, e.getMessage());
                }
            }

            if (processInstance.isEnded()) {
            	finishJob();
            }
        } else {
        	// Send reminder that job is already finished.
        	((FrameworkBus) BusManager.getBus()).jobFinishedReminder(jobId, getStatus(), requestId);

        	LOGGER.debug("Job (id={}) is not running. Can not mark task (id={}) as completed", jobId, requestId);
        }
    }

    public long getId() {
        return jobId;
    }

    @Override
    public synchronized void markTaskAsFailed(int requestId, TaskErrorReasonType reason, String description) {
    	if (running) {
    		ExecutionWrapper exec = findExecutionForTaskId(requestId);
    		try {
    			addErrorMessage(description);
    			exec.signal("taskFailed", new Object[] {requestId, reason, description});
    		} catch(Exception e) {
    			this.failureReason = reason;
    			this.lastActiveStepName = getActiveStepName();
    			LOGGER.info("Job failed (jobId={}, taskId={}, stepName={}, reason={}, errorMsg={})", new Object[] {jobId, requestId, this.lastActiveStepName, reason, description});
    			processInstance.deleteCascade(description);
    			finishJob();
    		}
    	} else {
    		LOGGER.debug("Job (id={}) is not running (already failed). Can not mark task (id={}) as failed. Ignoring new failure reason {} ({})", new Object[] {jobId, requestId, reason, description});
    		
        	// Send reminder that job is already finished.
        	((FrameworkBus) BusManager.getBus()).jobFinishedReminder(jobId, getStatus(), requestId);

        	LOGGER.debug("Job (id={}) is not running. Can not mark task (id={}) as completed", jobId, requestId);

    	}
    }

	private void addErrorMessage(String msg) {
		Integer i = errorMessages.get(msg);
		if (i == null) {
			i = 1;
		} else {
			i++;
		}
		errorMessages.put(msg, i);
	}

    private void finishJob(){
    	this.endTime  = System.currentTimeMillis();
		this.running = false;

        ExecutionWrapper utils = new ExecutionWrapper(processInstance);
        utils.getProcessContext().removeJobSuppressorHelper();

		try{
			((FrameworkBus)BusManager.getBus()).getObjectStoreConnector().sendJobFinished(jobId, getStatus());
		}
		catch (ObjectStoreConnectorException e) {
			LOGGER.error("Error when sending JobFinished to store!",e);
		}
		LOGGER.info("Job {} {} time: {}",new Object[]{jobId,getStatus(),endTime - startTime});
    }
    
    @Override
    public synchronized void resume() {
        if (running) {
            ExecutionWrapper wrapper = new ExecutionWrapper(processInstance);
            try {
                wrapper.signal("resume");
                updateActiveStepName();
            } catch (Exception e) {
                LOGGER.error("Error processing job", e);
                markTaskAsFailed(wrapper.getTaskId() == null ? -1 : wrapper.getTaskId(), TaskErrorReasonType.DEFUNCT, e.getMessage());
            }
        } else {
            LOGGER.debug("Job (id={}) is not running. Can not resume it's processes", jobId);
        }
    }

    private void updateActiveStepName(){
    	ActivityImpl activity = (ActivityImpl) processInstance.getActivity();
        if (activity != null) {
            HSNBehavior behavior = (HSNBehavior) activity.getActivityBehavior();
            lastActiveStepName = behavior.getStepName();
        }
    }
    
    @Override
    public synchronized String getActiveStepName() {
    	return lastActiveStepName;
    }
    
    @Override
    public boolean isErrorMessagesReceived(){
    	return !errorMessages.isEmpty();
    }
    
    @Override
    public String getErrorMessage() {
    	StringBuilder sb = new StringBuilder();
    	for (Entry<String, Integer> errMsgEntry : errorMessages.entrySet()) {
    		if (sb.length() != 0) {
    			sb.append("; ");
    		}
			sb.append(errMsgEntry.getKey()).append(" (").append(errMsgEntry.getValue()).append(")");
		}
        return sb.toString();
    }

    @Override
    public Map<String, Properties> getUserConfig() {
        return userConfig;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return endTime;
    }

    @Override
    public synchronized int getActiveSubtasksCount() {
        ExecutionWrapper utils = new ExecutionWrapper(processInstance);
        return utils.countActiveSubprocesses();
    }

    @Override
    public String getWorkflowRevision() {
        return workflowDefinitionDescriptor.getId();
    }
    
    @Override
    public String getWorkflowName() {
    	return workflowDefinitionDescriptor.getName();
    }

    @Override
    public DefaultTasksStatistics getTasksStatistics() {
        return stats;
    }
}
