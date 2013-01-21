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

import java.util.List;

import pl.nask.hsn2.bus.dispatcher.Command;
import pl.nask.hsn2.bus.dispatcher.CommandContext;
import pl.nask.hsn2.bus.dispatcher.CommandExecutionException;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.WorkflowBasicInfo;
import pl.nask.hsn2.bus.operations.WorkflowError;
import pl.nask.hsn2.bus.operations.WorkflowListRequest;
import pl.nask.hsn2.bus.operations.builder.WorkflowListReplyBuilder;
import pl.nask.hsn2.framework.core.WorkflowManager;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.policy.WorkflowPolicyManager;

public class WorkflowListRequestCmd implements Command<WorkflowListRequest> {

	@Override
	public Operation execute(CommandContext<WorkflowListRequest> context)
			throws CommandExecutionException {

		try {
			List<WorkflowDescriptor> defs = WorkflowManager.getInstance()
					.getWorkflowDefinitions(
							context.getSourceOperation().isEnabledOnly());		
	
			WorkflowListReplyBuilder builder = new WorkflowListReplyBuilder();
			for (WorkflowDescriptor descriptor : defs) {
				
				// checks is workflow is enabled by policy and if it's deployed
				boolean enabled = WorkflowPolicyManager.isEnabledByPolicy(descriptor.getName());
				
				builder.addWorkflowBasicInfo(
						new WorkflowBasicInfo(descriptor.getName(), enabled));
			}
			return builder.build();
		} catch (Exception e) {
			return new WorkflowError(e.getMessage());
		}
	}

}
