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
package org.squashtest.tm.service.internal.customreport;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.TableLike;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.squashtest.tm.api.security.acls.Permission;
import org.squashtest.tm.domain.customreport.CustomReportLibrary;
import org.squashtest.tm.domain.customreport.CustomReportTreeDefinition;
import org.squashtest.tm.jooq.domain.tables.CrlnRelationship;
import org.squashtest.tm.jooq.domain.tables.CustomReportLibraryNode;
import org.squashtest.tm.service.internal.dto.PermissionWithMask;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.security.PermissionEvaluationService;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.count;
import static org.squashtest.tm.api.security.acls.Permission.*;
import static org.squashtest.tm.domain.project.Project.PROJECT_TYPE;
import static org.squashtest.tm.jooq.domain.Tables.*;
import static org.squashtest.tm.service.internal.dto.PermissionWithMask.findByMask;

@Service("customReportWorkspaceDisplayService")
@Transactional(readOnly = true)
public class CustomReportWorkspaceDisplayService {

	private static final String RES_ID = "resId";

	@Inject
	DSLContext DSL;

	@Inject
	PermissionEvaluationService permissionEvaluationService;

	private static final String ROLE_ADMIN = "ROLE_ADMIN";
	private static final Permission[] NODE_PERMISSIONS = {WRITE, CREATE, DELETE, EXECUTE, EXPORT};
	private static final String[] PERM_NAMES = {WRITE.name(), CREATE.name(), DELETE.name(), EXECUTE.name(), EXPORT.name()};

	private static final CustomReportLibraryNode CRLN = CUSTOM_REPORT_LIBRARY_NODE.as("CRLN");
	private static final CrlnRelationship CRLNR = CRLN_RELATIONSHIP.as("CRLNR");

	public Collection<JsTreeNode> findAllLibraries(List<Long> readableProjectIds, UserDto currentUser, MultiMap expansionCandidates) {
		Set<Long> childrenIds = new HashSet<>();

		MultiMap libraryFatherChildrenMultiMap = new MultiValueMap();
		MultiMap libraryNodeFatherChildrenMultiMap = new MultiValueMap();

		getFatherChildrenMultiMaps(expansionCandidates, childrenIds, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap);

		Map<Long, JsTreeNode> libraryChildrenMap = getLibraryChildrenMap(childrenIds, currentUser);
		Map<Long, JsTreeNode> jsTreeNodes = doFindLibraries(readableProjectIds, currentUser);
		buildHierarchy(jsTreeNodes, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap, libraryChildrenMap);

		if (currentUser.isNotAdmin()) {
			findPermissionMap(currentUser, jsTreeNodes);
		}

		return jsTreeNodes.values();

	}

	public Collection<JsTreeNode> getNodeContent(Long nodeId, UserDto currentUser) {
		Set<Long> childrenIds = new HashSet<>();

		MultiMap libraryFatherChildrenMultiMap = new MultiValueMap();
		MultiMap libraryNodeFatherChildrenMultiMap = new MultiValueMap();

		MultiMap expansionCandidates = new MultiValueMap();
		expansionCandidates.put(new Object(), nodeId);

		getFatherChildrenMultiMaps(expansionCandidates, childrenIds, libraryFatherChildrenMultiMap, libraryNodeFatherChildrenMultiMap);

		Map<Long, JsTreeNode> libraryChildrenMap = getLibraryChildrenMap(childrenIds, currentUser);

		if (currentUser.isNotAdmin()) {
			findNodeChildrenPermissionMap(currentUser, libraryChildrenMap, nodeId);
		}

		return libraryChildrenMap.values();
	}

