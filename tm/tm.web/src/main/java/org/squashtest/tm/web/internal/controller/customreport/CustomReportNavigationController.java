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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.squashtest.tm.domain.customreport.CustomReportDashboard;
import org.squashtest.tm.domain.customreport.CustomReportFolder;
import org.squashtest.tm.domain.customreport.CustomReportLibraryNode;
import org.squashtest.tm.domain.library.LibraryNode;
import org.squashtest.tm.domain.tree.TreeEntity;
import org.squashtest.tm.domain.tree.TreeLibraryNode;
import org.squashtest.tm.service.customreport.CustomReportLibraryNodeService;
import org.squashtest.tm.service.customreport.CustomReportWorkspaceService;
import org.squashtest.tm.service.deletion.OperationReport;
import org.squashtest.tm.web.internal.controller.RequestParams;
import org.squashtest.tm.web.internal.model.builder.CustomReportListTreeNodeBuilder;
import org.squashtest.tm.web.internal.model.builder.CustomReportTreeNodeBuilder;
import org.squashtest.tm.service.internal.dto.json.JsTreeNode;

/**
 * This controller is dedicated to the operations in the tree of Custom Reports
 * It's bloated because the tree client side is made for {@link LibraryNode}.
 * The tree send several distinct requests for the different type of node.
 * This organisation had sense with the initial tree model (tree node and business entity are in same object),
 * but isn't optimized for the new tree model (tree node and business entity are distinct objects)
 * As we haven't the time to redefine the client tree, this controller just follow the client jstree requests...
 * Also, no milestones for v1, but we require active milestone as it probably will be here someday and the tree give it anyway
 * @author jthebault
 *
 */
@Controller
@RequestMapping("/custom-report-browser")
public class CustomReportNavigationController {

	@Inject
	private CustomReportWorkspaceService workspaceService;

	@Inject
	private CustomReportLibraryNodeService customReportLibraryNodeService;

	@Inject
	private CustomReportListTreeNodeBuilder listBuilder;

	@Inject
	@Named("customReport.nodeBuilder")
	private Provider<CustomReportTreeNodeBuilder> builderProvider;

	public static final Logger LOGGER = LoggerFactory.getLogger(CustomReportNavigationController.class);

	//----- CREATE NODE METHODS -----

	@ResponseBody
	@RequestMapping(value = "/drives/{libraryId}/content/new-folder", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewFolderInLibrary(@PathVariable Long libraryId, @Valid @RequestBody CustomReportFolder customReportFolder){
		return createNewCustomReportLibraryNode(libraryId, customReportFolder);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{folderId}/content/new-folder", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewFolderInFolder(@PathVariable Long folderId, @Valid @RequestBody CustomReportFolder customReportFolder){
		return createNewCustomReportLibraryNode(folderId, customReportFolder);
	}

	@ResponseBody
	@RequestMapping(value = "/drives/{libraryId}/content/new-dashboard", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewDashboardInLibrary(@PathVariable Long libraryId, @Valid @RequestBody CustomReportDashboard customReportDashboard){
		return createNewCustomReportLibraryNode(libraryId, customReportDashboard);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{folderId}/content/new-dashboard", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public JsTreeNode createNewDashboardInFolder(@PathVariable Long folderId, @Valid @RequestBody CustomReportDashboard customReportDashboard){
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
	@RequestMapping(value = "/folders/{destinationId}/content/new", method = RequestMethod.POST, params = {"nodeIds[]"})
	public List<JsTreeNode> copyNodesTofolder(@RequestParam("nodeIds[]") Long[] nodeIds,
											  @PathVariable("destinationId") long destinationId) {
		return copyNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/drives/{destinationId}/content/new", method = RequestMethod.POST, params = {"nodeIds[]"})
	public List<JsTreeNode> copyNodesToDrives(@RequestParam("nodeIds[]") Long[] nodeIds,
											  @PathVariable("destinationId") long destinationId) {
		return copyNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{destinationId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public void moveNodesToFolderWithPosition(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
											  @PathVariable("destinationId") long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/drives/{destinationId}/content/{nodeIds}/{position}", method = RequestMethod.PUT)
	public void moveNodesToDriveWithPosition(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
											 @PathVariable("destinationId") long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/folders/{destinationId}/content/{nodeIds}", method = RequestMethod.PUT)
	public void moveNodesToFolder(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
								  @PathVariable("destinationId") long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	@ResponseBody
	@RequestMapping(value = "/drives/{destinationId}/content/{nodeIds}", method = RequestMethod.PUT)
	public void moveNodesToDrive(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds,
								 @PathVariable("destinationId") long destinationId) {
		moveNodes(nodeIds, destinationId);
	}

	//-------------- DELETE-SIMULATION METHODS ---------------

	/**
	 * No return for V1, we delete all nodes inside container.
	 * @param nodeIds
	 * @param activeMilestone
	 * @param locale
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/content/{nodeIds}/deletion-simulation", method = RequestMethod.GET)
	public Messages simulateNodeDeletion() {
		return new Messages();	// from TM 1.13 until further notice the simulation doesn't do anything
	}

	//-------------- DELETE METHOD ---------------------------

	@ResponseBody
	@RequestMapping(value = "/content/{nodeIds}", method = RequestMethod.DELETE)
	public OperationReport confirmNodeDeletion(
@PathVariable(RequestParams.NODE_IDS) List<Long> nodeIds) {

		return customReportLibraryNodeService.delete(nodeIds);
	}


	//-------------- PRIVATE STUFF ---------------------------
	private JsTreeNode createNewCustomReportLibraryNode(Long libraryId, TreeEntity entity){
		CustomReportLibraryNode newNode = customReportLibraryNodeService.createNewNode(libraryId, entity);
		return builderProvider.get().build(newNode);
	}

	private List<JsTreeNode> getNodeContent(long folderId) {
		List<TreeLibraryNode> children = workspaceService.findContent(folderId);
		return listBuilder.build(children);
	}

	private void moveNodes(@PathVariable(RequestParams.NODE_IDS) Long[] nodeIds, @PathVariable("destinationId") long destinationId) {
		customReportLibraryNodeService.moveNodes(Arrays.asList(nodeIds),destinationId);
	}

	private List<JsTreeNode> copyNodes(@RequestParam("nodeIds[]") Long[] nodeIds, @PathVariable("destinationId") long destinationId) {
		List<TreeLibraryNode> nodeList;
		nodeList = customReportLibraryNodeService.copyNodes(Arrays.asList(nodeIds), destinationId);
		return listBuilder.build(nodeList);
	}


	//Class for messages

	protected static class Messages {

		private Collection<String> messages = new ArrayList<>();

		public Messages() {
			super();
		}

		public void addMessage(String msg) {
			this.messages.add(msg);
		}

		public Collection<String> getMessages() {
			return this.messages;
		}

	}

}
