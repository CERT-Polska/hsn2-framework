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

package pl.nask.hsn2.suppressor;

import java.util.Properties;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.connector.process.ProcessConnectorException;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.workflow.job.TasksStatistics;

public class JobSuppressorUtils {
	private JobSuppressorUtils() {
	}

	public static void sendTaskRequest(long jobId, int freeBufferSlots, int waitingRequestsCount, TasksStatistics stats,
			String serviceName, String serviceLabel, int taskId, long objectDataId, Properties properties) throws ProcessConnectorException {
		stats.updateSuppressorStats(freeBufferSlots, waitingRequestsCount);
		sendTaskRequest(jobId, stats, serviceName, serviceLabel, taskId, objectDataId, properties);
	}

	public static void sendTaskRequest(long jobId, TasksStatistics stats, String serviceName, String serviceLabel, int taskId,
			long objectDataId, Properties properties) throws ProcessConnectorException {
		((FrameworkBus) BusManager.getBus()).getProcessConnector().sendTaskRequest(serviceName, serviceLabel, jobId, taskId, objectDataId,
				properties);
		if (stats != null) {
			stats.taskStarted(serviceName);
		}
	}
}
