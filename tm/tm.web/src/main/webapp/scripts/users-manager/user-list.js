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
/**
 * settings : { 
 * 	data : { tableData : the json model of the data displayed by the
 * datatable },
 *  canManageLocalPassword : boolean, says whether the local password are manageable (ie 
 *  is the internal authentication provider enabled) 
 * 
 * 	},
 *
 * urls : { rootContext : the base url of the application backUrl : the url
 * where to go where to go when clicking the #back button baseUrl : the base url
 * of all regaring user adminitration (listing, adding, removing etc) },
 * 
 * language : { missingNewPassword : error message when the new password input
 * wasn't typed in missingConfirmPassword : same, for the confirmation input
 * differentConfirmation : error message when the new password and confirmation
 * button ok : label for ok cancel : label
 * for cancel } }
 */

define([ "jquery", "squash.translator",
         "jquery.squash.fragmenttabs", "squashtable",
         "jquery.squash.confirmdialog", "jquery.squash.formdialog" ],
		function($, translator, Frag) {
	"use strict";

	// ---------------- add user dialog ----------------------------


	// note : I don't trust hasOwnProperty due to its cross-browser issues.
	// We'll
	// do it low tech once again.
	function isFilled(selector) {
		var value = $(selector).val();
		if (!value.length) {
			return false;
		} else {
			return true;
		}

	}

	function buildPasswordValidation(settings) {
		
		// if local password are not manageable, return a truthy function
		if (! settings.data.canManageLocalPassword){
			return function(){return true;};
		}
		
		var language = settings.language;
		return function() {
			var lang = language;

			// first, clear error messages
			$("#add-user-table span.error-message").html('');

			var newPassOkay = true;
			var confirmPassOkay = true;
			var samePassesOkay = true;

			if (!isFilled("#add-user-password")) {
				$("span.error-message.password-error").html(
						lang.missingNewPassword);
				newPassOkay = false;
			}

			if (!isFilled("#new-user-confirmpass")) {
				$("span.error-message.confirmpass-error").html(
						lang.missingConfirmPassword);
				confirmPassOkay = false;
			}

			if ((newPassOkay) && (confirmPassOkay)) {
				var pass = $("#add-user-password").val();
				var confirm = $("#new-user-confirmpass").val();

				if (pass != confirm) {
					$("span.error-message.password-error").html(
							lang.differentConfirmation);
					samePassesOkay = false;
				}
			}

			return ((newPassOkay) && (confirmPassOkay) && (samePassesOkay));
		};
	}

	function readForm(settings) {
		var form = {
			login : $("#add-user-login").val(),
			firstName : $("#add-user-firstName").val(),
			lastName : $("#add-user-lastName").val(),
			email : $("#add-user-email").val(),
			groupId : $("#add-user-group").val()
		};

		if (settings.data.canManageLocalPassword){
			form.password = $("#add-user-password").val();
		}
		else{
			form.noPassword="";
		}

		return form;
	}


	function openAdd() {
		$("#add-user-dialog").formDialog('open');
	}

	function closeAdd() {
		$("#add-user-dialog").formDialog('close');
	}

	function resetAdd() {
		$("#add-user-group").val($("#add-user-group option:last").val());
	}

	function onUsersChanged() {
		$('#users-list-table').squashTable().refresh();
	}
	// ------------- dialog init -----------------

	function initDialog(settings) {
		// new user popup
		var passValidation = buildPasswordValidation(settings);

		var adduserDialog = $("#add-user-dialog");
		adduserDialog.formDialog({width : 600});

		function saveUser(postStep) {
			return function () {
				if (!passValidation()){
					return;
				}
				var url = settings.urls.baseUrl + "/new";
				$.ajax({
					url : url,
					type : 'POST',
					dataType : 'json',
					data : readForm(settings)
				}).success(function(){
					onUsersChanged();
					postStep();
				});
			};
		}

		adduserDialog.on('formdialogconfirm', saveUser(closeAdd));
		adduserDialog.on('formdialogcancel', closeAdd);
		adduserDialog.on('formdialogopen', resetAdd);
		adduserDialog.on('formdialogaddanother', saveUser(function() { openAdd(); resetAdd(); }));

		// License information popup
		var userLicenseInformation = settings.userLicenseInformation;
		if(userLicenseInformation != null && userLicenseInformation.length !== 0){
			var userLicenseInformationArray = userLicenseInformation.split("-");
			var activeUsersCount = userLicenseInformationArray[0];
			var maxUsersAllowed = userLicenseInformationArray[1];
			var allowCreateUsers = JSON.parse(userLicenseInformationArray[2]);

			var licenseInformationDialog = $("#license-information-dialog");
			var message;
			if(!allowCreateUsers){
				licenseInformationDialog.formDialog().on('formdialogclose', function () {
					licenseInformationDialog.formDialog('close');
				});
				licenseInformationDialog.formDialog().on('formdialogcancel', function () {
					licenseInformationDialog.formDialog('close');
				});
				message = translator.get("information.userExcess.warning2", maxUsersAllowed, activeUsersCount);
				licenseInformationDialog.find("#information-message").html(message);
			} else {
				var informationBlock = $("#information-block");
				informationBlock.css("visibility", "visible");
				informationBlock.find("span").html(translator.get("information.userExcess.warning1", maxUsersAllowed, activeUsersCount));
			}
		}

		$("#add-user-button").on('click', function(){
			if(userLicenseInformation != null && userLicenseInformation.length !== 0){
				var userLicenseInformationArray = userLicenseInformation.split("-");
				var allowCreateUsers = JSON.parse(userLicenseInformationArray[2]);
				if(allowCreateUsers) {
					openAdd();
				} else {
					var licenseInformationDialog = $("#license-information-dialog");
					licenseInformationDialog.formDialog('open');
				}
			} else {
				openAdd();
			}
		});

		// confirm deletion
		$("#delete-user-dialog").confirmDialog().on('confirmdialogconfirm', function(){
			var $this = $(this),
			table = $("#users-list-table").squashTable();

			var userId = $this.data('entity-id'),
				userIds = (!! userId) ? [ userId ] : table.getSelectedIds();

			$this.data('entity-id');	//reset
			$.ajax({
				url : usersUrl(userIds),
				type : 'delete'
			}).done(onUsersChanged);
		});

	}


	// ---------------------- button ----------------------

	function initButtons(settings) {
		function displayNothingSelected(){
			var warn = translator.get({
				errorTitle : 'popup.title.Info',
				errorMessage : 'message.EmptyTableSelection'
			});
			$.squash.openMessage(warn.errorTitle, warn.errorMessage);
		}


		$("#deactivate-user-button").on('click', function(){
			var table =  $("#users-list-table").squashTable();
			var ids = table.getSelectedIds();
			if (ids.length>0){
				table.deactivateUsers(ids);
			}
			else{
				displayNothingSelected();
			}
		});

		$("#activate-user-button").on('click', function(){
			var table =  $("#users-list-table").squashTable();
			var ids = table.getSelectedIds();
			if (ids.length>0){
				table.activateUsers(ids);
			}
			else{
				displayNothingSelected();
			}
		});

		$("#delete-user-button").on('click', function(){
			var ids = $("#users-list-table").squashTable().getSelectedIds();
			if (ids.length>0){
				var popup = $("#delete-user-dialog");
				popup.data('entity-id',null);
				popup.confirmDialog('open');
			}
			else{
				displayNothingSelected();
			}
		});
	}

	// ----------- table ---------------------

	function drawCallback(){
		/*jshint validthis: true */
		var table = this;

		// activation button
		table.find('tbody .user-active-cell').each(function(){
			var value = table.fnGetData(this);
			var $cell = $(this);
			var btnclass = (value) ? 'sq-icon user-active-btn icon-user-activated' : 'sq-icon user-active-btn icon-user-deactivated';
			$cell.empty().append('<a href="#" class="'+btnclass+'"/>');

		});

	}

	function usersUrl(ids) {
		return window.squashtm.app.contextRoot + "administration/users/" + ids.join(',');
	}

	function initTable(settings) {

		var datatableSettings = {
			"aaData" : settings.data.tableData,
			"fnDrawCallback" : drawCallback
		};

		var squashSettings = {

				deleteButtons : {
					delegate : "#delete-user-dialog",
					tooltip : translator.get('label.Remove')
				},

			functions : {


				activateUsers : function(ids){
					var table = this;
					// TODO url should be /active, message should contain value = true
					$.post(usersUrl(ids) + '/activate').done(function(){
						table._changeActivation(ids, true, "sq-icon user-active-btn icon-user-activated");
					});
				},

				deactivateUsers : function(ids){
					var table = this;
					// TODO url should be /active, message should contain value = false
					$.post(usersUrl(ids) + '/deactivate').done(function(){
						table._changeActivation(ids, false, "sq-icon user-active-btn icon-user-deactivated");
					});
				},

				_changeActivation : function(ids, value, cssclass){
					var _table = this;
					var rows = _table.getRowsByIds(ids);
					rows.each(function(){
						var data = _table.fnGetData(this);
						data['user-active'] = value;
					});
					rows.find('a.user-active-btn').attr('class', '').addClass(cssclass);
				}
			}
		};

		var table = $("#users-list-table").squashTable(datatableSettings, squashSettings);

		// various hooks

		table.on('click', 'a.user-active-btn', function(evt){

			var tr = this.parentNode.parentNode,
				data = table.fnGetData(tr),
				id = data['user-id'],
				active = data['user-active'];

			if (active){
				table.deactivateUsers([id]);
			}else{
				table.activateUsers([id]);
			}


		});

	}

	function initTabs(){
		Frag.init();
	}


	function init(settings) {
		initTabs();
		initButtons(settings);
		initTable(settings);
		initDialog(settings);
	}

	return {
		initUserListPage : init
	};

});
