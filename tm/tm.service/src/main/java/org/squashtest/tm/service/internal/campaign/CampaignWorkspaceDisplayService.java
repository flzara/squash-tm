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
package org.squashtest.tm.service.internal.campaign;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.TableField;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.campaign.CampaignLibrary;
import org.squashtest.tm.domain.campaign.CampaignLibraryPluginBinding;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.jooq.domain.tables.*;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateCampaignFolderDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateEntityDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateIterationDao;
import org.squashtest.tm.service.internal.workspace.AbstractWorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.count;
import static org.squashtest.tm.jooq.domain.Tables.*;

@Service("campaignWorkspaceDisplayService")
@Transactional(readOnly = true)
public class CampaignWorkspaceDisplayService extends AbstractWorkspaceDisplayService {

	@Inject
	DSLContext DSL;

	@Inject
	HibernateCampaignFolderDao hibernateCampaignFolderDao;

	@Inject
	HibernateCampaignDao hibernateCampaignDao;

	@Inject
	HibernateIterationDao hibernateIterationDao;

	private Campaign C = CAMPAIGN.as("C");
	private CampaignLibraryNode CLN = CAMPAIGN_LIBRARY_NODE.as("CLN");
	private CampaignFolder CF = CAMPAIGN_FOLDER.as("CF");
	private ClnRelationship CLNR = CLN_RELATIONSHIP.as("CLNR");
	private CampaignIteration CI = CAMPAIGN_ITERATION.as("CI");
	private MilestoneCampaign MC = MILESTONE_CAMPAIGN.as("MC");
	private Milestone M = MILESTONE.as("M");
	private IterationTestSuite ITS = ITERATION_TEST_SUITE.as("ITS");
	private Iteration IT = ITERATION.as("IT");
	private TestSuite TS = TEST_SUITE.as("TS");
	private TestSuiteTestPlanItem TSTPI = TEST_SUITE_TEST_PLAN_ITEM.as("TSTPI");
	private IterationTestPlanItem ITPI = ITERATION_TEST_PLAN_ITEM.as("ITPI");
	private TestCaseLibraryNode TCLN = TEST_CASE_LIBRARY_NODE.as("TCLN");

	private MultiMap campaignFatherChildrenMultimap = new MultiValueMap();
	private MultiMap iterationFatherChildrenMultiMap = new MultiValueMap();
	private Map<Long, JsTreeNode> iterationMap = new HashMap<>();
	private Map<Long, JsTreeNode> testSuiteMap = new HashMap<>();


