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

package pl.nask.hsn2.activiti;

import java.util.List;

import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;

import pl.nask.hsn2.workflow.engine.ProcessContext;

public final class ExtendedExecutionImpl extends ExecutionImpl {
	// variable names
    public static final String TASK_ID = "task_id";
    public static final String PARENT_EXECUTION = "parent_execution";
    public static final String CHILD_EXECUTIONS = "child_executions";
    public static final String PROCESS_CONTEXT= "process_context";
   
	private static final long serialVersionUID = -3070016232270340831L;
	private ProcessContext processContext;
	private ExtendedExecutionImpl parentExecution;
	private Integer taskId;
	private List<ExtendedExecutionImpl> childExecutions;

	@Override
    protected ExecutionImpl newExecution() {
        return new ExtendedExecutionImpl();
    }

    @Override
    protected void collectActiveActivityIds(List<String> activeActivityIds) {
        super.collectActiveActivityIds(activeActivityIds);
        if (subProcessInstance != null) {
            ((ExtendedExecutionImpl) subProcessInstance).collectActiveActivityIds(activeActivityIds);
        }

        List<ExtendedExecutionImpl> executions = getChildProcesses();
        if (executions != null) {
            for (ExtendedExecutionImpl execution: executions) {
               execution.collectActiveActivityIds(activeActivityIds);
            }
        }
    }

    @Override
    public ExtendedExecutionImpl findExecution(String activityId) {
        ExtendedExecutionImpl res = (ExtendedExecutionImpl) super.findExecution(activityId);

        if (res == null) {
            res = findExecutionInChildProcesses(activityId);
        }

        if (res == null && subProcessInstance != null) {
            return (ExtendedExecutionImpl) subProcessInstance.findExecution(activityId);
        } else {
            return res;
        }
    }

	private List<ExtendedExecutionImpl> getChildProcesses() {    	
        return childExecutions;
    }

    private ExtendedExecutionImpl findExecutionInChildProcesses(String activityId) {
        // TODO: string repeated in ExecutionWrapper!
        // FIXME: casting PvmExecution to ExecutionImpl!
        List<ExtendedExecutionImpl> executions = getChildProcesses();
        if (executions == null)
            return null;

        for (ExtendedExecutionImpl execution: executions) {
            ExtendedExecutionImpl exec = execution.findExecution(activityId);
            if (exec != null)
                return exec;
        }

        return null;
    }
    
    @Override
    public void setVariable(String variableName, Object value) {
    	if (getParent() == null || getVariableLocal(variableName) != null) {
    		this.setVariableLocal(variableName, value);
    	} else {
    		getParent().setVariable(variableName, value);
    	}
    }
    
    @Override
    public Object getVariable(String variableName) {    
    	Object o = this.getVariableLocal(variableName);
    	if (o == null && getParent() != null) {
    		o = getParent().getVariable(variableName);
    	}
    	
    	return o;
    }

    @Override
    public Object setVariableLocal(String variableName, Object value) {
    	if (PROCESS_CONTEXT.equals(variableName)) {
    		processContext = (ProcessContext) value;    		
    	} else if (PARENT_EXECUTION.equals(variableName)) {
    		parentExecution = (ExtendedExecutionImpl) value;
    	} else if (TASK_ID.equals(variableName)) {
    		taskId = (Integer) value;
    	} else if (CHILD_EXECUTIONS.equals(variableName)) {
    		childExecutions = (List<ExtendedExecutionImpl>) value;
    	} else {
    		ensureVariablesInitialized();
    		setVariableLocally(variableName, value);    		
    	}
    	
    	return value;
    }

    @Override
    public Object getVariableLocal(Object variableName) {
    	if (PROCESS_CONTEXT.equals(variableName)) {
    		return processContext;
    	} 
    	
    	if (PARENT_EXECUTION.equals(variableName)) {
    		return parentExecution;
    	}
    	
    	if (TASK_ID.equals(variableName)) {
    		return taskId;
    	}
    	
    	if (CHILD_EXECUTIONS.equals(variableName)) {
    		return childExecutions;
    	}
    	
        ensureVariablesInitialized();
        return variables.get(variableName);
    }

    @Override
    /**
     * Overridden to enable output/wait
     */
    public void end() {
        super.end();
        // notify all active waits in parent process, that the execution is ended.
        PvmExecution superProcess = getParentProcess();
        if (superProcess != null) {
            superProcess.signal("notify", null);
        }
    }
    
    @Override
    public void destroy() {
    	super.destroy();
    }
    
    @Override
    public void remove() {
    	super.remove();
    	ExtendedExecutionImpl parentProcess = (ExtendedExecutionImpl) getParentProcess();
    	if (parentProcess != null) {
    		parentProcess.getChildProcesses().remove(this);
    	}
    }

    private PvmExecution getParentProcess() {
        return parentExecution;
    }

    // TODO: an extension would be to return a collection of executions which match the condition
    public PvmExecution findExecutionWithVariable(String variableName, Object value) {

        Object var = getVariableLocal(variableName);
        PvmExecution ret = null;
        if (value.equals(var))
            ret  = this;

        synchronized(this) {    	
	        // check nested executions, child processes and a subprocess
	
	        // nested executions
	        if (ret == null)
	            ret = findExecutionWithVariable(executions, variableName, value);
	
	        // child processes
	        if (ret == null)
	            ret = findExecutionWithVariable(getChildProcesses(), variableName, value);
	
	        // a subprocess
	        if (ret == null && subProcessInstance != null)
	            ret = ((ExtendedExecutionImpl) subProcessInstance).findExecutionWithVariable(variableName, value);
    	}
        return ret;
    }

    private PvmExecution findExecutionWithVariable(List<? extends ExecutionImpl> executions, String variableName, Object value) {
        if (executions != null) {
            for (ExecutionImpl exec: executions) {
                if (!exec.isEnded()) {
                    PvmExecution ret = ((ExtendedExecutionImpl) exec).findExecutionWithVariable(variableName, value);
                    if (ret != null)
                        return ret;
                }
            }
        }

        return null;
    }

    public PvmExecution findExecuionWithTaskId(int taskId) {
        return findExecutionWithVariable(TASK_ID, taskId);
    }       
}