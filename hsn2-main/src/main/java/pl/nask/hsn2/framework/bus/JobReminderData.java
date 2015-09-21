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
		if (obj instanceof JobReminderData) {
			JobReminderData jrdObj = (JobReminderData) obj;
			return (jrdObj.jobId == jobId) && (jrdObj.time == time);
		}
		return false;
	}
}
