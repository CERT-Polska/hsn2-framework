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

import java.util.List;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.workflow.engine.WorkflowAlreadyRegisteredException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowEngineException;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

@Test
public class WorkflowDefinitionManagerTest {

    ActivitiWorkflowDefinitionManager mgr;

    @Mocked
    private FrameworkBus bus;
    
    @Mocked
    private ObjectStoreConnector connector;

    @BeforeMethod
    public void beforeTest() {
    	new NonStrictExpectations() {
		 {
			 bus.getObjectStoreConnector();result=connector;
		 }	
    	};
    	BusManager.setBus(this.bus);
        mgr = new ActivitiWorkflowDefinitionManager();
    }

    @Test(groups="registerWorkflow")
    public void testRegisterBadWorkflow() throws WorkflowAlreadyRegisteredException {
    	WorkflowDescriptor def = mgr.createDescritor("id", "file", null);
        mgr.registerWorkflow(def);
        Assert.assertEquals(def.getId(), "id");
        Assert.assertFalse(def.isParsed());
        Assert.assertFalse(def.isDeployed());
    }

    @Test(groups="registerWorkflow")
    public void testRegisterGoodWorkflow() throws WorkflowAlreadyRegisteredException {
        WorkflowDescriptor def = mgr.createDescritor("id", "file", buildWorkflow());
        mgr.registerWorkflow(def);
        Assert.assertEquals(def.getId(), "id");
        Assert.assertTrue(def.isParsed());
        Assert.assertFalse(def.isDeployed());
    }

    @Test(dependsOnGroups="registerWorkflow")
    public void testGet() throws WorkflowAlreadyRegisteredException {
    	WorkflowDescriptor def = mgr.createDescritor("id", "file", null);
        mgr.registerWorkflow(def);
        def = mgr.get("id");
        Assert.assertEquals(def.getId(), "id");
        Assert.assertFalse(def.isParsed());
        Assert.assertFalse(def.isDeployed());
    }

    @Test(dependsOnGroups="registerWorkflow")
    public void testGetWorkflowDefinitions() throws WorkflowAlreadyRegisteredException {
    	WorkflowDescriptor def = mgr.createDescritor("id", "file", null);
        mgr.registerWorkflow(def);
        List<? extends WorkflowDescriptor> defs = mgr.getWorkflowDefinitions(false);
        Assert.assertEquals(defs.size(), 1);

        def = defs.get(0);
        Assert.assertEquals(def.getId(), "id");
        Assert.assertFalse(def.isParsed());
        Assert.assertFalse(def.isDeployed());
    }

    @Test(expectedExceptions=WorkflowAlreadyRegisteredException.class, groups = "registerWorkflow")
    public void testRegisterWorkflowTwice() throws WorkflowAlreadyRegisteredException {
    	WorkflowDescriptor def = mgr.createDescritor("id", "file", null);
        mgr.registerWorkflow(def);
        // should throw an exception since id shouldn't be used twice
        mgr.registerWorkflow(def);
    }

    @Test(dependsOnGroups="registerWorkflow", dependsOnMethods="testGet")
    public void testDeployWorkflow() throws WorkflowEngineException {
        String id = "id";
        WorkflowDescriptor def = mgr.createDescritor(id, "file", buildWorkflow());
        mgr.registerWorkflow(def);
        
        mgr.deploy(id);
        def = mgr.get(id);

        Assert.assertEquals(def.getId(), "id");
        Assert.assertTrue(def.isParsed());
        Assert.assertTrue(def.isDeployed());
    }

    @Test
    public void testUndeployWorkflow() throws WorkflowEngineException {
        String id = "id";
        WorkflowDescriptor def = mgr.createDescritor(id, "file", buildWorkflow());
        mgr.registerWorkflow(def);
        mgr.deploy(id);
        def = mgr.get(id);
        Assert.assertTrue(def.isDeployed());

        mgr.undeploy(id);

        def = mgr.get(id);
        Assert.assertFalse(def.isDeployed());

    }

    private Workflow buildWorkflow() {
        Workflow w = new Workflow("name");
        ProcessDefinition def = new ProcessDefinition("main");
        def.addExecutionPoint(new Service("service"));
        w.addProcess(def);

        return w;
    }


}
