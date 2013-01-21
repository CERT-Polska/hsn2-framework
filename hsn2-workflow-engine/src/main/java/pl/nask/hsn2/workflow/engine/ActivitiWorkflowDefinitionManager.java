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

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.activiti.ActivitiWorkflowBuilder;
import pl.nask.hsn2.activiti.BehaviorFactory;
import pl.nask.hsn2.activiti.BehaviorFactoryImpl;
import pl.nask.hsn2.framework.workflow.engine.MapBasedWorkflowDescriptorManager;
import pl.nask.hsn2.framework.workflow.engine.ProcessBasedWorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;
import pl.nask.hsn2.framework.workflow.engine.WorkflowAlreadyDeployedException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowNotDeployedException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowNotRegisteredException;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

public class ActivitiWorkflowDefinitionManager extends MapBasedWorkflowDescriptorManager<ProcessBasedWorkflowDescriptor<PvmProcessDefinition>> {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivitiWorkflowDefinitionManager.class);

    private final BehaviorFactory behaviorFactory;

    public ActivitiWorkflowDefinitionManager() {
        behaviorFactory = new BehaviorFactoryImpl();
    }

    @Override
    public void deploy(String id) throws WorkflowAlreadyDeployedException, WorkflowNotRegisteredException {
    	ProcessBasedWorkflowDescriptor<PvmProcessDefinition> desc = this.get(id);

        if (desc == null) {
        	throw new WorkflowNotRegisteredException(id);
        }

        if (!desc.isParsed()) {
            LOGGER.warn("Workflow with id={} was not parsed so it cannot be deployed", id);
            throw new WorkflowNotRegisteredException(id);
        }

        if (desc.isDeployed()) {
            LOGGER.warn("Workflow with id={} is already deployed", id);
            throw new WorkflowAlreadyDeployedException(id);
        }

        ActivitiWorkflowBuilder builder = new ActivitiWorkflowBuilder(behaviorFactory);

        builder.buildWorkflow(desc.getWorkflow());

        ProcessDefinitionRegistry<PvmProcessDefinition> deployedDefinitions = builder.getRegistry();
        desc.setProcessDefinitionRegistry(deployedDefinitions);
        LOGGER.info("Workflow deployed. Name: {}, id: {}", desc.getName(), id);
    }

    @Override
    public void undeploy(String id) throws WorkflowNotDeployedException {
    	ProcessBasedWorkflowDescriptor<PvmProcessDefinition> desc = get(id);
    	if (desc == null) {
    		throw new WorkflowNotDeployedException(id);
    	}
    	desc.setProcessDefinitionRegistry(null);
    }

	@Override
	public ProcessBasedWorkflowDescriptor<PvmProcessDefinition> createDescritor(
			String id, String workflowName, Workflow workflow) {
		return new ProcessBasedWorkflowDescriptor<PvmProcessDefinition>(id, workflowName, workflow);
	}

}
