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
package org.squashtest.tm.plugin.testautomation.jenkins.internal;

import static org.squashtest.tm.plugin.testautomation.jenkins.internal.BuildStage.GATHER_RESULT;

import java.util.NoSuchElementException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.AbstractBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.BuildStep;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.StartBuild;

class FetchTestListStepSequence extends HttpBasedStepSequence implements StepSequence {


	private AbstractBuildProcessor processor;


	// ************** constructor ****************

	FetchTestListStepSequence(AbstractBuildProcessor processor) {
		super();
		this.processor=processor;
	}

	// ************* setters **************

	@Override
	void setClient(CloseableHttpClient client) {
		this.client = client;
	}


	@Override
	void setProject(TestAutomationProject project) {
		this.project = project;
	}


	@Override
	void setAbsoluteId(BuildAbsoluteId absoluteId) {
		this.absoluteId = absoluteId;
	}

	// ************** getters *************

	@Override
	protected AbstractBuildProcessor getProcessor() {
		return processor;
	}

	//*************** code ****************


	@Override
	public boolean hasMoreElements() {
		return currentStage != GATHER_RESULT;
	}

	@Override
	public BuildStep<?> nextElement() {
		switch(currentStage){

		case WAITING :
			currentStage = BuildStage.START_BUILD;
			return newStartBuild();

		case START_BUILD :
			currentStage = BuildStage.CHECK_QUEUE;
			return newCheckQueue();

		case CHECK_QUEUE :
			currentStage = BuildStage.GET_BUILD_ID;
			return newGetBuildID();

		case GET_BUILD_ID :
			currentStage = BuildStage.CHECK_BUILD_RUNNING;
			return newCheckBuildRunning();

		case CHECK_BUILD_RUNNING :
			currentStage = BuildStage.GATHER_RESULT;
			return newGatherTestList();

		case GATHER_RESULT :
			throw new NoSuchElementException();

		default : throw new NoSuchElementException();


		}
	}


	protected StartBuild newStartBuild(){

		HttpPost method = requestFactory.newStartFetchTestListBuild(project, absoluteId.getExternalId());

		StartBuild startBuild = new StartBuild(getProcessor());

		wireHttpSteps(startBuild, method);

		return startBuild;

	}

}
