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

package pl.nask.hsn2.activiti.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.expressions.EvaluationException;
import pl.nask.hsn2.expressions.ExpressionResolver;
import pl.nask.hsn2.framework.suppressor.JobSuppressorHelper;
import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ServiceParam;
import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;
import pl.nask.hsn2.workflow.engine.SubprocessParameters;

public class ServiceBehavior extends AbstractBpmnActivityBehavior implements HSNBehavior {
    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceBehavior.class);
    private final String serviceName;
    private final String serviceLabel;
    private final Properties serviceParameters;
    private final List<Output> outputs;
    private final ExpressionResolver expressionResolver;
    private ProcessDefinitionRegistry<PvmProcessDefinition> definitionRegistry;
	private List<TaskErrorReasonType> errorsIgnored;

    public ServiceBehavior(String serviceName, String serviceLabel, Properties parameters, List<Output> outputs, ExpressionResolver resolver, ProcessDefinitionRegistry<PvmProcessDefinition> definitionRegistry,List<TaskErrorReasonType> ignoreErrors) {
        this.serviceName = serviceName;
        this.serviceLabel = serviceLabel;
        this.serviceParameters = parameters;
        this.outputs = outputs;
        this.expressionResolver = resolver;
        this.definitionRegistry = definitionRegistry;
        if(ignoreErrors !=null)
        	this.errorsIgnored = ignoreErrors;
        else
        	this.errorsIgnored = new ArrayList<TaskErrorReasonType>();
    }

    @Override
    public void execute(ActivityExecution execution) {
        LOGGER.debug("Executing activity {}", execution.getActivity().getId());
        ExecutionWrapper wrapper = new ExecutionWrapper(execution);

        Long jobId = wrapper.getJobId();
        if (jobId == null) {
            throw new IllegalStateException("No job_id found for the execution: " + execution);
        }

		int taskId = wrapper.setNewTaskId();
		Properties params = ServiceParam.merge(serviceParameters, wrapper.getUserConfig().get(serviceLabel), true);
		SubprocessParameters processParams = wrapper.getSubprocessParameters();
		long objectDataId = (processParams != null) ? processParams.getObjectDataId() : 0L;
		DefaultTasksStatistics stats = wrapper.getJobStats();

        wrapper.getProcessContext().getJobSuppressorHelper().addTaskRequest(serviceName, serviceLabel, taskId, objectDataId, params, stats);
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Object signalData) throws EvaluationException {
        // TODO: inactive executions should not process the signal. the signal should be passed to the subprocess instead.
        // TODO: all signals should be passed to subexecutions?
    	
    	ExecutionWrapper wrapper = new ExecutionWrapper(execution);
    	JobSuppressorHelper jobSuppressorHelper = wrapper.getProcessContext().getJobSuppressorHelper();
    	
        LOGGER.debug("activity: {}", execution.getActivity().getId());
        LOGGER.debug("got signal: {} with data: {}", signalName, signalData);
        if (!execution.isActive()) {
            LOGGER.debug("Execution not active, ignoring the signal");
        }
        if ("completeTask".equalsIgnoreCase(signalName)) {
            completeTask(execution, signalData);
            jobSuppressorHelper.signalTaskCompletion(wrapper.getJobId(), wrapper.getTaskId());
        } else if ("subprocess".equalsIgnoreCase(signalName)) {
            runSubprocesses(execution, signalData);
        } else if ("taskFailed".equalsIgnoreCase(signalName)) {
        	handleTaskFailed(execution,(Object [])signalData);
        	jobSuppressorHelper.signalTaskCompletion(wrapper.getJobId(), wrapper.getTaskId());
        } else {
            LOGGER.debug("Unknown signal name, ignore: {}",signalName);
        }
    }

    private void handleTaskFailed(ActivityExecution execution, Object[] signalData) {
    	int reqId = (Integer) signalData[0];
    	TaskErrorReasonType errorType = (TaskErrorReasonType) signalData[1];
    	String errorDescription = (String)signalData[2];
    	if(!errorsIgnored.contains(errorType)) {
    		throw new FatalTaskErrorException("Non-ignored task error:"+errorType.name());
    	}
    	else {
    		LOGGER.info("Ignoring TaskError:[reqId={},descr={}]",reqId,errorDescription);
    		ExecutionWrapper wrapper = new ExecutionWrapper(execution);
    		DefaultTasksStatistics stats = wrapper.getJobStats();
    		if (stats != null) {
    			stats.taskCompleted(serviceName);
    		}
    		leave(execution);
    	}

    }


	private void runSubprocesses(ActivityExecution execution, Object signalData) throws EvaluationException {
        LOGGER.debug("run subprocesses");
        if (outputs == null || outputs.isEmpty())
            return;

        for (Output output: outputs) {
            if (isOutputExpressionTrue(execution, signalData, output))
                runSubprocess(execution, signalData, output);
        }
    }

    private boolean isOutputExpressionTrue(ActivityExecution execution, Object signalData, Output output) throws EvaluationException {
        String expression = output.getExpression();
        if (expression == null)
            return true;

        ExecutionWrapper wrapper = new ExecutionWrapper(execution);
        try {
        	return expressionResolver.evaluateBoolean(wrapper.getJobId(), ((SubprocessParameters) signalData).getObjectDataId(), expression);
        } catch (Exception e) {
        	LOGGER.warn("Error evaluating output condition for {}, returning false", output);
        	LOGGER.warn("Error evaluating output condition", e);
        	return false;
        }
    }

    private void runSubprocess(ActivityExecution execution, Object signalData, Output output) {
    	LOGGER.debug("run subprocesses");
        PvmProcessDefinition definition = definitionRegistry.getDefinition(output.getProcessName());
        PvmProcessInstance subprocess = definition.createProcessInstance();
        // set parent to the current execution/process
        ExecutionWrapper subProcessWrapper = new ExecutionWrapper(subprocess);
        subProcessWrapper.initProcessStateFrom(execution, (SubprocessParameters) signalData);

        subprocess.start();
    }

    private void completeTask(ActivityExecution execution, Object signalData) {
        LOGGER.debug("TaskCompleted");

        ExecutionWrapper wrapper = new ExecutionWrapper(execution);
        DefaultTasksStatistics stats = wrapper.getJobStats();
        if (stats != null) {
            stats.taskCompleted(serviceName);
        }
        leave(execution);
    }

    @Override
    public String toString() {
        return "ServiceBehavior (" + serviceName + ")";
    }

    @Override
    public String getStepName() {
        return "service(" + serviceName + ")";
    }
}
