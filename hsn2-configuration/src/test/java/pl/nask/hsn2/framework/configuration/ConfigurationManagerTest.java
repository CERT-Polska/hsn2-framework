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

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Assert;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import pl.nask.hsn2.framework.configuration.validation.ValidationException;

@Test
public class ConfigurationManagerTest {

    ConfigurationManager mgr;

    @BeforeTest
    public void prepareConfigurationManager() throws FileNotFoundException, IOException, ConfigurationException, ValidationException {
       mgr = new ConfigurationManagerImpl();
    }

    @Test
    public void checkJobLimit() throws ConfigurationException, ValidationException, MappingException {
    	Configuration cfg = new Configuration(null, null);
    	Assert.assertEquals(0, cfg.getJobsLimit()); // empty configuration, 0 expected

    	cfg.getInternalConfiguration().setProperty("jobs.limit", "0"); // 0 defined, 0 expected
    	Assert.assertEquals(0, cfg.getJobsLimit());

    	cfg.getInternalConfiguration().setProperty("jobs.limit", "-8"); // negative value, 0 expected
    	Assert.assertEquals(0, cfg.getJobsLimit());

    	cfg.getInternalConfiguration().setProperty("jobs.limit", "8"); // positive value, value expected
    	Assert.assertEquals(8, cfg.getJobsLimit());
    }
    
    // default config does not exists, so only the defaults should be loaded
    @Test
    public void reloadDefConfig() throws ConfigurationException, ValidationException, MappingException {
        mgr.reloadConfig();
        Configuration cfg = mgr.getCurrentConfig();
        Assert.assertEquals("fw:l", cfg.getAMQPFrameworkLowQueue());
        Assert.assertEquals("fw:h", cfg.getAMQPFrameworkHighQueue());
        Assert.assertEquals("os:l", cfg.getAMQPObjectStoreQueueLow());
        Assert.assertEquals("os:h", cfg.getAMQPObjectStoreQueueHigh());

        Assert.assertEquals("/etc/hsn2/workflows/", cfg.getWorkflowRepositoryPath());
        
        Assert.assertEquals(0, cfg.getJobsLimit());
    }

    // corrupted config should load 'as is', only valid values should be applied
    @Test
    public void reloadCorruptedConfig() throws ConfigurationException, ValidationException, MappingException {
        mgr.reloadConfig("corrupted-config.cfg");
    }

    @Test
    public void reloadUserConfig() throws ConfigurationException, ValidationException, MappingException {
        mgr.reloadConfig("user-config.cfg");

        Configuration cfg = mgr.getCurrentConfig();
        // value from the default config
        Assert.assertEquals("127.0.0.1", cfg.getAMQPServerAddress());
        Assert.assertEquals("fw:l", cfg.getAMQPFrameworkLowQueue());
        Assert.assertEquals("fw:h", cfg.getAMQPFrameworkHighQueue());
        Assert.assertEquals("os:l", cfg.getAMQPObjectStoreQueueLow());
        Assert.assertEquals("os:h", cfg.getAMQPObjectStoreQueueHigh());

        // value from the user config
        Assert.assertEquals("/etc/hsn2/workflows2/", cfg.getWorkflowRepositoryPath());
    }

    @Test
    public void reloadNonValidatingConfig() throws ConfigurationException, ValidationException, MappingException {
        mgr.reloadConfig("nonValidating-config.cfg");
        Configuration cfg = mgr.getCurrentConfig();
    }

    @Test
    public void testServicesConfig() throws ConfigurationException, ValidationException, MappingException {
        mgr.reloadConfig();

        Assert.assertEquals("127.0.0.1", mgr.getCurrentConfig().getAMQPServerAddress());
        Assert.assertEquals("srv-feeder:l", mgr.getCurrentConfig().getAMQPServicesNames()[0]);
        Assert.assertEquals("srv-crawler:l", mgr.getCurrentConfig().getAMQPServicesNames()[1]);
        Assert.assertEquals("srv-capture:l", mgr.getCurrentConfig().getAMQPServicesNames()[2]);
    }

    @Test
    public void testOverrideSocketConfig() throws ConfigurationException, ValidationException, MappingException {
        // use the defaultConfig as user config - there should be no error here
        mgr.reloadConfig("defaultConfig.cfg");
    }
}
