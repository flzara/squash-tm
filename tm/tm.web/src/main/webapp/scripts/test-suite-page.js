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
require(["common"], function () {
	require(["jquery", "app/pubsub", "squash.translator", "squash.basicwidgets", "workspace.event-bus", "workspace.routing",
			"app/ws/squashtm.workspace", "contextual-content-handlers", "jquery.squash.fragmenttabs",
			"bugtracker/bugtracker-panel", "test-suite-management", "custom-field-values", 'squash.statusfactory', "squash.configmanager", "page-components/general-information-panel",
			"test-suite/execution-buttons-panel", "test-automation/auto-execution-buttons-panel", "jquery.cookie"],
		function ($, ps, messages, basicwidg, eventBus, routing, WS, contentHandlers, Frag, bugtracker, tsmanagement, cufvalues, statusfactory, confman, general) {
			"use strict";

			$(document).on("click", "#duplicate-test-suite-button", function () {
				console.log("click", "#duplicate-test-suite-button");
				$("#confirm-duplicate-test-suite-dialog").confirmDialog("open");
				return false;
			});

			/* post a request to duplicate the test suite */
			/* should be put in global ns and referenced someplace */
			function duplicateTestSuite() {
				return $.ajax({
					"url": squashtm.page.api.copy,
					type: "POST",
					data: [],
					dataType: "json"
				});
			}

			function initExecutionStatus() {
				var refreshTestSuiteInfo = _.bind(function () {
					$.ajax({
						url: config.testSuiteURL + "/getExecutionStatus",
						type: "get"
					}).done(function (value) {

						var executionStatusIcon = $("#test-suite-execution-status-icon");
						executionStatusIcon.html(statusfactory.getIconFor(value));
						$("#test-suite-execution-status-icon > span").css("display", "inline");

						var executionStatusEditable = $("#test-suite-execution-status");
						executionStatusEditable.html(statusfactory.translate(value));

						eventBus.trigger('test-suite.execution-status-modified');
					});
					$("#general-information-panel").setAttribute
					general.refresh();
				}, window);

				squashtm.execution = squashtm.execution || {};
				squashtm.execution.refreshTestSuiteInfo = refreshTestSuiteInfo;

				var status = config.testSuiteExecutionStatus;
				var executionStatusIcon = $("#test-suite-execution-status-icon");

				executionStatusIcon.html(statusfactory.getIconFor(status));
				$("#test-suite-execution-status-icon > span").css("display", "inline");

				var executionStatusEditable = $("#test-suite-execution-status");
				executionStatusEditable.html(statusfactory.translate(status));

				if (config.writable) {
					executionStatusEditable.addClass('editable');
					var statusUrl = config.testSuiteURL;
					var statusCfg = confman.getJeditableSelect();
					statusCfg = $.extend(statusCfg, {
						data: JSON.stringify(config.testSuiteExecutionStatusCombo),
						callback: refreshTestSuiteInfo
					});

					executionStatusEditable.editable(statusUrl, statusCfg);
				}
			}

			// ******** rename popup *************

			function initRenameDialog() {

				var renameDialog = $("#rename-testsuite-dialog");
				renameDialog.formDialog();

				renameDialog.on('formdialogopen', function () {
					var name = $.trim($("#test-suite-name").text());
					$("#rename-test-suite-name").val(name);
				});

				renameDialog.on('formdialogconfirm', function () {
					$.ajax({
						url: config.testSuiteURL,
						type: 'POST',
						dataType: 'json',
						data: {"newName": $("#rename-test-suite-name").val()}
					})
						.done(function (json) {
							renameDialog.formDialog('close');

							eventBus.trigger("node.rename", {
								identity: config.identity,
								newName: json.newName
							});
						});
				});

				renameDialog.on('formdialogcancel', function () {
					renameDialog.formDialog('close');
				});

				$("#rename-test-suite-button").on('click', function () {
					renameDialog.formDialog('open');
				});
			}

			/* duplication sucess handler */
			/* should be put in global ns and referenced someplace */
			/* should be refreshed */

			var duplicateTestSuiteSuccess;

			if (config.isFullPage) {
				duplicateTestSuiteSuccess = function (idOfDuplicate) {
					$.squash.openMessage(messages.get("test-suite.duplicate.success.title"), messages.get("test-suite.duplicate.success.message"));
				};
			} else {
				duplicateTestSuiteSuccess = function (idOfDuplicate) {
					eventBus.trigger("node.add", {
						parent: squashtm.page.parentIdentity,
						child: {
							resid: idOfDuplicate,
							rel: "test-suite"
						}
					});
				};
			}

			squashtm.execution = squashtm.execution || {};
			squashtm.execution.refresh = function () {
				eventBus.trigger("context.content-modified");
			};

			ps.subscribe("reload.test-suite", function () {
				var config = _.extend({}, squashtm.page);

				config = _.defaults(config, {
					isFullPage: false,
					hasBugtracker: false,
					hasFields: false
				});

				WS.init();
				basicwidg.init();

				initExecutionStatus();
				// test suite dialog init
				initRenameDialog();

				// registers contextual events
				// TODO should be unregistered before ?
				function refreshExecButtons() {
					console.log("refreshExecButtons");
					var $panel = $("#test-suite-exec-btn-group");
					$panel.load($panel.data("content-url"));
				}

				eventBus.onContextual("context.content-modified", refreshExecButtons);
				eventBus.onContextual("context.content-modified", refreshStatistics); // WTF window namespace alert !
				eventBus.onContextual("context.content-modified", function () {
					$("#test-suite-test-plans-table").squashTable().refresh();
				});

				// some other pre-whichever-refactor event handling
				var nameHandler = contentHandlers.getSimpleNameHandler();

				nameHandler.identity = squashtm.page.identity;
				nameHandler.nameDisplay = "#test-suite-name";

				// ****** tabs configuration *******

				var fragConf = {
					active: 1
					/*cookie : {
					 name : "suite-tab-cookie",
					 path : routing.buildURL('testsuites.base')
					 }*/
				};
				Frag.init(fragConf);

				if (config.hasBugtracker) {
					bugtracker.load(config.bugtracker);
				}

				if (config.hasFields) {
					var url = config.customFields.url;
					$.getJSON(url)
						.success(function (jsonCufs) {
							$("#test-suite-custom-fields-content .waiting-loading").hide();
							var mode = (config.writable) ? "jeditable" : "static";
							cufvalues.infoSupport.init("#test-suite-custom-fields-content", jsonCufs, mode);
						});
				}

				var dialog = $("#confirm-duplicate-test-suite-dialog");

				var confirmHandler = function () {
					dialog.confirmDialog("close");
					duplicateTestSuite().done(function (json) {
						duplicateTestSuiteSuccess(json);
					});
				};

				dialog.confirmDialog({confirm: confirmHandler});
				console.log("test-suite-page refresh");
			});
			console.log("test-suite-page.js loaded");
		});

});
