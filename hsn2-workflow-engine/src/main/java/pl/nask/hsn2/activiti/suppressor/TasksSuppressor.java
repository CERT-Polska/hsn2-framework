package pl.nask.hsn2.activiti.suppressor;

import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.connector.process.ProcessConnectorException;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.workflow.hwl.ServiceParam;
import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;
import pl.nask.hsn2.workflow.engine.SubprocessParameters;

public class TasksSuppressor extends Thread {
	private final static Logger LOGGER = LoggerFactory.getLogger(TasksSuppressor.class);
	private static final long SLEEP_IF_NO_REQUESTS = 100;
	private BlockingDeque<TaskRequestData> waitingRequests = new LinkedBlockingDeque<>();
	private Semaphore semaphore;
	private long jobId;
	private boolean killRequested = false;

	public TasksSuppressor(long jobId, int tasksThresholdNumber) {
		this.jobId = jobId;
		this.semaphore = new Semaphore(tasksThresholdNumber);
	}

	@Override
	public void run() {
		while (!killRequested) {
			try {
				if (waitingRequests.isEmpty()) {
					Thread.sleep(SLEEP_IF_NO_REQUESTS);
				} else {
					LOGGER.info("Before semaphore acquire. Free space = " + getFreeBuforSpaces() + ", waiting list = " + getWaitingTasksNumber());
					semaphore.acquire();
					TaskRequestData taskRequestData = waitingRequests.pop();
					sendTaskRequest(taskRequestData);
					LOGGER.info("Before semaphore acquire. Free space = " + getFreeBuforSpaces() + ", waiting list = " + getWaitingTasksNumber());
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void sendTaskRequest(TaskRequestData taskRequestData) {
		try {
			((FrameworkBus) BusManager.getBus()).getProcessConnector().sendTaskRequest(taskRequestData.getServiceName(),
					taskRequestData.getServiceId(), jobId, taskRequestData.getTaskId(), taskRequestData.getObjectDataId(),
					taskRequestData.getParams());
		} catch (ProcessConnectorException e) {
			LOGGER.error(e.getMessage(), e);
			System.exit(1); // TODO: doggy! fix it ASAP! Maybe internal TaskError operation?
		}
		DefaultTasksStatistics stats = taskRequestData.getStats();
		if (stats != null) {
			stats.taskStarted(taskRequestData.getServiceName());
		}
		LOGGER.info("Task request (id={}) sent for service {}.", taskRequestData.getTaskId(), taskRequestData.getServiceId());
	}

	public void addTaskRequest(String serviceName, String serviceLabel, ExecutionWrapper wrapper, Properties serviceParameters) {
		LOGGER.info("Adding new task for service '{}' to waiting list.", serviceLabel);
		int taskId = wrapper.setNewTaskId();

		Properties params = ServiceParam.merge(serviceParameters, wrapper.getUserConfig().get(serviceLabel), true);

		SubprocessParameters processParams = wrapper.getSubprocessParameters();
		long objectDataId = (processParams != null) ? processParams.getObjectDataId() : 0L;

		DefaultTasksStatistics stats = wrapper.getJobStats();

		TaskRequestData trd = new TaskRequestData(serviceName, serviceLabel, taskId, objectDataId, params, stats);
		waitingRequests.push(trd);
		LOGGER.info("Task (id={}, service={}) added to waiting list.", taskId, serviceLabel);
	}

	public void signalTaskCompletion() {
		semaphore.release();
		LOGGER.info("Buffer's space released.");
	}
	
	public int getFreeBuforSpaces() {
		return semaphore.availablePermits();
	}
	
	public int getWaitingTasksNumber() {
		return waitingRequests.size();
	}
}
