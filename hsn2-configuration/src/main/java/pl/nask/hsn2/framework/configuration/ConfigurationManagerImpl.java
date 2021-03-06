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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.framework.configuration.validation.ValidationException;
import pl.nask.hsn2.framework.configuration.validation.Validator;
import pl.nask.hsn2.framework.configuration.validation.ValidatorFactory;

public class ConfigurationManagerImpl implements ConfigurationManager {

	/**
	 * Default config file name. Used when no user config is found.
	 */
	private static final String DEFAULT_CONFIG = "defaultConfig.cfg";

	/**
	 * Default user config file name.
	 */
	private static final String DEFAULT_CONFIG_FILENAME = "config.cfg";

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationManager.class);

	private Map<String, List<Validator>> validators;

	private Configuration configuration;

	private org.apache.commons.configuration.Configuration internalDefaultConfig;
	private org.apache.commons.configuration.Configuration internalUserConfig = new CompositeConfiguration();

	private ConfigurationReader<PropertiesConfiguration> parser = new ApacheConfigurationParser();

	private static final String VALIDATORS_CONFIG_FILENAME = "validation.cfg";

    public ConfigurationManagerImpl() throws IOException, ConfigurationException, ValidationException {
        // load validators config
        loadDefaultConfig();
    }

    private void loadDefaultConfig() throws IOException, ConfigurationException, ValidationException {
        initValidators();
        internalDefaultConfig = parser.parse(DEFAULT_CONFIG).interpolatedConfiguration();
        validateConfiguration(internalDefaultConfig);
    }

    private void initValidators() throws ConfigurationException {
    	try {
    		validators = new HashMap<String, List<Validator>>();
    		PropertiesConfiguration validatorsConfig = parser.parse(VALIDATORS_CONFIG_FILENAME);
    		Iterator<String> iterator = validatorsConfig.getKeys();
    		while (iterator.hasNext()) {
    			String propertyName = iterator.next();
    			String fieldValidators = validatorsConfig.getString(propertyName);
    			List<Validator> parsedValidators = parseValidators(fieldValidators);
    			validators.put(propertyName, parsedValidators);
    		}

    	} catch (FileNotFoundException e) {
    		throw new ConfigurationException("Failed to load " + VALIDATORS_CONFIG_FILENAME + " - no such file", e);
    	} catch (IOException e) {
    		throw new ConfigurationException("Failed to load " + VALIDATORS_CONFIG_FILENAME + " - IO exception", e);
    	}
    }

    private List<Validator> parseValidators(String fieldValidators) {
        StringTokenizer tokenizer = new StringTokenizer(fieldValidators, ",");
        List<Validator> v = new ArrayList<Validator>();
        while (tokenizer.hasMoreTokens()) {
            Validator inst = ValidatorFactory.getInstance(tokenizer.nextToken().trim());
            if (inst != null)
                v.add(inst);
        }

        return v;
    }

    @Override
    public final void reloadConfig() throws ConfigurationException, ValidationException, MappingException {
    	LOGGER.info("No configuration file name provided. Trying default user configuration file first.");
        reloadConfig(DEFAULT_CONFIG_FILENAME);
    }

    @Override
    public final void reloadConfig(String filePath) throws ConfigurationException, ValidationException, MappingException {
    	File file = new File(filePath);
        initValidators();
        LOGGER.info("Reloading configuration from: " + file.getAbsolutePath());
        try {
            org.apache.commons.configuration.Configuration cfg = parser.parse(filePath);
            internalUserConfig = cfg;
            Configuration newConfig = new Configuration(internalDefaultConfig, internalUserConfig);
            validateConfiguration(newConfig.getInternalConfiguration());
            this.configuration = newConfig;
        } catch (FileNotFoundException e) {
            // fail to load user config file, using default config only
            Configuration newConfig = new Configuration(internalDefaultConfig, null);
            validateConfiguration(newConfig.getInternalConfiguration());
            this.configuration = newConfig;
            LOGGER.warn("User configuration file not found. Using default configuration.");
        } catch (IOException e) {
            throw new ConfigurationException("Error reading input for file " + file.getAbsolutePath(), e);
        }
        LOGGER.info("Current configuration is: \n{}", configuration);
    }

    @Override
    public final void setConfigValue(String key, String value) throws ValidationException, MappingException {
        setConfigValue(configuration, key, value);
    }

    @Override
    public final Configuration getCurrentConfig() {
        return configuration;
    }

    protected final void setCurrentConfig(Configuration configuration) {
    	this.configuration = configuration;
    }
    
    protected final void setConfigValue(Configuration configuration, String key, String value) throws ValidationException, MappingException {
        validate(key, value);
        internalUserConfig.setProperty(key, value);
    }

    private void validate(String key, String value) throws ValidationException {
        List<Validator> validator = findMatchingValidators(key);
        if (validator != null) {
            for (Validator v: validator) {
                v.validate(key, value);
            }
        }
    }

    private List<Validator> findMatchingValidators(String key) {
        List<Validator> list = new ArrayList<Validator>();
        for (Map.Entry<String, List<Validator>> e: validators.entrySet()) {
            if (key.matches(e.getKey())) {
                list.addAll(e.getValue());
            }
        }

        return list;
    }

    private void validateConfiguration(org.apache.commons.configuration.Configuration config) throws ValidationException {
        Iterator<String> it = config.getKeys();

        while (it.hasNext()) {
            String key = it.next();
            validate(key, config.getString(key));
        }
    }
}
