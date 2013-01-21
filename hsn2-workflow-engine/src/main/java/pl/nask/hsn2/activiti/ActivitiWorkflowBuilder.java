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

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;

import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.workflow.builder.WorkflowBuilder;
import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionFlow;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionPoint;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.ServiceParam;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

public class ActivitiWorkflowBuilder implements WorkflowBuilder {
    private ExtendedProcessDefinitionBuilder builder;

    private ProcessDefinitionRegistry<PvmProcessDefinition> registry = new ProcessDefinitionRegistry<PvmProcessDefinition>();

    private String nextActivityId;
    private BehaviorFactory behaviorFactory;

    private IdGen idgen = IdGen.getInstance();

    public ActivitiWorkflowBuilder(BehaviorFactory behaviorFactory) {
        this.behaviorFactory = behaviorFactory;
    }

    @Override
    public void addConditional(String condition, ExecutionFlow onTrueFlow, ExecutionFlow onFalseFlow) {
        String conditionStartActivityId = popCurrentId();
        String endingActivityId = "condition-end-" + popCurrentId();

        List<ExecutionFlow> flows = Arrays.asList(onTrueFlow, onFalseFlow);
        String[] flowIds = splitExecution(conditionStartActivityId, behaviorFactory.decisionBehaviorInstance(condition), flows.size(), Arrays.asList("onTrue", "onFalse"));
        addFlows(flowIds, flows, endingActivityId);

        nextActivityId = endingActivityId;
    }

    @Override
    public void buildWorkflow(String name, List<ProcessDefinition> processes) {
        for (ProcessDefinition process: processes) {
            process.transformToWorkflow(this);
        }
    }

    public void buildWorkflow(Workflow workflow) {
        buildWorkflow(workflow.getName(), workflow.getProcessDefinitions());
    }

    @Override
    public void addProcess(String id, List<ExecutionPoint> executionPoints) {
        builder = new ExtendedProcessDefinitionBuilder(id);
        addStartState();

        addFlow(executionPoints);

        addProcessEndState();
        ExtendedProcessDefinitionImpl definition = builder.buildProcessDefinition();
        registry.add(definition.getId(), definition);
    }

    @Override
    public void addParallel(List<ExecutionFlow> threads) {
        if (threads.size() >= 2) {
            String forkActivityId = popCurrentId();
            String joinActivityId = "join-" + popCurrentId();

            // split executions with fork
            String[] flowIds = splitExecution(forkActivityId, behaviorFactory.forkBehaviorInstance(), threads.size(), null);
            addFlows(flowIds, threads, joinActivityId);

            // add join
            builder
              .createActivity(joinActivityId)
                .behavior(behaviorFactory.joinBehaviorInstance())
                .transition(genNextActivityId())
              .endActivity();
        } else if (threads.size() == 1) {
            addFlow(threads.get(0));
        }
    }

    @Override
    public void addService(String name, String id, List<ServiceParam> parameters, List<Output> output,List<TaskErrorReasonType> errorsToIgnore) {
       addActivity(behaviorFactory.serviceBehaviorInstance(name, id, parameters, output, registry, errorsToIgnore));
    }


    @Override
    public void addWait(String expression) {
        addActivity(behaviorFactory.waitBehavior(expression));
    }

    @Override
    public void addScript(String scriptBody) {
        addActivity(behaviorFactory.scriptBehavior(scriptBody));
    }

    private void addFlows(String[] flowIds, List<ExecutionFlow> flows, String endingActivityId) {
        for (int i=0; i < flowIds.length; i++) {
            nextActivityId = flowIds[i];
            addFlow(flows.get(i));

            // end the flow
            builder.createActivity(nextActivityId)
            .behavior(behaviorFactory.emptyBehaviorInstance())
            .transition(endingActivityId)
            .endActivity();
        }
    }

    private String[] splitExecution(String startActivityId, ActivityBehavior behaviorInstance, int numberOftransitions, List<String> transitionIds) {
        // add decision
        String[] flowIds = new String[numberOftransitions];
        builder
        .createActivity(startActivityId)
        .behavior(behaviorInstance);
        for (int i=0; i < numberOftransitions; i++) {
            flowIds[i] = "flow-start-" + popCurrentId();
            if (transitionIds != null && i < transitionIds.size()) {
                builder.transition(flowIds[i], transitionIds.get(i));
            } else {
                builder.transition(flowIds[i]);
            }
        }
        builder.endActivity();

        return flowIds;
    }

    private void addFlow(List<ExecutionPoint> executionPoints) {
        if (executionPoints != null) {
            for (ExecutionPoint ep: executionPoints) {
                ep.transformToWorkflow(this);
            }
        }
    }

    private void addFlow(ExecutionFlow flow) {
        if (!flow.isEmpty())
            addFlow(flow.getExecutionPoints());
    }

    private void addActivity(ActivityBehavior behavior) {
        builder.createActivity(popCurrentId())
        .behavior(behavior)
        .transition(nextActivityId)
        .endActivity();
    }

    private void addStartState() {
        builder
            .createActivity("start")
              .initial()
              .behavior(behaviorFactory.startBehavior())
              .transition(genNextActivityId())
              .endActivity();
    }

    private void addProcessEndState() {
        builder.createActivity(nextActivityId).behavior(behaviorFactory.waitBehavior(null)).endActivity();
    }

    private String popCurrentId() {
        String currentActId = nextActivityId;
        nextActivityId = idgen.nextId("act").getFormattedId();
        return currentActId;
    }

    private String genNextActivityId() {
        nextActivityId = idgen.nextId("act").getFormattedId();
        return nextActivityId;
    }

    public ProcessDefinitionRegistry<PvmProcessDefinition> getRegistry() {
        return registry;
    }

}
