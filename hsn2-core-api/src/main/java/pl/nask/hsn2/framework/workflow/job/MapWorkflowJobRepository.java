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

package pl.nask.hsn2.framework.workflow.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.utils.IdGenerator;
import pl.nask.hsn2.utils.IdGeneratorException;

/**
 * This is map based repository for jobs running in any workflow engine.
 * 
 * NOTE: The implementation MUST be thread-safe!
 *
 *
 */
public final class MapWorkflowJobRepository implements WorkflowJobRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(MapWorkflowJobRepository.class);
	
	private Map<Long, WorkflowJob> jobs = new ConcurrentHashMap<Long, WorkflowJob>();
	
	private IdGenerator idGenerator = null;
	
	public MapWorkflowJobRepository(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}
	
	@Override
	public WorkflowJob get(long id) {
		return jobs.get(id);
	}

	@Override
	public long add(WorkflowJob job) throws WorkflowJobRepositoryException {
		long id;
		try {
			id = idGenerator.nextId();
		} catch (IdGeneratorException e) {
			throw new WorkflowJobRepositoryException("Cannot generate id", e);
		}
		jobs.put(id, job);
		LOGGER.debug("Added job {} for workflow {}", id, job.getWorkflowName());
		return id;
	}

	@Override
	public List<WorkflowJobInfo> getJobs() {
		return new ArrayList<WorkflowJobInfo>(jobs.values());
	}

	@Override
	public void remove(long id) throws WorkflowJobRepositoryException {
		WorkflowJob job = jobs.get(id);
		if (job == null) {
			return;
		}
		if (!job.isEnded()) {
			throw new WorkflowJobRepositoryException("Running job cannot be removed from repository.");
		}
		jobs.remove(id);
	}

	@Override
	public void purgeEndedBefore(long timestamp)
			throws WorkflowJobRepositoryException {
		for (Map.Entry<Long, WorkflowJob> entry : jobs.entrySet()) {
			if (entry.getValue().isEnded() && entry.getValue().getEndTime() < timestamp) {
				jobs.remove(entry.getKey());
			}
		}
	}

}
