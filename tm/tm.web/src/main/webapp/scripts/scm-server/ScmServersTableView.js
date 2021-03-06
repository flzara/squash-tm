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
define(['jquery', 'backbone', "squash.translator", "./AddScmServerDialog", "./DeleteScmServerDialog", "./DeleteMultipleScmServersDialog", "jquery.squash.messagedialog", 'squashtable'],
		function($, Backbone, translator, AddScmServerDialog, DeleteScmServerDialog, DeleteMultipleScmServersDialog) {
			"use strict";

	var ScmServersTableView = Backbone.View.extend({

		el : "#scm-server-table-pane",

		events: {
			"click #add-scm-server" : "openAddScmServerDialog",
			"click #delete-scm-servers" : "openDeleteMultipleScmServersDialog"
		},

		initialize : function() {

			this.initTable();
			this.AddScmServerDialog = new AddScmServerDialog(this.table);
			this.DeleteScmServerDialog = new DeleteScmServerDialog(this.table);
			this.DeleteMultipleScmServersDialog = new DeleteMultipleScmServersDialog(this.table);

			this.infoDialog = $('#generic-info-dialog').messageDialog();
		},

		/* ==== Table functions ==== */
		initTable : function() {

			var squashSettings = {
				deleteButtons : {
        	delegate : "#delete-scm-server-popup",
        	tooltip : translator.get('label.Remove')
        }
			};
			this.table = this.$el.find('table').squashTable(squashtm.datatable.defaults, squashSettings);
		},

		/* ==== Add ScmServer Popup functions =====*/
		openAddScmServerDialog : function() {
			if(squashtm.pageConfiguration.scmServerKinds.length === 0) {
				this.infoDialog.messageDialog('open');
			} else {
				this.AddScmServerDialog.open();
			}
		},

		/* ==== Delete Multiple ScmServers Popup function ==== */
		openDeleteMultipleScmServersDialog : function() {

			this.DeleteMultipleScmServersDialog.open();
		}

	});

	return ScmServersTableView;
});
