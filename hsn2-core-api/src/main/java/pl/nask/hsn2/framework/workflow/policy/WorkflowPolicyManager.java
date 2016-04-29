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

package pl.nask.hsn2.framework.workflow.policy;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * This is workflow policy manager.
 * 
 * Currently only on/off policy is allowed. In future any other
 * policy elements can be added to this manager.
 * 
 * Manager can be accessed by many threads so KEEP IT thread-safe.
 * 
 *
 */
public final class WorkflowPolicyManager {

	/**
	 * Internal set of disabled workflows.
	 */
	private static Set<String> disabledWorkflows = new  ConcurrentSkipListSet<String>();
	
	
	/**
	 * This is utility class and cannot be instantiated.
	 */
	private WorkflowPolicyManager() {
		// hidden constructor
	}

	/**
	 * Checks if workflow (identified by name) is enabled by policy.
	 * 
	 * @param workflowName Name of the workflow.
	 * @return <code>true</code> is workflow is enabled by policy.
	 */
	public static boolean isEnabledByPolicy(String workflowName) {
		return !disabledWorkflows.contains(workflowName);
	}

	/**
	 * Sets enable/disable policy for specified workflow.
	 * 
	 * @param workflowName Name of the workflow.
	 * @param enable On/Off policy to be set.
	 * @return Previous policy flag for the workflow.
	 */
	public static boolean setEnablePolicy(String workflowName, boolean enable) {
		boolean isEnabled = isEnabledByPolicy(workflowName);
		if (!enable && isEnabled) {
			disabledWorkflows.add(workflowName);
		}
		if (enable && !isEnabled) {
			disabledWorkflows.remove(workflowName);
		}
		return isEnabled;
	}
}
