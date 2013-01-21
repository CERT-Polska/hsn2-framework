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

package pl.nask.hsn2.test.framework.workflow.hwl;

import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.Test;

import pl.nask.hsn2.framework.workflow.hwl.ServiceParam;

public class ServiceParamTest {

	@Test
	public void mergeTest() {
		
		Properties p;
		
		// merge 2 nulls
		p = ServiceParam.merge(null, null, false);
		Assert.assertNotNull(p);
		Assert.assertTrue(p.isEmpty());

		p = ServiceParam.merge(null, null, true);
		Assert.assertNotNull(p);
		Assert.assertTrue(p.isEmpty());

		// merge 2 empty lists
		p = ServiceParam.merge(new Properties(), new Properties(), false);
		Assert.assertNotNull(p);
		Assert.assertTrue(p.isEmpty());

		p = ServiceParam.merge(new Properties(), new Properties(), true);
		Assert.assertNotNull(p);
		Assert.assertTrue(p.isEmpty());

		// merge 2 lists no override
		Properties p1, p2, p3;
		p1 = new Properties();
		p1.put("s1", "v1");
		p1.put("s2", "v2");
		p2 = new Properties();
		p2.put("t1", "v1");
		p2.put("t2", "v2");
		p = ServiceParam.merge(p1, p2, false);
		Assert.assertNotNull(p);
		Assert.assertTrue(!p.isEmpty());
		Assert.assertEquals(p1.size(), 2);
		Assert.assertEquals(p2.size(), 2);
		Assert.assertEquals(p.size(), 4);
		p3 = new Properties();
		p3.put("s1", "v5");
		p = ServiceParam.merge(p, p3, false);
		Assert.assertEquals(p.size(), 4);
		Assert.assertEquals(p.getProperty("s1"), "v1");

		// merge 2 lists override
		p1 = new Properties();
		p1.put("s1", "v1");
		p1.put("s2", "v2");
		p2 = new Properties();
		p2.put("t1", "v1");
		p2.put("s2", "v5");
		p = ServiceParam.merge(p1, p2, true);
		Assert.assertNotNull(p);
		Assert.assertTrue(!p.isEmpty());
		Assert.assertEquals(p1.size(), 2);
		Assert.assertEquals(p2.size(), 2);
		Assert.assertEquals(p.size(), 3);
		Assert.assertEquals(p.getProperty("s2"), "v5");
		p = ServiceParam.merge(p, p1, true);
		Assert.assertEquals(p.size(), 3);
		Assert.assertEquals(p.getProperty("s2"), "v2");
	}
}
