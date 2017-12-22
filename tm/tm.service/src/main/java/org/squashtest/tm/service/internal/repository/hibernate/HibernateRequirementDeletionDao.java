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
package org.squashtest.tm.service.internal.repository.hibernate;

import org.hibernate.Query;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;
import org.squashtest.tm.domain.event.RequirementAuditEvent;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.requirement.*;
import org.squashtest.tm.service.internal.repository.ParameterNames;
import org.squashtest.tm.service.internal.repository.RequirementDeletionDao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class HibernateRequirementDeletionDao extends HibernateDeletionDao implements RequirementDeletionDao {

	private static final String REQUIREMENT_IDS = "requirementIds";
	private static final String VERSION_IDS = "versionIds";
	private static final String FOLDER_IDS = "folderIds";


	/*
	 * This method remove requirement versions. It assumes that no conflict will occur with Requirement#currentVersion
	 */
	@Override
	public void deleteVersions(List<Long> versionIds) {
		executeDeleteNamedQuery("requirementDeletionDao.deleteVersions", VERSION_IDS, versionIds);
	}


	// note 1 : this method will be ran twice per batch : one for folder deletion, one for requirement deletion
	// ( it is so because two distincts calls to #deleteNodes, see RequirementDeletionHandlerImpl#deleteNodes() )
	// It should run fine tho, at the cost of a few useless extra queries.

	// note 2 : the code below must handle the references of requirements and requirement folders to
	// their Resource and SimpleResource, making the thing a lot more funny and pleasant to maintain.
	@Override
	public void removeEntities(List<Long> entityIds) {
		if (!entityIds.isEmpty()) {

			for (Long entityId : entityIds) {
				RequirementLibraryNode node = entityManager().getReference(RequirementLibraryNode.class, entityId);

				removeEntitiesFromParentLibraryIfExists(entityId, node);

				removeEntitiesFromParentFolderIfExists(entityId, node);

				removeEntitiesFromParentRequirementIfExists(entityId, node);

				if (node != null) {
					entityManager().remove(node);
					entityManager().flush();
				}
			}
		}
	}


	private void removeEntitiesFromParentLibraryIfExists(Long entityId, RequirementLibraryNode node) {
		Query query = getSession().getNamedQuery("requirementLibraryNode.findParentLibraryIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		RequirementLibrary library = (RequirementLibrary) query.uniqueResult();
		if (library != null) {
			for (RequirementLibraryNode tcln : library.getContent()) {
				if (tcln.getId().equals(node.getId())) {
					library.removeContent(tcln);
					break;
				}
			}
		}
	}

	private void removeEntitiesFromParentFolderIfExists(Long entityId, RequirementLibraryNode node) {
		Query query = getSession().getNamedQuery("requirementLibraryNode.findParentFolderIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		RequirementFolder folder = (RequirementFolder) query.uniqueResult();
		if (folder != null) {
			for (RequirementLibraryNode tcln : folder.getContent()) {
				if (tcln.getId().equals(node.getId())) {
					folder.removeContent(tcln);
					break;
				}
			}
		}
	}

	private void removeEntitiesFromParentRequirementIfExists(Long entityId, RequirementLibraryNode node) {
		Query query = getSession().getNamedQuery("requirementLibraryNode.findParentRequirementIfExists");
		query.setParameter(ParameterNames.LIBRARY_NODE_ID, entityId);
		Requirement requirement = (Requirement) query.uniqueResult();
		if (requirement != null) {
			for (Requirement tcln : requirement.getContent()) {
				if (tcln.getId().equals(node.getId())) {
					requirement.removeContent(tcln);
					break;
				}
			}
		}
	}

	@Override
	public List<Long>[] separateFolderFromRequirementIds(List<Long> originalIds) {

		List<Long> folderIds = new ArrayList<>(0);
		List<Long> requirementIds = new ArrayList<>(0);

		List<BigInteger> filtredFolderIds = executeSelectSQLQuery(
			NativeQueries.REQUIREMENTLIBRARYNODE_SQL_FILTERFOLDERIDS, REQUIREMENT_IDS, originalIds);

		for (Long oId : originalIds) {
			if (filtredFolderIds.contains(BigInteger.valueOf(oId))) {
				folderIds.add(oId);
			} else {
				requirementIds.add(oId);
			}
		}

		return new List[]{folderIds, requirementIds};
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findRequirementAttachmentListIds(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			Query query = getSession().getNamedQuery("requirement.findAllAttachmentLists");
			query.setParameterList(REQUIREMENT_IDS, requirementIds);
			return query.list();
		}
		return new ArrayList<>(0);
	}

	@Override
	public List<Long> findRequirementVersionAttachmentListIds(List<Long> versionIds) {
		if (!versionIds.isEmpty()) {
			Query query = getSession().getNamedQuery("requirementVersion.findAllAttachmentLists");
			query.setParameterList(VERSION_IDS, versionIds);
			return query.list();
		}
		return new ArrayList<>(0);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Long> findRequirementFolderAttachmentListIds(
		List<Long> folderIds) {
		if (!folderIds.isEmpty()) {
			Query query = getSession().getNamedQuery("requirementFolder.findAllAttachmentLists");
			query.setParameterList(FOLDER_IDS, folderIds);
			return query.list();
		}
		return Collections.emptyList();
	}


	@Override
	public void removeFromVerifiedVersionsLists(List<Long> versionIds) {
		if (!versionIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVEFROMVERIFIEDVERSIONSLISTS, VERSION_IDS,
				versionIds);
		}

	}

	@Override
	public void removeFromLinkedVersionsLists(List<Long> versionIds) {
		if (!versionIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVEFROMLINKEDVERSIONSLISTS, VERSION_IDS,
				versionIds);
		}
	}

	@Override
	public void removeFromVerifiedRequirementLists(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVEFROMVERIFIEDREQUIREMENTLISTS, REQUIREMENT_IDS,
				requirementIds);
		}

	}

	@Override
	public void removeTestStepsCoverageByRequirementVersionIds(List<Long> requirementVersionIds) {
		if (!requirementVersionIds.isEmpty()) {
			executeDeleteSQLQuery(NativeQueries.REQUIREMENT_SQL_REMOVE_TEST_STEP_COVERAGE_BY_REQ_VERSION_IDS, VERSION_IDS,
				requirementVersionIds);
		}

	}

	@Override
	public void deleteRequirementAuditEvents(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			// we borrow the following from RequirementAuditDao
			List<RequirementAuditEvent> events = executeSelectNamedQuery(
				"requirementAuditEvent.findAllByRequirementIds", "ids", requirementIds);

			// because Hibernate sucks so much at polymorphic bulk delete, we're going to remove
			// them one by one.
			for (RequirementAuditEvent event : events) {
				removeEntity(event);
			}

			flush();
		}

	}

	@Override
	public void deleteRequirementVersionAuditEvents(List<Long> versionIds) {
		if (!versionIds.isEmpty()) {
			// we borrow the following from RequirementAuditDao
			List<RequirementAuditEvent> events = executeSelectNamedQuery(
				"requirementAuditEvent.findAllByRequirementVersionIds", "ids", versionIds);

			// because Hibernate sucks so much at polymorphic bulk delete, we're going to remove
			// them one by one.
			for (RequirementAuditEvent event : events) {
				removeEntity(event);
			}

			flush();
		}
	}

	@Override
	public List<Long> findVersionIds(List<Long> requirementIds) {
		return executeSelectNamedQuery("requirementDeletionDao.findVersionIds", "reqIds", requirementIds);
	}

	@Override
	public List<Long> findRemainingRequirementIds(List<Long> originalIds) {
		List<BigInteger> rawids = executeSelectSQLQuery(NativeQueries.REQUIREMENT_SQL_FINDNOTDELETED, "allRequirementIds", originalIds);
		List<Long> cIds = new ArrayList<>(rawids.size());
		for (BigInteger rid : rawids) {
			cIds.add(rid.longValue());
		}
		return cIds;
	}




	/* *************************************************************
	 *  			Methods for the milestone mode
	 ************************************************************ */

	/**
	 * See javadoc on the interface
	 *
	 */
	@Override
	public List<Long> findDeletableVersions(List<Long> requirementIds, Long milestoneId) {

		List<Long> deletableVersions = new ArrayList<>(0);

		// 1 - must belong to milestone
		List<Long> versionsBelongingToMilestone = findVersionIdsForMilestone(requirementIds, milestoneId);
		deletableVersions.addAll(versionsBelongingToMilestone);

		// 2 - must not belong to many milestones
		List<Long> hasManyMilestones = filterVersionIdsHavingMultipleMilestones(deletableVersions);
		deletableVersions.removeAll(hasManyMilestones);

		// 3 - must not be locked
		List<Long> lockedVersions = filterVersionIdsWhichMilestonesForbidsDeletion(deletableVersions);
		deletableVersions.removeAll(lockedVersions);

		return deletableVersions;
	}

	;


	/**
	 * See javadoc on the interface
	 *
	 */
	@Override
	public List<Long> findUnbindableVersions(List<Long> requirementIds, Long milestoneId) {

		List<Long> unbindableVersions = new ArrayList<>(0);

		// 1 - must belong to the milestone
		List<Long> versionsBelongingToMilestone = findVersionIdsForMilestone(requirementIds, milestoneId);
		unbindableVersions.addAll(versionsBelongingToMilestone);

		// 2 - must belong to many milestones
		versionsBelongingToMilestone = filterVersionIdsHavingMultipleMilestones(unbindableVersions);

		// 3 - must not be locked
		List<Long> lockedVersions = filterVersionIdsWhichMilestonesForbidsDeletion(unbindableVersions);
		versionsBelongingToMilestone.removeAll(lockedVersions);

		return versionsBelongingToMilestone;
	}


	@Override
	public List<Long> filterRequirementsHavingDeletableVersions(List<Long> requirementIds, Long milestoneId) {
		List<Long> deletableVersions = findDeletableVersions(requirementIds, milestoneId);
		return findByRequirementVersion(deletableVersions);
	}

	@Override
	public List<Long> filterRequirementsHavingUnbindableVersions(List<Long> requirementIds, Long milestoneId) {
		List<Long> deletableVersions = findUnbindableVersions(requirementIds, milestoneId);
		return findByRequirementVersion(deletableVersions);
	}


	@Override
	public List<Long> filterRequirementsIdsWhichMilestonesForbidsDeletion(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			MilestoneStatus[] lockedStatuses = new MilestoneStatus[]{MilestoneStatus.PLANNED, MilestoneStatus.LOCKED};
			Query query = getSession().getNamedQuery("requirementDeletionDao.findRequirementsWhichMilestonesForbidsDeletion");
			query.setParameterList(REQUIREMENT_IDS, requirementIds, LongType.INSTANCE);
			query.setParameterList("lockedStatuses", lockedStatuses);
			return query.list();
		} else {
			return new ArrayList<>(0);
		}
	}


	@Override
	public List<Long> filterVersionIdsWhichMilestonesForbidsDeletion(List<Long> versionIds) {
		if (!versionIds.isEmpty()) {
			MilestoneStatus[] lockedStatuses = new MilestoneStatus[]{MilestoneStatus.PLANNED, MilestoneStatus.LOCKED};
			Query query = getSession().getNamedQuery("requirementDeletionDao.findVersionsWhichMilestonesForbidsDeletion");
			query.setParameterList(VERSION_IDS, versionIds, LongType.INSTANCE);
			query.setParameterList("lockedStatuses", lockedStatuses);
			return query.list();
		} else {
			return new ArrayList<>(0);
		}
	}


	@Override
	public List<Long> filterVersionIdsHavingMultipleMilestones(List<Long> versionIds) {
		if (!versionIds.isEmpty()) {
			Query q = getSession().getNamedQuery("requirementDeletionDao.findVersionIdsHavingMultipleMilestones");
			q.setParameterList(VERSION_IDS, versionIds, LongType.INSTANCE);
			return q.list();
		} else {
			return new ArrayList<>(0);
		}
	}


	@Override
	public List<Long> findVersionIdsForMilestone(List<Long> requirementIds, Long milestoneId) {
		if (!requirementIds.isEmpty()) {
			Query query = getSession().getNamedQuery("requirementDeletionDao.findAllVersionForMilestone");
			query.setParameterList("nodeIds", requirementIds, LongType.INSTANCE);
			query.setParameter("milestoneId", milestoneId);
			return query.list();
		} else {
			return new ArrayList<>(0);
		}
	}

	@Override
	public void unbindFromMilestone(List<Long> requirementIds, Long milestoneId) {
		if (!requirementIds.isEmpty()) {
			Query query = getSession().createSQLQuery(NativeQueries.REQUIREMENT_SQL_UNBIND_MILESTONE);
			query.setParameterList(REQUIREMENT_IDS, requirementIds, LongType.INSTANCE);
			query.setParameter("milestoneId", milestoneId);
			query.executeUpdate();
		}

	}


	@Override
	public void unsetRequirementCurrentVersion(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			Query q = getSession().getNamedQuery("requirement.findAllById");
			q.setParameterList(REQUIREMENT_IDS, requirementIds);

			List<Requirement> requirements = q.list();

			for (Requirement r : requirements) {
				r.setCurrentVersion(null);
			}
		}
	}


	@Override
	public void resetRequirementCurrentVersion(List<Long> requirementIds) {
		if (!requirementIds.isEmpty()) {
			Query q = getSession().getNamedQuery("requirement.findAllRequirementsWithLatestVersionByIds");
			q.setParameterList(REQUIREMENT_IDS, requirementIds);

			List<Object[]> tuples = q.list();

			for (Object[] tuple : tuples) {
				RequirementVersion latest = (RequirementVersion) tuple[1];
				((Requirement) tuple[0]).setCurrentVersion(latest);
			}
		}
	}

	private List<Long> findByRequirementVersion(List<Long> versionIds) {
		if (!versionIds.isEmpty()) {
			Query q = getSession().getNamedQuery("requirement.findByRequirementVersion");
			q.setParameterList(VERSION_IDS, versionIds, LongType.INSTANCE);
			return q.list();
		} else {
			return new ArrayList<>(0);
		}
	}

}
