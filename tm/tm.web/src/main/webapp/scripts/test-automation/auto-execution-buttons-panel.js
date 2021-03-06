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
 * Controller for <code>execute-auto-button.tag</code>.
 *
 * <code>execute-auto-button.tag</code> issues a "reload.auto-exec-btns-panel" event through pubsub.
 * This modules initializes the buttons when the event is triggered. Client should only require this module.
 */
define([ "jquery", "squash.translator", "../app/pubsub", "jquery.squash.buttonmenu" ], function($, messages, ps) {
	"use strict";

	// init message cache
	messages.load({
		errorDlg : {
			title : "popup.title.error",
			message : "message.EmptyTableSelection"
		},
		doneDlg : {
			title : "popup.title.Info",
			message : "dialog.execution.auto.overview.error.none"
		}
	});

	function executeAll() {
		var unlaunchableTest;
		updateTAScript().done(function(map){
			// No arrow function in IE 11 ...
			var launchableIds = findTpiIdsWithAutomaticExecutionMode().done(function(tpiIdsWithAutomaticExecutionMode) {
				tpiIdsWithAutomaticExecutionMode.filter(function(id){
					return map[id] === undefined;
				});
			});
			if (launchableIds.length === 0){
				$.squash.openMessage(messages.get("popup.title.error"), messages.get("dialog.execution.auto.overview.error.noneAfterScriptUpdate"));
			} else {
				//Alternative which work with IE. The "better" version but not compatible IE is unlaunchableTest = Object.values(map);
				unlaunchableTest = Object.keys(map).map(function(e) {
					return map[e];
				});
				sendPreview(launchableIds).done(function(preview) {
					openAutosuiteOverview(preview, unlaunchableTest);
				});
			}
		});
	}

	function executeSelection() {
		var ids = $(".test-plan-table").squashTable().getSelectedIds();
		var unlaunchableTest;
		if (ids.length === 0) {
			$.squash.openMessage(messages.get("popup.title.error"), messages.get("message.EmptyTableSelection"));
		} else {
			updateTAScript(ids).done(function(map){
				// No arrow function in IE 11 ...
				var launchableIds = ids.filter(function(id){
					return map[id] === undefined;
				});
				if (launchableIds.length === 0){
					$.squash.openMessage(messages.get("popup.title.error"), messages.get("dialog.execution.auto.overview.error.noneAfterScriptUpdate"));
				} else {
					//Alternative which work with IE. The "better" version but not compatible IE is unlaunchableTest = Object.values(map);
					unlaunchableTest = Object.keys(map).map(function(e) {
						return map[e];
					}).filter(function(value, index, self){
						return self.indexOf(value) === index;
					});
					sendPreview(launchableIds).done(function(preview) {
						openAutosuiteOverview(preview, unlaunchableTest);
					});
				}
			});
		}
	}

	function openAutosuiteOverview(preview, unlaunchableTest) {
		squashtm.context.autosuiteOverview.start(preview, unlaunchableTest);
	}

	/**
	 * issues create suite ajax request and returns request promise
	 */
	function sendPreview(itemIds) {
		var previewUrl = $("#auto-exec-btns-panel").data("suites-url") + "/preview";

		var context = {
			type : squashtm.page.identity.restype === "iterations" ? "ITERATION" : "TEST_SUITE",
			id : squashtm.page.identity.resid
		};

    // set the test plan subset if defined
		var testPlanSubsetIds = (!!itemIds && itemIds.length > 0) ? itemIds : [];

		var payload = {
			context : context,
			testPlanSubsetIds : testPlanSubsetIds
		};

		return $.ajax({
			type : "POST",
			url : previewUrl,
			dataType : "json",
			data : JSON.stringify(payload),
			contentType : "application/json"
		});
	}

	function updateTAScript(itemIds){
		var associateUrl = squashtm.app.contextRoot + 'automation-requests/associate-TA-script';

		var data = {};
		var ent =  squashtm.page.identity.restype === "iterations" ? "iterationId" : "testSuiteId";
		data[ent] = squashtm.page.identity.resid;

		if (!!itemIds && itemIds.length > 0) {
			data.testPlanItemsIds = itemIds;
		}

		return $.ajax({
			type : "POST",
			url : associateUrl,
			dataType : "json",
			data : data,
			contentType : "application/x-www-form-urlencoded;charset=UTF-8"
		});
	}

	function findTpiIdsWithAutomaticExecutionMode() {
		var context = {
			type : squashtm.page.identity.restype === "iterations" ? "ITERATION" : "TEST_SUITE",
			id : squashtm.page.identity.resid
		};
		return $.ajax({
			type: 'POST',
			url: $("#auto-exec-btns-panel").data("suites-url") + "/automated-tpi-ids",
			dataType: 'json',
			data : JSON.stringify(context),
			contentType : 'application/json'
		});
	}

	ps.subscribe("reload.auto-exec-btns-panel", function() {
		console.log("refreshing auto-exec-btns-panel");
		$("#execute-auto-button").buttonmenu();
		$("#execute-auto-execute-all").on("click", executeAll);
		$("#execute-auto-execute-selection").on("click", executeSelection);
	});
});
