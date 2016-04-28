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
