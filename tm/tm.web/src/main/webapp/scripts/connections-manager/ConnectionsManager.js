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
define([ "jquery", "backbone", "./ConnectionsTable", "jqueryui", "squashtable" ], function($, Backbone, ConnectionsTable, rangedatepicker) {
	var View = Backbone.View.extend({
		el : "#connection-table-pane",

		initialize : function() {
			this.connectionsTable = new ConnectionsTable();
			this.$(".table-tab-wrap").prepend("<div class='rangedatepicker th_input '>"
			                                + "<label>Date</label>"
                                      + "<input class='rangedatepicker-input' readonly='readonly'/>"
                                      + "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
                                      + "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
                                      + "</div>");
		},

		events : {
		  "change .filter_input" : "applyDateFilter"
		},

		applyDateFilter : function(event) {
		  var dateData = this.$(".filter_input").val();
		  this.connectionsTable.$el.squashTable().fnFilter(dateData, 3);
		}

	});


	return View;
});
