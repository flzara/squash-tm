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
define(["jquery",  "jquery.squash.rangedatepicker", "squash.translator", "workspace.storage", "app/util/StringUtil", "underscore", "squash.attributeparser" ],
		function($, rangedatepicker, translator, storage, strUtils, _, attrparser){

	"use strict";

	var tableSelector = '.test-plan-table';

	/*
	 * Prepare some default values
	 */
	var _weights = translator.get({
		'VERY_HIGH' : 'test-case.importance.VERY_HIGH',
		'HIGH' : 'test-case.importance.HIGH',
		'MEDIUM' : 'test-case.importance.MEDIUM',
		'LOW' : 'test-case.importance.LOW'
	});
	// add the level
	_weights['VERY_HIGH'] 	= '1-'+_weights['VERY_HIGH'];
	_weights['HIGH'] 		= '2-'+_weights['HIGH'];
	_weights['MEDIUM']		= '3-'+_weights['MEDIUM'];
	_weights['LOW'] 		= '4-'+_weights['LOW'];



	function FilterMode(initconf){

		// ****** setup *******

		var table = $(tableSelector),
			entityId = table.data('entity-id'),
			entityType = table.data('entity-type'),
			columnDefs = extractColumnDefs(table),
			self = this;

		if (!entityId) {
			throw "filtermode : entity id absent from table data attributes";
		}
		if (!entityType) {
			throw "filtermode : entity type absent from table data attributes";
		}

		this.key = entityType + "-filter-" + entityId;

		this.active = false;
		table.find('>thead>tr').addClass('tp-filtermode-disabled');



		/*
		 * Issue 6576
		 *
		 * The datatable normally identify columns by index/position. This is bad for us because the same test plan can be displayed either in the normal page or the manager
		 * page, which have different sets of columns and thus different indexing. So, to make sure that restoring a filter in a normal page that was saved in the manager page
		 * (or vice-versa) will not fail miserably we must enforce column identification by name.
		 *
		 * The problem is, the configuration that holds the column names - specifically the property mDataProp - is not set yet because this code is part of the initialization.
		 * In our case, that configuration is held by the DOM itself (see the attribute 'data-def' on each of the column headers).
		 * We must re-parse here the columns and build a model of what will be table.fnSettings().aoColumns (or aoColumnDefs if you wish).
		 */

		function extractColumnDefs(table){
			return table.find('thead>tr>th').map(function(idx){
				var conf = attrparser.parse($(this).data('def'));
				return {
					mDataProp : conf.map
				};
			});
		}


		// ******* filter management***********

		function isDefaultFiltering(currentFilter){
			return $.grep(currentFilter, function(o){
				return ( strUtils.isBlank(o.sSearch) );
			}).length === 0;
		}


		/*
		 * Issue 6576
		 *
		 * Save the names of the column with each filter entry.
		 *
		 */
		this._save = function(_search){
			var sTable = table.squashTable(),
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
			}
		};

		function findColFilterByName(filter, mDataProp){
			return _.find(filter, function(f){return f.mDataProp === mDataProp});
		}


		function hideInputs() {
			table.find('>thead>tr').addClass('tp-filtermode-disabled');
		}

		function showInputs() {
			table.find('>thead>tr').removeClass('tp-filtermode-disabled');
		};


		/*
		 * Issue 6576
		 *
		 * Will restore the filters according to the column name.
		 *
		 */
		function restoreTableFilter(filter){

			if (filter === undefined){
				return;
			}
			else{
				var settings = table.squashTable().fnSettings();

				$.each(settings.aoColumns, function(idx, column){
					var colFilter = findColFilterByName(filter, column.mDataProp),
						$th = $(column.nTh);

					// set the filter if the column is filterable, is visible and has a filter defined
					if (column.bVisible && $th.is('.tp-th-filter') && !!colFilter){
						column.sSearch = colFilter.sSearch;
						settings.aoPreSearchCols[idx] = colFilter;
					}
				});
			}

		}

		function flushTableFilter(){

			var searchObject = table.squashTable().fnSettings().aoPreSearchCols;

			for (var i=0;i<searchObject.length;i++){
				searchObject[i].sSearch = '';
			}

		}

		/*
		 * Issue 6576
		 *
		 * Will restore the inputs according to the column name.
		 *
		 */
		function restoreInputs(filter){

			if (state === null){
				return;
			}

			var headers = table.find('thead>tr>th');

			headers.each(function(idx){
				var $th = $(this),
					col = columnDefs[idx],
					colFilter = findColFilterByName(filter, col.mDataProp);

				if ($th.is('.tp-th-filter') && !!colFilter){
					$th.find('.filter_input').val(colFilter.sSearch);
				}
			});
		};



		// ************** CONSTRUCTOR **********



		function _createCombo(th, id, content){
			if (!th || !content) {
				return;
			}
			// handlebars, dammit
			var combo = $("<select id='"+id+"' class='th_input filter_input' />");

			var nullOption = new Option("", "");
			$(nullOption).html("");

			combo.append(nullOption);

			$.each(content, function(index, value) {
				var o = new Option(value, index);
				$(o).html(value);
				combo.append(o);
			});

			th.append(combo);
		}


		var tableId = table.attr("id");
		$( tableId + "_filter").hide();

		/*
		 * some of fields below can use some defaults values in case they were
		 * not overriden in the conf
		 */
		var users = initconf.basic.assignableUsers,
			statuses = initconf.messages.executionStatus,
			weights = initconf.basic.weights || _weights,
			modes = initconf.basic.modes;


		table.find(".tp-th-project-name,.tp-th-reference,.tp-th-name,.tp-th-dataset,.tp-th-suite")
			 .append("<input class='th_input filter_input'/>");



		var execmodeTH = table.find("th.tp-th-exec-mode"),
			importanceTH = table.find(".tp-th-importance"),
			statusTH = table.find(".tp-th-status"),
			assigneeTH = table.find(".tp-th-assignee");


		_createCombo(execmodeTH, "#filter-mode-combo", modes);
		_createCombo(statusTH, "#filter-status-combo", statuses);
		_createCombo(assigneeTH, "#filter-user-combo", users);
		_createCombo(importanceTH, "#filter-weight-combo", weights);

		// use handlebars, dammit !
		table.find(".tp-th-exec-on").append("<div class='rangedatepicker th_input '>"
								+ "<input class='rangedatepicker-input' readonly='readonly'/>"
								+ "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
								+ "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
								+ "</div>");


		// other event bindings
		var allInputs = table.find(".th_input");



		allInputs.click(function(event) {
			event.stopPropagation();
		}).keypress(function(event){
			if (event.which == 13 )
			{
				event.stopPropagation();
				event.preventDefault();
				event.target.blur();
				event.target.focus();
			}
		});

		table.find("th").hover(function(event) {
			event.stopPropagation();
		});

		allInputs.change(function() {
			var sTable = table.squashTable(),
				settings = sTable.fnSettings(),
				api = settings.oApi,
				headers = table.find("th");

			var visiIndex =  headers.index($(this).parents("th:first")),
				realIndex = api._fnVisibleToColumnIndex( settings, visiIndex );


			var realInput = $(this).parent().find(".filter_input").get(0);
			sTable.fnFilter(realInput.value, realIndex);
			self._save();
		});


		/*
		 * Careful here :
		 *
		 * The following delays the initialization of the rangedatepicker widget for later.
		 * It is so because it contains a nested table with headers. Those extra headers
		 * mess up with the proper datatable init code because they are treated just as its
		 * own headers.
		 *
		 * By delaying the initialization of that widget, we delay the insertion of the nested
		 * table to a phase where the datatable has been properly initialized.
		 *
		 */
		table.on('init.dt', function(){
			rangedatepicker.init();
		});


		/*
		 * Now restore the content of the input fields
		 *
		 * Note that, in order to perform an actual filtering operation
		 * the property table.squashTable().fnSettings().aoPreSearchCols must be
		 * initialized too. This is not done here because the datatable will take care
		 * of that by itself when it initialize, provided we configure it properly.
		 *
		 * See the function this.loadSearchCols below for that purpose.
		 */
		var state = storage.get(this.key);

		if (state !== undefined){
			this.active = state.active;
			restoreInputs(state.filter);
			if (this.active){
				showInputs();
			}
			else{
				hideInputs();
			}
		};


		// ************** /CONSTRUCTOR **********



		// returns an object compliant with the
		// datatable configuration object.
		this.loadSearchCols = function(){
			var state = storage.get(this.key);
			if (state === undefined || state.active === false){
				return undefined;
			}
			else{
				// return an object compliant with the datatable initialization option
				/*
				 * Issue 6576
				 *
				 * Because the search model that was saved may not match the column defs of the table
				 * being loaded here, we must adapt the returned object to the new table definition.
				 */
				return _.map(columnDefs, function(col, idx){
					var f = findColFilterByName(state.filter, col.mDataProp);
					var search = (!!f) ? f.sSearch : '';
					return {
						'search' : search
					};
				});
			}
		};


		/*
		 * Arguments :
		 * - (none) : returns whether the filter is active or not
		 * - int : returns true if the filter is active and the given column is being actively filtered on, identified by position
		 * - string : returns true if the filter is active and the given column is being actively filtered on, identified by name
		 *
		 */
		this.isFiltering = function(arg){

			if (arg === undefined){
				return this.active;
			}

			else if (_.isNumber(arg)){
				var filterNotDef = strUtils.isBlank(table.squashTable().fnSettings().aoPreSearchCols[arg].sSearch);
				return this.active && (! filterNotDef);
			}

			else {
				var idx = table.squashTable().getColumnIndexByName(arg);
				var filterNotDef = strUtils.isBlank(table.squashTable().fnSettings().aoPreSearchCols[idx].sSearch);
				return this.active && (! filterNotDef);
			}
		};



		this.toggleFilter = function(){

			var filterObject = undefined;

			// note that, depending on the branch,
			// the filter object is different;
			if (this.active){
				this.active = false;
				filterObject = table.squashTable().fnSettings().aoPreSearchCols;

				this._save(filterObject);

				flushTableFilter();
				hideInputs();

			}
			else{
				this.active = true;
				var state = storage.get(this.key);

				if (state !== undefined){
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
		newInst : function(conf){
			return new FilterMode(conf);
		}
	}

});
