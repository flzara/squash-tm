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

import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.*;
import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.synchronisation.SynchronisationStatus;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.jooq.domain.tables.Requirement;
import org.squashtest.tm.jooq.domain.tables.RequirementFolder;
import org.squashtest.tm.jooq.domain.tables.RequirementLibraryNode;
import org.squashtest.tm.jooq.domain.tables.RequirementVersion;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateEntityDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementFolderDao;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.count;
import static org.squashtest.tm.domain.requirement.ManagementMode.SYNCHRONIZED;
import static org.squashtest.tm.domain.requirement.RequirementFolderSyncExtenderType.TARGET;
import static org.squashtest.tm.domain.synchronisation.SynchronisationStatus.FAILURE;
import static org.squashtest.tm.domain.synchronisation.SynchronisationStatus.SUCCESS;
import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("requirementWorkspaceDisplayService")
@Transactional(readOnly = true)
public class RequirementWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	@Inject
	HibernateRequirementFolderDao hibernateRequirementFolderDao;

	private RequirementLibraryNode RLN = REQUIREMENT_LIBRARY_NODE.as("RLN");
	private RequirementFolder RF = REQUIREMENT_FOLDER.as("RF");
	private Requirement REQ = REQUIREMENT.as("REQ");
	private RequirementVersion RV = REQUIREMENT_VERSION.as("RV");
	private RlnRelationship RLNR = RLN_RELATIONSHIP.as("RLNR");
	private RlnRelationshipClosure RLNRC = RLN_RELATIONSHIP_CLOSURE.as("RLNRC");
	private Resource RES = RESOURCE.as("RES");
	private InfoListItem ILI = INFO_LIST_ITEM.as("ILI");
	private MilestoneReqVersion MRV = MILESTONE_REQ_VERSION.as("MRV");

	private List<Long> reqsDontAllowClick = new ArrayList<>();

	@Override
	protected Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, MultiMap expansionCandidates, UserDto currentUser, Map<Long, List<Long>> allMilestonesForReqs, List<Long> milestonesModifiable, Long activeMilestoneId) {

		Map<Long, JsTreeNode> result;

		//get the repartition of node ie the type of each node
		RequirementLibraryNodeDistribution nodeDistribution = getRepartition(childrenIds);

		if (NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId)) {
			result = fetchAndBuildRequirementJsTreeNode(currentUser, allMilestonesForReqs, milestonesModifiable, activeMilestoneId, nodeDistribution.getReqIds());
		} else {
			reqsDontAllowClick = findReqsWithChildrenLinkedToActiveMilestone(activeMilestoneId);
			// remove nodes we don't want to build from reqDontAllowClick
			reqsDontAllowClick.retainAll(nodeDistribution.getReqIds());
			// build js nodes for requirements not linked to a milestone (i.e. the last version), concerns all the nodes in reqsDontAllowClick
			result = fetchAndBuildRequirementJsTreeNode(currentUser, allMilestonesForReqs, milestonesModifiable, activeMilestoneId, reqsDontAllowClick);
			// no need to build these nodes again
			nodeDistribution.getReqIds().removeAll(reqsDontAllowClick);
			// build js nodes for requirements linked to a milestone (i.e. the version linked to the milestone)
			result.putAll(buildRequirementJsTreeNodeLinkedToMilestone(currentUser, allMilestonesForReqs, milestonesModifiable, activeMilestoneId, nodeDistribution.getReqIds()));
		}
		//add the js node for res folder
		result.putAll(fetchAndBuildRequirementFoldersJsTreeNode(currentUser, nodeDistribution));

		return result;
	}

	private Map<Long, JsTreeNode> fetchAndBuildRequirementFoldersJsTreeNode(UserDto currentUser, RequirementLibraryNodeDistribution nodeDistribution) {
		//fetch requirement folders
		return DSL.select(RF.RLN_ID, RES.NAME, count(RLNR.ANCESTOR_ID).as("COUNT_CHILD")
			, REQUIREMENT_FOLDER_SYNC_EXTENDER.TYPE, REQUIREMENT_FOLDER_SYNC_EXTENDER.REMOTE_FOLDER_STATUS
			, REMOTE_SYNCHRONISATION.LAST_SYNC_STATUS)
			.from(RF)
			.innerJoin(RES).on(RES.RES_ID.eq(RF.RES_ID))
			.leftJoin(RLNR).on(RF.RLN_ID.eq(RLNR.ANCESTOR_ID))
			.leftJoin(REQUIREMENT_FOLDER_SYNC_EXTENDER).on(RF.RLN_ID.eq(REQUIREMENT_FOLDER_SYNC_EXTENDER.REQUIREMENT_FOLDER_ID))
			.leftJoin(REMOTE_SYNCHRONISATION).on(REQUIREMENT_FOLDER_SYNC_EXTENDER.REMOTE_SYNCHRONISATION_ID.eq(REMOTE_SYNCHRONISATION.REMOTE_SYNCHRONISATION_ID))
			.where(RF.RLN_ID.in(nodeDistribution.reqFolderIds))
			.groupBy(RF.RLN_ID, RES.NAME, RLNR.ANCESTOR_ID, REQUIREMENT_FOLDER_SYNC_EXTENDER.TYPE, REQUIREMENT_FOLDER_SYNC_EXTENDER.REMOTE_FOLDER_STATUS, REMOTE_SYNCHRONISATION.LAST_SYNC_STATUS)
			.fetch()
			.stream()
			.map(r -> {
				String lastRemoteSyncStatus = r.get(REMOTE_SYNCHRONISATION.LAST_SYNC_STATUS);
				if (lastRemoteSyncStatus != null) {
					return buildSynchronisedFolder(r.get(RLN.RLN_ID), r.get(RES.NAME), "requirement-folders", r.get(REMOTE_SYNCHRONISATION.LAST_SYNC_STATUS), r.get(REQUIREMENT_FOLDER_SYNC_EXTENDER.TYPE), r.get("COUNT_CHILD", Integer.class), currentUser);
				} else {
					return buildFolder(r.get(RLN.RLN_ID), r.get(RES.NAME), "requirement-folders", r.get("COUNT_CHILD", Integer.class), currentUser);
				}
			})
			.collect(Collectors.toMap(jsTreeNode -> (Long) jsTreeNode.getAttr().get("resId"), Function.identity()));
	}

	private JsTreeNode buildSynchronisedFolder(Long id, String name, String resType, String syncStatus, String syncFolderType, int childCount, UserDto currentUser) {
		JsTreeNode jsTreeNode = buildFolder(id, name, resType, childCount, currentUser);

		jsTreeNode.addAttr("sync-status", syncStatus);
		jsTreeNode.addAttr("sync-folder-type", syncFolderType);

		return jsTreeNode;
	}

	private Map<Long, JsTreeNode> fetchAndBuildRequirementJsTreeNode(UserDto currentUser, Map<Long, List<Long>> allMilestonesForReqs, List<Long> milestonesModifiable, Long activeMilestoneId, Collection<Long> nodeIdsToBuild) {

		return DSL.select(REQ.RLN_ID, REQ.MODE,
			RES.NAME,
			RV.REFERENCE, RV.REQUIREMENT_STATUS,
			ILI.ICON_NAME,
			count(RLNR.ANCESTOR_ID).as("COUNT_CHILD"))
			.from(REQ)
			.innerJoin(RES).on(RES.RES_ID.eq(REQ.CURRENT_VERSION_ID))
			.innerJoin(RV).on(RES.RES_ID.eq(RV.RES_ID))
			.innerJoin(ILI).on(RV.CATEGORY.eq(ILI.ITEM_ID.cast(Long.class)))
			.leftJoin(RLNR).on(REQ.RLN_ID.eq(RLNR.ANCESTOR_ID))
			.where(REQ.RLN_ID.in(nodeIdsToBuild))
			.groupBy(REQ.RLN_ID, REQ.MODE, RES.NAME, RV.REFERENCE, RV.REQUIREMENT_STATUS, ILI.ICON_NAME, RLNR.ANCESTOR_ID)
			.fetch()
			.stream()
			.map(r -> {
				return buildRequirementJsTreeNode(currentUser, allMilestonesForReqs, milestonesModifiable, activeMilestoneId, r);
			})
			.collect(Collectors.toMap(jsTreeNode -> (Long) jsTreeNode.getAttr().get("resId"), Function.identity()));
	}

	private JsTreeNode buildRequirementJsTreeNode(UserDto currentUser, Map<Long, List<Long>> allMilestonesForReqs, List<Long> milestonesModifiable, Long activeMilestoneId, Record7<Long, String, String, String, String, String, Integer> r) {
		Long id = r.get(REQ.RLN_ID);
		Integer milestonesNumber = getMilestonesNumberForReq(allMilestonesForReqs, id);
		String isMilestoneModifiable = isMilestoneModifiable(allMilestonesForReqs, milestonesModifiable, id);
		boolean isReqDontAllowClick = isReqDontAllowClick(reqsDontAllowClick, id);
		boolean isReqVersionModifiable = isReqVersionModifiable(r.get(RV.REQUIREMENT_STATUS), isMilestoneModifiable);
		String iconName = r.get(ILI.ICON_NAME);
		if (StringUtils.isBlank(iconName)) {
			iconName = "def_cat_noicon";
		}
		return buildRequirement(id, r.get(RES.NAME), "requirements", r.get(RV.REFERENCE),
			r.get(REQ.MODE), iconName, isReqVersionModifiable, r.get("COUNT_CHILD", Integer.class),
			currentUser, milestonesNumber, isMilestoneModifiable, isReqDontAllowClick, activeMilestoneId);
	}

	private Map<Long, JsTreeNode> buildRequirementJsTreeNodeLinkedToMilestone(UserDto currentUser, Map<Long, List<Long>> allMilestonesForReqs, List<Long> milestonesModifiable, Long activeMilestoneId, Collection<Long> nodeIdsToBuild) {

		return DSL.select(REQ.RLN_ID, REQ.MODE,
			RES.NAME,
			RV.REFERENCE, RV.REQUIREMENT_STATUS,
			ILI.ICON_NAME,
			count(RLNR.ANCESTOR_ID).as("COUNT_CHILD"))
			.from(REQ)
			.innerJoin(RV).on(RV.REQUIREMENT_ID.eq(REQ.RLN_ID))
			.innerJoin(MRV).on(MRV.REQ_VERSION_ID.eq(RV.RES_ID))
			.innerJoin(RES).on(RES.RES_ID.eq(RV.RES_ID))
			.innerJoin(ILI).on(RV.CATEGORY.eq(ILI.ITEM_ID.cast(Long.class)))
			.leftJoin(RLNR).on(REQ.RLN_ID.eq(RLNR.ANCESTOR_ID))
			.where(REQ.RLN_ID.in(nodeIdsToBuild))
			.and(MRV.MILESTONE_ID.eq(activeMilestoneId))
			.groupBy(REQ.RLN_ID, REQ.MODE, RES.NAME, RV.REFERENCE, RV.REQUIREMENT_STATUS, ILI.ICON_NAME, RLNR.ANCESTOR_ID)
			.fetch()
			.stream()
			.map(r -> {
				return buildRequirementJsTreeNode(currentUser, allMilestonesForReqs, milestonesModifiable, activeMilestoneId, r);
			})
			.collect(Collectors.toMap(jsTreeNode -> (Long) jsTreeNode.getAttr().get("resId"), Function.identity()));
	}

	private RequirementLibraryNodeDistribution getRepartition(Set<Long> childrenIds) {
		RequirementLibraryNodeDistribution nodes = new RequirementLibraryNodeDistribution();
		DSL.select(REQ.RLN_ID, RF.RLN_ID)
			.from(RLN)
			.leftJoin(REQ).on(REQ.RLN_ID.eq(RLN.RLN_ID))
			.leftJoin(RF).on(RF.RLN_ID.eq(RLN.RLN_ID))
			.where(RLN.RLN_ID.in(childrenIds))
			.fetch()
			.forEach(r -> {
				Long reqId = r.get(REQ.RLN_ID);
				Long reqFolderId = r.get(RF.RLN_ID);
				if (reqId != null) {
					nodes.addReqId(reqId);
				} else {
					nodes.addReqFolderId(reqFolderId);
				}
			});
		return nodes;
	}

	private JsTreeNode buildRequirement(Long id, String name, String restype, String reference, String mode, String categoryIcon,
										boolean isReqVersionModifiable, Integer childCount, UserDto currentUser, Integer milestonesNumber,
										String isMilestoneModifiable, boolean isReqDontAllowClick, Long activeMilestoneId) {
		Map<String, Object> attr = new HashMap<>();
		State state;
		attr.put("resId", id);
		attr.put("resType", restype);
		attr.put("name", name);
		attr.put("id", "Requirement-" + id);
		attr.put("rel", "requirement");
		attr.put("req-version-modifiable", isReqVersionModifiable);
		if (mode.equals(SYNCHRONIZED.name())) {
			attr.put("synchronized", true);
		}
		attr.put("category-icon", categoryIcon);

		if (childCount > 0) {
			state = JsTreeNode.State.closed;
		} else {
			state = State.leaf;
		}

		String title = name;
		if (!StringUtils.isEmpty(reference)) {
			title = reference + " - " + title;
			attr.put("reference", reference);
		}

		if (!NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId) && isReqDontAllowClick) {
			attr.put("milestones-dont-allow-click", "true");
		}

		return buildNode(title, state, attr, currentUser, milestonesNumber, isMilestoneModifiable);
	}

	private Integer getMilestonesNumberForReq(Map<Long, List<Long>> allMilestonesForReqs, Long id) {
		return (allMilestonesForReqs.get(id) != null) ? allMilestonesForReqs.get(id).size() : NODE_WITHOUT_MILESTONE;
	}

	private String isMilestoneModifiable(Map<Long, List<Long>> allMilestonesForReqs, List<Long> milestonesModifiable, Long id) {
		if (allMilestonesForReqs.get(id) != null && milestonesModifiable != null) {
			List<Long> allMilestonesForReq = allMilestonesForReqs.get(id);
			for (Long milestone : allMilestonesForReq) {
				if (!milestonesModifiable.contains(milestone)) {
					return "false";
				}
			}
		}
		return "true";
	}

	private boolean isReqDontAllowClick(List<Long> reqsDontAllowClick, Long id) {
		return reqsDontAllowClick.size() != 0 && reqsDontAllowClick.contains(id);
	}

	private boolean isReqVersionModifiable(String requirementStatus, String isMilestoneModifiable) {
		RequirementStatus reqStatus = RequirementStatus.valueOf(requirementStatus);
		return reqStatus.isRequirementModifiable() && Boolean.parseBoolean(isMilestoneModifiable);
	}

	public List<Long> findReqsWithChildrenLinkedToActiveMilestone(Long activeMilestoneId) {
		List<Long> reqIdsWithActiveMilestone =
			DSL.select(RV.REQUIREMENT_ID)
				.from(RV)
				.innerJoin(MRV).on(MRV.REQ_VERSION_ID.eq(RV.RES_ID))
				.where(MRV.MILESTONE_ID.eq(activeMilestoneId))
				.fetch(RV.REQUIREMENT_ID, Long.class);

		return DSL.selectDistinct(RLNRC.ANCESTOR_ID)
			.from(RLNRC)
			.where(RLNRC.DESCENDANT_ID.in(reqIdsWithActiveMilestone)
				.and(RLNRC.ANCESTOR_ID.notIn(DSL.select(REQUIREMENT_FOLDER.RLN_ID).from(REQUIREMENT_FOLDER)))
				.and(RLNRC.ANCESTOR_ID.notIn(reqIdsWithActiveMilestone)))
			.fetch(RLNRC.ANCESTOR_ID, Long.class);
	}

	// *************************************** send stuff to abstract workspace ***************************************

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.RL_ID;
	}

	@Override
	protected String getFolderName() {
		return "RequirementFolder";
	}

	@Override
	protected String getNodeName() {
		return "Requirement";
	}

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return REQUIREMENT_LIBRARY.RL_ID;
	}

	@Override
	protected Map<Long, List<Long>> findAllMilestonesForLN() {
		return DSL.select(REQUIREMENT_VERSION.REQUIREMENT_ID, MILESTONE_REQ_VERSION.MILESTONE_ID)
			.from(MILESTONE_REQ_VERSION)
			.join(REQUIREMENT_VERSION).on(MILESTONE_REQ_VERSION.REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
			.join(REQUIREMENT).on(REQUIREMENT_VERSION.REQUIREMENT_ID.eq(REQUIREMENT.RLN_ID))
			.fetchGroups(REQUIREMENT_VERSION.REQUIREMENT_ID, MILESTONE_REQ_VERSION.MILESTONE_ID);
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return REQUIREMENT_LIBRARY;
	}

	@Override
	protected TableLike<?> getLibraryTableContent() {
		return REQUIREMENT_LIBRARY_CONTENT;
	}

	@Override
	protected Field<Long> selectLNRelationshipAncestorId() {
		return RLN_RELATIONSHIP.ANCESTOR_ID;
	}

	@Override
	protected Field<Long> selectLNRelationshipDescendantId() {
		return RLN_RELATIONSHIP.DESCENDANT_ID;
	}

	@Override
	protected Field<Integer> selectLNRelationshipContentOrder() {
		return RLN_RELATIONSHIP.CONTENT_ORDER;
	}

	@Override
	protected TableLike<?> getLNRelationshipTable() {
		return RLN_RELATIONSHIP;
	}

	@Override
	protected Field<Long> getMilestoneLibraryNodeId() {
		return MILESTONE_REQ_VERSION.REQ_VERSION_ID;
	}

	@Override
	protected TableLike<?> getMilestoneLibraryNodeTable() {
		return MILESTONE_REQ_VERSION;
	}

	@Override
	protected Field<Long> getMilestoneId() {
		return MILESTONE_REQ_VERSION.MILESTONE_ID;
	}

	@Override
	protected HibernateEntityDao hibernateFolderDao() {
		return hibernateRequirementFolderDao;
	}

	@Override
	protected Set<Long> findLNByMilestoneId(Long activeMilestoneId) {
		List<Long> reqsDontAllowClick = findReqsWithChildrenLinkedToActiveMilestone(activeMilestoneId);
		return new HashSet<>(DSL.select(REQUIREMENT_VERSION.REQUIREMENT_ID)
			.from(MILESTONE_REQ_VERSION)
			.leftJoin(REQUIREMENT_VERSION).on(MILESTONE_REQ_VERSION.REQ_VERSION_ID.eq(REQUIREMENT_VERSION.RES_ID))
			.leftJoin(REQUIREMENT).on(REQUIREMENT_VERSION.REQUIREMENT_ID.eq(REQUIREMENT.RLN_ID))
			.where(MILESTONE_REQ_VERSION.MILESTONE_ID.eq(activeMilestoneId))
			.union(DSL.select(REQUIREMENT_FOLDER.RLN_ID).from(REQUIREMENT_FOLDER))
			.union(DSL.select(REQUIREMENT.RLN_ID).from(REQUIREMENT).where(REQUIREMENT.RLN_ID.in(reqsDontAllowClick)))
			.fetch(REQUIREMENT_VERSION.REQUIREMENT_ID, Long.class));
	}

	@Override
	protected boolean passesMilestoneFilter(JsTreeNode node, Long activeMilestoneId) {
		return (node != null && (NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId) || node.getAttr().get("rel").equals("folder") || nodeHasActiveMilestone(nodeLinkedToMilestone, (Long) node.getAttr().get("resId")) || reqsDontAllowClick.contains(node.getAttr().get("resId"))));
	}

	@Override
	protected Field<Long> selectLibraryContentContentId() {
		return REQUIREMENT_LIBRARY_CONTENT.CONTENT_ID;
	}

	@Override
	protected Field<Integer> selectLibraryContentOrder() {
		return REQUIREMENT_LIBRARY_CONTENT.CONTENT_ORDER;
	}

	@Override
	protected Field<Long> selectLibraryContentLibraryId() {
		return REQUIREMENT_LIBRARY_CONTENT.LIBRARY_ID;
	}

	@Override
	protected String getClassName() {
		return RequirementLibrary.class.getSimpleName();
	}

	@Override
	protected String getLibraryClassName() {
		return RequirementLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		return RequirementLibraryPluginBinding.RL_TYPE;
	}

	@Override
	public Collection<JsTreeNode> getCampaignNodeContent(Long folderId, UserDto currentUser, String libraryNode) {
		return null;
	}

	public class RequirementLibraryNodeDistribution {
		Set<Long> reqIds = new HashSet<>();
		Set<Long> reqFolderIds = new HashSet<>();
		Set<Long> reqSyncFolderIds = new HashSet<>();

		public Set<Long> getReqIds() {
			return reqIds;
		}

		public void addReqId(Long reqId) {
			this.reqIds.add(reqId);
		}

		public Set<Long> getReqFolderIds() {
			return reqFolderIds;
		}

		public void addReqFolderId(Long reqFolderId) {
			this.reqFolderIds.add(reqFolderId);
		}

		public Set<Long> getReqSyncFolderIds() {
			return reqSyncFolderIds;
		}

		public void addReqSyncFolderIds(Long reqSyncFolderId) {
			this.reqSyncFolderIds.add(reqSyncFolderId);
		}


	}
}
