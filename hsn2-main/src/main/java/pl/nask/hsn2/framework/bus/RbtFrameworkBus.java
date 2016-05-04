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

package pl.nask.hsn2.framework.bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.api.BusException;
import pl.nask.hsn2.bus.api.endpoint.ConsumeEndPoint;
import pl.nask.hsn2.bus.api.endpoint.ConsumeEndPointHandler;
import pl.nask.hsn2.bus.api.endpoint.ObservableConsumeEndPointHandler;
import pl.nask.hsn2.bus.api.endpoint.observer.ContentTypeObserver;
import pl.nask.hsn2.bus.api.endpoint.observer.RetryDeliveryObserver;
import pl.nask.hsn2.bus.connector.CommandExecutionConnector;
import pl.nask.hsn2.bus.connector.framework.DefaultJobEventsNotifier;
import pl.nask.hsn2.bus.connector.framework.JobEventsNotifier;
import pl.nask.hsn2.bus.connector.objectstore.DefaultObjectStoreConnector;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.bus.connector.process.DefaultProcessConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnector;
import pl.nask.hsn2.bus.dispatcher.CachableCommandFactory;
import pl.nask.hsn2.bus.dispatcher.CommandDispatcher;
import pl.nask.hsn2.bus.dispatcher.DefaultCommandDispatcher;
import pl.nask.hsn2.bus.dispatcher.ReflectionCommandFactory;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.Operation;
import pl.nask.hsn2.bus.rabbitmq.RbtDestination;
import pl.nask.hsn2.bus.rabbitmq.RbtUtils;
import pl.nask.hsn2.bus.rabbitmq.endpoint.RbtEndPointFactory;
import pl.nask.hsn2.bus.recovery.Recoverable;
import pl.nask.hsn2.bus.serializer.MessageSerializer;
import pl.nask.hsn2.bus.serializer.protobuf.ProtoBufMessageSerializer;
import pl.nask.hsn2.framework.core.WorkflowManager;

public final class RbtFrameworkBus implements FrameworkBus, Recoverable {

	private static final Logger LOGGER = LoggerFactory.getLogger(RbtFrameworkBus.class);

	private static final String SERVICE_QUEUE_PERFIX = "srv-";
	private static final String SERVICE_LOW_PRIORITY_SUFFIX = ":l";
	private static final String SERVICE_HIGH_PRIORITY_SUFFIX = ":h";
	private static final long ONE_MINUTE = 60 * 1000;
	
	private static final MessageSerializer<Operation> DEFAULT_SERIALIZER = new ProtoBufMessageSerializer();
	private static final CommandDispatcher DEFAULT_COMMAND_DISPATCHER = new DefaultCommandDispatcher(
			new CachableCommandFactory(
					new ReflectionCommandFactory(
							new String[] {	"pl.nask.hsn2.bus.commands",
											"pl.nask.hsn2.framework.commands"
									})));

	private ProcessConnector servicesConnector = null;
	private ObjectStoreConnector objectStoreConnector = null;

	private ConsumeEndPoint lowConsumeEndPoint = null;
	private ConsumeEndPoint highConsumeEndPoint = null;

	private JobEventsNotifier jobEventsNotifier = null;

	private RbtBusConfiguration config = null;
	
	private RbtEndPointFactory endPointFactory = null;
	
	private boolean running = false;
	private final Object mutex;

	private final List<JobReminderData> jobReminders = Collections.synchronizedList(new ArrayList<JobReminderData>());

	/**
	 * Default constructor.
	 * 
	 * @param config Configuration for the bus.
	 */
	public RbtFrameworkBus(RbtBusConfiguration config) throws BusException {
		this.config = config;
		this.endPointFactory = new RbtEndPointFactory();
		this.mutex = this;
		setup(true);
	}

