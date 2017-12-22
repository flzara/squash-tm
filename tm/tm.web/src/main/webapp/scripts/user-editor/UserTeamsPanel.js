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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil", "squash.translator", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil, translator) {
	var UMod = squashtm.app.UMod;
	var UserTeamsPanel = Backbone.View.extend({
		el : "#teams",
		initialize : function() {
			this.configureTable();
			this.configurePopups();
			this.configureButtons();
		},
		events : {},

		configurePopups : function() {
			this.configureRemoveTeamDialog();
			this.configureNoTeamSelectedDialog();
			this.configureAddTeamDialog();
		},
		configureButtons : function() {
			// ===============toogle buttons=================
			// this line below is here because toggle panel
			// buttons cannot be bound with the 'events'
			// property of Backbone.View.
			// my guess is that the event is bound to the button
			// before it is moved from it's "span.not-displayed"
			// to the toggle panel header.
			// TODO change our way to make toggle panels buttons
			// =============/toogle buttons===================

			this.$("#add-team-button").on('click', $.proxy(this.openAddTeam, this));
			this.$("#remove-teams-button").on('click', $.proxy(this.confirmRemoveTeam, this));
			
		},

		configureTable : function() {
			$("#teams-table").squashTable({}, {
				unbindButtons : {
					delegate : "#remove-teams-dialog",
					tooltip : translator.get('dialog.unbind-team.tooltip')
				}
			}); // pure DOM conf
		},
		confirmRemoveTeam : function(event) {
			var hasTeam = ($("#teams-table").squashTable().getSelectedIds().length > 0);
			if (hasTeam) {
				this.confirmRemoveTeamDialog.confirmDialog("open");
			} else {
				this.noTeamSelectedDialog.messageDialog('open');
			}
		},

		openAddTeam : function() {
			this.addTeamDialog.confirmDialog('open');
		},

		removeTeams : function(event) {
			var table = $("#teams-table").squashTable();
			var ids = table.getSelectedIds();
			if (ids.length === 0) {
				return;
			}

			$.ajax({
				url : UMod.user.url.simple + "teams/" + ids.join(','),
				type : 'delete'
			}).done($.proxy(table.refresh, table));

		},

		addTeam : function(event) {
			var dialog = this.addTeamDialog;
			var name = dialog.find('#add-team-input').val();
			var id = this.getIdOfNonAssociatedTeam(name);
			$.ajax({
				url : UMod.user.url.simple + "teams/" + id,
				type : 'PUT'
			}).success(function() {
				dialog.confirmDialog('close');
				$("#teams-table").squashTable().refresh();
			});
		},
		getIdOfNonAssociatedTeam : function(name) {
			var dialog = this.addTeamDialog;
			var nonAssociatedTeams = dialog.nonAssociatedTeams;
			if (nonAssociatedTeams.length > 0) {
				for ( var i = 0; i < nonAssociatedTeams.length; i++) {
					var nonAssociatedTeam = nonAssociatedTeams[i];
					if (nonAssociatedTeam.name === name) {
						return nonAssociatedTeam.id;
					}
				}
			}
			return 0;
		},
		configureRemoveTeamDialog : function() {
			this.confirmRemoveTeamDialog = $("#remove-teams-dialog").confirmDialog();
			this.confirmRemoveTeamDialog.on("confirmdialogconfirm", $.proxy(this.removeTeams, this));
		},

		configureNoTeamSelectedDialog : function() {
			this.noTeamSelectedDialog = $("#no-selected-teams").messageDialog();
		},

		configureAddTeamDialog : function() {
			var addTeamDialog = $("#add-team-dialog").confirmDialog();

			addTeamDialog.on("confirmdialogvalidate", function() {
				var name = addTeamDialog.find('#add-team-input').val();
				if (name === null || name === undefined || name.length === 0) {
					dialog.activate('no-selected-teams');
					return false;
				}else if (_.contains(addTeamDialog.find('#add-team-input').autocomplete( "option", "source" ),name)) {
					return true;
				} else {
					addTeamDialog.activate('invalid-team');
					return false;
				}
			});

			addTeamDialog.on("confirmdialogconfirm", $.proxy(this.addTeam, this));

			addTeamDialog.find('#add-team-input').autocomplete();

			addTeamDialog.on('confirmdialogopen', function() {
				var dialog = addTeamDialog;
				var input = dialog.find('#add-team-input');
				input.val("");
				dialog.activate('wait');
				dialog.nonAssociatedTeams = [];
				$.ajax({
					url : UMod.user.url.simple + "non-associated-teams",
					dataType : 'json'
				}).success(function(json) {
					if(json.status != 'ok'){
						dialog.activate(json.status);
					}else {
						var teams = json.teams;
						var source = _.map(teams, function(team) {
							return team.name;
						});
						input.autocomplete("option", "source", source);
						dialog.nonAssociatedTeams = teams;
						dialog.activate('main');
					} 
				});
			});

			addTeamDialog.activate = function(arg) {
				var cls = '.' + arg;
				this.find('div').not('.popup-dialog-buttonpane').filter(cls).show().end().not(cls).hide();
				if (arg !== 'main') {
					this.next().find('button:first').hide();
				} else {
					this.next().find('button:first').show();
				}
			};

			this.addTeamDialog = addTeamDialog;
		}
	});
	return UserTeamsPanel;
});