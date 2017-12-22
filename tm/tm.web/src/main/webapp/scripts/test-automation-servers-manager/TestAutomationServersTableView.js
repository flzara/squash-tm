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
define([ 'jquery', 'backbone', "underscore", './NewTestAutomationServerDialogView', './NewTestAutomationServerModel', 'app/util/ButtonUtil',  'squash.translator',
         "app/ws/squashtm.notification", "jquery.squash.confirmdialog", 'squashtable',	'jqueryui', 'jquery.squash.formdialog' ],
         function($, Backbone, _, NewTestAutomationServerDialogView, NewTestAutomationServerModel, ButtonUtil, translator, notification) {
	"use strict";

	var tasTable = squashtm.app.tasTable;
	/*
	 * Defines the controller for the test automation server table.
	 */
	var NewTestAutomationServersTableView = Backbone.View.extend({
		el : "#test-automation-server-table-pane",
		initialize : function() {
			var self = this;

			_.bindAll(this, "removeTestAutomationServer", "setConfirmRemoveDialogState", "showDeleteTestAutomationServerDialog");

			// DOM initialized table
			this.table = this.$("table");
			this.table.squashTable(squashtm.datatable.defaults, {
				deleteButtons : {
					delegate : "#remove-test-automation-server-confirm-dialog",
					tooltip : translator.get('label.Remove')
				}
			});
			this.configureRemoveTASDialog();


			this.configureRemoveTASPanel();


		},

		events : {
			"click #add-test-automation-server" : "showNewTestAutomationServerDialog",
			"click #delete-test-automation-server" : "showDeleteTestAutomationServerDialog"
		},





		// ******** delete dialog init ***********

		configureRemoveTASPanel :  function(event) {
			var tableCf = $("#test-automation-server-table").squashTable();
			var self = this;
			var deleteDialog = $("#remove-test-automation-server-confirm-dialog").formDialog();
			deleteDialog.formDialog('setState','processing');
			deleteDialog.on('formdialogconfirm', function(){
				var removedIds = tableCf.getSelectedIds().join(',');
				var urlDelete = squashtm.app.contextRoot + "test-automation-servers/" + removedIds;
							$.ajax({
								type : 'DELETE',
								url : urlDelete
							}).done(function(){
								deleteDialog.formDialog('close');
								tableCf.refresh();
								});
					});

			this.configureRemoveTASPanel = deleteDialog;

		},

		showDeleteTestAutomationServerDialog :  function(event) {
			var self = this, showButton = event.target;
			var table = $("#test-automation-server-table").squashTable();
			function refresh() {
				self.table.squashTable().fnDraw();
			}

			function discard() {
				self.stopListening(self.newTasDialog);
				self.newTasDialog = null;
				ButtonUtil.enable($(showButton));
				self.table.squashTable().fnDraw();
			}

			function discardAndRefresh() {
				discard();
				self.table.squashTable().fnDraw();
			}
				if (table.getSelectedRows().size()>0){
					$("#remove-test-automation-server-confirm-dialog").formDialog('open');
				}
				else{
						notification.showError(translator.get('testautomation.exceptions.no-selection'));
				}
			},

		showNewTestAutomationServerDialog : function(event) {
			var self = this, showButton = event.target;

			function refresh() {
				self.table.squashTable().fnDraw();
			}

			function discard() {
				self.stopListening(self.newTasDialog);
			//	self.newTasDialog.undelegateEvents();
				self.newTasDialog.$el.formDialog('close');
				self.newTasDialog = null;
				ButtonUtil.enable($(showButton));
				self.table.squashTable().fnDraw();
			}

			function discardAndRefresh() {
				discard();
				self.table.squashTable().fnDraw();
			}

			ButtonUtil.disable($(event.target));
			self.newTasDialog = new NewTestAutomationServerDialogView({
				model : new NewTestAutomationServerModel()
			});

			self.listenTo(self.newTasDialog, "newtestautomationserver.cancel", discard);
			self.listenTo(self.newTasDialog, "newtestautomationserver.confirm", discardAndRefresh);
			self.listenTo(self.newTasDialog, "newtestautomationserver.confirm-carry-on", refresh);
		},

		configureRemoveTASDialog : function() {
			var self= this;

			var dialog = $("#remove-test-automation-server-confirm-dialog").formDialog();
			dialog.formDialog('setState','processing');
			dialog.on("formdialogopen", this.setConfirmRemoveDialogState);
			dialog.on("formdialogcancel", function() {
				dialog.formDialog('close');
			});
			dialog.on("formdialogclose", $.proxy(function() {
				this.toDeleteIds = [];
				this.table.deselectRows();
				dialog.formDialog('setState','processing');
			}, this));

			this.confirmRemoveTASDialog = dialog;

		},
		setConfirmRemoveDialogState : function(event){
			var self = this;
			var table = $("#test-automation-server-table").squashTable();
			var ids = table.getSelectedIds().join(',');
				$.ajax({
					url : squashtm.app.contextRoot +"test-automation-servers/"+ids+"/usage-status",
					type: "GET"
				}).then(function(status){
					if(!status.hasBoundProject && !status.hasExecutedTests){
						self.configureRemoveTASPanel.formDialog('setState','case1');
					}else if (!status.hasExecutedTests){
						self.configureRemoveTASPanel.formDialog('setState','case2');
					}else{
						self.configureRemoveTASPanel.formDialog('setState','case3');
					}
				});

		},

		removeTestAutomationServer : function(event) {
			var self = this,
				table = this.table;
			var ids = table.getSelectedIds();
			if (ids.length === 0) {
				return;
			}
			$.ajax({
				url : squashtm.app.contextRoot + "test-automation-servers/" + ids.join(','),
				type : 'delete'
			})
			.then(function(){
				table.refresh();
				self.confirmRemoveTASDialog.formDialog('close');
			})
			.fail(function(wtf){
				try {
					squashtm.notification.handleJsonResponseError(wtf);
				} catch (wtf) {
					squashtm.notification.handleGenericResponseError(wtf);
				}
			});
		}

	});
	return NewTestAutomationServersTableView;
});
