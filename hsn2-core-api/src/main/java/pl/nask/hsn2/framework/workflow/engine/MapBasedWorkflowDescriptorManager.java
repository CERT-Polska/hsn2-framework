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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This is simple implementation of <code>WorkflowDescriptorManager</code>
 * based on Map. This manager must be thread-safe so ConcurrentHashMap is used.
 * 
 * 
 */
public abstract class MapBasedWorkflowDescriptorManager<T extends WorkflowDescriptor> implements WorkflowDescriptorManager<T> {
	
	/**
	 * Internal logger.
	 */
    private static final Logger LOGGER = LoggerFactory.getLogger(MapBasedWorkflowDescriptorManager.class);

    /**
     * Internal map contains descriptors.
     */
    private Map<String, T> descriptors = new ConcurrentHashMap<String, T>();

    @Override
    public final List<T> getWorkflowDefinitions(boolean deployedOnly) {
        if (deployedOnly) {
            List<T> res = new ArrayList<T>();
            for (T desc: this.descriptors.values()) {
                if (desc.isDeployed())
                    res.add(desc);
            }
            return res;
        } else {
            return new ArrayList<T>(this.descriptors.values());
        }
    }

    @Override
    public final T get(String id) {
        return this.descriptors.get(id);
    }

    @SuppressWarnings("unchecked")
	@Override
    public final void registerWorkflow(WorkflowDescriptor descriptor) throws WorkflowAlreadyRegisteredException {

    	if (descriptor == null) {
    		throw new IllegalArgumentException("Descriptor cannot be null!");
    	}
    	
    	String id = descriptor.getId();
    	
    	if (this.descriptors.containsKey(id)) {
            throw new WorkflowAlreadyRegisteredException(id);
        }

        this.descriptors.put(id, (T) descriptor);

        LOGGER.debug("Workflow registered. File: {}, id: {}", descriptor.getName(), id);
    }

    @Override
    public final void unregisterWorkflow(String id)
    		throws WorkflowNotRegisteredException {
    	if (!this.descriptors.containsKey(id)) {
    		throw new WorkflowNotRegisteredException(id);
    	}
    	this.descriptors.remove(id);
        LOGGER.debug("Workflow unregistered. id: {}", id);
    }
}
