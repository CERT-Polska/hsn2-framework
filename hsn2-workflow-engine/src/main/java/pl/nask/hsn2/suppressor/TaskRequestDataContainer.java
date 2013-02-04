package pl.nask.hsn2.suppressor;

import java.util.Properties;

import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;

public final class TaskRequestDataContainer {
	private final String serviceName;
	private final String serviceId;
	private final int taskId;
	private final long objectDataId;
	private final Properties params;
	private final DefaultTasksStatistics stats;

	public TaskRequestDataContainer(String serviceName, String serviceId, int taskId, long objectDataId, Properties params,
			DefaultTasksStatistics stats) {
		this.serviceName = serviceName;
		this.serviceId = serviceId;
		this.taskId = taskId;
		this.objectDataId = objectDataId;
		this.params = params;
		this.stats = stats;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getServiceId() {
		return serviceId;
	}

	public int getTaskId() {
		return taskId;
	}

	public long getObjectDataId() {
		return objectDataId;
	}

	public Properties getParams() {
		return params;
	}

	public DefaultTasksStatistics getStats() {
		return stats;
	}
}
