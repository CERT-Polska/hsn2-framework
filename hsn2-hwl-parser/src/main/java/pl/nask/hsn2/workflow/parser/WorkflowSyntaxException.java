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

public final class WorkflowSyntaxException extends WorkflowParseException {

	private static final long serialVersionUID = 7125330107045174466L;

	private int lineNumber = -1;
    private int columnNumber = -1;

    public WorkflowSyntaxException(Throwable cause) {
        super(cause);
    }

    public WorkflowSyntaxException(Throwable cause, int lineNo, int colNo, String msg) {
        super(msg, cause);
        this.lineNumber = lineNo;
        this.columnNumber = colNo;
    }

    @Override
    public String toString() {
        return String.format("line number: %s, column number: %s, message: %s", getLineNumber(), getColumnNumber(), getMessage());
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
