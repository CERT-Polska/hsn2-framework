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

import pl.nask.hsn2.framework.workflow.hwl.Workflow;

/**
 * This is implementation of <code>WorkflowDescriptor</code> which
 * can additionally store some kind of execution structures. Usually
 * it will be a registry of processes.
 * 
 *
 * @param <T> Type of a single process.
 */
public class ProcessBasedWorkflowDescriptor<T> extends DefaultWorkflowDescriptor {

    private ProcessDefinitionRegistry<T> processDefinitionRegistry = null;

    public ProcessBasedWorkflowDescriptor(String id, String name, Workflow workflow) {
    	super (id, name, workflow);
    }

    @Override
    public final boolean isDeployed() {
        return processDefinitionRegistry != null;
    }

    public final void setProcessDefinitionRegistry(ProcessDefinitionRegistry<T> deployedDefinitions) {
        this.processDefinitionRegistry = deployedDefinitions;
    }

    
    public final T getProcessDefinition(String processName) {
    	return processDefinitionRegistry.getDefinition(processName);
    }
}
