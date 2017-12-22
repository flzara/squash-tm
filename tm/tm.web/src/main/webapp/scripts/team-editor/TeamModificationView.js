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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil", "./TeamPermissionPanel", 'squash.translator', "jquery.squash",
		"jqueryui", "jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog", "jquery.squash.jeditable", "jquery.squash.formdialog" ], function($,
		Backbone, _, StringUtil, TeamPermissionPanel, translator) {
	var teamMod = squashtm.app.teamMod;
	var TeamModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {

			this.configureEditables();

			this.configureRenamePopup();
			this.configureDeletionDialog();
			this.configureRemoveMemberDialog();
			this.configureNoMemberSelectedDialog();
			this.configureInvalidMemberSelectedDialog();
			this.configureAddMemberDialog();
			this.configureMembersTable();
			new TeamPermissionPanel();
			this.configureButtons();

		},

		events : {
			"click #delete-team-button" : "confirmTeamDeletion"
		},

		confirmTeamDeletion : function(event) {
			this.confirmDeletionDialog.confirmDialog("open");
		},

		confirmRemoveMember : function(event) {
			var hasMember = ($("#members-table").squashTable().getSelectedIds().length > 0);
			if (hasMember) {
				this.confirmRemoveMemberDialog.confirmDialog("open");
			} else {
				this.noMemberSelectedDialog.messageDialog('open');
			}
		},

		openAddMember : function() {
			this.addMemberDialog.confirmDialog('open');
		},

		deleteTeam : function(event) {
			var self = this;

			$.ajax({
				type : "delete",
				url : document.location.href
			}).done(function() {
				self.trigger("team.delete");
			});

		},

		removeMembers : function(event) {
			var table = $("#members-table").squashTable();
			var ids = table.getSelectedIds();
			if (ids.length === 0) {
				return;
			}

			$.ajax({
				url : document.location.href + "/members/" + ids.join(','),
				type : 'delete'
			}).done($.proxy(table.refresh, table));

		},

		addMember : function(event) {
			var dialog = this.addMemberDialog;
			var login = dialog.find('#add-member-input').val();

			$.ajax({
				url : document.location.href + "/members/" + login,
				type : 'PUT'
			}).success(function() {
				dialog.confirmDialog('close');
				$("#members-table").squashTable().refresh();
			});
		},

		replacePlaceHolderByValue : function(index, message, replaceValue) {
			var pattern = /\{[\d,\w,\s]*\}/;
			var match = pattern.exec(message);
			var pHolder = match[index];
			return message.replace(pHolder, replaceValue);
		},

		configureButtons : function() {
			$.squash.decorateButtons();
			// ===============toogle buttons=================
			// this line below is here because toggle panel
			// buttons cannot be bound with the 'events'
			// property of Backbone.View.
			// my guess is that the event is bound to the button
			// before it is moved from it's "span.not-displayed"
			// to the toggle panel header.
			// TODO change our way to make toggle panels buttons
			// =============/toogle buttons===================

			this.$("#remove-members-button").on('click', $.proxy(this.confirmRemoveMember, this));
			this.$("#add-member-button").on('click', $.proxy(this.openAddMember, this));

		},


		configureEditables : function() {
			var settings = {
				url : teamMod.teamUrl,
				ckeditor : {
					customConfig : squashtm.app.contextRoot + "styles/ckeditor/ckeditor-config.js",
					language : teamMod.richEditLanguageValue
				},
				placeholder : teamMod.richEditPlaceHolder,
				submit : teamMod.richEditsubmitLabel,
				cancel : teamMod.cancelLabel,
				indicator : '<div class="processing-indicator"/>'

			};

			$('#team-description').richEditable(settings).addClass("editable");
		},

		configureMembersTable : function() {
			
			var datatableSettings = {
				'fnDrawCallback' : function(){
					var table = this;
					this.find('tbody tr').each(function(){
						var data = table.fnGetData(this);
						if (!! data && data['user-active'] === false){
							$(this).addClass('disabled-transparent');
						}
					});
						
				}
			};
			
			$("#members-table").squashTable(datatableSettings, {
				
				unbindButtons : {
					delegate : "#remove-members-dialog",
					tooltip : translator.get('dialog.unbind-ta-project.tooltip')
					
				}
			}); 
		},

		renameTeam : function() {
			var newNameVal = $("#rename-team-input").val();
			$.ajax({
				type : 'POST',
				data : {
					'value' : newNameVal
				},
				dataType : "json",
				url : teamMod.teamUrl + "/name"

			}).done(function(data) {
				$('#team-name-header').html(data.newName);
				$('#rename-team-popup').formDialog('close');
			});
		},

		configureRenamePopup : function() {

			var dialog = $("#rename-team-popup");
			
			dialog.formDialog();
			
			dialog.on('formdialogconfirm', this.renameTeam);
			
			dialog.on('formdialogcancel', this.closePopup);
			
			dialog.bind("formdialogopen", function(event, ui) {
				var name = $.trim($('#team-name-header').text());
				$("#rename-team-input").val($.trim(name));
			});
			
			$("#rename-team-button").on('click', function(){
				dialog.formDialog('open');
			});

		},

		configureDeletionDialog : function() {
			this.confirmDeletionDialog = this.$("#delete-warning-pane").confirmDialog();
			this.confirmDeletionDialog.on("confirmdialogconfirm", $.proxy(this.deleteTeam, this));
		},

		configureRemoveMemberDialog : function() {
			this.confirmRemoveMemberDialog = this.$("#remove-members-dialog").confirmDialog();
			this.confirmRemoveMemberDialog.on("confirmdialogconfirm", $.proxy(this.removeMembers, this));
		},

		configureNoMemberSelectedDialog : function() {
			this.noMemberSelectedDialog = this.$("#no-selected-users").messageDialog();
		},

		configureInvalidMemberSelectedDialog : function() {
			this.invalidMemberSelectedDialog = this.$("#invalid-user").messageDialog();
		},
		
		configureAddMemberDialog : function() {
			var addMemberDialog = this.$("#add-member-dialog").confirmDialog();

			addMemberDialog.on("confirmdialogvalidate", function() {
				var login = addMemberDialog.find('#add-member-input').val();
				if (login === null || login === undefined || login.length === 0) {
					addMemberDialog.activate('no-selected-users');
					return false;
				} else if (_.contains(addMemberDialog.find('#add-member-input').autocomplete( "option", "source" ),login)) {
					return true;
				} else {
					addMemberDialog.activate('invalid-user');
					return false;
				}
			});

			addMemberDialog.on("confirmdialogconfirm", $.proxy(this.addMember, this));

			addMemberDialog.find('#add-member-input').autocomplete();

			addMemberDialog.on('confirmdialogopen', function() {
				var dialog = addMemberDialog;
				var input = dialog.find('#add-member-input');
				input.val("");
				dialog.activate('wait');
				$.ajax({
					url : document.location.href + "/non-members",
					dataType : 'json'
				}).success(function(json) {
					if (json.length > 0) {
						var source = _.map(json, function(user) {
							return user.login;
						});
						input.autocomplete("option", "source", source);
						dialog.activate('main');
					} else {
						dialog.activate('no-more-users');
					}
				});
			});

			addMemberDialog.activate = function(arg) {
				var cls = '.' + arg;
				this.find('div').not('.popup-dialog-buttonpane').filter(cls).show().end().not(cls).hide();
				if (arg !== 'main') {
					this.next().find('button:first').hide();
				} else {
					this.next().find('button:first').show();
				}
			};

			this.addMemberDialog = addMemberDialog;
		},

		closePopup : function() {
			$(this).formDialog('close');
		}

	});

	return TeamModificationView;
});