	@Override
	protected Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, MultiMap expansionCandidates, UserDto currentUser, Map<Long, List<Long>> allMilestonesForLN, List<Long> milestonesModifiable, Long activeMilestoneId) {

		getCampaignHierarchy(currentUser, expansionCandidates);

		CampaignLibraryNodeDistribution nodeDistribution = getNodeDistribution(childrenIds);


		Map<Long, JsTreeNode> result = DSL.select(CLN.CLN_ID,
				CLN.NAME,
				C.REFERENCE,
				count(CI.CAMPAIGN_ID).as("ITERATION_COUNT"),
				MC.MILESTONE_ID,
				M.STATUS)
			.from(CLN)
			.innerJoin(C).on(CLN.CLN_ID.eq(C.CLN_ID))
			.leftJoin(CI).on(CLN.CLN_ID.eq(CI.CAMPAIGN_ID))
			.leftJoin(MC).on(CLN.CLN_ID.eq(MC.CAMPAIGN_ID))
			.leftJoin(M).on(MC.MILESTONE_ID.eq(M.MILESTONE_ID))
			.where(CLN.CLN_ID.in(nodeDistribution.getCampaignIds()))
			.groupBy(CLN.CLN_ID,CLN.NAME,C.REFERENCE,CI.CAMPAIGN_ID,MC.MILESTONE_ID,M.STATUS)
			.fetch()
			.stream()
			.map(r ->{
				boolean isMilestoneModifiable = isMilestoneModifiable(r.get(M.STATUS,String.class));
				return buildCampaign(r.get(CLN.CLN_ID), r.get(CLN.NAME), "campaigns", r.get(C.REFERENCE), r.get("ITERATION_COUNT", Integer.class), currentUser, r.get(MC.MILESTONE_ID), isMilestoneModifiable);
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));

		Map<Long, JsTreeNode> collect = DSL.select(CLN.CLN_ID, CLN.NAME, count(CLNR.ANCESTOR_ID).as("CHILD_COUNT"))
			.from(CLN)
			.innerJoin(CF).on(CF.CLN_ID.eq(CLN.CLN_ID))
			.leftJoin(CLNR).on(CLNR.ANCESTOR_ID.eq(CLN.CLN_ID))
			.where(CLN.CLN_ID.in(nodeDistribution.getCampaignFolderIds()))
			.groupBy(CLN.CLN_ID, CLN.NAME,CLNR.ANCESTOR_ID)
			.fetch()
			.stream()
			.map(r -> buildFolder(r.get(CLN.CLN_ID), r.get(CLN.NAME), "campaign-folders", r.get("CHILD_COUNT", Integer.class), currentUser))
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));

		result.putAll(collect);
		return result;
	}

	public Collection<JsTreeNode> getCampaignNodeContent(Long entityId, UserDto currentUser, String entityClass) {
		Set<Long> childrenIds = new HashSet<>();
		MultiMap expansionCandidates = new MultiValueMap();
		expansionCandidates.put(entityClass, entityId);
		Long libraryId;
		Map<Long, String> testSuiteDescriptions = getTestSuiteDescriptionList();

		MultiMap entityFatherChildrenMultimap = getFatherChildrenLibraryNode(entityClass, expansionCandidates);
		childrenIds.remove(entityId);

		Map<Long, JsTreeNode> libraryChildrenMap =
			(entityClass.equals("Campaign") ? getCampaignChildren(entityFatherChildrenMultimap, currentUser) : getIterationChildren(entityFatherChildrenMultimap, currentUser, testSuiteDescriptions));

		libraryId = entityClass.equals("Campaign") ? hibernateCampaignDao.findById(entityId).getLibrary().getId() : hibernateIterationDao.findById(entityId).getCampaignLibrary().getId();

		if (currentUser.isNotAdmin()) {
			findNodeChildrenPermissionMap(currentUser, libraryChildrenMap, libraryId);
		}

		return libraryChildrenMap.values();
	}

	// ********************************************** Utils ************************************************************

	private CampaignLibraryNodeDistribution getNodeDistribution(Set<Long> childrenIds) {
		CampaignLibraryNodeDistribution nodes = new CampaignLibraryNodeDistribution();
		DSL.select(C.CLN_ID, CF.CLN_ID)
			.from(CLN)
			.leftJoin(C).on(C.CLN_ID.eq(CLN.CLN_ID))
			.leftJoin(CF).on(CF.CLN_ID.eq(CLN.CLN_ID))
			.where(CLN.CLN_ID.in(childrenIds))
			.fetch()
			.forEach(r -> {
				Long campaignId = r.get(C.CLN_ID);
				Long campaignFolderId = r.get(CF.CLN_ID);
				if (campaignId != null) {
					nodes.addCampaignId(campaignId);
				} else {
					nodes.addCampaignFolderId(campaignFolderId);
				}
			});
		return nodes;
	}

	private JsTreeNode buildCampaign(Long campaignId, String name, String restype, String reference, int iterationCount, UserDto currentUser, Long milestone, boolean isMilestoneModifiable) {
		Map<String, Object> attr = new HashMap<>();

		attr.put("resId", campaignId);
		attr.put("resType", restype);
		attr.put("name", name);
		attr.put("id", "Campaign-" + campaignId);
		attr.put("rel", "campaign");

		String title = name;
		if (!StringUtils.isEmpty(reference)) {
			attr.put("reference", reference);
			title = reference + " - " + title;
		}
		Integer milestonesNumber = getMilestoneNumber(milestone);

		JsTreeNode campaign = buildNode(title, null, attr, currentUser, milestonesNumber, String.valueOf(isMilestoneModifiable));

		// Messy but still simpler than GOT's genealogy
		if (iterationCount == 0 ) {
			campaign.setState(State.leaf);
		} else if (campaignFatherChildrenMultimap.containsKey(campaignId)) {
			campaign.setState(State.open);
			for (Long iterationId : (ArrayList<Long>) campaignFatherChildrenMultimap.get(campaignId)) {
				if (iterationFatherChildrenMultiMap.containsKey(iterationId)) {
					iterationMap.get(iterationId).setState(State.open);
					for (Long testSuiteId : (ArrayList<Long>) iterationFatherChildrenMultiMap.get(iterationId)) {
						iterationMap.get(iterationId).addChild(testSuiteMap.get(testSuiteId));
					}
				}
				campaign.addChild(iterationMap.get(iterationId));
			}
		} else {
			campaign.setState(State.closed);
		}
		return campaign;
	}

	private JsTreeNode buildIteration(Long id, String name, String reference, Integer iterationOrder, boolean hasContent, UserDto currentUser, Long milestone, String isMilestoneModifiable) {
		Map<String, Object> attr = new HashMap<>();
		JsTreeNode.State state;

		attr.put("resId", id);
		attr.put("resType", "iterations");
		attr.put("name", name);
		attr.put("id", "Iteration-" + id);
		attr.put("rel", "iteration");
		attr.put("iterationIndex", String.valueOf(iterationOrder + 1));
		if (hasContent) {
			state = State.closed;
		} else {
			state = State.leaf;
		}

		String title = name;
		if (!StringUtils.isEmpty(reference)) {
			title = reference + " - " + title;
			attr.put("reference", reference);
		}
		Integer milestonesNumber = getMilestoneNumber(milestone);

		return buildNode(title, state, attr, currentUser, milestonesNumber, isMilestoneModifiable);
	}

	private JsTreeNode buildTestSuite(Long id, String name, String executionStatus, String description, UserDto currentUser, Long milestone, String isMilestoneModifiable) {
		Map<String, Object> attr = new HashMap<>();

		attr.put("resId", id);
		attr.put("name", name);
		attr.put("id", "TestSuite-" + id);
		attr.put("executionstatus", executionStatus);
		attr.put("resType", "test-suites");
		attr.put("rel", "test-suite");
		//build tooltip
		String[] args = {getMessage("execution.execution-status." + executionStatus)};
		String tooltip = getMessage("label.tree.testSuite.tooltip", args);
		attr.put("title", tooltip + "\n" + removeHtmlForDescription(description));
		Integer milestonesNumber = getMilestoneNumber(milestone);
		return buildNode(name, State.leaf, attr, currentUser, milestonesNumber, isMilestoneModifiable);

	}

	//Campaigns got iterations and test suites which aren't located in the campaign_library_node table.
	// We must fetch them separately, because they might have identical ids
	private void getCampaignHierarchy(UserDto currentUser, MultiMap expansionCandidates) {
		//first: iterations, get father-children relation, fetch them and add them to the campaigns
		campaignFatherChildrenMultimap = getFatherChildrenLibraryNode("Campaign", expansionCandidates);
		iterationMap = getCampaignChildren(campaignFatherChildrenMultimap, currentUser);
		//second test suites, get father-children relation, fetch them and add them  to  the iterations
		iterationFatherChildrenMultiMap = getFatherChildrenLibraryNode("Iteration", expansionCandidates);
		Map<Long, String> testSuiteDescriptions = getTestSuiteDescriptionList();
		testSuiteMap = getIterationChildren(iterationFatherChildrenMultiMap, currentUser, testSuiteDescriptions);

	}

	private MultiMap getFatherChildrenLibraryNode(String resType, MultiMap expansionCandidates) {
		MultiMap result = new MultiValueMap();

		TableField<?, ?> fatherColumn;
		TableField<?, ?> childColumn;
		TableField<?, ?> orderColumn;
		TableLike<?> table;

		if (resType.equals("Campaign")) {
			table = CI;
			fatherColumn = CI.CAMPAIGN_ID;
			childColumn = CI.ITERATION_ID;
			orderColumn = CI.ITERATION_ORDER;
		} else {
			table = ITS;
			fatherColumn = ITS.ITERATION_ID;
			childColumn = ITS.TEST_SUITE_ID;
			orderColumn = ITS.TEST_SUITE_ID;

		}
		List<Long> openedEntityIds = (List<Long>) expansionCandidates.get(resType);
		if (!CollectionUtils.isEmpty(openedEntityIds)) {
			DSL
				.select(
					fatherColumn,
					childColumn
				)
				.from(table)
				.where(fatherColumn.in(openedEntityIds))
				.orderBy(orderColumn)
				.fetch()
				.forEach(r ->
						result.put(r.get(fatherColumn), r.get(childColumn))
				);
		}
		return result;
	}

	private Map<Long, JsTreeNode> getCampaignChildren(MultiMap fatherChildrenEntity, UserDto currentUser) {
		return DSL
			.select(
				IT.ITERATION_ID,
				IT.NAME,
				IT.REFERENCE,
				CI.ITERATION_ORDER,
				count(ITS.ITERATION_ID).as("ITERATION_COUNT"),
				MC.MILESTONE_ID,
				M.STATUS
			)
			.from(IT)
			.leftJoin(CI).on(IT.ITERATION_ID.eq(CI.ITERATION_ID))
			.leftJoin(MC).on(CI.CAMPAIGN_ID.eq(MC.CAMPAIGN_ID))
			.leftJoin(M).on(MC.MILESTONE_ID.eq(M.MILESTONE_ID))
			.leftJoin(ITS).on(IT.ITERATION_ID.eq(ITS.ITERATION_ID))
			.where(IT.ITERATION_ID.in(fatherChildrenEntity.values()))
			.groupBy(IT.ITERATION_ID, CI.ITERATION_ORDER, ITS.ITERATION_ID, MC.MILESTONE_ID, M.STATUS)
			.fetch()
			.stream()
			.map(r -> {
				boolean milestoneModifiable = isMilestoneModifiable(r.get(M.STATUS));
				boolean hasContent = r.get("ITERATION_COUNT",Integer.class) > 0;
				return buildIteration(r.get(IT.ITERATION_ID), r.get(IT.NAME), r.get(IT.REFERENCE),
					r.get(CI.ITERATION_ORDER), hasContent, currentUser, r.get(MC.MILESTONE_ID), String.valueOf(milestoneModifiable));
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	private Map<Long, JsTreeNode> getIterationChildren(MultiMap fatherChildrenEntity, UserDto currentUser, Map<Long, String> testSuiteDescriptions) {
		return DSL
			.select(
				TS.ID,
				TS.NAME,
				TS.EXECUTION_STATUS,
				MC.MILESTONE_ID,
				M.STATUS
			)
			.from(TS)
			.innerJoin(ITS).on(TS.ID.eq(ITS.TEST_SUITE_ID))
			.innerJoin(CI).on(ITS.ITERATION_ID.eq(CI.ITERATION_ID))
			.leftJoin(MC).on(CI.CAMPAIGN_ID.eq(MC.CAMPAIGN_ID))
			.leftJoin(M).on(MC.MILESTONE_ID.eq(M.MILESTONE_ID))
			.where(TS.ID.in(fatherChildrenEntity.values()))
			.groupBy(TS.ID, MC.MILESTONE_ID, M.STATUS)
			.fetch()
			.stream()
			.map(r -> {
				String description = getTSDescription(testSuiteDescriptions, r.get(TS.ID));
				boolean milestoneModifiable = isMilestoneModifiable(r.get(M.STATUS));
				return buildTestSuite(r.get(TS.ID), r.get(TS.NAME), r.get(TS.EXECUTION_STATUS), description, currentUser, r.get(MC.MILESTONE_ID), String.valueOf(milestoneModifiable));
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get("resId"), Function.identity()));
	}

	private String getTSDescription(Map<Long, String> testSuiteDescriptions, Long id) {
		String description = testSuiteDescriptions.get(id);
		return (description != null) ? description : "";
	}

	private Map<Long, String> getTestSuiteDescriptionList() {
		Field<String> description = org.jooq.impl.DSL.coalesce(TCLN.DESCRIPTION, "");
		return DSL.selectDistinct(TS.ID, description)
			.from(TS)
			.leftJoin(TSTPI).on(TS.ID.eq(TSTPI.SUITE_ID))
			.leftJoin(ITPI).on(TSTPI.TPI_ID.eq(ITPI.ITEM_TEST_PLAN_ID))
			.leftJoin(TCLN).on(ITPI.TCLN_ID.eq(TCLN.TCLN_ID))
			.where(TSTPI.TEST_PLAN_ORDER.eq(0))
			.fetch()
			.stream()
			.collect(Collectors.toMap(r-> r.get(TS.ID), r-> r.get(description)));
	}

	private String removeHtmlForDescription(String html) {
		if (StringUtils.isBlank(html)) {
			return "";
		}
		String description = "<html>" + html + "</html>";
		description = description.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
		description = HtmlUtils.htmlUnescape(description);
		return (description.length() > 30) ? description.substring(0, 30) + "..." : description;
	}

	private Integer getMilestoneNumber(Long milestone) {
		return (milestone == null) ? NODE_WITHOUT_MILESTONE : 1;
	}

	// *************************************** send stuff to abstract workspace ***************************************

	@Override
	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.CL_ID;
	}

	@Override
	protected String getFolderName() {
		return "CampaignFolder";
	}

	@Override
	protected String getNodeName() {
		return "Campaign";
	}

	@Override
	protected String getRel() {
		return "drive";
	}

	@Override
	protected Field<Long> selectLibraryId() {
		return CAMPAIGN_LIBRARY.CL_ID;
	}

	@Override
	protected Map<Long, List<Long>> findAllMilestonesForLN() {
		return null;
	}

	@Override
	protected TableLike<?> getLibraryTable() {
		return CAMPAIGN_LIBRARY;
	}

	@Override
	protected TableLike<?> getLibraryTableContent() {
		return CAMPAIGN_LIBRARY_CONTENT;
	}

	@Override
	protected Field<Long> selectLNRelationshipAncestorId() {
		return CLN_RELATIONSHIP.ANCESTOR_ID;
	}

	@Override
	protected Field<Long> selectLNRelationshipDescendantId() {
		return CLN_RELATIONSHIP.DESCENDANT_ID;
	}

	@Override
	protected Field<Integer> selectLNRelationshipContentOrder() {
		return CLN_RELATIONSHIP.CONTENT_ORDER;
	}

	@Override
	protected TableLike<?> getLNRelationshipTable() {
		return CLN_RELATIONSHIP;
	}

	@Override
	protected Field<Long> getMilestoneLibraryNodeId() {
		return MILESTONE_CAMPAIGN.CAMPAIGN_ID;
	}

	@Override
	protected TableLike<?> getMilestoneLibraryNodeTable() {
		return MILESTONE_CAMPAIGN;
	}

	@Override
	protected Field<Long> getMilestoneId() {
		return MILESTONE_CAMPAIGN.MILESTONE_ID;
	}

	@Override
	protected HibernateEntityDao hibernateFolderDao() {
		return hibernateCampaignFolderDao;
	}

	@Override
	protected Set<Long> findLNByMilestoneId(Long activeMilestoneId) {
		return new HashSet<>(DSL.select(MILESTONE_CAMPAIGN.CAMPAIGN_ID)
			.from(MILESTONE_CAMPAIGN)
			.where(MILESTONE_CAMPAIGN.MILESTONE_ID.eq(activeMilestoneId))
			.union(DSL.select(CAMPAIGN_FOLDER.CLN_ID).from(CAMPAIGN_FOLDER))
			.fetch(MILESTONE_CAMPAIGN.CAMPAIGN_ID, Long.class));
	}

	@Override
	protected boolean passesMilestoneFilter(JsTreeNode node, Long activeMilestoneId) {
		return (node != null && (NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId) || node.getAttr().get("rel").equals("folder") || nodeHasActiveMilestone(nodeLinkedToMilestone, (Long) node.getAttr().get("resId"))));
	}

	@Override
	protected Field<Long> selectLibraryContentContentId() {
		return CAMPAIGN_LIBRARY_CONTENT.CONTENT_ID;
	}

	@Override
	protected Field<Integer> selectLibraryContentOrder() {
		return CAMPAIGN_LIBRARY_CONTENT.CONTENT_ORDER;
	}

	@Override
	protected Field<Long> selectLibraryContentLibraryId() {
		return CAMPAIGN_LIBRARY_CONTENT.LIBRARY_ID;
	}

	@Override
	protected String getClassName() {
		return CampaignLibrary.class.getSimpleName();
	}

	@Override
	protected String getLibraryClassName() {
		return CampaignLibrary.class.getName();
	}

	@Override
	protected String getLibraryPluginType() {
		return CampaignLibraryPluginBinding.CL_TYPE;
	}

	class CampaignLibraryNodeDistribution {
		Set<Long> campaignIds = new HashSet<>();
		Set<Long> campaignFolderIds = new HashSet<>();

		public Set<Long> getCampaignIds() {
			return campaignIds;
		}

		public void addCampaignId(Long campaignId) {
			this.campaignIds.add(campaignId);
		}

		public Set<Long> getCampaignFolderIds() {
			return campaignFolderIds;
		}

		public void addCampaignFolderId(Long campaignFolderId) {
			this.campaignFolderIds.add(campaignFolderId);
		}

	}
}
