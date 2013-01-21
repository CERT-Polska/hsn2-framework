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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;

/**
 * This is main interface for managing execution of jobs against of workflows.
 * 
 * 
 */
public interface WorkflowEngine {

	/**
	 * Starts single job described by <code>descriptor</code> starting from
	 * <code>procesName</code> and with provided additional parameters.
	 * 
	 * @param descriptor
	 *            Descriptor of the job to be started.
	 * @param processName
	 *            Name of start process name (service).
	 * @param jobParameters
	 *            Additional parameters for this job processing.
	 * @return Identifier of created job.
	 * @throws WorkflowEngineException
	 *             Any problem with starting a job will rise the exception.
	 */
	long startJob(WorkflowDescriptor descriptor, String processName,
			Map<String, Properties> jobParameters)
			throws WorkflowEngineException;

	/**
	 * Starts single job described by <code>descriptor</code> starting from
	 * <code>processName</code> and with no additional parameters.
	 * 
	 * @param descriptor
	 *            Descriptor of the job to be started.
	 * @param processName
	 *            Name of start process name (service).
	 * @return Identifier of created job.
	 * @throws WorkflowEngineException
	 *             Any problem with starting a job will rise the exception.
	 */
	long startJob(WorkflowDescriptor descriptor, String processName)
			throws WorkflowEngineException;

	/**
	 * Starts single job described by <code>descriptor</code> starting from
	 * "main" process name and with no additional parameters.
	 * 
	 * @param descriptor
	 *            Descriptor of the job to be started.
	 * @return Identifier of created job.
	 * @throws WorkflowEngineException
	 *             Any problem with starting a job will rise the exception.
	 */
	long startJob(WorkflowDescriptor descriptor)
			throws WorkflowEngineException;

	/**
	 * Marks single task in specified job as accepted.
	 * 
	 * @param jobId
	 *            Identifier of the job.
	 * @param requestId
	 *            Identifier of the task.
	 */
    void taskAccepted(long jobId, int requestId);

    /**
	 * Marks single task in specified job as completed.
	 * 
	 * @param jobId
	 *            Identifier of the job.
	 * @param requestId
	 *            Identifier of the task.
	 * @param newObjects
	 *            Set of new objects created during processing the task. Can be
	 *            <code>null</code> if there are no new objects created.
	 */
    void taskCompleted(long jobId, int requestId, Set<Long> newObjects);

    /**
	 * Marks single task in specified job as error.
	 * 
	 * @param jobId
	 *            Identifier of the job.
	 * @param requestId
	 *            Identifier of the task.
	 * @param reason
	 *            Reason category.
	 * @param description
	 *            Detailed reason, can be <code>null</code>.
	 */
    void taskError(long jobId, int requestId, TaskErrorReasonType reason,
            String description);

    /**
	 * Gets a list of all jobs info (<code>WorkflowJobInfo</code>) stored in
	 * engine.
	 * 
	 * @return List of jobs info. Can be empty list but can NOT be
	 *         <code>null</code>.
	 */
    List<WorkflowJobInfo> getJobs();

    /**
	 * Gets a count of jobs with specified status.
	 * 
	 * @param status
	 *            Status of jobs to be found.
	 * @return Count of found jobs.
	 */
    int getJobsCount(JobStatus status);

    /**
	 * Gets single job with specified <code>jobId</code>.
	 * 
	 * @param jobId
	 *            Identifier of the job.
	 * @return Found job info or null if there is no job with specified
	 *         identifier. Also any internal problem will cause
	 *         <code>null</code>.
	 */
    WorkflowJobInfo getJobInfo(long jobId);

	/**
	 * Resumes paused job specified by <code>jobId</code>.
	 * 
	 * @param jobId
	 *            Identifier of the job to be resumed.
	 */
    void resume(long jobId);
}
