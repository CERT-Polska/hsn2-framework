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

package pl.nask.hsn2.framework.workflow.hwl;

import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class ServiceParam {

    @XmlValue
    private String value;

    @XmlAttribute(required=true)
    private String name;

    @SuppressWarnings("unused")
    private ServiceParam() {}

    public ServiceParam(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public final String getName() {
        return name;
    }

    public final String getValue() {
        return value;
    }

    public static Properties getAsProperties(List<ServiceParam> list) {
    	Properties props = new Properties();
    	if (list != null && !list.isEmpty()) {
    		for (ServiceParam p : list) {
            	props.put(p.getName(), p.getValue());
    		}
    	}
    	return props;
    }

    public static Properties merge(Properties props1, Properties props2, boolean override) {
        Properties props = new Properties();

        
        // add elements from first list
        if (props1 != null) {
        	props.putAll(props1);
        }

        // add elements from second list
        if (props2 != null) {
		    if (override) {
	        	props.putAll(props2);
		    } else {
		    	Enumeration<?> e = props2.propertyNames();
		    	while (e.hasMoreElements()) {
		    		Object key = (String) e.nextElement();
		    		if (!props.containsKey(key)) {
		    			props.put(key, props2.get(key));
		    		}
		    	}
		    }
        }
    	return props;
    }
    
    @Override
    public final String toString() {
        return "ServiceParam [name=" + name + ", value=" + value + "]";
    }


}
