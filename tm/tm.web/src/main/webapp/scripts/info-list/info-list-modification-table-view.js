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
define(["jquery", "backbone", "underscore", "squash.basicwidgets", "jeditable.simpleJEditable",
		"workspace.routing", "./NewInfoListItemDialog", "./IconSelectDialog", "squash.translator",
		"app/lnf/Forms", "app/util/StringUtil",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.formdialog",
		"jquery.squash", "jqueryui",  "jquery.squash.confirmdialog", "jquery.squash.messagedialog" ],
		function($, backbone, _, basic,	SimpleJEditable, routing, NewInfoListItemDialog, IconSelectDialog, translator, Forms, StringUtil) {
	"use strict";

	translator.load(["label.infoListItems.icon.none",
		"dialog.delete.info-list-item.isDefault",
		"dialog.delete.info-list-item.used.message",
		"dialog.delete.info-list-item.unused.message",
		"label.Delete"]);

	var TableView = Backbone.View.extend({
		el : "#table-view",
		initialize : function(config) {
			this.config = config;
			_.bindAll(this, "openAddItemPopup");
			this.initErrorPopup();
			this.tableInit();
			this.configureDeleteInfoListItemPopup();
			this.configureChangeLabelPopup();
			this.configureChangeCodePopup();
			this.configureReindexPopup();
			this.$("#add-info-list-item-button").on("click", this.openAddItemPopup);
		},

		events : {
			"click .isDefault>input:radio" : "changeDefaultOption",
			"click td.opt-label" : "openChangeLabelPopup",
			"click td.opt-code" : "openChangeCodePopup",
			"click .sq-icon" : "openChangeIconPopup",
			"click .sq-icon-noicon" : "openChangeIconPopup", // cf. drawIcon to know why
			"click td.delete-button" : "openDeleteOptionPopup"
		},

		initErrorPopup : function() {
			this.errorPopup = $("#generic-error-dialog").messageDialog();
		},

		tableInit : function() {

			this.optionsTable = this.$("#info-list-item-table");
			var self = this;

			var squashSettings = {
				enableDnD : true,
				buttons : [ {
					tooltip : translator.get("label.Delete"),
					tdSelector : "td.delete-button",
					uiIcon : "ui-icon-trash",
					jquery : true
				} ],

				functions : {
					dropHandler : function(dropData) {
						var url = routing.buildURL('info-list.position', self.config.data.infoList.id);
						$.post(url, dropData, function() {
							self.optionsTable._fnAjaxUpdate();
						});
					},
					drawIcon : function(value, cell) {
						// rem: handlebars would be nicer...
						// rem: we cannot put .sq-icon when no icon because (1) it sets a width, (2) cell is centered => broken rendering
						var tpl = '<span class="@class@">@text@</span>';
						var span = tpl.replace(/@class@/, (value === "noicon" ? "" : "sq-icon ") + "sq-icon-" + value).replace(/@text@/, value === "noicon" ? translator.get("label.infoListItems.icon.none") : "");
						cell.html(span);
					}
				}

			};
			this.optionsTable.squashTable({
				"bServerSide" : false
			}, squashSettings);
		},

		changeDefaultOption : function(event) {
			var self = this;
			// It's now radio
			var radio = event.currentTarget;

			if (!radio.checked) {
				radio.checked = true;
				notification.showError("ERROR");
				return;
			}

			var cell = radio.parentElement;
			var row = cell.parentElement;
			var data = self.optionsTable.fnGetData(row);

			$.ajax({
				url : routing.buildURL("info-list-item.info", data['entity-id']),
				type : 'POST',
				data : {
					id : 'info-list-item-default'
				}
			}).done(function() {
				self.optionsTable.find(".isDefault>input:radio").prop("checked", false);
				radio.checked = true;
			}).fail(function() {
				radio.checked = !radio.checked;
			});
		},

		openChangeLabelPopup : function(event) {
			var self = this;
			var labelCell = event.currentTarget;

			var row = labelCell.parentElement;
			var data = this.optionsTable.fnGetData(row);
			var id = data['entity-id'];
			var value = $(labelCell).text();

			self.ChangeLabelPopup.find("#rename-popup-info-list-item-id").val(id);
			self.ChangeLabelPopup.formDialog("open");
			self.ChangeLabelPopup.find("#rename-popup-info-list-item-label").val(value);
		},

		openChangeIconPopup : function(event) {
			var self = this;

			var labelCell = event.currentTarget;
			var row = labelCell.parentElement.parentElement;
			var data = self.optionsTable.fnGetData(row);
			var id = data['entity-id'];
			var iconName = data['iconName'];

			function discard() {
				self.newIconDialog.off("selectIcon.cancel selectIcon.confirm");
				self.newIconDialog.undelegateEvents();
				self.newIconDialog = null;
			}

			function discardAndRefresh(icon) {
				discard();

				$.ajax({
					url : routing.buildURL("info-list-item.info", id),
					type : 'POST',
					data : {
						id : 'info-list-item-icon',
						value : icon
					}
				}).done(function() {
					self.optionsTable._fnAjaxUpdate();

				}).fail(function() {

				});
			}

			self.newIconDialog = new IconSelectDialog({
				el : "#choose-item-icon-popup",
				model : {
					icon : "sq-icon-" + iconName
				}
			});

			self.newIconDialog.on("selectIcon.cancel", discard);
			self.newIconDialog.on("selectIcon.confirm", discardAndRefresh);
		},

		configureChangeLabelPopup : function() {
			var self = this;

			var dialog = $("#rename-info-list-item-popup");
			this.ChangeLabelPopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function() {
				self.changeLabel.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);

		},

		changeLabel : function() {
			var self = this;
			var id = self.ChangeLabelPopup.find("#rename-popup-info-list-item-id").val();
			var newValue = self.ChangeLabelPopup.find("#rename-popup-info-list-item-label").val();

			$.ajax({
				type : 'POST',
				data : {
					id : 'info-list-item-label',
					'value' : newValue
				},
				url : routing.buildURL("info-list-item.info", id)
			}).done(function() {
				self.optionsTable._fnAjaxUpdate();
				self.ChangeLabelPopup.formDialog('close');
			});
		},

		openDeleteOptionPopup : function(event) {
			var self = this;
			var cell = event.currentTarget;

			var row = cell.parentElement;
			var data = self.optionsTable.fnGetData(row);
			var id = data['entity-id'];

			$.ajax({
				type : 'GET',
				url : routing.buildURL('info-list.defaultItem', self.config.data.infoList.id)
			}).done(function(defaultItemId) {

				if (defaultItemId === id) {
					self.errorPopup.find('.generic-error-main').html(translator.get("dialog.delete.info-list-item.isDefault"));
					self.errorPopup.messageDialog('open');
				} else {

					var message = $("#delete-info-list-item-warning");
					var reindexWarn = $("#delete-info-list-item-warning-reindex");
					$.ajax({
						type : 'GET',
						url : routing.buildURL('info-list-item.isUsed', id)
					}).done(function(isUsed) {

						if (isUsed === true) {
							message.text(translator.get("dialog.delete.info-list-item.used.message"));
							reindexWarn.text(translator.get("dialog.info-list.warning.reindex.before"));
						} else {
							message.text(translator.get("dialog.delete.info-list-item.unused.message"));
						}
						self.DeleteInfoListItemPopup.data('isUsed', isUsed);
						self.DeleteInfoListItemPopup.find("#delete-info-list-item-popup-info-list-item-id").val(id);
						self.DeleteInfoListItemPopup.formDialog("open");

					});
				}
			});
		},

		configureReindexPopup : function() {
			var self = this;
			var reindexPopup = $("#reindex-popup");
			this.reindexPopup = reindexPopup.formDialog();

			reindexPopup.on('formdialogcancel', function() {
				self.reindexPopup.formDialog('close');
			});

			reindexPopup.on('formdialogconfirm', function() {
				document.location.href = squashtm.app.contextRoot + "/administration/indexes";
			});
		},

		openReindexPopup : function() {
			var self = this;
			self.reindexPopup.formDialog('open');
		},
		configureDeleteInfoListItemPopup : function() {
			var self = this;

			var dialog = $("#delete-info-list-item-popup");
			this.DeleteInfoListItemPopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function() {

				self.deleteInfoListItem.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);
		},

		deleteInfoListItem : function() {
			var self = this;
			var id = self.DeleteInfoListItemPopup.find("#delete-info-list-item-popup-info-list-item-id").val();

			var isUsed = self.DeleteInfoListItemPopup.data('isUsed');

			$.ajax({
				type : 'DELETE',
				url : routing.buildURL("info-list-item.delete", self.config.data.infoList.id, id)
			}).done(function(data) {
				self.optionsTable._fnAjaxUpdate();
				if (isUsed) {
					self.openReindexPopup();
				}
				self.DeleteInfoListItemPopup.formDialog('close');
			});
		},

		openChangeCodePopup : function(event) {
			var self = this;
			var codeCell = event.currentTarget;

			var row = codeCell.parentElement;
			var data = self.optionsTable.fnGetData(row);
			var id = data['entity-id'];
			var value = $(codeCell).text();
			var reindexWarn = $("#change-code-reindex-warn");

			//clean previous error message in popup
			Forms.input($("#change-code-popup-info-list-item-code")).clearState();

			$.ajax({
				type : 'GET',
				url : routing.buildURL('info-list-item.isUsed', id)
			}).done(function(isUsed) {

				if (isUsed === true) {
					reindexWarn.text(translator.get("dialog.info-list.warning.reindex.before"));
				} else {
					reindexWarn.text("");
				}

				self.ChangeCodePopup.find("#change-code-popup-info-list-item-id").val(id);
				self.ChangeCodePopup.formDialog("open");
				self.ChangeCodePopup.data('isUsed', isUsed);
				self.ChangeCodePopup.find("#change-code-popup-info-list-item-code").val(value);
			});
		},

		configureChangeCodePopup : function() {
			var self = this;

			var dialog = $("#change-code-info-list-item-popup");
			this.ChangeCodePopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function() {
				self.changeCode.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);

		},

		checkIfCodeExists : function checkIfCodeExists(newValue){
			var exists = false;


			return exists;
		},

		changeCode : function() {
			var self = this;
			var id = self.ChangeCodePopup.find("#change-code-popup-info-list-item-id").val();
			var newValue = self.ChangeCodePopup.find("#change-code-popup-info-list-item-code").val();
			var isUsed = self.ChangeCodePopup.data('isUsed');

			if (StringUtil.isBlank(newValue)){
				Forms.input($("#change-code-popup-info-list-item-code")).setState("error",
						translator.get("message.notBlank"));
				return;
			}

			$.ajax({
				type : 'GET',
				url : routing.buildURL("info-list-item.exist", newValue),
				data : {
					format : "exists"
				}
			}).done(function(data) {
				if (data.exists) {
					Forms.input($("#change-code-popup-info-list-item-code")).setState("error",
							translator.get("message.optionCodeAlreadyDefined"));
				}
				else {
					$.ajax({
						type : 'POST',
						data : {
							id : 'info-list-item-code',
							'value' : newValue
						},
						url : routing.buildURL("info-list-item.info", id)
					}).done(function() {
						self.optionsTable._fnAjaxUpdate();
						if (isUsed) {
							self.openReindexPopup();
						}
						self.ChangeCodePopup.formDialog('close');
					});
				}
			});

		},


		closePopup : function() {
			$(this).formDialog('close');
		},

		openAddItemPopup : function() {
			var self = this;

			function discard() {
				self.newItemDialog.off("newOption.cancel newOption.confirm");
				self.newItemDialog.undelegateEvents();
				self.newItemDialog = null;
			}

			function discardAndRefresh() {
				discard();
				refresh();
			}

			function refresh() {
				self.optionsTable._fnAjaxUpdate();
			}

			self.newItemDialog = new NewInfoListItemDialog({
				model : {
					"listId" : self.config.data.infoList.id
				}
			});

			self.newItemDialog.on("newOption.cancel", discard);
			self.newItemDialog.on("newOption.confirm", discardAndRefresh);
			self.newItemDialog.on("newOption.addanother", refresh);
		}

	});

	return TableView;

});
