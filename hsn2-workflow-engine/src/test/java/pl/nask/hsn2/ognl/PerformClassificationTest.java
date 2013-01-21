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

package pl.nask.hsn2.ognl;

import java.util.List;

import org.apache.commons.ognl.OgnlContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.expressions.OgnlExpression;
import pl.nask.hsn2.os.OSObject;
import pl.nask.hsn2.os.ObjectStore;
import pl.nask.hsn2.os.OgnlRootObject;

public class PerformClassificationTest {
    private ObjectStore os;

    private OgnlContext context;
    private OgnlRootObject rootObject;
    Object value;


    @BeforeMethod
    public void beforeMethod() {
        os = new MockedObjectStore();
        context = new OgnlContext();
        rootObject(2);
    }

    private void rootObject(int id) {
        rootObject = new OSObject(os, id);
        context.put("current", rootObject);
    }


    @Test
    public void testFindChildren() throws Exception {
        String ex = "findByValue('parent', #current)";
        OgnlExpression expr = new OgnlExpression(ex);
        value = expr.getValue(context , rootObject);
        Assert.assertTrue(((List) value).size() > 0);
        System.out.println(value);
    }

    @Test
    public void testFindNonClassifiedChildren() throws Exception {
        String ex = "findByValue('parent', #current).{? #this.redirect and #this.classification == 'malicious'}";
        OgnlExpression expr = new OgnlExpression(ex);
        value = expr.getValue(context , rootObject);
        Assert.assertTrue(((List) value).isEmpty());
        System.out.println(value);
    }



    @Test
    public void testPerformClassification() throws Exception {
        String ex = "!findByValue('parent', #current).{? #this.redirect and #this.classification == 'malicious'}.isEmpty " +
                "or js_classification == 'malicious' or hpc_classification == 'malicious' " +
                "? (#current.classification = 'malicious') : (#current.classification = 'benign')";
        OgnlExpression expr = new OgnlExpression(ex);
        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals("benign", ((String) value));
    }
}
