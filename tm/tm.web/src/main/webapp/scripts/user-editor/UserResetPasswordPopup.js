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
define([ "jquery", "backbone", "underscore", "handlebars", "app/util/StringUtil", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog", "jquery.squash.formdialog" ], function($, Backbone, _, Handlebars, StringUtil) {
	"use strict";

	var UMod = window.squashtm.app.UMod;
	var UserResetPasswordPopup = Backbone.View.extend({
		initialize : function(options) {
			this.options = options;
			_.bindAll(this, "validatePassword", "userPasswordSuccess", "submitPassword");

			// TODO I should not handle events from an outside dialog, I SHOULD BE the dialog
			this.render();
			var dialog = $("#"+ this.options.popupId);

			dialog.formDialog({width:420});

			dialog.on('formdialogconfirm', this.submitPassword);


			
			dialog.on('formdialogcancel', function(){

				dialog.formDialog('close');
			});
			dialog.on("formdialogclose", this.dialogCleanUp);

			// TODO I should not handle events from an outside button
			$("#" + this.options.openerId).on('click', function(){
				dialog.formDialog({width:420});
				dialog.formDialog('open');
			});

			this.$dialog = dialog;

		},

		render : function() {
			var source = $("#password-reset-popup-tpl").html();
			var template = Handlebars.compile(source);
			this.$el.html(template({
				popupId : this.options.popupId
			}));

			return this;
		},

		events : {},

		submitPassword : function() {
			if (!this.validatePassword()) {
				return;
			}

			var newPassword = this.$dialog.find(".password").val();

			var self = this;
			$.ajax({
				url : self.options.url,
				type : self.options.type,
				dataType : "json",
				data : {
					"password" : newPassword
				},
				success : function() {
					self.userPasswordSuccess();
				}
			});
		},

		// <%-- we validate the passwords only. Note that
		// validation also occurs server side. --%>
		validatePassword : function() {
			// first, clear error messages
			this.$dialog.find(".user-account-password-panel span.error-message").html('');

			// has the user attempted to change his password ?

			var newPassOkay = true;
			var confirmPassOkay = true;
			var samePassesOkay = true;

			if (!this.isFilled(".password")) {
				this.$dialog.find("span.error-message.password-error").html(UMod.message.newPassError);
				newPassOkay = false;
			}

			if (!this.isFilled(".user-account-confirmpass")) {
				this.$dialog.find("span.error-message.user-account-confirmpass-error").html(UMod.message.confirmPassError);
				confirmPassOkay = false;
			}

			if ((newPassOkay) && (confirmPassOkay)) {
				var pass = this.$dialog.find(".password").val();
				var confirm = this.$dialog.find(".user-account-confirmpass").val();

				if (pass != confirm) {
					this.$dialog.find("span.error-message.password-error").html(UMod.message.samePassError);
					samePassesOkay = false;
				}
			}

			return ((newPassOkay) && (confirmPassOkay) && (samePassesOkay));
		},

		isFilled : function(selector) {
			var value = this.$dialog.find(selector).val();
			if (!value.length) {
				return false;
			} else {
				return true;
			}

		},

		userPasswordSuccess : function() {
			window.squashtm.notification.showInfo(UMod.message.passSuccess);
			this.$dialog.formDialog("close");
			this.model.set("hasAuthentication", true);

		},

		/**
		 * context of this method should be the dialog
		 */
		dialogCleanUp : function() {
			var $this = $(this);
			$this.find(".password").val('');
			$this.find(".user-account-confirmpass").val('');
		}

	});
	return UserResetPasswordPopup;
});
