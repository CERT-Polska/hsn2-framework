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

package pl.nask.hsn2.framework.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.dispatcher.Command;
import pl.nask.hsn2.bus.dispatcher.CommandContext;
import pl.nask.hsn2.bus.dispatcher.CommandExecutionException;
import pl.nask.hsn2.bus.operations.JobAccepted;
import pl.nask.hsn2.bus.operations.JobDescriptor;
import pl.nask.hsn2.bus.operations.JobRejected;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.framework.core.WorkflowManager;
import pl.nask.hsn2.framework.workflow.engine.WorkflowNotDeployedException;
import pl.nask.hsn2.framework.workflow.policy.WorkflowPolicyManager;

public final class JobDescriptorCmd implements Command<JobDescriptor> {

	private static final Logger LOGGER = LoggerFactory.getLogger(JobDescriptorCmd.class);
	
	private static final String ERROR_JOBS_LIMIT = "Maximum running jobs limit exceeded.";
	private static final String ERROR_WORKFLOW_NOT_DEPLOYED = "Workflow not deployed: ";
	private static final String ERROR_OTHER = "Error running workflow ";
	
	@Override
	public Operation execute(CommandContext<JobDescriptor> context)
			throws CommandExecutionException {

		// checks if workflow is enabled by policy
		if (!WorkflowPolicyManager.isEnabledByPolicy(context.getSourceOperation().getWorkflowId())) {
			return new JobRejected("Workflow is disabled by policy.");
		}
		
		// checks if maximum running job limit exceeded
		if (WorkflowManager.getInstance().isMaximumProcessingJobsLimitExeeded()) {
			LOGGER.warn(ERROR_JOBS_LIMIT);
			return new JobRejected(ERROR_JOBS_LIMIT);
		}
		
		try {
			long jobId = WorkflowManager.getInstance().runJob(
					context.getSourceOperation().getWorkflowId(),
					context.getSourceOperation().getServicesConfigs(),
					context.getSourceOperation().getWorkflowVersion());
			WorkflowManager.getInstance().resume(jobId);
			return new JobAccepted(jobId);
		} catch (WorkflowNotDeployedException e) {
			LOGGER.error(e.getMessage(), e);
			return new JobRejected(ERROR_WORKFLOW_NOT_DEPLOYED + e.getWorkflowId());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return new JobRejected(ERROR_OTHER + context.getSourceOperation().getWorkflowId());
		}
	}

}
