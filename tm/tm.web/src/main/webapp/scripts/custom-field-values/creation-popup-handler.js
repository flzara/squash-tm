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
/*
 *
 * The CUFValuesCreator handles CustomFieldValues for entities that are being created.
 * They differ from the standard handling of the custom field values because, since they are
 * being created, they don't have an id yet. That's why we have a separate manager here.
 *
 */

define(
		[ "jquery", "handlebars", "underscore", "./lib/cuf-values-utils", "jqueryui", "./lib/jquery.editableCustomfield"],
		function($, handlebars, _, utils) {
		
			"use strict";
			utils.registerHandlebarHelpers(handlebars);
			
			var template =
				'{{#each this}}' +
				'<tr class="create-node-custom-field-row">' +
					'<td><label>{{label}}</label></td>' +
					'<td data-cuf-id="{{id}}" class="create-node-custom-field" >' +
					'{{#ifequals inputType.enumName "TAG"}}' +
						'<ul class="abort-key-enter">' +
						'{{#each defaultValue}}' +
							'<li>{{this}}</li>' +
						'{{/each}}' +
						'</ul>' +
					'{{else}}' +
						'{{defaultValue}}' + 
					'{{/ifequals}}' +
				'</td>' +
				'</tr>' +
				'{{/each}}';

			/*
			 * settings : - url : the url where to fetch the creator panel
			 * jQuery object (but not as a jQuery.DataTable)
			 */
			function CUFValuesCreator(settings) {

				this.table = settings.table;
				this.rowTemplate = handlebars.compile(template);

				if (this.table === undefined || !this.table.is('table')) {
					throw "illegal argument : the settings must provide an attribute 'table' referencing  a jquery table";
				}

				this.source = settings.source;

				/*
				 * loads the custom field values into the table. Parameter can be either :
				 * - undefined : this.source will be used instead
				 * - a String : will thus be treated as an URL where to fetch data from
				 * - else : directly the array of beans that define the custom fields. 
				 * 
				 */
				this.loadPanel = function(urlOrBeans) {
					var pleaseWait = $('<tr class="cuf-wait" style="line-height:50px;"><td colspan="2" style="height : 50px;" class="waiting-loading"></td></tr>');
					var table = this.table;
					var source = urlOrBeans || this.source;

					// cleanup of the previous calls (if any)
					table.find('.create-node-custom-field-row').remove();

					table.append(pleaseWait);

					var self = this;
					
					function generate(jsonDef){
						table.find(".cuf-wait").remove();
						// only required fields are shown in creation popup
						self.cufDefs = _.where(jsonDef, {optional: false});
						self.init();
					}
					
					if (typeof source === "string"){
						$.getJSON(source).success(generate);
					}
					else{
						generate(source);
					}
					// the source might have been redefined if a parameter was supplied -> redefine it
					this.source = source; 

				};

				/* reload the custom field values using the last source used*/
				this.reloadPanel = function() {
					this.loadPanel();
				};

				/* init the widgets used by the custom field values */
				this.init = function() {
					var table = this.table, cufDefs = this.cufDefs;

					table.append(this.rowTemplate(cufDefs));

					var fields = table.find(".create-node-custom-field");
					fields.find('.abort-key-enter').on('keydown', function(event) {
						if (event.keyCode === 13) {
							event.preventDefault();
							event.stopPropagation();
						}
					});
					
					if (fields.length > 0) {
						fields.each(function(idx) {
							var $this = $(this);
							$this.editableCustomfield(cufDefs[idx]);
							$this.append('<span class="error-message customFields-'+cufDefs[idx].id+'-error"></span>');
						});

						this.reset(table);
					}

				};

				/*
				 * reset the values of the custom field values. Will not
				 * reinitialise the widgets themselves.
				 */
				this.reset = function() {
					var table = this.table, cufDefs = this.cufDefs;

					var fields = table.find(".create-node-custom-field");
					if (fields.length > 0) {
						fields.each(function(idx) {
							var defValue = cufDefs[idx].defaultValue;
							$(this).editableCustomfield("value", defValue);
						});
					}
				};

				this.destroy = function() {
					var table = this.table;

					var field = table.find(".create-node-custom-field");
					if (field.length > 0) {
						field.each(function(idx) {
							$(this).editableCustomfield("destroy");
						});
					}
				};

				/*
				 * returns : a map of { id, value }, suitable for posting with
				 * the rest of the entity model
				 */
				this.readValues = function() {
					var result = {
						customFields : {}
					};
					var table = this.table,
						cufDefs = this.cufDefs;

					var $this,
						fields = table.find(".create-node-custom-field");
					
					if (fields.length > 0) {
						fields.each(function(idx) {
							$this = $(this);
							result.customFields[$this.data('cuf-id')] = $this.editableCustomfield("value");
						});
					}
					return result;
				};

			}

			

			return CUFValuesCreator;

		});