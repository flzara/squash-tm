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
package org.squashtest.tm.web.internal.controller.customreport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.squashtest.tm.domain.customfield.BindableEntity;
import org.squashtest.tm.domain.customfield.CustomFieldBinding;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.customfield.CustomFieldBindingFinderService;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.service.deletion.*;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.customreport.CustomReportWorkspaceDisplayService;
import org.squashtest.tm.service.internal.dto.UserDto;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;
import org.squashtest.tm.service.user.UserAccountService;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.builder.CustomReportListTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.CustomReportTreeNodeBuilder;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This controller is dedicated to the operations in the tree of Custom Reports
 * It's bloated because the tree client side is made for {@link LibraryNode}.
 * The tree send several distinct requests for the different type of node.
 * This organisation had sense with the initial tree model (tree node and business entity are in same object),
 * but isn't optimized for the new tree model (tree node and business entity are distinct objects)
 * As we haven't the time to redefine the client tree, this controller just follow the client jstree requests...
 * Also, no milestones for v1, but we require active milestone as it probably will be here someday and the tree give it anyway
 *
 * @author jthebault
 */
@Controller
@RequestMapping("/custom-report-browser")
public class CustomReportNavigationController {

	public static final Logger LOGGER = LoggerFactory.getLogger(CustomReportNavigationController.class);
	private static final String NODE_IDS = "nodeIds[]";
	private static final String DESTINATION_ID = "destinationId";
	@Inject
	private CustomReportWorkspaceService workspaceService;
	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;
	@Inject
	private CustomReportWorkspaceDisplayService customReportWorkspaceDisplayService;
	@Inject
	private CustomReportListTreeNodeBuilder listBuilder;
	@Inject
	@Named("customReport.nodeBuilder")
	private Provider<CustomReportTreeNodeBuilder> builderProvider;
	@Inject
	private UserAccountService userAccountService;
	@Inject
	private CustomFieldBindingFinderService customFieldBindingFinderService;

	@Inject
	private PrivateCustomFieldValueService customValueService;


	//----- CREATE NODE METHODS -----

