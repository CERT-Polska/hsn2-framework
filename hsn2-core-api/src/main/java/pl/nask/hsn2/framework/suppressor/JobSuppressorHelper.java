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

package pl.nask.hsn2.framework.suppressor;

import java.util.Properties;

import pl.nask.hsn2.framework.workflow.job.TasksStatistics;

/**
 * Interface used while suppressing number of tasks done by single job.
 */
public interface JobSuppressorHelper {
	/**
	 * Sends task request to proper service if any of them is waiting to be sent, but only if there are free task buffer
	 * space. This is non blocking method.
	 */
	void tryToSendRequest();

	/**
	 * Adds task request to waiting list. Also notifies suppressor that job status changed.
	 * 
	 * @param serviceName
	 * @param serviceLabel
	 * @param taskId
	 * @param objectDataId
	 * @param serviceParameters
	 * @param stats
	 */
	void addTaskRequest(String serviceName, String serviceLabel, int taskId, long objectDataId, Properties serviceParameters,
			TasksStatistics stats);

	/**
	 * Sends info about task completion to main suppressor effecting with releasing free space for other's waiting tasks
	 * to be taken. Also notifies suppressor that job status changed.
	 * 
	 * @param jobId
	 *            Job ID.
	 * @param taskId
	 *            Task ID.
	 * @param stats
	 *            Job statistics.
	 */
	void signalTaskCompletion(Long jobId, Integer taskId, TasksStatistics stats);

	/**
	 * Provides actual tasks requests number waiting to be processed.
	 * 
	 * @return Tasks requests number.
	 */
	int getWaitingTasksRequestsCount();

	/**
	 * Provides actual free task buffer spaces.
	 * 
	 * @return Free task buffer spaces.
	 */
	int getFreeBuforSpacesCount();
}
