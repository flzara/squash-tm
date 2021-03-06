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
define(["jquery", "jquery.squash.rangedatepicker", "squash.translator", "workspace.storage", "app/util/StringUtil", "underscore"],
	function ($, rangedatepicker, translator, storage, strUtils, _) {

		"use strict";
		var tableSelector = ".automation-table";

		function FilterMode(initConf) {
			/* jshint validthis:true */
			var table = $(tableSelector),
				self = this;
			this.active = false;
			this.key = 'automation-filter-' + initConf.customKey;
			table.find('>thead>tr').addClass("tp-filtermode-disabled");

			this._save = function (_search) {
				var sTable = table.squashTable(),
					columnDefs = sTable.fnSettings().aoColumns,
					searchObject = _search || sTable.fnSettings().aoPreSearchCols;

				if (this.active === false && isDefaultFiltering(searchObject)) {
					// a bit of cleanup doesn't harm
					storage.remove(this.key);
				}
				else {
					// add the column name to each entry in the searchObject
					_.each(searchObject, function (entry, idx) {
						entry.mDataProp = columnDefs[idx].mDataProp;
					});

					storage.set(this.key, {
						active: this.active,
						filter: searchObject
					});
				}
			};

			function _createCombo(th, id, content, sortable) {
				var combo = $("<select id='" + id + "' class='th_input filter_input' />");

				var nullOption = new Option("", "");
				$(nullOption).html("");

				combo.append(nullOption);

				$.each(content, function (index, value) {
					var o;
					if(th.hasClass("tp-th-createdby") || th.hasClass("tp-th-assignedto")) {
						o = new Option(value, value);
					} else {
						o = new Option(value, index);
					}

					$(o).html(value);
					combo.append(o);
				});
				if(sortable) {
					var options = combo.find("option");
					options.sort(function(a, b) { return $(a).text() > $(b).text() ? 1 : -1; });
					combo.html("").append(options);
				}
				th.append(combo);
			}

			function _createTimePicker(th) {
				th.append("<div class='rangedatepicker th_input'>"
					+ "<input class='rangedatepicker-input' readonly='readonly'/>"
					+ "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
					+ "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
					+ "</div>");
			}

			function isDefaultFiltering(currentFilter) {
				return $.grep(currentFilter, function (o) {
					return (strUtils.isBlank(o.sSearch));
				}).length === 0;
			}

			function findColFilterByName(filter, mDataProp) {
				return _.find(filter, function (f) { return f.mDataProp === mDataProp; });
			}


			function hideInputs() {
				table.find('>thead>tr').addClass('tp-filtermode-disabled');
			}

			function showInputs() {
				table.find('>thead>tr').removeClass('tp-filtermode-disabled');
			}

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
				var columnDefs = initConf.aoColumnDefs;
				headers.each(function (idx) {
					var $th = $(this),
						col = columnDefs[idx],
						colFilter = findColFilterByName(filter, col.mDataProp);
					if ($th.is('.tp-th-filter') && !!colFilter) {
						$th.find('.filter_input').val(colFilter.sSearch);
						$th.find('.rangedatepicker-input').val(colFilter.sSearch);
					}
				});
			}

			var model = squashtm.app;

			var tableId = table.attr("id");
			$(tableId + "_filter").hide();
			table.find('.tp-th-project-name,.tp-th-id,.tp-th-reference,.tp-th-label,.tp-th-priority,.tp-th-script,.assigned-script, ' +
				'.tp-th-uuid, .tp-th-conflictAssociation, .scm-url, .automated-test-reference')
				.append("<input class='th_input filter_input'/>");

			$('.tp-th-label .th_input').css('width', '90%');

			var userCombo = table.find(".tp-th-createdby"),
				statusCombo = table.find(".tp-th-status"),
				assignedToCombo = table.find(".tp-th-assignedto"),
				assignedTime = table.find(".tp-th-affectedon"),
				transmittedTime = table.find(".tp-th-transmittedon");


			if(userCombo.length !== 0) {
				var users = initConf.testers;
				_createCombo(userCombo, "#filter-mode-combo", users, true);
			}

			if(statusCombo.length !== 0) {
				_createCombo(statusCombo, "#filter-mode-combo", initConf.statuses, false);
			}

			if(assignedToCombo.length !== 0) {
				_createCombo(assignedToCombo, "#filter-mode-combo", model.assignableUsersGlobalView, true);
			}

			_createTimePicker(transmittedTime);
			_createTimePicker(assignedTime);

			this.loadSearchCols = function () {
				var state = storage.get(this.key);
				var columnDefs = initConf.aoColumnDefs;
				if (state === undefined || state.active === false) {
					return undefined;
				}
				else {
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

			var state = storage.get(this.key);

			if (state !== undefined) {
				this.active = state.active;
				restoreInputs(state.filter);
				if (this.active) {
					showInputs();
				} else {
					hideInputs();
				}
			}

			this.toggleFilter = function () {

				var filterObject;
				if (this.active) {
					this.active = false;
					filterObject = table.squashTable().fnSettings().aoPreSearchCols;
					this._save(filterObject);
					flushTableFilter();
					hideInputs();

				} else {
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
			var allInputs = table.find(".th_input");
			allInputs.click(function (event) {
				event.stopPropagation();
			}).keypress(function (event) {
				if (event.which == 13) {
					event.stopPropagation();
					event.preventDefault();
					event.target.blur();
					event.target.focus();
				}
			});

			table.find("th").hover(function (event) {
				event.stopPropagation();
			});

			allInputs.change(function () {
				var sTable = table.squashTable(),
					settings = sTable.fnSettings(),
					api = settings.oApi,
					headers = table.find("th");

				var visiIndex = headers.index($(this).parents("th:first")),
					realIndex = api._fnVisibleToColumnIndex(settings, visiIndex);


				var realInput = $(this).parent().find(".filter_input").get(0);
				sTable.fnFilter(realInput.value, realIndex);
				self._save();
			});

		}

		return {
			newInst: function (conf) {
				return new FilterMode(conf);
			}
		};

	});
