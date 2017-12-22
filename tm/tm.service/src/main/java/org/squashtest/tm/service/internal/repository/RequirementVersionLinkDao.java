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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;

/**
 * Data access methods for {@link RequirementVersionLink}. Methods are all dynamically generated.
 *
 * @author jlor
 *
 * Note: This Dao uses NamedQueries written in hibernate/package-info.
 */
public interface RequirementVersionLinkDao extends CrudRepository<RequirementVersionLink, Long>, CustomRequirementVersionLinkDao {

	/**
	 *  Find the unique {@link RequirementVersionLink} between the two RequirementVersions which Ids are given as parameters.
	 *  It gets the link with the natural direction given by the two parameters.
	 * @param reqVersionId
	 * @param relatedReqVersionId
	 * @return The unique RequirementVersionLink if it exists between the two RequirementVersions. Null otherwise.
	 */
	RequirementVersionLink findByReqVersionsIds(@Param("reqVersionId") Long reqVersionId, @Param("relatedReqVersionId") Long relatedReqVersionId);

	/**
	 * Deletes all the RequirementVersionLinks that exist between the single given RequirementVersion and all the several others.
	 * The request deletes two RequirementVersionLink per pair of ids, one for each link direction.
	 * @param singleRequirementVersionId
	 * @param requirementVersionIdsToUnlink
	 */
	@Modifying
	void deleteAllLinks(@Param("singleRequirementVersionId") long singleRequirementVersionId,
						@Param("requirementVersionIdsToUnlink") Iterable<Long> requirementVersionIdsToUnlink);
}
