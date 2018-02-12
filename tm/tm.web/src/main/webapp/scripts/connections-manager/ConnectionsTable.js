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
define([ "jquery", "backbone", "squashtable", "jqueryui"], function($, Backbone) {
	var View = Backbone.View.extend({
		el : "#connections-table",
		initialize : function() {
			var self = this, tableConf = {
				"oLanguage" : {
					"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
				},
				"sAjaxSource" : squashtm.app.contextRoot + "/administration/connections",
				"bDeferRender" : true,
				"bServerSide" : true,
				"bFilter" : true,
				"aaSorting" : [ [ 0, "asc" ] ],
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
					"mDataProp" : "login",
					"bSortable" : true
				}, {
					"aTargets" : [ 3 ],
					"mDataProp" : "connection-date",
					"bSortable" : true
				}, {
					"aTargets" : [ 4 ],
					"mDataProp" : "successful",
					"mRender": function (data, type, full){
						var response = data;
						var language = window.squashtm.app.connectionsManager.settings.language;
					  if(data===true){
              response = language.yes;
					  } else if (data===false){
					    response = language.no;
					  }
					  return response;
					},
					"bSortable" : true
				} ],
				"sDom" : 't<"dataTables_footer"lp>'
			}, squashConf = {
				enableHover : true
			};

			this.$el.squashTable(tableConf, squashConf);
		}
	});

	return View;
});
