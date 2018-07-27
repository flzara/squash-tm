/*
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
define(["jquery", "tree","./permissions-rules", "workspace.contextual-content", "workspace.event-bus", "squash.translator" ,
        "workspace.tree-node-copier", "workspace.tree-event-handler", "app/ws/squashtm.notification"],
        function($, zetree, rules, ctxcontent, eventBus,  translator, copier, treehandler, notification){
	"use strict";

	var messages = {
		"no-libraries-allowed"	: "tree.button.copy-node.error.nolibrary",
		"not-unique"			: "tree.button.copy-node.error.notOneEditable",
		"not-creatable"			: "tree.button.copy-node.error.notOneEditable",
		"empty-selection"		: "tree.button.copy-node.error.nothing-to-paste",
		"invalid-content"		: "tree.button.copy-node.error.pastenothere",
		"not-deletable"			: "dialog.label.delete-node.rejected",
		"mixed-nodes-iteration-selection" : "tree.button.copy-node.mixediteration",
		"mixed-nodes-testsuite-selection" : "tree.button.copy-node.mixedsuite",
		"milestone-denied"		: "squashtm.action.exception.milestonelocked.campaign"
	};

	translator.load(messages);

	function showError(messageName){
		notification.showInfo(translator.get(messages[messageName]));
	}

	function copyIfOk(tree){
		var nodes = tree.jstree("get_selected");
		if (rules.canCopy(nodes)){
			copier.copyNodesToCookie();
		}
		else{
			var why = rules.whyCantCopy(nodes);
			showError(why);
		}
	}

	function pasteIfOk(tree){
		if (rules.canPaste()){
			copier.pasteNodesFromCookie();
		}
		else{
			var why = rules.whyCantPaste();
			showError(why);
		}
	}

	function loadFragment(tree){
		var selected =  tree.jstree("get_selected");
		if (selected.length == 1){
			ctxcontent.loadWith(selected.getResourceUrl());
		}
		else{
			ctxcontent.unload();
		}
	}

	function onClickExport(tree, exporter) {
		return function(event) {
			var nodeId = tree.jstree("get_selected").attr("resid");
			var nodeType = tree.jstree("get_selected").attr("restype");

			if (nodeId === undefined || nodeType !== "campaigns") {
				var dialog = $("#export-campaign-error-dialog").formDialog();
				dialog.on('formdialogcancel', function(){
					dialog.formDialog('close');
				});
				dialog.formDialog('open');

			} else {
				exporter.call(this, {source: event, nodeType: nodeType, nodeId: nodeId});
			}

		};
	}

	function exportUrl(nodeId) {
		return window.squashtm.app.contextRoot+"/campaign-browser/export-campaign/" + nodeId;
	}

	return {
		init : function(){

			var tree = zetree.get();

			tree.on("select_node.jstree deselect_node.jstree", function(){
				loadFragment(tree);
			});

			// ************* creation ***************

			$("#new-folder-tree-button").on("click", function(){
				$("#add-folder-dialog").formDialog("open");
			});

			$("#new-campaign-tree-button").on("click", function(){
				$("#add-campaign-dialog").formDialog("open");
			});

			$("#new-iteration-tree-button").on("click", function(){
				$("#add-iteration-dialog").formDialog("open");
			});

			// *************** copy paste ****************

			$("#copy-node-tree-button").on("click", function(){
				copyIfOk(tree);
			});

			// issue 2762 : the events "copy.squashtree" and the native js event "copy" (also triggered using ctrl+c) would both fire this
			// handler. Its a bug of jquery, fixed in 1.9.
			// TODO : upgrade to jquery 1.9
			tree.on("copy.squashtree", function(evt){
				if (evt.namespace==="squashtree"){
					copyIfOk(tree);
				}
			});

			$("#paste-node-tree-button").on("click", function(){
				pasteIfOk(tree);
			});

			// issue 2762 : the events "paste.squashtree" and the native js event "paste" (also triggered using ctrl+v) would both fire this
			// handler. Its a bug of jquery, fixed in 1.9
			// TODO : upgrade to jquery 1.9
			tree.on("paste.squashtree", function(evt){
				if (evt.namespace === "squashtree"){
					pasteIfOk(tree);
				}
			});

			// ***************** rename **********************

			$("#rename-node-tree-button").on("click", function(){
				$("#rename-node-dialog").formDialog("open");
			});

			tree.on("rename.squashtree", function(){
				$("#rename-node-dialog").formDialog("open");
			});

			// ****************** exports *********************

			// NOTE : DO NOT BIND USING $("menu").on("click", "button", handler), this breaks under (true) IE8. See #3268
			$("#export-L-tree-button").on("click", onClickExport(tree, function(event){
				document.location.href = exportUrl(event.nodeId) + "?export=csv&exportType=L";
			}));

			$("#export-S-tree-button").on("click", onClickExport(tree, function(event){
					document.location.href= exportUrl(event.nodeId) + "?export=csv&exportType=S";
			}));

			$("#export-F-tree-button").on("click", onClickExport(tree, function(event){
				document.location.href = exportUrl(event.nodeId) + "?export=csv&exportType=F";
			}));

			// *****************  search  ********************

			$("#search-tree-button").on("click", function(){
				// get value of Campaign Workspace Cookie
				var cookieValueSelect = $.cookie("jstree_select");
				var cookieValueOpen = $.cookie("workspace-prefs");
				document.location.href = window.squashtm.app.contextRoot +
					"/advanced-search?searchDomain=campaign";
			});

			// ***************** deletion ********************

			function openDeleteDialogIfDeletable(){
				var nodes = tree.jstree('get_selected');
				if (!rules.canDelete(nodes)) {
					showError(rules.whyCantDelete(nodes));
				}
				else{
					$("#delete-node-dialog").delcampDialog("open");
				}
			}

			function tryToRefresh() {
				if (!$("#dashboard-grid").size()) {
					window.requestAnimationFrame(tryToRefresh);
				}
				else {
					wreqr.trigger("favoriteDashboard.reload");
				}
			}

			$("#delete-node-tree-button").on("click", openDeleteDialogIfDeletable);

			tree.on("suppr.squashtree", openDeleteDialogIfDeletable);

			//**************** favorite dashboard **************

			var wreqr = squashtm.app.wreqr;
			wreqr.on("favoriteDashboard.showDefault", function () {
				//[Issue 6476] We want to reload the page with the dashboar tab selected
				//as we will NOT modify contextual content and a bunch of controllers for that, we'll use a pure client side solution based on global var
				var selectedIteration = tree.jstree("get_selected").filter("[restype='iterations']");
				if(selectedIteration.length > 0 ){
					squashtm.workspace.shouldShowFavoriteDashboardTab = true;
				}
				//we need to unload the whole view as we cannot replace the backbone view by a new JSP fragment easily
				//it's far easier and cleaner to reload the contextual content after backbone view has been destroyed
				ctxcontent.unload();
				loadFragment(tree);
			  });

			wreqr.on("favoriteDashboard.showFavorite", function () {
				//see just above
				var selectedIteration = tree.jstree("get_selected").filter("[restype='iterations']");
				if(selectedIteration.length > 0 ){
					squashtm.workspace.shouldShowFavoriteDashboardTab = true;
				}
				ctxcontent.unload();
				loadFragment(tree);
			  });

			wreqr.on("favoriteDashboard.milestone.showDefault", function () {
				ctxcontent.unload();
				ctxcontent.loadWith(squashtm.app.contextRoot+"/campaign-browser/dashboard-milestones");
			});

			wreqr.on("favoriteDashboard.milestone.showFavorite", function () {
				ctxcontent.unload();
				ctxcontent.loadWith(squashtm.app.contextRoot+"/campaign-browser/dashboard-milestones");
			});


		}
	};

});
