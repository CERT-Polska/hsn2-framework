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

import java.io.File;
import java.net.URISyntaxException;

import pl.nask.hsn2.framework.configuration.Configuration;
import pl.nask.hsn2.framework.configuration.ConfigurationManager;

public final class FrameworkContext {
	
    private static ConfigurationManager configurationManager;

    private FrameworkContext() {}
    
    public static Configuration getCurrentConfig() {
        assertNotNull(configurationManager, "ConfigurationManager");
        return configurationManager.getCurrentConfig();
    }

    static void registerConfigurationManager(ConfigurationManager mgr) {
        assertNull(configurationManager, "ConfigurationManager");
        configurationManager = mgr;
    }

    private static void assertNull(Object object, String objName) {
        if (object != null)
            throw new IllegalStateException(objName + " already registered");
    }

    private static void assertNotNull(Object object, String objName) {
        if (object == null)
            throw new IllegalStateException("FrameworkContext not initialized properly: " + objName + " not registered");
    }

    public static void clean() {
        configurationManager = null;
    }

    public static String getFrameworkPath(){
		try {
			String clazzPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			File clazzFile = new File(clazzPath);
			return clazzFile.getParent();
		} catch (URISyntaxException e) {
			throw new RuntimeException("Error while trying to get Framework path.", e);
		}
	}
}
