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
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import pl.nask.hsn2.framework.workflow.builder.WorkflowBuilder;

@XmlRootElement
public class Conditional implements ExecutionPoint, Transformable {

    @XmlAttribute(name="expr", required=true)
    private String condition;

    @XmlElement(name="true")
    private ExecutionFlow onTrueFlow = new ExecutionFlow();

    @XmlElement(name="false")
    private ExecutionFlow onFalseFlow = new ExecutionFlow();

    @Override
    public final List<Output> getOutputs() {
        List<Output> outputs = new ArrayList<Output>();
        outputs.addAll(onTrueFlow.getOutputs());
        outputs.addAll(onFalseFlow.getOutputs());
        return outputs;
    }

    @Override
    public final List<? extends Service> getAllServices() {
        List<Service> services = new ArrayList<Service>();
        services.addAll(onTrueFlow.getAllServices());
        services.addAll(onFalseFlow.getAllServices());
        return services;
    }

    /* (non-Javadoc)
     * @see pl.nask.hsn2.workflow.hwl.Transformable#transformToWorkflow(pl.nask.hsn2.workflow.builder.WorkflowBuilder)
     */
    @Override
    public final void transformToWorkflow(WorkflowBuilder builder) {
        builder.addConditional(condition, onTrueFlow, onFalseFlow);
    }

    protected Conditional() {
    }

    public Conditional(String condition, ExecutionFlow onTrueFlow, ExecutionFlow onFalseFlow) {
        this.condition = condition;
        if (onTrueFlow != null)
            this.onTrueFlow = onTrueFlow;
        if (onFalseFlow != null )
            this.onFalseFlow = onFalseFlow;
    }

    @XmlTransient
    public final ExecutionFlow getFalseFlow() {
        return onFalseFlow;
    }

    @XmlTransient
    public final ExecutionFlow getTrueFlow() {
        return onTrueFlow;
    }

}
