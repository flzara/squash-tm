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
define([ "jquery", "backbone", "underscore", "app/lnf/Forms", "app/util/StringUtil", "jquery.squash.formdialog", "squashtable"],
		function($,	Backbone, _, Forms, StringUtil) {

	var NewDatasetDialog = Backbone.View.extend({

		el : "#add-dataset-dialog",
		paramInputIdPrefix : "add-dataset-paramValue",
		paramRowClass : "parameterRow",
		inputClass : "paramValue",

		initialize : function(options) {
			this.settings = options.settings;
			this.$textFields = this.$el.find("input:text");
			this._initializeDialog();
			this._initializeTable();
		},

		_initializeDialog : function(){
			this.$el.formDialog({
				autoOpen : true,
				width : 600
			});

		},

		_initializeTable : function(){

			var table = $("#add-dataset-dialog-table");

			var tableSettings = {

				bPaginate : false,

				fnRowCallback : function(nRow){
					$(nRow).find(".add-parameter-input").append('<input type="text" maxlength="255" size="50"/>');
				}
			};

			var squashSettings = {};

			table.squashTable(tableSettings, squashSettings);
			table.fnDraw();
		},

		events : {


			"formdialogcancel" : "cancel",
			"formdialogvalidate" : "validate",
			"formdialogconfirm" : "confirm",
			"formdialogaddanother" : "addanother"
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newDataset.cancel");
		},

		confirm : function(event) {
			var res = this.validate();			
			if (res){
				this.trigger("newDataset.confirm");
				this.cleanup();
			}
		},

		addanother : function(event) {	
			if (this.validate()) {
				this.cleanup();
				$('#datasets-table').squashTable().refresh();
			}
		},


		validate : function(event) {
			var self = this;
			var res = true, validationErrors = this.validateAll();
			Forms.form(this.$el).clearState();

			if (validationErrors !== null) {
				for ( var key in validationErrors) {
					Forms.input(this.$("input[name='add-dataset-" + key + "']")).setState("error", validationErrors[key]);
				}
				res = false;
				return res;
			}

			var table = $("#add-dataset-dialog-table").squashTable();

			var parameters = [];

			table.find('tbody tr').each(function(){

				var $row = $(this);
				var paramValue = $row.find(".add-parameter-input input").val();
				var data = table.fnGetData(this);

				// 'null' might happen if there is no parameters in the table (but still one row saying that
				// the table is empty)
				if (data !== null){
					var paramId = data['entity-id'];
					var paramData = [paramId, paramValue];
					parameters.push(paramData);
				}
			});

			var params = {name:$("#add-dataset-name").val(), paramValues:parameters};

			$.ajax({
				url : this.settings.basic.testCaseDatasetsUrl + '/new',
				type : 'POST',
				contentType : "application/json",
				async : false,
				data : JSON.stringify(params),
				dataType : 'json',
				success : function(){	
					res = true;
				},
				error : function(jqXHR, textStatus, errorThrown){
					res = false;
				}
			});
			return res;
		},

		validateAll : function() {
			var name = $("#add-dataset-name").val(), errors = null;
			if (StringUtil.isBlank(name)) {
				errors = errors || {};
				errors.name = "message.notBlank";
			}
			return errors;
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			this._resetForm();
			
			if(this.$el.data().formDialog !== undefined) {
				this.$el.formDialog("focusMainInput");
			}
		},

		_resetForm : function() {
			// ? parameters text fields disappear from $textFields
			this.$textFields = this.$el.find("input:text");
			this.$textFields.val("");
			Forms.form(this.$el).clearState();
		},

		destroy : function(){
			this.$el.formDialog('destroy');
		}

	});

	return NewDatasetDialog;
});