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

import java.util.List;

import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.Paging;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.project.GenericProject;

/**
 * An interface for services around {@link CustomField}. This is a 'finder' service : those methods are meant to find
 * data, not to modify them.
 * 
 * The methods in this service need not to be secured.
 * 
 * 
 * @author bsiri
 * 
 */

public interface CustomFieldBindingFinderService {

	/**
	 * returns all the existing custom fields.
	 * 
	 * @return what I just said.
	 */
	List<CustomField> findAvailableCustomFields();

	/**
	 * same as {@link #findAvailableCustomFields()}, restricted to the given project and bindable entity. The only
	 * returned fields are those who aren't already mapped for that project and entity.
	 * 
	 * @param projectId
	 * @param entity
	 * @return
	 */
	List<CustomField> findAvailableCustomFields(long projectId, BindableEntity entity);
	
	
	/**
	 * Returns the complementary of {@link #findAvailableCustomFields(long, BindableEntity)}
	 * 
	 * @param projectId
	 * @param entity
	 * @return
	 */
	List<CustomField> findBoundCustomFields(long projectId, BindableEntity entity);

	
	/**
	 * returns all the custom field bindings associated to a {@linkplain GenericProject}.
	 * 
	 * 
	 * @param projectId
	 * @return
	 */
	List<CustomFieldBinding> findCustomFieldsForGenericProject(long projectId);

	/**
	 * 
	 * returns all the custom field bindings associated to a project for a given entity type
	 * 
	 * @param projectId
	 * @return
	 */
	List<CustomFieldBinding> findCustomFieldsForProjectAndEntity(long projectId, BindableEntity entity);

	/**
	 * returns all the custom field bindinds associated to that entity wrt its project and {@link BindableEntity} type
	 * 
	 * @param boundEntity
	 * @return
	 */
	List<CustomFieldBinding> findCustomFieldsForBoundEntity(BoundEntity boundEntity);

	/**
	 * 
	 * returns all the custom field bindings associated to a project for a given entity type (paged version)
	 * 
	 * @param projectId
	 * @return
	 */
	PagedCollectionHolder<List<CustomFieldBinding>> findCustomFieldsForProjectAndEntity(long projectId,
			BindableEntity entity, Paging paging);

}
