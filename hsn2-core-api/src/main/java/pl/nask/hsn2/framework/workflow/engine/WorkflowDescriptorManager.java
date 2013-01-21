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

package pl.nask.hsn2.framework.workflow.engine;

import java.util.List;

import pl.nask.hsn2.framework.workflow.hwl.Workflow;

/**
 * This is an interface to manage a <code>WortkflowDescriptor</code>s.
 *
 * NOTE: Implementation of this manager can be used by many threads
 *       so keep them thread-safe!
 *
 */
public interface WorkflowDescriptorManager<T extends WorkflowDescriptor> {

	/**
	 * Gets a list of <code>WorkflowDescriptor</code>s known by the manager.
	 * 
	 * @param deployedOnly
	 *            <code>true</code> if only correctly deployed descriptors
	 *            should be returned, <code>false</code> if we want to get all
	 *            known descriptors.
	 *
	 * @return List of <code>WorkflowDescriptor</code>s.
	 */
    List<T> getWorkflowDefinitions(boolean deployedOnly);

	/**
	 * Deploys descriptor identified by <code>id</code>.
	 * The descriptor must be registered first.
	 * 
	 * @param id
	 *            Identifier of the descriptor to be deployed.
	 *
	 * @throws WorkflowAlreadyDeployedException
	 *             if descriptor is already deployed the exception will thrown.
	 */
    void deploy(String id) throws WorkflowAlreadyDeployedException, WorkflowNotRegisteredException;

	/**
	 * Gets a <code>WorkflowDescriptor</code> identified by <code>id</code>.
	 * 
	 * @param id
	 *            Identifier of the descriptor to get.
	 *
	 * @return Descriptor or null if cannot be found.
	 */
	T get(String id);

	/**
	 * Undeploys descriptor identified by <code>id</code>.
	 * 
	 * @param id
	 *            Identifier of the descriptor to be undeployed.
	 *
	 * @throws WorkflowNotDeployedException
	 *             if descriptor is not deployed the exception will thrown.
	 */
	void undeploy(String id) throws WorkflowNotDeployedException;

	/**
	 * Registers descriptor identified by <code>id</code>.
	 * 
	 * @param descriptor
	 *            Descriptor to be registered.
	 * 
	 * @throws WorkflowAlreadyRegisteredException
	 *             Id descriptor is already registered the exception will
	 *             thrown.
	 */
	void registerWorkflow(WorkflowDescriptor descriptor)
			throws WorkflowAlreadyRegisteredException;

    /**
	 * Unregisters descriptor.
	 * 
	 * @param id
	 *            Identifier of the descriptor.
	 * @throws WorkflowNotRegisteredException
	 *             If descriptor is not registered the exception will thrown.
	 */
	void unregisterWorkflow(String id) throws WorkflowNotRegisteredException;
	
	/**
	 * Creates <code>WorkflowDefinition</code> which will be managed by the
	 * manager.
	 * 
	 * @param id
	 *            Identifier of the descriptor.
	 * @param workflowName
	 *            Workflow name.
	 * @param workflow
	 *            Workflow associated with the descriptor.
	 *
	 * @return <code>WorkflowDescriptr</code> implementation.
	 */
	T createDescritor(String id, String workflowName, Workflow workflow);
}
