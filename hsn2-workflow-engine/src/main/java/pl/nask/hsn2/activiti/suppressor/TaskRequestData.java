package pl.nask.hsn2.activiti.suppressor;

import java.util.Properties;

import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;

public class TaskRequestData {
	private String serviceName;
	private String serviceId;
	private int taskId;
	private long objectDataId;
	private Properties params;
	private DefaultTasksStatistics stats;

	public TaskRequestData(String serviceName, String serviceId, int taskId, long objectDataId, Properties params,
			DefaultTasksStatistics stats) {
		this.serviceName = serviceName;
		this.serviceId = serviceId;
		this.taskId = taskId;
		this.objectDataId = objectDataId;
		this.params = params;
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