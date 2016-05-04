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

package pl.nask.hsn2.expressions;

import java.util.List;

import pl.nask.hsn2.os.OSObject;
import pl.nask.hsn2.os.ObjectStore;
import pl.nask.hsn2.os.OgnlRootObject;


public final class OgnlRootObjectImpl implements OgnlRootObject {

    private final ObjectStore objectStore;
    private OSObject current;

    public OgnlRootObjectImpl(ObjectStore objectStore, long osId) {
        this.objectStore = objectStore;
        this.current = new OSObject(objectStore, osId);
    }

    /* (non-Javadoc)
     * @see pl.nask.hsn2.expressions.OgnlRootObject#findByName(java.lang.String)
     */
    @Override
    public List<OSObject> findByName(String attributeName) {
        return objectStore.findByName(attributeName);
    }

    /* (non-Javadoc)
     * @see pl.nask.hsn2.expressions.OgnlRootObject#findByValue(java.lang.String, java.lang.String)
     */
    @Override
    public List<OSObject> findByValue(String attributeName, String value) {
        return objectStore.findByValue(attributeName, value);
    }

    /* (non-Javadoc)
     * @see pl.nask.hsn2.expressions.OgnlRootObject#findByValue(java.lang.String, pl.nask.hsn2.expressions.OSObject)
     */
    @Override
    public List<OSObject> findByValue(String attributeName, OSObject value) {
        return objectStore.findByValue(attributeName, value.getId());
    }

    public OSObject getCurrent() {
        return current;
    }
}
