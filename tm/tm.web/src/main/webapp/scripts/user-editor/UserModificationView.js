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
define([ "jquery", "backbone", "underscore", "jeditable.simpleJEditable", "app/util/StringUtil",
		"./UserResetPasswordPopup", "./UserPermissionsPanel", "./UserTeamsPanel", "squash.attributeparser", "app/lnf/Forms", "app/ws/squashtm.notification", "squash.translator", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog", "jquery.squash.jeditable", "jquery.switchButton" ], function($, Backbone, _,
		SimpleJEditable, StringUtil, UserResetPasswordPopup, UserPermissionsPanel, UserTeamsPanel, attrparser, Forms, notification, translator) {
	var UMod = squashtm.app.UMod;
	var UserModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {

			this.configureEditables();
			this.configureDeletionDialog();
			this.configureActivation();
			
			this.model = new Backbone.Model({
				hasAuthentication : UMod.user.hasAuthentication
			});
			

			if (this.model.get("hasAuthentication")) {
				this.resetPasswordPopup = this.createResetPasswordPopup();

			} else {
				this.createAuthPopup = new UserResetPasswordPopup({
					el : "#auth-pop-pane",
					popupId : "create-auth-popup",
					openerId : "create-auth-button",
					url: UMod.user.url.admin + "authentication", 
					type: "put",
					model : this.model
				});

			}

			new UserPermissionsPanel();
			new UserTeamsPanel();
			this.configureButtons();

			this.listenTo(this.model, "change:hasAuthentication", this.onChangeHasAuthentication);
		},

		events : {
			"click #delete-user-button" : "confirmUserDeletion",
			"change #toggle-activation-checkbox" : "toggleUserActivation",
			"change #user-group" : "changeUserGroup"
		},

		confirmUserDeletion : function(event) {
			this.confirmDeletionDialog.confirmDialog("open");
		},
		
		changeUserGroup : function(event) {
			var url = UMod.user.url.changeGroup;
			$.ajax({
				type : 'POST',
				url : url,
				data : "groupId=" + $(event.target).val(),
				dataType : 'json'
			});
		},
		
		deleteUser : function(event) {
			var self = this;
			$.ajax({
				type : 'delete',
				url : UMod.user.url.admin,
				data : {},
				dataType : 'json'

			}).done(function() {
				self.trigger("user.delete");
			});

		},

		configureButtons : function() {
			$.squash.decorateButtons();
		},

		

		configureEditables : function() {
			this.makeSimpleJEditable("user-login"); 
			this.makeSimpleJEditable("user-first-name");
			this.makeSimpleJEditable("user-last-name");
			this.makeSimpleJEditable("user-email");
		},

		configureDeletionDialog : function() {
			this.confirmDeletionDialog = $("#delete-warning-pane").confirmDialog();
			this.confirmDeletionDialog.on("confirmdialogconfirm", $.proxy(this.deleteUser, this));
		},
		
		configureActivation : function(){

			var activCbx = $("#toggle-activation-checkbox"),
				activConf = attrparser.parse(activCbx.data('def'));
			
			activCbx.switchButton(activConf);
			
			//a bit of css tweak now
			activCbx.siblings('.switch-button-background').css({position : 'relative', top : '5px'});
		},
		
		toggleUserActivation : function(){
			var shouldActivate = $("#toggle-activation-checkbox").prop('checked');
			if (shouldActivate){
				this.activateUser();
			}
			else{
				this.deactivateUser();
			}
				
		},		
		
		activateUser : function(){
			$.post(UMod.user.url.admin + '/activate')
			.done(function(){
				$("#user-name-deactivated-hint").addClass('not-displayed');
			});
		},
		
		deactivateUser : function(){
			$.post(UMod.user.url.admin + '/deactivate')
			.done(function(){
				$("#user-name-deactivated-hint").removeClass('not-displayed');
			});			
		},
		
		makeSimpleJEditable : function(inputId) {
			var self = this;

			var onerror = function(settings, original, xhr) {
				xhr.errorIsHandled = true;
				var errormsg = notification.getErrorMessage(xhr);
				Forms.input(self.$("#" + inputId)).setState("error", errormsg);					
				return ($.editable.types[settings.type].reset || $.editable.types.defaults.reset).apply(this, arguments);
			};

			new SimpleJEditable({
				targetUrl : UMod.user.url.admin,  
				componentId : inputId,
				jeditableSettings : {
					onerror: onerror,
					onsubmit: function() { Forms.input(self.$("#" + inputId)).clearState(); }
				}
			});
		},

		createResetPasswordPopup: function() {
			return new UserResetPasswordPopup({
				el: "#pass-pop-pane",
				popupId: "password-reset-popup",
				openerId: "reset-password-button",
				url: UMod.user.url.admin, 
				type: "post",
				model: this.model
			});
		},

		onChangeHasAuthentication : function() {
			if (this.model.get("hasAuthentication")) {
				this.resetPasswordPopup = this.resetPasswordPopup || this.createResetPasswordPopup();
				
				this.$("#reset-password-button").removeClass("not-displayed");
				this.$("#create-auth-button").addClass("not-displayed");

				if (this.createAuthPopup) {
					this.createAuthPopup.remove();
				}

			} // the other way is not possible
		}
	});
	return UserModificationView;
});