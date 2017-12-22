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
define([ "jquery", "underscore", "backbone", "handlebars", "app/lnf/Forms", "./NewTestAutomationServerModel", "squash.configmanager", "jquery.squash.formdialog", "jquery.ckeditor",
		"datepicker/jquery.squash.datepicker-locales" ], function($, _, Backbone, Handlebars, Forms, Model, confman) {
	"use strict";

	/*
	 * Defines the controller for the new test automation panel.
	 */
	var NewTestAutomationServerDialogView = Backbone.View.extend({
		el : "#new-test-automation-server-popup",
		initialize : function() {
			_.bindAll(this, "onConfirm", "onConfirmAndCarryOn", "onCancel");

			this.configureCKEs();

			// tried the more convenient `this.listenTo` but it generates errors in jq event processing
			
			// Issue 4954 bis : this hack will ensure the buttons still work despite the odd 
			// dialog lifecycle 
			this.$el.off("formdialogconfirm formdialogcancel formdialogclose formdialogconfirm-carry-on");
			
			this.$el.on("formdialogconfirm", this.onConfirm);
			this.$el.on("formdialogcancel", this.onCancel);
			this.$el.on("formdialogclose", this.onCancel);
			this.$el.on("formdialogconfirm-carry-on", this.onConfirmAndCarryOn);

			this.render();

			this.$el.formDialog({
				autoOpen : true
			});

		}, 

		render: function() {
			Forms.form(this.$el).clearState();
			var model = this.model;

			this.$("input:text.strprop").each(function() {
				this.value = model.get(this.name);
			});
			this.$("input:password.strprop").each(function() {
				this.value = model.get(this.name);
			});
			this.$("input:password.strprop").each(function() {
				this.value = model.get(this.name);
			});
			this.$("input:checkbox.boolprop").each(function() {
				$(this).prop("checked", model.get(this.name));
			});

			this.resetCKE();

			if(this.$el.data().formDialog !== undefined) {
				this.$el.formDialog("focusMainInput");
			}
			
			return this;
		},

		events : {
			// textboxes with class .strprop are bound to the
			// model prop which name matches the textbox name
			"blur input:text.strprop" : "changeStrProp",
			"blur input:password.strprop" : "changeStrProp",
			// "change textarea" : "updateCKEModelAttr",
			// did not work because of _CKE instances (cf method
			// configureCKEs to see how manual binding is done.
			"click input:checkbox" : "changeBoolProp",
			"confirmdialogcancel" : "cancel",
			"confirmdialogvalidate" : "validate",
			"confirmdialogconfirm" : "confirm",
		},

		changeStrProp : function(event) {
			var textbox = event.target;
			this.model.set(textbox.name, textbox.value);
		},

		changeBoolProp : function(event) {
			var cbox = event.target; 
			this.model.set(cbox.name, cbox.checked);
		},

		onCancel : function(event) {
			
			this.trigger("newtestautomationserver.cancel");
			this.cleanup();
		},

		onConfirm : function(event) {
		
			if (this.validate(event)) { 
				this.trigger("newtestautomationserver.confirm");
			}
			this.cleanup();
		},

		onConfirmAndCarryOn : function(event) {
			if (this.validate(event)) {
				this.model = new Model();
				this.render();
				this.trigger("newtestautomationserver.confirm-carry-on");
			}
		},

		validate : function(event) {
			var res = true;
			var validationErrors = this.model.validateAll();

			Forms.form(this.$el).clearState();

			if (validationErrors !== null) {
				for (var key in validationErrors) {
					Forms.input(this.$("input[name='" + key + "']")).setState("error", validationErrors[key]);
				}

				return false;
			}

			this.model.save(null, {
				async : false,
				error : function() {
					res = false;
					event.preventDefault();
				}
			});

			return res;
		},

		cleanup : function() {
			// issue 4954 : shouldn't clean anything after all
		},

		configureCKEs : function() {
			var self = this;
			var textareas = this.$el.find("textarea"),
				ckconf = confman.getStdCkeditor();
			
			textareas.each(function() {
				$(this).ckeditor(function() {
				}, ckconf);

				CKEDITOR.instances[$(this).attr("id")].on('change', function(e) {
					self.updateCKEModelAttr.call(self, e);
				});
			});
		},

		updateCKEModelAttr : function(event) {
			var attrInput = event.sender;
			var attrName = attrInput.element.$.getAttribute("name");
			var attrValue = attrInput.getData();
			this.model.set(attrName, attrValue);
		},
		resetCKE: function() {
			var self = this;
			var textareas = this.$el.find("textarea");
			textareas.each(function() {
				CKEDITOR.instances[this.id].setData(self.model.get(this.name));
			});
		}

	});

	return NewTestAutomationServerDialogView;
});