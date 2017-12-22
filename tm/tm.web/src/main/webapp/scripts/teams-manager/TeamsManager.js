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
define([ "jquery", "backbone", "./TeamsTable", "./NewTeamDialog", "./DeleteTeamDialog", "app/ws/squashtm.notification", "squash.translator", "jqueryui" ], function($, Backbone, TeamsTable,
		NewTeamDialog, DeleteTeamDialog, notification, translator) {
	var View = Backbone.View.extend({
		el : "#team-table-pane",

		initialize : function() {
			this.teamsTable = new TeamsTable();

			this.newTeamDialog = new NewTeamDialog({
				model : {
					name : "",
					description : ""
				}
			});

			this.deleteTeamDialog = new DeleteTeamDialog({
				model : {
					name : "",
					description : ""
				}
			});

			this.listenTo(this.newTeamDialog, "newteam.confirm", $.proxy(this.teamsTable.refresh, this.teamsTable));
			this.listenTo(this.deleteTeamDialog, "deleteteam.confirm", $.proxy(this.teamsTable.refresh, this.teamsTable));
		},

		events : {
			"click #new-team-button" : "showNewTeamDialog",
			"click #delete-team-button" : "showDeleteTeamDialog"
		},

		showNewTeamDialog : function(event) {
			this.newTeamDialog.show();
		},

		showDeleteTeamDialog : function(event) {

			var table = $("#teams-table").squashTable();
			var ids = table.getSelectedIds();

			if (ids.length === 0) {
				notification.showError(translator.get('message.noTeamSelected'));
			} else {
			 this.deleteTeamDialog.show();
		}

		}

	});

	return View;
});