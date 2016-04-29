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

import java.io.File;

import mockit.Mocked;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import pl.nask.hsn2.framework.workflow.job.MapWorkflowJobRepository;
import pl.nask.hsn2.framework.workflow.job.WorkflowJob;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobRepository;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobRepositoryException;
import pl.nask.hsn2.utils.AtomicLongIdGenerator;
import pl.nask.hsn2.utils.FileIdGenerator;
import pl.nask.hsn2.utils.IdGenerator;
import pl.nask.hsn2.utils.IdGeneratorException;

public class MapWorkflowJobRepositoryTest {

	@Mocked
	private WorkflowJob job1;
	@Mocked
	private WorkflowJob job2;
	@Mocked
	private WorkflowJob job3;
	
	private WorkflowJobRepository multiRepo = new MapWorkflowJobRepository(new FileIdGenerator());
	
	@Test
	public void simpleTest() throws WorkflowJobRepositoryException {
		WorkflowJobRepository repo = new MapWorkflowJobRepository(new AtomicLongIdGenerator());
		long id = repo.add(job1);
		Assert.assertEquals(id, 1L);
		Assert.assertEquals(repo.get(id), job1);
		id = repo.add(job2);
		Assert.assertEquals(id, 2L);
		Assert.assertEquals(repo.get(id), job2);
		id = repo.add(job3);
		Assert.assertEquals(id, 3L);
		Assert.assertEquals(repo.get(id), job3);

		Assert.assertEquals(repo.getJobs().size(), 3);
	}

	@Test(expectedExceptions=WorkflowJobRepositoryException.class)
	public void simpleTestWithException() throws WorkflowJobRepositoryException {
		WorkflowJobRepository repo = new MapWorkflowJobRepository(new IdGenerator(){

			@Override
			public long nextId() throws IdGeneratorException {
				throw new IdGeneratorException("Cannot get nextId()");
			}

			@Override
			public void reset() throws IdGeneratorException {
			}
			
		});
		repo.add(null);
	}
	
	@Test(invocationCount=100, threadPoolSize=5)
	public void multithreadedTest() throws WorkflowJobRepositoryException {
		multiRepo.add(job1);
		multiRepo.add(job2);
		multiRepo.add(job3);
	}

	@BeforeTest
	public void setup() throws WorkflowJobRepositoryException {
		new File(".seq").delete();
	}

	@AfterTest
	public void cleanup() throws WorkflowJobRepositoryException {
		Assert.assertEquals(multiRepo.getJobs().size(), 100*3);
		Assert.assertEquals(multiRepo.add(job3), 301);
		new File(".seq").delete();
	}
}
