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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil) {
	var UMod = squashtm.app.UMod;
	

	/*
	 * That code seems to be unused as for now. If you intend to reactivate it, consider use the 
	 * refactored code in user-account-password-popup 
	 */
	var UserChangePasswordPopup = Backbone.View.extend({
		el : "#password-change-popup",
		initialize : function() {
			var self = this;
			var params = {
				selector : "#password-change-popup",
				title : UMod.message.changePasswordPopupTitle,
				openedBy : "#reset-password-button",
				isContextual : true,
				closeOnSuccess : false,
				buttons : [ {
					'text' : UMod.message.confirmLabel,
					'click' : function() {
						self.submitPassword.call(self);
					}
				} ],
				width : 420
			};

			squashtm.popup.create(params);
			$("#password-change-popup").bind("dialogclose", self.cleanUp);

		},
		events : {},

		submitPassword : function() {
			self = this;
			if (!self.validatePassword.call(self)) {
				return;
			}

			var oldPassword = $("#oldPassword").val();
			var newPassword = $("#newPassword").val();

			$.ajax({
				url : UMod.user.url.admin,
				type : "POST",
				dataType : "json",
				data : {
					"oldPassword" : oldPassword,
					"newPassword" : newPassword
				},
				success : function() {
					self.userPasswordSuccess.call(self);
				}
			});

		},

		// <%-- we validate the passwords only. Note that
		// validation also occurs server side. --%>
		validatePassword : function() {
			var self = this;
			// first, clear error messages
			$("#user-account-password-panel span.error-message").html('');

			// has the user attempted to change his password ?

			var oldPassOkay = true;
			var newPassOkay = true;
			var confirmPassOkay = true;
			var samePassesOkay = true;

			if (!self.isFilled("#oldPassword")) {
				$("span.error-message.oldPassword-error").html(UMod.message.oldPassError);
				oldPassOkay = false;
			}

			if (!self.isFilled("#newPassword")) {
				$("span.error-message.newPassword-error").html(UMod.message.newPassError);
				newPassOkay = false;
			}

			if (!self.isFilled("#user-account-confirmpass")) {
				$("span.error-message.user-account-confirmpass-error").html(UMod.message.confirmPassError);
				confirmPassOkay = false;
			}

			if ((newPassOkay) && (confirmPassOkay)) {
				var pass = $("#newPassword").val();
				var confirm = $("#user-account-confirmpass").val();

				if (pass != confirm) {
					$("span.error-message.newPassword-error").html(UMod.message.samePassError);
					samePassesOkay = false;
				}
			}

			return ((oldPassOkay) && (newPassOkay) && (confirmPassOkay) && (samePassesOkay));

		},

		isFilled : function(selector) {
			var value = $(selector).val();
			if (!value.length) {
				return false;
			} else {
				return true;
			}

		},

		hasPasswdChanged : function() {
			return ((this.isFilled("#oldPassword")) || (this.isFilled("#newPassword")) || (this
					.isFilled("#user-account-confirmpass")));
		},

		userPasswordSuccess : function() {
			$(this.el).dialog('close');
			squashtm.notification.showInfo(UMod.message.passSuccess);
		},

		cleanUp : function() {
			$("#oldPassword").val('');
			$("#newPassword").val('');
			$("#user-account-confirmpass").val('');

		}
	});
	return UserChangePasswordPopup;
});