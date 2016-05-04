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

import javax.xml.bind.JAXBException;

import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import pl.nask.hsn2.framework.workflow.hwl.Workflow;

@Test(dependsOnGroups={"hwlParseExample", "hwlParseExamplesWithSchemaErrors"})
public class ValidateRulesTest {

    WorkflowParser parser;
    WorkflowSerializer serializer;
    WorkflowValidator validator = new WorkflowValidator(new String[] {
            "crawler", "crawler2", "feeder", "analyzer1", "analyzer2", "exporter"} );
    @BeforeTest
    public void prepare() throws JAXBException, IOException, SAXException {
        parser = new HWLParser();
        serializer = new WorkflowSerializer();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testValidateConditional() throws WorkflowValidationException {
        parseValid("conditional.hwl.xml");
    }

    public void testValidateFullExample() throws WorkflowValidationException {
        parseValid("fullExample.hwl.xml");
    }

    public void testValidateOneService() throws WorkflowValidationException {
        parseValid("oneService.hwl.xml");
    }

    public void testValidateParallel() throws WorkflowValidationException {
        parseValid("parallel.hwl.xml");
    }

    public void testValidateServiceParameters() throws WorkflowValidationException {
        parseValid("serviceParameters.hwl.xml");
    }

    public void testValidateTwoProcesses() throws WorkflowValidationException {
        parseValid("twoProcesses.hwl.xml");
    }

    @Test(expectedExceptions=WorkflowValidationException.class)
    public void testValidateNoMainProcess() throws WorkflowValidationException {
        parseInvalid("no-main-process.hwl.xml");
    }

    @Test(expectedExceptions=WorkflowValidationException.class)
    public void testValidateOutputNoName() throws WorkflowValidationException {
        parseInvalid("output-no-name.hwl.xml");
    }

    @Test(expectedExceptions=WorkflowValidationException.class)
    public void testValidateIllegalServiceName() throws WorkflowValidationException {
        parseInvalid("output-illegal-service-name.hwl.xml");
    }

    private void parseValid(String fileName) throws WorkflowValidationException {
        parseAndValidate("/" + fileName);
    }

    private void parseInvalid(String fileName) throws WorkflowValidationException {
        parseAndValidate("/incorrect/rules/" + fileName);
    }

    private void parseAndValidate(String path) throws WorkflowValidationException {
        try {
            Workflow w = parser.parse(getClass().getResourceAsStream(path));
            validator.validateAll(w);
        } catch (WorkflowValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
