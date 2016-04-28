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

package pl.nask.hsn2.suppressor;

import java.util.Properties;

import pl.nask.hsn2.framework.workflow.job.TasksStatistics;

public final class TaskRequestDataContainer {
	private final String serviceName;
	private final String serviceId;
	private final int taskId;
	private final long objectDataId;
	private final Properties params;
	private final TasksStatistics stats;

	public TaskRequestDataContainer(String serviceName, String serviceId, int taskId, long objectDataId, Properties params,
			TasksStatistics stats) {
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

	public TasksStatistics getStats() {
		return stats;
	}
}
