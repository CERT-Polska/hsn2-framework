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

package pl.nask.hsn2.expressions;

import org.apache.commons.ognl.Ognl;
import org.apache.commons.ognl.OgnlContext;
import org.apache.commons.ognl.OgnlException;

import pl.nask.hsn2.os.OgnlRootObject;


/**
 *
 *
 */
public class OgnlExpression {
    private Object expression;

    public OgnlExpression( String expressionString )
        throws OgnlException
    {
        super();
        expression = Ognl.parseExpression( expressionString );
    }

    public Object getExpression()
    {
        return expression;
    }

    public Object getValue( OgnlContext context, Object rootObject )
        throws OgnlException
    {
        return Ognl.getValue( getExpression(), context, rootObject );
    }

    public Object evaluate (OgnlContext ctx, OgnlRootObject rootObject) throws OgnlException {
        return Ognl.getValue(getExpression(), ctx, rootObject);
    }

    public void setValue( OgnlContext context, Object rootObject, Object value )
        throws OgnlException
    {
        Ognl.setValue(getExpression(), context, rootObject, value);
    }

}
