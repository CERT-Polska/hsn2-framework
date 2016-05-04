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

import org.apache.commons.ognl.Node;
import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.expressions.OgnlExpression;
import pl.nask.hsn2.os.OSObject;
import pl.nask.hsn2.os.ObjectStore;
import pl.nask.hsn2.os.OgnlRootObject;

public class OgnlPocTest {
    ObjectStore os = new MockedObjectStore();

    OgnlExpression expr;
    OgnlContext context;
    OgnlRootObject rootObject;

    @BeforeMethod
    public void beforeMethod() {
        expr = null;
        rootObject = new OSObject(os, 1);
        context = new OgnlContext();
        context.put("current", rootObject);
    }

    @Test
    public void testFindByName() throws OgnlException {
        expr = new OgnlExpression("findByName('url')");
        System.out.println(expr.getValue(context , rootObject));
    }

    private OSObject get(int id) {
        return new OSObject(os, id);
    }

    @Test
    public void testFindByValue() throws OgnlException {
        expr = new OgnlExpression("findByValue('url', 'nask.pl')");
        System.out.println(expr.getValue(context , rootObject));
    }

    @Test
    public void testLocalAssignment() throws OgnlException {
        expr = new OgnlExpression("#test = findByName('url')");
        System.out.println(expr.getValue(context , rootObject));
    }

    @Test
    public void testObjectStoreObjectAccess() throws OgnlException {
        expr = new OgnlExpression("#current.url");
        System.out.println(expr.getValue(context , rootObject));
    }

    @Test
    public void testCollectionSelector() throws OgnlException {
        expr = new OgnlExpression("findByName('url').{^ #this.id == 1}");
        System.out.println(expr.getValue(context , rootObject));
    }

    @Test
    public void testCollectionSelectorCompiled() throws Exception {
        Node expr = Ognl.compileExpression(context, rootObject, "findByName('url').{^ #this.id == 1}");
        System.out.println(expr.getValue(context , rootObject));
    }
}
