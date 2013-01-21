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

import static org.testng.Assert.assertNotNull;

import org.activiti.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.pvm.delegate.SignallableActivityBehavior;
import org.testng.annotations.Test;

@Test
public class PvmApiExampleTest extends AbstractActivitiTest {

    public void testExampleFromUserguide() {
        PvmProcessDefinition processDefinition = createOnlineExample();

        PvmProcessInstance processInstance = processDefinition.createProcessInstance();
        processInstance.start();

        PvmExecution activityInstance = processInstance.findExecution("a");
        assertNotNull(activityInstance);

        activityInstance.signal(null, null);

        activityInstance = processInstance.findExecution("b");
        assertNotNull(activityInstance);

        activityInstance.signal(null, null);

        activityInstance = processInstance.findExecution("c");
        assertNotNull(activityInstance);
    }

    public void testExampleFromUserguideModified() {
        PvmProcessDefinition processDefinition = createOnlineExample();

        PvmProcessInstance processInstance = processDefinition.createProcessInstance();
        processInstance.start();

        PvmActivity act = processInstance.getActivity();
        PvmExecution activityInstance = processInstance.findExecution("a");
        assertNotNull(activityInstance);

        processInstance.signal(null, null);
        act = processInstance.getActivity();
        activityInstance = processInstance.findExecution("b");
        assertNotNull(activityInstance);

        processInstance.signal(null, null);
        act = processInstance.getActivity();
        activityInstance = processInstance.findExecution("c");
        assertNotNull(activityInstance);
    }

    public void testMultipleInstancesPerformance() {
        printMemStats("init");
        PvmProcessDefinition def = createOnlineExample();
        printMemStats("definitionCreated");
        super.testMultipleInstancesPerformance("PvmExample", def);
    }



    private PvmProcessDefinition createOnlineExample() {
        PvmProcessDefinition processDefinition = pvmFactory.newProcessDefinitionBuilder("newProcess1")
        .createActivity("a")
          .initial()
          .behavior(new EmptyActivitiBehavior())
          .transition("b")
        .endActivity()
        .createActivity("b")
          .behavior(new EmptyActivitiBehavior())
          .transition("c")
        .endActivity()
        .createActivity("c")
          .behavior(new EmptyActivitiBehavior())
        .endActivity()
        .buildProcessDefinition();

        return processDefinition;
    }

    public static class EmptyActivitiBehavior extends AbstractBpmnActivityBehavior implements SignallableActivityBehavior{
        @Override
        public void execute(ActivityExecution execution) throws Exception {
        }

        @Override
        public void signal(ActivityExecution execution, String signalEvent,  Object signalData) throws Exception {
            super.leave(execution);
        }
    }

}
