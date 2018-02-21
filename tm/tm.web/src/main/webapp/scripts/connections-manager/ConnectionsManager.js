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
define([ "jquery", "backbone", "./ConnectionsTable", "./export-history-popup", "jqueryui", "squashtable", "jquery.squash.formdialog"], function($, Backbone, ConnectionsTable, popup) {
	var View = Backbone.View.extend({
		el : "#connection-table-pane",

		initialize : function() {
		  popup.init();
			this.connectionsTable = new ConnectionsTable();
		},

		events : {
		  "change .filter_input" : "applyDateFilter",
		  "keyup #login_filter_input" : "applyLoginFilter",
		  "click #export-history-button" : "openExportDialog"
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
    },

    openExportDialog : function(event) {
      $("#export-connection-history-dialog").exportDialog("open");
    }
	});
	return View;
});
