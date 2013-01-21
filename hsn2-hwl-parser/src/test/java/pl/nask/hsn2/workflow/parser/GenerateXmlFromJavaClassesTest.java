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

import javax.xml.bind.JAXBException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pl.nask.hsn2.framework.workflow.hwl.Conditional;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionFlow;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.Parallel;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Wait;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

@Test
public class GenerateXmlFromJavaClassesTest {

    private WorkflowSerializer serializer;

    @BeforeMethod
    public void prepare() throws JAXBException {
        serializer = new WorkflowSerializer();
    }

    public void testCreateOneServiceXml() throws JAXBException {
        Workflow workflow = new Workflow();

        ProcessDefinition process = new ProcessDefinition("main");
        Service service = new Service("feeder");

        process.addExecutionPoint(service);

        workflow.addProcess(process);

        marshall("oneService", workflow);
    }

    public void testCreateConditionalXml() throws JAXBException {
        Conditional cond = new Conditional("this.depth > 9", new ExecutionFlow(new Service("crawler")), new ExecutionFlow(new Service("crawler2")));

        ProcessDefinition process = new ProcessDefinition("main");
        process.addExecutionPoint(cond);

        Workflow workflow = new Workflow();
        workflow.addProcess(process);

        marshall("conditional", workflow);
    }

    public void testCreateServiceParametersXml() throws JAXBException {
        Service service = new Service("crawler");
        service.addParam("download", "HTML");

        ProcessDefinition pd = new ProcessDefinition("main");
        pd.addExecutionPoint(service);

        Workflow w = new Workflow();
        w.addProcess(pd);

        marshall("serviceParameters", w);
    }

    public void testCreateParallelXml() throws JAXBException {
        Service service1 = new Service("service1");

        Service service2 = new Service("service2");

        Parallel p = new Parallel();
        p.addThread(service1);
        p.addThread(service2);

        ProcessDefinition pd = new ProcessDefinition("main");
        pd.addExecutionPoint(p);

        Workflow w = new Workflow();
        w.addProcess(pd);

        marshall("parallel", w);
    }

    public void testCreateTwoProcessesXml() throws JAXBException {
        ProcessDefinition main = new ProcessDefinition("main");
        Service feeder = new Service("feeder");
        feeder.addOutput(new Output("process_url"));
        main.addExecutionPoint(feeder);

        ProcessDefinition second = new ProcessDefinition("process_url");
        second.addExecutionPoint(new Service("crawler"));

        Workflow w = new Workflow();
        w.addProcess(main);
        w.addProcess(second);

        marshall("twoProcesses", w);
    }

    public void testCreateFullExampleXml() throws JAXBException {
        Service feeder = new Service("feeder");
        feeder.addParam("path", "file.txt");
        feeder.addParam("limit", "100");
        feeder.addOutput(new Output("process_url"));

        Wait wait = new Wait();

        Service exporter = new Service("exporter");
        exporter.addParam("format", "PDF");
        exporter.addParam("data", "all");

        ProcessDefinition main = new ProcessDefinition("main");
        main.addExecutionPoint(feeder);
        main.addExecutionPoint(wait);
        main.addExecutionPoint(exporter);

        Service crawler = new Service("crawler");
        crawler.addParam("download", "HTML");
        crawler.addOutput(new Output("process_url"));

        Parallel parallel = new Parallel();
        parallel.addThread(new Service("analyzer1"));
        parallel.addThread(new Service("analyzer2"));

        Conditional conditional = new Conditional("this.depth > 9", new ExecutionFlow(crawler), new ExecutionFlow(parallel));

        ProcessDefinition processUrl = new ProcessDefinition("process_url");
        processUrl.addExecutionPoint(conditional);

        Workflow w = new Workflow();
        w.addProcess(main);
        w.addProcess(processUrl);
        marshall("fullExample", w);
    }

    private void marshall(String caseName, Workflow workflow) throws JAXBException {
        System.out.println("\nGenerating  " + caseName);
        serializer.marshall(workflow, System.out);
    }
}
