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

package pl.nask.hsn2.framework.workflow.engine;

import java.io.Serializable;

import pl.nask.hsn2.framework.workflow.hwl.Workflow;

/**
 * This is descriptor of single job to be run
 * in workflow engine. Depends on engine implementation
 * this descriptor can be implemented in various ways.
 * 
 * Basically many workflow engines need to do extra stuff
 * with a workflow before it will be ready to run a job
 * against of it. This is interface of some kind of container
 * which has a workflow and all internal structures needed
 * to run a job.
 * 
 * There are some assumptions:
 * a) there could be many descriptors for each workflow, but
 *    there could be only one descriptor for each version of
 *    the workflow,
 * b) the descriptor can be used many times e.g. for each job
 *    related to particular revision of a workflow, so descriptor
 *    must be thread-safe and do not keep a job states.
 * 
 *
 */
public interface WorkflowDescriptor extends Serializable {

	/**
	 * Gets identifier of the descriptor.
	 * 
	 * @return Identifier of the descriptor.
	 */
    String getId();

    /**
     * Gets name of a workflow associated with the descriptor.
     * 
     * @return Name of the workflow.
     */
    String getName();

    /**
     * Gets description of a workflow associated with the descriptor.
     * 
     * @return Description of the workflow.
     */
    String getDescription();

    /**
     * Gets workflow associated with the descriptor.
     * 
     * @return Workflow associated with the descriptor.
     */
    Workflow getWorkflow();

    /**
     * Checks if a workflow associated with the descriptor is parsable.
     * 
     * @return <code>true</code> if the workflow is parsable, <code>false</code> otherwise.
     */
    boolean isParsed();

    /**
     * Checks if descriptor is deployed.
     * 
     * @return <code>true</code> if descriptor is deployed, <code>false</code> otherwise.
     */
    boolean isDeployed();
}