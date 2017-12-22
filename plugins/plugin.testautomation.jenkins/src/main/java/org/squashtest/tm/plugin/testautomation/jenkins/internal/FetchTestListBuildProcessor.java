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

import org.apache.http.impl.client.CloseableHttpClient;
import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.StepSequence;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasks.SynchronousBuildProcessor;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.BuildAbsoluteId;
import org.squashtest.tm.plugin.testautomation.jenkins.internal.tasksteps.GatherTestList;

import java.util.ArrayList;
import java.util.Collection;


public class FetchTestListBuildProcessor extends SynchronousBuildProcessor<Collection<AutomatedTest>> {

	private FetchTestListStepSequence stepSequence = new FetchTestListStepSequence(this);

	private TestAutomationProject project;

	//******* collaborators *********

	public void setClient(CloseableHttpClient client) {
		stepSequence.setClient(client);
	}

	public void setProject(TestAutomationProject project) {
		stepSequence.setProject(project);
		this.project = project;
	}

	public void setBuildAbsoluteId(BuildAbsoluteId absoluteId) {
		stepSequence.setAbsoluteId(absoluteId);
	}


	//******* the result we obtain once the computation is over *********

	private Collection<AutomatedTest> tests = new ArrayList<>();


	@Override
	public Collection<AutomatedTest> getResult() {
		return tests;
	}

	@Override
	protected void buildResult() {

		if (!stepSequence.hasMoreElements()) {

			Collection<String> names = ((GatherTestList) currentStep).getTestNames();

			for (String name : names) {
				AutomatedTest test = new AutomatedTest(name, project);
				tests.add(test);
			}

		} else {
			throw new RuntimeException("tried to build the result before the computation is over, probably due to a buggy thread");
		}
	}

	@Override
	protected StepSequence getStepSequence() {
		return stepSequence;
	}
}
