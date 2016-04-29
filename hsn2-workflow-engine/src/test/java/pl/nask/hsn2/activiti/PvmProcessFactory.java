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

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.ProcessDefinitionBuilder;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.bus.api.Bus;

public class PvmProcessFactory {
    Bus bus = new Bus() {
		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}

		@Override
		public boolean isRunning() {
			return true;
		}
    };

    BehaviorFactory behaviourFactory = new BehaviorFactoryImpl();

    public PvmProcessFactory() {}

    public PvmProcessFactory(Bus bus) {
        this.bus = bus;
        this.behaviourFactory = new BehaviorFactoryImpl();
    }

    public void addStart(ProcessDefinitionBuilder builder, String transition) {
        builder
        .createActivity("start")
        .behavior(behaviourFactory.emptyBehaviorInstance())
        .initial()
        .transition(transition)
        .endActivity();
    }

    public void addEnd(ProcessDefinitionBuilder builder) {
        builder
        .createActivity("end")
        .behavior(behaviourFactory.emptyBehaviorInstance())
        .endActivity();
    }

    public void addSignallable(ProcessDefinitionBuilder builder, String actName, String transition) {
        builder
        .createActivity(actName)
        .behavior(signallable(actName, null))
        .transition(transition)
        .endActivity();
    }

    private ActivityBehavior signallable(String actName, Object object) {
        return new AbstractBpmnActivityBehavior() {
          private Logger logger = LoggerFactory.getLogger("TestSignallableBehavior");

          @Override
          public void execute(ActivityExecution execution) throws Exception {}
          @Override
          public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
              logger.debug("activity: {}", execution.getActivity().getId());
              logger.debug("got signal: {} with data: {}", new Object[] {signalName, signalData});
              leave(execution);
          }
        };
    }

    public void addFork(ProcessDefinitionBuilder builder, String actName, String... transitions) {
        builder.createActivity(actName).behavior(behaviourFactory.forkBehaviorInstance());
        for (String trans: transitions) {
            builder.transition(trans);
        }
        builder.endActivity();
    }

    public void addJoin(ProcessDefinitionBuilder builder, String actName, String transition) {
        builder
        .createActivity(actName)
        .behavior(behaviourFactory.joinBehaviorInstance())
        .transition(transition)
        .endActivity();
    }

    public ExtendedProcessDefinitionBuilder newProcessDefinitionBuilder(String processName) {
        return new ExtendedProcessDefinitionBuilder(processName);
    }

    public ProcessDefinitionBuilder newProcessDefinitionBuilder() {
        return new ExtendedProcessDefinitionBuilder();
    }

    public ExtendedProcessDefinitionImpl createSimpleProcess(String processName) {
        ExtendedProcessDefinitionBuilder builder = newProcessDefinitionBuilder(processName);
        addStart(builder, "service");
        addSignallable(builder, "service", "end");
        addEnd(builder);
        return builder.buildProcessDefinition();
    }
}