	private void setup(boolean purgeQueues) throws BusException {
		this.endPointFactory
			.setNumberOfconsumerThreads(config.getAmqpConsumersNumber())
			.setServerAddress(config.getAMQPServerAddress())
			.reconnect();

		List<String> queues = new LinkedList<String>();

		// Declarations of exchanges and bindings between them
		String exchangeMonitoringName = config.getAmqpExchangeMonitoringName();
		String exchangeCommonName = config.getAmqpExchangeCommonName();
		String exchangeServicesName = config.getAmqpExchangeServicesName();
		LOGGER.info("Creating exchanges:{}",new Object[]{exchangeCommonName,exchangeMonitoringName,exchangeServicesName});
		RbtUtils.createExchanges(endPointFactory.getConnection(), exchangeCommonName, exchangeServicesName, exchangeMonitoringName);

		// services queues
		for (String sName : config.getServicesNames()) {
			queues.add(SERVICE_QUEUE_PERFIX + sName + SERVICE_HIGH_PRIORITY_SUFFIX);
			queues.add(SERVICE_QUEUE_PERFIX + sName + SERVICE_LOW_PRIORITY_SUFFIX);
		}

		// Object Store queues
		queues.add(config.getOsHiQueueName());
		queues.add(config.getOsLowQueueName());

		// Framework queues
		queues.add(config.getFrameworkHighQueue());
		queues.add(config.getFrameworkLowQueue());

		// deadletter queue
		queues.add("deadletter");

		LOGGER.info("Creating queues ({}): {}", purgeQueues?"purging":"queues data preserved", queues);
		RbtUtils.createQueues(endPointFactory.getConnection(), exchangeServicesName, queues.toArray(new String[queues.size()]), purgeQueues);
	}

	@Override
	public void start() throws BusException {
		
		synchronized(mutex) {
			// do not start if bus is running
			if (running) {
				return;
			}
			
			LOGGER.debug("Starting RbtBus");
			
			initOutgoingConnectors();
			
			// initiate message consumer with command dispatcher
		    ConsumeEndPointHandler commandConsumerHandler = new CommandExecutionConnector(
		    		DEFAULT_SERIALIZER,
		    		endPointFactory.createFireAndForgetEndPoint(),
		    		DEFAULT_COMMAND_DISPATCHER);
	
			ObservableConsumeEndPointHandler observable = new ObservableConsumeEndPointHandler(
					commandConsumerHandler);
			observable.addDeliveryObserver(new ContentTypeObserver());
			observable.addDeliveryObserver(new RetryDeliveryObserver(
					endPointFactory.createFireAndForgetEndPoint()));
		    
			LOGGER.info("Starting consumer endpoints for the bus...");
			lowConsumeEndPoint = endPointFactory.createConsumeEndPoint(
					observable, config.getFrameworkLowQueue());
			highConsumeEndPoint = endPointFactory.createConsumeEndPoint(
					observable, config.getFrameworkHighQueue());
			this.running = true;
		}
	}
	
	@Override
	public ObjectStoreConnector getObjectStoreConnector() {
		return objectStoreConnector;
	}
	
	@Override
	public ProcessConnector getProcessConnector() {
		return servicesConnector;
	}
	
	@Override
	public void stop() {
		synchronized(mutex) {
			if (!running) {
				return;
			}
			
			try {
				lowConsumeEndPoint.close();
			} catch (BusException e) {
				// problem with closing endpoint, ignoring
			}
			lowConsumeEndPoint = null;

			try {
				highConsumeEndPoint.close();
			} catch (BusException e) {
				// problem with closing endpoint, ignoring
			}
			highConsumeEndPoint = null;

			try {
				RbtUtils.closeConnection(endPointFactory.getConnection());
			} catch (BusException e) {
				// problem with closing connection, ignoring
			}
			objectStoreConnector.releaseResources();
			objectStoreConnector = null;
			
			servicesConnector.releaseResources();
			servicesConnector = null;
			
			jobEventsNotifier.releaseResources();
			jobEventsNotifier = null;
			running = false;
			
		}
	}

	@Override
	public boolean isRunning() {
		synchronized(mutex) {return this.running;}
	}

	@Override
	public void recovery() {
		boolean recover = false;
		try {
			if (!endPointFactory.isConnectionValid()) {
				stop();
				if (!recover) {			
					LOGGER.error("Connection failure. Cannot reliable recover.");
					LOGGER.error("Unfinished jobs:{}.shutting down.",WorkflowManager.getInstance().getWorkflowJobs()) ;
					System.exit(1);
				} else {
					LOGGER.info("Bus problem occured, trying to recover...");
				}
				setup(false);
				start();
			}
		} catch(BusException ex) {
			LOGGER.error("Bus recovery failed.", ex);
		}
	}

