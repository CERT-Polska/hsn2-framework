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

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.testng.Assert;
import org.testng.annotations.Test;

import pl.nask.hsn2.activiti.behavior.ServiceBehavior;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.suppressor.JobSuppressorHelperImpl;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

@Test
public class StartingSubprocessesTest extends AbstractActivitiTest {

    @Test
    public void testNoJoins() {
        ProcessDefinitionRegistry<PvmProcessDefinition> registry = new ProcessDefinitionRegistry<PvmProcessDefinition>();
        ExtendedProcessDefinitionImpl process = simpleProcessWithOutputs(registry, "main", new Output("subprocess"));
        registry.add(process.getId(), process);
        process = pvmFactory.createSimpleProcess("subprocess");
        registry.add(process.getId(), process);

        PvmProcessDefinition def = registry.getDefinition("main");
        PvmProcessInstance instance = def.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(0, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(0, jobSuppressorHelper);
        instance.start();
        assertNotEnded(instance);
        assertActive(instance, 1);
        Assert.assertEquals(wrapper.countActiveSubprocesses(), 0);

        // this should spawn a new subprocess - a separate process instance which is linked to it's parent execution
        signalActiveExecution(instance, "subProcess");
        assertNotEnded(instance);
        // there should be 2 executions
        assertActive(instance, 2);
        Assert.assertEquals(wrapper.countActiveSubprocesses(), 1);

        // this should end a subprocess
        signalNamedExecution(instance, "service", "completeTask");
        // and again one active process
        assertActive(instance, 1);
        // and no active subprocesses
        Assert.assertEquals(wrapper.countActiveSubprocesses(), 0);
        signalNamedExecution(instance, "newService", "completeTask");
        assertActive(instance, 1);
        signalNamedExecution(instance, "lastService", "completeTask");
        assertActive(instance, 0);
        assertEnded(instance);
    }

    private ExtendedProcessDefinitionImpl simpleProcessWithOutputs(ProcessDefinitionRegistry<PvmProcessDefinition> registry, String processName, Output output) {
        ExtendedProcessDefinitionBuilder builder = pvmFactory.newProcessDefinitionBuilder(processName);
        pvmFactory.addStart(builder, "newService");
        ActivityBehavior newProcessBehavior = new ServiceBehavior("testService", null, null, Arrays.asList(output), null, registry,null);
        builder.createActivity("newService").behavior(newProcessBehavior).transition("lastService").endActivity();
        pvmFactory.addSignallable(builder, "lastService", "end");
        pvmFactory.addEnd(builder);
        return builder.buildProcessDefinition();
    }
}
