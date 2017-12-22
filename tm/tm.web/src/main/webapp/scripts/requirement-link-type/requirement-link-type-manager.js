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
define([ 'module',  "jquery", "backbone", "underscore", "squash.basicwidgets", "jeditable.simpleJEditable",
		"workspace.routing", "squash.translator", "app/lnf/Forms", "app/util/StringUtil","jquery.squash.togglepanel", "jquery.squash.formdialog", "squashtable", "app/ws/squashtm.workspace"],
		function(module, $, backbone, _, basic, SimpleJEditable, routing, translator, Forms, StringUtils) {
	"use strict";

	var config = module.config();

	//translator.load([]);

	var reqLinkTypeManagerView = Backbone.View.extend({
		el : "#link-types-table-pane",
		initialize : function() {
			this.basicInit();
			this.config = config;

			this.initTable();

			this.configureNewLinkTypePopup();
			this.configureChangeRolePopup();
			this.configureChangeCodePopup();

			this.initErrorPopup();
			this.configureDeleteTypePopup();
			this.configureDeleteMultipleTypesPopup();

		},
		basicInit : function() {
			basic.init();

		},

		initTable : function() {
			var squashSettings = {
    		searching : true,
    		buttons : [{
    			tooltip : translator.get("label.Delete"),
    			tdSelector : "td.delete-button",
    			uiIcon : "ui-icon-trash",
    			jquery : true
    		}]
    	};
			this.table = $("#requirement-link-types-table").squashTable(
				{
        	//"bServerSide" : false,
        	aaData : config.tableData.aaData
        },
        squashSettings
			);
		},

		events : {
			"click #add-link-type-btn" : "openAddLinkTypePopup",
			"click .isDefault>input:radio" : "changeDefaultType",
			"click td.opt-role1" : "openChangeRole1Popup",
			"click td.opt-role2" : "openChangeRole2Popup",
			"click td.opt-code1" : "openChangeCode1Popup",
			"click td.opt-code2" : "openChangeCode2Popup",
			"click td.delete-button" : "openDeleteTypePopup",
			"click #remove-selected-link-types" : "openDeleteMultipleTypesPopup"
		},

		/* ====== AddNewLinkType Popup functions ====== */
		configureNewLinkTypePopup : function(){

      var self = this;

      var dialog = $("#add-link-type-popup");
      this.AddLinkTypePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
				self.addNewLinkType(function() {
					self.table.refresh();
					self.AddLinkTypePopup.formDialog('close');
        });
      });

      dialog.on('formdialogaddanother', function() {
				self.addNewLinkType(function() {
        	self.table.refresh();
        	self.AddLinkTypePopup.formDialog('cleanup');
        });
      });

    	dialog.on('formdialogcancel', self.closePopup);
    },

		openAddLinkTypePopup : function(){
			var self = this;
			self.clearErrorMessages();
			self.AddLinkTypePopup.formDialog("open");
		},

		closePopup : function() {

    	$(this).formDialog('close');
    },

		clearErrorMessages : function() {
			Forms.input($("#add-link-type-popup-role1")).clearState();
      Forms.input($("#add-link-type-popup-role1-code")).clearState();
      Forms.input($("#add-link-type-popup-role2")).clearState();
      Forms.input($("#add-link-type-popup-role2-code")).clearState();
		},

		/**
    * Try to add the LinkType submitted by user.
    * Manage potential errors, post adding request and execute callback.
    */
    addNewLinkType(callback) {

			var self = this;

			var params = self.retrievePopupParams();

			self.clearErrorMessages();

			if(!self.checkBlankInputsAndDisplayErrors(params)) {

				self.checkCodesExistence(params).done(function(codesInfos) {

					if (!self.checkCodesExistenceAndDisplayErrors(codesInfos)) {

						self.doAddNewLinkType(params).then(callback);
					}
				});
			}
    },

		/**
		*	Check the four inputs, displays errors for the ones which are left blank.
		* @param params: {
    *						"role1",
    *						"role1Code",
    *						"role2",
    *						"role2Code"
    *					}
		* @return true if at least one is left blank, else returns false.
		*/
		checkBlankInputsAndDisplayErrors(params) {

			var newRole1 = params.role1;
      var newRole1Code = params.role1Code;
      var newRole2 = params.role2;
      var newRole2Code = params.role2Code;

			var oneInputIsBlank = false;

			if(StringUtils.isBlank(newRole1))	 {
        oneInputIsBlank = true;
      	Forms.input($("#add-link-type-popup-role1")).setState("error", translator.get("message.notBlank"));
      }
      if(StringUtils.isBlank(newRole1Code))	 {
            	Forms.input($("#add-link-type-popup-role1-code")).setState("error", translator.get("message.notBlank"));
            	oneInputIsBlank = true;
            }
      if(StringUtils.isBlank(newRole2))	 {
            	Forms.input($("#add-link-type-popup-role2")).setState("error", translator.get("message.notBlank"));
            	oneInputIsBlank = true;
            }
      if(StringUtils.isBlank(newRole2Code))	 {
      	Forms.input($("#add-link-type-popup-role2-code")).setState("error", translator.get("message.notBlank"));
        oneInputIsBlank = true;
      }

      return oneInputIsBlank;
		},

		/**
		* Check if the codes submitted already exist in database.
		* @param params: {
		*						"role1",
		*						"newRole1Code",
		*						"newRole2",
		*						"newRole2Code"
		*					}
		* @return Promise of codes-checking request.
		*/
		checkCodesExistence(params) {
      return $.ajax({
      	url : routing.buildURL("requirementLinkType.checkCodes"),
      	type : 'GET',
      	dataType: 'json',
      	data : params
      });
		},

		/**
		* Given codes information about their existence in database,
		*	displays errors for each code input.
		* @params codesInfos.
		* @return true if at least one code exists in database, else returns false.
		*/
		checkCodesExistenceAndDisplayErrors(codesInfos) {
			if(!codesInfos.areCodesAndRolesConsistent) {
				Forms.input($("#add-link-type-popup-role1-code")).setState("error", translator.get("requirement-version.link.type.rejection.codesAndRolesNotConsistent"));
				Forms.input($("#add-link-type-popup-role2-code")).setState("error", translator.get("requirement-version.link.type.rejection.codesAndRolesNotConsistent"));
				return true;
			}
			var oneCodeAlreadyExists = false;
			if(codesInfos.code1Exists) {
      	Forms.input($("#add-link-type-popup-role1-code")).setState("error", translator.get("requirement-version.link.type.rejection.codeAlreadyExists"));
        oneCodeAlreadyExists = true;
      }
      if(codesInfos.code2Exists) {
      	Forms.input($("#add-link-type-popup-role2-code")).setState("error", translator.get("requirement-version.link.type.rejection.codeAlreadyExists"));
      	oneCodeAlreadyExists = true;
      }
      return oneCodeAlreadyExists;
		},

		/**
		* Get the input values of the AddLinkTypePopup and returns it as a structured object.
		*/
		retrievePopupParams() {

			var self = this;

			var newRole1 = self.AddLinkTypePopup.find("#add-link-type-popup-role1").val();
      var newRole1Code = self.AddLinkTypePopup.find("#add-link-type-popup-role1-code").val();
      var newRole2 = self.AddLinkTypePopup.find("#add-link-type-popup-role2").val();
      var newRole2Code = self.AddLinkTypePopup.find("#add-link-type-popup-role2-code").val();

      var params = {
      	"role1" : newRole1,
        "role1Code" : newRole1Code,
        "role2" : newRole2,
        "role2Code" : newRole2Code
      };

      return params;
		},

		/**
		* Send Ajax Post Request to create the new RequirementVersionType.
		*	@param paramLinkType.
		*	@return Promise of Post Request.
		*/
		doAddNewLinkType : function(paramLinkType) {
			return $.ajax({
      	url : routing.buildURL("requirementLinkType"),
      	type : 'POST',
      	dataType: 'json',
      	data : paramLinkType
      });
		},

		/* ====== Change Default Type functions ====== */

		changeDefaultType : function(event) {
			var self = this;
			var radio = event.currentTarget;

			if(!radio.checked) {
				radio.checked = true;
			}

			var cell = radio.parentElement;
			var row = cell.parentElement;
			var data = self.table.fnGetData(row);

			// POST Modification
			$.ajax({
				url : routing.buildURL("requirement.link.type", data["type-id"]),
				type : 'POST',
				data : {
					id : 'requirement-link-type-default'
				}
				}).done(function() {
					self.table.find(".isDefault>input:radio").prop("checked", false);
      		radio.checked = true;
				}).fail(function() {
      		radio.checked = !radio.checked;
				});
		},

		/* ====== Change  Role functions ====== */

		configureChangeRolePopup : function() {
			var self = this;

      var dialog = $("#change-type-role-popup");
      this.ChangeRolePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.changeRole.call(self);
      });

      dialog.on('formdialogcancel', this.closePopup);
		},

		clearChangeRoleErrorMessage : function() {

    	Forms.input($("#change-type-role-popup-role")).clearState();
    },

		isRoleEditable(data) {
			if(data['type-role1'] === data['type-role2']
				&& data['type-role1-code'] === data['type-role2-code']) {
					return false;
			}
			return true;

		},

		openChangeRolePopup : function(event, roleNumber) {
			var self = this;
			// clear error messages
			self.clearChangeRoleErrorMessage();

			// fill label input
			var roleCell = event.currentTarget;

			var row = roleCell.parentElement;
			var data = this.table.fnGetData(row);

			if(!self.isRoleEditable(data)) {
				self.ErrorPopup.find('.generic-error-main').html(translator.get("requirement-version.link.type.rejection.roleLocked"));
				self.ErrorPopup.messageDialog('open');
				return;
			}

			var typeId = data['type-id'];
			var currentRole = $(roleCell).text();

			self.ChangeRolePopup.data('typeId', typeId);
			self.ChangeRolePopup.data('roleNumber', roleNumber);
			self.ChangeRolePopup.formDialog('open');
			self.ChangeRolePopup.find("#change-type-role-popup-role").val(currentRole);
    },

		openChangeRole1Popup : function(event) {

			this.openChangeRolePopup(event, 1);
		},

		openChangeRole2Popup : function(event) {

    	this.openChangeRolePopup(event, 2);
    },

		changeRole : function() {
			var self = this;
			var typeId = self.ChangeRolePopup.data('typeId');
			var roleNumber = self.ChangeRolePopup.data('roleNumber');
			var newRole = self.ChangeRolePopup.find("#change-type-role-popup-role").val();

			// Verifications
			if(StringUtils.isBlank(newRole))	 {
      	Forms.input($("#change-type-role-popup-role")).setState("error", translator.get("message.notBlank"));
      } else {
				self.doChangeRole(typeId, roleNumber, newRole);
      }
		},

		doChangeRole : function(typeId, roleNumber, newRole) {
			var self = this;
			var requestId = 'requirement-link-type-role' + roleNumber;

			$.ajax({
				url : routing.buildURL("requirement.link.type", typeId),
				type : 'POST',
				data : {
					id : requestId,
					value : newRole
				}
			}).done(function(data) {
				if(!data.areCodesAndRolesConsistent) {
					Forms.input($("#change-type-code-popup-code")).setState("error", translator.get("requirement-version.link.type.rejection.codesAndRolesNotConsistent"));
				} else {
					self.table.refresh();
					self.ChangeRolePopup.formDialog('close');
				}
			});

		},

		/* ====== Change Code functions ====== */

		configureChangeCodePopup : function() {
    	var self = this;

      var dialog = $("#change-type-code-popup");
      this.ChangeCodePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.changeCode.call(self);
   		});

      dialog.on('formdialogcancel', this.closePopup);
    },

    clearChangeCodeErrorMessage : function() {

    	Forms.input($("#change-type-code-popup-code")).clearState();
    },

		openChangeCode1Popup : function(event) {

    	this.openChangeCodePopup(event, 1);
    },

    openChangeCode2Popup : function(event) {

    	this.openChangeCodePopup(event, 2);
    },

		openChangeCodePopup : function(event, codeNumber) {
    	var self = this;
    	// clear error messages
    	self.clearChangeCodeErrorMessage();
    	// fill label input
    	var codeCell = event.currentTarget;

    	var row = codeCell.parentElement;
    	var data = this.table.fnGetData(row);
    	var typeId = data['type-id'];
    	var currentCode = $(codeCell).text();

    	self.ChangeCodePopup.data('typeId', typeId);
    	self.ChangeCodePopup.data('codeNumber', codeNumber);
    	self.ChangeCodePopup.formDialog('open');
    	self.ChangeCodePopup.find("#change-type-code-popup-code").val(currentCode);
    },

		changeCode : function() {
    	var self = this;
    	var typeId = self.ChangeCodePopup.data('typeId');
    	var codeNumber = self.ChangeCodePopup.data('codeNumber');
    	var newCode = self.ChangeCodePopup.find("#change-type-code-popup-code").val();

    	// Verifications
    	if(StringUtils.isBlank(newCode))	 {
         Forms.input($("#change-type-code-popup-code")).setState("error", translator.get("message.notBlank"));
      } else {
      	// Check code existence
				$.ajax({
					url : routing.buildURL("requirement.link.type", typeId),
					type : 'GET',
					data : {
						id : 'check-code',
						value: newCode
						}

					}).done(function(data) {
						if(data.codeExists) {
							Forms.input($("#change-type-code-popup-code")).setState("error", translator.get("requirement-version.link.type.rejection.codeAlreadyExists"));
						} else {
    					self.doChangeCode(typeId, codeNumber, newCode);
						}
					});
			}
    },

    doChangeCode : function(typeId, codeNumber, newCode) {
    	var self = this;
    	var requestId = 'requirement-link-type-code' + codeNumber;

    	$.ajax({
    		url : routing.buildURL("requirement.link.type", typeId),
    		type : 'POST',
    		data : {
    			id : requestId,
    			value : newCode
    		}
    	}).done(function(data) {
    		if(!data.areCodesAndRolesConsistent) {
    			Forms.input($("#change-type-code-popup-code")).setState("error", translator.get("requirement-version.link.type.rejection.codesAndRolesNotConsistent"));
    		} else {
					self.table.refresh();
					self.ChangeCodePopup.formDialog('close');
    		}
    	});

    },

		/* ====== Delete Type functions ====== */
		initErrorPopup : function() {

				this.ErrorPopup = $("#generic-error-dialog").messageDialog();
		},

		configureDeleteTypePopup : function() {

			var self = this;

      var dialog = $("#delete-link-type-popup");
      this.DeleteTypePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.deleteType.call(self);
      });

      dialog.on('formdialogcancel', this.closePopup);
		},

		openDeleteTypePopup(event) {
			var self = this;
      var cell = event.currentTarget;

      var row = cell.parentElement;
      var data = self.table.fnGetData(row);
      var typeId = data['type-id'];

      $.ajax({
      	type: 'GET',
      	url: routing.buildURL('requirement.link.type', typeId),
      	data: {
      		id : 'isDefault'
      	}
      }).done(function(data) {
      	if(data.isTypeDefault) {
					self.ErrorPopup.find('.generic-error-main').html(translator.get("requirement-version.link.type.error.message.typeIsDefault"));
          self.ErrorPopup.messageDialog('open');
      	} else {
      		var deletePopupMessage = $("#delete-link-type-warning");
      		$.ajax({
      			type: 'GET',
      			url: routing.buildURL('requirement.link.type', typeId),
      			data: {
      				id: 'isUsed'
      			}
      		}).done(function(data) {
						if(data.isLinkTypeUsed) {
								deletePopupMessage.text(translator.get("requirement-version.link.type.delete.warning.linkTypeIsUsed"));
						} else {
								deletePopupMessage.text(translator.get("requirement-version.link.type.delete.warning.linkTypeIsUnused"));
						}
						self.DeleteTypePopup.data('typeId', typeId);
						self.DeleteTypePopup.formDialog('open');
      		});
      	}
      });
		},

		deleteType : function() {
			var self = this;
			var typeId = self.DeleteTypePopup.data('typeId');

			$.ajax({
				url: routing.buildURL('requirement.link.type', typeId),
				method: 'DELETE'
			}).done(function() {
				self.DeleteTypePopup.formDialog('close');
				self.table.refresh();
			});
		},

		/* ====== Multiple Delete functions ====== */

		configureDeleteMultipleTypesPopup : function() {

			var self = this;

      var dialog = $("#multiple-delete-link-type-popup");
      this.DeleteMultipleTypesPopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.deleteMultipleTypes.call(self);
      });

      dialog.on('formdialogcancel', this.closePopup);
		},

		openDeleteMultipleTypesPopup : function() {
			var self = this;
			var ids = self.table.getSelectedIds();
			if(ids.length === 0) {
				self.ErrorPopup.find('.generic-error-main').html(translator.get("message.EmptyTableSelection"));
        self.ErrorPopup.messageDialog('open');
			} else {
				var builtUrl = routing.buildURL('requirementLinkType') + '/' + ids.join(',');
				$.ajax({
					url: builtUrl,
					method: 'GET',
					data: {
						id: 'doesContainDefault'
					}
				}).done(function(data) {
					var deleteMessage = $("#multiple-delete-link-type-warning");
					if(data.containsDefault) {
						self.ErrorPopup.find('.generic-error-main').html(translator.get("requirement-version.link.type.error.message.containsDefaultType"));
            self.ErrorPopup.messageDialog('open');
					} else {
						self.DeleteMultipleTypesPopup.data('typesIds', ids);
						if(ids.length === 1) {
							deleteMessage.text(translator.get("requirement-version.link.type.delete.warning.linkTypesAreUsed"));
						} else {
							deleteMessage.text(translator.get("requirement-version.link.type.delete.warning.linkTypesAreUnused"));
						}
            self.DeleteMultipleTypesPopup.formDialog('open');
					}
				});
			}
		},

		deleteMultipleTypes : function() {
			var self = this;
			var ids = self.DeleteMultipleTypesPopup.data('typesIds');
			var builtUrl = routing.buildURL('requirementLinkType') + '/' + ids.join(',');
			$.ajax({
      	url: builtUrl,
      	method: 'DELETE'
      }).done(function(data) {
				self.table.refresh();
				self.DeleteMultipleTypesPopup.formDialog('close');
      });
		}


	});

	return reqLinkTypeManagerView;

});
