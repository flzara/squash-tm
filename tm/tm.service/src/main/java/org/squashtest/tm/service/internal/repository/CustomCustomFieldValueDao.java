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

import org.squashtest.tm.domain.EntityReference;
import org.squashtest.tm.domain.EntityType;

import java.util.List;
import java.util.Map;

public interface CustomCustomFieldValueDao {

	/**
	 * Given a scope represented by an EntityReference
	 * and a Map listing all the CustomField ids requested mapped by EntityType,
	 * get a Map which keys are EntityReferences contained in the Campaign and values are
	 * Maps containing CustomFieldValues mapped by CustomField id.
	 * @param scopeEntity The EntityReference representing the scope
	 * @param cufMapByEntityType A Map containing the list of requested CustomField ids mapped by EntityType
	 * @return A Map which keys are EntityReferences and values are Maps containing CustomFieldValues mapped by CustomField id.
	 * <br/>
	 * <b>
	 *     Careful: It is possible to request denormalized CustomFieldValues of ExecutionSteps
	 * by adding the EntityType.TEST_STEP in the <i>cufMapByEntityType</i> parameter. In this case, the resulting Map
	 * will contain EntityReferences of type TEST_STEP but with the ID corresponding to the referenced EXECUTION_STEP.
	 * </b>
	 */
	Map<EntityReference, Map<Long, Object>> getCufValuesMapByEntityReference(
		EntityReference scopeEntity, Map<EntityType, List<Long>> cufMapByEntityType);
}
