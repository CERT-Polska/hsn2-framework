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

/**
 * This is abstract <code>WorkflowEngineException</code> exception
 * for group of exceptions realted to particular workflow.
 * 
 *
 */
public abstract class WorkflowIdentifiedException extends WorkflowEngineException {

	private static final long serialVersionUID = -3385064660015329917L;

	private final String workflowId;

    public WorkflowIdentifiedException(String workflowId) {
    	super();
        this.workflowId = workflowId;
    }

    public WorkflowIdentifiedException(String workflowId, Throwable e) {
        super(workflowId, e);
        this.workflowId = workflowId;
    }

    public final String getWorkflowId() {
        return workflowId;
    }
}
