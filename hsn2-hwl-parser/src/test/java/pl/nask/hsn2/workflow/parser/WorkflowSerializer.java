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

import java.io.PrintStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import pl.nask.hsn2.framework.workflow.hwl.Conditional;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionFlow;
import pl.nask.hsn2.framework.workflow.hwl.Parallel;
import pl.nask.hsn2.framework.workflow.hwl.Script;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Wait;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

public class WorkflowSerializer {
    private JAXBContext ctx;
    private Marshaller marshaller;

    public WorkflowSerializer() throws JAXBException {
        ctx = JAXBContext.newInstance(
                Workflow.class,
                Process.class,
                Service.class,
                Conditional.class,
                Parallel.class,
                ExecutionFlow.class,
                Wait.class,
                Script.class);
        marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    }

    public void marshall(Workflow workflow, PrintStream out) throws JAXBException {
        JAXBElement<Workflow> jaxbElement = new JAXBElement<Workflow>(new QName("workflow"), Workflow.class, workflow );
        marshaller.marshal(jaxbElement, out);
    }

    public void marshall(Workflow workflow, StringWriter writer) throws JAXBException {
        JAXBElement<Workflow> jaxbElement = new JAXBElement<Workflow>(new QName("workflow"), Workflow.class, workflow );
        marshaller.marshal(jaxbElement, writer);
    }
}
