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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;

public class ApacheConfigurationParser implements ConfigurationReader<PropertiesConfiguration> {

    @Override
    public PropertiesConfiguration parse(String filePath) throws FileNotFoundException,
            IOException, ConfigurationException {
        File conf = new File(filePath);
        InputStream is = null;
        try{
	        if (conf.exists()) {
	            is = new FileInputStream(conf);
	        } else {
	            is = getClass().getResourceAsStream("/"+filePath);
	            if (is == null)
	                throw new FileNotFoundException("classpath:" + filePath);
	        }
	        return parse(is);
        }
        finally{
        	IOUtils.closeQuietly(is);
        }
    }

    @Override
    public PropertiesConfiguration parse(InputStream conf) throws FileNotFoundException, IOException, ConfigurationException {
        PropertiesConfiguration pconf = new PropertiesConfiguration();
        try {
            pconf.load(conf);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            throw new ConfigurationException("error loading configuration", e);
        }
        return pconf;
    }
}
