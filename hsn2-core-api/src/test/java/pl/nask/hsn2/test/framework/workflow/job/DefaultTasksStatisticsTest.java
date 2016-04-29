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

package pl.nask.hsn2.test.framework.workflow.job;

import org.testng.Assert;
import org.testng.annotations.Test;

import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;

public class DefaultTasksStatisticsTest {

	@Test
	public void testConstructor() {
		DefaultTasksStatistics stats = new DefaultTasksStatistics();
		
		Assert.assertNotNull(stats.getStarted());
		Assert.assertNotNull(stats.getFinished());

		Assert.assertEquals(stats.getStarted().size(), 0);
		Assert.assertEquals(stats.getFinished().size(), 0);
	}
	
	@Test
	public void testStartedIncrements() {
		DefaultTasksStatistics stats = new DefaultTasksStatistics();
		
		stats.taskStarted("task1");
		Assert.assertEquals(stats.getStarted().size(), 1);
		Assert.assertEquals(stats.getFinished().size(), 0);
		
		stats.taskStarted("task2");
		Assert.assertEquals(stats.getStarted().size(), 2);
		Assert.assertEquals(stats.getFinished().size(), 0);
		
		stats.taskStarted("task1");
		Assert.assertEquals(stats.getStarted().size(), 2);
		Assert.assertEquals(stats.getFinished().size(), 0);
		Assert.assertEquals((int)stats.getStarted().get("task1"), (int)2);
		Assert.assertEquals((int)stats.getStarted().get("task2"), (int)1);
	}

	@Test
	public void testCompletedIncrements() {
		DefaultTasksStatistics stats = new DefaultTasksStatistics();
		
		stats.taskCompleted("task1");
		Assert.assertEquals(stats.getStarted().size(), 0);
		Assert.assertEquals(stats.getFinished().size(), 1);
		
		stats.taskCompleted("task2");
		Assert.assertEquals(stats.getStarted().size(), 0);
		Assert.assertEquals(stats.getFinished().size(), 2);
		
		stats.taskCompleted("task1");
		Assert.assertEquals(stats.getStarted().size(), 0);
		Assert.assertEquals(stats.getFinished().size(), 2);
		Assert.assertEquals((int)stats.getFinished().get("task1"), (int)2);
		Assert.assertEquals((int)stats.getFinished().get("task2"), (int)1);
	}
}
