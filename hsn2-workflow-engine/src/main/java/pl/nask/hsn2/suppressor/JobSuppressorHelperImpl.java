package pl.nask.hsn2.suppressor;

import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.connector.process.ProcessConnectorException;
import pl.nask.hsn2.framework.suppressor.JobSuppressorHelper;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;

public class JobSuppressorHelperImpl implements JobSuppressorHelper {
	private final static Logger LOGGER = LoggerFactory.getLogger(JobSuppressorHelperImpl.class);
	private BlockingDeque<TaskRequestDataContainer> waitingRequests = new LinkedBlockingDeque<>();
	private final Semaphore semaphore;
	private final long jobId;
	private final SingleThreadTasksSuppressor mainSuppressor;

	public JobSuppressorHelperImpl(long jobId, int tasksThresholdNumber, SingleThreadTasksSuppressor mainSuppressor) {
		this.jobId = jobId;
		this.mainSuppressor = mainSuppressor;
		waitingRequests = new LinkedBlockingDeque<>();
		semaphore = new Semaphore(tasksThresholdNumber);
	}

	@Override
	public void tryToSendRequest() {
		if (semaphore.tryAcquire()) {
			// At this point semaphore is acquired.
			if (waitingRequests.isEmpty()) {
				// Nothing to do - release semaphore.
				semaphore.release();
				LOGGER.debug("Nothing to do this time...");
			} else {
				sendRequest();
			}
		}
	}

	@Override
	public void addTaskRequest(String serviceName, String serviceLabel, int taskId, long objectDataId, Properties serviceParameters,
			DefaultTasksStatistics stats) {
		LOGGER.debug("Adding new task to waiting list.", serviceLabel);

		// Add to waiting requests.
		TaskRequestDataContainer trd = new TaskRequestDataContainer(serviceName, serviceLabel, taskId, objectDataId, serviceParameters,
				stats);
		waitingRequests.push(trd);
		stats.updateSuppressorStats(getFreeBuforSpacesCount(), getWaitingTasksRequestsCount());

		// Inform main suppressor there is an action to do.
		mainSuppressor.notifyAboutJobStateChange(this);

		LOGGER.debug("Task added to waiting list. (job={}, id={}, service={})", new Object[] { jobId, taskId, serviceLabel });
	}

	@Override
	public void signalTaskCompletion(Long jobId, Integer taskId, DefaultTasksStatistics stats) {
		// Release semaphore.
		semaphore.release();
		stats.updateSuppressorStats(getFreeBuforSpacesCount(), getWaitingTasksRequestsCount());

		// Inform main suppressor job status has changed.
		mainSuppressor.notifyAboutJobStateChange(this);

		LOGGER.debug("Task completion signal received. Buffer's space released. (job={}, task={}, freeSpace={}, waitingList={})",
				new Object[] { jobId, taskId, getFreeBuforSpacesCount(), getWaitingTasksRequestsCount() });
	}

	@Override
	public int getWaitingTasksRequestsCount() {
		return waitingRequests.size();
	}

	@Override
	public int getFreeBuforSpacesCount() {
		return semaphore.availablePermits();
	}

	private void sendRequest() {
		TaskRequestDataContainer taskRequestData = waitingRequests.pop();
		taskRequestData.getStats().updateSuppressorStats(getFreeBuforSpacesCount(), getWaitingTasksRequestsCount());

		LOGGER.debug("Action will be taken. (job={}, task={}, freeSpace={}, waitingList={})",
				new Object[] { jobId, taskRequestData.getTaskId(), getFreeBuforSpacesCount(), getWaitingTasksRequestsCount() });

		try {
			JobSuppressorUtils.sendTaskRequest(jobId, getFreeBuforSpacesCount(), getWaitingTasksRequestsCount(),
					taskRequestData.getStats(), taskRequestData.getServiceName(), taskRequestData.getServiceId(),
					taskRequestData.getTaskId(), taskRequestData.getObjectDataId(), taskRequestData.getParams());
		} catch (ProcessConnectorException e) {
			LOGGER.error(e.getMessage(), e);
			System.exit(1); // TODO: doggy! fix it ASAP! Maybe internal TaskError operation?
		}

		LOGGER.debug("Task request sent for service.(job={}, task={}, service={})", new Object[] { jobId, taskRequestData.getTaskId(),
				taskRequestData.getServiceId() });
	}
}
