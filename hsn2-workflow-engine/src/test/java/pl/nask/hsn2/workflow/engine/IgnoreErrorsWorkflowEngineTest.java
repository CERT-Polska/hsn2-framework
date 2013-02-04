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

import java.util.TreeSet;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.workflow.engine.WorkflowAlreadyDeployedException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowAlreadyRegisteredException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowEngineException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowNotRegisteredException;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.framework.workflow.job.WorkflowJob;
import pl.nask.hsn2.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.utils.AtomicLongIdGenerator;

public class IgnoreErrorsWorkflowEngineTest {
	private static final String ALL_ERRORS_IGNORED = "w1";
	private static final String NO_ERRORS_ALLOWED = "w2";
	private static final String TWO_PROCESSES_IGNORED = "w3";
	
	ActivitiWorkflowEngine activityEngine;
	ActivitiWorkflowDefinitionManager workflowManager ;

	WorkflowEngineTest.MyBus myBus;

	@Test(dataProvider="dataProvider")
	public void twoServicesAllErrorsIgnoredTest(TaskErrorReasonType err,String name) throws WorkflowEngineException {

		TaskErrorReasonType error = err;
		WorkflowDescriptor wd = workflowManager.get(ALL_ERRORS_IGNORED);

		final long jobId = activityEngine.startJob(wd);
		WorkflowJob job = activityEngine.getJob(jobId);
		job.resume();

		int taskId =  myBus.getLastTaskId();
		activityEngine.taskAccepted(jobId, taskId);
		
		Assert.assertEquals(job.getStatus(), JobStatus.PROCESSING);
		Assert.assertEquals(job.getActiveStepName(),"service(service1)");
		
		activityEngine.taskError(jobId, taskId,error , name);	
		
	
		Assert.assertEquals(job.getStatus(), JobStatus.PROCESSING);
		Assert.assertEquals(job.getActiveStepName(),"service(service2)");
		
		taskId = myBus.getLastTaskId();
		activityEngine.taskAccepted(jobId, taskId);
		activityEngine.taskCompleted(jobId, taskId, new TreeSet<Long>());
		
		Assert.assertEquals(job.getStatus(),JobStatus.COMPLETED);
		Assert.assertEquals(job.isEnded(), true);
	}

	
	
	
	
	@Test(dataProvider="dataProvider")
	public void noErrorsIgnored(TaskErrorReasonType err,String name) throws WorkflowEngineException {
		
		WorkflowDescriptor wd = workflowManager.get(NO_ERRORS_ALLOWED);
		
		TaskErrorReasonType error = err;
		final long jobId = activityEngine.startJob(wd);
		WorkflowJob job = activityEngine.getJob(jobId);
		job.resume();
		
		int taskId = myBus.getLastTaskId();
		activityEngine.taskAccepted(jobId, taskId);
		Assert.assertEquals(job.getActiveStepName(), "service(service1)");
		
		activityEngine.taskError(jobId, taskId, error, name);
		
		Assert.assertEquals(job.getStatus(),JobStatus.FAILED);
		Assert.assertEquals(job.getErrorMessage(), name);

	}
	
	
	
	@DataProvider(name="dataProvider")
	public Object[][] dataProvider() {
		
		Object[][] ret = new Object[TaskErrorReasonType.values().length][2];
		for (int i=0; i < TaskErrorReasonType.values().length; i++) {
			ret[i][0] = TaskErrorReasonType.values()[i];
			ret[i][1] = TaskErrorReasonType.values()[i].name();
		}
		
//		return new Object[][] {
//				{TaskErrorReasonType.DEFUNCT,TaskErrorReasonType.DEFUNCT.name()},
//				{TaskErrorReasonType.OBJ_STORE,TaskErrorReasonType.OBJ_STORE.name()},
//				{TaskErrorReasonType.DATA_STORE,TaskErrorReasonType.DATA_STORE.name()},
//				{TaskErrorReasonType.PARAMS,TaskErrorReasonType.PARAMS.name()},
//				{TaskErrorReasonType.RESOURCE,TaskErrorReasonType.RESOURCE.name()},
//				{TaskErrorReasonType.INPUT,TaskErrorReasonType.INPUT.name()}
//		};
		
		return ret;
	}
	
	
	
