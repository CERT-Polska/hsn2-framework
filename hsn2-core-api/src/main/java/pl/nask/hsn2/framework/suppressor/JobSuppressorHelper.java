package pl.nask.hsn2.framework.suppressor;

import java.util.Properties;

import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;

/**
 * Interface used while suppressing number of tasks done by single job.
 */
public interface JobSuppressorHelper {
	/**
	 * Sends task request to proper service if any of them is waiting to be sent, but only if there are free task buffer
	 * space. This is non blocking method.
	 */
	void tryToSendRequest();

	/**
	 * Adds task request to waiting list. Also notifies suppressor that job status changed.
	 * 
	 * @param serviceName
	 * @param serviceLabel
	 * @param taskId
	 * @param objectDataId
	 * @param serviceParameters
	 * @param stats
	 */
	void addTaskRequest(String serviceName, String serviceLabel, int taskId, long objectDataId, Properties serviceParameters,
			DefaultTasksStatistics stats);

	/**
	 * Sends info about task completion to main suppressor effecting with releasing free space for other's waiting tasks
	 * to be taken. Also notifies suppressor that job status changed.
	 * 
	 * @param jobId
	 *            Job ID.
	 * @param taskId
	 *            Task ID.
	 * @param stats
	 *            Job statistics.
	 */
	void signalTaskCompletion(Long jobId, Integer taskId, DefaultTasksStatistics stats);

	/**
	 * Provides actual tasks requests number waiting to be processed.
	 * 
	 * @return Tasks requests number.
	 */
	int getWaitingTasksRequestsCount();

	/**
	 * Provides actual free task buffer spaces.
	 * 
	 * @return Free task buffer spaces.
	 */
	int getFreeBuforSpacesCount();
}
