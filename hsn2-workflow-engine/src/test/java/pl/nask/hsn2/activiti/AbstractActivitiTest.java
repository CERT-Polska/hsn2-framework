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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import mockit.Mocked;

import org.activiti.engine.impl.pvm.PvmExecution;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.PvmProcessInstance;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.runtime.ExecutionImpl;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;

import pl.nask.hsn2.bus.api.BusManager;
import pl.nask.hsn2.bus.connector.objectstore.ObjectStoreConnector;
import pl.nask.hsn2.bus.connector.process.ProcessConnector;
import pl.nask.hsn2.bus.connector.process.StubProcessConnector;
import pl.nask.hsn2.bus.operations.JobStatus;
import pl.nask.hsn2.framework.bus.FrameworkBus;
import pl.nask.hsn2.framework.suppressor.SingleThreadTasksSuppressor;
import pl.nask.hsn2.suppressor.JobSuppressorHelperImpl;
import pl.nask.hsn2.workflow.engine.ExecutionWrapper;

public class AbstractActivitiTest {

    private long freeMem;
    private long totalMem;
    private long maxMem;
    private long usedMem;

    protected PvmProcessFactory pvmFactory = null;
    
    @Mocked
    private ObjectStoreConnector connector ; 
    
    @BeforeMethod
    void prepare() {
    	if (pvmFactory == null) {
    		BusManager.setBus(new FrameworkBus() {
    			public ObjectStoreConnector getObjectStoreConnector() { return connector; }
    			public ProcessConnector getProcessConnector() {
    				return new StubProcessConnector();
    			}
    			@Override public void start() { }
    			@Override public void stop() { }
    			@Override public boolean isRunning() { return true; }
    			@Override public void jobStarted(long jobId) { }
    			@Override public void jobFinished(long jobId, JobStatus status) { }
				@Override public void jobFinishedReminder(long jobId, JobStatus status, int offendingTask) {}
    		});
    		pvmFactory = new PvmProcessFactory();
    	}
    }

    public static void assertProcessState( PvmProcessInstance instance, String activityName) {
        try {
            AssertJUnit.assertEquals(activityName, instance.getActivity().getId());
        } catch (AssertionError e) {
            stats(instance);
            throw e;
        }
    }

    public static void assertEnded(PvmProcessInstance instance) {
        try {
            AssertJUnit.assertTrue(instance.isEnded());
        } catch (AssertionError e) {
            stats(instance);
            throw e;
        }
    }

    public static void assertNotEnded(PvmProcessInstance instance) {
        try {
            AssertJUnit.assertFalse(instance.isEnded());
        } catch (AssertionError e) {
            stats(instance);
            throw e;
        }
    }

    public static void assertActive(PvmProcessInstance instance, int numberOfActive) {
        List<String> active = instance.findActiveActivityIds();
        try {
            AssertJUnit.assertEquals(numberOfActive, active.size());
        } catch (AssertionError e) {
            stats(instance);
            throw e;
        }
    }

	protected void printMemStats(String msg) {
		long free = Runtime.getRuntime().freeMemory();
		long total = Runtime.getRuntime().totalMemory();
		long max = Runtime.getRuntime().maxMemory();
		long used = total - free;

		System.out.printf("%s: Mem used %,d (%,d), free %,d (%,d), total %,d (%,d), max %,d (%,d)\n", msg, used, (used - usedMem), free, (free - freeMem),
				total, (total - totalMem), max, (max - maxMem));

		this.freeMem = free;
		this.totalMem = total;
		this.maxMem = max;
		this.usedMem = used;
	}

	public void testMultipleInstancesPerformance(String message, PvmProcessDefinition def) {
		testMultipleInstancesPerformance(message, def, null);
	}

