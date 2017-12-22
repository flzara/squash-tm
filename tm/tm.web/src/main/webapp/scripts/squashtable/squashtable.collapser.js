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
define([ "jquery" ], function() {

	function TableCollapserEvent(collapser) {

		this.collapser = collapser;
		this.eventHandlers = [];

		this.addHandler = function(eventHandler) {
			this.eventHandlers.push(eventHandler);
		};

		this.execute = function() {
			for ( var i = 0; i < this.eventHandlers.length; i++) {
				this.eventHandlers[i](this.collapser);
			}
		};

	}

	function makeDefaultCellSelector(columnsP) {
		return function(row) {
			var columns = columnsP;
			var length = columns.length;
			var result = [];
			var tds = $(row).children('td');
			for ( var i = 0; i < length; i++) {
				result.push(tds[columns[i]]);
			}
			return result;
		};
	}

	return function(dataTableP, columnsP) {

		var self = this;

		var dataTable = dataTableP;

		var cellSelector;
		if ($.isFunction(columnsP)) {
			cellSelector = columnsP;
		} else {
			cellSelector = makeDefaultCellSelector(columnsP);
		}

		var columns = columnsP;
		this.isOpen = true;
		var rows = [];
		this.collapsibleCells = [];
		this.closeHandlers = new TableCollapserEvent(this);
		this.openHandlers = new TableCollapserEvent(this);

		// ************** private functions ****************

		var indexCollapsibleCells = $.proxy(function() {

			this.collapsibleCells = [];

			var rows = dataTableP.children('tbody').children('tr');

			for ( var j = 0; j < rows.length; j++) {
				var cells = cellSelector(rows[j]);
				this.collapsibleCells = this.collapsibleCells.concat(cells);
			}
		}, this);

		var setCellsData = $.proxy(function() {
			for ( var k = 0; k < this.collapsibleCells.length; k++) {
				var cell = $(this.collapsibleCells[k]);
				cell.data('completeHtml', cell.html());
				var truncated = cell.text();
				var maxChar = 50;
				if (truncated.length > maxChar) {
					truncated = truncated.substring(0, 50) + " [...]";
				}
				cell.data('truncatedHtml', truncated);
			}

		}, this);

		// ****************** public functions ************

		this.onOpen = function(handler) {
			this.openHandlers.addHandler(handler);
		};

		this.onClose = function(handler) {
			this.closeHandlers.addHandler(handler);
		};

		this.closeAll = function() {
			indexCollapsibleCells();
			setCellsData();
			for ( var k = 0; k < this.collapsibleCells.length; k++) {
				var cell = $(this.collapsibleCells[k]);
				cell.html(cell.data('truncatedHtml'));
			}
			this.closeHandlers.execute();
			this.isOpen = false;

		};

		this.openAll = function() {
			for ( var k = 0; k < this.collapsibleCells.length; k++) {
				var cell = $(this.collapsibleCells[k]);
				cell.html(cell.data('completeHtml'));
			}
			this.openHandlers.execute();
			this.isOpen = true;
		};

		this.refreshTable = function() {
			indexCollapsibleCells();
			if (!this.isOpen) {
				this.closeAll();
			}
		};

		// init
		indexCollapsibleCells();
	};
});
