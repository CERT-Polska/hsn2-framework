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

import javax.xml.bind.annotation.XmlAttribute;

public final class Output {

    @XmlAttribute(required=true, name="process")
    private String processName;

    @XmlAttribute(required=false, name="expr")
    private String expression;

    public Output() {
    }

    public Output(String processName) {
        this.processName = processName;
    }

    public Output(String processName, String expression) {
        this.processName = processName;
        this.expression = expression;
    }

    public String getProcessName() {
        return processName;
    }

    public String getExpression() {
        return expression;
    }
    
    @Override
    public String toString() {   
    	return "Output(process=" + processName + ", expr=" + expression + ")";
    }
}
