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
/*
 * Conf : see the conf of the main module + the following metadata generated in the main module :
 *
 * {
 *  data:{
 *  identity : {
 *		resid : equivalent to campaignId,
 *		restype : hardcoded to "campaigns", considering that we are in the campaign core init module
 *  },
 *  dashboard : see the parameters for the dashboard. Here we define a master and a cache key
 *
 * }
 * }
 *
 */
define(["jquery", "squash.basicwidgets", "contextual-content-handlers", "jquery.squash.fragmenttabs",
		"bugtracker/bugtracker-panel", "workspace.event-bus", "squash.translator",
		"dashboard/campaigns-dashboard/campaigns-dashboard-main", "./planning", "datepicker/datepickers-pair", "datepicker/datepickers-auto-pair",
		"./test-plan-panel", "custom-field-values", "squash.configmanager", "favorite-dashboard", "underscore",
		"jqueryui", "jquery.squash.formdialog"
	],
	function ($, basicwidg, contentHandlers, Frag, bugtrackerPanel, eventBus, translator,
						dashboard, planning, datePickers, datePickersAuto, testplan, cufvalues, confman, favoriteView, _) {


		function init(conf) {

			initTranslator();

			basicwidg.init();

			initTabs(conf);

			initDescription(conf);

			initCufs(conf);

			initRenameHandler(conf);

			initRenameDialog(conf);

			initPlanning(conf);

			initDatePickers(conf);

			initDatePickersAuto(conf);

			initDashboard(conf);

			initTestplan(conf);

			initBugtracker(conf);

			initStatus(conf);
		}

		// Load all the needed message properties to the cache
		function initTranslator() {
			var messages = {
				// Properties for datepickers-pair
				"datePicker-period-notConsistent": "message.exception.timePeriodNotConsistent",
				"dateFormatShort": "squashtm.dateformatShort",
				"dateFormatShort-datePicker": "squashtm.dateformatShort.datepicker",
				// Properties for bugtracker-panel
				"bugtracker-timeout": "message.bugtracker.unavailable.timeout",
				// Properties for campaigns-dashboard-main
				"dashboard-test-cases-search": "dashboard.test-cases.search",
				// Properties for planning
				"planning-notConsistent-periods": "message.exception.planning.notConsistentPeriods"
			};
			translator.load(messages);
		}

		// initialize the description bloc (not just the attribute)
		function initDescription(conf) {
			if (conf.features.writable) {
				var refEditable = $("#campaign-reference").addClass('editable');
				var url = conf.data.campaignUrl;
				var cfg = confman.getStdJeditable();
				cfg = $.extend(cfg, {
					maxLength: 50,
					callback: function (value, settings) {
						var escaped = $("<span/>").html(value).text();
						eventBus.trigger('node.update-reference', {identity: conf.data.identity, newRef: escaped});
					}
				});

				refEditable.editable(url, cfg);
			}
		}

		function initStatus(conf) {
			if (conf.features.writable) {
				var statusEditable = $("#campaign-status").addClass('editable');
				var url = conf.data.campaignUrl;
				var cfg = confman.getJeditableSelect();
				cfg = $.extend(cfg, {
					data: conf.data.campaignStatusComboJson,
					callback: function (value, settings) {
						var keyOfValue = _.findKey(settings.data, function (dataValue) {
							return value === dataValue;
						});
						var iconStatus = $("#campaign-status-icon");
						iconStatus.attr("class", ""); //reset
						iconStatus.addClass("sq-icon campaign-status-" + keyOfValue);
					}

				});
				statusEditable.editable(url, cfg);
			}
		}

		function initCufs(conf) {
			if (conf.features.hasCUF) {
				var url = conf.data.cufValuesUrl + "?boundEntityId=" + conf.data.campaignId + "&boundEntityType=CAMPAIGN";
				$.getJSON(url)
					.success(function (jsonCufs) {
						$("#campaign-custom-fields-content .waiting-loading").hide();
						var mode = (conf.features.writable) ? "jeditable" : "static";
						cufvalues.infoSupport.init("#campaign-custom-fields-content", jsonCufs, mode);
					});
			}
		}

		function initRenameHandler(conf) {
			var nameHandler = contentHandlers.getNameAndReferenceHandler();
			nameHandler.identity = conf.data.identity;
			nameHandler.nameDisplay = "#campaign-name";
			nameHandler.nameHidden = "#campaign-raw-name";
			nameHandler.referenceHidden = "#campaign-raw-reference";
		}

		function initTabs(conf) {
			var fragConf = {
				activate: function (event, ui) {
					if (ui.newPanel.is("#campaign-dashboard")) {
						eventBus.trigger('dashboard.appear');
					}
				}
			};

			Frag.init(fragConf);
		}

		function initBugtracker(conf) {
			if (conf.features.hasBugtracker) {
				bugtrackerPanel.load({
					url: conf.data.bugtrackerUrl,
					style: "fragment-tab"
				});
			}
		}

		function initPlanning(conf) {
			if (conf.features.writable) {
				planning.init(conf);
			}
		}

		function initDatePickers(conf) {
			if (conf.features.writable) {
				datePickers.init(conf);
			}
		}

		function initDatePickersAuto(conf) {
			if (conf.features.writable) {
				datePickersAuto.init(conf);
			}
		}

		function initDashboard(conf) {
			var shouldShowFavoriteDashboard = squashtm.workspace.shouldShowFavoriteDashboard;
			if (shouldShowFavoriteDashboard) {
				favoriteView.init();
			} else {
				dashboard.init(conf.dashboard);
			}
		}

		function initTestplan(conf) {
			testplan.init(conf);
		}

		function initRenameDialog(conf) {

			var dialog = $("#rename-campaign-dialog"),
				campaignUrl = conf.data.campaignUrl,
				campaignId = conf.data.campaignId;

			dialog.formDialog();

			dialog.on("formdialogopen", function () {
				var name = $('#campaign-raw-name').text();
				var trimmed = $.trim(name);
				$("#rename-campaign-name").val(trimmed);
			});

			dialog.on('formdialogconfirm', function () {
				var newName = $("#rename-campaign-name").val();
				$.ajax({
					url: campaignUrl,
					type: 'POST',
					dataType: 'json',
					data: {newName: newName}
				})
					.done(function (data) {
						dialog.formDialog('close');
						eventBus.trigger('node.rename', {identity: conf.data.identity, newName: data.newName});
					});
			});

			dialog.on('formdialogcancel', function () {
				dialog.formDialog('close');
			});

			$("#rename-campaign-button").on('click', function () {
				dialog.formDialog('open');
			});

		}


		return {
			init: init
		};


	});
