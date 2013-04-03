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

package pl.nask.hsn2.workflow.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.api.Message;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnectorException;
import pl.nask.hsn2.bus.connector.objectstore.StubObjectStoreConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnector;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.ObjectData;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.core.WorkflowManager;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.engine.ProcessBasedWorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptorManager;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;
import pl.nask.hsn2.framework.workflow.repository.GitWorkflowRepository;
import pl.nask.hsn2.framework.workflow.repository.WorkflowRepoException;
import pl.nask.hsn2.utils.AtomicLongIdGenerator;
import pl.nask.hsn2.workflow.engine.ActivitiWorkflowDefinitionManager;
import pl.nask.hsn2.workflow.engine.ActivitiWorkflowEngine;

public class FullExampleTest {

    WorkflowParser parser = new HWLParser();
    WorkflowDescriptorManager<ProcessBasedWorkflowDescriptor<PvmProcessDefinition>> definitionManager;

    MyBus bus = new MyBus();

	@BeforeClass
    public void prepare() throws JAXBException, IOException, SAXException, ObjectStoreConnectorException {
        BusManager.setBus(bus);
        parser = new HWLParser();
        definitionManager = new ActivitiWorkflowDefinitionManager();
        WorkflowManager.setWorkflowDefinitionManager(definitionManager);
        WorkflowManager.setKnownServiceNames(new String[] {"feeder-list", "webclient"});
    }

    @Test
    public void parseFullExample() throws WorkflowSyntaxException, WorkflowParseException {
        Workflow w = parser.parse(new File("fullExample.hwl.xml"));
        Assert.assertNotNull(w);
    }

    @Test(dependsOnMethods="parseFullExample")
    public void registerFullExampleWorkflow() throws WorkflowSyntaxException, WorkflowParseException, WorkflowRepoException, FileNotFoundException {
        WorkflowManager.setWorkflowRepository(new GitWorkflowRepository("target/git-repo/", true));
        WorkflowManager.setWorkflowEngine(new ActivitiWorkflowEngine(new AtomicLongIdGenerator(), new SingleThreadTasksSuppressor(true), 1));
        InputStream is = new FileInputStream("fullExample.hwl.xml");
        Assert.assertNotNull("InputStream is null. Again.", is);
        WorkflowManager.getInstance().uploadWorkflow("workflow-1", is, true);
        Assert.assertEquals(1, WorkflowManager.getInstance().getWorkflowDefinitions(false).size());
        WorkflowDescriptor wd = WorkflowManager.getInstance().getWorkflowDefinitions(false).get(0);
        Assert.assertFalse(wd.isParsed());
        Assert.assertFalse(wd.isDeployed());
        Assert.assertEquals("workflow-1", wd.getName());
    }

    @Test(dependsOnMethods="registerFullExampleWorkflow")
    public void runFullExampleWorkflow() throws  Exception {
        long jobId = WorkflowManager.getInstance().runJob("workflow-1", null, null);
        List<WorkflowJobInfo> jobs = WorkflowManager.getInstance().getWorkflowJobs();
        Assert.assertEquals(1, jobs.size());
        Assert.assertEquals(jobId, jobs.get(0).getId());
        Assert.assertEquals(JobStatus.PROCESSING, jobs.get(0).getStatus());
    }
    
    public static class MyBus implements FrameworkBus {
        //private int lastTaskId = -1;
        private ObjectStoreConnector objectStoreConnector = new StubObjectStoreConnector(){
        	@Override
			public Long sendObjectStoreData(long jobId, ObjectData dataList)
					throws ObjectStoreConnectorException {
				return 1L;
			}
        };
        
		public void sendHighPriorityMessage(Message message) {}
		public ObjectStoreConnector getObjectStoreConnector() {
			return this.objectStoreConnector;
		}

		@Override
		public ProcessConnector getProcessConnector() {
			return null;
		}

		@Override public void start() { }
		@Override public void stop() { }
		@Override public boolean isRunning() { return true; }
		@Override public void jobStarted(long jobId) { }
		@Override public void jobFinished(long jobId, JobStatus status) { }
		@Override public void jobFinishedReminder(long jobId, JobStatus status, int offendingTask) {}
		@Override public void releaseResources() { }
		
    }
}
