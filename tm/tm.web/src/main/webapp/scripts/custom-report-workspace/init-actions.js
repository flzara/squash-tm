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
        "workspace.tree-node-copier", "workspace.tree-event-handler","workspace.routing"], function($, zetree, rules, ctxcontent, eventBus, translator, copier, treehandler, urlBuilder){


	function showError(messageName){

		var messages = translator.get({
			"no-libraries-allowed"	: "tree.button.copy-node.error.nolibrary",
			"not-unique"			: "tree.button.copy-node.error.notOneEditable",
			"not-creatable"			: "tree.button.copy-node.error.notOneEditable",
			"empty-selection"		: "tree.button.copy-node.error.nothing-to-paste",
			"invalid-content"		: "tree.button.copy-node.error.pastenothere",
			"not-deletable"			: "dialog.label.delete-node.rejected",
			"milestone-denied"		: "squashtm.action.exception.milestonelocked"
		});

		squashtm.notification.showInfo(messages[messageName]);
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

    if (selected.length === 1) {
      navigateToDetails(tree,selected);
    }
    else {
      navigateToEmpty();
    }
	}

  function navigateToDetails (tree,selectedNode) {
    var backboneRouter = squashtm.app.router;
    var nodeType = selectedNode.getDomType();
    var nodeId = selectedNode.getResId();//yeah it's node id in resId attr... wa cannot take time to change tree...
    var url;
    switch (nodeType) {
      case "drive":
        url = urlBuilder.buildURL("custom-report-library",nodeId);
        backboneRouter.navigate(url, {trigger: true});
        break;
      case "folder":
        url = urlBuilder.buildURL("custom-report-folder",nodeId);
        backboneRouter.navigate(url, {trigger: true});
        break;
      case "dashboard":
        url = urlBuilder.buildURL("custom-report-dashboard",nodeId);
        backboneRouter.navigate(url, {trigger: true});
        break;
      case "chart":
        url = urlBuilder.buildURL("custom-report-chart",nodeId);
        backboneRouter.navigate(url, {trigger: true});
        break;
			case "report":
				url = urlBuilder.buildURL("custom-report-report",nodeId);
				backboneRouter.navigate(url, {trigger: true});
				break;
      default:

    }
  }

  function navigateToEmpty(){
    var backboneRouter = squashtm.app.router;
    backboneRouter.navigate("", {trigger: true});
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

			$("#new-dashboard-tree-button").on("click", function(){
				$("#add-dashboard-dialog").formDialog("open");
			});

			$("#new-report-tree-button").on("click", function(){
				var selectedNode =  tree.jstree("get_selected");
				if (!selectedNode.canContainNodes()) {
					selectedNode = selectedNode.getParent();
				}
				var nodeId = selectedNode.getResId();
				url = urlBuilder.buildURL("report-workspace",nodeId);
				document.location.href = url;
			});

      $("#new-chart-tree-button").on("click", function(){
        var selectedNode =  tree.jstree("get_selected");
        if (!selectedNode.canContainNodes()) {
          selectedNode = selectedNode.getParent();
        }
        var nodeId = selectedNode.getResId();
        url = urlBuilder.buildURL("chart.wizard",nodeId);
        document.location.href = url;
      });

      // *************** copy paste ****************

			$("#copy-node-tree-button").on("click", function(){
				copyIfOk(tree);
			});

			tree.on("copy.squashtree", function(evt){
				if (evt.namespace==="squashtree"){
					copyIfOk(tree);
				}
			});

			$("#paste-node-tree-button").on("click", function(){
				pasteIfOk(tree);
			});

			tree.on("paste.squashtree", function(evt){
				if (evt.namespace === "squashtree"){
					pasteIfOk(tree);
				}
			});


			// ***************** rename **********************

			$("#rename-node-tree-button").on("click", function(){
				openRenamePopup();
			});

			tree.on("rename.squashtree", function(){
				openRenamePopup();
			});

      function openRenamePopup() {
        	$("#rename-node-dialog").formDialog("open");
      }

			function refreshAfterMove() {
				navigateToEmpty();
				loadFragment(tree);
			}

      var wreqr = squashtm.app.wreqr;
      wreqr.on("renameNode",openRenamePopup);
			wreqr.on("tree.moveNodes.done", refreshAfterMove);


			// ***************** deletion ********************

			function openDeleteDialogIfDeletable(){
				var nodes = tree.jstree('get_selected');
				if (!rules.canDelete(nodes)) {
					showError(rules.whyCantDelete(nodes));
				}
				else{
					$("#delete-node-dialog").delnodeDialog("open");
				}
			}

			$("#delete-node-tree-button").on("click", openDeleteDialogIfDeletable);

			tree.on("suppr.squashtree", openDeleteDialogIfDeletable);


      // ***************** chart export ********************

      function openExportChartPopup() {
        	$("#export-chart-dialog").formDialog("open");
      }

      wreqr.on("exportChart",openExportChartPopup);

		}
	};

});
