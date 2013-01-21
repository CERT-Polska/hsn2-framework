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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An ID generator which generates unique String identifiers using supplied prefix.
 *
 * his class is thread-safe
 */
public final class IdGen {
    private Map<String, AtomicLong> counters = new HashMap<String, AtomicLong>();

    private IdGen() {}

    public Id nextId(String prefix) {
        return new Id(prefix, incCounter(prefix));
    }

    private AtomicLong getCounter(String prefix) {
    	AtomicLong counter = counters.get(prefix);
        if (counter == null) {
        	counter = new AtomicLong(0);
        	counters.put(prefix, counter);
        }
        return counter;
    }

    private long incCounter(String prefix) {
    	return getCounter(prefix).incrementAndGet();
    }

    public static IdGen getInstance() {
        return new IdGen();
    }

    public static final class Id {
        private final String prefix;
        private final long counter;
        private final String id;

        public Id(String prefix, long counter) {
            this.prefix = prefix;
            this.counter = counter;
            this.id = prefix + "-" + counter;
        }

        public long getCounter() {
            return counter;
        }

        public String getPrefix() {
            return prefix;
        }

        /**
         * @returns ID formatted as 'prefix-counter'
         */
        public String getFormattedId() {
            return id;
        }

        /**
         * @returns ID formatted as 'prefix-counter'
         */
        @Override
        public String toString() {
            return id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Id other = (Id) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }


    }

}