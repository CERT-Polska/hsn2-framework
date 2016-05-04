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

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.bpmn.behavior.ParallelGatewayActivityBehavior;
import org.activiti.engine.impl.pvm.ProcessDefinitionBuilder;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.testng.Assert;
import org.testng.annotations.Test;

import pl.nask.hsn2.activiti.behavior.TransientParallelGatewayBehavior;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.hwl.Parallel;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.suppressor.JobSuppressorHelperImpl;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

@Test
public class StaticParallelTest extends AbstractActivitiTest {

    public void threeFlows() {
        ProcessDefinitionBuilder builder = pvmFactory.newProcessDefinitionBuilder();
        ParallelGatewayActivityBehavior forkBehavior = new TransientParallelGatewayBehavior();
        pvmFactory.addStart(builder, "fork");

        builder
        .createActivity("fork")
        .behavior(forkBehavior )
        .transition("flow1")
        .transition("flow2")
        .transition("flow3")
        .endActivity();

        builder.createActivity("flow1")
        .behavior(new PvmApiExampleTest.EmptyActivitiBehavior())
        .transition("join")
        .endActivity();

        builder.createActivity("flow2")
        .behavior(new PvmApiExampleTest.EmptyActivitiBehavior())
        .transition("join")
        .endActivity();

        builder.createActivity("flow3")
        .behavior(new PvmApiExampleTest.EmptyActivitiBehavior())
        .transition("join")
        .endActivity();

        builder.createActivity("join")
        .transition("end")
        .behavior(new TransientParallelGatewayBehavior())
        .endActivity();

        pvmFactory.addEnd(builder);

        PvmProcessDefinition pd = builder.buildProcessDefinition();
        PvmProcessInstance pi = pd.createProcessInstance();
        pi.start();
        assertActive(pi, 3);
        signalAllActiveExecutions(pi);

        assertProcessState(pi, "end");
        Assert.assertTrue(pi.isEnded());
    }

    public void twoFlows() {
        ProcessDefinitionBuilder builder = pvmFactory.newProcessDefinitionBuilder();
        ParallelGatewayActivityBehavior forkBehavior = new TransientParallelGatewayBehavior();
        pvmFactory.addStart(builder, "fork");

        builder
        .createActivity("fork")
        .behavior(forkBehavior )
        .transition("leftFlow")
        .transition("rightFlow")
        .endActivity();

        builder.createActivity("leftFlow")
        .behavior(new PvmApiExampleTest.EmptyActivitiBehavior())
        .transition("join")
        .endActivity();

        builder.createActivity("rightFlow")
        .behavior(new PvmApiExampleTest.EmptyActivitiBehavior())
        .transition("join")
        .endActivity();

        builder.createActivity("join")
        .transition("end")
        .behavior(new TransientParallelGatewayBehavior())
        .endActivity();

        pvmFactory.addEnd(builder);

        PvmProcessDefinition pd = builder.buildProcessDefinition();
        PvmProcessInstance pi = pd.createProcessInstance();
        pi.start();
        assertActive(pi, 2);
        signalAllActiveExecutions(pi);

        assertProcessState(pi, "end");
        Assert.assertTrue(pi.isEnded());
    }

    private PvmProcessDefinition createHwlProcessDef() {
        Parallel p = new Parallel();
        p.addThread(new Service("service1"));
        p.addThread(new Service("service2"));

        ProcessDefinition def = new ProcessDefinition("main");
        def.addExecutionPoint(p);

        ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(pvmFactory.behaviourFactory);

        builder.buildWorkflow("wf", Arrays.asList(def));
        List<PvmProcessDefinition> definitions = builder.getRegistry().getDefinitions();
        Assert.assertEquals(definitions.size(), 1);
        return definitions.get(0);
    }

    public void hwlTwoFlows() {
        PvmProcessDefinition pdef = createHwlProcessDef();
        PvmProcessInstance instance = pdef.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        // two executions should be found
        assertActive(instance, 2);
        signalAllActiveExecutions(instance);
        assertEnded(instance);
    }

    public void testMultipleInstancesPerformance() {
        PvmProcessDefinition pdef = createHwlProcessDef();
        super.testMultipleInstancesPerformance("THREADS", pdef);
    }
}
