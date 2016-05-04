/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.1.
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

package pl.nask.hsn2.framework.workflow.builder;

import java.util.List;

import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionFlow;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionPoint;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.ServiceParam;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

public interface WorkflowBuilder {
    void buildWorkflow(String name, List<ProcessDefinition> processes);

    void buildWorkflow(Workflow workflow);

    void addConditional(String condition, ExecutionFlow onTrueFlow, ExecutionFlow onFalseFlow);

    void addProcess(String id, List<ExecutionPoint> executionPoints);

    void addParallel(List<ExecutionFlow> threads);

    void addService(String name, String id, List<ServiceParam> parameters, List<Output> outputs,List<TaskErrorReasonType> errorsToIgnore);

    void addWait(String expression);

    void addScript(String scriptBody);
}
