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

import java.io.IOException;

import javax.xml.bind.JAXBException;

import junit.framework.Assert;

import org.custommonkey.xmlunit.XMLUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

@Test(groups="hwlParseExamplesWithSchemaErrors")
public class ParseExamplesWithSchemaErrorsTest {
    WorkflowParser parser;
    WorkflowSerializer serializer;

    @BeforeTest
    public void prepare() throws JAXBException, IOException, SAXException {
        parser = new HWLParser();
        serializer = new WorkflowSerializer();
        XMLUnit.setIgnoreWhitespace(true);
    }

    public void testConditionalNoExpr() throws WorkflowParseException {
        parse("conditional-no-expr.hwl.xml", "cvc-complex-type.4: Attribute 'expr' must appear on element 'conditional'.");
    }

    public void testOneServiceNoName() throws WorkflowParseException {
        parse("oneService-no-name.hwl.xml", "cvc-complex-type.4: Attribute 'name' must appear on element 'service'.");
    }

    public void testOutputNoName() throws WorkflowParseException {
        parse("output-no-name.hwl.xml", "cvc-complex-type.4: Attribute 'process' must appear on element 'output'.");
    }

    public void testProcessNoId() throws WorkflowParseException {
        parse("process-no-id.hwl.xml", "cvc-complex-type.4: Attribute 'id' must appear on element 'process'.");
    }

    public void testServiceParametersNoName() throws WorkflowParseException {
        parse("serviceParameters-no-name.hwl.xml", "cvc-complex-type.4: Attribute 'name' must appear on element 'parameter'.");
    }

    private void parse(String fileName, String expectedError) throws WorkflowParseException {
        try {
            parser.parse(getClass().getResourceAsStream("/incorrect/schema/" + fileName));
        } catch (WorkflowSyntaxException e) {
            // check, if it's an expected error
            Assert.assertEquals(expectedError, e.getMessage());
        }
    }
}
