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

package pl.nask.hsn2.framework.commands;

import java.util.List;

import pl.nask.hsn2.bus.dispatcher.Command;
import pl.nask.hsn2.bus.dispatcher.CommandContext;
import pl.nask.hsn2.bus.dispatcher.CommandExecutionException;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.WorkflowError;
import pl.nask.hsn2.bus.operations.WorkflowHistoryRequest;
import pl.nask.hsn2.bus.operations.builder.WorkflowHistoryReplyBuilder;
import pl.nask.hsn2.framework.core.WorkflowManager;
import pl.nask.hsn2.framework.workflow.repository.WorkflowVersionInfo;

public final class WorkflowHistoryRequestCmd implements Command<WorkflowHistoryRequest> {

	@Override
	public Operation execute(CommandContext<WorkflowHistoryRequest> context)
			throws CommandExecutionException {

		try {
			List<WorkflowVersionInfo> list = WorkflowManager.getInstance().getWorkflowHistory(context.getSourceOperation().getName());
			
			WorkflowHistoryReplyBuilder rb = new WorkflowHistoryReplyBuilder();
			
			for (WorkflowVersionInfo info : list) {
				rb.addHistoryEntry(info.getVersion(), info.getVersionTimestamp());
			}
			return rb.build();
		} catch (Exception e) {
			return new WorkflowError(e.getMessage());
		}
	
	}

}
