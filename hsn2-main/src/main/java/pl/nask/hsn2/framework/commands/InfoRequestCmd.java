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

import pl.nask.hsn2.bus.dispatcher.Command;
import pl.nask.hsn2.bus.dispatcher.CommandContext;
import pl.nask.hsn2.bus.dispatcher.CommandExecutionException;
import pl.nask.hsn2.bus.operations.InfoData;
import pl.nask.hsn2.bus.operations.InfoError;
import pl.nask.hsn2.bus.operations.InfoRequest;
import pl.nask.hsn2.bus.operations.InfoType;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.ObjectData;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.builder.ObjectDataBuilder;
import pl.nask.hsn2.framework.core.WorkflowManager;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;

public class InfoRequestCmd implements Command<InfoRequest> {

	@Override
	public Operation execute(CommandContext<InfoRequest> context)
			throws CommandExecutionException {
		
		WorkflowManager workflowManager = WorkflowManager.getInstance();
		
		WorkflowJobInfo workflowJobInfo = workflowManager.getJobInfo(context.getSourceOperation().getId());

		Operation operation = null;
		if (workflowJobInfo != null) {
			ObjectData objData = buildObjData(workflowJobInfo);
			operation = new InfoData(InfoType.JOB, objData);
		} else {
			operation = new InfoError(InfoType.JOB, "Job doesn't exist.");
		}
		return operation;
	}

	private ObjectData buildObjData(WorkflowJobInfo workflowJobInfo) {		
		ObjectDataBuilder objDataBuilder = new ObjectDataBuilder();
		objDataBuilder.addTimeAttribute("job_start_time",
						workflowJobInfo.getStartTime());

		int processingTime;
		if (workflowJobInfo.getEndTime() > 0) {
			objDataBuilder.addTimeAttribute("job_end_time",
    						workflowJobInfo.getEndTime());
            processingTime = (int) ((workflowJobInfo.getEndTime() - workflowJobInfo.getStartTime()) / 1000);
        } else {
            processingTime = (int) ((System.currentTimeMillis() - workflowJobInfo.getStartTime()) / 1000);
        }
		objDataBuilder.addIntAttribute("job_processing_time_sec", processingTime);

		objDataBuilder.addStringAttribute("job_status", workflowJobInfo.getStatus().name());
        if (workflowJobInfo.getStatus() != JobStatus.COMPLETED) {
        	objDataBuilder.addStringAttribute("job_active_step", workflowJobInfo.getActiveStepName());
        	objDataBuilder.addIntAttribute("job_active_subprocess_count", workflowJobInfo.getActiveSubtasksCount());
        }
        objDataBuilder.addIntAttribute("job_started_subprocess_count", workflowJobInfo.getTasksStatistics().getSubprocessesStarted());

        if (workflowJobInfo.getErrorMessage() != null) {
        	objDataBuilder.addStringAttribute("job_error_message", workflowJobInfo.getErrorMessage());
        }
        objDataBuilder.addStringAttribute("job_workflow_name", workflowJobInfo.getWorkflowName());
        objDataBuilder.addStringAttribute("job_workflow_revision", workflowJobInfo.getWorkflowRevision());
        objDataBuilder.addStringAttribute("job_custom_params", workflowJobInfo.getUserConfig().toString());

		objDataBuilder.addMaps("task_count_", workflowJobInfo
				.getTasksStatistics().getStarted(), workflowJobInfo
				.getTasksStatistics().getFinished());

		return objDataBuilder.build();
	}
}