	@ResponseBody
	@RequestMapping(value = "/drives/{libraryId}/content/new-folder", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewFolderInLibrary(@PathVariable Long libraryId, @Valid @RequestBody CustomReportFolder customReportFolder) {
		JsTreeNode node = createNewCustomReportLibraryNode(libraryId, customReportFolder);
		generateCuf(customReportFolder);
		return node;
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{folderId}/content/new-folder", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewFolderInFolder(@PathVariable Long folderId, @Valid @RequestBody CustomReportFolder customReportFolder) {
		JsTreeNode node = createNewCustomReportLibraryNode(folderId, customReportFolder);
		generateCuf(customReportFolder);
		return node;
	}

	private void generateCuf(CustomReportFolder newFolder){
		List<CustomFieldBinding> projectsBindings = customFieldBindingFinderService.findCustomFieldsForProjectAndEntity(newFolder.getProject().getId(), BindableEntity.CUSTOM_REPORT_FOLDER);
		for(CustomFieldBinding binding: projectsBindings){
			customValueService.cascadeCustomFieldValuesCreationNotCreatedFolderYet(binding, newFolder);
		}
	}


	@ResponseBody
	@RequestMapping(value = "/drives/{libraryId}/content/new-dashboard", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewDashboardInLibrary(@PathVariable Long libraryId, @Valid @RequestBody CustomReportDashboard customReportDashboard) {
		return createNewCustomReportLibraryNode(libraryId, customReportDashboard);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{folderId}/content/new-dashboard", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewDashboardInFolder(@PathVariable Long folderId, @Valid @RequestBody CustomReportDashboard customReportDashboard) {
		return createNewCustomReportLibraryNode(folderId, customReportDashboard);
	}

	//-------------- SHOW-NODE-CHILDREN METHODS ---------------

	@ResponseBody
	@RequestMapping(value = "/drives/{nodeId}/content", method = RequestMethod.GET)
	public List<JsTreeNode> getRootContentTreeModel(@PathVariable long nodeId) {
		return getNodeContent(nodeId);

	}

	@ResponseBody
	@RequestMapping(value = "/folders/{nodeId}/content", method = RequestMethod.GET)
	public List<JsTreeNode> getFolderContentTreeModel(@PathVariable long nodeId) {
		return getNodeContent(nodeId);
	}

	@ResponseBody
	@RequestMapping(value = "/dashboard/{nodeId}/content", method = RequestMethod.GET)
	public List<JsTreeNode> getDashboardContentTreeModel(@PathVariable long nodeId) {
		return getNodeContent(nodeId);
	}

	//-------------- COPY-NODES ------------------------------
	//Two Request mappings for the same function, as we have to follow the jstree logic... or re do the tree :-(

	@ResponseBody
	@RequestMapping(value = "/folders/{destinationId}/content/new", method = RequestMethod.POST, params = {NODE_IDS})
	public List<JsTreeNode> copyNodesTofolder(@RequestParam(NODE_IDS) Long[] nodeIds,
	                                          @PathVariable(DESTINATION_ID) long destinationId) {
		return copyNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/drives/{destinationId}/content/new", method = RequestMethod.POST, params = {NODE_IDS})
	public List<JsTreeNode> copyNodesToDrives(@RequestParam(NODE_IDS) Long[] nodeIds,
	                                          @PathVariable(DESTINATION_ID) long destinationId) {
		return copyNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{destinationId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public void moveNodesToFolderWithPosition(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
	                                          @PathVariable(DESTINATION_ID) long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/drives/{destinationId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public void moveNodesToDriveWithPosition(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
	                                         @PathVariable(DESTINATION_ID) long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{destinationId}/content/{nodeIds}", method = RequestMethod.PUT)
	public void moveNodesToFolder(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
	                              @PathVariable(DESTINATION_ID) long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/drives/{destinationId}/content/{nodeIds}", method = RequestMethod.PUT)
	public void moveNodesToDrive(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
	                             @PathVariable(DESTINATION_ID) long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	//-------------- DELETE-SIMULATION METHODS ---------------

	/**
	 * No return for V1, we delete all nodes inside container.
	 *
	 * @param nodeIds
	 * @param activeMilestone
	 * @param locale
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/content/{nodeIds}/deletion-simulation", method = RequestMethod.GET)
	public Messages simulateNodeDeletion() {
		return new Messages();    // from TM 1.13 until further notice the simulation doesn't do anything
	}

	//-------------- DELETE METHOD ---------------------------

	@ResponseBody
	@RequestMapping(value = "/content/{nodeIds}", method = RequestMethod.DELETE)
	public OperationReport confirmNodeDeletion(
		@PathVariable(RequestParams.NODE_IDS) List<Long> nodeIds) {

		OperationReport report = customReportLibraryNodeService.delete(nodeIds);
		logOperations(report);
		return report;
	}


	//-------------- PRIVATE STUFF ---------------------------
	private JsTreeNode createNewCustomReportLibraryNode(Long libraryId, TreeEntity entity) {
		CustomReportLibraryNode newNode = customReportLibraryNodeService.createNewNode(libraryId, entity);
		JsTreeNode node = builderProvider.get().build(newNode);
		return node;
	}

	private void moveNodes(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds, @PathVariable(DESTINATION_ID) long destinationId) {
		customReportLibraryNodeService.moveNodes(Arrays.asList(nodeIds), destinationId);
	}

	private List<JsTreeNode> copyNodes(@RequestParam(NODE_IDS) Long[] nodeIds, @PathVariable(DESTINATION_ID) long destinationId) {
		List<TreeLibraryNode> nodeList;
		nodeList = customReportLibraryNodeService.copyNodes(Arrays.asList(nodeIds), destinationId);
		return listBuilder.build(nodeList);
	}

	private List<JsTreeNode> getNodeContent(Long nodeId) {
		UserDto currentUser = userAccountService.findCurrentUserDto();
		return new ArrayList<>(customReportWorkspaceDisplayService.getNodeContent(nodeId, currentUser));
	}

	private void logOperations(OperationReport report) {
		for (Node deletedNode : report.getRemoved()) {
			LOGGER.info("The node #{} was removed", deletedNode.getResid());
		}
		for (NodeMovement movedNode : report.getMoved()) {
			LOGGER.info("The nodes #{} were moved to node #{}",
				movedNode.getMoved().stream().map(Node::getResid).collect(Collectors.toList()),
				movedNode.getDest().getResid());
		}
		for (NodeRenaming renamedNode : report.getRenamed()) {
			LOGGER.info("The node #{} was renamed to {}", renamedNode.getNode().getResid(), renamedNode.getName());
		}
		for (NodeReferenceChanged nodeReferenceChanged : report.getReferenceChanges()) {
			LOGGER.info("The node #{} reference was changed to {}", nodeReferenceChanged.getNode().getResid(), nodeReferenceChanged.getReference());
		}
	}

	//Class for messageCollection

	protected static class Messages {

		private Collection<String> messageCollection = new ArrayList<>();

		public Messages() {
			super();
		}

		public void addMessage(String msg) {
			this.messageCollection.add(msg);
		}

		public Collection<String> getMessageCollection() {
			return this.messageCollection;
		}

	}

}
