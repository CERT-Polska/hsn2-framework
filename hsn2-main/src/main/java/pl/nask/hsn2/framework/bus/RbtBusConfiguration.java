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

import java.util.Arrays;

/**
 * This class represents RabbitMQ Bus configuration for HSN2 Framework.
 * 
 */

public final class RbtBusConfiguration {

	private static final int DEFAULT_AMQP_CONSUMERS_NUMBER = 10;
	
	private String amqpServerAddress;

	// Queues for framework
	private String amqpFrameworkLowQueue;
	private String amqpFrameworkHighQueue;
	private String[] servicesNames;

	// Queues for Object Store
	private String osLowQueueName;
	private String osHiQueueName;
	
	// Job processing events queue
	private String jobEventsQueueName;

	// Exchange names
	private String amqpExchangeMonitoringName = "notify";
	private String amqpExchangeCommonName = "main";
	private String amqpExchangeServicesName = "direct";
	
	private int amqpConsumersNumber = DEFAULT_AMQP_CONSUMERS_NUMBER;

	public String getAmqpExchangeMonitoringName() {
		return amqpExchangeMonitoringName;
	}

	public RbtBusConfiguration setAmqpExchangeMonitoringName(String amqpExchangeMonitoringName) {
		if (amqpExchangeMonitoringName != null && !amqpExchangeMonitoringName.isEmpty()) {
			this.amqpExchangeMonitoringName = amqpExchangeMonitoringName;
		}
		return this;
	}

	public String getAmqpExchangeCommonName() {
		return amqpExchangeCommonName;
	}

	public RbtBusConfiguration setAmqpExchangeCommonName(String amqpExchangeCommonName) {
		if (amqpExchangeCommonName != null && !amqpExchangeCommonName.isEmpty()) {
			this.amqpExchangeCommonName = amqpExchangeCommonName;
		}
		return this;
	}

	public String getAmqpExchangeServicesName() {
		return amqpExchangeServicesName;
	}

	public RbtBusConfiguration setAmqpExchangeServicesName(String amqpExchangeServicesName) {
		if (amqpExchangeServicesName != null && !amqpExchangeServicesName.isEmpty()) {
			this.amqpExchangeServicesName = amqpExchangeServicesName;
		}
		return this;
	}

	public String getJobEventsQueueName() {
		return jobEventsQueueName;
	}

	public RbtBusConfiguration setJobEventsQueueName(String jobEventsQueueName) {
		this.jobEventsQueueName = jobEventsQueueName;
		return this;
	}

	public RbtBusConfiguration setAMQPServerAddress(String amqpServerAddress) {
		this.amqpServerAddress = amqpServerAddress;
		return this;
	}
	
	public RbtBusConfiguration setAMQPFrameworkLowQueue(String amqpFrameworkLowQueue) {
		this.amqpFrameworkLowQueue = amqpFrameworkLowQueue;
		return this;
	}

	public RbtBusConfiguration setAMQPFrameworkHighQueue(String amqpFrameworkHighQueue) {
		this.amqpFrameworkHighQueue = amqpFrameworkHighQueue;
		return this;
	}

	public RbtBusConfiguration setServicesNames(String[] servicesNames) {
		if (servicesNames == null) {
			throw new IllegalArgumentException("Services names array cannot by null!");
		}
		this.servicesNames = servicesNames.clone();
		Arrays.sort(this.servicesNames);
		return this;
	}

	public RbtBusConfiguration setOsLowQueueName(String queueName) {
		this.osLowQueueName = queueName;
		return this;
	}

	public RbtBusConfiguration setOsHiQueueName(String queueName) {
		this.osHiQueueName = queueName;
		return this;
	}

	public RbtBusConfiguration setAmqpConsumersNumber(String consumersNumberTextValue) {
		if (consumersNumberTextValue != null && !consumersNumberTextValue.isEmpty()) {
			try {
				this.amqpConsumersNumber = Integer.valueOf(consumersNumberTextValue);
				if (this.amqpConsumersNumber < 1) {
					throw new IllegalArgumentException("Consumers number has to be positive number");
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Consumers number has to be positive number");
			}
		}
		return this;
	}

	public int getAmqpConsumersNumber() {
		return amqpConsumersNumber;
	}

	public String getAMQPServerAddress() {
		return amqpServerAddress;
	}

	public String getFrameworkLowQueue() {
		return amqpFrameworkLowQueue;
	}

	public String getFrameworkHighQueue() {
		return amqpFrameworkHighQueue;
	}

	public String[] getServicesNames() {
		return servicesNames;
	}

	public String getOsLowQueueName() {
		return this.osLowQueueName;
	}

	public String getOsHiQueueName() {
		return this.osHiQueueName;
	}
}
