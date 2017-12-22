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
/*
 settings :
 - isAdmin : boolean indicating if the user is administrator
 - tmProjectURL : the url of the TM project
 - availableServers : an array of TestAutomationServer
 - TAServerId : the id of the selected server if there is one, or null if none
 */
define([ "jquery","backbone","handlebars", "jeditable.selectJEditable", "./AddTAProjectsDialog", "./EditTAProjectDialog", "app/ws/squashtm.notification", "squash.translator", "squashtable", "jquery.squash.formdialog" ],
		function($, Backbone, Handlebars, SelectJEditable, BindPopup, EditTAProjectPopup, WTF, translator) {
			// *************************************** ConfirmChangePopup **********************************************
			var ConfirmChangePopup = Backbone.View.extend({

				el : "#ta-server-confirm-popup",

				initialize : function(conf) {
					this.changeUrl = conf.tmProjectURL + '/test-automation-server';
					var dialog = this.$el.formDialog();
				},

				events : {
					"formdialogconfirm" : "confirm",
					"formdialogcancel" : "cancel",
					"formdialogclose" : "close"
				},
				confirm : function() {
					var self = this;
					$.ajax({
						url : self.changeUrl,
						type : "post",
						data : {
							serverId : this.newSelectedId
						}
					}).done(function() {
						// trigger confirm success event with active selected id
						self.trigger("confirmChangeServerPopup.confirm.success", [ self.newSelectedId ]);
						self.selectedId = self.newSelectedId;
						self.newSelectedId = null;
						self.close();
					}).fail(function(wtf){
						WTF.handleJsonResponseError(wtf);
						// trigger confirm fail event with active selected id
						self.trigger("confirmChangeServerPopup.confirm.fail", [ self.selectedId ]);
						self.newSelectedId = null;
					});
				},
				cancel : function() {
					var self = this;
					// trigger cancel event with active selected id
					this.trigger("confirmChangeServerPopup.cancel", [ self.selectedId ]);
					self.newSelectedId = null;
					this.close();
				},

				close : function() {
					this.$el.formDialog("close");
				},

				show : function(newSelected) {
					var self = this;
					this.newSelectedId = newSelected;
					if(parseInt(this.selectedId,10) !== 0){
						this.$el.formDialog("open");
						this.$el.formDialog('setState', 'pleasewait');
						// edit state of popup depending on datas retrieved by ajax
						$.ajax(
								{
									url : squashtm.app.contextRoot + "/test-automation-servers/" + self.selectedId +
											"/usage-status",
									type : "GET"
								}).then(function(status) {
							if (!status.hasExecutedTests) {
								self.$el.formDialog('setState', 'case1');
							} else {
								self.$el.formDialog('setState', 'case2');
							}
						});
					}else{
						this.confirm();
					}


				},

				setSelected : function(selected) {
					this.selectedId = selected;
				},
				setParentPanel : function(parentPanel){
					this.parentPanel = parentPanel;
				}
			});

			// *************************************** UnbindPopup **********************************************

			var UnbindPopup = Backbone.View.extend({
				el : "#ta-projects-unbind-popup",

				initialize : function() {
					this.confirmSuccess = $.proxy(this._confirmSuccess, this);
					this.confirmFail = $.proxy(this._confirmFail, this);

					this.$el.formDialog();
				},
				events : {
					'formdialogopen' : 'open',
					'formdialogconfirm' : 'confirm',
					'formdialogcancel' : 'cancel'
				},
				confirm : function() {
					this.trigger("unbindTAProjectPopup.confirm");
					$.ajax({
						url : squashtm.app.contextRoot + "/test-automation-projects/" + this.deletedId,
						type : "delete"
					}).done(this.confirmSuccess).fail(this.confirmFail);
				},
				open : function(){
					var self = this;
					this.deletedId = this.$el.data('entity-id');
					this.jobName = this.$el.data('jobName');
					this.$el.formDialog("setState", "pleaseWait");

					function displayState(whichcase){
						var $removeP = self.$el.find(".remove-message");
						$removeP.empty();
						var source = $("#remove-message-tpl-"+whichcase).html();
						var template = Handlebars.compile(source);
						var message = template({jobName : self.jobName});
						$removeP.html(message);
						self.$el.formDialog('setState', whichcase);
					}

					// set state with or without warnin message depending on project's usage status
					$.ajax({
						url: squashtm.app.contextRoot + "/test-automation-projects/"+this.deletedId+"/usage-status",
						type : "get"
					}).then(function(status){
						if (!status.hasExecutedTests){
							displayState("case1");
						}else{
							displayState("case2");
						}
					});
				},
				_confirmSuccess : function(){
					this.trigger("unbindTAProjectPopup.confirm.success");
					this.$el.formDialog("close");
				},
				_confirmFail : function(xhr){
					this.trigger("unbindTAProjectPopup.confirm.fail");
					WTF.handleUnknownTypeError(xhr);
					this.$el.formDialog("close");
				},
				cancel : function() {
					this.trigger("unbindTAProjectPopup.cancel");
					this.$el.formDialog("close");
				},
				setParentPanel : function(parentPanel){
					this.parentPanel = parentPanel;
				}
			});
			// *************************************** AutomationPanel **********************************************

			var AutomationPanel = Backbone.View.extend({

				el : "#test-automation-management-panel",

				initialize : function(conf, popups) {
					var self = this;
					this.isAdmin = conf.isAdmin;
					this.popups = popups;
					for(var popup in popups){
						popups[popup].setParentPanel(this);
					}
					this.initSelect(conf);
					this.initTable();

					// listens to change-server-popup cancel and fail events to update the server's select-jeditable
					// status accordingly
					this.onChangeServerComplete = $.proxy(this._onChangeServerComplete, this);
					this.listenTo(self.popups.confirmChangePopup, "confirmChangeServerPopup.confirm.success",
							self.onChangeServerComplete);
					this.listenTo(self.popups.confirmChangePopup, "confirmChangeServerPopup.cancel",
							self.onChangeServerComplete);
					this.listenTo(self.popups.confirmChangePopup, "confirmChangeServerPopup.confirm.fail",
							self.onChangeServerComplete);
					this.refreshTable = $.proxy(this._refreshTable, this);
					this.listenTo(self.popups.bindPopup, "bindTAProjectPopup.confirm.success", self.refreshTable);
					this.listenTo(self.popups.unbindPopup, "unbindTAProjectPopup.confirm.success", self.refreshTable);
					this.listenTo(self.popups.editTAProjectPopup, "edittestautomationproject.confirm.success", self.refreshTable);
				},

				events : {
					"click #ta-projects-bind-button" : "openAuthenticationPopup"
				},

				initTable : function(){
					var self = this;
					this.table = $("#ta-projects-table").squashTable({}, {
						buttons:[{
								tdSelector:"td.edit-job-button",
								uiIcon : "edit-pencil",
								onClick : function(table, cell) {
									var row = cell.parentNode.parentNode;
									var jobId = table.getODataId(row);
									var data = table.getDataById(jobId);
									var taProject = {
											id : data['entity-id'],
											jobName :data["jobName"],
											label : data["label"],
											slaves : data["slaves"]
									};
									self.popups.editTAProjectPopup.$el.data('projectId', jobId).data('taProject', taProject);
									self.openAuthenticationPopup();
								}
							}
						]
				});
				},
				openBindPopup : function() {
					this.popups.bindPopup.show();
				},
				openAuthenticationPopup: function() {
					var self = this;
					if(this.isAdmin || this._isAuthenticated()) {
						this._checkWhichPopupWasCalledAndOpenIt();
						return;
					}
					var authDialog = $("#add-ta-projects-login-dialog").formDialog();
					authDialog.formDialog('open');
					
					/* Unbind formdialogconfirm in order not to have multiple bound events. */
					authDialog.off('formdialogconfirm');
					
					authDialog.on('formdialogconfirm', function() {
						var login = $("#login-dialog-login").val();
					    var password = $("#login-dialog-password").val();
						authDialog.data('login', login).data('password', password);
						authDialog.formDialog('close');
					    self._checkWhichPopupWasCalledAndOpenIt();
					});
					
					authDialog.on('formdialogcancel', function() {
						authDialog.formDialog('close');
					});
				},
				_checkWhichPopupWasCalledAndOpenIt: function() {
					var self = this;
					var projectId = self.popups.editTAProjectPopup.$el.data('projectId');
					var taProject = self.popups.editTAProjectPopup.$el.data('taProject');
					/* If there is a projectId and a taProject, the edit button was clicked. */
					if(projectId && taProject) {
						self.popups.editTAProjectPopup.show();
					/* Else, it is the add button that was pressed. */
					} else {
						self.openBindPopup();
					} 
				},
				_isAuthenticated: function() {
					var authDialog = $("#add-ta-projects-login-dialog");
					var login = authDialog.data('login');
					var password = authDialog.data('password');
					return (!!login && !!password); 
				},
				_refreshTable : function(){
					this.table.refresh();
				},
				// when the select jeditable popup completes we change the server's select-jeditable status accordingly.
				_onChangeServerComplete : function(newServerId) {
					this.selectServer.setValue(newServerId);
					this.table.refresh();
					var $addBlock = this.$el.find(".ta-projects-block");
					if(parseInt(newServerId,10) === 0){
						$addBlock.hide();
					}else{
						$addBlock.show();
					}
				},

				initSelect : function(conf) {
					var self = this;
					var data = {
						'0' : translator.get('label.NoServer')
					};

					for ( var i = 0, len = conf.availableServers.length; i < len; i++) {
						var server = conf.availableServers[i];
						data[server.id] = server.name;
					}

					if (conf.TAServerId !== null) {
						data.selected = conf.TAServerId;
					}
					this.popups.confirmChangePopup.setSelected(data.selected);
					var targetFunction = function(value, settings) {
						self.popups.confirmChangePopup.show(value);
						return value;
					};
					this.selectServer = new SelectJEditable({
						target : targetFunction,
						componentId : "selected-ta-server-span",
						jeditableSettings : {
							data : data
						}
					});
				}
			});


			// *************************************** automation panel **********************************************

			return {
				init : function(conf) {
					var popups = {
						unbindPopup : new UnbindPopup(conf),
						confirmChangePopup : new ConfirmChangePopup(conf),
						editTAProjectPopup : new EditTAProjectPopup(conf),
						bindPopup : new BindPopup(conf)
					};
					new AutomationPanel(conf, popups);
				}
			};

		});
