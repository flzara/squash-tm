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
package org.squashtest.tm.service.testautomation.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>
 *     This class is a preview of an AutomatedSuite would look like. It is designed to send an instant response to the
 *     user in the GUI; so it has a very dedicated purpose and is not very useful beyond that.
 * </p>
 *
 * <p>
 *     Note that in particular the instances of TestAutomationProjectContent WILL NOT include tests.
 *     Their test attribute will be an empty collection. Because it's faster that way.
 * </p>
 */
public class AutomatedSuitePreview {

	private AutomatedSuiteCreationSpecification specification = null;

	private Collection<TestAutomationProjectContent> projects = new ArrayList<>();


	public AutomatedSuiteCreationSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(AutomatedSuiteCreationSpecification specification) {
		this.specification = specification;
	}

	public Collection<TestAutomationProjectContent> getProjects() {
		return projects;
	}

	public void setProjects(Collection<TestAutomationProjectContent> projects) {
		this.projects = projects;
	}

	public void addProject(TestAutomationProjectContent project){
		this.projects.add(project);
	}
}
