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

public abstract class AbstractBuildProcessor implements BuildProcessor {

	protected StepScheduler scheduler = new SameThreadStepScheduler();

	private int defaultReschedulingInterval = 3000;

	// ******* state variables *********

	protected BuildStep<?> currentStep = null;

	protected StepFuture currentFuture = null;

	private boolean canceled;

	// ******** accessors **************

	public StepScheduler getScheduler() {
		return scheduler;
	}

	public void setDefaultReschedulingDelay(int defaultReschedulingDelay) {
		this.defaultReschedulingInterval = defaultReschedulingDelay;
	}

	protected BuildStep<?> getCurrentStep() {
		return currentStep;
	}

	protected StepFuture getCurrentFuture() {
		return currentFuture;
	}

	public int getDefaultReschedulingDelay() {
		return defaultReschedulingInterval;
	}

	// *********** code *************

	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel() {
		currentFuture.cancel();
		canceled = true;
	}

	public boolean taskHasBegun() {
		return currentStep != null;
	}

	protected void scheduleNextStep() {

		if (!taskHasBegun()) {
			currentStep = getStepSequence().nextElement();
			scheduler.schedule(currentStep);

		} else if (currentStep.needsRescheduling()) {
			reschedule();

		} else {
			currentStep = getStepSequence().nextElement();
			scheduler.schedule(currentStep);
		}

	}

	protected void reschedule() {

		int delay;

		if (currentStep.suggestedReschedulingInterval() != null) {
			delay = currentStep.suggestedReschedulingInterval();
		} else {
			delay = defaultReschedulingInterval;
		}

		currentFuture = scheduler.schedule(currentStep, delay);
	}

	protected abstract StepSequence getStepSequence();

}
