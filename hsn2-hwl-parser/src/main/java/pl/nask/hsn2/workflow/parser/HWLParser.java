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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import pl.nask.hsn2.framework.workflow.hwl.Conditional;
import pl.nask.hsn2.framework.workflow.hwl.ExecutionFlow;
import pl.nask.hsn2.framework.workflow.hwl.Parallel;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.framework.workflow.hwl.Wait;
import pl.nask.hsn2.framework.workflow.hwl.Workflow;

public class HWLParser implements WorkflowParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HWLParser.class);

    private JAXBContext ctx;
    private Schema schema;
    private String schemaSystemId;
    private Node schemaNode;

    public HWLParser() {
        try {
            createContext();
            createSchema();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (SAXException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Workflow parse(File file) throws WorkflowParseException {
        try {
            return (Workflow) createUnmarshaller().unmarshal(file);
        } catch (ValidationException e) {
            throw makeWorkflowSyntaxException(e);
        } catch (UnmarshalException e) {
            throw makeWorkflowSyntaxException(e);
        } catch (JAXBException e) {
            throw new WorkflowParseException(e);
        }
    }

    @Override
    public Workflow parse(InputStream is) throws WorkflowParseException {
        try{
            return (Workflow) createUnmarshaller().unmarshal(is);
        } catch (ValidationException e) {
            throw makeWorkflowSyntaxException(e);
        } catch (UnmarshalException e) {
            throw makeWorkflowSyntaxException(e);
        } catch (JAXBException e) {
            throw new WorkflowParseException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private WorkflowSyntaxException makeWorkflowSyntaxException(JAXBException e) {
        int lineNo = -1;
        int colNo = -1;
        String msg = e.getMessage();
        if (e.getLinkedException() instanceof SAXParseException) {
            SAXParseException linked = (SAXParseException) e.getLinkedException();
            lineNo = linked.getLineNumber();
            colNo = linked.getColumnNumber();
            msg = linked.getMessage();
        }

        return new WorkflowSyntaxException(e, lineNo, colNo, msg);
    }

    private Unmarshaller createUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return unmarshaller;
    }

    private void createContext() throws JAXBException {
        ctx = JAXBContext.newInstance(
                Workflow.class,
                Process.class,
                Service.class,
                Conditional.class,
                Parallel.class,
                ExecutionFlow.class,
                Wait.class);
    }

    private void createSchema() throws IOException, SAXException {
        final DOMResult result = new DOMResult();

        SchemaOutputResolver outputResolver = new HwlSchemaOutputResolver(result);

        ctx.generateSchema(outputResolver);
        this.schemaNode = result.getNode();
        this.schemaSystemId = result.getSystemId();

        Source source = new DOMSource(schemaNode, schemaSystemId);

        this.schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(source);
    }

    public void printSchema() {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult res = new StreamResult(System.out);
            t.transform(new DOMSource(schemaNode, schemaSystemId), res);
        } catch (TransformerConfigurationException e) {
            LOGGER.error("Transformer configuration exception", e);
        } catch (TransformerFactoryConfigurationError e) {
            LOGGER.error("Transformer factory configuration exception", e);
        } catch (TransformerException e) {
            LOGGER.error("Transformer exception", e);
        }
    }

    public static class HwlSchemaOutputResolver extends SchemaOutputResolver {

        private final Result result;

        public HwlSchemaOutputResolver(Result result) {
            this.result = result;
        }

        @Override
        public Result createOutput(String namespaceUri, String suggestedFileName)
                throws IOException {
            result.setSystemId(suggestedFileName);
            return result;
        }
    }
}
