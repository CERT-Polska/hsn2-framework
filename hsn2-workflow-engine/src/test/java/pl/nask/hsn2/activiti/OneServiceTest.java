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

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.testng.Assert;
import org.testng.annotations.Test;

import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.framework.workflow.hwl.ProcessDefinition;
import pl.nask.hsn2.framework.workflow.hwl.Service;
import pl.nask.hsn2.suppressor.JobSuppressorHelperImpl;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

@Test
public class OneServiceTest extends AbstractActivitiTest {
    public void hwlService() {
        PvmProcessDefinition pvmDef = createHwlProcessDef();
        PvmProcessInstance pi = pvmDef.createProcessInstance();
        ExecutionWrapper wrapper = new ExecutionWrapper(pi);
        JobSuppressorHelperImpl jobSuppressorHelper = new JobSuppressorHelperImpl(1, 100, new SingleThreadTasksSuppressor(true));
        wrapper.initProcessState(1, jobSuppressorHelper);
        pi.start();
        assertProcessState(pi, "start");
        pi.signal("resume", null);
        // start is automatic, act-1 sends a message, act-2 should be waiting for a signal...
        assertProcessState(pi, "act-1");
        signalActiveExecution(pi);
        // should be in the 'end' state
        assertProcessState(pi, "act-2");
        assertEnded(pi);
    }

    public void testMultipleInstancesPerformance() {
        PvmProcessDefinition def = createHwlProcessDef();
        super.testMultipleInstancesPerformance("OneService", def);
    }

    private PvmProcessDefinition createHwlProcessDef() {
        Service s = new Service("service");
        ProcessDefinition pd = new ProcessDefinition("main");
        pd.addExecutionPoint(s);
        ActivitiWorkflowBuilder awb = new ActivitiWorkflowBuilder(pvmFactory.behaviourFactory);
        awb.buildWorkflow("main", Arrays.asList(pd));

        List<PvmProcessDefinition> pvmDefs = awb.getRegistry().getDefinitions();
        Assert.assertEquals(pvmDefs.size(), 1);
        return pvmDefs.get(0);
    }
}
