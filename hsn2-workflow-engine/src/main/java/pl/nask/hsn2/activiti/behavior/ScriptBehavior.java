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
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.expressions.ExpressionResolver;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

public final class ScriptBehavior extends AbstractBpmnActivityBehavior implements HSNBehavior {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptBehavior.class);

    private final String scriptBody;
    private final ExpressionResolver expressionResolver;

    public ScriptBehavior(String scriptBody, ExpressionResolver expressionResolver) {
        this.scriptBody = scriptBody;
        this.expressionResolver = expressionResolver;
    }

    @Override
    public String getStepName() {
        return "script";
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
        ExecutionWrapper wrapper = new ExecutionWrapper(execution);
        long jobId = wrapper.getJobId();
        long objectId = wrapper.getSubprocessParameters().getObjectDataId();
        LOGGER.debug("Executing script (jobId={}, objectId={})", jobId, objectId);
        Object result = expressionResolver.evaluateExpression(jobId, objectId, scriptBody);
        LOGGER.debug("Script executed (jobId={}, objectId={}) with result = {}", new Object[]{jobId, objectId, result});

        leave(execution);
    }

}
