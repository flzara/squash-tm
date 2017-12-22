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
define([ "jquery", "backbone", "squashtable", "jqueryui" ], function($, Backbone) {

	function extractDefinitionText(row, data) {
		var descriptionCell = $(row).find("td.description");
		var text = descriptionCell.text();
		if (text.length > 150) {
			text = text.slice(0, 150) + "[...]";
		}
		descriptionCell.html(text);
	}

	var View = Backbone.View.extend({
		el : "#teams-table",
		initialize : function() {
			var self = this, tableConf = {
				"oLanguage" : {
					"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
				},
				"sAjaxSource" : squashtm.app.contextRoot + "/administration/teams",
				"bDeferRender" : true,
				"bFilter" : true,
				"fnRowCallback" : this.teamTableRowCallback,
				"aaSorting" : [ [ 2, "asc" ] ],
				"aoColumnDefs" : [ {
					"bVisible" : false,
					"bSortable" : false,
					"aTargets" : [ 0 ],
					"mDataProp" : "entity-id",
					"sClass" : "entity-id"
				}, {
					"aTargets" : [ 1 ],
					"mDataProp" : "entity-index",
					"bSortable" : false,
					"sClass" : "select-handle centered"
				}, {
					"aTargets" : [ 2 ],
					"mDataProp" : "name",
					"sClass" : "name",
					"bSortable" : true
				}, {
					"aTargets" : [ 3 ],
					"mDataProp" : "description",
					"sClass" : "description",
					"bSortable" : false
				}, {
					"aTargets" : [ 4 ],
					"mDataProp" : "nb-associated-users",
					"bSortable" : true
				}, {
					"aTargets" : [ 5 ],
					"mDataProp" : "created-on",
					"bSortable" : true
				}, {
					"aTargets" : [ 6 ],
					"mDataProp" : "created-by",
					"bSortable" : true
				}, {
					"aTargets" : [ 7 ],
					"mDataProp" : "last-mod-on",
					"bSortable" : true
				}, {
					"aTargets" : [ 8 ],
					"mDataProp" : "last-mod-by",
					"bSortable" : true
				}, {
					"aTargets" : [ 9 ],
					"mDataProp" : "empty-delete-holder",
					"sClass" : "centered delete-button",
					"sWidth" : "2em",
					"bSortable" : false
				} ],
				"sDom" : 'ft<"dataTables_footer"lp>'
			}, squashConf = {
				enableHover : true,
				bindLinks : {
					list : [ {
						target : 2,
						url : squashtm.app.contextRoot + "/administration/teams/{entity-id}",
						isOpenInTab : false
					} ]
				},
				deleteButtons : {
					url : squashtm.app.contextRoot + "/administration/teams/{entity-id}",
					popupmessage : squashtm.app.teamsManager.table.deleteButtons.popupmessage,
					tooltip : squashtm.app.teamsManager.table.deleteButtons.tooltip,
					success : function() {
						self.refresh();
					},
					fail : function() {
					}
				}

			};

			this.$el.squashTable(tableConf, squashConf);
		},

		teamTableRowCallback : function(row, data, displayIndex) {
			extractDefinitionText(row, data);
			return row;
		},

		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return View;
});