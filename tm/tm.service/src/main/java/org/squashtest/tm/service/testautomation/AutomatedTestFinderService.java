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
package org.squashtest.tm.service.testautomation;

import java.util.Collection;
import java.util.Optional;

import org.squashtest.tm.domain.testautomation.AutomatedTest;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.service.testautomation.model.TestAutomationProjectContent;

public interface AutomatedTestFinderService {


	/**
	 * Returns the aggregated results of {@link #listTestsFromRemoteServers(Collection)}
	 * and {@link #listTestsFromScm(Collection)}.
	 *
	 * @param projects
	 * @return
	 */
	Collection<TestAutomationProjectContent> listTestsInProjects(Collection<TestAutomationProject> projects);

	/**
	 * Given a collection of {@link TestAutomationProject}, will fetch the tests published on the remote test servers and
	 * returns the aggregated list of {@link AutomatedTest} paired with their owner project. The returned instances of
	 * {@link AutomatedTest} are transient, ie none is retrieved from the database and have no ID.
	 *
	 * @param projects
	 * @return
	 */
	Collection<TestAutomationProjectContent> listTestsFromRemoteServers(Collection<TestAutomationProject> projects);

	/**
	 * Given a collection of {@link TestAutomationProject}, will retrieve the tests from the SCM repositories that <i>could</i>
	 * run on them. The tests scripts will be paired with an automation project if that project can run the language of those scripts
	 * (ie if there is a match on supported technology). Furthermore tests hosted in a repository will be paired to an automation project
	 * only if they are bound to same Squash TM project.
	 *
	 * @param projects
	 * @return
	 */
	public Collection<TestAutomationProjectContent> listTestsFromScm(Collection<TestAutomationProject> projects);


}
