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

package pl.nask.hsn2.workflow.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import pl.nask.hsn2.framework.workflow.hwl.ExecutionPoint;
import pl.nask.hsn2.framework.workflow.hwl.Script;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Wait;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;


@Test(groups="hwlParseExample")
public class ParseExampleTest {

    WorkflowParser parser;
    WorkflowSerializer serializer;
    @BeforeTest
    public void prepare() throws JAXBException, IOException, SAXException {
        parser = new HWLParser();
        serializer = new WorkflowSerializer();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testParseConditional() throws WorkflowParseException {
        parseAndCompare("conditional.hwl.xml");
    }

    public void testParseFullExample() throws WorkflowParseException {
        String fileName = "/fullExample.hwl.xml";
        Workflow w = parser.parse(getClass().getResourceAsStream(fileName));
        String s = serialize(w);
        compareXml(s, getClass().getResourceAsStream(fileName));
    }

    public void testParseOneService() throws WorkflowParseException {
        parseAndCompare("oneService.hwl.xml");
    }

    public void testParseParallel() throws WorkflowParseException {
        parseAndCompare("parallel.hwl.xml");
    }

    public void testParseServiceParameters() throws WorkflowParseException {
        parseAndCompare("serviceParameters.hwl.xml");
    }

    public void testParseTwoProcesses() throws WorkflowParseException {
       parseAndCompare("twoProcesses.hwl.xml");
    }

    public void testParseWithScript() throws WorkflowParseException {
        Workflow w = parseAndCompare("script.hwl.xml");
        List<ExecutionPoint> execPoints = w.getProcessDefinitions().get(0).getExecutionPoints();
        Assert.assertEquals(execPoints.size(), 1);
        Assert.assertTrue(execPoints.get(0) instanceof Script, "Process contains only 'script' ");
        Assert.assertEquals(((Script) execPoints.get(0)).getScriptBody(), "true");
    }

    public void testParseWithDescription() throws WorkflowParseException {
        Workflow w = parseAndCompare("description.hwl.xml");
        Assert.assertEquals(w.getDescription(), "Simple description");
     }

    public void testParseMultipleOutputsWithExpr() throws WorkflowParseException {
        Workflow w = parseAndCompare("multipleOutputs.hwl.xml");
        Service service = (Service) w.getProcessDefinitions().get(0).getExecutionPoints().get(0);
        Assert.assertEquals(2, service.getOutputs().size());
        Assert.assertEquals("true", service.getOutputs().get(0).getExpression());
    }

    public void testParseWaitWithExpr() throws WorkflowParseException {
        Workflow w = parseAndCompare("waitWithExpr.hwl.xml");
        Wait wait = (Wait) w.getProcessDefinitions().get(0).getExecutionPoints().get(1);
        Assert.assertEquals("true", wait.getExpression());
    }

    private Workflow parseAndCompare(String fileName) throws WorkflowParseException {
        Workflow w = parser.parse(getClass().getResourceAsStream("/" + fileName));
        String s = serialize(w);
        compareXml(s, getClass().getResourceAsStream("/" + fileName));
        return w;
    }

    private void compareXml(String xml, InputStream is) {
        try {
            Diff d = XMLUnit.compareXML(new InputStreamReader(is), xml);
            Assert.assertTrue(d.similar(), d.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String serialize(Workflow w)  {
        try {
            StringWriter writer = new StringWriter();
            serializer.marshall(w, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void testParseOneServiceWithIgnoreErrors() throws WorkflowParseException {
        parseAndCompare("oneServiceWithIgnoreErrors.hwl.xml");
    }

    public void testParseServiceNoOrder() throws WorkflowParseException {
    	parseAndCompare("serviceElementsOrdering.hwl.xml");
    }
}
