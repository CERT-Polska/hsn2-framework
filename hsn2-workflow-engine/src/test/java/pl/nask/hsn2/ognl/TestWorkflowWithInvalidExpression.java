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

package pl.nask.hsn2.ognl;

import junit.framework.Assert;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.MockedBus;
import pl.nask.hsn2.activiti.ActivitiWorkflowBuilder;
import pl.nask.hsn2.activiti.BehaviorFactory;
import pl.nask.hsn2.activiti.BehaviorFactoryImpl;
import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.engine.ProcessBasedWorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.hwl.Conditional;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionFlow;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;
import pl.nask.hsn2.utils.AtomicLongIdGenerator;
import pl.nask.hsn2.workflow.engine.ActivitiWorkflowEngine;

public class TestWorkflowWithInvalidExpression {
	private ActivitiWorkflowEngine engine = new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), new SingleThreadTasksSuppressor(true), 1);
	private BehaviorFactory behaviorFactory;
	/**
	 * Test for bug #6674. Workflow tested:
	 * 
<pre>
	<workflow name="bad">
		<process id="main">
			<conditional expr="findByName(&quot;abc&quot;).abc()">
				<true>
				</true>
			</conditional>
		</process>
	</workflow>
</pre>
 
	 This wokflow should end quickly
	 */
	@Test
	public void testInvalidConditionalExprAfterResume() throws Exception {
		// prepare workflow
		ProcessDefinition def = new ProcessDefinition("main");
		
		Conditional conditional = new Conditional("findByName(\"abc\")", new ExecutionFlow(), null);
		
		def.addExecutionPoint(conditional);
		Workflow w = new Workflow();
		w.addProcess(def);
		
		// run workflow
    	long jobId = runWorkflow(w);
    	
    	// verify job state (the job should be ended)
    	WorkflowJobInfo info = engine.getJobInfo(jobId);    	
    	Assert.assertEquals(JobStatus.COMPLETED, info.getStatus());
	}
	
	/**
	 * Test for bug #6674. Workflow tested:
	 * 
<pre>
	<workflow name="bad">
		<process id="main">           
            <service name="service" />     
			<conditional expr="findByName(&quot;abc&quot;).abc()">
				<true>
				</true>
			</conditional>
		</process>
	</workflow>
</pre>
 
	 This wokflow should end quickly
	 */
	@Test
	public void testInvalidConditionalExprAfterTaskCompleted() throws Exception {
		// prepare workflow
		ProcessDefinition def = new ProcessDefinition("main");
		
		Service service = new Service("service");
		def.addExecutionPoint(service);
		
		Conditional conditional = new Conditional("findByName(\"abc\")", new ExecutionFlow(), null);
		
		def.addExecutionPoint(conditional);
		Workflow w = new Workflow();
		w.addProcess(def);
		
		// run workflow
    	long jobId = runWorkflow(w);
    	
    	// verify job state (the job should be running)
    	WorkflowJobInfo info = engine.getJobInfo(jobId);    	
    	Assert.assertEquals(JobStatus.PROCESSING, info.getStatus());
    	
    	engine.taskCompleted(jobId, 0, null);
    	Assert.assertEquals(JobStatus.COMPLETED, info.getStatus());
	}
	
	
	private long runWorkflow(Workflow w) throws Exception {
		// run workflow
        ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(behaviorFactory );
        builder.buildWorkflow(w);
        ProcessBasedWorkflowDescriptor<PvmProcessDefinition> wdf = new ProcessBasedWorkflowDescriptor<PvmProcessDefinition>("w1", "w1", w);
        wdf.setProcessDefinitionRegistry(builder.getRegistry());
        
    	long jobId = engine.startJob(wdf);
    	engine.resume(jobId);
    	
    	return jobId;
	}
	
	@BeforeMethod
	public void initBusManager() {
		BusManager.setBus(new MockedBus());
		behaviorFactory = new BehaviorFactoryImpl();
	}
}
