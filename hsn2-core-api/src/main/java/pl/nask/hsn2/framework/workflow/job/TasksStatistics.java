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

package pl.nask.hsn2.framework.workflow.job;

import java.io.Serializable;
import java.util.Map;

/**
 * This is an interface to access statistics of
 * the tasks processing by a job.
 * 
 * This statistic interface can be implemented in various
 * ways depends on workflow engine. But most popular
 * probably is simple maps based <code>DefaultTasksStatistics</code>
 * implementation.
 * 
 *
 */
public interface TasksStatistics extends Serializable {

	/**
	 * Gets a map of started tasks. Each key in the map
	 * is running task name, the value for task name
	 * is number of subprocesses currently running.
	 * 
	 * @return Map of processing tasks.
	 */
	Map<String, Integer> getStarted();

	/**
	 * Gets a map of finished tasks. Each key in the map
	 * is finished task name, the value for task name
	 * is number of run processes.
	 * 
	 * @return Map of finished tasks.
	 */
    Map<String, Integer> getFinished();

	int getSubprocessesStarted();
	
	int getFreeTaskBufferSpacesCount();
	
	int getWaitingTasksRequestCount();

	/**
	 * Update stats by new started task or subprocess.
	 * 
	 * @param taskName
	 *            Started task name.
	 */
	void taskStarted(String taskName);

	/**
	 * Update stats by finished task or subprocess.
	 * 
	 * @param taskName
	 *            Finished task name.
	 */
	void taskCompleted(String taskName);

	void subprocessStarted();
	
	void updateSuppressorStats(int freeBufferSpaces, int waitingTasksNumber);
}