	@Override
	public void jobStarted(long jobId) {
		if (jobEventsNotifier != null) {
			jobEventsNotifier.jobStarted(jobId);
		} else {
			LOGGER.debug("Job events notifier not enabled, will not inform...");
		}
	}

	@Override
	public void jobFinished(long jobId, JobStatus status) {
		if (jobEventsNotifier != null) {
			jobEventsNotifier.jobFinished(jobId, status);
		} else {
			LOGGER.debug("Job events notifier not enabled, will not inform...");
		}
	}

	public void initOutgoingConnectors() throws BusException {
		if (servicesConnector == null) {
			servicesConnector = new DefaultProcessConnector(
					DEFAULT_SERIALIZER,
					endPointFactory,
					new RbtDestination(config.getAmqpExchangeCommonName(), config.getFrameworkLowQueue()),
					getServicesDestinations(config.getAmqpExchangeCommonName(), config.getServicesNames(), SERVICE_QUEUE_PERFIX, SERVICE_LOW_PRIORITY_SUFFIX));
		}
		if (objectStoreConnector == null) {
			objectStoreConnector = new DefaultObjectStoreConnector(
					DEFAULT_SERIALIZER,
					new RbtDestination(config.getAmqpExchangeCommonName(), config.getOsHiQueueName()),
					endPointFactory.createThreadSafeRequestResponseEndPoint(),
					endPointFactory.createFireAndForgetEndPoint());
		}
		if (jobEventsNotifier == null) {			
			jobEventsNotifier = new DefaultJobEventsNotifier(
					DEFAULT_SERIALIZER, endPointFactory,
					new RbtDestination(config.getAmqpExchangeMonitoringName(), ""));
		}
		LOGGER.info("Initialized outgoing connectors:\n\t{},\n\t{},\n\t{}",new Object[]{servicesConnector,objectStoreConnector,jobEventsNotifier});
	}

	private Map<String, RbtDestination> getServicesDestinations(String exchange, String[] serviceNames, String prefix, String suffix) {
		HashMap<String, RbtDestination> servicesDestinations = new HashMap<String, RbtDestination>();
		String routingKey;
		for (String s : serviceNames) {
			routingKey = new StringBuilder(prefix).append(s).append(suffix).toString();
			servicesDestinations.put(s, new RbtDestination(exchange, routingKey));
		}
		return servicesDestinations;
	}

	@Override
	public void jobFinishedReminder(long jobId, JobStatus status, int offendingTask) {
		if (jobEventsNotifier == null) {
			LOGGER.debug("Job events notifier not enabled, will not inform. (jobId={}, taskId={})", jobId, offendingTask);
			return;
		}

		synchronized (jobReminders) {
			// Cut off old reminders.
			long now = System.currentTimeMillis();
			int index = Collections.binarySearch(jobReminders, now - ONE_MINUTE);
			if (index < 0) {
				index = -index - 1;
			} else {
				index++;
			}
			jobReminders.subList(0, index).clear();

			// Now only reminders from last minute left. Search for job id.
			JobReminderData reminderFound = null;
			for (JobReminderData tempReminder : jobReminders) {
				if (tempReminder.getJobId() == jobId) {
					reminderFound = tempReminder;
					break;
				}
			}

			// Proceed with reminder sending if needed.
			if (reminderFound == null) {
				// Reminder not found, sending new one.
				jobReminders.add(new JobReminderData(jobId, now));
				LOGGER.debug("Reminder not found, sending new one. (jobId={}, taskId={})", jobId, offendingTask);
				jobEventsNotifier.jobFinishedReminder(jobId, status, offendingTask);
			} else {
				// Reminder found, ignore current one.
				LOGGER.debug("Reminder found, ignore current one. (jobId={}, taskId={})", jobId, offendingTask);
			}
		}
	}

	@Override
	public void releaseResources() {
		stop();
		
	}

}