	private void getFatherChildrenMultiMaps(MultiMap expansionCandidates, Set<Long> childrenIds,
											MultiMap libraryFatherChildrenMultiMap, MultiMap libraryNodeFatherChildrenMultiMap) {
		List<Long> openedLibraries = new ArrayList<>(expansionCandidates.values());
		if (!CollectionUtils.isEmpty(openedLibraries)) {
			DSL.select(selectLibraryNodeRelationshipAncestorId(),
				selectLibraryNodeRelationshipDescendantId())
				.from(getLibraryNodeRelationshipTable())
				.where(selectLibraryNodeRelationshipAncestorId().in(openedLibraries))
				.groupBy(selectLibraryNodeRelationshipAncestorId(), selectLibraryNodeRelationshipDescendantId(), selectLibraryNodeRelationshipContentOrder())
				.orderBy(selectLibraryNodeRelationshipContentOrder())
				.fetch()
				.forEach(r -> {
						if (expansionCandidates.get(getLibrarySimpleClassName()) != null && ((List<Long>) expansionCandidates.get(getLibrarySimpleClassName())).contains(r.get(selectLibraryNodeRelationshipAncestorId()))) {
							libraryFatherChildrenMultiMap.put(r.get(selectLibraryNodeRelationshipAncestorId()), r.get(selectLibraryNodeRelationshipDescendantId()));
						} else {
							libraryNodeFatherChildrenMultiMap.put(r.get(selectLibraryNodeRelationshipAncestorId()), r.get(selectLibraryNodeRelationshipDescendantId()));
						}
					}
				);
		}
		childrenIds.addAll(libraryFatherChildrenMultiMap.values());
		childrenIds.addAll(libraryNodeFatherChildrenMultiMap.values());

	}

