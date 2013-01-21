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

package pl.nask.hsn2.test.framework.workflow.engine;

import junit.framework.Assert;

import org.testng.annotations.Test;

import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;

public class ProcessDefinitionRegistryTest {

	@Test
	public void testConstructor() {
		ProcessDefinitionRegistry<Long> registry = new ProcessDefinitionRegistry<Long>();
		
		Assert.assertNotNull(registry.getDefinitions());
		Assert.assertEquals(0, registry.getDefinitions().size());
	}

	@Test
	public void testAdd() {
		ProcessDefinitionRegistry<Long> registry = new ProcessDefinitionRegistry<Long>();
		Long id1 = new Long(1);
		Long id2 = new Long(2);
		
		registry.add("id-1", id1);
		Assert.assertEquals(registry.getDefinition("id-1"), id1);
		
		registry.add("id-2", id2);
		Assert.assertEquals(registry.getDefinition("id-2"), id2);
		
		Assert.assertEquals(registry.getDefinitions().size(), 2);
	}
}
