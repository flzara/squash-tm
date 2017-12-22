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
package org.squashtest.tm.service.customfield;

import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.internal.dto.CustomFieldBindingModel;
import org.squashtest.tm.service.internal.dto.CustomFieldModel;

import java.util.List;
import java.util.Map;

public interface CustomFieldModelService {

	/**
	 * Find all cuf bindings as {@link CustomFieldBindingModel}, hydrated with their cuf as {@link org.squashtest.tm.service.internal.dto.CustomFieldModel} for a list of projects designed by their ids.
	 * Method is not secured, you must provide projectIds checked previously for ACLs
	 * @param projectIds The readables {@link org.squashtest.tm.domain.project.Project} ids.
	 * @return a map with {@link CustomFieldBindingModel} grouped by {@link org.squashtest.tm.domain.project.Project} id and {@link org.squashtest.tm.domain.customfield.BindableEntity}
	 * Example if i call this method for {@link org.squashtest.tm.domain.project.Project} 1 and 32
	 * PROJECT_ID : 1 The key for first project : 1
	 * 		TEST_CASE : {@link List} of {@link CustomFieldBindingModel} bound to {@link org.squashtest.tm.domain.testcase.TestCase} for the project designed by id 1.
	 * 		REQUIREMENT_VERSION : {@link List} of {@link CustomFieldBindingModel} bound to {@link org.squashtest.tm.domain.requirement.Requirement} for the project designed by id 1.
	 * 	... (All other {@link org.squashtest.tm.domain.customfield.BindableEntity})
	 *
	 * PROJECT_ID : 32 The key for second project : 32
	 * ... all {@link CustomFieldBindingModel} grouped by {@link org.squashtest.tm.domain.customfield.BindableEntity} for {@link org.squashtest.tm.domain.project.Project} 32
	 */
	Map<Long, Map<String, List<CustomFieldBindingModel>>> findCustomFieldsBindingsByProject(List<Long> projectIds);
	Map<Long, CustomFieldModel> findAllUsedCustomFieldsByEntity(List<Long> projectIds,BindableEntity entity);
}
