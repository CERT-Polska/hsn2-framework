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

import org.apache.commons.io.IOUtils;

import pl.nask.hsn2.bus.dispatcher.Command;
import pl.nask.hsn2.bus.dispatcher.CommandContext;
import pl.nask.hsn2.bus.dispatcher.CommandExecutionException;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.operations.WorkflowGetRequest;
import pl.nask.hsn2.bus.operations.builder.WorkflowErrorBuilder;
import pl.nask.hsn2.bus.operations.builder.WorkflowGetReplyBuilder;
import pl.nask.hsn2.framework.core.WorkflowManager;

public class WorkflowGetRequestCmd implements Command<WorkflowGetRequest> {

	@Override
	public Operation execute(CommandContext<WorkflowGetRequest> context)
			throws CommandExecutionException {
		try {
			String revision = WorkflowManager.getInstance()
					.getCurrentVersionIfEmpty(
							context.getSourceOperation().getName(),
							context.getSourceOperation().getRevision());
			String workflow = IOUtils.toString(WorkflowManager.getInstance()
					.downloadWorkflow(context.getSourceOperation().getName(),
							revision));
		
		return new WorkflowGetReplyBuilder(workflow, revision).build();
		} catch (Exception e) {
			return new WorkflowErrorBuilder(e.getMessage()).build();
		}
	}

}
