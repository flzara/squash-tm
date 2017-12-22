/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.TaskScheduler;

public class ThreadPoolStepScheduler implements StepScheduler {

	private TaskScheduler scheduler;

	public TaskScheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(TaskScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public ThreadPoolStepScheduler() {
		super();
	}

	public ThreadPoolStepScheduler(TaskScheduler scheduler) {
		this.scheduler = scheduler;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public StepFuture schedule(BuildStep step, int millisDelay) {
		Date startTime = new Date(System.currentTimeMillis() + millisDelay);

		ScheduledFuture<?> future = scheduler.schedule(step, startTime);

		return new ScheduledFutureWrapper(future);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public StepFuture schedule(BuildStep step) {
		return schedule(step, 0);
	}

	private static class ScheduledFutureWrapper implements StepFuture {

		private ScheduledFuture<?> future;

		public ScheduledFutureWrapper(ScheduledFuture<?> future) {
			this.future = future;
		}

		@Override
		public void cancel() {
			if (!future.isCancelled() && !future.isDone()) {
				future.cancel(false);
			}
		}

	}

}
