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
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.WorkflowError;
import pl.nask.hsn2.bus.operations.WorkflowStatusRequest;
import pl.nask.hsn2.bus.operations.builder.WorkflowRevisionInfoBuilder;
import pl.nask.hsn2.bus.operations.builder.WorkflowStatusReplyBuilder;
import pl.nask.hsn2.framework.core.WorkflowManager;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.policy.WorkflowPolicyManager;
import pl.nask.hsn2.framework.workflow.repository.WorkflowRepoException;
import pl.nask.hsn2.framework.workflow.repository.WorkflowVersionInfo;

public final class WorkflowStatusRequestCmd implements Command<WorkflowStatusRequest> {

	@Override
	public Operation execute(CommandContext<WorkflowStatusRequest> context)
			throws CommandExecutionException {

		try {

			WorkflowDescriptor workflowDesc = WorkflowManager.getInstance()
					.getWorkflowDefinition(
							context.getSourceOperation().getName(),
							context.getSourceOperation().getRevision());
			
			WorkflowVersionInfo version = WorkflowManager.getInstance()
					.getWorkflowVersionInfo(
							context.getSourceOperation().getName(),
							context.getSourceOperation().getRevision());

			// it should not happen, but in case
			if (version == null) {
				return new WorkflowError("Workflow version cannot be obtained.");
			}

			// checks is workflow is enabled by policy
			boolean enabled = WorkflowPolicyManager.isEnabledByPolicy(context.getSourceOperation().getName());
					
			WorkflowStatusReplyBuilder wsrb = new WorkflowStatusReplyBuilder(workflowDesc.isParsed(), enabled,
					new WorkflowRevisionInfoBuilder(version.getVersion(), version.getVersionTimestamp()).build());
			
			wsrb.setDescription(workflowDesc.getDescription());
			
			return wsrb.build();

		} catch (WorkflowRepoException e) {
			return new WorkflowError(e.getMessage());
		} catch (Exception e) { // just in case of internal problems
			return new WorkflowError(e.getMessage());
		}
	}

}
