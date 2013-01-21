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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*	
 * Comparable is implemented due to a bug in OGNL which forces a non-numeric object to implement comparable for ==/
 */
public class OSObject implements Map<String, Object>, Comparable<OSObject>, OgnlRootObject {
    private Map<String, Object> object;
    protected final ObjectStore objectStore;
    private final long id;

    public OSObject(ObjectStore os, long id) {
        this.id = id;
        objectStore = os;
    }

    public long getId() {
        return id;
    }

    @Override
    public Object get(Object key) {
        ensureInitialized();
        return object.get(key);
    }

    private void ensureInitialized() {
        if (object == null)
            object = objectStore.get(id);
    }

    @Override
    public int size() {
        ensureInitialized();
        return object.size();
    }

    @Override
    public boolean isEmpty() {
        ensureInitialized();
        return object.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        ensureInitialized();
        return object.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        ensureInitialized();
        return object.containsValue(value);
    }


    @Override
    public Object put(String key, Object value) {
        // TODO: this is a OS operation !!!. shall be performed in the OS also...
        ensureInitialized();
        objectStore.addAttribute(id, key, value);
        return object.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> keySet() {
        ensureInitialized();
        return object.keySet();
    }

    @Override
    public Collection<Object> values() {
        ensureInitialized();
        return object.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        ensureInitialized();
        return object.entrySet();
    }

    @Override
    public String toString() {
        return String.format("OSO(id=%s, values=%s)", id, object == null ? "notInitialized" : object);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OSObject other = (OSObject) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public int compareTo(OSObject o) {
        if (o == null) {
            return 1;
        } else {
            return Long.valueOf(id).compareTo(o.id);
        }
    }

    @Override
    public List<OSObject> findByName(String attributeName) {
        return objectStore.findByName(attributeName);
    }

    @Override
    public List<OSObject> findByValue(String attributeName, String value) {
        return objectStore.findByValue(attributeName, value);
    }

    @Override
    public List<OSObject> findByValue(String attributeName, OSObject value) {
        return objectStore.findByValue(attributeName, value);
    }
}
