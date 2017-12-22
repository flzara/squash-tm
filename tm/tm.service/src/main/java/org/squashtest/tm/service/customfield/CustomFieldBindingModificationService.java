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

import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.project.Project;

/**
 * An interface for services around {@link CustomField}. The user calling the following methods must have a role 'admin'
 * or 'project manager'.
 * 
 * 
 * @author bsiri
 * 
 */
public interface CustomFieldBindingModificationService extends CustomFieldBindingFinderService {

	/**
	 * Will attach a {@link CustomField} to a {@link Project}. The details and conditions of that binding is described
	 * in the {@link CustomFieldBinding} newBinding. The new binding will be inserted last.
	 * 
	 * @param projectId
	 * @param customFieldId
	 * @param entity
	 * @param newBinding
	 */
	void addNewCustomFieldBinding(long projectId, BindableEntity entity, long customFieldId,
			CustomFieldBinding newBinding);

	/**
	 * Add a rendering location to a custom field binding
	 * 
	 * @param bindingId
	 * @param location
	 */
	void addRenderingLocation(long bindingId, RenderingLocation location);

	/**
	 * Remove the rendering location from a custom field binding
	 * 
	 * @param bindingId
	 * @param location
	 */
	void removeRenderingLocation(long bindingId, RenderingLocation location);

	/**
	 * removes a batch of custom field bindings using their ids.
	 * 
	 */
	void removeCustomFieldBindings(List<Long> bindingIds);

	/**
	 * removes all the custom field bindings defined for a project.
	 * 
	 * @param projectId
	 *            the id of the project
	 */
	void removeCustomFieldBindings(Long projectId);

	/**
	 * Given a list of {@link CustomFieldBinding}s, will reorder them with respect to their project and bound entity.
	 * This method assumes that they all bind the same entity to the same project. If the input list mixes binding for
	 * different projects and/or entities, unexpected behavior may occur.
	 * 
	 * @param bindingIds
	 * @param newIndex
	 */
	void moveCustomFieldbindings(List<Long> bindingIds, int newIndex);

	/**
	 * Will copy the custom field bindings of the template and apply them to the project.
	 * 
	 * @param target
	 * @param source
	 */
	void copyCustomFieldsSettingsFromTemplate(GenericProject target, GenericProject source);

}
