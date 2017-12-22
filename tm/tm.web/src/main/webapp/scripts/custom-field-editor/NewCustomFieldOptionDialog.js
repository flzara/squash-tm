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
define([ "jquery", "backbone", "handlebars", "app/lnf/Forms", "app/ws/squashtm.notification",
		"jquery.squash.formdialog" ], function($, Backbone, Handlebars,
		Forms, notification) {
	var View = Backbone.View.extend({
		el : "#add-cuf-option-popup",

		initialize : function() {

			this.$el.find("input:text").val("");
			this._resetForm();
			this.$el.formDialog({
				autoOpen : true
			});
		},

		events : {
			"formdialogcancel" : "cancel",
			"formdialogvalidate" : "validate",
			"formdialogaddanother" : "addanother",
			"formdialogconfirm" : "validate"
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newOption.cancel");
		},

		confirm : function(event) {
			this.cleanup();
			this.trigger("newOption.confirm");
		},

		addanother : function(event) {
			var res = true;
			this.populateModel();
			var self = this;
			Forms.form(this.$el).clearState();

			$.ajax({
				url : squashtm.app.cfMod.optionsTable.newOptionUrl,
				type : 'POST',
				data : self.model,
				// note : we cannot use promise api with async param. see
				// http://bugs.jquery.com/ticket/11013#comment:40
				async : false,
				dataType : 'json'
			}).fail(function(jqXHR, textStatus, errorThrown) {
				res = false;
				event.preventDefault();
				var errormsg = notification.getErrorMessage(jqXHR);
				Forms.input(self.$("#" + inputId)).setState("error", errormsg);		
			});
			$('#options-table').squashTable().refresh();
			this._resetForm();
			return res;
		},
		
		validate : function(event) {
			var res = true;
			this.populateModel();
			var self = this;
			Forms.form(this.$el).clearState();

			$.ajax({
				url : squashtm.app.cfMod.optionsTable.newOptionUrl,
				type : 'POST',
				data : self.model,
				// note : we cannot use promise api with async param. see
				// http://bugs.jquery.com/ticket/11013#comment:40
				async : false,
				dataType : 'json'
			}).fail(function(jqXHR, textStatus, errorThrown) {
				res = false;
				event.preventDefault();
				var errormsg = notification.getErrorMessage(jqXHR);
				Forms.input(self.$("#" + inputId)).setState("error", errormsg);		
			});
			$('#options-table').squashTable().refresh(); 
			this.$el.formDialog("close");
			return res;
		},

		_resetForm : function() {
			this.$textFields = this.$el.find("input:text");
			this.$textFields.val("");
		//	this.$errorMessages.text("");
			Forms.form(this.$el).clearState();
			if(this.$el.data().formDialog !== undefined) {
				this.$el.formDialog("focusMainInput");
			}
		},
		 
		cleanup : function() {
			this.$el.addClass("not-displayed");
			Forms.form(this.$el).clearState();
			this.$el.formDialog("destroy");
		},

		populateModel : function() {
			var $el = this.$el;
			this.model.label = $el.find("#new-cuf-option-label").val();
			this.model.code = $el.find("#new-cuf-option-code").val();
		}

	});

	return View;
});