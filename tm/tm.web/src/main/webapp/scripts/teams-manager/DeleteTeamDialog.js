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
define([ "jquery", "backbone", "handlebars", "app/lnf/Forms",
		"jquery.squash.formdialog" ], function($, Backbone, Handlebars,  Forms) {
	var View = Backbone.View.extend({
		el : "#remove-team-dialog",

		initialize : function() {
			this.$textAreas = this.$el.find("textarea");
			this.$textFields = this.$el.find("input:text");
			this.$errorMessages = this.$el.find("span.error-message");

			this._resetForm();
		},

		events : {
			"formdialogcancel" : "cancel",
			"formdialogvalidate" : "validate",
			"formdialogconfirm" : "confirm"
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("deleteteam.cancel");
		},

		confirm : function(event) {
			var res = true, self = this;

			this._populateModel();
			Forms.form(this.$el).clearState();
			var table = $("#teams-table").squashTable();
			var ids = table.getSelectedIds().join(',');
			var urlDelete = squashtm.app.contextRoot + "/administration/teams/" + ids ;

			$.ajax({
				type : 'delete',
				url : urlDelete,
				dataType : 'json',
				// note : we cannot use promise api with async param. see
				// http://bugs.jquery.com/ticket/11013#comment:40
				async : false,
				data : self.model,
				error : function(jqXHR, textStatus, errorThrown) {
					res = false;
					event.preventDefault();
				}
			});
			$('#teams-table').squashTable().refresh();
			this.trigger("deleteteam.confirm");
			this.$el.formDialog("close");
			return res;


		},

		validate : function(event) {
			var res = true, self = this;
			this._populateModel();
			Forms.form(this.$el).clearState();

			$.ajax({
				type : 'post',
				url : squashtm.app.contextRoot + "/administration/teams/{entity-id}",
				dataType : 'json',
				// note : we cannot use promise api with async param. see
				// http://bugs.jquery.com/ticket/11013#comment:40
				async : false,
				data : self.model,
				error : function(jqXHR, textStatus, errorThrown) {
					res = false;
					event.preventDefault();
				}
			});

			return res;
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			this._resetForm();
			this.$el.formDialog("close");
		},

		/**
		 * resets the content of the dialog.
		 */
		_resetForm : function() {
			this.$textFields.val("");
			this.$textAreas.val("");
			this.$errorMessages.text("");
			Forms.form(this.$el).clearState();
		},

		show : function() {
			if (!this.dialogInitialized) {
				this._initializeDialog();
			}

			this.$el.formDialog("open");
		},

		_initializeDialog : function() {
			function decorateArea() {
				$(this).ckeditor(function() {
				}, {
					customConfig : squashtm.app.contextRoot + "/styles/ckeditor/ckeditor-config.js",
					language : squashtm.app.ckeditorLanguage
				});
			}

			this.$textAreas.each(decorateArea);
			this.$el.formDialog();

			this.dialogInitialized = true;
		},

		_populateModel : function() {
			var model = this.model, $el = this.$el;

			model.name = $el.find("#add-team-name").val();
			model.description = $el.find("#add-team-description").val();
		}
	});

	return View;
});