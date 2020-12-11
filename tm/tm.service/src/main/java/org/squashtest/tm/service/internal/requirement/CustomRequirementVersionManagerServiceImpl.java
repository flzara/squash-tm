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
package org.squashtest.tm.service.internal.requirement;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.IdCollector;
import org.squashtest.tm.domain.attachment.Attachment;
import org.squashtest.tm.domain.infolist.InfoListItem;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersion.PropertiesSetter;
import org.squashtest.tm.exception.InconsistentInfoListItemException;
import org.squashtest.tm.exception.requirement.IllegalRequirementModificationException;
import org.squashtest.tm.service.annotation.CheckLockedMilestone;
import org.squashtest.tm.service.annotation.Id;
import org.squashtest.tm.service.attachment.AttachmentManagerService;
import org.squashtest.tm.service.infolist.InfoListItemFinderService;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.MilestoneDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.milestone.MilestoneMembershipManager;
import org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;
import org.squashtest.tm.service.requirement.RequirementBulkUpdate;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.squashtest.tm.service.security.Authorizations.CREATE_REQUIREMENT_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_REQUIREMENT_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_REQVERSION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.WRITE_REQVERSION_OR_ROLE_ADMIN;

/**
 * @author Gregory Fouquet
 *
 */
@Service("CustomRequirementVersionManagerService")
@Transactional
public class CustomRequirementVersionManagerServiceImpl implements CustomRequirementVersionManagerService {

	@Inject
	private MilestoneDao milestoneDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private RequirementDao requirementDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private InfoListItemFinderService infoListItemService;

	@Inject
	private MilestoneMembershipManager milestoneManager;

	@Inject
	private PrivateCustomFieldValueService customFieldValueService;

	@Inject
	private LinkedRequirementVersionManagerService requirementLinkService;

	@Inject
	private PermissionEvaluationService permService;

	@Inject
	AttachmentManagerService attachmentManagerService;

	@PersistenceContext
	private EntityManager em;

	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;

	@Override
	@PreAuthorize(READ_REQUIREMENT_OR_ROLE_ADMIN)
	public Requirement findRequirementById(long requirementId) {
		return requirementVersionDao.findRequirementById(requirementId);
	}

	@Override
	@PostFilter("hasPermission(filterObject , 'READ')" + OR_HAS_ROLE_ADMIN)
	public List<Requirement> findRequirementsAllByIds(List<Long> requirementIds) {
		return requirementDao.findAllByIds(requirementIds);
	}

	@Override
	@PreAuthorize(CREATE_REQUIREMENT_OR_ROLE_ADMIN)
	public void createNewVersion(long requirementId, boolean inheritReqLinks, boolean inheritTestcasesReqLinks) {
		Requirement req = requirementVersionDao.findRequirementById(requirementId);
		RequirementVersion previousVersion = req.getCurrentVersion();

		req.increaseVersion();
		em.persist(req.getCurrentVersion());
		RequirementVersion newVersion = copyAttachmentsForNewVersions(req);

		customFieldValueService.copyCustomFieldValues(previousVersion, newVersion);
		if (inheritReqLinks) {
			requirementLinkService.copyRequirementVersionLinks(previousVersion, newVersion);
		}
		if (inheritTestcasesReqLinks) {
			requirementLinkService.postponeTestCaseToNewRequirementVersion(previousVersion, newVersion);
		}
	}

	@Override
	@PreAuthorize(CREATE_REQUIREMENT_OR_ROLE_ADMIN)
	public void createNewVersion(long requirementId, Collection<Long> milestoneIds, boolean inheritReqLinks, boolean inheritTestcasesReqLinks) {

		createNewVersion(requirementId, inheritReqLinks, inheritTestcasesReqLinks);
		Requirement req = requirementVersionDao.findRequirementById(requirementId);

		for (RequirementVersion version : req.getRequirementVersions()) {
			for (Long mid : milestoneIds) {
				version.unbindMilestone(mid);
			}
		}

		milestoneManager.bindRequirementVersionToMilestones(req.getCurrentVersion().getId(), milestoneIds);

	}

	/**
	 * @see org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService#changeCriticality(long,
	 *      org.squashtest.tm.domain.requirement.RequirementCriticality)
	 */
	@Override
	@PreAuthorize(WRITE_REQVERSION_OR_ROLE_ADMIN)
	public void changeCriticality(long requirementVersionId, RequirementCriticality criticality) {
		RequirementVersion requirementVersion = requirementVersionDao.getOne(requirementVersionId);
		RequirementCriticality oldCriticality = requirementVersion.getCriticality();
		requirementVersion.setCriticality(criticality);
		testCaseImportanceManagerService.changeImportanceIfRequirementCriticalityChanged(requirementVersionId,
			oldCriticality);
	}

