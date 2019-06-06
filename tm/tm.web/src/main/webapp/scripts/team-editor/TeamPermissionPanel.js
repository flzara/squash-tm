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
define(["jquery", "backbone", "underscore", "app/util/StringUtil", "app/ws/squashtm.notification", 'squash.translator',
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog", 'jquery.squash.formdialog'],
	function ($, Backbone, _, StringUtil, notification, translator) {

		$.ajaxPrefilter(function (options, originalOptions, jqXHR) {
			var token = $("meta[name='_csrf']").attr("content");
			var header = $("meta[name='_csrf_header']").attr("content");
			jqXHR.setRequestHeader(header, token);
		});
		var teamMod = squashtm.app.teamMod;
		var TeamPermissionPanel = Backbone.View.extend({
			el: "#permissions",
			initialize: function () {

				this.configureTable();
				this.configurePopups();
				this.configureNoPermissionSelectedDialog();
				this.configureRemovePermissionDialog();
				this.configureButtons();
			},
			events: {
				"change .permission-list": "changePermission"
			},

			changePermission: function (event) {
				var select = $(event.target);
				var permission_id = select.val();
				var project_id = select.attr('id').replace("permission-list-", "");

				$.ajax({
					type: 'POST',
					url: teamMod.permission.url.add,
					data: {
						project: project_id,
						permission: permission_id
					},
					dataType: "json",
					success: function () {

					}
				});
			},

			configureTable: function () {
				$("#permission-table").squashTable({
					"fnRowCallback": function (nRow, data) {
						var select = $("#permission-table-templates select").clone();
						select.attr('id', 'permission-list-' + data["project-id"]);
						select.val(data['permission-name']);
						$('.permission-select', nRow).empty().append(select);
					}
				}, {
					unbindButtons: {
						delegate: "#remove-permission-dialog",
						tooltip: translator.get('dialog.unbind-ta-project.tooltip')
					}
				});
			},

			configurePopups: function () {
				this.configureLicenseInformationDialog();
				this.configureAddPermissionDialog();
				this.configureRemovePermissionDialog();
			},

			configureButtons: function () {
				var userLicenseInformation = squashtm.app.userLicenseInformation;
				if(userLicenseInformation != null && userLicenseInformation.length !== 0){
					this.$("#add-permission-button").on('click', function (){
						$("#license-information-dialog").formDialog('open');
					});
				} else {
					this.$("#add-permission-button").on('click', $.proxy(this.openAddPermission, this));
				}
				this.$("#remove-permission-button").on('click', $.proxy(this.confirmRemovePermission, this));
			},

			confirmRemovePermission: function (event) {
				var hasPermission = ($("#permission-table").squashTable().getSelectedIds().length > 0);
				if (hasPermission) {
					this.confirmRemovePermissionDialog.confirmDialog("open");
				} else {
					notification.showError(translator.get('message.NoPermissionSelected'));
				}
			},

			configureNoPermissionSelectedDialog: function () {
				this.noPermissionSelectedDialog = this.$("#no-selected-permissions").messageDialog();
			},

			openAddPermission: function () {
				this.addPermissionDialog.formDialog('open');
			},

			removePermissions: function (event) {
				var table = $("#permission-table").squashTable();
				var ids = table.getSelectedIds();
				$.ajax({
					url: teamMod.permission.url.remove,
					type: 'post',
					data: {
						project: ids[0]
					}
				}).done(function () {
					$("#permission-table").squashTable().refresh();
				});

			},

			addPermission: function (event) {
				var dialog = this.addPermissionDialog;
				var name = dialog.find('#add-permission-input').val();
			},

			configureRemovePermissionDialog: function () {
				this.confirmRemovePermissionDialog = $("#remove-permission-dialog").confirmDialog({width: 300});
				this.confirmRemovePermissionDialog.on("confirmdialogconfirm", $.proxy(this.removePermissions, this));
			},

			configureAddPermissionDialog: function () {
				var addPermissionDialog = $("#add-permission-dialog").formDialog();
				var table = $("#permission-table").squashTable();

				addPermissionDialog.on("formdialogadd-another", function () {
					addPermissionDialog.addOnePermission(function () {
						addPermissionDialog.initialize();
						$("#permission-table").squashTable().refresh();
					});
				});

				addPermissionDialog.on("formdialogadd-close", function () {
					addPermissionDialog.addOnePermission(function () {
						addPermissionDialog.formDialog('close');
						$("#permission-table").squashTable().refresh();
					});
				});

				addPermissionDialog.on("formdialogcancel", function () {
					addPermissionDialog.formDialog('close');
				});

				addPermissionDialog.on("formdialogconfirm", $.proxy(this.addPermission, this));

				addPermissionDialog.find('#add-permission-input').autocomplete();

				addPermissionDialog.on('formdialogopen', function () {
					addPermissionDialog.initialize();
				});

				addPermissionDialog.addOnePermission = function (callback) {
					$.ajax({
						type: 'POST',
						url: teamMod.permission.url.add,
						data: {
							project: $("#project-input").val(),
							permission: $("#permission-input").val()
						},
						dataType: "json",
						success: function () {
							callback();
						}
					});
				};

				addPermissionDialog.initialize = function () {
					var dialog = addPermissionDialog;
					var input = dialog.find('#add-permission-input');
					dialog.formDialog('setState', 'wait');

					$.ajax({
						url: teamMod.permission.url.popup,
						dataType: 'json'
					}).success(function (json) {
						if (json.myprojectList.length === 0) {
							dialog.formDialog('setState', 'no-more-projects');
						} else {
							$("#project-input").html("");
							for (var i = 0; i < json.myprojectList.length; i++) {
								var text = json.myprojectList[i].name;
								var value = json.myprojectList[i].id;
								var option = new Option(text, value);
								$(option).html(text); // for ie8
								$("#project-input").append(option);
							}
							dialog.formDialog('setState', 'main');
						}

					});
				};


				this.addPermissionDialog = addPermissionDialog;
			},

			configureLicenseInformationDialog: function () {
				// License information popup
				var userLicenseInformation = squashtm.app.userLicenseInformation;
				if(userLicenseInformation != null && userLicenseInformation.length !== 0){
					var userLicenseInformationArray = userLicenseInformation.split("-");
					var activeUsersCount = userLicenseInformationArray[0];
					var maxUsersAllowed = userLicenseInformationArray[1];
					var allowCreateUsers = JSON.parse(userLicenseInformationArray[2]);

					var licenseInformationDialog = $("#license-information-dialog");
					var message;
					if(allowCreateUsers){
						message = translator.get("information.userExcess.warning1", maxUsersAllowed, activeUsersCount);
						licenseInformationDialog.formDialog().on('formdialogclose', $.proxy(function () {
							licenseInformationDialog.formDialog('close');
							this.openAddPermission();
						}, this));
						licenseInformationDialog.formDialog().on('formdialogcancel', $.proxy(function () {
							licenseInformationDialog.formDialog('close');
							this.openAddPermission();
						}, this));
					} else {
						licenseInformationDialog.formDialog().on('formdialogclose', function () {
							licenseInformationDialog.formDialog('close');
						});
						licenseInformationDialog.formDialog().on('formdialogcancel', function () {
							licenseInformationDialog.formDialog('close');
						});
						message = translator.get("information.userExcess.warning2", maxUsersAllowed, activeUsersCount);
					}
					licenseInformationDialog.find("#information-message").html(message);
				}
			}
		});
		return TeamPermissionPanel;
	});
