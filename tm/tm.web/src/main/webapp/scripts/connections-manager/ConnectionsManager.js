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
define([ "jquery", "backbone", "./ConnectionsTable", "jqueryui", "squashtable"], function($, Backbone, ConnectionsTable) {
	var View = Backbone.View.extend({
		el : "#connection-table-pane",

		initialize : function() {
			this.connectionsTable = new ConnectionsTable();
			this.$(".table-tab-wrap").prepend("<span class='rangedatepicker th_input '>"
                                        + "<label>Date</label>"
                                        + "<input class='rangedatepicker-input' readonly='readonly'/>"
                                        + "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
                                        + "<input id='date_filter_input' type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
                                      + "</span>"
                                      + "<span>"
      			                            + "<label> Login</label>"
                                        + "<input id='login_filter_input'/>"
                                      + "</span>");
		},

		events : {
		  "change .filter_input" : "applyDateFilter",
		  "keyup #login_filter_input" : "applyLoginFilter"
		},

		applyDateFilter : function(event) {
		  var dateData = $("#date_filter_input").val();
		  var table = this.connectionsTable.$el.squashTable();
      table.fnSettings().aoPreSearchCols[3].sSearch = dateData;
      table.fnDraw(true);
		},

		applyLoginFilter : function(event) {
    		  var loginData = $("#login_filter_input").val();
    		  var table = this.connectionsTable.$el.squashTable();
    		  table.fnSettings().aoPreSearchCols[2].sSearch = loginData;
          table.fnDraw(true);
    		}
	});
	return View;
});
