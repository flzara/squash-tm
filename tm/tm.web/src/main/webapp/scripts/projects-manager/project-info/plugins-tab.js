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
define(
	["jquery", "backbone", "underscore", "squash.translator", "workspace.routing", "app/pubsub", "workspace.event-bus", "jquery.squash.togglepanel",
		"jqueryui", "squashtable", "jquery.switchButton"],

	function ($, Backbone, _, translator, routing, pubsub, eventBus) {

		translator.load(["label.Enabled", "label.disabled", "label.Configure"]);

		function urlPlugin(projectId, pluginId) {
			return routing.buildURL('project-plugins', projectId, pluginId);
		}

		function putBackButtonSwitch(btn, checked, data, event) {
			var $row = btn.parents('tr').first();
			btn.switchButton("option", "checked", !checked);
			configureStyle($row, data);
		}

		function configureSwitch($row, data) {
			var switchcell = $row.find('.plugin-enabled');

			var chk = $("<input>", {
				'type': 'checkbox'
			});

			if (data['enabled']) {
				chk.prop('checked', true);
			}

			switchcell.empty().append(chk);

			chk.switchButton({
				on_label: translator.get('label.Enabled'),
				off_label: translator.get('label.disabled')
			});
		}

		function configureStatus($row, data) {
			var statscell = $row.find('.plugin-status');

			// TODO : use dedicated css classes once the image are available
			var css = (data['status'] === "OK") ? 'exec-status-success' : 'exec-status-blocked';
			var sp = $("<span>", {
				'class': 'sq-icon ' + css
			});

			statscell.empty().append(sp);
		}

		function configureLink($row, data) {
			var confcell = $row.find('.plugin-configure');
			confcell.text(translator.get('label.Configure'));

		}

		function noNavigation(evt) {
			evt.preventDefault();
		}

		function configureStyle($row, data) {
			var cells = $row.find('>td').not('.plugin-index, plugin-enabled'),
				state = data['enabled'];

			if (state) {
				cells.removeClass('disabled-transparent');
				cells.off('click', noNavigation);
			} else {
				cells.addClass('disabled-transparent');
				cells.on('click', noNavigation);
			}
		}

		function configurePopup(pluginId) {
			var popup = $("#disabled-plugin").formDialog();
			var label = popup.find($("#msgDisablePlugin"));
			label.empty();
			if (pluginId.indexOf("jirasync") != -1) {
				label.append(translator.get('message.disabled.plugin.xsquashjira'));
			} else {
				label.append(translator.get('message.disabled.plugin.workflow-automjira'));
			}
		}


		return function (conf) {

			var table = $("#plugins-table");

			table.squashTable({
				aaData: conf.plugins,
				bServerSide: false,
				fnRowCallback: function (row, data, displayIndex) {

					var $row = $(row);
					// enable switch setup
					configureSwitch($row, data);

					// change the style
					configureStyle($row, data);

					// enable the status
					configureStatus($row, data);

					// 'configure' link placeholder if plugin is configurable
					if (!!data.configUrl) {
						configureLink($row, data);
					}
				}
			}, {});

			function updateAutomationWorkflowSelect(checked, projectId, reload) {

				var url = squashtm.app.contextRoot + 'generic-projects/' + projectId;
				var method = 'POST';
				var id = 'change-automation-workflow';

				if (checked === true) {
					$.ajax({url: url, type: method, data: {id: id, value: 'REMOTE_WORKFLOW'}}).done(function() {
						if (reload) {
							document.location.reload();
						}
				  });
				} else {
					$.ajax({url: url, type: method, data: {id: id, value: 'NONE'}}).done(function() {
						if (reload) {
							document.location.reload();
						}
					});
				}

			}

			table.on('change', 'input[type="checkbox"]', function (evt) {
				var selectBoxCancelBtn = $('#project-workflows-select button[type="cancel"]');
				if (selectBoxCancelBtn.length == 1) {
					selectBoxCancelBtn.click();
				}
				var btn = $(evt.currentTarget);
				var selectedWAOption = btn.prop("chosenAutomationWorkflowOption");
				var $row = btn.parents('tr').first();
				var checked = btn[0].checked;
				var pluginType = table.fnGetData($row.get(0))['pluginType'];
				var disabledPluginPopup = $("#disabled-plugin").formDialog();

				var projectId = conf.projectId;
				var pluginId = table.fnGetData($row.get(0))['id'];
				var pluginN = table.fnGetData($row.get(0))['name'];
				var pluginName = disabledPluginPopup.find($("#pluginName"));

				pluginName.empty();
				pluginName.append(pluginN);

				if (pluginId.indexOf("jirasync") != -1 || pluginId.indexOf("workflow.automjira") != -1) {
					configurePopup(pluginId);
				} else {
					var label = disabledPluginPopup.find($("#msgDisablePlugin"));
					label.empty();
					label.append(translator.get('message.disabled.plugins'));
				}

				var data = table.fnGetData($row);
				data['enabled'] = checked;

				var url = urlPlugin(projectId, pluginId);
				var method = (btn[0].checked) ? 'POST' : 'DELETE';
				var newType = (btn[0].checked) ? 'REMOTE_WORKFLOW' : selectedWAOption;
				if (checked === false) {
					if (data['hasConf'] === true && pluginId !== "squash.tm.wizard.campaignassistant") {
						disabledPluginPopup.formDialog("open");
						disablePluginWithConf(url, checked, btn, data, pluginType);
					} else {
						disablePluginWithoutConf(url, checked, btn, data);
					}
				} else {
					$.ajax({url: url, type: 'POST'}).success(function () {
						/*when we activate the plugin, we update the automation workflow list*/
						if (pluginType === 'AUTOMATION') {
							updateAutomationWorkflowSelect(checked, projectId, false);
							eventBus.trigger("project.plugin.toggled", newType);
						}
						data['enabled'] = true;

					}).error(function (event) {
						putBackButtonSwitch(btn, checked, data, event);
						configureSwitch($row, data);
					});
				}
				configureStyle($row, data);

			});

			/**/

			function disablePlugin(url, checked, btn, data, saveConf) {
				var $row = btn.parents('tr').first();
				var pluginType = table.fnGetData($row.get(0))['pluginType'];
				var pluginId = table.fnGetData($row.get(0))['id'];
				var projectId = conf.projectId;
				var newType = (btn[0].checked) ? 'REMOTE_WORKFLOW' : btn.prop("chosenAutomationWorkflowOption");
				$.ajax({url: url, type: 'DELETE', data: {saveConf: saveConf}}).success(function () {
					/*when we activate or deactivate the plugin, we update the automation workflow list*/
					if (pluginType === 'AUTOMATION') {
						updateAutomationWorkflowSelect(checked, projectId, true);
						eventBus.trigger("project.plugin.toggled", newType);
					} else if (data['hasConf'] === true) {
						document.location.reload();
					}
					data['enabled'] = false;

				}).error(function (event) {
					putBackButtonSwitch(btn, checked, data, event);
				});

			}

			function disablePluginWithConf(url, checked, btn, data, pluginType) {
				$("#saveConf").prop("checked", true);
				var disabledPluginPopup = $("#disabled-plugin").formDialog();

				disabledPluginPopup.one("formdialogconfirm", function () {
					var saveConf = $("#saveConf").prop("checked");
					disablePlugin(url, checked, btn, data, saveConf);
				});

				disabledPluginPopup.on("formdialogcancel", function () {
					disabledPluginPopup.formDialog("close");
				});

				disabledPluginPopup.on("formdialogclose", function() {
					document.location.reload();
				});
			}

			function disablePluginWithoutConf(url, checked, btn, data) {
				var saveConf = false;
				disablePlugin(url, checked, btn, data, saveConf);
			}
		};
});