	@Override
	@PreAuthorize(WRITE_REQVERSION_OR_ROLE_ADMIN)
	@CheckLockedMilestone(entityType = RequirementVersion.class)
	public void rename(@Id long requirementVersionId, String newName) {
		RequirementVersion v = requirementVersionDao.getOne(requirementVersionId);

		/*
		 * FIXME : there is a loophole here. What exactly means DuplicateNameException for requirements, that can have
		 * multiple names (one for each version) ? What happens when the library is displayed in milestone mode and that
		 * two versions of different requirements happens to have the same name and same milestone (hint : they would be
		 * displayed both anyway).
		 *
		 * Because of this we are waiting for better specs on that matter, and the implementation here remains trivial
		 * in the mean time.
		 */

		// Requirement name can not be modified when its status is approved or obsolete.
		if (v.isModifiable()) {
			v.setName(newName.trim());
		} else {
			throw new IllegalRequirementModificationException();
		}

	}

	/**
	 * @see org.squashtest.tm.service.requirement.CustomRequirementVersionManagerService#findAllByRequirement(long,
	 *      org.springframework.data.domain.Pageable)
	 */
	@Override
	@PreAuthorize(READ_REQUIREMENT_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public Page<RequirementVersion> findAllByRequirement(long requirementId, Pageable pageable) {
		return requirementVersionDao.findAllByRequirementId(requirementId, pageable);
	}

	@Override
	@PreAuthorize(READ_REQUIREMENT_OR_ROLE_ADMIN)
	@Transactional(readOnly=true)
	public List<RequirementVersion> findAllByRequirement(long requirementId) {
		Pageable pageable = new PageRequest(0, Integer.MAX_VALUE, Sort.Direction.DESC, "versionNumber");
		return findAllByRequirement(requirementId, pageable).getContent();
	}

	@Override
	@PreAuthorize(WRITE_REQVERSION_OR_ROLE_ADMIN)
	public void changeCategory(long requirementVersionId, String categoryCode) {
		RequirementVersion version = requirementVersionDao.getOne(requirementVersionId);
		InfoListItem category = infoListItemService.findByCode(categoryCode);

		if (infoListItemService.isCategoryConsistent(version.getProject().getId(), categoryCode)) {
			version.setCategory(category);
		} else {
			throw new InconsistentInfoListItemException("requirementCategory", categoryCode);
		}
	}

	@Override
	public Collection<Long> bulkUpdate(List<Long> requirementVersionIds, RequirementBulkUpdate update) {

		List<Long> failures = new ArrayList<>();

		List<RequirementVersion> versions = requirementVersionDao.findAllById(requirementVersionIds);

		InfoListItem category = null;
		if (update.hasCategoryDefined()) {
			category = infoListItemService.findByCode(update.getCategory());
		}


		for (RequirementVersion rv : versions) {
			try {
				// security check
				SecurityCheckableObject check = new SecurityCheckableObject(rv, "WRITE");
				PermissionsUtils.checkPermission(permService, check);

				PropertiesSetter ps = rv.getPropertySetter();

				// update category if needed
				if (update.hasCategoryDefined()) {
					updateCategoryIfNeeded( rv, category, ps , update);
				}

				// update status if needed
				if (update.hasStatusDefined()) {
					ps.setStatus(update.getStatus());
				}

				// update criticality if needed
				if (update.hasCriticalityDefined()) {
					ps.setCriticality(update.getCriticality());
				}

			} catch (Exception ex) {//NOSONAR lots of legitimate business exception could happen so I won't log them here
				failures.add(rv.getId());
			}
		}

		return failures;

	}

	private void updateCategoryIfNeeded(RequirementVersion rv,InfoListItem category,PropertiesSetter ps ,RequirementBulkUpdate update){
		if (infoListItemService.isCategoryConsistent(rv.getProject().getId(), update.getCategory())) {
			ps.setCategory(category);
		} else {
			throw new InconsistentInfoListItemException("requirementCategory", update.getCategory());
		}
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'READ')"
		+ OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAllMilestones(long versionId) {
		return milestoneManager.findMilestonesForRequirementVersion(versionId);
	}

	@Override
	@Transactional(readOnly = true)
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'READ')"
		+ OR_HAS_ROLE_ADMIN)
	public Collection<Milestone> findAssociableMilestones(long versionId) {
		return milestoneManager.findAssociableMilestonesToRequirementVersion(versionId);
	}

	@Override
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')"
		+ OR_HAS_ROLE_ADMIN)
	public void bindMilestones(long versionId, Collection<Long> milestoneIds) {
		milestoneManager.bindRequirementVersionToMilestones(versionId, milestoneIds);
	}

	@Override
	@PreAuthorize("hasPermission(#versionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'WRITE')"
		+ OR_HAS_ROLE_ADMIN)
	public void unbindMilestones(long versionId, Collection<Long> milestoneIds) {
		milestoneManager.unbindRequirementVersionFromMilestones(versionId, milestoneIds);
	}

	@Override
	public Collection<Milestone> findAssociableMilestonesForMassModif(List<Long> reqVersionIds) {

		List<RequirementVersion> versions = requirementVersionDao.findAllById(reqVersionIds);

		// find all associable milestone sets
		List<Set<Milestone>> milestoneSetList =
			versions.stream()
				// first find the distinct projects
				.map ( v -> v.getProject() )
				.distinct()
				// now fetch all the milestones that can be modified and return them as set
				.map ( p -> this.retainModifiableMilestones(p.getMilestones()) )
				// return the list of sets
				.collect(Collectors.toList());

		// find the intersection.
		return intersect(milestoneSetList);

	}



	@Override
	public Collection<Long> findBindedMilestonesIdForMassModif(List<Long> reqVersionIds) {

		List<RequirementVersion> versions = requirementVersionDao.findAllById(reqVersionIds);

		// find all associable milestone sets
		List<Set<Milestone>> milestoneSetList =
			versions.stream()
				// now fetch the set of those milestones (only those that can be modified though)
				.map ( version -> this.retainModifiableMilestones(version.getMilestones()) )
				// return the list of sets
				.collect(Collectors.toList());

		// find the intersection
		Set<Milestone> milestones = intersect(milestoneSetList);

		// return the ids
		return CollectionUtils.collect(milestones, new IdCollector());
	}

	private Set<Milestone> retainModifiableMilestones(Collection<Milestone> milestones){
		return milestones.stream().filter( this::isModifiableMilestone ).collect(Collectors.toSet());
	}

	// a milestone is mass modifiable if its status is neither locked not planned
	private boolean isModifiableMilestone(Milestone milestone){
		MilestoneStatus status = milestone.getStatus();
		return ! (status == MilestoneStatus.LOCKED || status == MilestoneStatus.PLANNED);
	}

	private Set<Milestone> intersect(List<Set<Milestone>> milestoneSetList){

		if (milestoneSetList.isEmpty()){
			return new HashSet<>();
		}

		Set<Milestone> result = new HashSet<>(milestoneSetList.get(0));

		return milestoneSetList.stream().skip(1)
					.collect( () -> result,
								Collection::retainAll,
								CollectionUtils::retainAll
					);

	}




	@Override
	public boolean haveSamePerimeter(List<Long> reqVersionIds) {

		boolean allMatch = true;

		if (reqVersionIds.size() != 1) {
			// find the milestone sets
			List<Set<Milestone>> milestoneSetList =
				requirementVersionDao.findAllById(reqVersionIds).stream()
					// first find the distinct projects
					.map ( v -> v.getProject() )
					.distinct()
					// now gather the milestone sets
					.map ( p -> new HashSet<>(p.getMilestones()) )
					// return the list of sets
					.collect(Collectors.toList());

			// verify they have the same content
			Set<Milestone> sample = milestoneSetList.get(0);
			allMatch = milestoneSetList.stream().allMatch( set -> set.size() == sample.size() && set.containsAll(sample));

		}

		return allMatch;
	}

	@Override
	public boolean isOneMilestoneAlreadyBindToAnotherRequirementVersion(List<Long> reqVIds, List<Long> milestoneIds) {
		return milestoneDao.isOneMilestoneAlreadyBindToAnotherRequirementVersion(reqVIds, milestoneIds);

	}

	@Override
	@Transactional(readOnly=true)
	public Long findReqVersionIdByRequirementAndVersionNumber(
		long requirementId, Integer versionNumber) {
		RequirementVersion requirementVersion = requirementVersionDao.findByRequirementIdAndVersionNumber(requirementId, versionNumber);
		if (requirementVersion != null) {
			return requirementVersion.getId();
		}
		return null;
	}


	@Override
	@PreAuthorize(READ_REQVERSION_OR_ROLE_ADMIN)
	public RequirementVersion findByRequirementIdAndVersionNumber(long requirementVersionId, int versionNumber) {
		return requirementVersionDao.findByRequirementIdAndVersionNumber(requirementVersionId, versionNumber);
	}

	//As Squash 1.18, for file repositories we need to copy the attachment by service call.
	//Model is not able to handle file manipulation by itself...
	private RequirementVersion copyAttachmentsForNewVersions(Requirement req) {
		RequirementVersion newVersion = req.getCurrentVersion();
		for (Attachment attachment : newVersion.getAttachmentList().getAllAttachments()) {
			attachmentManagerService.copyContent(attachment);
		}
		return newVersion;
	}

}