	@Test(dataProvider="dataProvider")
	public void twoProcessesWithIgnored(TaskErrorReasonType err,String n) throws WorkflowEngineException {
		
		WorkflowDescriptor wd = workflowManager.get(TWO_PROCESSES_IGNORED);
		final long jobId = activityEngine.startJob(wd);
		WorkflowJob job = activityEngine.getJob(jobId);
		job.resume();
		int taskId = myBus.getLastTaskId();
		activityEngine.taskAccepted(jobId, taskId);
		
		Assert.assertEquals(job.getStatus(), JobStatus.PROCESSING);
		Assert.assertEquals(job.getActiveStepName(),"service(service1)");
		
		activityEngine.taskError(jobId, taskId, err,n);
		Assert.assertEquals(job.getStatus(), JobStatus.COMPLETED);
	}
	
	
	
	@Test
	public void taskCompletedTwoProcessesReferenceTest() throws WorkflowEngineException {
		
		WorkflowDescriptor wd = workflowManager.get(TWO_PROCESSES_IGNORED);
		final long jobId = activityEngine.startJob(wd);
		WorkflowJob job = activityEngine.getJob(jobId);
		job.resume();
		Assert.assertEquals(job.getStatus(), JobStatus.PROCESSING);
		
		int taskId = myBus.getLastTaskId();
		Assert.assertEquals(job.getActiveStepName(),"service(service1)");
		activityEngine.taskAccepted(jobId, taskId);
		
		TreeSet<Long> set = new TreeSet<Long>(); set.add(1l);
		activityEngine.taskCompleted(jobId, taskId, set);
		Assert.assertEquals(job.getStatus(), JobStatus.PROCESSING);
		
		taskId = myBus.getLastTaskId();
		activityEngine.taskAccepted(jobId, taskId);
		activityEngine.taskCompleted(jobId, taskId, new TreeSet<Long>());
		Assert.assertEquals(job.getStatus(), JobStatus.COMPLETED);
		
	}


	@BeforeClass
	public void beforeClass() throws WorkflowEngineException {
		myBus = new WorkflowEngineTest.MyBus();
		BusManager.setBus(myBus);
		activityEngine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), new SingleThreadTasksSuppressor(), 1) ;
		workflowManager = new ActivitiWorkflowDefinitionManager();
		createWorkflows();
	}

	private void createWorkflows() throws WorkflowAlreadyRegisteredException,
	WorkflowAlreadyDeployedException, WorkflowNotRegisteredException {
		
		
		Workflow w = new Workflow();
		Service service = createServiceInstance("service1");
		ProcessDefinition process = new ProcessDefinition("main");
		process.addExecutionPoint(service);
		process.addExecutionPoint(new Service("service2"));
		w.addProcess(process);
		WorkflowDescriptor wd = workflowManager.createDescritor(ALL_ERRORS_IGNORED, "AnyErrors", w);
		workflowManager.registerWorkflow(wd);
		workflowManager.deploy(ALL_ERRORS_IGNORED);
		
		
		
		w = new Workflow();
		process = new ProcessDefinition("main");
		process.addExecutionPoint(new Service("service1"));
		process.addExecutionPoint(new Service("service2"));
		w.addProcess(process);
		wd = workflowManager.createDescritor(NO_ERRORS_ALLOWED, "NoErrors", w);
		workflowManager.registerWorkflow(wd);
		workflowManager.deploy(NO_ERRORS_ALLOWED);
		
		
		
		w = new Workflow();
		service = createServiceInstance("service1");
		service.addOutput(new Output("process1"));
		
		process = new ProcessDefinition("main");
		process.addExecutionPoint(service);
		w.addProcess(process);
		
		process = new ProcessDefinition("process1");
		service = createServiceInstance("service2");
		process.addExecutionPoint(service);
		w.addProcess(process);
		Assert.assertEquals(w.getProcessDefinitions().size() , 2);
		
		wd = workflowManager.createDescritor(TWO_PROCESSES_IGNORED, "TwoProcesses", w);
		workflowManager.registerWorkflow(wd);
		workflowManager.deploy(TWO_PROCESSES_IGNORED);
		
		
		Assert.assertEquals(workflowManager.getWorkflowDefinitions(true).size() , 3);
		
		
	}

	private Service createServiceInstance(String string) {
		Service service = new Service(string);
		for (TaskErrorReasonType t: TaskErrorReasonType.values()) {
			service.addIgnoreErrors(t);
		}

		return service;
	}

	
	@AfterClass
	public void afterClass() {
	}

}
