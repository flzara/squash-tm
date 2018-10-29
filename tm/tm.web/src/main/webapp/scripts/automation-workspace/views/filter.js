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
define(["jquery", "jquery.squash.rangedatepicker", "squash.translator", "workspace.storage", "app/util/StringUtil", "underscore", "squash.attributeparser"],
	function ($, rangedatepicker, translator, storage, strUtils, _, attrparser) {

		"use strict";
		var tableSelector = ".assigned-table";

		var formats = translator.get({
			"Gherkin": "test-case.format.gherkin",
			"Standard": "test-case.format.standard"
		})

		function filterMode(initConf) {
			var table = $(tableSelector),
				self = this;
			this.active = false;
			table.find('>thed>tr').addClass("tp-filtermode-disabled");

		
		this._save = function (_search) {
			/*var sTable = table.squashTable(),
				columnDefs = sTable.fnSettings().aoColumns,
				searchObject =  _search || sTable.fnSettings().aoPreSearchCols;
	
			if (this.active === false && isDefaultFiltering(searchObject)){
				// a bit of cleanup doesn't harm
				storage.remove(this.key);
			}
			else{
				// add the column name to each entry in the searchObject
				_.each(searchObject, function(entry,idx){
					entry.mDataProp = columnDefs[idx].mDataProp;
				});
	
				storage.set(this.key,{
					active : this.active,
					filter : searchObject
				});
			}*/
		};

		function _createCombo(th, id, content) {
			var combo = $("<select id='" + id + "' class='th_input filter_input' />");

			var nullOption = new Option("", "");
			$(nullOption).html("");

			combo.append(nullOption);

			$.each(content, function (index, value) {
				var o = new Option(value, index);
				$(o).html(value);
				combo.append(o);
			});

			th.append(combo);
		}

		function _createTimePicker(th) {
			th.append("<div class='rangedatepicker th_input '>"
				+ "<input class='rangedatepicker-input' readonly='readonly'/>"
				+ "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
				+ "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
				+ "</div>")
		}

		function isDefaultFiltering(currentFilter) {
			return $.grep(currentFilter, function (o) {
				return (strUtils.isBlank(o.sSearch));
			}).length == 0;
		}

		function findColFilterByName(filter, mDataProp) {
			return _.find(filter, function (f) { return f.mDataProp === mDataProp });
		}


		function hideInputs() {
			table.find('>thead>tr').addClass('tp-filtermode-disabled');
		}

		function showInputs() {
			table.find('>thead>tr').removeClass('tp-filtermode-disabled');
		};

		function restoreTableFilter(filter) {

			if (filter === undefined) {
				return;
			}
			else {
				var settings = table.squashTable().fnSettings();

				$.each(settings.aoColumns, function (idx, column) {
					var colFilter = findColFilterByName(filter, column.mDataProp),
						$th = $(column.nTh);

					// set the filter if the column is filterable, is visible and has a filter defined
					if (column.bVisible && $th.is('.tp-th-filter') && !!colFilter) {
						column.sSearch = colFilter.sSearch;
						settings.aoPreSearchCols[idx] = colFilter;
					}
				});
			}

		}

		function flushTableFilter() {

			var searchObject = table.squashTable().fnSettings().aoPreSearchCols;

			for (var i = 0; i < searchObject.length; i++) {
				searchObject[i].sSearch = '';
			}

		}

		function restoreInputs(filter) {

			if (state === null) {
				return;
			}

			var headers = table.find('thead>tr>th');

			headers.each(function (idx) {
				var $th = $(this),
					col = columnDefs[idx],
					colFilter = findColFilterByName(filter, col.mDataProp);

				if ($th.is('.tp-th-filter') && !!colFilter) {
					$th.find('.filter_input').val(colFilter.sSearch);
				}
			});
		};

		var tableId = table.attr("id");
		$(tableId + "_filter").hide();
		table.find('.tp-th-project-name,.tp-th-id,.tp-th-reference,.tp-th-label,.tp-th-priority,.tp-th-script')
			.append("<input class='th_input filter_input'/>");
		var userCombo = table.find(".tp-th-createdby"),
			formatCombo = table.find(".tp-th-format"),
			assignedTime = table.find(".tp-th-affectedon"),
			transmittedTime = table.find(".tp-th-transmittedon");
		_createCombo(userCombo, "#filter-mode-combo", formats);
		_createCombo(formatCombo, "#filter-mode-combo", formats		);
		
		transmittedTime.append("<div class='rangedatepicker th_input '>"
		+ "<input class='rangedatepicker-input' readonly='readonly'/>"
		+ "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
		+ "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
		+ "</div>");
		assignedTime.append("<div id='2' class='rangedatepicker th_input '>"
		+ "<input class='rangedatepicker-input' readonly='readonly'/>"
		+ "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
		+ "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
		+ "</div>");
		this.loadSearchCols = function () {
			var state = storage.get(this.key);
			if (state === undefined || state.active === false) {
				return undefined;
			}
			else {
				// return an object compliant with the datatable initialization option
				/*
				 * Issue 6576
				 *
				 * Because the search model that was saved may not match the column defs of the table
				 * being loaded here, we must adapt the returned object to the new table definition.
				 */
				return _.map(columnDefs, function (col, idx) {
					var f = findColFilterByName(state.filter, col.mDataProp);
					var search = (!!f) ? f.sSearch : '';
					return {
						'search': search
					};
				});
			}
		};
		this.isFiltering = function (arg) {

			var filterNotDef;
			if (arg === undefined) {
				return this.active;
			} else if (_.isNumber(arg)) {
				filterNotDef = strUtils.isBlank(table.squashTable().fnSettings().aoPreSearchCols[arg].sSearch);
				return this.active && (!filterNotDef);
			} else {
				var idx = table.squashTable().getColumnIndexByName(arg);
				filterNotDef = strUtils.isBlank(table.squashTable().fnSettings().aoPreSearchCols[idx].sSearch);
				return this.active && (!filterNotDef);
			}
		};

		table.on('init.dt', function () {
			rangedatepicker.init();
		});

		this.toggleFilter = function () {

			var filterObject = undefined;

			// note that, depending on the branch,
			// the filter object is different;
			if (this.active) {
				this.active = false;
				filterObject = table.squashTable().fnSettings().aoPreSearchCols;

				this._save(filterObject);

				flushTableFilter();
				hideInputs();

			}
			else {
				this.active = true;
				var state = storage.get(this.key);

				if (state !== undefined) {
					restoreTableFilter(state.filter);
				}

				showInputs();

				this._save();

			}


			table.squashTable().refresh();

			return this.active;
		};
	}

		return {
	newInst: function (conf) {
		return new filterMode(conf);
	}
}

	});
