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

package pl.nask.hsn2.framework.configuration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the current configuration of the HSN2 Framework.
 * 
 * 
 * 
 * According to the task #4010: BA: FUC-1.1 Reading configuration file the following configuration is needed: 1 Logging
 * 1.1 Log filename 1.2 Debugging level 2 Workflow packages configuration 2.1 Workflow packages filenames 3 Bus
 * configuration 3.1 Connectivity 3.1.1 Port number 4 CLI configuration 4.1 Connectivity 4.1.1 Ports 5 Services
 * configuration (not required? - because of broadcast messages) 6 Limits 6.1 Maximum number of jobs processed by the
 * framework 6.2 Maximum number of URLs processed by the framework (optional)
 * 
 * The following configuration options will be available: logger.filename logger.debugLevel bus.portNumber
 * jobs.max.concurent jobs.max.urls
 * 
 * 
 */
public class Configuration {

	private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

	private static final String VALUES_SEPARATOR = " ";
	private CompositeConfiguration internalConfiguration;

	Configuration(org.apache.commons.configuration.Configuration defaultConfig, org.apache.commons.configuration.Configuration userConfig) {

		internalConfiguration = new CompositeConfiguration();
		if (userConfig != null)
			internalConfiguration.addConfiguration(userConfig);
		if (defaultConfig != null)
			internalConfiguration.addConfiguration(defaultConfig);
	}

	public String getJobSequenceFile() {
		return internalConfiguration.getString("jobs.sequence.file");
	}

	public int getJobsLimit() {
		int value = 0;
		try {
			value = internalConfiguration.getInteger("jobs.limit", 0);
			if (value < 0)
				throw new ConversionException();
		} catch (ConversionException e) {
			LOGGER.error("Incorrect value for key 'jobs.limit'. Expected positive integer.");
			value = 0;
		}
		return value;
	}

	public String getAMQPFrameworkLowQueue() {
		return internalConfiguration.getString("AMQP.queues.framework.low");
	}

	public String getAMQPFrameworkHighQueue() {
		return internalConfiguration.getString("AMQP.queues.framework.high");
	}

	public String getAMQPServerAddress() {
		return internalConfiguration.getString("AMQP.server.address");
	}

	public String getWorkflowRepositoryPath() {
		return internalConfiguration.getString("workflow.repository");
	}

	public String[] getAMQPServicesNames() {
		return internalConfiguration.getStringArray("AMQP.services");
	}

	public String getAMQPObjectStoreQueueHigh() {
		return internalConfiguration.getString("AMQP.queues.objectStore.high");
	}

	public String getAMQPObjectStoreQueueLow() {
		return internalConfiguration.getString("AMQP.queues.objectStore.low");
	}

	public String getAMQPExchangeMonitoring() {
		return internalConfiguration.getString("AMQP.exchange.monitoring");
	}

	public String getAMQPExchangeCommon() {
		return internalConfiguration.getString("AMQP.exchange.common");
	}

	public String getAMQPExchangeServices() {
		return internalConfiguration.getString("AMQP.exchange.services");
	}

	public String getAMQPConsumersNumber() {
		return internalConfiguration.getString("AMQP.consumers.number");
	}
	
	public int getJobsSuppressorTasksThreshold() throws ConfigurationException {
		try {
			int number = Integer.parseInt(internalConfiguration.getString("jobs.suppressor.tasks.threshold"));
			if (number < 1) {
				throw new ConfigurationException("Illegal 'jobs.suppressor.tasks.threshold' value. Has to be positive number.");
			}
			return number;
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Cannot parse 'jobs.suppressor.tasks.threshold' value. Has to be positive number.", e);
		}
	}

	org.apache.commons.configuration.Configuration getInternalConfiguration() {
		return internalConfiguration;
	}

	@Override
	public String toString() {
		return ConfigurationUtils.toString(internalConfiguration);
	}

	public Map<String, String> getConfigAsMap() {
		Map<String, String> map = new HashMap<String, String>();
		Iterator<String> iterator = internalConfiguration.getKeys();
		while (iterator.hasNext()) {
			String k = (String) iterator.next();
			map.put(k, combineAllValues(k));
		}
		return map;
	}

	private String combineAllValues(String key) {
		StringBuilder allValuesInOne = new StringBuilder();
		String[] values = internalConfiguration.getStringArray(key);

		for (String value : values) {
			allValuesInOne.append(value);
			allValuesInOne.append(VALUES_SEPARATOR);
		}
		int length = allValuesInOne.length() - VALUES_SEPARATOR.length();
		return allValuesInOne.substring(0, length);
	}

	public String[] getAMQPObjectStoreQueueNames() {
		return new String[] { getAMQPObjectStoreQueueLow(), getAMQPObjectStoreQueueHigh() };
	}
}
