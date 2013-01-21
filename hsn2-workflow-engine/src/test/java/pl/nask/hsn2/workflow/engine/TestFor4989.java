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

import java.util.Arrays;
import java.util.HashSet;

import junit.framework.Assert;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pl.nask.hsn2.MockedBus;
import pl.nask.hsn2.activiti.ActivitiWorkflowBuilder;
import pl.nask.hsn2.activiti.BehaviorFactory;
import pl.nask.hsn2.activiti.BehaviorFactoryImpl;
import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnectorException;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.framework.workflow.engine.ProcessBasedWorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowNotDeployedException;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.framework.workflow.job.WorkflowJob;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;
import pl.nask.hsn2.utils.AtomicLongIdGenerator;

/**
 * Test for bug #4989 (https://drotest4.nask.net.pl:3000/issues/4989)
 *
<pre>
ERROR - 2011-10-04 10:15:29,397 [CommandDispatcher] ERROR pl.nask.hsn2.framework.core.CommandDispatcher - Error executiong command
java.lang.IllegalStateException: Execution for taskId=4 cannot be found. The task may be already completed.
at pl.nask.hsn2.workflow.engine.ActivitiJob.newSubprocess(ActivitiJob.java:145)
at pl.nask.hsn2.workflow.engine.ActivitiWorkflowEngine.newSubprocess(ActivitiWorkflowEngine.java:78)
at pl.nask.hsn2.framework.core.WorkflowManager.newSubprocess(WorkflowManager.java:170)
at pl.nask.hsn2.framework.core.commands.ObjectAddedCmd.doExecute(ObjectAddedCmd.java:46)
at pl.nask.hsn2.framework.core.commands.AbstractCommand.execute(AbstractCommand.java:24)
at pl.nask.hsn2.framework.core.CommandDispatcher.run(CommandDispatcher.java:30)
at java.lang.Thread.run(Thread.java:679)

I happens when system has more then one webclient.
</pre>

 * Since there was only one worker thread, this is not a multithreading issue.
 * It can be reproduced by creating some subprocesses and completing their tasks, but in different order the subprocesses were created.
 *
 */
public class TestFor4989 {
	
    ProcessBasedWorkflowDescriptor<PvmProcessDefinition> workflowDefinitionDescriptorImpl;
    
    MockedBus myBus = new MockedBus();
    
    @BeforeClass
    public void before() throws ObjectStoreConnectorException {
        ProcessDefinition mainDef = new ProcessDefinition("main");
        Service mainService = new Service("mainService");
        mainService.addOutput(new Output("subprocess"));
        mainDef.addExecutionPoint(mainService);

        ProcessDefinition subprocess= new ProcessDefinition("subprocess");
        Service subService = new Service("subService");
        subprocess.addExecutionPoint(subService);

        Workflow workflow= new Workflow("testWorkflow");
        workflow.addProcess(mainDef );
        workflow.addProcess(subprocess);

        BusManager.setBus(myBus);

        BehaviorFactory behaviorFactory = new BehaviorFactoryImpl();
        ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(behaviorFactory );
        builder.buildWorkflow(workflow);

        ProcessBasedWorkflowDescriptor<PvmProcessDefinition> wdf = new ProcessBasedWorkflowDescriptor<PvmProcessDefinition>("w1", "w1", workflow);

        wdf.setProcessDefinitionRegistry(builder.getRegistry());
        workflowDefinitionDescriptorImpl = wdf;
    }

    /**
     * this test should pass even if the bug is present
     * @throws WorkflowNotDeployedException
     */
    @Test
    public void testSameSequence() throws Exception {
        ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator());
        long jobId = engine.startJob(workflowDefinitionDescriptorImpl);
        engine.resume(jobId);
        myBus.requests.clear();
        // assume taskId
        int taskId = 0;

        // spawn subprocesses
        engine.taskCompleted(jobId, taskId, new HashSet<Long>(Arrays.asList(0L, 1L, 2L, 3L)));
        WorkflowJobInfo info = engine.getJobInfo(jobId);
        Assert.assertEquals(4, info.getActiveSubtasksCount());
        Assert.assertEquals(4, myBus.requests.size());

        // complete subtasks in the same order, as they were run
        engine.taskCompleted(jobId, myBus.requests.get(0).getTaskId(), null);
        engine.taskCompleted(jobId, myBus.requests.get(1).getTaskId(), null);
        engine.taskCompleted(jobId, myBus.requests.get(2).getTaskId(), null);
        engine.taskCompleted(jobId, myBus.requests.get(3).getTaskId(), null);

        assertJobCompleted(engine.getJob(jobId));
    }

    private void assertJobCompleted(WorkflowJob mainJob) {
        Assert.assertEquals(0, mainJob.getActiveSubtasksCount());
        Assert.assertEquals(JobStatus.COMPLETED, mainJob.getStatus());
    }


    /**
     * this test should pass only, if the bug is fixed
     * @throws WorkflowNotDeployedException
     */
	@Test
    public void testBugFix() throws Exception {
        ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator());
        long jobId = engine.startJob(workflowDefinitionDescriptorImpl);
        engine.resume(jobId);
        myBus.requests.clear();
        // assume taskId
        int taskId = 0;

        engine.taskCompleted(jobId, taskId, new HashSet<Long>(Arrays.asList(0L, 1L, 2L, 3L)));

        WorkflowJobInfo info = engine.getJobInfo(jobId);
        Assert.assertEquals(4, info.getActiveSubtasksCount());
        Assert.assertEquals(4, myBus.requests.size());
        // signal main process

        // complete tasks in mixed order
        engine.taskCompleted(jobId, myBus.requests.get(1).getTaskId(), null);
        engine.taskCompleted(jobId, myBus.requests.get(0).getTaskId(), null);
        engine.taskCompleted(jobId, myBus.requests.get(3).getTaskId(), null);
        engine.taskCompleted(jobId, myBus.requests.get(2).getTaskId(), null);

        assertJobCompleted(engine.getJob(jobId));
    }
}
