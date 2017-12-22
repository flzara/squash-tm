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
define(["jquery", "./lib/jquery.staticCustomfield", "./lib/jquery.jeditableCustomfield"],
	function ($) {
		"use strict";
		/******************************
		 * JS DATATABLE CONFIGURATION
		 ******************************/

		// ***** column definitions
		function createColumnDefs(cufDefinitions) {

			var columns = [];
			var i, total = cufDefinitions.length;

			for (i = 0; i < total; i++) {

				var code = cufDefinitions[i].code;
				var type = cufDefinitions[i].itype;
				var newColumn;

				if (cufDefinitions[i].denormalized) {
					newColumn = {
						'bVisible': true,
						'bSortable': false,
						'mDataProp': "denormalizedFields." + code + ".value",
						'sClass': 'denormalized-field-value denormalized-field-' + code + " cft-" + type,
						'sWidth': "5em",
						'aTargets': ['denormalized-field-' + code]
					};
				} else {
					newColumn = {
						'bVisible': true,
						'bSortable': false,
						'mDataProp': "customFields." + code + ".value",
						'sClass': 'custom-field-value custom-field-' + code + " cft-" + type,
						'sWidth': "5em",
						'aTargets': ['custom-field-' + code]
					};
				}

				// special delivery for tags :
				if (cufDefinitions[i].itype === "TAG") {
					newColumn.mRender = function (data, type, full) {
						if (data != null) { // check for call step
							var html = "<ul>";
							$.each(data, function (idx, t) {
								html += "<li>" + t + "</li>";
							});
							html += "</ul>";
							return html;
						} else {
							return "";
						}
					};
				}

				columns.push(newColumn);

			}

			return columns;

		}

		function mergeColumnDefs(regularColumnDefs, cufColumnDefs, insertionIndex) {
			/*
			 * update the aTargets of the existing columns if they use an index (instead of a classname) and if their index is
			 * above the insertionIndex
			 */
			var i, regularLength = regularColumnDefs.length, cufLength = cufColumnDefs.length;

			for (i = 0; i < regularLength; i++) {
				var regDef = regularColumnDefs[i];
				var aTarget = regDef.aTargets[0];
				if ((typeof aTarget == "number") && (aTarget >= insertionIndex)) {
					regDef.aTargets[0] = aTarget + cufLength;
				}
			}

			// no we can merge the column defs
			var spliceArgs = [insertionIndex, 0].concat(cufColumnDefs);
			Array.prototype.splice.apply(regularColumnDefs, spliceArgs);

			return regularColumnDefs;

		}

		// **** table draw callback
		function mapDefinitionsToCode(cufDefinitions) {

			var resultMap = {};

			var i = 0, length = cufDefinitions.length;

			for (i = 0; i < length; i++) {
				var currentDef = cufDefinitions[i];
				resultMap[currentDef.code] = currentDef;
			}

			return resultMap;
		}

		// we have to make a post function for our jeditable custom fields. Indeed
		// we can't know at creation time what the id of the selected cuf will be (obviously).
		// hence this function, that can fetch it at runtime.
		function makePostFunction(cufCode, table) {
			return function (value) {

				var $this = $(this),
					cell = $this.closest('td'),
					row = $this.closest('tr').get(0);

				var cufId,
					url = window.squashtm.app.contextRoot,
					isDenormalized = cell.hasClass('denormalized-field-value');

				if (isDenormalized) {
					cufId = table.fnGetData(row).denormalizedFields[cufCode].id;
					url += "/denormalized-fields/values/" + cufId;
				}
				else {
					cufId = table.fnGetData(row).customFields[cufCode].id;
					url += "/custom-fields/values/" + cufId;
				}

				return $.ajax({
					url: url,
					type: 'POST',
					data: JSON.stringify(value),
					contentType: 'application/json'
				});
			};
		}


		function createCufValuesDrawCallback(cufDefinitions, editable) {

			var definitionMap = mapDefinitionsToCode(cufDefinitions);

			return function () {

				var table = this;
				var defMap = definitionMap;
				var isEditable = editable;

				// A cell holds a custom field value if it has the class
				// .custom-field-value, and if the data model is not empty
				// for that one. Same goes for denormalized custom field, which
				// has a class .denormalized-field-value
				var cufCells = table.find('td.custom-field-value, td.denormalized-field-value').filter(function () {
					return (table.fnGetData(this) !== null);
				});


				// now wrap the content with a span
				cufCells.wrapInner('<span/>');

				for (var code in defMap) {

					var def = defMap[code];

					var cufselts;
					if (def.itype === "TAG") {
						cufselts = table.find('td.custom-field-' + code + '>span>ul, td.denormalized-field-' + code + '>span>ul');
					}
					else {
						cufselts = table.find('td.custom-field-' + code + '>span, td.denormalized-field-' + code + '>span');
					}

					if (isEditable) {
						var postFunction = makePostFunction(code, table);
						cufselts.jeditableCustomfield(def, postFunction);
					}
					else {
						cufselts.staticCustomfield(def);
					}

				}

			};
		}

		// ************ datasource model
		// configuration **************************************

		function defaultFnServerDataImpl(sSource, aoData, fnCallback, oSettings) {
			oSettings.jqXHR = $.ajax({
				"dataType": 'json',
				"type": oSettings.sServerMethod,
				"url": sSource,
				"data": aoData,
				"success": fnCallback
			});
		}

		/*
		 *
		 */
		function createDefaultDefinitions(cufDefinitions) {
			var i,
				length = cufDefinitions.length,
				cufCode,
				cufOrDeno,
				result = {
					customFields: {},
					denormalizedFields: {}
				};

			for (i = 0; i < length; i++) {
				cufCode = cufDefinitions[i].code;
				cufOrDeno = (cufDefinitions[i].denormalized ? "denormalizedFields" : "customFields");

				result[cufOrDeno][cufCode] = {
					id: null,
					value: null,
					code: null
				};

			}

			return result;
		}

		function fillMissingCustomFields(aaData, defaults) {

			var length = aaData.length, i, data;

			for (i = 0; i < length; i++) {

				data = aaData[i];
				data.customFields = $.extend({}, defaults.customFields, data.customFields);
				data.denormalizedFields = $.extend({}, defaults.denormalizedFields, data.denormalizedFields);

			}

			return aaData;
		}

		// the goal here is to prevent faulty table redraw if some custom
		// fields aren't part of the model of a given row.
		// it may happen for tables mixing heterogeneous data, eg action
		// steps/call steps, or testcase from project A or project B.
		//
		// Hence we aim to fill the holes in the model, by decorating the
		// function fnCallback, then invoke the initial
		// decoratedFnServerData with it.
		function ajaxPostProcessorFactory(defaultDefinitions, decoratedFnServerData) {

			// now the decorated fnServerData function, that will invoke the
			// original fnServerData with the decorated callback
			return function (sSource, aoData, fnCallback, oSettings) {

				var decoratedCallback = function (json, xhr, statusText) {

					var ajaxProp = oSettings.sAjaxDataProp;
					if (ajaxProp === "data") { // 'data' is the default starting with DT 1.10
						ajaxProp = "aaData";
						window.console.log("[table-handler.js] WARN: Legacy compatibility - sAjaxDataProp returned 'data', 'aaData' will be used instead");
					}

					var origData = (ajaxProp !== "") ? json[ajaxProp] : json;
					var fixedData = fillMissingCustomFields(origData, defaultDefinitions);

					var fixedJson;
					if (ajaxProp !== "") {
						fixedJson = json;
						fixedJson[ajaxProp] = fixedData;
					} else {
						fixedJson = fixedData;
					}

					fnCallback.call(this, fixedJson, xhr, statusText);
				};

				decoratedFnServerData.call(this, sSource, aoData, decoratedCallback, oSettings);
			};
		}

		// **** main decorator

		function decorateTableSettings(tableSettings, cufDefinitions, index, isEditable) {

			var editable = (isEditable === undefined) ? false : isEditable;

			// decorate the column definitions
			var cufDefs = createColumnDefs(cufDefinitions);
			var origDef = tableSettings.aoColumnDefs;
			tableSettings.aoColumnDefs = mergeColumnDefs(origDef, cufDefs, index);

			// decorate the model and ajax processor
			var defaultDefinitions = createDefaultDefinitions(cufDefinitions);

			if (tableSettings.aaData !== undefined) {
				tableSettings.aaData = fillMissingCustomFields(tableSettings.aaData, defaultDefinitions);
			}

			var origFnServerData = tableSettings.fnServerData || defaultFnServerDataImpl;
			tableSettings.fnServerData = ajaxPostProcessorFactory(defaultDefinitions, origFnServerData);

			// decorate the table draw callback
			var oldDrawCallback = tableSettings.fnDrawCallback;

			var addendumCallback = createCufValuesDrawCallback(cufDefinitions, editable);

			tableSettings.fnDrawCallback = function () {
				if (!!oldDrawCallback) {
					oldDrawCallback.apply(this, arguments);
				}
				addendumCallback.call(this);
			};

			return tableSettings;
		}

		/***************************
		 * DOM TABLE CONFIGURATION
		 ***************************/

		function decorateDOMTable(zeTable, cufDefinitions, index) {
			var table = (zeTable instanceof jQuery) ? zeTable : $(zeTable);

			// create the new header columns
			var newTDSet = $();

			var i, length = cufDefinitions.length;

			for (i = 0; i < length; i++) {
				var def = cufDefinitions[i];
				var newTD;
				if (cufDefinitions[i].denormalized) {
					newTD = $('<th class="denormalized-field-' + def.code + '">' + def.label + '</th>');
				} else {
					newTD = $('<th class="custom-field-' + def.code + '">' + def.label + '</th>');
				}
				newTDSet = newTDSet.add(newTD);
			}

			// insert them
			var header = table.find('thead tr');
			var firstHeaders = header.find('th').slice(0, index);
			header.prepend(newTDSet);
			header.prepend(firstHeaders);

		}

		return {
			decorateTableSettings: decorateTableSettings,
			decorateDOMTable: decorateDOMTable
		};

	});
