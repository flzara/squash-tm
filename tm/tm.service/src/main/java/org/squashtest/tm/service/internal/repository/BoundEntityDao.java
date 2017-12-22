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

import java.util.List;

import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.BoundEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customfield.CustomFieldValue;

public interface BoundEntityDao {

	/**
	 * given a {@link CustomFieldBinding, returns all the existing entities subject to that binding; namely all the {@link BindableEntity} that belongs
	 * to the project of that binding.
	 * 
	 * @param customFieldBinding
	 * @return
	 */
	List<BoundEntity> findAllForBinding(CustomFieldBinding customFieldBinding);
	
	
	/**
	 * given a {@link CustomFieldValue} id, returns the {@link BoundEntity} instance it is related to.
	 * 
	 * @param customFieldValueId
	 * @return
	 */
	BoundEntity findBoundEntity(CustomFieldValue customFieldValue);
	
	
	/**
	 * Will retrieve a {@link BoundEntity} according to its ID and type.
	 * 
	 * @param boundEntityId
	 * @param entityType
	 * @return
	 */
	BoundEntity findBoundEntity(Long boundEntityId, BindableEntity entityType);
	
	
	/**
	 * Tells whether the given bound entity has custom fields or not.
	 * 
	 * @param boundEntityId
	 * @param entityType
	 * @return
	 */
	boolean hasCustomField(Long boundEntityId, BindableEntity entityType);
	
}
