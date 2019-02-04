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
package org.squashtest.tm.service.internal.repository;

import org.squashtest.tm.core.foundation.collection.Filtering;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.ProjectTemplate;

import java.util.Collection;
import java.util.List;

/**
 * @author Gregory Fouquet
 *
 */
public interface CustomGenericProjectDao {
	/**
	 * Coerces the Project of given id into a ProjectTemplate. This method evicts the Project from the session cache,
	 * yet it should not be invoked when the template is loaded.
	 *
	 * @param templateId
	 * @return the coerced project.
	 */
	ProjectTemplate coerceProjectIntoTemplate(long templateId);

	/**
	 * Tells whether the project of id 'projectId' is a project template or not
	 *
	 * @param projectId
	 * @return
	 */
	boolean isProjectTemplate(long projectId);

	/**
	 * Returns the list of all {@link GenericProject}s which match the given filter.
	 * @param entity {@link GenericProject} or any subtype
	 * @param filtering
	 * @return
	 */
	<T extends GenericProject> List<T> findAllWithTextProperty(Class<T> entity, Filtering filtering);

	/**
	 * Tells if a {@linkplain GenericProject} is bound to a {@linkplain ProjectTemplate}.
	 * @param genericProjectId
	 * @return
	 */
	boolean isBoundToATemplate(long genericProjectId);

	/**
	 * Tells if one of the given {@linkplain CustomFieldBinding} is bound to a Project which is bound to a Template.
	 * @return
	 */
	boolean oneIsBoundToABoundProject(Collection<Long> bindingIds);
}
