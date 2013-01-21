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

package pl.nask.hsn2.activiti;

import java.util.List;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;

import pl.nask.hsn2.activiti.behavior.DecisionActivityBehavior;
import pl.nask.hsn2.activiti.behavior.EmptyBehavior;
import pl.nask.hsn2.activiti.behavior.ScriptBehavior;
import pl.nask.hsn2.activiti.behavior.ServiceBehavior;
import pl.nask.hsn2.activiti.behavior.StartBehavior;
import pl.nask.hsn2.activiti.behavior.TransientParallelGatewayBehavior;
import pl.nask.hsn2.activiti.behavior.WaitBehavior;
import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.expressions.ExpressionResolver;
import pl.nask.hsn2.expressions.OgnlExpressionResolver;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ServiceParam;

public class BehaviorFactoryImpl implements BehaviorFactory {
    private final ActivityBehavior emptyBehavior = new EmptyBehavior();

    private ExpressionResolver expressionResolver;

    public BehaviorFactoryImpl() {
        this.expressionResolver = new OgnlExpressionResolver(((FrameworkBus)BusManager.getBus()).getObjectStoreConnector());
    }

    @Override
    public ActivityBehavior decisionBehaviorInstance(String condition) {
        return
            new DecisionActivityBehavior(condition, expressionResolver);
    }

    @Override
    public ActivityBehavior emptyBehaviorInstance() {
        return emptyBehavior;
    }

    @Override
    public ActivityBehavior forkBehaviorInstance() {
        return new TransientParallelGatewayBehavior();
    }

    @Override
    public ActivityBehavior joinBehaviorInstance() {
        return new TransientParallelGatewayBehavior();
    }

    @Override
    public ActivityBehavior waitBehavior(String expression) {
        return new WaitBehavior(expression, expressionResolver);
    }

    @Override
    public ActivityBehavior serviceBehaviorInstance(String serviceName, String id, List<ServiceParam> parameters, List<Output> outputs, ProcessDefinitionRegistry<PvmProcessDefinition> definitionRegistry,List<TaskErrorReasonType> ignoredErrors) {
    	return new ServiceBehavior(serviceName, id,
				ServiceParam.getAsProperties(parameters), outputs,
				expressionResolver, definitionRegistry,ignoredErrors);
    }

    @Override
    public ActivityBehavior startBehavior() {
        return new StartBehavior();
    }

    @Override
    public ActivityBehavior scriptBehavior(String scriptBody) {
        return new ScriptBehavior(scriptBody, expressionResolver);
    }


}
