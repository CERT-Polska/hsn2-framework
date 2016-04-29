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

package pl.nask.hsn2.expressions;

import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.os.CachingObjectStoreImpl;
import pl.nask.hsn2.os.OSObject;

public class OgnlExpressionEvaluation {
    private static final Logger LOG = LoggerFactory.getLogger(OgnlExpressionEvaluation.class);

    private final long jobId;
    private final long objectDataId;
    private final String expression;
    private final ObjectStoreConnector connector;

    public OgnlExpressionEvaluation(ObjectStoreConnector connector, long jobId, long objectDataId, String expression) {
        this.connector = connector;
        this.jobId = jobId;
        this.objectDataId = objectDataId;
        this.expression = expression;
    }

    public Object eval() throws EvaluationException {
        if (expression == null)
            return null;
        try {
            LOG.debug("About to evaluate expression. Object Id={}, expression : {}", objectDataId, expression);
            OgnlContext ctx = new OgnlContext();
            CachingObjectStoreImpl cachingObjectStore = new CachingObjectStoreImpl(connector, jobId);
            OSObject root = new OSObject(cachingObjectStore , objectDataId);
            ctx.put("current", root);
            OgnlExpression expr = new OgnlExpression(expression);

            Object value = expr.evaluate(ctx, root);
            LOG.debug("Expression evaluated to value = {} ", value);

            return value;
        } catch (OgnlException e) {
            throw new EvaluationException("Error evaluating expression", e);
        }
    }



}
