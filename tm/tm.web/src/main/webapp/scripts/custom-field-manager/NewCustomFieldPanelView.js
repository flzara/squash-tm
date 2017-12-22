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
define(
		[ "jquery", "backbone", "handlebars", "app/lnf/SquashDatatablesLnF", "app/lnf/Forms", "squash.configmanager",
		  "./NewCustomFieldModel", "jquery.squash.formdialog", "datepicker/jquery.squash.datepicker-locales", "jquery.squash.tagit" ],
	function($, Backbone, Handlebars, SD, Forms, confman, NewCustomFieldModel) {
		"use strict";
		/**
		 * Validates model and sets error messages accordingly
		 * /!\ Inputs are fetched BY NAME
		 * @param view the NewCustomFieldPanelView to validate
		 * @returns {Boolean} true if model validates
		 */
		function validateView(view) {
			var validationErrors = view.model.validateAll();

			Forms.form(view.$el).clearState();

			if (validationErrors !== null) {
				for (var key in validationErrors) {
					Forms.input(view.$("[name='" + key + "']")).setState("error",
							validationErrors[key]);
				}

				return false;
			}

			return true;
		}

		/**
		 * returns the function which should be used as a callback.
		 */
	function optionRow(self) {
		return function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
			var row = $(nRow),
				defaultCell = row.find(".is-default"),
				removeCell = row.find(".remove-row"),
				option = aData[0],
				checked = option === self.model.get("defaultValue"),
				tplData = {
					option : option,
					checked : checked
				};

			optionRow.removeTpl = optionRow.removeTpl || Handlebars.compile($("#remove-cell-tpl").html());
			removeCell.html(optionRow.removeTpl(tplData));

			optionRow.defaultTpl = optionRow.defaultTpl || Handlebars.compile($("#default-cell-tpl").html());
			defaultCell.html(optionRow.defaultTpl(tplData));
		};
	}


		/**
		 * Saves the model (it should be valid !)
		 * @param model
		 * @param event (optional) if invoked in repsonse to an event, please provide that event.
		 * @returns {Boolean} true if model was saved without errors
		 */
		function saveModel(model, event) {
			var ssok = true;

			var csok = model.save(null, {
				async : false,
				error : function() {
					ssok = false;
					if (!! event) {
						event.preventDefault();
					}
				}
			});

			return csok && ssok;
		}

		/*
		 * Defines the controller for the new custom field panel.
		 */
		var NewCustomFieldPanelView = Backbone.View.extend({
			el : "#new-cf-pane",
			defaultWidth : 600,
			richWidth : 1000,
			initialize : function() {
				var self = this;
				this.model = new NewCustomFieldModel();
				$.datepicker.setDefaults($.datepicker.regional[squashtm.app.locale]);
				this.defaultValueField = this.$("input:text[name='defaultValue']");

				this.initializeForm();
				this.render();

				this.$el.formDialog({
					autoOpen : true,
					close : function() {
						self.cancel.call(self);
					}
				});
				this._resize();
			},

			initializeForm: function() {
				var model = this.model;
				this.$("input:text.strprop").each(function() {
					this.value = model.get(this.name);
				});
				this.$("input:checkbox[name='optional']").get()[0].checked = model.get("optional");
				this.$("select[name='inputType']").val(model.get("inputType"));
				
				if(this.$el.data().formDialog !== undefined) {
					this.$el.formDialog("focusMainInput");
				}
			},

			render : function() {
				// in case the input type was previously rich text.
				// MUST be done before template is inserted, otherwise the text field wont be in the dom anymore
				confman.destroyCkeditor("#defaultValue");

				var inputType = this.model.get("inputType");
				var source = $("#" + inputType + "-default-tpl").html();
				var template = Handlebars.compile(source);
				this.$("#default-value-pane").html(template(this.model.toJSON()));

				switch (inputType) {
				case "DROPDOWN_LIST":
					this.renderNewOptionPane();
					this.renderOptionsTable();
					this.renderOptional(true);
					break;

				case "CHECKBOX":
					this.renderOptional(false);
					break;

				case "PLAIN_TEXT":
					this.renderOptional(true);
					break;

				case "DATE_PICKER":
					this.renderOptional(true);
					$("#defaultValue").datepicker({
						dateFormat : squashtm.app.localizedDateFormat
					});
					break;

				case "RICH_TEXT" :
					this.renderOptional(true);
					this.renderRichText();
					break;

				case "TAG" :
					this.renderOptional(true);
					this.renderTagList();
					break;
				case "NUMERIC":
					this.renderOptional(true);
					break;
				}
				this._resize();
				return this;
			},

			_resize : function(){
				if (this.$el.data().formDialog !== undefined){
					var type = this.model.get("inputType");
					var width = (type === "RICH_TEXT") ? this.richWidth : this.defaultWidth;
					this.$el.formDialog("option", "width", width);
				}
			},

			events : {
				// textboxes with class .strprop are bound to the
				// model prop which name matches the textbox name
				"blur input:text.strprop" : "changeStrProp",
				"change select.optprop" : "changeOptProp",
				"change input:text.dateprop" : "changeDateProp",
				"change textarea.richprop" : "changeRichProp",
				"change ul.tagprop" : "changeTagProp",
				"invalidtag ul.tagprop" : "invalidTag",
				"change select[name='inputType']" : "changeInputType",
				"click input:checkbox[name='optional']" : "changeOptional",
				"formdialogcancel" : "cancel",
				"formdialogvalidate" : "addAndClose",
				"formdialogaddanother" : "addAnother",
				"click .add-option" : "addOption",
				"click .remove-row>a" : "removeOption",
				"click .is-default>input:checkbox" : "changeDefaultOption"
			},

			changeStrProp : function(event) {
				var textbox = event.target;
				this.model.set(textbox.name, textbox.value);
			},

			changeDateProp : function(event) {
				var textbox = event.target;
				var date = $(textbox).datepicker("getDate");
				var dateToString = $.datepicker.formatDate($.datepicker.ATOM, date);
				this.model.set(textbox.name, dateToString);
			},

			changeOptProp : function(event) {
				var option = event.target;
				this.model.set(option.name, option.value);
			},

			changeRichProp : function(event){
				var area = $("#defaultValue");
				this.model.set(area.attr('id'), area.val());
			},

			changeTagProp : function(event){
				var tags = $("#defaultValue").squashTagit("assignedTags").join("|");
				this.model.set("defaultValue", tags);
			},

			changeInputType : function(event) {
				var model = this.model;

				model.set("inputType", event.target.value);
				model.resetDefaultValue();

				this.render();
			},

			changeOptional : function(event) {
				this.model.set("optional", event.target.checked);
			},

			cancel : function(event) {
				this.cleanup();
				this.trigger("newcustomfield.cancel");
			},

			invalidTag : function(event) {
				var res = true, invalidTag = this.model.invalidTag();
				Forms.form(this.$el).clearState();
				Forms.input(this.$("ul[name='" + "defaultValue" + "']")).setState("error", invalidTag["tagCode"]);
				return false;
			},

			addAnother : function(event) {
				if (validateView(this) && saveModel(this.model, event)) {
					this.trigger("newcustomfield.added", { source: event, view: this, model: this.model });
					this._resetForm();
				}
			},

			addAndClose: function(event) {
				if (validateView(this) && saveModel(this.model, event)) {
					this.trigger("newcustomfield.added", { source: event, view: this, model: this.model });
					this.cleanup();
					this.trigger("newcustomfield.cancel", { source: event, view: this });
				}
			},

			cleanup : function() {
				this.$el.addClass("not-displayed");
				Forms.form(this.$el).clearState();
				this.$el.formDialog("destroy");
			},

			renderOptional : function(show) {
				var renderPane = this.$("#optional-pane");
				if (show) {
					renderPane.show();
				} else {
					renderPane.hide();
				}
			},

			renderOptionsTable : function() {
				this.optionsTable = this.$("#options-table");
				this.optionsTable.dataTable({
					"oLanguage" : {
						"sUrl" : squashtm.app.cfTable.languageUrl
					},
					"bAutoWidth" : false,
					"bJQueryUI" : true,
					"bFilter" : false,
					"bPaginate" : false,
					"bServerSide" : false,
					"bDeferRender" : true,
					"bRetrieve" : false,
					"bSort" : false,
					"aaSorting" : [],
					"fnRowCallback" : optionRow(this),
					"fnDrawCallback" : this.decorateOptionsTable,
					"aoColumnDefs" : [ {
						"aTargets" : [ 0 ],
						"sClass" : "option"
					}, {
						"aTargets" : [ 1 ],
						"sClass" : "code"
					}, {
						"aTargets" : [ 2 ],
						"sClass" : "is-default"
					}, {
						"aTargets" : [ 3 ],
						"sClass" : "remove-row"
					} ]
				});
			},

			renderNewOptionPane: function() {
				var src = $("#new-option-pane-tpl").html();
				var tpl =  Handlebars.compile(src);
				this.$("#new-option-pane").html(tpl({}));
			},

			renderRichText: function() {
				var conf = confman.getStdCkeditor();
				$("#defaultValue").ckeditor(function(){}, conf);
				// the following reroute the blur event from the ckeditor and relocate it as thrown by the textarea
				CKEDITOR.instances["defaultValue"].on('change', function(){
					$("#defaultValue").trigger('change');
				});
			},

			renderTagList: function() {
				var tagconf = confman.getStdTagit();
				var triggerChange = function(event, ui){
					$("#defaultValue").trigger('change');
				};

				$.extend(true, tagconf, {
					validate :  function(label){
						if (label.indexOf("|") !== -1){
							$("#defaultValue").trigger('invalidtag');
							return false;
						} else{
							return true;
						}
					},
					afterTagAdded: triggerChange,
					afterTagRemoved: triggerChange
				});
				$("#defaultValue").squashTagit(tagconf);
			},

			_resetForm : function() {
				this.model = new NewCustomFieldModel();
				confman.destroyCkeditor("#defaultValue");
				Forms.form(this.$el).clearState();
				this.initializeForm();
				this.render();
			},

			addOption : function() {
				Forms.form(this.$("#new-option-pane")).clearState();
				var $label = this.$("input[name='new-option-label']");
				var $code = this.$("input[name='new-option-code']");

				try {
					this.model.addOption([ $label.val(), $code.val() ]);
					this.optionsTable.dataTable().fnAddData([ $label.val(), $code.val(), false, "" ]);
					this.renderNewOptionPane();
				} catch (ex) {
					if (ex.name === "ValidationException") {
						var errs = ex.validationErrors;
						if (errs.optionLabel) {
							Forms.input($label).setState("error", errs.optionLabel);
						}
						if (errs.optionCode) {
							Forms.input($code).setState("error", errs.optionCode);
						}
					}
				}
			},

			removeOption : function(event) {
				// target of click event is a <span> inside of <button>, so we use currentTarget
				var button = event.currentTarget, $button = $(button), option = $button.data("value"), row = $button
						.parents("tr")[0];

				this.model.removeOption(option);
				this.optionsTable.dataTable().fnDeleteRow(row);
			},

			changeDefaultOption : function(event) {
				var checkbox = event.currentTarget, option = checkbox.value, defaultValue = checkbox.checked ? option
						: "", uncheckSelector = ".is-default>input:checkbox" +
						(checkbox.checked ? "[value!='" + option + "']" : ""), optionsInput = Forms
						.input(this.$("input[name='options']"));

				optionsInput.clearState();

				if (this.model.get("optional") === false && checkbox.checked === false) {
					event.preventDefault();
					optionsInput.setState("warning", "message.defaultOptionMandatory");
					return;
				}

				this.model.set("defaultValue", defaultValue);
				this.optionsTable.find(uncheckSelector).attr("checked", false);

			},

			decorateOptionsTable : function() {
				SD.deleteButton($(this).find(".remove-row>a"));
			}
		});

		return NewCustomFieldPanelView;
		});