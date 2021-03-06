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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.LinkedRequirementVersion;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.domain.requirement.RequirementVersionLinkType;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.exception.requirement.link.AlreadyLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.LinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.SameRequirementLinkedRequirementVersionException;
import org.squashtest.tm.exception.requirement.link.UnlinkableLinkedRequirementVersionException;
import org.squashtest.tm.service.audit.AuditModificationService;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionLinkTypeDao;
import org.squashtest.tm.service.milestone.ActiveMilestoneHolder;
import org.squashtest.tm.service.requirement.LinkedRequirementVersionManagerService;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.testcase.VerifyingTestCaseManagerService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.squashtest.tm.service.security.Authorizations.LINK_REQVERSION_OR_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.OR_HAS_ROLE_ADMIN;
import static org.squashtest.tm.service.security.Authorizations.READ_REQVERSION_OR_ROLE_ADMIN;

@Service("squashtest.tm.service.LinkedRequirementVersionManagerService")
@Transactional
public class LinkedRequirementVersionManagerServiceImpl implements LinkedRequirementVersionManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkedRequirementVersionManagerService.class);

	@Inject
	private RequirementVersionDao reqVersionDao;
	@Inject
	private RequirementVersionLinkDao reqVersionLinkDao;
	@Inject
	private RequirementVersionLinkTypeDao reqVersionLinkTypeDao;
	@Inject
	private ActiveMilestoneHolder activeMilestoneHolder;
	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;
	@Inject
	private VerifyingTestCaseManagerService verifyingTestCaseManagerService;
	@Inject
	private VerifiedRequirementsManagerService verifiedRequirementsManagerService;
	@Inject
	private AuditModificationService auditModificationService;

	@Override
	@PreAuthorize(READ_REQVERSION_OR_ROLE_ADMIN)
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<LinkedRequirementVersion>>
	findAllByRequirementVersion(long requirementVersionId, PagingAndSorting pagingAndSorting) {

		// Issue 7497 we need to get the total number of requirement version links to correctly display the number of page
		long totalNumberOfItems = reqVersionDao.getOne(requirementVersionId).getRequirementVersionLinks().size();

		List<RequirementVersionLink> requirementVersionLinksList =
			reqVersionLinkDao.findAllByReqVersionId(requirementVersionId, pagingAndSorting);

		List<LinkedRequirementVersion> linkedReqVersionsList =
			new ArrayList<>();

		for (RequirementVersionLink reqVerLink : requirementVersionLinksList) {
			linkedReqVersionsList.add(
				reqVerLink.getRelatedLinkedRequirementVersion());
		}

		return new PagingBackedPagedCollectionHolder<>(pagingAndSorting, totalNumberOfItems, linkedReqVersionsList);
	}

	@Override
	@PreAuthorize(LINK_REQVERSION_OR_ROLE_ADMIN)
	public void removeLinkedRequirementVersionsFromRequirementVersion(
		long requirementVersionId, List<Long> requirementVersionIdsToUnlink) {

		List<Long> impactedVersionIds = new ArrayList<>(requirementVersionIdsToUnlink);
		impactedVersionIds.add(requirementVersionId);
		List<RequirementVersion> impactedVersions = reqVersionDao.findAllById(impactedVersionIds);
		auditModificationService.updateRelatedToRequirementLinkAuditableEntity(impactedVersions);

		reqVersionLinkDao.deleteAllLinks(requirementVersionId, requirementVersionIdsToUnlink);
	}

	@Override
	public Collection<LinkedRequirementVersionException> addLinkedReqVersionsToReqVersion(
		Long mainReqVersionId, List<Long> otherReqVersionsIds) {

		List<RequirementVersion> requirementVersions = findRequirementVersions(otherReqVersionsIds);
		List<LinkedRequirementVersionException> rejections = new ArrayList<>();

		RequirementVersion mainReqVersion = reqVersionDao.getOne(mainReqVersionId);
		for (RequirementVersion otherRequirementVersion : requirementVersions) {

			try {
				checkIfLinkAlreadyExists(mainReqVersion, otherRequirementVersion);
				checkIfSameRequirement(mainReqVersion, otherRequirementVersion);
				checkIfVersionsAreLinkable(mainReqVersion, otherRequirementVersion);

			} catch (LinkedRequirementVersionException exception) {
				rejections.add(exception);
			}
		}
		if(rejections.isEmpty()){
			for (RequirementVersion otherRequirementVersion : requirementVersions) {
				try {
					/* No exception -> Adding */
					RequirementVersionLink newReqVerLink =
						new RequirementVersionLink(
							mainReqVersion,
							otherRequirementVersion,
							reqVersionLinkTypeDao.getDefaultRequirementVersionLinkType(),
							false);
					reqVersionLinkDao.addLink(newReqVerLink);
				} catch (LinkedRequirementVersionException exception) {
					rejections.add(exception);
				}
			}
		}
		return rejections;
	}

	/*TODO: Change Javascript to get reqVerionId and not Node. */
	@Override
	@PreAuthorize("hasPermission(#reqVersionNodeId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'LINK')" +
		OR_HAS_ROLE_ADMIN)
	public Collection<LinkedRequirementVersionException> addDefaultLinkWithNodeIds(Long reqVersionNodeId, Long relatedReqVersionNodeId) {
		List<Long> reqVerNodeIds = new ArrayList<>(1);
		reqVerNodeIds.add(reqVersionNodeId);

		List<Long> relatedReqVerNodeIds = new ArrayList<>(1);
		relatedReqVerNodeIds.add(relatedReqVersionNodeId);

		List<RequirementVersion> requirementVersions = findRequirementVersions(reqVerNodeIds);
		return addLinkedReqVersionsToReqVersion(requirementVersions.get(0).getId(), relatedReqVerNodeIds);
	}


	@Override
	@PreAuthorize("hasPermission(#sourceVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion', 'LINK')" +
		OR_HAS_ROLE_ADMIN)
	public void addOrUpdateRequirementLink(Long sourceVersionId, Long destVersionId, String destRole) {

		// compute the type and direction
		// the new direction for the outboundLink is deduced from the role of the destination version
		// if the code designate the role 2, then the outboundLink direction should be false (meaning : outbound)
		RequirementVersionLinkType type = reqVersionLinkTypeDao.findByRoleCode(destRole);
		boolean outboundDirection = type.getRole2Code().equals(destRole) ? false : true;

		// the requirement versions
		RequirementVersion source = reqVersionDao.getOne(sourceVersionId);
		RequirementVersion dest = reqVersionDao.getOne(destVersionId);

		// checks
		checkIfSameRequirement(source, dest);
		checkIfVersionsAreLinkable(source, dest);

		// see if one need to create or just update the links
		RequirementVersionLink outboundLink = reqVersionLinkDao.findByReqVersionsIds(sourceVersionId, destVersionId);

		// if null, we need to create them
		if (outboundLink == null) {
			outboundLink = new RequirementVersionLink(source, dest, type, outboundDirection);
			reqVersionLinkDao.addLink(outboundLink);
		}
		// else we just update them
		else {
			RequirementVersionLink inboundLink = reqVersionLinkDao.findByReqVersionsIds(destVersionId, sourceVersionId);

			outboundLink.setLinkType(type);
			outboundLink.setLinkDirection(outboundDirection);

			inboundLink.setLinkType(type);
			inboundLink.setLinkDirection(!outboundDirection);
		}

	}


	@Override
	@PreAuthorize(LINK_REQVERSION_OR_ROLE_ADMIN)
	public void updateLinkTypeAndDirection(
		long requirementVersionId, long relatedReqNodeId, boolean isRelatedIdANodeId,
		long linkTypeId, boolean linkDirection) {

		long relatedVersionId = relatedReqNodeId;

		if (isRelatedIdANodeId) {
			List<Long> reqVerNodeIds = new ArrayList<>();
			reqVerNodeIds.add(relatedReqNodeId);
			List<RequirementVersion> list = findRequirementVersions(reqVerNodeIds);
			RequirementVersion relatedReqVersion = list.get(0);
			relatedVersionId = relatedReqVersion.getId();
		}

		RequirementVersionLink linkToUpdate = reqVersionLinkDao.findByReqVersionsIds(requirementVersionId, relatedVersionId);
		RequirementVersionLink symmetricalLinkToUpdate = reqVersionLinkDao.findByReqVersionsIds(relatedVersionId, requirementVersionId);

		List<RequirementVersion> requirementVersions = reqVersionDao.findAllById(Arrays.asList(requirementVersionId, relatedVersionId));

		RequirementVersionLinkType newLinkType = reqVersionLinkTypeDao.getOne(linkTypeId);

		linkToUpdate.setLinkType(newLinkType);
		linkToUpdate.setLinkDirection(linkDirection);

		symmetricalLinkToUpdate.setLinkType(newLinkType);
		symmetricalLinkToUpdate.setLinkDirection(!linkDirection);

		auditModificationService.updateRelatedToRequirementLinkAuditableEntity(requirementVersions);
	}

	@PreAuthorize(LINK_REQVERSION_OR_ROLE_ADMIN)
	@Override
	public Collection<LinkedRequirementVersionException> addLinkWithNodeIds(Long requirementVersionId, Long relatedReqVersionNodeId, long reqVersionLinkTypeId, boolean reqVersionLinkTypeDirection) {

		List<Long> requirementVersions = Collections.singletonList(relatedReqVersionNodeId);
		RequirementVersionLinkType type = reqVersionLinkTypeDao.getOne(reqVersionLinkTypeId);
		boolean outboundDirection = reqVersionLinkTypeDirection;
		return addLinkedReqVersionsToReqVersion(requirementVersionId, requirementVersions, type, outboundDirection);
	}

	@Override
	public void copyRequirementVersionLinks(RequirementVersion previousVersion, RequirementVersion newVersion) {
		for (RequirementVersionLink reqVerLink : previousVersion.getRequirementVersionLinks()) {
			try {
				addDetailedReqVersionLink(
					newVersion.getId(),
					reqVerLink.getRelatedLinkedRequirementVersion().getId(),
					reqVerLink.getLinkType().getId(),
					reqVerLink.getLinkDirection());
			} catch (LinkedRequirementVersionException exception) {
				LOGGER.info("RequirementVersion " + previousVersion.getName() +
						" could not be linked to RequirementVersion " + newVersion.getName(),
					exception);
			}
		}
	}

	@Override
	public RequirementVersionLink addDetailedReqVersionLink(long reqVersionId, long relatedReqVersionId, long linkTypeId, boolean linkDirection)
		throws LinkedRequirementVersionException {

		RequirementVersion reqVersion = reqVersionDao.getOne(reqVersionId);
		RequirementVersion relatedReqVersion = reqVersionDao.getOne(relatedReqVersionId);

		checkIfLinkAlreadyExists(reqVersion, relatedReqVersion);
		checkIfSameRequirement(reqVersion, relatedReqVersion);
		checkIfVersionsAreLinkable(reqVersion, relatedReqVersion);

		RequirementVersionLinkType linkType = reqVersionLinkTypeDao.getOne(linkTypeId);
		RequirementVersionLink newLink = new RequirementVersionLink(reqVersion, relatedReqVersion, linkType, linkDirection);
		reqVersionLinkDao.addLink(newLink);
		return newLink;
	}

	@Override
	public List<RequirementVersionLinkType> getAllReqVersionLinkTypes() {
		return reqVersionLinkTypeDao.getAllRequirementVersionLinkTypes();
	}

	@Override
	public Set<String> findAllRoleCodes() {
		List<RequirementVersionLinkType> allTypes = getAllReqVersionLinkTypes();

		Set<String> codes = new HashSet<>();

		for (RequirementVersionLinkType type : allTypes) {
			codes.add(type.getRole1Code());
			codes.add(type.getRole2Code());
		}

		return codes;
	}
	public List<RequirementVersionLinkType> findAllRequirementVersionLinkType() {
					return getAllReqVersionLinkTypes();
	}
	@Override
	public void postponeTestCaseToNewRequirementVersion(RequirementVersion previousVersion, RequirementVersion newVersion) {
		for (TestCase testCaseToPostpone : verifyingTestCaseManagerService.findAllByRequirementVersion(previousVersion.getId())) {
			try {
				verifiedRequirementsManagerService.changeVerifiedRequirementVersionOnTestCase(previousVersion.getId(), newVersion.getId(), testCaseToPostpone.getId());
			} catch (VerifiedRequirementException exception) {
				LOGGER.info("Could not change VerifiedRequirementVersion of VerifyingTestCase " + testCaseToPostpone.getName(),
					exception);
			}
		}

	}


	@Override
	public PagedCollectionHolder<List<RequirementVersionLinkType>> getAllPagedAndSortedReqVersionLinkTypes(PagingAndSorting pagingAndSorting) {
		List<RequirementVersionLinkType> reqLinkTypesList = reqVersionLinkTypeDao.getAllPagedAndSortedReqVersionLinkTypes(pagingAndSorting);
		return new PagingBackedPagedCollectionHolder<>(pagingAndSorting, reqLinkTypesList.size(), reqLinkTypesList);
	}

	@Override
	public void checkIfLinkAlreadyExists(RequirementVersion reqVersion, RequirementVersion relatedReqVersion) {
		if (reqVersionLinkDao.linkAlreadyExists(reqVersion.getId(), relatedReqVersion.getId())) {
			throw new AlreadyLinkedRequirementVersionException();
		}
	}

	@Override
	public void checkIfSameRequirement(RequirementVersion reqVersion, RequirementVersion relatedReqVersion) {
		if (reqVersion.getRequirement().getId().equals(relatedReqVersion.getRequirement().getId())) {
			throw new SameRequirementLinkedRequirementVersionException();
		}
	}

	@Override
	public void checkIfVersionsAreLinkable(RequirementVersion reqVersion, RequirementVersion relatedReqVersion) {
		if (!reqVersion.isLinkable() || !relatedReqVersion.isLinkable()) {
			throw new UnlinkableLinkedRequirementVersionException();
		}
	}


	@Override
	public Map<String, String> getRequirementVersionInformation(List<Long> requirementVersionids, boolean isRelatedIdANodeId) {

		List<RequirementVersion> requirementVersions;

		if(isRelatedIdANodeId == true){
		requirementVersions = findRequirementVersions(requirementVersionids);
		}
		else {
		requirementVersions = reqVersionDao.findAllById(requirementVersionids);
		}
		String name = "";
		Map<String, String> versionInfosMap = new HashMap<>();
		for (RequirementVersion relatedId : requirementVersions) {
				if (name.equals("")) {
					name = name + HtmlUtils.htmlEscape(relatedId.getName());
				} else {
					name = name + ", " + HtmlUtils.htmlEscape(relatedId.getName());
				}
		}
		versionInfosMap.put("versionName", name);
		return versionInfosMap;
	}

	private Collection<LinkedRequirementVersionException> addLinkedReqVersionsToReqVersion
		(Long mainReqVersionId, List<Long> otherReqVersionsIds, RequirementVersionLinkType requirementVersionLinkType, boolean linkDirection) {

		List<RequirementVersion> requirementVersions = findRequirementVersions(otherReqVersionsIds);
		List<LinkedRequirementVersionException> rejections = new ArrayList<>();
		RequirementVersion mainReqVersion = reqVersionDao.getOne(mainReqVersionId);
		List<Long> reqVersionsIds = new ArrayList<>();
		for (RequirementVersion otherRequirementVersion : requirementVersions) {

			try {
				checkIfLinkAlreadyExists(mainReqVersion, otherRequirementVersion);
				checkIfSameRequirement(mainReqVersion, otherRequirementVersion);
				checkIfVersionsAreLinkable(mainReqVersion, otherRequirementVersion);

			} catch (LinkedRequirementVersionException exception) {
				rejections.add(exception);
			}
		}

		if(rejections.isEmpty()){
			for (RequirementVersion otherRequirementVersion : requirementVersions) {
				try {
					/* No exception -> Adding */
					RequirementVersionLink newReqVerLink =
						new RequirementVersionLink(
							mainReqVersion,
							otherRequirementVersion,
							requirementVersionLinkType,
							linkDirection);
					reqVersionLinkDao.addLink(newReqVerLink);
					reqVersionsIds.add(otherRequirementVersion.getId());
				} catch (LinkedRequirementVersionException exception) {
					rejections.add(exception);
				}
			}
		}
		return rejections;
	}

	private List<RequirementVersion> findRequirementVersions(
		List<Long> requirementNodesIds) {

		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao
			.findAllByIds(requirementNodesIds);

		if (!nodes.isEmpty()) {
			List<Requirement> requirements = new RequirementNodeWalker()
				.walk(nodes);
			if (!requirements.isEmpty()) {
				return extractVersions(requirements);
			}
		}
		return Collections.emptyList();
	}

	private List<RequirementVersion> extractVersions(List<Requirement> requirements) {

		List<RequirementVersion> rvs = new ArrayList<>(requirements.size());

		Optional<Milestone> activeMilestone = activeMilestoneHolder.getActiveMilestone();

		for (Requirement requirement : requirements) {

			// normal mode
			if (!activeMilestone.isPresent()) {
				rvs.add(requirement.getResource());
			}
			// milestone mode
			else {
				rvs.add(requirement.findByMilestone(activeMilestone.get()));
			}
		}
		return rvs;
	}

}
