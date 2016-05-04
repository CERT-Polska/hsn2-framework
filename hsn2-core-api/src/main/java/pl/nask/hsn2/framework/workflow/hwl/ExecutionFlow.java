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

package pl.nask.hsn2.framework.workflow.hwl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ExecutionFlow {

    @XmlAnyElement
    @XmlElementRefs({
        @XmlElementRef(type=Conditional.class),
        @XmlElementRef(type=Service.class),
        @XmlElementRef(type=Parallel.class),
        @XmlElementRef(type=Wait.class),
        @XmlElementRef(type=Script.class)
    })
    protected List<ExecutionPoint> executionPoints;


    protected ExecutionFlow () {
    }

    public ExecutionFlow(ExecutionPoint... service) {
        executionPoints = new ArrayList<ExecutionPoint>(Arrays.asList(service));
    }

    public final void addExecutionPoint(ExecutionPoint executionPoint) {
        if (executionPoints == null)
            executionPoints = new ArrayList<ExecutionPoint>();
        executionPoints.add(executionPoint);
    }

    public final List<ExecutionPoint> getExecutionPoints() {
        return executionPoints;
    }

    public final List<Output> getOutputs() {
        if (executionPoints != null) {
            List<Output> outputs = new ArrayList<Output>();
            for (ExecutionPoint p: executionPoints) {
                List<Output> o = p.getOutputs();
                outputs.addAll(o);
            }
            return outputs;
        } else {
            return Collections.emptyList();
        }
    }

    public final boolean isEmpty() {
        return (executionPoints == null) ? true : executionPoints.isEmpty();
    }

    public final List<Service> getAllServices() {
        if (executionPoints != null) {
            List<Service> services = new ArrayList<Service>();
            for (ExecutionPoint p: executionPoints) {
                services.addAll(p.getAllServices());
            }
            return services;
        } else {
            return Collections.emptyList();
        }
    }
}
