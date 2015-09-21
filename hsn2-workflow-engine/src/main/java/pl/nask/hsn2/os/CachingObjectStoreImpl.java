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

package pl.nask.hsn2.os;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnectorException;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreGetCallback;
import pl.nask.hsn2.bus.operations.Attribute;
import pl.nask.hsn2.bus.operations.ObjectData;
import pl.nask.hsn2.bus.operations.Reference;
import pl.nask.hsn2.bus.operations.builder.ObjectDataBuilder;

public final class CachingObjectStoreImpl implements ObjectStore {
	
    private static final Logger LOG = LoggerFactory.getLogger(CachingObjectStoreImpl.class);

    Map<Long, Map<String, Object>> cache = new HashMap<Long, Map<String,Object>>();

    private final ObjectStoreConnector connector;
    private final long jobId;

    public CachingObjectStoreImpl(ObjectStoreConnector connector, long jobId) {
        this.connector = connector;
        this.jobId = jobId;
    }

    @Override
    public Map<String, Object> get(long id) {
        try {
            Map<String, Object> result;
            if (inCache(id)) {
                result = getFromCache(id);
            } else {
            	final ObjectStore os = this;
                ObjectData data = connector.getObjectStoreData(jobId, id);
                
                Map<String, Object> map = asMap(new ObjectStoreGetCallback() {
					@Override
					public Object handleGet(long id) {
						return new OSObject(os, id);
					}
        		}, data);
                insertIntoCache(id, map);
                result = map;
            }

            return result;
        } catch (ObjectStoreConnectorException e) {
	        throw new RuntimeException(e);
	    }
    }

	private Map<String, Object> asMap(ObjectStoreGetCallback callback,
			ObjectData obj) {
		Map<String, Object> attributeMap = new HashMap<String, Object>();

		for (Attribute attr : obj.getAttributes()) {
			attributeMap.put(attr.getName().toLowerCase(),
					valueOf(callback, attr));
		}

		attributeMap.put("id", obj.getId());
		return attributeMap;
	}

	private Object valueOf(ObjectStoreGetCallback callback, Attribute attr) {
		switch (attr.getType()) {
		case BOOL:
			return attr.getBool();
		case INT:
			return attr.getInteger();
		case STRING:
			return attr.getString();
		case BYTES:
			Reference ref = attr.getBytes();
			Map<String, Object> mapRef = new HashMap<String, Object>();
			mapRef.put("key", ref.getKey());
			mapRef.put("store", ref.getStore());
			return mapRef;
		case FLOAT:
			return attr.getFloat();
		case OBJECT:
			return callback.handleGet(attr.getObejectRef());
		case TIME:
			return attr.getTime();
		case EMPTY:
		default:
			return null;
		}
	}

    @Override
    public List<OSObject> findByName(String attributeName) {
        try {
        	Set<Long> ids = connector.findByAttributeName(jobId, attributeName);
            return osObjects(ids);
        } catch (ObjectStoreConnectorException e) {
	        throw new RuntimeException(e);
	    }
    }

    @Override
    public List<OSObject> findByValue(String attributeName, Object value) {
        Set<Long> ids;
        try {
            if (value instanceof String) {
            	ids = connector.findByAttributeValue(jobId, attributeName, (String) value);
            } else if (value instanceof Boolean) {
                ids = connector.findByAttributeValue(jobId, attributeName, (Boolean) value);
            } else if (value instanceof Long) {
            	ids = connector.findByAttributeValue(jobId, attributeName, (Long) value);
            } else if (value instanceof OSObject) {
            	ids = connector.findByObjectId(jobId, attributeName, ((OSObject) value).getId());
            } else if (value instanceof Integer) {
            	ids = connector.findByAttributeValue(jobId, attributeName, (Integer) value);
            } else {
                throw new RuntimeException("Unknown object type for: " + value);
            }

            return osObjects(ids);
        } catch (ObjectStoreConnectorException e) {
	        throw new RuntimeException(e);
	    }

    }

    @Override
    public void addAttribute(long id, String key, Object value)  {
        Map<String, Object> obj = get(id);
        obj.put(key, value);
        try {
        	Attribute attribute = new Attribute(key);
        	if (value instanceof Reference) {
        		attribute.setDataRef((Reference) value);
        	} else if (value instanceof OSObject){
        		attribute.setObjectRef(((OSObject)value).getId());
        	} else {
        		attribute.setSimpleValue(value);
        	}

        	connector.updateObjectStoreData(jobId, Arrays.asList(
            		new ObjectDataBuilder().setId(id).addAttribute(attribute).build()));
        } catch (ObjectStoreConnectorException e) {
        	LOG.error("Failed to update data in the ObjectStore (jobId={}, dataId={}, attrName={}, value={}, responseType={}, responseMsg={})",
        			new Object[]{jobId, id, key, value,e.getResponseType(), e.getError()});
        	throw new IllegalStateException(e);
	    }
    }

    private void insertIntoCache(long id, Map<String, Object> map) {
        cache.put(id, map);
    }

    private Map<String, Object> getFromCache(long id) {
        return cache.get(id);
    }

    private boolean inCache(long id) {
        return cache.containsKey(id);
    }

    private List<OSObject> osObjects(Set<Long> ids) {

        List<OSObject> list = new ArrayList<OSObject>(ids.size());
        for (Long id: ids) {
            list.add(new OSObject(this, id));
        }
        return list;
    }
}
