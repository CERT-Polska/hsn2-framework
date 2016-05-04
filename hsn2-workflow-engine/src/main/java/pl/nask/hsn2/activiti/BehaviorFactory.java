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

import java.util.List;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;

import pl.nask.hsn2.bus.operations.TaskErrorReasonType;
import pl.nask.hsn2.framework.workflow.engine.ProcessDefinitionRegistry;
import pl.nask.hsn2.framework.workflow.hwl.Output;
import pl.nask.hsn2.framework.workflow.hwl.ServiceParam;

public interface BehaviorFactory {

    ActivityBehavior decisionBehaviorInstance(String condition);

    ActivityBehavior emptyBehaviorInstance();

    ActivityBehavior serviceBehaviorInstance(String serviceName, String id, List<ServiceParam> parameters, List<Output> outputs, ProcessDefinitionRegistry<PvmProcessDefinition> registry,List<TaskErrorReasonType>ignoredErrors);

    ActivityBehavior joinBehaviorInstance();

    ActivityBehavior forkBehaviorInstance();

    ActivityBehavior waitBehavior(String expression);

    ActivityBehavior startBehavior();

    ActivityBehavior scriptBehavior(String scriptBody);
}
