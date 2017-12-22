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

import org.squashtest.tm.service.testautomation.spi.TestAutomationException;

public abstract class SynchronousBuildProcessor<RESULT> extends AbstractBuildProcessor {

	@Override
	public void run() {

		while( getStepSequence().hasMoreElements() && ! isCanceled()){
			scheduleNextStep();
		}

		buildResult();
	}


	@Override
	public void notifyStepDone() {
		//nothing, let the method run loop
	}


	public abstract RESULT getResult();


	protected abstract void buildResult();


	@Override
	public void notifyException(Exception ex) {
		// TODO this is either equivalent to an instanceof or too arcane for my puny brain
		try{
			throw ex;
		}
		catch(TestAutomationException exe){
			throw exe;
		}
		catch(Exception exe){
			throw new TestAutomationException(exe);
		}
	}
}
