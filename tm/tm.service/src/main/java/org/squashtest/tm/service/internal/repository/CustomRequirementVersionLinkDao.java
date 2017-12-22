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
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;

import java.util.List;

/**
 * Created by jlor on 19/05/2017.
 */
public interface CustomRequirementVersionLinkDao {

	/**
	 * Returns a paged and ordered list of all the {@link RequirementVersionLink} in which the given {@link RequirementVersion} is involved.
	 * This method is used to display the returned list: it only returns the {@link RequirementVersionLink} in which the given RequirementVersion
	 * is the requirementVersion and not the relatedRequirementVersion. See {@link RequirementVersionLink}
	 * @param requirementVersionId The ID of the Requirement Version of which we want all the Links.
	 * @return The List of all the {@link RequirementVersionLink} in which the given RequirementVersion is involved.
	 */
	List<RequirementVersionLink> findAllByReqVersionId(@Param("requirementVersionId") long requirementVersionId, PagingAndSorting pagingAndSorting);
	/**
	 *  Verifies if a link already exists between the two RequirementVersions which Ids are given as parameters.
	 *  It only check the existence of the link in a single direction, since the other direction should also appear in the database.
	 * @param reqVersionId
	 * @param relatedReqVersionId
	 * @return true if the link already exists between the two RequirementVersions, false otherwise.
	 */
	boolean linkAlreadyExists(Long reqVersionId, Long relatedReqVersionId);
	/**
	 * Saves a given RequirementVersionLink. Given the particular model, the link must be saved twice: one time for each
	 * linkDirection.
	 * Use the returned instance for further operations as the save operation might have changed the
	 * entity instance completely.
	 * @param requirementVersionLink The RequirementVersionLink to persist.
	 * @return The persisted RequirementVersionLink given as parameter.
	 */
	RequirementVersionLink addLink(RequirementVersionLink requirementVersionLink);

	/**
	 * Saves the given RequirementVersionLinks.
	 * @see #addLink(RequirementVersionLink) */
	void addLinks(List<RequirementVersionLink> requirementVersionLinks);

	/** For all the links of given type, replace the given type by the default one.
	 * @param linkTypeToReplace
	 * @param defaultLinkType
	 * */
	@Modifying
	void setLinksTypeToDefault(RequirementVersionLinkType linkTypeToReplace, RequirementVersionLinkType defaultLinkType);
}
