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

package pl.nask.hsn2.ognl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.nask.hsn2.os.OSObject;
import pl.nask.hsn2.os.ObjectStore;

public class MockedObjectStore implements ObjectStore {
    Map<Long, Map<String, Object>> map = new HashMap<Long, Map<String,Object>>();

    public MockedObjectStore() {
        map.put(1L, newObject(1, "nask.pl"));
        map.put(2L, newObject(2, "cert.pl"));
        Map<String, Object> c = newObject(3, "cert.pl");
        c.put("parent", 2L);
        map.put(3L, c);
    }

    private Map<String, Object> newObject(long id, String url) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", id);
        map.put("url", new String(url));
        return map;
    }

    /* (non-Javadoc)
     * @see pl.nask.hsn2.ognl.ObjectStore#get(long)
     */
    @Override
    public Map<String, Object> get(long id) {
        return map.get(id);
    }

    /* (non-Javadoc)
     * @see pl.nask.hsn2.ognl.ObjectStore#findByName(java.lang.String)
     */
    @Override
    public List<OSObject> findByName(String attributeName) {
        List<OSObject> res = new ArrayList<OSObject>();
        for (Map<String, Object> obj: map.values()) {
            if (obj.containsKey(attributeName)) {
                res.add(new OSObject(this, (Long) obj.get("id")));
            }
        }
        return res;
    }

    /* (non-Javadoc)
     * @see pl.nask.hsn2.ognl.ObjectStore#findByValue(java.lang.String, java.lang.Object)
     */
    @Override
    public List<OSObject> findByValue(String attributeName, Object value) {
        if (value instanceof OSObject)
            value = ((OSObject) value).getId();
        List<OSObject> res = new ArrayList<OSObject>();
        for (Map<String, Object> obj: map.values()) {
            if (obj.containsKey(attributeName) && value.equals(obj.get(attributeName))) {
                res.add(new OSObject(this, (Long) obj.get("id")));
            }
        }

        return res;
    }

    @Override
    public void addAttribute(long id, String key, Object value) {
        Map<String, Object> obj = map.get(id);
        obj.put(key, value);
    }
}
