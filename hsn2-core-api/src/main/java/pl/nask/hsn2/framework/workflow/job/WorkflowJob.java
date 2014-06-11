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

package pl.nask.hsn2.framework.workflow.job;

import java.util.Set;

import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.suppressor.JobSuppressorHelper;

/**
 * This is job management operations interface.
 * 
 *
 */
public interface WorkflowJob extends WorkflowJobInfo {
	/**
	 * Starts the job and gives them provided identifier.
	 * 
	 * @param jobId Identifier for the job.
	 */
    void start(long jobId, JobSuppressorHelper jobSuppressorHelper);

    /**
     * Marks specified task as accepted by a service.
     * 
     * @param requestId Accepted task identifier.
     */
    void markTaskAsAccepted(int requestId);

	/**
	 * Marks specified task as completed by a service.
	 * 
	 * @param requestId
	 *            Completed task identifier.
	 * @param newObjects
	 *            Set of objects identifiers created during processing the task
	 *            by a service.
	 */
    void markTaskAsCompleted(int requestId, Set<Long> newObjects);

    /**
     * Marks specified task as failed in case of fail processing it by a services.
     * 
     * @param requestId Failed task identifier.
     * @param reason Category reason why task failed.
     * @param description Detailed description why task failed.
     */
    void markTaskAsFailed(int requestId, TaskErrorReasonType reason, String description);

    /**
     * Resume the job.
     */
    void resume();
    /**
     * Abort the job.
     */
	void cancel();
}
