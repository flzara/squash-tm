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

import javax.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * RequirementVersion management services which cannot be dyanmically generated.
 *
 * @author Gregory Fouquet
 *
 */
public interface CustomRequirementVersionManagerService {


	Requirement findRequirementById(long requirementId);

	List<Requirement> findRequirementsAllByIds(List<Long> requirementIds);

	/**
	 * Increase the current version of the given requirement. Note that the argument is a requirement Id, not the id of a given version
	 * If links inheritance was chosen, all requirement version links will be copied from the previous version.
	 *
	 * @param requirementId
	 */
	void createNewVersion(long requirementId, boolean inheritReqLinks, boolean inheritTestcasesReqLinks);

	/**
	 * Increase the current version of the given requirement and associates the requirement to the given milestones. If other versions of the same requirements
	 * were bound to those milestones, they won't be anymore.
	 *
	 * Note that the argument is a requirement Id, not the id of a given version
	 *
	 * If links inheritance was chosen, all requirement version links will be copied from the previous version.
	 *
	 * @param requirementId
	 */
	void createNewVersion(long requirementId, Collection<Long> milestoneIds, boolean inheritReqLinks, boolean inheritTestcasesReqLinks);


	void rename(long requirementVersionId, String newName);

	/**
	 * will change the requirement criticality and update the importance of any associated TestCase with importanceAuto
	 * == true.<br>
	 * (even through call steps)
	 *
	 * @param requirementVersionId
	 * @param criticality
	 */
	void changeCriticality(long requirementVersionId, @NotNull RequirementCriticality criticality);

	void changeCategory(long requirementVersionId, String categoryCode);

	/**
	 * Applies a {@link RequirementBulkUpdate} to a bunch of requirement versions given their ids. The operation can
	 * complete or fail individually for each RV. The ID of the RV for which it fails is returned in the result. In
	 * particular an empty list indicates a success for all.
	 *  @param requirementVersionIds
	 * @param update
	 */
	Collection<Long> bulkUpdate(List<Long> requirementVersionIds, RequirementBulkUpdate update);

	/**
	 * Fetches the paged, sorted collection of versions for the given requirement.
	 *
	 * @param requirementId
	 * @param pas
	 * @return
	 */
	@Transactional(readOnly = true)
	Page<RequirementVersion> findAllByRequirement(long requirementId, @NotNull Pageable pas);

	/**
	 * Fetches all versions for the given requirement
	 * @param id
	 * @return
	 */
	@Transactional(readOnly=true)
	List<RequirementVersion> findAllByRequirement(long requirementId);

	@Transactional(readOnly=true)
	Long findReqVersionIdByRequirementAndVersionNumber(long requirementId, Integer versionNumber);

	RequirementVersion findByRequirementIdAndVersionNumber(long requirementId, int versionNumber);

	/*
	 *
	 *
	 * Milestones
	 *
	 */
	void bindMilestones(long versionId, Collection<Long> milestoneIds);

	void unbindMilestones(long versionId, Collection<Long> milestoneIds);

	@Transactional(readOnly=true)
	Collection<Milestone> findAssociableMilestones(long versionId);

	@Transactional(readOnly=true)
	Collection<Milestone> findAllMilestones(long versionId);

	Collection<Milestone> findAssociableMilestonesForMassModif(List<Long> reqVersionIds);

	Collection<Long> findBindedMilestonesIdForMassModif(List<Long> reqVersionIds);

	boolean haveSamePerimeter(List<Long> reqVersionIds);

	boolean isOneMilestoneAlreadyBindToAnotherRequirementVersion(List<Long> reqVIds, List<Long> milestoneIds);



}
