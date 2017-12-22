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
package org.squashtest.tm.service.requirement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.LinkedRequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.exception.requirement.link.AlreadyLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.SameRequirementLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException;

/**
 * Service for management of Requirement Versions linked to other Requirement Versions.
 *
 * @author jlor
 *
 */
public interface LinkedRequirementVersionManagerService {

	/**
	 * Finds all the {@link LinkedRequirementVersion} linked to the {@link RequirementVersion}
	 * whose id is given as parameter and returns the result as a {@link PagedCollectionHolder}
	 * according to the specifications of the given {@link PagingAndSorting}.
	 *
	 * @param requirementVersionId The id of the {@link RequirementVersion} of which all the {@link LinkedRequirementVersion} will be found.
	 * @param pagingAndSorting The Paging and Sorting specifications.
	 *
	 * @return A {@link PagedCollectionHolder} corresponding to the given {@link PagingAndSorting}, containing all the {@link LinkedRequirementVersion}
	 */
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<LinkedRequirementVersion>> findAllByRequirementVersion(
		long requirementVersionId, PagingAndSorting pagingAndSorting);

	/**
	 * Removes all the {@link RequirementVersionLink} involving the single {@link RequirementVersion}
	 * whose id is given as parameter and all the other {@link RequirementVersion} whose ids are given as parameter.
	 *
	 * @param requirementVersionId The single {@link RequirementVersion} id
	 * @param requirementVersionIdsToUnlink The other {@link RequirementVersion} ids
	 */
	void removeLinkedRequirementVersionsFromRequirementVersion(
		long requirementVersionId, List<Long> requirementVersionIdsToUnlink);

	/**
	 * Creates all the {@link RequirementVersionLink} between the single {@link RequirementVersion}
	 * whose id is given as parameter and all the other {@link RequirementVersion} whose ids are given as parameters.
	 *
	 * @param singleReqVersionId The id of the single {@link RequirementVersion}
	 * @param otherReqVersionsIds The ids of the other {@link RequirementVersion}
	 *
	 * @return A {@link Collection} containing all the {@link LinkedRequirementVersionException}
	 * which appeared while trying to link all the {@link RequirementVersion}.
	 */
	Collection<LinkedRequirementVersionException> addLinkedReqVersionsToReqVersion(
		Long singleReqVersionId, List<Long> otherReqVersionsIds);

	Collection<LinkedRequirementVersionException> addDefaultLinkWithNodeIds(
		Long reqVersionNodeId, Long relatedReqVersionNodeId);

	void addOrUpdateRequirementLink(Long sourceVersionId, Long destVersionId, String destRole);

	List<RequirementVersionLinkType> getAllReqVersionLinkTypes();

	PagedCollectionHolder<List<RequirementVersionLinkType>> getAllPagedAndSortedReqVersionLinkTypes(PagingAndSorting pas);

	void updateLinkTypeAndDirection(
		long requirementVersionId, long relatedRequirementNodeId, boolean isRelatedIdANodeId,
		long reqVersionLinkTypeId, boolean reqVersionLinkTypeDirection);

	void copyRequirementVersionLinks(RequirementVersion previousVersion, RequirementVersion newVersion);

	RequirementVersionLink addDetailedReqVersionLink(
		long reqVersionId, long relatedReqVersionId, long linkTypeId, boolean linkDirection);

	Set<String> findAllRoleCodes();

	void postponeTestCaseToNewRequirementVersion(RequirementVersion previousVersion, RequirementVersion newVersion);

	void checkIfLinkAlreadyExists(RequirementVersion reqVersion, RequirementVersion relatedReqVersion)
		throws AlreadyLinkedRequirementVersionException;

	void checkIfSameRequirement(RequirementVersion reqVersion, RequirementVersion relatedReqVersion)
		throws SameRequirementLinkedRequirementVersionException;

	void checkIfVersionsAreLinkable(RequirementVersion reqVersion, RequirementVersion relatedReqVersion)
		throws UnlinkableLinkedRequirementVersionException;
}