    public void testMultipleInstancesPerformance(String message, PvmProcessDefinition def, Map<String, Object> variables) {
        System.out.println(message + " : testMultipleInstancesPerformance");

        List<PvmProcessInstance> instances = new ArrayList<PvmProcessInstance>(5000);

        for (int i=0; i<5000; i++) {
            PvmProcessInstance instance = def.createProcessInstance();
            ExecutionWrapper wrapper = new ExecutionWrapper(instance);
            wrapper.initProcessState(i, new JobSuppressorHelperImpl(1L, 100, new SingleThreadTasksSuppressor(true)));
            setVariables(instance, variables);
            instances.add(instance);
        }
        printMemStats("instancesCreated");

        for (PvmProcessInstance pi: instances) {
            pi.start();
            pi.signal("resume", null);
        }
        printMemStats("instancesStarted");


        List<PvmProcessInstance> activeProcesses = new ArrayList<PvmProcessInstance>(instances);
        List<PvmProcessInstance> processed = new ArrayList<PvmProcessInstance>();
        int i = 0;
        do {
            i++;
            for (PvmProcessInstance pi: activeProcesses) {
                if (!pi.isEnded()) {
                    signalActiveExecution(pi);
                    processed.add(pi);
                }
            }
            activeProcesses = new ArrayList<PvmProcessInstance>(processed);
            processed = new ArrayList<PvmProcessInstance>();
            printMemStats("signal " + i);
        } while (activeProcesses.size() > 0);

        for (PvmProcessInstance pi: instances) {
            try {
                AssertJUnit.assertTrue(pi.isEnded());
            } catch (AssertionError e) {
                stats(pi);
                throw e;
            }
            pi.deleteCascade("end");
        }
        printMemStats("ending");


    }

    private void setVariables(PvmProcessInstance instance, Map<String, Object> variables) {
        if (variables != null) {
            for (Map.Entry<String, Object> e: variables.entrySet()) {
                instance.setVariable(e.getKey(), e.getValue());
            }
        }
    }

    protected void signalAllActiveExecutions(PvmProcessInstance instance) {
        List<String> active = instance.findActiveActivityIds();
        for (int i = active.size(); i > 0; i--) {
            instance.findExecution(active.get(i - 1)).signal("completeTask", null);
        }
    }

    protected void signalActiveExecution(PvmProcessInstance pi) {
        getActiveExecution(pi).signal("completeTask", null);
    }

    protected void signalNamedExecution(PvmProcessInstance pi, String activityId, String signalName) {
        PvmExecution exec = pi.findExecution(activityId);
        if (exec != null) {
            exec.signal(signalName, null);
        } else {
            stats(pi);
            Assert.fail(String.format("Skipping signal: %s, there is no activity %s in %s\n", signalName, activityId, pi));
        }
    }

    protected void signalActiveExecution(PvmProcessInstance pi, String signalName) {
        getActiveExecution(pi).signal(signalName, null);
    }

    protected void signalActiveExecution(PvmProcessInstance pi, String signalName, Object signalData) {
        getActiveExecution(pi).signal(signalName, signalData);
    }

    protected static void stats(String prefix, PvmExecution exec) {
        stats(prefix, (ExecutionImpl) exec);
    }

    protected static void stats(PvmExecution exec) {
        stats("", (ExecutionImpl) exec);
    }

    private static void stats(String prefix, ExecutionImpl pi) {
        if (pi == null)
            return;
        List<String> ids = pi.findActiveActivityIds();
        System.out.printf("\n%sProcessInstance: %s with activeActivityIds: %s\n", prefix, pi, ids);
        List<ExecutionImpl> executions = pi.getExecutions();
        System.out.printf("%sExecutions: %s\n", prefix, executions);
        for (PvmExecution exec: executions) {
            stats("  " + prefix, exec);
        }
        System.out.printf("%sVariables: %s\n", prefix, pi.getVariables());
        System.out.printf("%sSubProcess: \n", prefix);
        stats("  " + prefix, pi.getSubProcessInstance());
        System.out.printf("%sSubexecutions: \n", prefix);
        @SuppressWarnings("unchecked")
		List <PvmExecution> children = (List<PvmExecution>) pi.getVariable("child_executions");
        System.out.printf("%sManagedProcesses: %s\n", prefix, children);
        if (children != null) {
            for (PvmExecution exec: children)
                stats("  " + prefix, exec);
        }
        System.out.printf("%sParent Process: %s\n", prefix, pi.getVariable("parent_execution"));
        System.out.printf("%sProcessDefinition: %s\n", prefix, pi.getProcessDefinition());
        List<ActivityImpl> activities = pi.getProcessDefinition().getActivities();
        System.out.printf("%sProcessDefinition.activities: [", prefix);
        for (ActivityImpl act: activities) {
            System.out.printf("%s (%s), " , act.getId(), act.getActivityBehavior());
        }
        System.out.println("]");
        System.out.printf("%sProcessState.ended: %s\n", prefix, pi.isEnded());
    }

    protected PvmExecution getActiveExecution(PvmProcessInstance pi) {
        List<String> ids = pi.findActiveActivityIds();
        System.out.println("Active executions: " + ids);
        AssertJUnit.assertTrue(ids.size() > 0);
        return pi.findExecution(ids.get(ids.size() - 1));
    }
}
