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

package pl.nask.hsn2.framework.configuration;

import pl.nask.hsn2.framework.configuration.validation.ValidationException;

public interface ConfigurationManager {

    /**
     * Loads the default configuration file. If the configuration is valid, the current configuration is replaced with the new one.
     *
     * @throws ConfigurationException if the configuration file can't be loaded (a general exception)
     * @throws ValidationException if the configuration from the file doesn't pass the validation
     * @throws MappingException if the configuration from the file can't be mapped onto configuration class
     */
    void reloadConfig() throws ConfigurationException, ValidationException, MappingException;

    /**
     * Loads the configuration file from the path given as a parameter. If the configuration is valid, the current configuration is replaced with the new one.
     *
     * @param filePath the path to the configuration file to be loaded.
     *
     * @throws ConfigurationException if the configuration file can't be loaded (a general exception)
     * @throws ValidationException if the configuration from the file doesn't pass the validation
     * @throws MappingException if the configuration from the file can't be mapped onto configuration class
     */
    void reloadConfig(String filePath)
            throws ConfigurationException, ValidationException,
            MappingException;

    /**
     * Allows to change (set) configuration entry manually.
     *
     * @param key configuration entry key (as used in the configuration file)
     * @param value string representation of the value to be set.
     *
     * @throws ValidationException if the value doesn't pass the validation rules for this configuration entry
     * @throws MappingException if the key cannot be mapped onto configuration class
     */
    void setConfigValue(String key, String value)
            throws ValidationException, MappingException;

    /**
     * Returns the current configuration.
     *
     * @return Current instance of <code>Configuration</code>. 
     */
    Configuration getCurrentConfig();

}