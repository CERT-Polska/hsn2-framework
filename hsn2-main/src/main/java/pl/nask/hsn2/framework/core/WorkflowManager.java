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

package pl.nask.hsn2.framework.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.workflow.engine.ProcessBasedWorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowAlreadyDeployedException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowAlreadyRegisteredException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;
import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptorManager;
import pl.nask.hsn2.framework.workflow.engine.WorkflowEngine;
import pl.nask.hsn2.framework.workflow.engine.WorkflowEngineException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowNotDeployedException;
import pl.nask.hsn2.framework.workflow.engine.WorkflowNotRegisteredException;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;
import pl.nask.hsn2.framework.workflow.job.WorkflowJobInfo;
import pl.nask.hsn2.framework.workflow.repository.WorkflowRepoException;
import pl.nask.hsn2.framework.workflow.repository.WorkflowRepository;
import pl.nask.hsn2.framework.workflow.repository.WorkflowVersionInfo;
import pl.nask.hsn2.workflow.parser.HWLParser;
import pl.nask.hsn2.workflow.parser.WorkflowParseException;
import pl.nask.hsn2.workflow.parser.WorkflowParser;
import pl.nask.hsn2.workflow.parser.WorkflowSyntaxException;
import pl.nask.hsn2.workflow.parser.WorkflowValidationException;
import pl.nask.hsn2.workflow.parser.WorkflowValidator;

