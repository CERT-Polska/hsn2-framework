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

package pl.nask.hsn2.activiti;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.testng.Assert;
import org.testng.annotations.Test;

import pl.nask.hsn2.activiti.PvmApiExampleTest.EmptyActivitiBehavior;
import pl.nask.hsn2.activiti.behavior.DecisionActivityBehavior;
import pl.nask.hsn2.expressions.EvaluationException;
import pl.nask.hsn2.expressions.ExpressionResolver;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.suppressor.JobSuppressorHelperImpl;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

@Test
public class ConditionalTest extends AbstractActivitiTest {

    public void basicConditionalWithTrue() {
        ExpressionResolver resolver = new ExpressionResolver() {
            @Override
            public boolean evaluateBoolean(long jobId, long objectDataId,
                    String expression) {
                return "true".equalsIgnoreCase(expression);
            }

            @Override
            public Object evaluateExpression(long jobId, long objectDataId,
                    String expression) throws EvaluationException {
                return "true".equalsIgnoreCase(expression);
            }
        };

        ActivityBehavior decision = new DecisionActivityBehavior("true", resolver);

        PvmProcessDefinition processDefinition = createProcessDefinition(decision);

        PvmProcessInstance pi = processDefinition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(pi);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);

        pi.start();
        Assert.assertEquals(pi.getActivity().getId(), "start");

        pi.signal(null, null);
        Assert.assertEquals(pi.getActivity().getId(), "onTrue");

        pi.signal(null, null);
        Assert.assertEquals(pi.getActivity().getId(), "end");
        assertEnded(pi);
    }

    public void basicConditionalWithFalse() {
        ExpressionResolver resolver = new ExpressionResolver() {
            @Override
            public boolean evaluateBoolean(long jobId, long objectDataId,
                    String expression) {
                return "true".equalsIgnoreCase(expression);
            }

            @Override
            public Object evaluateExpression(long jobId, long objectDataId,
                    String expression) throws EvaluationException {
                return "true".equalsIgnoreCase(expression);
            }
        };

        ActivityBehavior decision = new DecisionActivityBehavior("false", resolver);

        PvmProcessDefinition processDefinition = createProcessDefinition(decision);

        PvmProcessInstance pi = processDefinition.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(pi);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        pi.start();
        Assert.assertEquals(pi.getActivity().getId(), "start");

        pi.signal(null, null);
        Assert.assertEquals(pi.getActivity().getId(), "onFalse");

        pi.signal(null, null);
        Assert.assertEquals(pi.getActivity().getId(), "end");
        assertEnded(pi);
    }

    private PvmProcessDefinition createProcessDefinition(
            ActivityBehavior decision) {
        return pvmFactory.newProcessDefinitionBuilder("conditional")
      .createActivity("start")
        .initial()
        .behavior(new EmptyActivitiBehavior())
        .transition("conditional")
      .endActivity()
      .createActivity("conditional")
        .behavior(decision)
        .transition("onTrue", "onTrue")
        .transition("onFalse", "onFalse")
      .endActivity()
      .createActivity("onTrue")
        .behavior(new EmptyActivitiBehavior())
        .transition("end")
      .endActivity()
      .createActivity("onFalse")
        .behavior(new EmptyActivitiBehavior())
        .transition("end")
      .endActivity()
      .createActivity("end")
        .behavior(pvmFactory.behaviourFactory.emptyBehaviorInstance())
      .buildProcessDefinition();
    }
}
