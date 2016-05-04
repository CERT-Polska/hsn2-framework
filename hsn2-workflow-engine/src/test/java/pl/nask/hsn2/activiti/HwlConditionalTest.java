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

package pl.nask.hsn2.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.testng.Assert;
import org.testng.annotations.Test;

import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.hwl.Conditional;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionFlow;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionPoint;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.suppressor.JobSuppressorHelperImpl;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

@Test
public class HwlConditionalTest extends AbstractActivitiTest {

    public void hwlConditionalEmptyFlowsWithTrue() {
        PvmProcessDefinition definition = createProcessWithCondition("true", null, null);
        PvmProcessInstance instance = definition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        // should already be in the 'end' state
        assertProcessState(instance, "condition-end-act-2");
        assertEnded(instance);
    }

    public void hwlConditionalEmptyFlowsWithFalse() {
        PvmProcessDefinition definition = createProcessWithCondition("false", null, null);
        PvmProcessInstance instance = definition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        // should already be in the 'end' state
        assertProcessState(instance, "condition-end-act-2");
        assertEnded(instance);
    }

    public void hwlConditionalWithFalse() {
        PvmProcessDefinition definition = createProcessWithCondition("false", "trueService", "falseService");
        PvmProcessInstance instance = definition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        // shall fall into 'false' branch - true is empty, so we can't have "condition-end"
        signalActiveExecution(instance);
        assertProcessState(instance, "condition-end-act-2");
        assertEnded(instance);
    }

    public void hwlConditionalWithEmptyFalse() {
        PvmProcessDefinition definition = createProcessWithCondition("false", "trueService", null);
        PvmProcessInstance instance = definition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        assertProcessState(instance, "condition-end-act-2");
        assertEnded(instance);
    }

    public void hwlConditionalWithTrue() {
        PvmProcessDefinition definition = createProcessWithCondition("true", "trueService", null);
        PvmProcessInstance instance = definition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        // shall fall into 'true' branch - false is empty, so we can't have "condition-end"
        signalActiveExecution(instance);
        assertProcessState(instance, "condition-end-act-2");
        assertEnded(instance);
    }

    public void hwlConditionalWithEmptyTrue() {
        PvmProcessDefinition definition = createProcessWithCondition("true", null, "falseService");
        PvmProcessInstance instance = definition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        assertProcessState(instance, "condition-end-act-2");
        assertEnded(instance);
    }

    public void testMultipleInstancesPerformance() {
        PvmProcessDefinition def = createProcessWithCondition("true", "trueService", "falseService");
        super.testMultipleInstancesPerformance("Condition", def);
    }

    private PvmProcessDefinition createProcessWithCondition(String expression, String onTrueServiceName, String onFalseServiceName) {
        ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(pvmFactory.behaviourFactory);

        ExecutionFlow trueFlow = flowWithService(onTrueServiceName);
        ExecutionFlow falseFlow = flowWithService(onFalseServiceName);

        Conditional conditional = new Conditional(expression, trueFlow, falseFlow);

        List<ExecutionPoint> executionPoints = new ArrayList<ExecutionPoint>();
        executionPoints.add(conditional);

        builder.addProcess("main", executionPoints );

        List<PvmProcessDefinition> definitions = builder.getRegistry().getDefinitions();
        Assert.assertEquals(definitions.size(), 1, "number of process definitions");
        return definitions.get(0);
    }

    private ExecutionFlow flowWithService(String serviceName) {
        if (serviceName != null) {
            Service s = new Service(serviceName);
            ExecutionFlow flow = new ExecutionFlow();
            flow.addExecutionPoint(s);
            return flow;
        } else {
            return null;
        }
    }
}
