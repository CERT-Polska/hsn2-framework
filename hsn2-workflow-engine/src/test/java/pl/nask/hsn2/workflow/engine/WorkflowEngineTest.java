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
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.api.Message;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnectorException;
import pl.nask.hsn2.bus.connector.objectstore.StubObjectStoreConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnectorException;
import pl.nask.hsn2.bus.connector.process.StubProcessConnector;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.ObjectData;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowEngineException;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.framework.workflow.job.WorkflowJob;
import pl.nask.hsn2.utils.AtomicLongIdGenerator;

public class WorkflowEngineTest {
    ActivitiWorkflowEngine engine;
    ActivitiWorkflowDefinitionManager definitionManager;

    MyBus bus = new MyBus();

    @BeforeClass
    public void prepareWorkflows() throws WorkflowEngineException {
    	BusManager.setBus(bus);
        definitionManager = new ActivitiWorkflowDefinitionManager();

        definitionManager.registerWorkflow(
        		definitionManager.createDescritor("twoServices", "noFile", twoServicesWorkflow()));
        definitionManager.registerWorkflow(
        		definitionManager.createDescritor("withSubProcess", "noFile", oneServiceWithSubprocess()));
        definitionManager.registerWorkflow(
        		definitionManager.createDescritor("withEmptySubprocess", "noFile", oneServiceWithEmptySubprocess()));

        definitionManager.deploy("twoServices");
        definitionManager.deploy("withSubProcess");
        definitionManager.deploy("withEmptySubprocess");
    }



    private Workflow oneServiceWithSubprocess() {
        Workflow w = new Workflow();
        ProcessDefinition process = new ProcessDefinition("main");
        Service service = new Service("service");
        service.addOutput(new Output("subprocess"));
        process.addExecutionPoint(service);
        w.addProcess(process);

        ProcessDefinition subprocess = new ProcessDefinition("subprocess");
        subprocess.addExecutionPoint(new Service("subservice"));
        w.addProcess(subprocess);

        return w;
    }

    private Workflow oneServiceWithEmptySubprocess() {
        Workflow w = new Workflow();
        ProcessDefinition process = new ProcessDefinition("main");
        Service service = new Service("service");
        service.addOutput(new Output("subprocess"));
        process.addExecutionPoint(service);
        w.addProcess(process);

        ProcessDefinition subprocess = new ProcessDefinition("subprocess");
        w.addProcess(subprocess);

        return w;
    }

    private Workflow twoServicesWorkflow() {
        Workflow w = new Workflow();
        ProcessDefinition process = new ProcessDefinition("main");
        process.addExecutionPoint(new Service("service"));
        process.addExecutionPoint(new Service("service2"));
        w.addProcess(process );
        return w;
    }

    @BeforeMethod
    public void prepareEngine() {
        engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), new SingleThreadTasksSuppressor(true), 1);
    }

    @Test
    public void testStartJob() throws Exception {
        WorkflowDescriptor descriptor = definitionManager.get("twoServices");
        long jobId = engine.startJob(descriptor);
        WorkflowJob job = engine.getJob(jobId);
        Assert.assertEquals(JobStatus.PROCESSING, job.getStatus());
        Assert.assertFalse(job.isEnded());
        Assert.assertEquals(job.getActiveStepName(), "start");
    }

    @Test
    public void testAcceptTask() throws Exception  {
        WorkflowDescriptor descriptor = definitionManager.get("twoServices");
        long jobId = engine.startJob(descriptor);
        WorkflowJob job = engine.getJob(jobId);
        job.resume();
        Assert.assertEquals(job.getActiveStepName(), "service(service)");
        int taskId = bus.getLastTaskId();
        //int taskId = getLastTaskId();
        job.markTaskAsAccepted(taskId);
        // service name is still the same
        Assert.assertEquals(job.getActiveStepName(), "service(service)");
        // same as the taskId
        //Assert.assertEquals(taskId, getLastTaskId());
        Assert.assertEquals(taskId, bus.getLastTaskId());
    }

    @Test
    public void testCompleteTask() throws Exception {
        WorkflowDescriptor descriptor = definitionManager.get("twoServices");
        long jobId = engine.startJob(descriptor);
        WorkflowJob job = engine.getJob(jobId);
        job.resume();
        //int taskId = getLastTaskId();
        int taskId = bus.getLastTaskId();
        job.markTaskAsCompleted(taskId, null);
        Assert.assertEquals("service(service2)", job.getActiveStepName());
        //Assert.assertFalse(taskId == getLastTaskId());
        Assert.assertFalse(taskId == bus.getLastTaskId());
    }

    @Test
    public void testResumeMainProcess() throws Exception {
        WorkflowDescriptor descriptor = definitionManager.get("twoServices");
        long jobId = engine.startJob(descriptor);
        WorkflowJob job = engine.getJob(jobId);
        Assert.assertEquals("start", job.getActiveStepName());
        job.resume();
        Assert.assertEquals("service(service)", job.getActiveStepName());
    }

    @Test
    public void testCompleteSubprocessTask() throws Exception {
        WorkflowDescriptor descriptor = definitionManager.get("withSubProcess");
        long jobId = engine.startJob(descriptor);
        WorkflowJob job = engine.getJob(jobId);
        job.resume();
        //int taskId = getLastTaskId();
        int taskId = bus.getLastTaskId();
        job.markTaskAsCompleted(taskId, new HashSet<Long>(Arrays.asList(1L)));
        // the main process should stop on the wait state
        Assert.assertFalse(job.isEnded());
        int subtaskId = bus.getLastTaskId();
        //int subtaskId = getLastTaskId();
        job.markTaskAsCompleted(subtaskId, null);
        // this should trigger 'wait' in the main to end the process
        Assert.assertTrue(job.isEnded());
    }

    public static class MyBus implements FrameworkBus {
        private int lastTaskId = -1;
        private ObjectStoreConnector objectStoreConnector = new StubObjectStoreConnector(){
        	@Override
            public Long sendObjectStoreData(
            		long jobId, ObjectData dataList)
            				throws ObjectStoreConnectorException {
        		return 1L;
        	}
        	
        };
        
		public void sendHighPriorityMessage(Message message) {}
		public ObjectStoreConnector getObjectStoreConnector() {
			return this.objectStoreConnector;
		}
		public ProcessConnector getProcessConnector() {
			return new StubProcessConnector() {

				@Override
				public void sendTaskRequest(String serviceName,
						String serviceId, long jobId, int taskId,
						long objectDataId, Properties parameters)
						throws ProcessConnectorException {
					lastTaskId = taskId;
				}

			};
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}

		@Override
		public boolean isRunning() {
			return true;
		}
		@Override
		public void jobStarted(long jobId) {
		}
		@Override
		public void jobFinished(long jobId, JobStatus status) {
		}
		int getLastTaskId() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// nothing to do
			}
			return lastTaskId;
		}
		@Override public void jobFinishedReminder(long jobId, JobStatus status, int offendingTask) {
		}		
    }
}
