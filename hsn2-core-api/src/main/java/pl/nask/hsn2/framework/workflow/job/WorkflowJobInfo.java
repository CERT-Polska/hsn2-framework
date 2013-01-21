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

import java.util.Map;
import java.util.Properties;

import pl.nask.hsn2.bus.operations.JobStatus;

/**
 * This interfaces represents single job in the workflow engine.
 * 
 *
 */
public interface WorkflowJobInfo {

    /**
     * Gets identifier of the job.
     * 
     * @return ID of the WorkflowJob in the system
     */
    long getId();

	/**
	 * Checks if job has finished.
	 * 
	 * @return <code>true</code> if the job was finished, <code>false</code>
	 *         otherwise.
	 * 
	 */
    boolean isEnded();

    /**
     * Gets current status of the job.
     * 
     * @return Current status of the job.
     */
    JobStatus getStatus();

    /**
     * Gets current step processing in the job.
     * 
     * @return The name of the active step in the 'main' process
     */
    String getActiveStepName();

    /**
     * Gets time of job started.
     * 
     * @return The timestamp generated when the job was started
     */
    long getStartTime();

    /**
	 * Gets time when job finished.
	 * 
	 * @return The timestamp generated when the job was finished. <code>0</code>
	 *         if job is still under processing.
	 */
    long getEndTime();

    /**
     * Gets user configuration provided to the job.
     * 
     * @return Configuration provided when the job was started.
     */
    Map<String, Properties> getUserConfig();

    /**
     * Gets more detailed error message when job failed or aborted.
     * 
     * @return Error message.
     */
    String getErrorMessage();

    /**
     * Gets current number of active subtasks.
     * 
     * @return The number of job's active subtasks
     */
    int getActiveSubtasksCount();

    /**
     * Gets a name of workflow which job's run against of.
     * 
     * @return Name of the workflow.
     */
    String getWorkflowName();

    /**
     * Gets a revision number of workflow which job's run againt of.
     * 
     * @return Revision of the workflow.
     */
    String getWorkflowRevision();
    
	/**
	 * Gets current tasks statistics for the job.
	 * 
	 * @return The map <taskName, counter> with the counters indicating, how
	 *         many tasks of the given type were created
	 */
    TasksStatistics getTasksStatistics();
}
