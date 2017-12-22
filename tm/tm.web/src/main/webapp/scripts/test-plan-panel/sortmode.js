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
 * That object makes things according to the state of sorting of a datatable.
 */

/*
 * configuration : add the following data attr to a test plan table
 * <pre>
 *   <table class="test-plan-table" data-entity-id="..." data-entity-type="...">
 * </pre>
 *
 */
define([ "jquery", "workspace.storage", "app/util/ButtonUtil", "squash.attributeparser" ],
	function($, storage, ButtonUtil, attrparser) {
	"use strict";

	var tableSelector = ".test-plan-table";

	function SortMode(conf) {

		var $table = $(".test-plan-table");
		var entityId = $table.data("entity-id");
		var entityType = $table.data("entity-type");
		this.storage = storage;

		// **************** configuration ******************

		this.reorderable = conf.permissions.reorderable || false;

		if (!entityId) {
			throw "sortmode : entity id absent from table data attributes";
		}
		if (!entityType) {
			throw "sortmode : entity type absent from table data attributes";
		}

		this.key = entityType + "-sort-" + entityId;
		

		this.state = {
			active : false,		// whether the message is displayed and DnD disabled, and conversely
			saveable : true 	// whether saving the reordering is allowed or not. Note that it's different 
								// from the state of the Reorder button.
		};		

		

		// ******************* state logic ***********************


		function isDefaultSorting (_someSorting) {
			var someSorting = fromIndexedToNamed(_someSorting);
			var defaultSorting = StaticSortMode.defaultSorting();
			return (someSorting.length === 1 && someSorting[0][0] === defaultSorting[0][0] && someSorting[0][1] === defaultSorting[0][1]);
		};
		
		/* 
		 * This function will initialize an index that maps
		 * the name (mDataProp) of a column to its index. 
		 * 
		 * We need to handle the conversion between column names 
		 * and their index. It is so because storing the index 
		 * (as it should) could lead to inconsistencies because of 
		 * invisible column, table header being different depending 
		 * on the page it is displayed etc
		 * 
		 * The trick here is, when the sortmode is initialized the 
		 * squashtable might not be ready so we can't rely on its API.
		 * We have to parse the table header from scratch.
		 */
		

		var initIndex = $.proxy(function(){
			this.byIndex = {};
			this.byName = {};
			var self = this;
			
			var headers = $table.find('>thead>tr>th');
			
			for (var i=0; i< headers.length; i++){
				var head = $(headers[i]);
				var opts = attrparser.parse(head.data('def'));
				var name = opts.map;
				self.byIndex[i] = name;
				self.byName[name] = i;
			}
		}, this);
		
		function _convert(inputSorting, mapping){
			var outputSorting = [];
			for (var i=0; i<inputSorting.length; i++){
				var sorting = inputSorting[i];
				var converted = mapping[sorting[0]];
				if (converted !== undefined){ // prevent cases where the target column is not defined
					outputSorting.push([ converted, sorting[1] ]);
				}
			}
			return outputSorting;
		}
		
		var fromIndexedToNamed = $.proxy(function(sorting){
			return _convert(sorting, this.byIndex);
		}, this);
		
		var fromNamedToIndexed = $.proxy(function(sorting){
			return _convert(sorting, this.byName);
		}, this);
		
		// ****** private state transition function ********
		
		this._activate = function() {
			$("#test-plan-sort-mode-message").show();
			$(".test-plan-table").find(".select-handle").removeClass("drag-handle");
			this.state.active = true;
		};

		this._deactivate = function() {
			$("#test-plan-sort-mode-message").hide();
			$(tableSelector).find(".select-handle").addClass("drag-handle");
			this.state.active = false;
		};
		
		this._updateBtnState = function(){
			if (this.state.active && this.state.saveable && this.reorderable){
				ButtonUtil.enable($("#reorder-test-plan-button"));
			}
			else{
				ButtonUtil.disable($("#reorder-test-plan-button"));
			}
		};

		
		// ***** public functions ******
		
		this.enableReorder = function(){
			this.state.saveable = true;
			this.update();
		};
		
		this.disableReorder = function(){			
			this.state.saveable = false;
			this.update();
		};

		
		this.resetTableOrder = function(table) {
			var defSorting = StaticSortMode.defaultSorting();
			table.fnSettings().aaSorting = fromNamedToIndexed(defSorting);
			this.update();
		};
		

		this.update = function(_sort) {
			
			var sorting = _sort || $table.squashTable().fnSettings().aaSorting;
			
			// if has an argument
			if (isDefaultSorting(sorting)){
				this._deleteaaSorting();
				this._deactivate();				
			}
			else{
				this._saveaaSorting(sorting);
				this._activate();			
			}
			
			// and in any case : 
			this._updateBtnState();

		};


		// ******************** I/O ********************

		this.loadaaSorting = function() {
			var _sorting = this.storage.get(this.key);
			if (!! _sorting) {
				return fromNamedToIndexed(_sorting);
			} else {
				return fromNamedToIndexed(StaticSortMode.defaultSorting());
			}
		};

		this._saveaaSorting = function(aaSorting) {

			var sorting = fromIndexedToNamed(aaSorting);

			this.storage.set(this.key, sorting);
		};

		this._deleteaaSorting = function() {
			this.storage.remove(this.key);
		};
		
		
		// **************** init state ***************

		initIndex();
		var initialsort = this.loadaaSorting();
		this.update(initialsort);

	}

	var StaticSortMode = {
		newInst : function(conf) {
			return new SortMode(conf);
		},
		defaultSorting : function() {
			return [ [ "entity-index", "asc" ] ];
		}
	};

	return StaticSortMode;

});