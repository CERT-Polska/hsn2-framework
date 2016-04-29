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

package pl.nask.hsn2.workflow.engine;

import java.util.Collections;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.MockedBus;
import pl.nask.hsn2.activiti.AbstractActivitiTest;
import pl.nask.hsn2.activiti.ActivitiWorkflowBuilder;
import pl.nask.hsn2.activiti.BehaviorFactory;
import pl.nask.hsn2.activiti.BehaviorFactoryImpl;
import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.operations.ObjectData;
import pl.nask.hsn2.bus.operations.TaskRequest;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.engine.ProcessBasedWorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;
import pl.nask.hsn2.utils.AtomicLongIdGenerator;

@Test(enabled=true)
public class HwlOutputWaitTest extends AbstractActivitiTest {
	private MockedBus myBus;
	
	@BeforeClass
	public void prepareTestClass() {
		myBus = new MockedBus();
		BusManager.setBus(myBus);
	}
	
	@BeforeMethod
	public void prepareTest() {
		myBus.requests.clear();
	}

    public void serviceWithSimpleOutput() throws Exception {
    	ProcessBasedWorkflowDescriptor<PvmProcessDefinition> wdf = buildWorkflowDefinition(null);
    	SingleThreadTasksSuppressor suppressor = new SingleThreadTasksSuppressor(true);
    	ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), suppressor, 1);
    	long jobId = engine.startJob(wdf);
    	engine.resume(jobId);    	
    	WorkflowJobInfo jobInfo = engine.getJobInfo(jobId);    	
    	Assert.assertFalse(jobInfo.isEnded(), "job should be running and waiting for TaskCompleted");
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "job shoud have no subprocesses running");
        
    	// trigger creation of a subprocess
    	TaskRequest req = myBus.requests.take();
    	int taskId = req.getTaskId();
    	engine.taskCompleted(jobId, taskId, Collections.singleton(1000L));
        // subprocesses should be created
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 1, "one subprocess should be running");
    	
        // terminate subprocess
    	req = myBus.requests.take();
    	engine.taskCompleted(jobId, req.getTaskId(), null);
        Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "job shoud have no subprocesses running");
        
        Assert.assertTrue(jobInfo.isEnded(), "job should be ended");
    }


    public void recursiveOutput() throws Exception {
    	ProcessBasedWorkflowDescriptor<PvmProcessDefinition> wdf = buildWorkflowDefinition(null);
    	SingleThreadTasksSuppressor suppressor = new SingleThreadTasksSuppressor(true);
    	ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), suppressor, 1);
    	long jobId = engine.startJob(wdf);
    	engine.resume(jobId);    	
    	WorkflowJobInfo jobInfo = engine.getJobInfo(jobId);    	
    	Assert.assertFalse(jobInfo.isEnded(), "job should be running and waiting for TaskCompleted");
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "job shoud have no subprocesses running");
        
    	// trigger creation of a subproces
    	TaskRequest req = myBus.requests.take();
    	int taskId = req.getTaskId();
    	engine.taskCompleted(jobId, taskId, Collections.singleton(1000L));
        // subprocesses should be created
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 1, "one subprocess should be running");
    	
    	// trigger creation of sub-subprocess
    	req = myBus.requests.take();
    	taskId = req.getTaskId();
    	engine.taskCompleted(jobId, taskId, Collections.singleton(1001L));
        // subprocess should be created
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 2, "2 subprocesses should be running");
    	
        // terminate subprocess
    	req = myBus.requests.take();
    	engine.taskCompleted(jobId, req.getTaskId(), null);
        Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "job shoud have no subprocesses running");
        
        Assert.assertTrue(jobInfo.isEnded(), "job should be ended");                
    }
    
    public void outputConditionFalse() throws Exception {
    	ProcessBasedWorkflowDescriptor<PvmProcessDefinition> wdf = buildWorkflowDefinition("id==1000");
    	SingleThreadTasksSuppressor suppressor = new SingleThreadTasksSuppressor(true);
    	ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), suppressor, 1);
    	long jobId = engine.startJob(wdf);
    	engine.resume(jobId);    	
    	WorkflowJobInfo jobInfo = engine.getJobInfo(jobId);    	
    	Assert.assertFalse(jobInfo.isEnded(), "job should be running and waiting for TaskCompleted");
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "job shoud have no subprocesses running");
    	
    	ObjectData obj = new ObjectData();
    	obj.setId(1001L);
		myBus.addObject(1001L, obj );
    	// trigger creaction of a subproces
    	TaskRequest req = myBus.requests.take();
    	int taskId = req.getTaskId();
    	engine.taskCompleted(jobId, taskId, Collections.singleton(1001L));
        // subprocesses should be created
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "condition should resolve to false, output should not creata a subprocess");
    	       
        Assert.assertTrue(jobInfo.isEnded(), "job should be ended");                
    }
    
    public void outputConditionTrue() throws Exception {
    	ProcessBasedWorkflowDescriptor<PvmProcessDefinition> wdf = buildWorkflowDefinition("id==1000");
    	SingleThreadTasksSuppressor suppressor = new SingleThreadTasksSuppressor(true);
    	ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), suppressor, 1);
    	long jobId = engine.startJob(wdf);
    	engine.resume(jobId);    	
    	WorkflowJobInfo jobInfo = engine.getJobInfo(jobId);    	
    	Assert.assertFalse(jobInfo.isEnded(), "job should be running and waiting for TaskCompleted");
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "job shoud have no subprocesses running");
        
    	// trigger creaction of a subproces
    	TaskRequest req = myBus.requests.take();
    	ObjectData obj = new ObjectData();
    	obj.setId(1000L);
		myBus.addObject(1000L, obj );
    	int taskId = req.getTaskId();
    	engine.taskCompleted(jobId, taskId, Collections.singleton(1000L));
        // subprocesses should be created
    	Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 1, "output condition should resolve to true, so one process should be created");
    	
        // terminate subprocess
    	req = myBus.requests.take();
    	engine.taskCompleted(jobId, req.getTaskId(), null);
        Assert.assertEquals(jobInfo.getActiveSubtasksCount(), 0, "job shoud have no subprocesses running");
        
        Assert.assertTrue(jobInfo.isEnded(), "job should be ended");                
    }   

    private ProcessBasedWorkflowDescriptor<PvmProcessDefinition> buildWorkflowDefinition(String conditionForOutput) {
            ProcessDefinition mainDef = new ProcessDefinition("main");
            Service mainService = new Service("mainService");
            mainService.addOutput(new Output("subprocess", conditionForOutput));
            mainDef.addExecutionPoint(mainService);

            ProcessDefinition subprocess= new ProcessDefinition("subprocess");
            Service subService = new Service("subservice");
            subService.addOutput(new Output("subprocess", conditionForOutput));
            subprocess.addExecutionPoint(subService);

            Workflow workflow= new Workflow("testWorkflow");
            workflow.addProcess(mainDef );
            workflow.addProcess(subprocess);

            BehaviorFactory behaviorFactory = new BehaviorFactoryImpl();
            ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(behaviorFactory );
            builder.buildWorkflow(workflow);

            ProcessBasedWorkflowDescriptor<PvmProcessDefinition> wdf = new ProcessBasedWorkflowDescriptor<PvmProcessDefinition>("w1", "w1", workflow);

            wdf.setProcessDefinitionRegistry(builder.getRegistry());
           return wdf;
    }
}
