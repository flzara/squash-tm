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
package org.squashtest.tm.service.internal.customfield;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.project.Project;
import org.squashtest.tm.service.customfield.CustomFieldValueManagerService;

/**
 * That interface is called so because it should remain private to this bundle. The reason is that the methods will not
 * be secured.
 * 
 * @author bsiri
 * 
 */
public interface PrivateCustomFieldValueService extends CustomFieldValueManagerService {

	/**
	 * Will create a custom field value for all the entities affected by the given binding
	 * 
	 * @param binding
	 */
	void cascadeCustomFieldValuesCreation(CustomFieldBinding binding);

	/**
	 * Will remove the custom field values corresponding to the given binding
	 * 
	 * @param binding
	 */
	void cascadeCustomFieldValuesDeletion(CustomFieldBinding binding);

	/**
	 * Will remove the custom field values corresponding to the bindings, given their ids.
	 * 
	 * @param binding
	 */
	void cascadeCustomFieldValuesDeletion(List<Long> customFieldBindingIds);

	/**
	 * Will create all the custom field values for one entity.
	 * 
	 * @param entity
	 * @param project
	 *            . If null, the project of the given entity will be used.
	 */
	void createAllCustomFieldValues(BoundEntity entity, Project project);

	
	/**
	 * batched version of {@link #createAllCustomFieldValues(BoundEntity, Project)}. 
	 * The entities are assumed to be all of the same concrete class and of the 
	 * same project.
	 * 
	 * 
	 * @param entities
	 * @param project
	 */
	void createAllCustomFieldValues(Collection<? extends BoundEntity> entities, Project project);
	
	/**
	 * will delete all the custom field vales for one entity
	 * 
	 * @param entity
	 */
	void deleteAllCustomFieldValues(BoundEntity entity);

	/**
	 * Will delete all the custom field values for multiple BoundEntities
	 * 
	 * @param entityType
	 *            the BindableEntity that all of the BoundEntity must share
	 * @param entityIds
	 *            the ids of those BoundEntities
	 */
	void deleteAllCustomFieldValues(BindableEntity entityType, List<Long> entityIds);

	/**
	 * Will copy the custom field values from an entity to another entity, creating them in the process
	 * 
	 * @param source : the {@link BoundEntity} from witch the cuf are copied
	 * @param recipient : the BoundEntity
	 */
	void copyCustomFieldValues(BoundEntity source, BoundEntity recipient);

	/**
	 * *Will copy the custom field values from entities to others, creating them in the process
	 * 
	 * @param copiedEntityBySource
	 *            : a Map with
	 *            <ul>
	 *            <li>key : the source BoundEntity id</li>
	 *            <li>value : the copy BoundEntity  to add the cufs to</li>
	 *            </ul>
	 * @param bindableEntityType
	 *            : the {@link BindableEntity} type for all BoundEntity in the "copiedEntityBySource" map.
	 */
	void copyCustomFieldValues(Map<Long, BoundEntity> copiedEntityBySource, BindableEntity bindableEntityType);

	/**
	 * Will copy the custom field values from an entity to another entity. It assumes that the custom field values
	 * already exists for both, and will simply invoke {@link CustomFieldValue#setValue(String)} from one to the other.
	 * 
	 * 
	 * @param source
	 * @param dest
	 */
	void copyCustomFieldValuesContent(BoundEntity source, BoundEntity recipient);

	/**
	 * Will ensure that the custom field values of an entity are consistent with the custom fields bound to the project
	 * it belongs to, at the time when the method is invoked. Indeed inconsistencies arise when an entity is moved from
	 * one project to another. This method will fix these inconsistencies in three steps :
	 * <ol>
	 * <li>creating the custom field values corresponding to the bindings of the current project,</li>
	 * <li>copy the custom field values from the former project into those of the new project if any matching values are
	 * found,</li>
	 * <li>delete the custom field values from the former project.</li>
	 * <ol>
	 * 
	 * 
	 * @param entity
	 */
	void migrateCustomFieldValues(BoundEntity entity);

	/**
	 * Same as {@link #migrateCustomFieldValues(BoundEntity)}, batched version.
	 * 
	 * @param entities
	 */
	void migrateCustomFieldValues(Collection<BoundEntity> entities);

}
