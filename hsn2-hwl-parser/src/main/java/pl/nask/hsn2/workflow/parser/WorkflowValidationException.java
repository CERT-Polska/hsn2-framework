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

import pl.nask.hsn2.framework.workflow.hwl.Workflow;

public class WorkflowValidationException extends Exception {

    private final String workflowName;
    private final String subMessage;

    public WorkflowValidationException(Workflow workflow, String subMessage) {
        this.subMessage = subMessage;
        this.workflowName = workflow.getName();
    }

    @Override
    public String getMessage() {
        return String.format("Error validating workflow (name=%s), error message is: %s", workflowName, subMessage);
    }

}
