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
/**
 * @since 1.12.0.RC2 This module uses Wreqr to listen to these events and react accordingly :
 * * `verifying-test-cases:unbind-row` unbinds the TC matching the table row which was clicked
 * * `verifying-test-cases:unbind-selected` unbinds the TCs matching the selected rows of the table
 */
require([ "common" ], function() {
	require([ "jquery", "app/squash.wreqr.init", "workspace.event-bus", "workspace.tree-event-handler",
    "squash.translator", "app/ws/squashtm.notification", "verifying-test-cases/VerifyingTestCasesPanel", "jqueryui", "jquery.squash.messagedialog", "squashtable", "app/ws/squashtm.workspace" ],
			function($, squash, eventBus, treehandler, msg, notification, VerifyingTestCasesPanel) {
		"use strict";

		msg.load([
			"requirement-version.verifying-test-case.already-verified-rejection",
			"requirement-version.verifying-test-case.not-linkable-rejection",
			"dialog.unbind-ta-project.tooltip"
		]);
		
        
  	  function lock(){
  		  $('#add-items-button').button('disable');
  		  $('#remove-items-button').button('disable');
  	  }
  	  
  	  function unlock(){
  		  $('#add-items-button').button('enable');
  		  $('#remove-items-button').button('enable');
  	  }

		function sendUpdateTree(ids){
			eventBus.trigger("node.update-reqCoverage", {targetIds : ids});
		}

		function showAddSummary(summary) {
			if (summary) {
				var summaryMessages = {
						alreadyVerifiedRejections: msg.get("requirement-version.verifying-test-case.already-verified-rejection"),
						notLinkableRejections: msg.get("requirement-version.verifying-test-case.not-linkable-rejection")
				};

				var summaryRoot = $( "#add-summary-dialog > ul" );
				summaryRoot.empty();

				for(var rejectionType in summary) {
					var message = summaryMessages[rejectionType];

					if (message) {
						summaryRoot.append('<li>' + message + '</li>');
					}
				}

				if (summaryRoot.children().length > 0) {
					$( "#add-summary-dialog" ).messageDialog("open");
				}
			}
		}

		/**
		 * returns the datatable (well, squashtable) object for verifying TCs
		 */
		function table() {
			return $("#verifying-test-cases-table").squashTable();
		}

		$(document).on("click", "#add-items-button", function(event){
			squash.vent.trigger("verifying-test-cases:bind-selected", { source: event });
		});

		
		// maybe here
		$(document).on("click", "#remove-items-button", function(event){
			squash.vent.trigger("verifying-test-cases:unbind-selected", { source: event });
		});

		squash.vent.on("verifyingtestcasespanel:unbound", function(event) {
			sendUpdateTree(event.model);
		});

		//the case 'get ids from the research tab' is disabled here, waiting for refactoring.
		function getTestCasesIds(){
			
			var ids =	[];
			var nodes = 0;
			if( $( '#linkable-test-cases-tree' ).jstree('get_selected').length > 0 ) {
				 nodes = $( '#linkable-test-cases-tree' ).jstree('get_selected').not(':library').treeNode();
				 ids = nodes.all('getResId');
			}	 
			return $.map(ids, function(id) { return parseInt(id); });
		}

		$(function() {
			

			// init the table
			$("#verifying-test-cases-table").squashTable({
				aaData : window.squashtm.bindingsManager.model
			},{
	          unbindButtons : {
	            delegate : "#unbind-active-row-dialog",
	            tooltip : msg.get('dialog.unbind-ta-project.tooltip')
	          }		
			});
			
			// init the panel
			new VerifyingTestCasesPanel({ apiUrl: window.squashtm.bindingsManager.bindingsUrl })

			$("#add-summary-dialog").messageDialog();

			var bind = VerifyingTestCasesPanel.bindingActionCallback(window.squashtm.bindingsManager.bindingsUrl, "POST");

			$("#add-items-button").on("click", function() {
				lock();
				var tree = $('#linkable-test-cases-tree');
				var ids = getTestCasesIds();
			
				if (ids.length === 0) {
					notification.showError(msg.get('message.emptySelectionTestCase'));
					return;
				}

				tree.jstree('deselect_all');
				
				bind(ids).success(function(data){
					showAddSummary(data);
					table().refresh();
					unlock();
					sendUpdateTree(data.linkedIds);
				});

			});
			
			
		});
	});
});
