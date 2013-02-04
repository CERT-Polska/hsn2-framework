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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;

import pl.nask.hsn2.activiti.ExtendedExecutionImpl;
import pl.nask.hsn2.framework.suppressor.JobSuppressorHelper;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;


public final class ExecutionWrapper {
	// used by ExtendedExecutionImpl
    public final static String TASK_ID = "task_id";
    public final static String PARENT_EXECUTION = "parent_execution";
    public final static String CHILD_EXECUTIONS = "child_executions";
    
    public final static String PROCESS_CONTEXT= "process_context";

    private final PvmExecution pvmExecution;
    private final ActivityExecution activityExecution;
    
    private ProcessContext processContext;

    public ExecutionWrapper(PvmExecution exec) {
        this.pvmExecution = exec;
        activityExecution = null;
    }

    public ExecutionWrapper(ActivityExecution exec) {
        this.activityExecution = exec;
        pvmExecution = null;
    }

    public void initProcessState(long jobId, long objectId, Map<String, Properties> userConfig, WorkflowDescriptor wdf, JobSuppressorHelper jobSuppressorHelper) {
        initProcessState(jobId, objectId, userConfig, wdf, new DefaultTasksStatistics(), jobSuppressorHelper);
    }

    public void initProcessState(long jobId, long objectId,	Map<String, Properties> userConfig, WorkflowDescriptor wdf, DefaultTasksStatistics stats, JobSuppressorHelper jobSuppressorHelper) {
        if (pvmExecution == null)
            throw new IllegalStateException("Process variables can be initialized in the PvmExecution only");
        if (getProcessContext() != null)
            throw new IllegalStateException("Process variables are already initialized");

        ProcessContext subprocessContext = new ProcessContext(jobId, userConfig, new SubprocessParameters(wdf, objectId), stats, jobSuppressorHelper);
        pvmExecution.setVariable(PROCESS_CONTEXT, subprocessContext);
    }
    
    public ProcessContext getProcessContext() {
    	if (processContext == null) {
    		processContext = (ProcessContext) getVariable(PROCESS_CONTEXT);
    	}
    	return processContext;
    }

    public void initProcessState(long jobId, JobSuppressorHelper jobSuppressorHelper) {
        initProcessState(jobId, 0, null, null, jobSuppressorHelper);
    }


    public void markTaskAsAccepted() {
    	getProcessContext().markTaskAsAccepted();
    }

    private void setVariable(String variableName, Object value) {
    	if (pvmExecution != null) {
    		pvmExecution.setVariable(variableName, value);
    	} else {
    		activityExecution.setVariableLocal(variableName, value);
    	}
    }

    private Object getVariable(String variableName) {
        if (pvmExecution != null) {
            return pvmExecution.getVariable(variableName);
        } else {
            return activityExecution.getVariable(variableName);
        }
    }

    /**
     *
     * @return taskId value which has been assigned to the execution
     */
    public int setNewTaskId() {
    	int tid = getProcessContext().newTaskId();
    	setVariable(TASK_ID, tid);
    	return tid;
    }

    public Long getJobId() {
        return getProcessContext().getJobId();
    }

    public Integer getTaskId() {
        return getProcessContext().getTaskId();
    }

	public Map<String, Properties> getUserConfig() {
        return getProcessContext().getUserConfig();
    }

    /**
     * initialize process state by copying the state of the process given as an argument.
     * @param execution
     * @param subprocessParams 
     */
    public void initProcessStateFrom(ActivityExecution execution, SubprocessParameters subprocessParams) {
        if (pvmExecution == null) {
            throw new IllegalStateException("process may be only initialized from within PvmExecution");
        }

    	setVariable(PROCESS_CONTEXT, new ExecutionWrapper(execution).getProcessContext().subprocessContext(subprocessParams));
    	setVariable(PARENT_EXECUTION, execution);
    	getChildExecutions(execution).add(pvmExecution);
    }

    public void signal(String signalName, Object requestId) {
        if (pvmExecution != null) {
            pvmExecution.signal(signalName, requestId);
        }
    }

    public void signal(String signalName) {
    	if (pvmExecution != null) {
    		pvmExecution.signal(signalName, null);
    	} else {
    		((ExtendedExecutionImpl) activityExecution).signal(signalName, null);
    	}

    	if ("resume".equals(signalName)) {
    		List<ExecutionWrapper> waiting = getWaitingOnResume();
    		while (waiting.size() > 0) {
    			ExecutionWrapper wrapper = waiting.remove(waiting.size() - 1);
    			wrapper.signal(signalName);
    		}
    	} else {
    		for (PvmExecution exec: getChildExecutions()) {
    			new ExecutionWrapper(exec).signal(signalName);
    		}
    	}
    }

    public void subprocess(WorkflowDescriptor wdf, long objectDataId) {
        SubprocessParameters params = new SubprocessParameters(wdf, objectDataId);
        pvmExecution.signal("subprocess", params);
    }

    public int countActiveSubprocesses() {

    	int active = 0;
    	List<PvmExecution> execs = getChildExecutions();

    	for (PvmExecution exec: execs) {
    		ExecutionImpl execImpl = (ExecutionImpl) exec;
    		if (!execImpl.isEnded()) {
    			active ++;
    			ExecutionWrapper wrapper = new ExecutionWrapper(exec);
    			active += wrapper.countActiveSubprocesses();
    		}
    	}

    	return active;
    }

    public List<PvmExecution> getChildExecutions() {
    	if (pvmExecution != null) {
    		List<PvmExecution> execs = getChildExecutions(pvmExecution);
    		return Collections.unmodifiableList(execs);
    	} else {
    		List<PvmExecution> execs = getChildExecutions(activityExecution);
    		return Collections.unmodifiableList(execs);
    	}
    }

    private List<PvmExecution> getChildExecutions(ActivityExecution execution) {
        @SuppressWarnings("unchecked")
        List<PvmExecution> executions = (List<PvmExecution>) execution.getVariable(CHILD_EXECUTIONS);
        if (executions == null) {
            executions = new LinkedList<PvmExecution>();
            execution.setVariableLocal(CHILD_EXECUTIONS, executions);
        }
        return executions;
    }

    private List<PvmExecution> getChildExecutions(PvmExecution execution) {
        @SuppressWarnings("unchecked")
        List<PvmExecution> executions = (List<PvmExecution>) execution.getVariable(CHILD_EXECUTIONS);
        if (executions == null) {
            executions = new LinkedList<PvmExecution>();
            execution.setVariable(CHILD_EXECUTIONS, executions);
        }
        return executions;
    }

    public SubprocessParameters getSubprocessParameters() {
        return getProcessContext().getSubprocessParameters();
    }

	private List<ExecutionWrapper> getWaitingOnResume() {
        return getProcessContext().getWaitingForResume();
    }

    public DefaultTasksStatistics getJobStats() {
        return getProcessContext().getJobStats();
    }

    public void schedule() {
        getWaitingOnResume().add(this);
    }
}
