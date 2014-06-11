package pl.nask.hsn2.framework.commands;

import pl.nask.hsn2.bus.dispatcher.Command;
import pl.nask.hsn2.bus.dispatcher.CommandContext;
import pl.nask.hsn2.bus.dispatcher.CommandExecutionException;
import pl.nask.hsn2.bus.operations.JobCancelReply;
import pl.nask.hsn2.bus.operations.JobCancelRequest;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.framework.core.WorkflowManager;

public class JobCancelRequestCmd implements Command<JobCancelRequest> {

	@Override
	public Operation execute(CommandContext<JobCancelRequest> context) throws CommandExecutionException {
		
		WorkflowManager workflowManager = WorkflowManager.getInstance();
		workflowManager.jobCancel(context.getSourceOperation().getId());
		return new JobCancelReply(true);
	}

}
