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
package org.squashtest.tm.service.internal.workspace;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.TableLike;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.domain.milestone.MilestoneStatus;
import org.squashtest.tm.domain.project.ProjectResource;
import org.squashtest.tm.service.internal.dto.PermissionWithMask;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode.State;
import org.squashtest.tm.service.internal.dto.json.JsonProject;
import org.squashtest.tm.service.internal.helper.HyphenedStringHelper;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateEntityDao;
import org.squashtest.tm.service.internal.repository.hibernate.HibernateRequirementDao;
import org.squashtest.tm.service.workspace.WorkspaceDisplayService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.count;
import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.service.internal.dto.PermissionWithMask.findByMask;

public abstract class AbstractWorkspaceDisplayService implements WorkspaceDisplayService {

	protected static final String RES_ID = "resId";

	@Inject
	private MessageSource messageSource;

	@Inject
	private DSLContext DSL;

	@Inject
	private HibernateRequirementDao hibernateRequirementDao;

	protected static final String MILESTONE_STATUS_IN_PROGRESS = "IN_PROGRESS";
	protected static final String MILESTONE_STATUS_FINISHED = "FINISHED";
	protected static final Integer NODE_WITHOUT_MILESTONES_ATTRIBUTE = -1;
	protected static final Integer NODE_WITHOUT_MILESTONE = Integer.valueOf(0);
	protected static final Long NO_ACTIVE_MILESTONE_ID = -9000L;
	protected Set<Long> nodeLinkedToMilestone = new HashSet<>();

	// ************************************* get Stuff to show the workspace trees *************************************

	public Collection<JsTreeNode> findAllLibraries(List<Long> readableProjectIds, UserDto currentUser, MultiMap expansionCandidates, Long activeMilestoneId) {
		Set<Long> childrenIds = new HashSet<>();

		if (!NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId)) {
			nodeLinkedToMilestone = findLNByMilestoneId(activeMilestoneId);
		}

		MultiMap libraryFatherChildrenMultiMap = getLibraryFatherChildrenMultiMap(expansionCandidates, childrenIds, nodeLinkedToMilestone, activeMilestoneId);
		MultiMap libraryNodeFatherChildrenMultiMap = getLibraryNodeFatherChildrenMultiMap(expansionCandidates, childrenIds, nodeLinkedToMilestone, activeMilestoneId);

		// milestones
		Map<Long, List<Long>> allMilestonesForLN = findAllMilestonesForLN();
		List<Long> milestonesModifiable = getMilestonesModifiable();
		Map<Long, JsTreeNode> libraryChildrenMap = getLibraryChildrenMap(childrenIds, expansionCandidates, currentUser, allMilestonesForLN, milestonesModifiable, activeMilestoneId);
		Map<Long, JsTreeNode> jsTreeNodes = doFindLibraries(readableProjectIds, currentUser);

		buildHierarchy(jsTreeNodes, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap, libraryChildrenMap, activeMilestoneId);

		findWizards(readableProjectIds, jsTreeNodes);

		if (currentUser.isNotAdmin()) {
			findPermissionMap(currentUser, jsTreeNodes);
		}

