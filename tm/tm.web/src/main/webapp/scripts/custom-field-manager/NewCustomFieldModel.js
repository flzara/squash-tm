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
		[ "jquery", "backbone", "app/util/StringUtil" ],
		function($, Backbone, StringUtil) {
			"use strict";

			var defaultValueByInputType = {
				PLAIN_TEXT : "",
				CHECKBOX : "false",
				DROPDOWN_LIST : "",
				DATE_PICKER : "",
				RICH_TEXT : "",
				TAG : ""
			};

			function isBlank(val) {
				return StringUtil.isBlank(val);
			}

			/*
			 * Defines the model for a new Custom Field
			 */
			var NewCustomFieldModel = Backbone.Model
					.extend({
						url : squashtm.app.contextRoot + "/custom-fields/new",
						defaults : {
							name : "",
							label : "",
							code : "",
							inputType : "PLAIN_TEXT",
							optional : true,
							defaultValue : "",
							options : [][2]
						},

						resetDefaultValue : function() {
							this.set("defaultValue",
									defaultValueByInputType[this
											.get("inputType")]);
							this.set("options", []);
						},

						/**
						 * Validates an option and then adds it.
						 *
						 * @throws an
						 *             exception when option does not validate.
						 *             exception.name === "ValidationException"
						 */
						addOption : function(option) {
							var options = this.attributes.options, errors = this
									.validateOption(option);
							if (errors) {
								throw {
									name : "ValidationException",
									validationErrors : errors
								};
							}
							options.push(option);
						},

						/**
						 * Validates a tag and then adds it.
						 *
						 * @throws an
						 *             exception when option does not validate.
						 *             exception.name === "ValidationException"
						 */
						addTag : function(option) {
							var options = this.attributes.options, errors = this
									.validateTag(option);
							if (errors) {
								throw {
									name : "ValidationException",
									validationErrors : errors
								};
							}
							options.push(option);
						},
						/**
						 * usage : var mapper = function(item) { return item[<what
						 * we want>]; }; var isDefined =
						 * this.optionPropertyAlreadyDefined(mapper) if
						 * (isDefined("candidate prop")) { ... }
						 *
						 * @param mapper
						 *            function which should receive an item from
						 *            the options array and return the property
						 *            of this item we need to check
						 * @return a function which checks if a given property
						 *         is already defined
						 */
						optionPropertyAlreadyDefined : function(propertyMapper) {
							var options = this.attributes.options;

							return function(propertyValue) {
								return $.inArray(propertyValue, $.map(options,
										propertyMapper)) >= 0;
							};
						},

						optionAlreadyDefined : function(optionLabel) {
							return (this.optionPropertyAlreadyDefined(function(
									item) {
								return item[0];
							}))(optionLabel);
						},

						optionCodeAlreadyDefined : function(optionCode) {
							return (this.optionPropertyAlreadyDefined(function(
									item) {
								return item[1];
							}))(optionCode);
						},

						optionCodePatternValid : function(optionCode) {
							// first condition specific for IE8 that does not
							// understand "^" and "$"
							// as "from start to end" of the string
							// hence without the following line "mqlskd slqk"
							// would be validated even
							// thought it has white spaces.
							return ((!optionCode.match(/\s+/)) && optionCode
									.match(/^[A-Za-z0-9_]*$/));
						},

						/**
						 * removes an option from the given label.
						 */
						removeOption : function(optionLabel) {
							var options = this.attributes.options;

							var labels = $.map(options, function(item) {
								return item[0];
							});
							var pos = $.inArray(optionLabel, labels);

							if (pos > -1) {
								options.splice(pos, 1);
							}
						},

						validateAll : function() {
							var attrs = this.attributes, errors = null;

							if (!attrs.optional) {
								if (isBlank(attrs.defaultValue)) {
									errors = errors || {};
									errors.defaultValue = (attrs.inputType === "DROPDOWN_LIST" ? "message.defaultOptionMandatory"
											: "message.defaultValueMandatory");
								}
							}
							if (isBlank(attrs.name)) {
								errors = errors || {};
								errors.name = "message.notBlank";
							}
							if (isBlank(attrs.label)) {
								errors = errors || {};
								errors.label = "message.notBlank";
							}
							if (isBlank(attrs.code)) {
								errors = errors || {};
								errors.code = "message.notBlank";
							} else if (!this.optionCodePatternValid(attrs.code)) {
								errors = errors || {};
								errors.code = "message.optionCodeInvalidPattern";
							}

							return errors;
						},

						validateOption : function(option) {
							var errors = null;

							// Validate option label
							if (isBlank(option[0])) {
								errors = errors || {};
								errors.optionLabel = "message.notBlank";

							} else if (this.optionAlreadyDefined(option[0])) {
								errors = errors || {};
								errors.optionLabel = "message.optionAlreadyDefined";

							}

							// validate option code
							if (isBlank(option[1])) {
								errors = errors || {};
								errors.optionCode = "message.notBlank";

							} else if (!this.optionCodePatternValid(option[1])) {
								errors = errors || {};
								errors.optionCode = "message.optionCodeInvalidPattern";

							} else if (this.optionCodeAlreadyDefined(option[1])) {
								errors = errors || {};
								errors.optionCode = "message.optionCodeAlreadyDefined";
							}

							return errors;
						},


						invalidTag : function(option) {
							var errors = null;
							errors = errors || {};
							errors.tagCode = "message.customFieldTag.add.notValid";
							return errors;
						}


					});

			return NewCustomFieldModel;
		});