	private Map<Long, JsTreeNode> getLibraryChildrenMap(Set<Long> childrenIds, UserDto currentUser) {
		if (childrenIds.isEmpty()) {
			return new HashMap<>();
		}

		return DSL.select(
			CRLN.CRLN_ID,
			CRLN.NAME,
			CRLN.ENTITY_TYPE,
			count(CRLNR.ANCESTOR_ID).as("ITERATION_COUNT")
		)
			.from(CRLN)
			.leftJoin(CRLNR).on(CRLN.CRLN_ID.eq(CRLNR.ANCESTOR_ID))
			.where(CRLN.CRLN_ID.in(childrenIds))
			.groupBy(CRLN.CRLN_ID, CRLN.NAME, CRLN.ENTITY_TYPE, CRLNR.ANCESTOR_ID)
			.fetch()
			.stream()
			.map(r -> build(r.get(CRLN.CRLN_ID), r.get(CRLN.NAME), r.get(CRLN.ENTITY_TYPE), r.get("ITERATION_COUNT", Integer.class), currentUser))
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get(RES_ID), Function.identity()));

	}

	private Map<Long, JsTreeNode> doFindLibraries(List<Long> readableProjectIds, UserDto currentUser) {
		List<Long> filteredProjectIds;
		if (hasActiveFilter(currentUser.getUsername())) {
			filteredProjectIds = findFilteredProjectIds(readableProjectIds, currentUser.getUsername());
		} else {
			filteredProjectIds = readableProjectIds;
		}

		return DSL.select(
			selectLibraryNodeLibraryNodeId(),
			selectLibraryNodeLibraryNodeName(),
			count(selectLibraryNodeRelationshipAncestorId()).as("COUNT_CHILD"))
			.from(getLibraryNodeTable())
			.join(PROJECT).using(selectLibraryNodeLibraryId())
			.leftJoin(getLibraryNodeRelationshipTable()).on(selectLibraryNodeLibraryNodeId().eq(selectLibraryNodeRelationshipAncestorId()))
			.where(PROJECT.PROJECT_ID.in(filteredProjectIds))
			.and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE))
			.and(selectLibraryNodeLibraryNodeEntityType().eq("LIBRARY"))
			.groupBy(selectLibraryNodeLibraryId(), selectLibraryNodeLibraryNodeId(), selectLibraryNodeLibraryNodeName(), selectLibraryNodeRelationshipAncestorId())
			.fetch()
			.stream()
			.map(r -> build(r.get(selectLibraryNodeLibraryNodeId()), r.get(selectLibraryNodeLibraryNodeName()), "LIBRARY", r.get("COUNT_CHILD", Integer.class), currentUser))
			.collect(Collectors.toMap(node -> (Long) node.getAttr().get(RES_ID), Function.identity(),
				(u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u));
				},
				LinkedHashMap::new));
	}

	private void buildHierarchy(Map<Long, JsTreeNode> jsTreeNodes, MultiMap fatherChildrenLibrary, MultiMap fatherChildrenEntity, Map<Long, JsTreeNode> allChildren) {
		// First we iterate over the libraries and give them their children
		boolean openedLibrary = false;

		for (Map.Entry<Long, List<Long>> parentChildrenEntry : (Set<Map.Entry>) fatherChildrenLibrary.entrySet()) {
			Long parentKey = parentChildrenEntry.getKey();
			if (jsTreeNodes.containsKey(parentKey)) {
				for (Long childKey : parentChildrenEntry.getValue()) {
					jsTreeNodes.get(parentKey).addChild(allChildren.get(childKey));
					openedLibrary = true;
				}
				if (openedLibrary) {
					setNodeOpen(jsTreeNodes.get(parentKey));
					buildSubHierarchy(jsTreeNodes.get(parentKey).getChildren(), fatherChildrenEntity, allChildren);
				}
			}
		}
	}

	private void buildSubHierarchy(List<JsTreeNode> children, MultiMap fatherChildrenEntity, Map<Long, JsTreeNode> allChildren) {
		// Then we iterate over the entities and give them their children
		boolean openedEntity = false;
		for (JsTreeNode jsTreeNodeChild : children) {
			if (fatherChildrenEntity.containsKey(jsTreeNodeChild.getAttr().get(RES_ID))) {
				for (Long childKey : (ArrayList<Long>) fatherChildrenEntity.get(jsTreeNodeChild.getAttr().get(RES_ID))) {
					jsTreeNodeChild.addChild(allChildren.get(childKey));
					openedEntity = true;
				}
				if (openedEntity) {
					setNodeOpen(jsTreeNodeChild);
					buildSubHierarchy(jsTreeNodeChild.getChildren(), fatherChildrenEntity, allChildren);
				}
			}
		}
	}

	private List<Long> findFilteredProjectIds(List<Long> readableProjectIds, String username) {
		return DSL.select(PROJECT_FILTER_ENTRY.PROJECT_ID)
			.from(PROJECT_FILTER)
			.join(PROJECT_FILTER_ENTRY).on(PROJECT_FILTER.PROJECT_FILTER_ID.eq(PROJECT_FILTER_ENTRY.FILTER_ID))
			.where(PROJECT_FILTER.USER_LOGIN.eq(username)).and(PROJECT_FILTER_ENTRY.PROJECT_ID.in(readableProjectIds))
			.fetch(PROJECT_FILTER_ENTRY.PROJECT_ID, Long.class);
	}

	private boolean hasActiveFilter(String userName) {
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

	public JsTreeNode build(Long nodeId, String nodeName, String nodeType, Integer iterationCount, UserDto currentUser) {

		nodeName = HtmlUtils.htmlEscape(nodeName);

		JsTreeNode builtNode = new JsTreeNode();
		builtNode.setTitle(nodeName);
		builtNode.addAttr(RES_ID, nodeId);
		builtNode.addAttr("name", nodeName);

		//No milestone for custom report tree in first version so yes for all perm
		builtNode.addAttr("milestone-creatable-deletable", "true");
		builtNode.addAttr("milestone-editable", "true");

		//permissions set to false by default except for admin which have rights by definition
		EnumSet<PermissionWithMask> permissions = EnumSet.allOf(PermissionWithMask.class);
		for (PermissionWithMask permission : permissions) {
			builtNode.addAttr(permission.getQuality(), String.valueOf(currentUser.isAdmin()));
		}

		//A visitor would be elegant here and allow interface type development but we don't want hibernate to fetch each linked entity
		//for each node and we don't want subclass for each node type. sooooo the good old switch on enumerated type will do the job...
		CustomReportTreeDefinition entityType = CustomReportTreeDefinition.valueOf(nodeType);//NO SONAR the argument for this method is a CustomReportLibraryNode so entity type is a CustomReportTreeDefinition

		switch (entityType) {
			case LIBRARY:
				doLibraryBuild(builtNode, nodeId, iterationCount);
				break;
			case FOLDER:
				doFolderBuild(builtNode, nodeId, iterationCount);
				break;
			case CHART:
				doChartBuild(builtNode, nodeId);
				break;
			case REPORT:
				doReportBuild(builtNode, nodeId);
				break;
			case DASHBOARD:
				doDashboardBuild(builtNode, nodeId);
				break;
			default:
				throw new UnsupportedOperationException("The node builder isn't implemented for node of type : " + entityType);
		}

		return builtNode;
	}

	private void doLibraryBuild(JsTreeNode builtNode, Long nodeId, Integer iterationCount) {
		setNodeHTMLId(builtNode, "CustomReportLibrary-" + nodeId);
		setNodeRel(builtNode, "drive");
		setNodeResType(builtNode, "custom-report-libraries");
		setStateForNodeContainer(builtNode, iterationCount);
	}

	private void doFolderBuild(JsTreeNode builtNode, Long nodeId, Integer iterationCount) {
		setNodeHTMLId(builtNode, "CustomReportFolder-" + nodeId);
		setNodeRel(builtNode, "folder");
		setNodeResType(builtNode, "custom-report-folders");
		setStateForNodeContainer(builtNode, iterationCount);
	}

	private void doChartBuild(JsTreeNode builtNode, Long nodeId) {
		setNodeHTMLId(builtNode, "CustomReportChart-" + nodeId);
		setNodeRel(builtNode, "chart");
		setNodeResType(builtNode, "custom-report-chart");
		setNodeLeaf(builtNode);
	}

	private void doReportBuild(JsTreeNode builtNode, Long nodeId) {
		setNodeHTMLId(builtNode, "CustomReportReport-" + nodeId);
		setNodeRel(builtNode, "report");
		setNodeResType(builtNode, "custom-report-report");
		setNodeLeaf(builtNode);
	}

	private void doDashboardBuild(JsTreeNode builtNode, Long nodeId) {
		setNodeHTMLId(builtNode, "CustomReportDashboard-" + nodeId);
		setNodeRel(builtNode, "dashboard");
		setNodeResType(builtNode, "custom-report-dashboard");
		setNodeLeaf(builtNode);
	}

	public void findPermissionMap(UserDto currentUser, Map<Long, JsTreeNode> jsTreeNodes) {
		Set<Long> libraryIds = jsTreeNodes.keySet();

		DSL
			.selectDistinct(selectLibraryNodeLibraryNodeId(), ACL_GROUP_PERMISSION.PERMISSION_MASK)
			.from(getLibraryNodeTable())
			.join(PROJECT).on(getProjectLibraryColumn().eq(selectLibraryNodeLibraryId()))
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.IDENTITY.eq(selectLibraryNodeLibraryId()))
			.join(ACL_RESPONSIBILITY_SCOPE_ENTRY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_GROUP_PERMISSION).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.eq(ACL_GROUP_PERMISSION.ACL_GROUP_ID))
			.join(ACL_CLASS).on(ACL_GROUP_PERMISSION.CLASS_ID.eq(ACL_CLASS.ID).and(ACL_CLASS.CLASSNAME.eq(getLibraryClassName())))
			.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(currentUser.getPartyIds())).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE)).and(selectLibraryNodeLibraryNodeId().in(libraryIds))
			.fetch()
			.stream()
			.collect(groupingBy(
				r -> r.getValue(selectLibraryNodeLibraryNodeId()),
				mapping(
					r -> r.getValue(ACL_GROUP_PERMISSION.PERMISSION_MASK),
					toList()
				)
			)).forEach((Long nodeId, List<Integer> masks) -> {
			JsTreeNode node = jsTreeNodes.get(nodeId);
			givePermissions(node, masks);
		});
	}

	protected void findNodeChildrenPermissionMap(UserDto currentUser, Map<Long, JsTreeNode> libraryChildrenMap, Long nodeId) {
		List<Integer> masks = DSL
			.selectDistinct(ACL_GROUP_PERMISSION.PERMISSION_MASK)
			.from(getLibraryNodeTable())
			.join(PROJECT).on(getProjectLibraryColumn().eq(selectLibraryNodeLibraryId()))
			.join(ACL_OBJECT_IDENTITY).on(ACL_OBJECT_IDENTITY.IDENTITY.eq(selectLibraryNodeLibraryId()))
			.join(ACL_RESPONSIBILITY_SCOPE_ENTRY).on(ACL_OBJECT_IDENTITY.ID.eq(ACL_RESPONSIBILITY_SCOPE_ENTRY.OBJECT_IDENTITY_ID))
			.join(ACL_GROUP_PERMISSION).on(ACL_RESPONSIBILITY_SCOPE_ENTRY.ACL_GROUP_ID.eq(ACL_GROUP_PERMISSION.ACL_GROUP_ID))
			.join(ACL_CLASS).on(ACL_GROUP_PERMISSION.CLASS_ID.eq(ACL_CLASS.ID).and(ACL_CLASS.CLASSNAME.eq(getLibraryClassName())))
			.where(ACL_RESPONSIBILITY_SCOPE_ENTRY.PARTY_ID.in(currentUser.getPartyIds())).and(PROJECT.PROJECT_TYPE.eq(PROJECT_TYPE)).and(selectLibraryNodeLibraryNodeId().in(nodeId))
			.fetch(ACL_GROUP_PERMISSION.PERMISSION_MASK, Integer.class);

		for (JsTreeNode node : libraryChildrenMap.values()) {
			givePermissions(node, masks);
		}
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

	private void setNodeRel(JsTreeNode builtNode, String rel) {
		builtNode.addAttr("rel", rel);
	}

	private void setNodeResType(JsTreeNode builtNode, String resType) {
		builtNode.addAttr("resType", resType);
	}

	private void setNodeLeaf(JsTreeNode builtNode) {
		builtNode.setState(JsTreeNode.State.leaf);
	}

	private void setNodeOpen(JsTreeNode builtNode) {
		builtNode.setState(JsTreeNode.State.open);
	}

	private void setNodeHTMLId(JsTreeNode builtNode, String id) {
		builtNode.addAttr("id", id);
	}

	private void setStateForNodeContainer(JsTreeNode builtNode, Integer iterationCount) {
		if (iterationCount > 0) {
			builtNode.setState(JsTreeNode.State.closed);
		} else {
			builtNode.setState(JsTreeNode.State.leaf);
		}
	}

	private String getLibrarySimpleClassName() {
		return CustomReportLibrary.class.getSimpleName();
	}

	private String getLibraryClassName() {
		return CustomReportLibrary.class.getName();
	}

	private TableLike<?> getLibraryNodeRelationshipTable() {
		return CRLN_RELATIONSHIP;
	}

	private TableLike<?> getLibraryNodeTable() {
		return CRLN;
	}

	private Field<Long> selectLibraryNodeRelationshipAncestorId() {
		return CRLN_RELATIONSHIP.ANCESTOR_ID;
	}

	private Field<Long> selectLibraryNodeRelationshipDescendantId() {
		return CRLN_RELATIONSHIP.DESCENDANT_ID;
	}

	private Field<Long> selectLibraryNodeRelationshipContentOrder() {
		return CRLN_RELATIONSHIP.CONTENT_ORDER;
	}

	private Field<Long> selectLibraryNodeLibraryId() {
		return CRLN.CRL_ID;
	}

	private Field<String> selectLibraryNodeLibraryNodeName() {
		return CRLN.NAME;
	}

	private Field<String> selectLibraryNodeLibraryNodeEntityType() {
		return CRLN.ENTITY_TYPE;
	}

	private Field<Long> selectLibraryNodeLibraryNodeId() {
		return CRLN.CRLN_ID;
	}

	protected Field<Long> getProjectLibraryColumn() {
		return PROJECT.CRL_ID;
	}
}
