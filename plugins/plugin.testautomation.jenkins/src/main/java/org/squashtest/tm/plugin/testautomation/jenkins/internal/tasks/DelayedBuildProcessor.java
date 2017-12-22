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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;

public abstract class DelayedBuildProcessor extends AbstractBuildProcessor {


	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBuildProcessor.class);

	public DelayedBuildProcessor(TaskScheduler scheduler){
		super();
		this.scheduler = new ThreadPoolStepScheduler(scheduler);
	}


	@Override
	public void run() {
		notifyStepDone();
		//then return immediately
	}

	@Override
	//should be overriden by subclasses if a more appropriate treatment is needed
	public void notifyException(Exception ex) {
		if (LOGGER.isErrorEnabled()){
			LOGGER.error(ex.getMessage(),ex);
		}
	}

	@Override
	public void notifyStepDone() {
		if(getStepSequence().hasMoreElements() && ! isCanceled()){
			scheduleNextStep();
		}
	}
}
