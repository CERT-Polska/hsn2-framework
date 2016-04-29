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

import pl.nask.hsn2.framework.workflow.hwl.Workflow;

/**
 * This is simple <code>WorkflowDescriptor</code> with no additional
 * structures.
 * 
 *
 */
public class DefaultWorkflowDescriptor implements WorkflowDescriptor {

	private static final long serialVersionUID = 2878387637708561687L;

	/**
	 * Identifier of a worklow.
	 */
	private final String id;

	/**
	 * Name of a workflow.
	 */
    private final String name;

    /**
     * Workflow used by this descriptor.
     */
    private final Workflow workflow;

    /**
     * Default constructor.
     * 
     * @param id Identifier of a worklow.
     * @param name Name of a workflow.
     * @param workflow Workflow used by this descriptor.
     */
    public DefaultWorkflowDescriptor(String id, String name, Workflow workflow) {
        this.id = id;
        this.name = name;
        this.workflow = workflow;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final boolean isParsed() {
        return workflow != null;
    }

    @Override
    public boolean isDeployed() {
        return true;
    }

    @Override
    public final String toString() {
        return this.getName() + " [id=" + id + ", name="
                + name + ", isParsed()=" + isParsed() + ", isDeployed()="
                + isDeployed() + "]";
    }

    @Override
    public final Workflow getWorkflow() {
        return workflow;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getDescription() {
    	return workflow != null? workflow.getDescription(): "";
    }
}
