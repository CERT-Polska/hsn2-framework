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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is standard repository for all processes related to single job.
 * 
 * The repository is usually based on workflow definition and is strictly
 * depended on workflow engine implementation. So template <T> could
 * be used for different types of processes regarding to workflow engine.
 * 
 *
 * @param <T> Single process definition, usually common interface.
 */
public class ProcessDefinitionRegistry<T> {

	/**
	 * List of process definitions.
	 */
	private List<T> definitions = new ArrayList<T>();
	
	/**
	 * Map of the processes definitions. In this map
	 * the name of the process is mapped to process instance. 
	 */
    private Map<String, T> definitionsMap = new HashMap<String, T>();

    /**
	 * Adds process to the repository.
	 * 
	 * @param id
	 *            Identifier of the process.
	 * @param definition
	 *            Single process instance.
	 */
    public final void add(String id, T definition) {
        definitions.add(definition);
        definitionsMap.put(id, definition);
    }

    /**
	 * Gets single process definition associated with the name.
	 * 
	 * @param processName
	 *            Name of the process.
	 * @return Process if exists in the repository or null if process name
	 *         cannot be found.
	 */
    public final T getDefinition(String processName) {
        return definitionsMap.get(processName);
    }

    /**
     * Gets full list of defined processes.
     * 
     * @return List of processes.
     */
    public final List<T> getDefinitions() {
        return definitions;
    }
}
