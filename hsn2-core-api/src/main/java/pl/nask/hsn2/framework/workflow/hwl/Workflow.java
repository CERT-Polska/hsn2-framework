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

package pl.nask.hsn2.framework.workflow.hwl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import pl.nask.hsn2.framework.workflow.builder.WorkflowBuilder;

@XmlRootElement(name="workflow")
public class Workflow implements Transformable {
    @XmlAttribute
    private String name;

    @XmlElement
    private String description;

    @XmlElement(name="process")
    private List<ProcessDefinition> processes;

    public Workflow() {
    }

    public Workflow(String name) {
        this.name = name;
    }

    public final void addProcess(ProcessDefinition def) {
        if (processes == null)
            processes = new ArrayList<ProcessDefinition>();
        processes.add(def);
    }

    @XmlTransient
    public final List<ProcessDefinition> getProcessDefinitions() {
        return processes;
    }

    @XmlTransient
    public final String getName() {
        return name;
    }

    @Override
    public final void transformToWorkflow(WorkflowBuilder builder) {
        builder.buildWorkflow(name, processes);
    }

    public final String getDescription() {
        return description;
    }
}
