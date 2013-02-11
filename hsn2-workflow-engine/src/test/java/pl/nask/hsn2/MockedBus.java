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

package pl.nask.hsn2;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.api.Message;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnectorException;
import pl.nask.hsn2.bus.connector.objectstore.StubObjectStoreConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnectorException;
import pl.nask.hsn2.bus.connector.process.StubProcessConnector;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.ObjectData;
import pl.nask.hsn2.bus.operations.TaskRequest;
import pl.nask.hsn2.bus.operations.builder.TaskRequestBuilder;
import pl.nask.hsn2.framework.bus.FrameworkBus;

public class MockedBus implements FrameworkBus {
	private static final Logger LOGGER = LoggerFactory.getLogger(MockedBus.class);
	
	public BlockingQueue<TaskRequest> requests = new ArrayBlockingQueue<TaskRequest>(1000);

	private Map<Long, ObjectData> objects = new HashMap<Long, ObjectData>();		
	
	public void sendHighPriorityMessage(Message message) {}
	
	@Override
	public ObjectStoreConnector getObjectStoreConnector() {
		return new StubObjectStoreConnector(){

			@Override
			public Long sendObjectStoreData(long jobId, ObjectData dataList)
							throws ObjectStoreConnectorException {
				return 1L;
			}
			
			@Override
			public ObjectData getObjectStoreData(long jobId, long objectsId)
					throws ObjectStoreConnectorException {
				return objects.get(objectsId);
			}
		};
	}
	@Override
	public ProcessConnector getProcessConnector() {
		return new StubProcessConnector() {
			@Override
			public void sendTaskRequest(String serviceName,
					String serviceId, long jobId, int taskId,
					long objectDataId, Properties parameters)
							throws ProcessConnectorException {
				LOGGER.info("Publishing task: {}:{}", jobId, taskId);
				TaskRequestBuilder builder = new TaskRequestBuilder(jobId, taskId, objectDataId);
				requests.add(builder.build());
			}
		};
	}
	
	public void addObject(Long id, ObjectData obj) {
		objects.put(id, obj);
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public boolean isRunning() {
		return true;
	}

	@Override
	public void jobStarted(long jobId) {
	}

	@Override
	public void jobFinished(long jobId, JobStatus status) {
	}

	@Override
	public void jobFinishedReminder(long jobId, JobStatus status, int offendingTask) {
	}
}