		return jsTreeNodes.values();
	}

	protected Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser) {
		List<Long> filteredProjectIds;
		if (hasActiveFilter(currentUser.getUsername())) {
			filteredProjectIds = findFilteredProjectIds(readableProjectIds, currentUser.getUsername());
		} else {
			filteredProjectIds = readableProjectIds;
		}

		return DSL
			.select(
				selectLibraryId(),
				PROJECT.PROJECT_ID,
				PROJECT.NAME,
				PROJECT.LABEL,
				PROJECT.ALLOW_AUTOMATION_WORKFLOW,
				count(selectLibraryContentLibraryId()).as("COUNT_CHILD"))
			.from(getLibraryTable())
			.join(PROJECT).using(selectLibraryId())
			.leftJoin(getLibraryTableContent()).on(selectLibraryId().eq(selectLibraryContentLibraryId()))
			.where(PROJECT.PROJECT_ID.in(filteredProjectIds))
			.and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
			.groupBy(selectLibraryId(), PROJECT.PROJECT_ID, PROJECT.NAME, PROJECT.LABEL, PROJECT.ALLOW_AUTOMATION_WORKFLOW, selectLibraryContentLibraryId())
			.fetch()
			.stream()
			.map(r -> {
				Map<String, Object> attr = new HashMap<>();
				State state;
				Long libraryId = r.get(selectLibraryId(), Long.class);
				attr.put(RES_ID, libraryId);
				attr.put("resType", getResType());
				attr.put("rel", getRel());
				attr.put("name", HtmlUtils.htmlEscape(r.get(PROJECT.NAME)));
				attr.put("id", getClassName() + '-' + libraryId);
				attr.put("title",removeHtmlForDescription(r.get(PROJECT.LABEL)));
				attr.put("project", r.get(PROJECT.PROJECT_ID));

				if ("test-case-libraries".equals(getResType())) {
					attr.put("allowAutomWorkflow", r.get(PROJECT.ALLOW_AUTOMATION_WORKFLOW));
				}

				Integer countChild = r.get("COUNT_CHILD", Integer.class);
				if (countChild > 0) {
					state = State.closed;
				} else {
					state = State.leaf;
				}

				return buildNode(HtmlUtils.htmlEscape(r.get(PROJECT.NAME)), state, attr, currentUser, NODE_WITHOUT_MILESTONES_ATTRIBUTE, "true");
			})
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get(RES_ID), Function.identity(),
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				},
				LinkedHashMap::new));
	}

	public String removeHtmlForDescription(String html) {
		if (StringUtils.isBlank(html)) {
			return "";
		}
		String description = "<html>" + html + "</html>";
		description = description.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
		description = HtmlUtils.htmlUnescape(description);
		return (description.length() > 30) ? description.substring(0, 30) + "..." : description;
	}

	public Collection<JsonProject> findAllEmptyProjects(List<Long> readableProjectIds) {
		Map<Long, JsonProject> jsonProjects = findEmptyJsonProjects(readableProjectIds);
		return jsonProjects.values();
	}

	public Collection<JsTreeNode> getNodeContent(Long entityId, UserDto currentUser, String entityClass, Long activeMilestoneId) {
		Set<Long> childrenIds = new HashSet<>();
		MultiMap expansionCandidates = new MultiValueMap();
		Long libraryId;

		if (!NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId)) {
			nodeLinkedToMilestone = findLNByMilestoneId(activeMilestoneId);
		}

		switch (entityClass) {
			case "library":
				expansionCandidates.put(getClassName(), entityId);
				getLibraryFatherChildrenMultiMap(expansionCandidates, childrenIds, nodeLinkedToMilestone, activeMilestoneId);
				libraryId = entityId;
				break;
			case "folder":
				expansionCandidates.put(getFolderName(), entityId);
				getLibraryNodeFatherChildrenMultiMap(expansionCandidates, childrenIds, nodeLinkedToMilestone, activeMilestoneId);
				libraryId = ((ProjectResource) hibernateFolderDao().findById(entityId)).getLibrary().getId();
				childrenIds.remove(entityId);
				break;
			default: //used for requirements only
				expansionCandidates.put(entityClass, entityId);
				getLibraryNodeFatherChildrenMultiMap(expansionCandidates, childrenIds, nodeLinkedToMilestone, activeMilestoneId);
				libraryId = ((ProjectResource) hibernateRequirementDao.findById(entityId)).getLibrary().getId();
				childrenIds.remove(entityId);
				break;
		}

		// milestones
		Map<Long, List<Long>> allMilestonesForLN = findAllMilestonesForLN();
		List<Long> milestonesModifiable = getMilestonesModifiable();
		Map<Long, JsTreeNode> libraryChildrenMap = getLibraryChildrenMap(childrenIds, expansionCandidates, currentUser, allMilestonesForLN, milestonesModifiable, activeMilestoneId);

		if (currentUser.isNotAdmin()) {
			findNodeChildrenPermissionMap(currentUser, libraryChildrenMap, libraryId);
		}

		return libraryChildrenMap.values();
	}


	// ********************************************** Utils ************************************************************

	protected void findNodeChildrenPermissionMap(UserDto currentUser, Map<Long, JsTreeNode> libraryChildrenMap, Long libraryId) {
		List<Integer> masks = DSL
			.selectDistinct(ACL_GROUP_PERMISSION.PERMISSION_MASK)
			.from(getLibraryTable())
			.join(PROJECT).on(getProjectLibraryColumn().eq(selectLibraryId()))
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.IDENTITY.eq(selectLibraryId()))
			.join(ACL_RESPONSIBILITY_SCOPE_ENTRY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_GROUP_PERMISSION).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.eq(ACL_GROUP_PERMISSION.ACL_GROUP_ID))
			.join(ACL_CLASS).on(ACL_GROUP_PERMISSION.CLASS_ID.eq(ACL_CLASS.ID).and(ACL_CLASS.CLASSNAME.eq(getLibraryClassName())))
			.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(currentUser.getPartyIds())).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE)).and(getProjectLibraryColumn().in(libraryId))
			.fetch(ACL_GROUP_PERMISSION.PERMISSION_MASK, Integer.class);

		for (JsTreeNode node : libraryChildrenMap.values()) {
			givePermissions(node, masks);
		}
	}

	private Map<Long, JsonProject> findEmptyJsonProjects(List<Long> readableProjectIds) {
		return DSL.select(PROJECT.PROJECT_ID, PROJECT.NAME, PROJECT.REQ_CATEGORIES_LIST, PROJECT.TC_NATURES_LIST, PROJECT.TC_TYPES_LIST)
			.from(PROJECT)
			.where(PROJECT.PROJECT_ID.in(readableProjectIds)).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
			.orderBy(PROJECT.PROJECT_ID)
			.stream()
			.map(r -> {
				Long projectId = r.get(PROJECT.PROJECT_ID);
				JsonProject jsonProject = new JsonProject(projectId, r.get(PROJECT.NAME));
				return jsonProject;

			}).collect(Collectors.toMap(JsonProject::getId, Function.identity()));
	}


	public void findPermissionMap(UserDto currentUser, Map<Long, JsTreeNode> jsTreeNodes) {
		Set<Long> libraryIds = jsTreeNodes.keySet();

		DSL
			.selectDistinct(selectLibraryId(), ACL_GROUP_PERMISSION.PERMISSION_MASK)
			.from(getLibraryTable())
			.join(PROJECT).on(getProjectLibraryColumn().eq(selectLibraryId()))
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.IDENTITY.eq(selectLibraryId()))
			.join(ACL_RESPONSIBILITY_SCOPE_ENTRY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_GROUP_PERMISSION).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.eq(ACL_GROUP_PERMISSION.ACL_GROUP_ID))
			.join(ACL_CLASS).on(ACL_GROUP_PERMISSION.CLASS_ID.eq(ACL_CLASS.ID).and(ACL_CLASS.CLASSNAME.eq(getLibraryClassName())))
			.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(currentUser.getPartyIds())).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE)).and(getProjectLibraryColumn().in(libraryIds))
			.fetch()
			.stream()
			.collect(groupingBy(
				r -> r.getValue(selectLibraryId()),
				mapping(
					r -> r.getValue(ACL_GROUP_PERMISSION.PERMISSION_MASK),
					toList()
				)
			)).forEach((Long nodeId, List<Integer> masks) -> {
			JsTreeNode node = jsTreeNodes.get(nodeId);
			givePermissions(node, masks);
		});
	}

	private void givePermissions(JsTreeNode node, List<Integer> masks) {
		for (Integer mask : masks) {
			PermissionWithMask permission = findByMask(mask);
			if (permission != null) {
				node.addAttr(permission.getQuality(), String.valueOf(true));
			}
		}

		if (!CollectionUtils.isEmpty(node.getChildren())) {
			for (JsTreeNode child : node.getChildren()) {
				givePermissions(child, masks);
			}
		}

	}

	protected MultiMap getLibraryFatherChildrenMultiMap(MultiMap expansionCandidates, Set<Long> childrenIds, Set<Long> nodesLinkedToMilestone, Long activeMilestoneId) {
		//TODO is there a collector for apache Multimap?
		MultiMap result = new MultiValueMap();
		List<Long> openedLibraries = (List<Long>) expansionCandidates.get(getClassName());
		if (!CollectionUtils.isEmpty(openedLibraries)) {
			DSL.select(selectLibraryContentLibraryId(),
				selectLibraryContentContentId())
				.from(getLibraryTableContent())
				.where(selectLibraryContentLibraryId().in(openedLibraries))
				.orderBy(selectLibraryContentOrder())
				.fetch()
				.stream()
				.forEach(r ->
					result.put(r.get(selectLibraryContentLibraryId()), r.get(selectLibraryContentContentId()))
				);
		}
		if (!NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId)) {
			Long lnId;
			for (Object resultValue : result.values()) {
				lnId = (Long) resultValue;
				if (nodesLinkedToMilestone.contains(lnId)) {
					childrenIds.add((lnId));
				}
			}
		} else {
			childrenIds.addAll(result.values());
		}
		return result;
	}

	protected MultiMap getLibraryNodeFatherChildrenMultiMap(MultiMap expansionCandidates, Set<Long> childrenIds, Set<Long> nodesLinkedToMilestone, Long activeMilestoneId) {
		MultiMap result = new MultiValueMap();
		List<Long> openedLibraryNodeIds = getOpenedLibraryNodeIds(expansionCandidates);
		if (!CollectionUtils.isEmpty(openedLibraryNodeIds)) {
			DSL
				.select(
					selectLNRelationshipAncestorId(),
					selectLNRelationshipDescendantId()
				)
				.from(getLNRelationshipTable())
				.where(selectLNRelationshipAncestorId().in(openedLibraryNodeIds))
				.orderBy(selectLNRelationshipContentOrder())
				.fetch()
				.stream()
				.forEach(r ->
					result.put(r.get(selectLNRelationshipAncestorId()), r.get(selectLNRelationshipDescendantId()))
				);
		}
		childrenIds.addAll(result.keySet());
		if (!NO_ACTIVE_MILESTONE_ID.equals(activeMilestoneId)) {
			Long lnId;
			for (Object resultValue : result.values()) {
				lnId = (Long) resultValue;
				if (nodesLinkedToMilestone.contains(lnId)) {
					childrenIds.add((lnId));
				}
			}
		} else {
			childrenIds.addAll(result.values());
		}
		return result;
	}

	private List<Long> getOpenedLibraryNodeIds(MultiMap expansionCandidates) {
		List<Long> openedLibraryNodeIds = new ArrayList<>();

		List<Long> folderId = (List<Long>) expansionCandidates.get(getFolderName());
		List<Long> nodeId = (List<Long>) expansionCandidates.get(getNodeName());

		if (!CollectionUtils.isEmpty(folderId)) {
			openedLibraryNodeIds.addAll(folderId);
		}
		if (!CollectionUtils.isEmpty(nodeId)) {
			openedLibraryNodeIds.addAll(nodeId);
		}

		return openedLibraryNodeIds;
	}

	protected JsTreeNode buildFolder(Long id, String name, String restype, int childCount, UserDto currentUser) {
		Map<String, Object> attr = new HashMap<>();
		State state;

		name = HtmlUtils.htmlEscape(name);

		attr.put(RES_ID, id);
		attr.put("resType", restype);
		attr.put("name", name);
		attr.put("id", getFolderName() + "-" + id);
		attr.put("rel", "folder");
		if (childCount > 0) {
			state = State.closed;
		} else {
			state = State.leaf;
		}
		return buildNode(name, state, attr, currentUser, NODE_WITHOUT_MILESTONES_ATTRIBUTE, "true");
	}

	protected JsTreeNode buildNode(String title, State state, Map<String, Object> attr, UserDto currentUser, Integer milestonesNumber, String isMilestoneModifiable) {
		JsTreeNode node = new JsTreeNode();
		node.setTitle(title);
		if (state != null) {
			node.setState(state);
		}
		node.setAttr(attr);

		//permissions set to false by default except for admin which have rights by definition
		EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
		for (PermissionWithMask permission : permissions) {
			node.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
		}
		if (!NODE_WITHOUT_MILESTONES_ATTRIBUTE.equals(milestonesNumber)) { // only for nodes which have 'milestones' attr
			node.addAttr("milestones", milestonesNumber);
		}
		// milestone attributes : libraries are yes-men
		node.addAttr("milestone-creatable-deletable", isMilestoneModifiable);
		node.addAttr("milestone-editable", isMilestoneModifiable);
		node.addAttr("wizards", new HashSet<String>());
		return node;
	}

	protected void buildHierarchy(Map<Long, JsTreeNode> jsTreeNodes, MultiMap fatherChildrenLibrary, MultiMap fatherChildrenEntity, Map<Long, JsTreeNode> allChildren, Long activeMilestoneId) {
		// First we iterate over the libraries and give them their children
		boolean openedLibrary = false;

		for (Map.Entry<Long, List<Long>> parentChildrenEntry : (Set<Map.Entry>) fatherChildrenLibrary.entrySet()) {
			Long parentKey = parentChildrenEntry.getKey();
			if (jsTreeNodes.containsKey(parentKey)) {
				builParentHierarchy(  openedLibrary,
				parentKey,  parentChildrenEntry, jsTreeNodes, fatherChildrenEntity,
					 allChildren, 	activeMilestoneId);
			}
		}
	}

	private void builParentHierarchy(boolean openedLibrary, Long parentKey, Map.Entry<Long, List<Long>> parentChildrenEntry, Map<Long, JsTreeNode> jsTreeNodes, MultiMap fatherChildrenEntity, Map<Long, JsTreeNode> allChildren, Long activeMilestoneId){
		for (Long childKey : parentChildrenEntry.getValue()) {
			if (passesMilestoneFilter(allChildren.get(childKey), activeMilestoneId)) {
				jsTreeNodes.get(parentKey).addChild(allChildren.get(childKey));
				openedLibrary = true;
			}
		}
		if (openedLibrary) {
			jsTreeNodes.get(parentKey).setState(State.open);
			buildSubHierarchy(jsTreeNodes.get(parentKey).getChildren(), fatherChildrenEntity, allChildren, activeMilestoneId);
		}
	}

	private void buildSubHierarchy(List<JsTreeNode> children, MultiMap fatherChildrenEntity, Map<Long, JsTreeNode> allChildren, Long activeMilestoneId) {
		// Then we iterate over the entities and give them their children
		boolean openedEntity = false;
		for (JsTreeNode jsTreeNodeChild : children) {
			if (fatherChildrenEntity.containsKey(jsTreeNodeChild.getAttr().get(RES_ID))) {
				buildSubHierarchyItems( openedEntity,jsTreeNodeChild, fatherChildrenEntity,  allChildren, activeMilestoneId);
				}
		}
	}

	private void buildSubHierarchyItems(boolean openedEntity, JsTreeNode jsTreeNodeChild, MultiMap fatherChildrenEntity, Map<Long, JsTreeNode> allChildren, Long activeMilestoneId){
		for (Long childKey : (ArrayList<Long>) fatherChildrenEntity.get(jsTreeNodeChild.getAttr().get(RES_ID))) {
			if (passesMilestoneFilter(allChildren.get(childKey), activeMilestoneId)) {
				jsTreeNodeChild.addChild(allChildren.get(childKey));
				openedEntity = true;
			}
		}
		if (openedEntity) {
			jsTreeNodeChild.setState(State.open);
			buildSubHierarchy(jsTreeNodeChild.getChildren(), fatherChildrenEntity, allChildren, activeMilestoneId);
		}
	}

	protected boolean nodeHasActiveMilestone(Set<Long> nodesLinkedToMilestone, Long libraryNodeId) {
		for (Long nodeId : nodesLinkedToMilestone) {
			if (libraryNodeId.equals(nodeId)) {
				return true;
			}
		}
		return false;
	}

	protected List<Long> findFilteredProjectIds(List<Long> readableProjectIds, String username) {
		return DSL.select(PROJECT_FILTER_ENTRY.PROJECT_ID)
			.from(PROJECT_FILTER)
			.join(PROJECT_FILTER_ENTRY).on(PROJECT_FILTER.PROJECT_FILTER_ID.eq(PROJECT_FILTER_ENTRY.FILTER_ID))
			.where(PROJECT_FILTER.USER_LOGIN.eq(username)).and(PROJECT_FILTER_ENTRY.PROJECT_ID.in(readableProjectIds))
			.fetch(PROJECT_FILTER_ENTRY.PROJECT_ID, Long.class);
	}

	protected boolean hasActiveFilter(String userName) {
		//first we must filter by global filter
		Record1<Boolean> record1 = DSL.select(PROJECT_FILTER.ACTIVATED)
			.from(PROJECT_FILTER)
			.where(PROJECT_FILTER.USER_LOGIN.eq(userName))
			.fetchOne();

		if (record1 == null) {
			return false;
		}
		return record1.get(PROJECT_FILTER.ACTIVATED);
	}

	private String buildResourceType(String classSimpleName) {
		String singleResourceType = HyphenedStringHelper.camelCaseToHyphened(classSimpleName);
		return singleResourceType.replaceAll("y$", "ies");
	}

	public void findWizards(List<Long> readableProjectIds, Map<Long, JsTreeNode> jsTreeNodes) {

		Map<Long, Set<String>> pluginByLibraryId = DSL.select(getProjectLibraryColumn(), LIBRARY_PLUGIN_BINDING.PLUGIN_ID)
			.from(PROJECT)
			.join(getLibraryTable()).using(getProjectLibraryColumn())
			.join(LIBRARY_PLUGIN_BINDING).on(LIBRARY_PLUGIN_BINDING.LIBRARY_ID.eq(getProjectLibraryColumn()).and(LIBRARY_PLUGIN_BINDING.LIBRARY_TYPE.eq(getLibraryPluginType())))
			.where(PROJECT.PROJECT_ID.in(readableProjectIds).and((PROJECT.PROJECT_TYPE).eq(PROJECT_TYPE)))
			.fetch()
			.stream()
			.collect(Collectors.groupingBy(r -> r.get(getProjectLibraryColumn()), mapping(r -> r.get(LIBRARY_PLUGIN_BINDING.PLUGIN_ID), toSet())));

		pluginByLibraryId.forEach((libId, pluginIds) -> {
			if (jsTreeNodes.get(libId) != null) {
				jsTreeNodes.get(libId).addAttr("wizards", pluginIds);
			}
		});
	}

	private List<Long> getMilestonesModifiable() {
		return DSL.select(MILESTONE.MILESTONE_ID)
			.from(MILESTONE)
			.where(MILESTONE.STATUS.eq(MILESTONE_STATUS_IN_PROGRESS)).or(MILESTONE.STATUS.eq(MILESTONE_STATUS_FINISHED))
			.fetch(MILESTONE.MILESTONE_ID, Long.class);
	}

	protected String getMessage(String key) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(key, null, locale);
	}

	protected String getMessage(String code, Object[] args) {
		Locale locale = LocaleContextHolder.getLocale();
		return messageSource.getMessage(code, args, locale);
	}

	// *************************************** get Stuff From Specific Workspace ***************************************

	protected abstract Map<Long, List<Long>> findAllMilestonesForLN();

	protected abstract TableLike<?> getLibraryTable();

	protected abstract TableLike<?> getLibraryTableContent();

	protected abstract Field<Long> selectLibraryContentContentId();

	protected abstract Field<Integer> selectLibraryContentOrder();

	protected abstract Field<Long> selectLibraryContentLibraryId();

	protected abstract Field<Long> selectLibraryId();

	protected abstract Field<Long> selectLNRelationshipAncestorId();

	protected abstract Field<Long> selectLNRelationshipDescendantId();

	protected boolean isMilestoneModifiable(String rawStatus) {
		if (StringUtils.isBlank(rawStatus)) {
			return true;
		}

		MilestoneStatus status = EnumUtils.getEnum(MilestoneStatus.class, rawStatus);
		return status.isAllowObjectModification();
	}

	protected abstract Field<Long> getProjectLibraryColumn();

	protected abstract Field<Integer> selectLNRelationshipContentOrder();

	protected abstract TableLike<?> getLNRelationshipTable();

	protected abstract String getClassName();

	protected abstract String getLibraryClassName();

	protected abstract String getRel();

	protected abstract String getFolderName();

	protected abstract Object getNodeName();

	protected abstract String getLibraryPluginType();

	protected String getResType() {
		return buildResourceType(getClassName());
	}

	protected abstract Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, MultiMap expansionCandidates, UserDto currentUser, Map<Long, List<Long>> allMilestonesForLN, List<Long> milestonesModifiable, Long activeMilestoneId);

	protected abstract Field<Long> getMilestoneLibraryNodeId();

	protected abstract TableLike<?> getMilestoneLibraryNodeTable();

	protected abstract Field<Long> getMilestoneId();

	protected abstract HibernateEntityDao hibernateFolderDao();

	protected abstract Set<Long> findLNByMilestoneId(Long activeMilestoneId);

	protected abstract boolean passesMilestoneFilter(JsTreeNode node, Long activeMilestoneId);

}
