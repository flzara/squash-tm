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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.customfield.RenderingLocation;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldHolderType;
import org.squashtest.tm.domain.denormalizedfield.DenormalizedFieldValue;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.Collection;
import java.util.List;

/**
 * Data access methods for {@link DenormalizedFieldValue}.
 * @author mpagnon
 *
 */

public interface DenormalizedFieldValueDao extends JpaRepository<DenormalizedFieldValue, Long> {

	String PARAM_ENTITY_TYPE = "entityType";
	String PARAM_ENTITY_ID = "entityId";

	DenormalizedFieldValue findById(long denormalizedFieldHolderId);

	/**
	 * Return all denormalized field values related to the denormalizedFieldHolder matching params. The list is ordered
	 * by position asc.
	 *
	 * @param denormalizedFieldHolderId
	 * @param denormalizedFieldHolderType
	 * @return the list of corresponding {@link DenormalizedFieldValue} ordered by position asc.
	 */
	@Query
	List<DenormalizedFieldValue> findDFVForEntity(@Param(PARAM_ENTITY_ID) long denormalizedFieldHolderId,
		@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType);

	/**
	 * Return all denormalized field values related to the denormalizedFieldHolder matching params. The list is ordered
	 * by position asc.
	 */
	@Query
	List<DenormalizedFieldValue> findDFVForEntityAndRenderingLocation(
		@Param(PARAM_ENTITY_ID) long denormalizedFieldHolderId,
		@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType,
		@Param("renderingLocation") RenderingLocation renderingLocation);


	@Query
	@EmptyCollectionGuard
	List<DenormalizedFieldValue> findDFVForEntities(@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType type, @Param(ParameterNames.ENTITY_IDS) Collection<Long> entities);


	@Query
	@EmptyCollectionGuard
	List<DenormalizedFieldValue> findDFVForEntitiesAndLocations(
		@Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType denormalizedFieldHolderType,
		@Param(ParameterNames.ENTITY_IDS) Collection<Long> entities,
		@Param("locations") Collection<RenderingLocation> locations);

	@Query
	long countDenormalizedFields(@Param(PARAM_ENTITY_ID) long entityId, @Param(PARAM_ENTITY_TYPE) DenormalizedFieldHolderType entityType);

}
