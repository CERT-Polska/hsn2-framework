package pl.nask.hsn2.suppressor;

import java.util.Properties;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.connector.process.ProcessConnectorException;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;

public class JobSuppressorUtils {
	private JobSuppressorUtils() {
	}

	public static void sendTaskRequest(long jobId, int freeBufferSlots, int waitingRequestsCount, DefaultTasksStatistics stats,
			String serviceName, String serviceLabel, int taskId, long objectDataId, Properties properties) throws ProcessConnectorException {
		stats.updateSuppressorStats(freeBufferSlots, waitingRequestsCount);
		sendTaskRequest(jobId, stats, serviceName, serviceLabel, taskId, objectDataId, properties);
	}

	public static void sendTaskRequest(long jobId, DefaultTasksStatistics stats, String serviceName, String serviceLabel, int taskId,
			long objectDataId, Properties properties) throws ProcessConnectorException {
		((FrameworkBus) BusManager.getBus()).getProcessConnector().sendTaskRequest(serviceName, serviceLabel, jobId, taskId, objectDataId,
				properties);
		if (stats != null) {
			stats.taskStarted(serviceName);
		}
	}
}
