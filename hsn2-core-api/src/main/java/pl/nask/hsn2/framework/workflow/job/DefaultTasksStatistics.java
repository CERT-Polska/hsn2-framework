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

package pl.nask.hsn2.framework.workflow.job;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is simple implementation of <code>TasksStatistics</code>.
 * 
 * This implementation is thread-safe.
 * 
 *
 */
public class DefaultTasksStatistics implements TasksStatistics {
	
	/**
	 * Internal map of running tasks.
	 */
    private ConcurrentMap<String, AtomicInteger> started = new ConcurrentHashMap<String, AtomicInteger>();
    
    /**
     * Internal map of finished tasks.
     */
    private ConcurrentMap<String, AtomicInteger> finished = new ConcurrentHashMap<String, AtomicInteger>();
    
    private AtomicInteger subprocessesStarted = new AtomicInteger();
    
    @Override
    public final Map<String, Integer> getStarted() {
        return getValues(started);
    }

    @Override
    public final Map<String, Integer> getFinished() {
        return getValues(finished);
    }

    /**
     * Update stats by new started task or subprocess.
     * 
     * @param taskName Started task name.
     */
    public final void taskStarted(String taskName) {
        inc(taskName, started);
    }

    /**
     * Update stats by finished task or subprocess.
     * 
     * @param taskName Finished task name.
     */
    public final void taskCompleted(String taskName) {
        inc(taskName, finished);
    }

    private void inc(String taskName, ConcurrentMap<String, AtomicInteger> src) {
        AtomicInteger counter = src.putIfAbsent(taskName, new AtomicInteger(1));
        if (counter != null)
            counter.incrementAndGet();
    }

    private Map<String, Integer> getValues(ConcurrentMap<String, AtomicInteger> src) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Map.Entry<String, AtomicInteger> entry: src.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get());
        }

        return map;
    }

    
	public void subprocessStarted() {
		subprocessesStarted.incrementAndGet();
	}
	
	@Override
	public int getSubprocessesStarted() {
		return subprocessesStarted.get();
	}
}
