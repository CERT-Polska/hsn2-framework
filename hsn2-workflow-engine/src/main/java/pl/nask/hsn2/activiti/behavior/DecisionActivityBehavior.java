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

import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.expressions.EvaluationException;
import pl.nask.hsn2.expressions.ExpressionResolver;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

public class DecisionActivityBehavior implements ActivityBehavior, HSNBehavior {
    private final static Logger LOGGER = LoggerFactory.getLogger(DecisionActivityBehavior.class);
    private final String expression;
    private final ExpressionResolver resolver;

    public DecisionActivityBehavior(String expression, ExpressionResolver resolver) {
       this.expression = expression;
       this.resolver = resolver;
    }

    @Override
    public void execute(ActivityExecution execution){
        String transitionId;
        ExecutionWrapper wrapper = new ExecutionWrapper(execution);
        try{
	        if (resolver.evaluateBoolean(wrapper.getJobId(), wrapper.getSubprocessParameters().getObjectDataId(), expression)) {
	            LOGGER.debug("condition {} is true", expression);
	            transitionId = "onTrue";
	        } else {
	            LOGGER.debug("condition {} is false", expression);
	            transitionId = "onFalse";
	        }
        } catch (EvaluationException e) {
        	LOGGER.error(e.getMessage(),e);
        	LOGGER.debug("condition {} has an error, returning false.", expression);
            transitionId = "onFalse";
		}

        PvmTransition transition = execution.getActivity().findOutgoingTransition(transitionId);
        execution.take(transition);
    }

    @Override
    public String getStepName() {
        return "decision";
    }

}
