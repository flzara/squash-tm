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

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.CustomFieldValue;
import org.squashtest.tm.domain.customfield.RenderingLocation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CustomFieldValueFinderService {

	/**
	 * Will return the list of the custom field values associated to the specified bound entity.
	 *
	 * The authenticated use must be administrator or have read permission on that entity.
	 *
	 * @param entityId
	 * @param entityType
	 * @return
	 */
	List<CustomFieldValue> findAllCustomFieldValues(BoundEntity boundEntity);

	/**
	 * Same as {@link #findAllCustomFieldValues(BoundEntity)}, using a List of entities instead. This method is pure
	 * convenience, to fetch custom fields in bulk (and soften the db queries overhead).
	 *
	 * The order of the result is arbitrary.
	 *
	 * @param boundEntity
	 * @return
	 */
	List<CustomFieldValue> findAllCustomFieldValues(Collection<? extends BoundEntity> boundEntities);

	/**
	 * Same as {@link #findAllCustomFieldValues(Collection)}, but only the values refering to one of the custom fields
	 * given as argument will be retained.
	 *
	 *
	 * @param boundEntities
	 * @param restrictedToThoseCustomfields
	 * @return
	 */
	List<CustomFieldValue> findAllCustomFieldValues(Collection<? extends BoundEntity> boundEntities,
													Collection<CustomField> restrictedToThoseCustomfields);

	/**
	 * Tells whether the given bound entity has custom fields or not.
	 *
	 *
	 * @param boundEntity
	 * @return
	 */
	boolean hasCustomFields(BoundEntity boundEntity);

	/**
	 * Same as {@link #hasCustomFields(BoundEntity)}, the bound entity being identified by its type and id
	 *
	 * @param boundEntityId
	 * @param bindableEntity
	 * @return
	 */
	boolean hasCustomFields(Long boundEntityId, BindableEntity bindableEntity);

	/**
	 * Same as {@link #findAllCustomFieldValues(BoundEntity)}, but the properties identifying a BoundEntity are broken
	 * down into its ID and type.
	 *
	 * @param boundEntityId
	 * @param bindableEntity
	 * @return
	 */
	List<CustomFieldValue> findAllCustomFieldValues(long boundEntityId, BindableEntity bindableEntity);

	/**
	 * Tells if the CF values of the given entity are editable, according to both security rules and sensible business
	 * rules.
	 *
	 * @param boundEntityId
	 * @param bindableEntity
	 * @return
	 */
	boolean areValuesEditable(long boundEntityId, BindableEntity bindableEntity);


	List<CustomFieldValue> findAllForEntityAndRenderingLocation(BoundEntity boundEntity, RenderingLocation renderingLocation);

	/**
	 * Given a campaignId and a Map of the requested CustomField ids Lists mapped by EntityType,
	 * return a Map which keys are all the entities contained in the Campaign as EntityReferences
	 * and values are Maps listing CustomFieldValues mapped by CustomField ids.
	 * @param campaignId The Campaign id
	 * @param cufIdsMapByEntityType The Map of the CustomField ids lists mapped by EntityType.
	 * @return The Map<EntityReference, Map<Long, Object>> where the keys are the entities contained in the Campaign and
	 * the values are Maps of CustomFieldValues mapped by CustomField ids.
	 */
	Map<EntityReference, Map<Long, Object>> getCufValueMapByEntityRef(EntityReference entity, Map<EntityType, List<Long>> cufIdsMapByEntityType);
}
