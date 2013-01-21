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

package pl.nask.hsn2.framework.workflow.repository;

/**
 * The class <code>WorkflowRepoException</code> is a form of
 * <code>Throwable</code> that indicates conditions that a reasonable
 * application might want to catch if there is any problem with
 * access to the repository of workflows.
*/
public class WorkflowRepoException extends Exception {

	private static final long serialVersionUID = -4293086751415715820L;

	public WorkflowRepoException(String msg, Throwable e) {
        super(msg, e);
    }

	public WorkflowRepoException(String msg) {
		super(msg);
	}
}
