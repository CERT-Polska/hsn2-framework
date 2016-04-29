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

package pl.nask.hsn2.framework.bus;

public final class JobReminderData implements Comparable<Long> {

	private final long jobId;
	private final long time;

	public JobReminderData(long jobId, long time) {
		this.jobId = jobId;
		this.time = time;
	}

	public long getJobId() {
		return jobId;
	}

	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		return String.format("%s {jobId=%s, time=%s}",
				JobReminderData.class.getSimpleName(),
				jobId, time);
	}

	@Override
	public int compareTo(Long o) {
		if (o < time) {
			return 1;
		} else if (o > time) {
			return -1;
		} else {
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || (obj.getClass() != this.getClass())) {
			return false;
		}
		JobReminderData jrdObj = (JobReminderData) obj;
		return (jrdObj.jobId == jobId) && (jrdObj.time == time);
	}
	
	@Override
	public int hashCode() {
		return (int) ((31 * jobId + time)^time)>>>32;
	}
}
