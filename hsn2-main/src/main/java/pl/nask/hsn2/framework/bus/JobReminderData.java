package pl.nask.hsn2.framework.bus;

public class JobReminderData implements Comparable<Long> {
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
		return JobReminderData.class.getSimpleName() + "{jobId=" + jobId + ",time=" + time + "}";
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
}
