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
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.workflow.builder.WorkflowBuilder;

@XmlRootElement
public class Service implements ExecutionPoint {

    @XmlAttribute(required=true)
    private String name;

    @XmlAttribute(required=false, name="id")
    private String id;
    
    @XmlAttribute(name="ignore_errors")
    private List<TaskErrorReasonType> ignoreErrors;

    @XmlElements({
    	@XmlElement(name="output", type=Output.class),
    	@XmlElement(name="parameter", type=ServiceParam.class)
    })
    private List<Object> outputOrParameter;

	public Service() {
	}

	public Service(String name) {
		this.name = name;
	}

	public final String getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final void addParam(String name, String value) {
		addParam(new ServiceParam(name, value));
	}

	public final void addParam(ServiceParam parameter) {
		addOutputOrParameter(parameter);
	}

	@Override
	public final List<Output> getOutputs() {
		return filterOutputOrParams(Output.class);
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> filterOutputOrParams(Class<T> clazz) {
		if (outputOrParameter == null)
			return Collections.emptyList();

		List<T> res = new ArrayList<T>();
		for (Object o : outputOrParameter) {
			if (clazz.isAssignableFrom(o.getClass())) {
				res.add((T) o);
			}
		}
		return res;
	}

	private List<ServiceParam> getParameters() {
		return filterOutputOrParams(ServiceParam.class);
	}

	@Override
	public final void transformToWorkflow(WorkflowBuilder builder) {
		builder.addService(name, id, getParameters(), getOutputs(),ignoreErrors);
	}

	private void addOutputOrParameter(Object o) {
		if (outputOrParameter == null)
			outputOrParameter = new ArrayList<Object>();
		outputOrParameter.add(o);
	}

	public final void addOutput(Output output) {
		addOutputOrParameter(output);
	}

	@Override
	public final List<? extends Service> getAllServices() {
		return Collections.singletonList(this);
	}

	public final List<TaskErrorReasonType> getIgnoreErrors() {
		return ignoreErrors;
	}

	public final void addIgnoreErrors(TaskErrorReasonType taskErrorType) {
		if (ignoreErrors == null)
			ignoreErrors = new ArrayList<TaskErrorReasonType>();
		ignoreErrors.add(taskErrorType);
	}
}
