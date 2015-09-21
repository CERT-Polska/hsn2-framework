package pl.nask.hsn2.framework.suppressor;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SingleThreadTasksSuppressor extends Thread {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadTasksSuppressor.class);
	
	private BlockingDeque<JobSuppressorHelper> jobSuppressors = new LinkedBlockingDeque<>();

	private final boolean isEnabled;
	// private final int bufferSize;

	public SingleThreadTasksSuppressor(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	@Override
	public void run() {
		if (isEnabled) {
			LOGGER.info("Tasks suppressor enabled.");
			while (true) {
				try {
					LOGGER.debug("Waiting for action request.");

					// Block until there is some action to do.
					JobSuppressorHelper js = jobSuppressors.take();

					// Do the action.
					js.tryToSendRequest();
				} catch (InterruptedException e) {
					// Nothing to do here.
				}
			}
		} else {
			LOGGER.info("Tasks suppressor disabled.");
		}
	}

	public void notifyAboutJobStateChange(JobSuppressorHelper jobSuppressor) {
		jobSuppressors.push(jobSuppressor);
	}

	public boolean isEnabled() {
		return isEnabled;
	}
}
