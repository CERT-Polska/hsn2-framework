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

package pl.nask.hsn2.activiti.behavior;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

public final class StartBehavior extends AbstractBpmnActivityBehavior implements ActivityBehavior, HSNBehavior {

	private static final Logger LOG = LoggerFactory.getLogger(StartBehavior.class);

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        LOG.debug("Scheduling for later execution: {}. Use 'resume' signal to proceed with processing", execution);
        ExecutionWrapper wrapper = new ExecutionWrapper(execution);
        wrapper.getJobStats().subprocessStarted();
        wrapper.schedule();
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
        LOG.debug("Resuming execution of {}", execution);
        if ("resume".equalsIgnoreCase(signalName)) {
        	leave(execution);	
        }
    }

    @Override
    public String getStepName() {
        return "start";
    }

}
