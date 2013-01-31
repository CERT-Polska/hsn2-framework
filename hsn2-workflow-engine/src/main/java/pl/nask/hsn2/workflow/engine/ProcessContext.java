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
package pl.nask.hsn2.workflow.engine;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.activiti.IntIdGen;
import pl.nask.hsn2.activiti.suppressor.TasksSuppressor;
import pl.nask.hsn2.framework.workflow.job.DefaultTasksStatistics;

public class ProcessContext {	
	private final static Logger LOG = LoggerFactory.getLogger(ProcessContext.class); 
	
	private final long jobId;
	private final SubprocessParameters subprocessParameters;
	private final Map<String, Properties> userConfig;
	private final IntIdGen taskIdGenerator;
	private final DefaultTasksStatistics stats;
	private final List<ExecutionWrapper> waitingForResume;
	private final TasksSuppressor tasksSuppressor;
	
	private Integer currentTaskId;
	private int taskAccepted;
	
	public ProcessContext(long jobId, Map<String, Properties> userConfig, SubprocessParameters subprocessParams, DefaultTasksStatistics stats, TasksSuppressor tasksSuppressor) {
		this(jobId, userConfig, subprocessParams, stats, new IntIdGen(), new LinkedList<ExecutionWrapper>(), tasksSuppressor);
	}

	private ProcessContext(long jobId, Map<String, Properties> userConfig, SubprocessParameters subprocessParams, DefaultTasksStatistics stats, IntIdGen intIdGen, List<ExecutionWrapper> waitingForResume, TasksSuppressor tasksSuppressor) {
		this.stats = stats;
		this.jobId = jobId;
		this.subprocessParameters = subprocessParams;
		if (userConfig == null) {
			this.userConfig = Collections.emptyMap();
		} else {
			this.userConfig = userConfig;
		}	
		this.taskIdGenerator = intIdGen;
		this.waitingForResume = waitingForResume;
		this.tasksSuppressor = tasksSuppressor;
	}

	int newTaskId() {
		this.currentTaskId = taskIdGenerator.nextId();
		taskAccepted = 0;
		return currentTaskId;
	}

	public Long getJobId() {
		return jobId;
	}

	public Integer getTaskId() {		
		return currentTaskId;
	}

	public Map<String, Properties> getUserConfig() {
		return userConfig;
	}

	public SubprocessParameters getSubprocessParameters() {
		return subprocessParameters;
	}

	public List<ExecutionWrapper> getWaitingForResume() {
		return waitingForResume;
	}

	public DefaultTasksStatistics getJobStats() {
		return stats;
	}

	public ProcessContext subprocessContext(SubprocessParameters subprocessParams) {
		return new ProcessContext(jobId, userConfig, subprocessParams, stats, taskIdGenerator, waitingForResume, tasksSuppressor);
	}

	public void markTaskAsAccepted() {
		this.taskAccepted ++;
		if (taskAccepted > 1) {
			LOG.warn("got {} TaskAccepted messages for jobid={}, taskId={}", new Object[]{taskAccepted, jobId, currentTaskId});
		}
	}

	public TasksSuppressor getTasksSuppressor() {
		return tasksSuppressor;
	}
}
