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
		[ "jquery", "backbone", "underscore", "squash.translator", "workspace.routing", "app/pubsub", "jquery.squash.togglepanel",
		  "jqueryui", "squashtable", "jquery.switchButton"],

		function($, Backbone, _, translator, routing, pubsub) {

			translator.load(["label.Enabled", "label.disabled", "label.Configure"]);


			function configureSwitch($row, data){
				var switchcell = $row.find('.plugin-enabled');

				var chk = $("<input>", {
					'type' : 'checkbox'
				});

				if (data['enabled']){
					chk.prop('checked', true);
				}

				switchcell.empty().append(chk);

				chk.switchButton({
					on_label : translator.get('label.Enabled'),
					off_label : translator.get('label.disabled')
				});
			}

			function configureStatus($row, data){
				var statscell = $row.find('.plugin-status');

				// TODO : use dedicated css classes once the image are available
				var css = (data['status'] === "OK") ? 'exec-status-success' : 'exec-status-blocked';
				var sp = $("<span>", {
					'class' : 'sq-icon ' + css
				});

				statscell.empty().append(sp);
			}

			function configureLink($row, data){
				var confcell = $row.find('.plugin-configure');
				confcell.text(translator.get('label.Configure'));
			}

			function noNavigation(evt){
				evt.preventDefault();
			}

			function configureStyle($row, data){
				var cells = $row.find('>td').not('.plugin-index, plugin-enabled'),
					state = data['enabled'];

				if (state){
					cells.removeClass('disabled-transparent');
					cells.off('click', noNavigation);
				}
				else{
					cells.addClass('disabled-transparent');
					cells.on('click', noNavigation);
				}
			}


			return function(conf){

				var table = $("#plugins-table");

				table.squashTable({
					aaData : conf.plugins,
					bServerSide : false,
					fnRowCallback : function(row, data, displayIndex) {

						var $row = $(row);
						// enable switch setup
						configureSwitch($row, data);

						// change the style
						configureStyle($row, data);

						// enable the status
						configureStatus($row, data);

						// 'configure' link placeholder if plugin is configurable
						if(!!data.configUrl) {
							configureLink($row, data);
						}
					}
				},{});


				function updateAutomationWorkflowSelect(checked, projectId){

					var url = squashtm.app.contextRoot + 'generic-projects/' + projectId;
					var method= 'POST';
					var id = 'change-automation-workflow'
					var activateTab = $(document.activeElement.id);

					if(checked==true){
							$.ajax({url : url, type : method, data: {id: id, value: 'REMOTE_WORKFLOW'}})
							 .success(function(){
									document.location.reload();
									activateTab.attr('aria-selected',true);
							 });
					}else{
							$.ajax({url : url, type : method, data: {id: id, value: 'NONE'}})
							 .success(function(){
									document.location.reload();
									activateTab.attr('aria-selected',true);
							 });
					}

				}
				table.on('change', 'input[type="checkbox"]', function(evt){
					var btn = $(evt.currentTarget);
					var $row = btn.parents('tr').first();
					var checked = btn[0].checked;
					var pluginType = table.fnGetData($row.get(0))['pluginType']

					var projectId = conf.projectId,
						pluginId = table.fnGetData($row.get(0))['id'];

					var data = table.fnGetData($row);
					data['enabled'] = checked;

					var url = routing.buildURL('project-plugins', projectId, pluginId),
						method = (btn[0].checked) ? 'POST' : 'DELETE';

					$.ajax({url : url, type : method}).success(function() {
						pubsub.publish("project.plugin.toggled");
						/*when we activate or deactivate the plugin, we update the automation workflow list*/
						if(pluginType == 'AUTOMATION'){
							updateAutomationWorkflowSelect(checked, projectId);
						}

					}).error(function(event) {
						btn.switchButton("option", "checked", !checked);
						data['enabled'] = true;
						configureSwitch($row, data);
					});


					configureStyle($row, data);

				});

			};

});
