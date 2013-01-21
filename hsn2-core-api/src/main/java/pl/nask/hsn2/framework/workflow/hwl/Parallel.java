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

package pl.nask.hsn2.framework.workflow.hwl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import pl.nask.hsn2.framework.workflow.builder.WorkflowBuilder;

@XmlRootElement
public class Parallel implements ExecutionPoint {

    @XmlElement(name="thread")
    private List<ExecutionFlow> threads = null;

    public final void addThread(ExecutionFlow thread) {
        if (threads == null)
            threads = new ArrayList<ExecutionFlow>();
        threads.add(thread);
    }

    public final void addThread(ExecutionPoint p) {
        ExecutionFlow f = new ExecutionFlow();
        f.addExecutionPoint(p);
        addThread(f);
    }

    @Override
    public final List<Output> getOutputs() {
        List<Output> outputs = new ArrayList<Output>();
        for (ExecutionFlow f: threads) {
            outputs.addAll(f.getOutputs());
        }
        return outputs;

    }

    @Override
    public final List<? extends Service> getAllServices() {
        List<Service> services = new ArrayList<Service>();
        for (ExecutionFlow f: threads) {
            services.addAll(f.getAllServices());
        }
        return services;

    }

    public final List<ExecutionFlow> getThreads() {
        return threads;
    }

    @Override
    public final void transformToWorkflow(WorkflowBuilder builder) {
        builder.addParallel(threads);

    }
}
