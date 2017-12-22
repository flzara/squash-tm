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
define([ "jquery", "backbone", "squash.translator", '../test-plan-panel/exec-runner',"jeditable.simpleJEditable", "workspace.projects",
         "squash.configmanager", "workspace.routing", "app/ws/squashtm.notification", 'test-automation/automated-suite-overview', "squashtable",
         "jqueryui", "jquery.squash.jeditable", "jquery.cookie"],
         function($, Backbone, translator, execrunner, SimpleJEditable, projects, confman, routing, notification, autosuitedialog) {

	var CampaignSearchResultTable = Backbone.View.extend({
		el : "#campaign-search-result-table",
		initialize : function(model, isAssociation, associateType, associateId) {
			this.model = model;
			this.getTableRowId = $.proxy(this._getTableRowId, this);
			this.tableRowCallback = $.proxy(this._tableRowCallback, this);

			var self = this;

			var tableConf = {
					"oLanguage" : {
						"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
					},
					"bServerSide": true,
					"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
					"fnServerParams": function ( aoData )
						{
							aoData.push( { "name": "model", "value": JSON.stringify(model) } );
							aoData.push( { "name": "campaign", "value": "campaign" } );
						},
					"sServerMethod": "POST",
					"bDeferRender" : true,
					"bFilter" : false,
					"fnRowCallback" : this.tableRowCallback,
					"fnDrawCallback" : this.tableDrawCallback,
					"sDom" : 'ft<"dataTables_footer"lip>'
				};

			var squashConf = {
				enableHover : true
			};

			this.$el.squashTable(tableConf, squashConf);

		},

		_getTableRowId : function(rowData) {
			return rowData[2];
		},

		manualHandler : function() {

			var $this = $(this);
			var	tpid = $this.data('tpid');
			var itId = $this.data('itid');

			var	ui = ($this.is('.run-popup')) ? "popup" : "oer";
			var newurl = squashtm.app.contextRoot + "/iterations/" + itId + "/test-plan/" + tpid + "/executions/new";

			$.post(newurl, {
				mode : 'manual'
			}, 'json').done(function(execId) {
				var execurl = squashtm.app.contextRoot + "/executions/" + execId + '/runner';
				if (ui === "popup") {
					execrunner.runInPopup(execurl);
				} else {
					execrunner.runInOER(execurl);
				}

			});
		},

		automatedHandler : function() {

			var row =$("#campaign-search-result-table").squashTable().fnGetData($(this).parent().parent());



			var	tpid = row['tpid'];
			var itId = row['iteration-id'];

			var url = squashtm.app.contextRoot + "/automated-suites/new";



			$.ajax({
				url : url,
				dataType:'json',
				type : 'post',
				data : {	testPlanItemsIds :[tpid],
					iterationId : itId}
			}).done(function(suite) {

				squashtm.context.autosuiteOverview.start(suite);
			});


		},

		_tableRowCallback : function(row, data, displayIndex) {

			// add the execute shortcut menu

			var automated = translator.get("test-case.execution-mode.AUTOMATED");
			var isTcDel = data['is-tc-deleted'];
			var isAutomated = data['itpi-isauto'];


			var tpId = data['itpi-id'];
			var itId = data['iteration-id'];
			var $td = $(row).find('.search-open-interface2-holder');

			var strmenu = $("#shortcut-exec-menu-template").html().replace(/#placeholder-tpid#/g, tpId);
			strmenu = strmenu.replace(/#placeholder-itid#/g, itId );

			$td.empty();
			$td.append(strmenu);

			// if the test case is deleted : just disable the whole thing
			// Plot twist  : Launch button has to be greyed
			if (isTcDel) {
				$td.find('.execute-arrow').addClass('disabled-transparent');
				$("#test-suite-execution-button").addClass('disabled-transparent');
			}

			// if the test case is manual : configure a button menu,
			// althgouh we don't want it
			// to be skinned as a regular jquery button
			else if (!isAutomated) {
				$td.find('.buttonmenu').buttonmenu({
					anchor : "right"
				});
				$td.on('click', '.run-menu-item', this.manualHandler);
			}

			// if the test case is automated : just configure the button
			else {
				$td.find('.execute-arrow').click(this.automatedHandler);
			}


				$(row).addClass("nonEditable");


			//add the search open tree icon
			var $cell = $(".search-open-tree-holder", row);
			$cell.append('<span class="search-open-tree"></span>')
				.click(function() {
					$.cookie("workspace-prefs", "ITERATION-" + itId, {path : "/"});
					window.location = squashtm.app.contextRoot + "campaign-workspace/";
				});
		},



		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return CampaignSearchResultTable;
});
