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

package pl.nask.hsn2.workflow.engine;

import java.io.Serializable;

import pl.nask.hsn2.framework.workflow.engine.WorkflowDescriptor;

public final class SubprocessParameters implements Serializable {

	private static final long serialVersionUID = -8950099099103372295L;

	private final WorkflowDescriptor wdf;
    private final long objectDataId;

    public SubprocessParameters(WorkflowDescriptor wdf, long objectDataId) {
        this.wdf = wdf;
        this.objectDataId = objectDataId;
    }

    public WorkflowDescriptor getWdf() {
        return wdf;
    }

    public long getObjectDataId() {
        return objectDataId;
    }

	@Override
	public String toString() {
		return "SubprocessParameters [wdf=" + wdf + ", objectDataId="
				+ objectDataId + "]";
	}
    
    
}
