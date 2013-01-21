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

package pl.nask.hsn2.framework.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerManager.class);
	private static Properties actualProperties = new Properties();

	private LoggerManager() {}

	static{
		setActualProperties("/log4j.properties");
	}

	public static void changeLog4jProperty(String key, String value) {
		actualProperties.setProperty(key, value);
		LOGGER.info("Change {} to {}.", key, value);
		PropertyConfigurator.configure(actualProperties);
	}

	public static void replaceFirstPartLog4jProperty(String key, String value) {
		String originallyProperty = actualProperties.getProperty(key);
		int partsSeparatorIndex = originallyProperty.indexOf(",");
		String replacedProperty = value + originallyProperty.substring(partsSeparatorIndex);
		changeLog4jProperty(key, replacedProperty);
	}

	public static void resetConfiguration(Properties properties){
		LogManager.resetConfiguration();
		PropertyConfigurator.configure(properties);
	}

	public static void setActualProperties(String file){
		InputStream configStream = null;
		try {
			configStream = LoggerManager.class.getResourceAsStream(file);
			actualProperties.load(configStream);
		} catch (IOException e) {
            LOGGER.error("Error reading from file: " + file, e);
        } finally {
            close(configStream);
		}
	}

	private static void close(InputStream configStream) {
	    try {
            if (configStream != null)
                configStream.close();
        } catch (IOException e) {
            LOGGER.error("Error closing stream", e);
        }
    }

    public static void changeLogLevel(String level){
    	LoggerManager.replaceFirstPartLog4jProperty("log4j.rootLogger", level);
	}
}
