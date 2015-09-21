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

package pl.nask.hsn2.activiti.behavior;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.expressions.ExpressionResolver;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

public final class WaitBehavior extends AbstractBpmnActivityBehavior implements HSNBehavior {

	private static final Logger LOGGER = LoggerFactory.getLogger(WaitBehavior.class);

	private final String condition;
    private final ExpressionResolver expressionResolver;

    public WaitBehavior(String condition, ExpressionResolver expressionResolver) {
        this.condition = condition;
        this.expressionResolver = expressionResolver;
    }

    @Override
    public void execute(ActivityExecution execution) {
        // if there are no pending subprocesses, leave!
        tryToLeave(execution);
    }

    private void tryToLeave(ActivityExecution execution) {
        if (noPendingProcesses(execution))
            leave(execution);
    }

    // TODO: use expressionResolver to calculate the
    private boolean noPendingProcesses(ActivityExecution execution) {
        ExecutionWrapper wrapper = new ExecutionWrapper(execution);
        int activeSubprocesses = wrapper.countActiveSubprocesses();
        return (activeSubprocesses == 0);
    }


    /**
     * handles "notify" signal sent from ending subprocesses.
     */
    @Override
    public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
        if ("notify".equalsIgnoreCase(signalName)) {
            tryToLeave(execution);
        } else {
            LOGGER.debug("Ignoring unsupported signal: {} with signalData: {}", signalName, signalData);
        }
    }

    @Override
    public String toString() {
        return "WaitBehavior";
    }

    @Override
    public String getStepName() {
        return "wait";
    }
}
