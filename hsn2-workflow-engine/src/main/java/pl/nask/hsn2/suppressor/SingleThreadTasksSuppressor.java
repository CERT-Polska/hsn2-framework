package pl.nask.hsn2.suppressor;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.framework.suppressor.JobSuppressorHelper;

public class SingleThreadTasksSuppressor extends Thread {
	private final static Logger LOGGER = LoggerFactory.getLogger(SingleThreadTasksSuppressor.class);
	private BlockingDeque<JobSuppressorHelper> jobSuppressors = new LinkedBlockingDeque<>();

	@Override
	public void run() {
		LOGGER.info("Tasks suppressor started.");
		while (true) {
			try {
				LOGGER.info("Waiting for action request.");

				// Block until there is some action to do.
				JobSuppressorHelper js = jobSuppressors.take();

				// Do the action.
				js.tryToSendRequest();
			} catch (InterruptedException e) {
				// Nothing to do here.
			}
		}
	}

	public void notifyAboutJobStateChange(JobSuppressorHelper jobSuppressor) {
		jobSuppressors.push(jobSuppressor);
	}
}
