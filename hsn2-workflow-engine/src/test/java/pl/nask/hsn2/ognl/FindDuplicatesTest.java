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

import java.util.List;

import org.apache.commons.ognl.OgnlContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.expressions.OgnlExpression;
import pl.nask.hsn2.os.OSObject;
import pl.nask.hsn2.os.ObjectStore;
import pl.nask.hsn2.os.OgnlRootObject;

public class FindDuplicatesTest {
    private ObjectStore os = new MockedObjectStore();

    private OgnlContext context;
    private OgnlRootObject rootObject;
    Object value;


    @BeforeMethod
    public void beforeMethod() {
        context = new OgnlContext();
        rootObject(1);
    }

    private void rootObject(int id) {
        rootObject = new OSObject(os, id);
        context.put("current", rootObject);
    }


    @Test
    public void testFindNaskPl() throws Exception {
        String ex = "findByValue('url', 'nask.pl')";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals(1, ((List) value).size());
    }

    @Test
    public void testFindCertPl() throws Exception {
        String ex = "findByValue('url', 'cert.pl')";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals(2, ((List) value).size());
    }


    @Test
    public void testFindNaskCheckNotDuplicated() throws Exception {
        String ex = "findByValue('url', 'nask.pl').{^ #this.duplicated == null}";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals(1, ((List) value).size());
    }

    @Test
    public void testFindNaskCheckDuplicated() throws Exception {
        String ex = "findByValue('url', 'nask.pl').{^ #this.duplicated != null}";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals(0, ((List) value).size());
    }

    @Test
    public void testFindNaskCheckNotTheSame() throws Exception {
        String ex = "findByValue('url', 'nask.pl').{^ #current != #this}";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals(0, ((List) value).size());
    }

    @Test
    public void testFindCertCheckNotTheSame() throws Exception {
        rootObject(2);
        String ex = "findByValue('url', 'cert.pl').{^ #current != #this}";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals(1, ((List) value).size());
    }


    @Test
    public void testCertFindDuplicates() throws Exception {
        rootObject(2);
        String ex = "findByValue('url', 'cert.pl').{^ ((#this != #current) && (#this.duplicated == null))}";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertEquals(1, ((List) value).size());
    }


    @Test
    public void testSaveDuplicatedLocal() throws Exception {
        String ex = "#dupe = findByValue('url', 'nask.pl').{^ (#this != #current) && (#this.duplicated == null)}, " +
                " #dupe.isEmpty";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertTrue(((Boolean) value).booleanValue());
    }

    @Test
    public void testMarkAsDuplicated() throws Exception {
        String ex = "#dupe = findByValue('url', 'nask.pl').{^ (#this != #current) && (#this.duplicated == null)}, " +
                " #dupe.isEmpty ? null : (#current.duplicated = #dupe[0])";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertNull(value);
    }

    @Test
    public void testMarkCertAsDuplicated() throws Exception {
        rootObject(2);
        String ex = "#dupe = findByValue('url', 'cert.pl').{^ (#this != #current) && (#this.duplicated == null)}, " +
                " #dupe.isEmpty ? null : (#current.duplicated = #dupe[0])";
        OgnlExpression expr = new OgnlExpression(ex);

        value = expr.getValue(context , rootObject);
        System.out.println(value);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof OSObject);
    }
}
