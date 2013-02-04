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

import junit.framework.Assert;

import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.testng.annotations.Test;

import pl.nask.hsn2.activiti.behavior.WaitBehavior;
import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.suppressor.JobSuppressorHelperImpl;
import pl.nask.hsn2.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

public class WaitTest extends AbstractActivitiTest {

    // every process MUST end with the wait state (if the wait state is not provided, than it has to be added by the builder)
    @Test
    public void testWaitAddedByDefault() {
        ProcessDefinition pd = new ProcessDefinition("main");
        pd.addExecutionPoint(new Service("service"));

        ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(new BehaviorFactoryImpl());

        pd.transformToWorkflow(builder);

        PvmProcessDefinition def = builder.getRegistry().getDefinition("main");
        System.out.println(def.getActivities());
        List<? extends PvmActivity> activities = def.getActivities();
        ActivityImpl lastActivity = (ActivityImpl) activities.get(activities.size() -1);
        Assert.assertTrue(lastActivity.getActivityBehavior() instanceof WaitBehavior);
    }


    @Test
    public void testWaitHoldsMainProcess() {
        PvmProcessInstance instance = createAndStartProcess();
        ExecutionWrapper instanceWrapper = new ExecutionWrapper(instance);
        stats(instance);

        // signal to create a subprocess
        signalNamedExecution(instance, "act-1", "subProcess"); // signal mainService
        assertNotEnded(instance);
        assertProcessState(instance, "act-1");
        // check that the subprocess is created
        Assert.assertEquals(1, instanceWrapper.countActiveSubprocesses());

        // signaling the main process should not end it since the subprocess is still running
        // (the main process should stay in the 'wait' state)
        signalNamedExecution(instance, "act-1", "completeTask");
        assertProcessState(instance, "act-2");
        assertNotEnded(instance);
    }

    @Test
    public void testEndingSubprocessEndsWaitingProcess() {
        PvmProcessInstance instance = createAndStartProcess();
        ExecutionWrapper wrapper = new ExecutionWrapper(instance);
        // start subprocess, signal main process to reach 'wait', than signal the subprocess to end itself.
        signalNamedExecution(instance, "act-1", "subprocess");
        Assert.assertEquals("Number of subprocesses", 1, wrapper.countActiveSubprocesses());
        wrapper.signal("resume"); // resume subprocesses
        signalNamedExecution(instance, "act-1", "completeTask");
        assertNotEnded(instance); // just to make sure, that the process didn't end too early
        Assert.assertEquals("Number of subprocesses", 1, wrapper.countActiveSubprocesses());

        signalNamedExecution(instance, "act-3", "completeTask");
        Assert.assertEquals("Number of subprocesses", 0, wrapper.countActiveSubprocesses());
        assertEnded(instance);
    }



    private PvmProcessInstance createAndStartProcess() {
        // build process
        ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(new BehaviorFactoryImpl());

        Service mainService = new Service("mainService");
        mainService.addOutput(new Output("subprocess"));
        ProcessDefinition main = new ProcessDefinition("main");
        main.addExecutionPoint(mainService);

        ProcessDefinition subprocess = new ProcessDefinition("subprocess");
        subprocess.addExecutionPoint(new Service("subService"));

        Workflow w = new Workflow("testWorkflow1");
        w.addProcess(main);
        w.addProcess(subprocess);

        w.transformToWorkflow(builder);

        ProcessDefinitionRegistry<PvmProcessDefinition> registry = builder.getRegistry();

        // test:

        // start process
        PvmProcessDefinition mainDef = registry.getDefinition("main");
        PvmProcessInstance instance = mainDef.createProcessInstance();
        ExecutionWrapper instanceWrapper = new ExecutionWrapper(instance);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(0, 100, new SingleThreadTasksSuppressor());
        instanceWrapper.initProcessState(0, jobSuppressorHelper);
        instance.start();
        instance.signal("resume", null);
        return instance;
    }
}