public final class WorkflowManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(WorkflowManager.class);

    private final static WorkflowManager INSTANCE = new WorkflowManager();

    private WorkflowParser parser = new HWLParser();

    private WorkflowDescriptorManager<? extends WorkflowDescriptor> workflowDefinitionManager;

    private WorkflowEngine workflowEngine;

    private WorkflowRepository repository;

    private WorkflowValidator validator;
    
    private int maximumRunningJobLimit = 0; // unlimited by default

    private WorkflowManager() {
    }

    public static WorkflowManager getInstance() {

    	if (INSTANCE.workflowEngine == null) {
    		throw new IllegalStateException("WorkflowManager not configured properly: workflowEngine is missing");
    	}
    	
        if (INSTANCE.workflowDefinitionManager == null) {
            throw new IllegalStateException("WorkflowManager not configured properly: workflowDefinitionManager is missing");
        }

        if (INSTANCE.validator == null) {
            throw new IllegalStateException("WorkflowManager not configured properly: validator is not initialized (setKnownServiceNames was not called)");
        }

        if (INSTANCE.repository == null) {
            throw new IllegalStateException("WorkflowManager not configured properly: repository is not initialized");
        }
        return INSTANCE;
    }

    /**
     * Initializes workflow validator
     *
     * @param serviceNames
     */
    public static void setKnownServiceNames(String[] serviceNames) {
        INSTANCE.validator = new WorkflowValidator(serviceNames.clone());
    }

    /**
     * Starts a job (workflow process instance) for the given workflowId
     *
     * @param workflowName
     * @param workflowConfig
     * @return id of the started job
     * @throws WorkflowNotDeployedException
     * @throws WorkflowRepoException
     */
    public long runJob(String workflowName, Map<String, Properties> workflowConfig, String workflowVersion) throws WorkflowNotDeployedException, WorkflowEngineException, WorkflowRepoException  {
        String version = getCurrentVersionIfEmpty(workflowName, workflowVersion);
        WorkflowDescriptor w = workflowDefinitionManager.get(version);

        if (w == null) {
            w = deployWorkflow(workflowName, version);
        }

        return workflowEngine.startJob(w, "main", workflowConfig);
    }

    public String getCurrentVersionIfEmpty(String workflowName, String workflowVersion) throws WorkflowRepoException {
        if (workflowVersion != null && !"".equals(workflowVersion)){
            return workflowVersion;
        }

        List<WorkflowVersionInfo> versions = repository.getVersions(workflowName);
        if (versions == null || versions.isEmpty()) {
            throw new WorkflowRepoException("Workflow cannot be found: " + workflowName);
        } else {
            return versions.get(0).getVersion();
        }
    }

    public WorkflowVersionInfo getWorkflowVersionInfo(String workflowName, String workflowVersion) throws WorkflowRepoException {

		List<WorkflowVersionInfo> versions = repository.getVersions(workflowName);

		if (workflowVersion == null || "".equals(workflowVersion)) {

			// Is there is any version of the workflow? 
			if (versions == null || versions.isEmpty()) {
                throw new WorkflowRepoException("Workflow cannot be found: " + workflowName);
            }

			// take HEAD
    		return (WorkflowVersionInfo) versions.get(0);
    	} else {
    		for (WorkflowVersionInfo v : versions) {
    			if (workflowVersion.equals(v.getVersion())) {
    				return v;
    			}
    		}
    		throw new WorkflowRepoException(
    				new StringBuffer("Version '")
    				.append(workflowVersion).append("' ")
    				.append("not found for workflow name '")
    				.append(workflowName).append("'.").toString());
    	}
    }
    
    private WorkflowDescriptor deployWorkflow(String workflowName, String version) throws WorkflowNotDeployedException {
        InputStream is = null;
        try {
            is = repository.getWorkflowFile(workflowName, version);
            Workflow workflow = parser.parse(is);
            WorkflowDescriptor desc = workflowDefinitionManager.createDescritor(version, workflowName, workflow);
            workflowDefinitionManager.registerWorkflow(desc);

            validator.validateAll(workflow);
            workflowDefinitionManager.deploy(version);
            return desc;
        } catch (WorkflowRepoException e) {
            LOGGER.error("Couldn't retrieve workflow from the repository, name={}, version={}", workflowName, version);
            throw new WorkflowNotDeployedException(workflowName, e);
        } catch (WorkflowSyntaxException e) {
            LOGGER.error("Workflow syntax invalid: {}, name={}, version={}", new Object[]{e.getMessage(), workflowName, version});
            throw new WorkflowNotDeployedException("Workflow syntax invalid:" + workflowName, e);
        } catch (WorkflowParseException e) {
            LOGGER.error("Workflow parse error: {}, name={}, version={}", new Object[]{e.getMessage(), workflowName, version});
            throw new WorkflowNotDeployedException("Workflow parse error:" + workflowName, e);
        } catch (WorkflowValidationException e) {
            LOGGER.error("Workflow validation error: {}, name={}, version={}", new Object[] {e.getMessage(), workflowName, version});
            throw new WorkflowNotDeployedException("Workflow validation error:" + workflowName, e);
		} catch (WorkflowAlreadyRegisteredException e) {
			throw new WorkflowNotDeployedException(workflowName, e);
		} catch (WorkflowAlreadyDeployedException e) {
			throw new WorkflowNotDeployedException(workflowName, e);
		} catch (WorkflowNotRegisteredException e) {
			throw new WorkflowNotDeployedException(workflowName, e);
		} finally {
   			IOUtils.closeQuietly(is);
        }

    }

    public List<WorkflowDescriptor> getWorkflowDefinitions(boolean enabledOnly) throws WorkflowRepoException {
        List<WorkflowDescriptor> res = new ArrayList<WorkflowDescriptor>(workflowDefinitionManager.getWorkflowDefinitions(enabledOnly));
        Set<String> names = new HashSet<String>();
        for (WorkflowDescriptor d: res) {
            names.add(d.getName());
        }
        if (!enabledOnly) {
	        List<String> workflowsInRepo = repository.listWorkflowNames();
	        for (String name: workflowsInRepo) {
	            if (!names.contains(name)) { //non-nulls are already on the list
                	ProcessBasedWorkflowDescriptor<PvmProcessDefinition> desc = new ProcessBasedWorkflowDescriptor<PvmProcessDefinition>(null, name, null);
                    res.add(desc);
                }
            }
        }

        return res;
    }

	public WorkflowDescriptor getWorkflowDefinition(
			String workflowName, String workflowVersion)
			throws WorkflowRepoException {
		
    	String version = getCurrentVersionIfEmpty(workflowName, workflowVersion);
    	WorkflowDescriptor w = workflowDefinitionManager.get(version);

    	if (w != null) {
    		return w;
    	}
    	
    	InputStream is = null;
    	Workflow workflow = null;
    	try {
	    	is = repository.getWorkflowFile(workflowName, version);
	        workflow = parser.parse(is);
    	} catch (WorkflowSyntaxException e) {
    		// not important, in this case workflow will be null
		} catch (WorkflowParseException e) {
    		// not important, in this case workflow will be null
		} finally {
   			IOUtils.closeQuietly(is);
    	}
        return new ProcessBasedWorkflowDescriptor<PvmProcessDefinition>(version, workflowName, workflow);
    }
    
    public void taskAccepted(long jobId, int requestId) {
        workflowEngine.taskAccepted(jobId, requestId);
    }

    public void taskCompleted(long jobId, int requestId, Set<Long> newObjects) {
        workflowEngine.taskCompleted(jobId, requestId, newObjects);
    }

    public List<WorkflowJobInfo> getWorkflowJobs() {
        return workflowEngine.getJobs();
    }

    public WorkflowJobInfo getJobInfo(long jobId) {
        return workflowEngine.getJobInfo(jobId);
    }

    public void taskError(long jobId, int requestId, TaskErrorReasonType reason,
            String description) {
        workflowEngine.taskError(jobId, requestId, reason, description);
    }

    public static void setWorkflowDefinitionManager(WorkflowDescriptorManager<? extends WorkflowDescriptor> mgr) {
        INSTANCE.workflowDefinitionManager = mgr;
    }

    public static void setWorkflowRepository(WorkflowRepository repo) {
        INSTANCE.repository = repo;
    }

    public static void setWorkflowEngine(WorkflowEngine engine) {
        INSTANCE.workflowEngine = engine;
    }

    public static void setMaximumRunningJobLimit(int maximumRunningJobLimit) {
    	INSTANCE.maximumRunningJobLimit = maximumRunningJobLimit;
	}

	public void resume(long jobId) {
        workflowEngine.resume(jobId);
    }

    public String uploadWorkflow(String name, InputStream inputStream, boolean override) throws WorkflowRepoException {
        if (!override) {
            // check if the repo contains any version of the workflow
            List<WorkflowVersionInfo> versions = repository.getVersions(name);
            if (!versions.isEmpty()) {
                throw new IllegalArgumentException("Cannot override workflow with name: " + name);
            }
        }

        WorkflowVersionInfo info = repository.saveWorkflow(name, inputStream);
        return info.getVersion();
    }
 
    public List<WorkflowVersionInfo> getWorkflowHistory(String workflowName) throws WorkflowRepoException {
    	return repository.getVersions(workflowName);
    }
   
    public InputStream downloadWorkflow(String workflowName, String version) throws WorkflowRepoException{
    	return repository.getWorkflowFile(workflowName, getCurrentVersionIfEmpty(workflowName, version));
    }
    
    public boolean isMaximumProcessingJobsLimitExeeded() {
    	if (this.maximumRunningJobLimit > 0
    			&& workflowEngine.getJobsCount(JobStatus.PROCESSING)>=this.maximumRunningJobLimit) {
    		return true;
    	}
    	return false;
    }
}
