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
package org.squashtest.tm.service.project;

import org.squashtest.tm.core.dynamicmanager.factory.DynamicManagerInterfaceSpecification
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.project.GenericProjectManagerService;
import org.squashtest.tm.service.project.ProjectManagerService;

import spock.lang.Shared

/**
 * @author mpagnon
 *
 */
class GenericProjectDynamicManagerTest extends DynamicManagerInterfaceSpecification {
	@Shared Class entityType = Project
	@Shared Class managerType = GenericProjectManagerService

	@Shared List changeServiceCalls = [
		{
			it.changeDescription(10L, "foo")
		},
		{
			it.changeLabel(10L, "bar")
		},
		{
			it.changeName(10L, "bar")
		},
		{
			it.changeActive(10L, true)
		}
	]
}
