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

import org.squashtest.tm.domain.testautomation.TestAutomationProject;

public interface TestAutomationProjectManagerService extends TestAutomationProjectFinderService {

	// *********************** entity management *******************

	void persist(TestAutomationProject newProject);

	void deleteProject(long projectId);

	void deleteAllForTMProject(long tmProjectId);

	// *********************** Properties mutators ****************************

	void changeLabel(long projectId, String name);

	void changeJobName(long projectId, String jobName);

	/**
	 * Note : the slave list is a semicolon separated list
	 * 
	 */
	void changeSlaves(long projectId, String slaveList);

	/**
	 * Will edit the label, jobName and slaves properties of the {@link TestAutomationProject} matching the given id
	 * with the ones held by the given newValues parameter.
	 * 
	 * @param projectId
	 * @param newValues
	 */
	void editProject(long projectId, TestAutomationProject newValues);

}
