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

package pl.nask.hsn2.workflow.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

public final class WorkflowValidator {
    private Set<String> serviceNames = new HashSet<String>();

    public WorkflowValidator(String[] serviceNames) {
        this.serviceNames.addAll(Arrays.asList(serviceNames));
    }

    public void validateAll(Workflow workflow) throws WorkflowValidationException {
        Map<String, ProcessDefinition> processMap = buildProcessMap(workflow);
        validateMainProcessExists(workflow, processMap);
        validateCalledProcessExists(workflow, processMap);
        validateServiceNames(workflow);
    }

    private void validateServiceNames(Workflow workflow) throws WorkflowValidationException {
        Set<String> unknownServices = new HashSet<String>();
        for (ProcessDefinition pd: workflow.getProcessDefinitions()) {
            List<Service> services = pd.getAllServices();
            for (Service s: services) {
                if (!serviceNames.contains(s.getName())) {
                    unknownServices.add(s.getName());
                }
            }
        }
        if (!unknownServices.isEmpty()) {
            throw new WorkflowValidationException(workflow, "There is no queue defined in the config for this services: " + unknownServices);
        }
    }

    void validateCalledProcessExists(Workflow workflow, Map<String, ProcessDefinition> processMap) throws WorkflowValidationException {
        for (ProcessDefinition pd: workflow.getProcessDefinitions()) {
            List<Output> outputs = pd.getOutputs();
            for (Output o: outputs) {
                if (!processMap.containsKey(o.getProcessName())) {
                    throw new WorkflowValidationException(workflow, "output uses unknown processName: " + o.getProcessName());
                }
            }
        }

    }

    void validateMainProcessExists(Workflow workflow, Map<String, ProcessDefinition> processMap) throws WorkflowValidationException {
        if (!processMap.containsKey("main"))
            throw new WorkflowValidationException(workflow, "main process not found");
    }

    private Map<String, ProcessDefinition> buildProcessMap(Workflow workflow) {
        Map<String, ProcessDefinition> processMap = new HashMap<String, ProcessDefinition>();
        for (ProcessDefinition pd: workflow.getProcessDefinitions()) {
            processMap.put(pd.getId(), pd);
        }
        return processMap;
    }
}
