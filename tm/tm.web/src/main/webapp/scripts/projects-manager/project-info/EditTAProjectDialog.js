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
define([ "jquery", "backbone", "app/ws/squashtm.notification", "app/lnf/Forms", "app/util/StringUtil",
		"jquery.squash.formdialog", "squashtest/jquery.squash.popuperror" ], function($, Backbone, WTF, Forms,
		StringUtil) {
	function isBlank(val) {
		return StringUtil.isBlank(val);
	}
	var TAProjectModel = Backbone.Model.extend({
		// url : need to be passed to constructor with test-automation-project id
		defaults : {
			label : "",
			jobName : "",
			slaves : ""
		},

		validateAll : function() {
			var attrs = this.attributes, errors = null;
			if (isBlank(attrs.label)) {
				errors = errors || {};
				errors.label = "message.notBlank";
			}
			return errors;
		}, 
		
		urlRoot  : squashtm.app.contextRoot + "/test-automation-projects/"

	});

	var EditTAProjectPopup = Backbone.View.extend({
		el : "#ta-project-edit-popup",

		initialize : function(conf) {
			
			this.isAdmin = conf.isAdmin;
			this.projecUrl = conf.tmProjectURL;
			this.$el.formDialog();
			this.error = this.$(".ta-projectsedit-error").popupError();
			this.showErrorMessage = $.proxy(this._showErrorMessage, this);
			this.manageFatalError = $.proxy(this._manageFatalError, this);
			this.updateComboDatasAndOpen = $.proxy(this._updateComboDatasAndOpen, this);
			
		},

		events : {
			// textboxes with class .strprop are bound to the
			// model prop which name matches the textbox name
			"blur input:text.strprop" : "changeStrProp",
			"change select" : "changeStrProp",
			"formdialogconfirm" : "confirm",
			"formdialogcancel" : "cancel",
			"formdialogclose" : "close"
		},

		changeStrProp : function(event) {
			var textbox = event.target;
			this.model.set(textbox.name, textbox.value);
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newtestautomationproject.cancel");
			this.$el.formDialog("close");
		},

		close : function() {
			this.cleanup();
		},
		confirm : function(event) {
			var self = this;
			var res = true, validationErrors = this.model.validateAll();

			Forms.form(this.$el).clearState();

			if (validationErrors !== null) {
				for ( var key in validationErrors) {
					Forms.input(this.$("input[name='" + key + "']")).setState("error", validationErrors[key]);
				}

				return false;
			}

			this.model.save(null, {
				async : false,
				error : function(model, response) {
					WTF.handleUnknownTypeError(response);
					self.trigger("edittestautomationproject.confirm.error");
				},
				success : function() {
					self.trigger("edittestautomationproject.confirm.success");					
					self.$el.formDialog("close");

				}
			});

		},
		show : function() {
			var self = this;
			var authDialog = $('#add-ta-projects-login-dialog');

			var login = authDialog.data('login');
			var password = authDialog.data('password');
			var taProjectId = self.$el.data('projectId');
			var taProject = self.$el.data('taProject');
			
			this.$el.formDialog("setState", "pleasewait");
			this.$el.formDialog("open");
			
			this.taProjectId = taProjectId;
			this.model = new TAProjectModel(taProject);
			
			// populate inputs
			this.$el.find("input[name=label]").val(taProject.label);
			this.$el.find("input[name=slaves]").val(taProject.slaves);
			
			/* If the user is Admin, we don't ask for credentials */
			if(this.isAdmin) {
				$.ajax({
					url : this.projecUrl + "/available-ta-projects",
					type : "GET",
				}).done(self.updateComboDatasAndOpen)
				  .fail(self.manageFatalError);
			/* Else, we use the credentials. */
			} else {
				$.ajax({
					url : this.projecUrl + "/available-ta-projects",
					type : "GET",
					data: {
						'login': login,
						'password': password
					}
				}).done(self.updateComboDatasAndOpen)
				  .fail(function(jsonFailResult) {
					  self.manageFatalError(jsonFailResult);
					  authDialog.data('login', "").data('password', "");
				  });
			}
			// Erase data
			self.$el.data('projectId', '');
			self.$el.data('taProject', '');
		},

		cleanup : function() {
			Forms.form(this.$el).clearState();
		},

		_updateComboDatasAndOpen : function(taProjects) {
			var $jobNameSelect = this.$el.find("select[name=jobName]");
			$jobNameSelect.empty(); // remove old options
			$.each(taProjects, function(index) {
				addOption(taProjects[index].jobName);
			});
			addOption(this.model.attributes.jobName);
			$jobNameSelect.val(this.model.attributes.jobName);

			function addOption(jobName) {
				var option = $("<option>" + jobName + "</option>");
				option.attr("value", jobName);
				$jobNameSelect.append(option);
			}
			this.$el.formDialog("setState", "main");

		},

		_manageFatalError : function(json) {
			var message = "";
			try {
				message = WTF.getErrorMessage(json);
			} catch (parseException) {
				message = json.responseText;
			}
			this.showErrorMessage(message);

		},

		_showErrorMessage : function(message) {
			this.error.find('span').text(message);
			/* Second error message ? */
			//this.error.popupError('show');
			this.$el.formDialog('close');
		},
		setParentPanel : function(parentPanel) {
			var self = this;
			this.parentPanel = parentPanel;
		}
	});

	return EditTAProjectPopup;

});
