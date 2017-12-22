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
	"use strict";

	require(["jquery", "underscore", "app/pubsub", "squash.basicwidgets", "contextual-content-handlers",
			"jquery.squash.fragmenttabs", "bugtracker/bugtracker-panel", "workspace.event-bus", "workspace.routing",
			"iteration-management", "app/ws/squashtm.workspace", "custom-field-values", "squash.configmanager",
			"favorite-dashboard", "./user-account/user-prefs", "test-automation/auto-execution-buttons-panel", "jquery.squash.formdialog"],
		function ($, _, ps, basicwidg, contentHandlers, Frag, bugtracker, eventBus, routing, itermanagement, WS, cufvalues,
							confman, favoriteView, userPrefs) {

			// *********** event handler ***************

			var refreshTestPlan = _.bind(function () {
				console.log("squashtm.execution.refresh");
				$("#iteration-test-plans-table").squashTable().refresh();
			}, window);

			squashtm.execution = squashtm.execution || {};
			squashtm.execution.refresh = refreshTestPlan;

			// this is executed on each fragment load
			ps.subscribe("reload.iteration", function () {
				var config = _.extend({}, squashtm.page);

				config = _.defaults(config, {
					isFullPage: false,
					hasBugtracker: false,
					hasFields: false
				});

				WS.init();
				basicwidg.init();

				var nameHandler = contentHandlers.getNameAndReferenceHandler();
				nameHandler.identity = config.identity;
				nameHandler.nameDisplay = "#iteration-name";
				nameHandler.nameHidden = "#iteration-raw-name";
				nameHandler.referenceHidden = "#iteration-raw-reference";

				// todo : uniform the event handling.
				// rem : what does it mean ?
				itermanagement.initEvents();


				// init reference
				if (config.writable) {
					var refEditable = $("#iteration-reference").addClass('editable');
					var url = config.iterationURL;
					var cfg = confman.getStdJeditable();
					cfg = $.extend(cfg, {
						maxLength: 50,
						callback: function (value, settings) {
							var escaped = $("<span/>").html(value).text();
							eventBus.trigger('node.update-reference', {identity: config.identity, newRef: escaped});
						}
					});

					refEditable.editable(url, cfg);
				}

				// init status
				if (config.writable) {
					var statusEditable = $("#iteration-status").addClass('editable');
					var url = config.iterationURL;
					var cfg = confman.getJeditableSelect();
					cfg = $.extend(cfg, {
						data: config.iterationStatusComboJson,
						callback: function (value, settings) {
							var keyOfValue = _.findKey(settings.data, function (dataValue) {
								return value === dataValue;
							});
							var iconStatus = $("#iteration-status-icon");
							iconStatus.attr("class", ""); //reset
							iconStatus.addClass("sq-icon iteration-status-" + keyOfValue);
						}

					});
					statusEditable.editable(url, cfg);

				}

				// ****** tabs configuration *******


				var fragConf = {
					active: 2,
					/*cookie : {
					 name : "iteration-tab-cookie",
					 path : routing.buildURL('iterations.base')
					 },*/
					activate: function (event, ui) {
						if (ui.newPanel.is("#dashboard-iteration")) {
							eventBus.trigger("dashboard.appear");
						}
					}
				};
				Frag.init(fragConf);

				if (config.hasBugtracker) {
					bugtracker.setBugtrackerMode(config.bugtrackerMode);
					bugtracker.load(config.bugtracker);
				}

				if (config.hasFields) {
					var url = config.customFields.url;
					$.getJSON(url)
						.success(function (jsonCufs) {
							$("#iteration-custom-fields-content .waiting-loading").hide();
							var mode = (config.writable) ? "jeditable" : "static";
							cufvalues.infoSupport.init("#iteration-custom-fields-content", jsonCufs, mode);
						});
				}


				// ******** rename popup **************

				var renameDialog = $("#rename-iteration-dialog");
				renameDialog.formDialog();

				renameDialog.on('formdialogopen', function () {
					var name = $.trim($("#iteration-raw-name").text());
					$("#rename-iteration-name").val(name);
				});

				renameDialog.on('formdialogconfirm', function () {
					$.ajax({
						url: config.iterationURL,
						type: 'POST',
						dataType: 'json',
						data: {"newName": $("#rename-iteration-name").val()}
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

				$("#rename-iteration-button").on('click', function () {
					renameDialog.formDialog('open');
				});

				// ********** dashboard **************
				var shouldShowFavoriteDashboard = userPrefs.shouldShowFavoriteDashboardInWorkspace();

				if (shouldShowFavoriteDashboard) {
					favoriteView.init();
				}
				else {
					itermanagement.initDashboardPanel({
						master: "#dashboard-master",
						cacheKey: "it" + config.identity.resid
					});
				}

				var wreqr = window.squashtm.app.wreqr;
				//refresh when clicking on first tab so the gridster view can initialize properly.
				//if hidden, the view grid initialize itself with 0 size...
				$("#dashboard-tab-list-item").on("click", function () {
					wreqr.trigger("favoriteDashboard.reload");
				});

				//[Issue 6476] ugly hack to select properly the first tab if user just changed favorite dashboard/default dashboard
				//we can't set properly the active tab as we do some pre styling server side and it's a bad idea to change that (fouc...)
				if (!!squashtm.workspace.shouldShowFavoriteDashboardTab) {
					squashtm.workspace.shouldShowFavoriteDashboardTab = false;
					$("#dashboard-tab-list-item").click();
				}
				console.log("iteration-page refresh.iteration");
			});
			console.log("iteration-page.js loaded");
		});
})
;
