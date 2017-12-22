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

import javax.validation.constraints.NotNull;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.customfield.CustomField;
import org.squashtest.tm.domain.customfield.SingleSelectField;

/**
 * Facade service for custom fields read only access methods
 * 
 * @author mpagnon
 * 
 */
@Transactional(readOnly = true)
public interface CustomFieldFinderService {
	/**
	 * Will find all custom fields available and return them into a list ordered by custom field's name.
	 * 
	 * @return the list of all existing {@link CustomField} ordered by {@link CustomField#getName()}
	 */
	List<CustomField> findAllOrderedByName();

	/**
	 * Will find all custom fields available and return them ordered according to the given parameters.
	 * 
	 * @param filter
	 *            the {@link PagingAndSorting} that holds order and paging params.
	 * @return the filtered collection of all custom field available in squash
	 */
	PagedCollectionHolder<List<CustomField>> findSortedCustomFields(PagingAndSorting filter);

	/**
	 * Will find the {@link SingleSelectField} of the given id
	 * 
	 * @param customFieldId
	 *            the id of the {@link SingleSelectField}
	 * @return the {@link SingleSelectField} or <code>null</code>
	 */
	SingleSelectField findSingleSelectFieldById(Long customFieldId);

	/**
	 * @param name
	 * @return
	 */
	CustomField findByName(@NotNull String name);

	CustomField findById(Long customFieldId);

}
