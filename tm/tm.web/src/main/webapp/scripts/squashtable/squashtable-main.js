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
 dependencies :

 jquery,
 tableDnD,
 dataTables
 KeyEventListener
 jquery.squash.oneshotdialog.js
 jquery.squash.datatables

 */

/**
 * ======================Introduction===================================
 *
 * Legacy Ajax ----------------------------------------------
 *
 * Support for datatables v 1.10 is not ready yet. We force DataTable.ext.legacy.ajax=true.
 *
 * keys used for data lookup -------------------------
 *
 * That table uses mPropData for its columns. More explicitly, it uses json data as a map. Specifically, the defaults
 * keys used here are : - 'entity-id' : the entity id - 'entity-index' : the position of the entity when the list is
 * sorted
 *
 * Those keys may be redefined through configuration, using a field object 'dataKeys' : { ... dataKeys : { entityId :
 * default is 'entity-id' , entityIndex : default is 'entity-index' } }
 *
 * In some cases more keys might be required for the modules described below, refer to the documentation if need be.
 *
 *
 * Place-holders : --------------
 *
 * When configuring a module sometimes you will see that a given string supports place-holders. It means that anything
 * between curly braces '{something}' are place-holders that will be replaced by the corresponding value from
 * aoData["something"]. That's where the data keys above are useful.
 *
 *
 * filtering : -----------------
 *
 * activation : just give css class 'datatable-filterable' to the relevant th elements. Whenever a datatable is redrawn,
 * a hook will check if any filtering had been applied and enable/disable the class 'datatable-filtered' when
 * appropriate.
 *
 *
 * static functions : ----------
 *
 * $.fn.squashTable.configuration{ fromDOM(table) : table is either a selector or a jquery object pointing to the
 * datatable. returns [ datatableSettings, squashSettings ] on the basis of what could be found on various 'data-*'
 * attributes of the nodes. Best is to read the code and see what's available in there. }
 *
 *
 * $.fn.squashTable.decorator{ rewriteSentData(datatableSettings) : will decorate the datatableSettigns.fnServerData
 * with a preprocessor that will turn the mDataProp_x to something that makes sense to Spring databinder - eg, will
 * write mDataProp[x] instead. If the settings specified any fnServerParams, the decorator will append its code in last
 * position (and will not overwrite it). }
 *
 * =========== Regular Datatable settings=======================================
 *
 * the inherited part of the datatable is configured using the first parameter : 'datatableSettings'. Any regular
 * datatable configuration is supported.
 *
 * It uses defaults values yet the following parameters are still REQUIRED : - "oLanguage" (internationalization), -
 * "sAjaxSource" (ajax loading), - "aoColumnDefs" (the columns)
 *
 *
 * ============= object datasource and DOM data ================================
 *
 * Structured object datasource is great except when you need to read those data from the DOM. Normally the initial data
 * should be provided by other means (eg ajax call or supplied to the configuration), because DOM-based simply doesn't
 * fit. For instance, if you configure your column to use "mDataProp : 'cake.cherry'", datatable.js will crash because
 * it cannot find it in the DOM (because it assumes that all you want is a scalar, not an object).
 *
 * If you still decide to use an object datasource yet initialize it by reading the DOM, this datatable will help you to
 * work around this by creating the missing parts of the data object on the fly. Note that it still can produce buggy
 * datatables if later on the datatable uses data that couldn't be found that way.
 *
 * To enable this feature, please add to your configuration 'fixObjectDOMInit : true'
 *
 * ============= Squash additional settings=====================================
 *
 *
 * The squash specifics are configured using the second parameter : 'squashSettings', for additional configuration. The
 * next items describe the additional configuration available, that are passed as member of the 'squashSettings' object.
 * end of this file)
 *
 * ============= Squash table functions override================================
 *
 * Member name : 'functions' What : any function defined as public member of the table can be redefined as a member of
 * .functions (read the source to pimpoint them at the end of this file) param : an object { itemIds : array of row ids,
 * newIndex : the drop position } default : nothing
 *
 * examples : dropHandler : what : a function that must handle the row drop. param : an object { itemIds : array of row
 * ids, newIndex : the drop position } default : nothing
 *
 * getODataId : what : a function fetching the id from the data param : what $().dataTable().fnGetData() would normally
 * accept default : return fnGetData()["entity-id"] ============= Drag and drop :
 * =============================================
 *
 *
 * Member name : 'enableDnD' : true|false
 *
 *
 * ============== Hovering (css style) =======================================
 *
 * Member name : 'enableHover' : true|false.
 *
 *
 * ============== Object data model read from the DOM =========================
 *
 * Member name : 'fixObjectDOMInit' : true|false, refer to the documentation above ('object datasource and DOM data'),
 * default is false
 *
 *
 * ============== Generic multipurpose popup configuration ====================
 *
 * Member name : 'confirmPopup'
 *
 * If set, will configure any confirmation dialog used in that table. it's an object whose members are : oklabel : label
 * for okay buttons cancellabel : label for cancel buttons
 *
 * ============== Attachments ==================================================
 *
 * Member name : 'attachments'
 *
 * If the table finds tds having a given cssClass (see cssMatcher) if will turn them into link to the attachment
 * manager. 'attachments' is an object. It must define at least url. It may also override the others of course.
 *
 * url : url where the attachment manager is. Accepts placeholders. Note : that one accepts no defaults ! cssMatcher :
 * the css class of cells that must be treated. defaults to 'has-attachment-cells' aoDataNbAttach : the name of the
 * column in aoData where to look for how many attachment the row has. defaults to "nb-attachments" aoDataListId : the
 * name of the column in aoData where to look for the attachment list id, defaults to "attach-list-id"
 *
 * ============== Rich editables configuration ================================= *
 *
 * Member name : 'richEditables'
 *
 * If set, will attempt to turn some cells to rich editables. If undefined, nothing will happen. the property
 * 'richEditables' is an compound object and must define at least 1 member for 'target'.
 *
 * conf : a map of key-values. A key represents a css class and the value can either
 * - represents an url supporting placeholders or
 * - an url and an event name to trigger when the edit is completed the cell.
 *
 * Any td having the given css class will be turned to a rich jeditable configured with the standard condiguration
 * and posting to the supplied url.
 *
 * example :
 *
 * richEditable {
 *   'class1' : some/url,
 *   'class2' : {
 *      url : some/url,
 *      oncomplete : someeventname
 *   }
 * }
 *
 * ============== (text) editable configuration ======================================
 *
 * Just like for richEditables, but for text editable fields.
 *
 *
 * textEditables {
 *   'class1' : some/url,
 *   'class2' : {
 *      url : some/url,
 *      oncomplete : someeventname
 *   }
 * }
 *}
 *
 * ============== Execution status icons ======================================
 *
 * If a td has a css class of 'has-status', the table will automatically attempt to format
 * the content as an execution status (including style and translation). No other conf is
 * required.
 *
 *
 * ============== Delete row button ========================================
 *
 * Member name : 'deleteButtons'
 *
 * If set (ie not left undefined), then will look for cells having the css class 'delete-button'. It
 * turns them to buttons with an icon 'trash' or 'minus' ('trash' is default. See 'unbindButtons' if
 * you mean to use a 'minus' icon).
 *
 * Instead of configuring what happens on click the normal way (using callbacks etc), if your intended
 * handler is to open a dialog then you can do so by providing on of the following additional configuration
 *
 *
 * Option A : delegate to a dialog.
 * -------------------------------
 *
 * Just say which dialog the clicked button should open. When the button is clicked the following happens :
 * - the dialog's .data() map will receive the id of the entity displayed in that row (namely, dialog.data('entity-id', theid))
 * - the dialog is then opened.
 *
 * It's up to the dialog then to handle the click.
 *
 *  Configuration is as follow :
 *
 *  deleteButtons : {
 *  	delegate : jquery selector of another popup, that will be used instead of the generated one.
 *  }
 *
 *
 * Option B : define a dialog
 * --------------------------
 *
 * You may also configure a popup right here. When the button is clicked the following happens :
 * - a oneshotdialog is generated on the fly with a defined content,
 * - the confirm button will send a 'DELETE' request at the given URL,
 * - the cancel button will just deselect the selected row.
 *
 * The URL where a 'DELETE' request is issued supports placeholders.
 *
 * Configuration as follow
 *
 * deleteButtons : {
 * 	url : the url where to post the 'delete' instruction. Supports placeholders.
 * 	popupmessage : the message that will be displayed
 * 	tooltip : the tooltip displayed by the button
 * 	success : a callback on the ajax call when successful
 * 	fail : a callback on the ajax call when failed.
 * 	dataType : the dataType parameter for the post. (default = "text")
 * }
 *
 * ============== Add hyperlink to a cell =====================================
 *
 * Member name : 'bindLinks'
 *
 * If set then will look for cells according to the parameters given and make their text a link to the wanted url.
 *
 *  Configuration as follow:
 *
 *  bindLinks{
 *		list : [{
 *  		url : the url to wrap the  text with (place holder will be set to row object id)
 * 			target : the td rank in the row (starts with 1)
 * 			targetClass :  alternate to the above, uses css class to find its target
 * 			isOpenInTab : boolean to set the target of the url to  "_blank" or not.
 * 			beforeNavigate : function(row, data). A function that will be executed before navigation. Arguments will be the
 *  						row, and the data for this row, of the clicked element. 'this' will be the table.. If this function returns false,
 *  						the navigation will be aborted.
 *		}]
 *
 *	}
 *  list :  a list of object to represent each td of a row to make as url Object params as follow :
 *

 *
 * ============== Toggable rows ===============================================
 *
 * Member name : 'toggleRows'
 *
 * Coonfiguration as follow :
 *
 * {
 *   toggleRows : {
 *       '<css-selector-1>' : url where to load the content of an expanded row when the elements selected by 'css-selector-1'
 *                           are clicked.
 *
 *     '<css-selector-2>' : function(table, jqExpandedRow, jqNewRow){
 *          this function will load the content of an expanded row when the elements selected
 *          by 'css-selector-2' are clicked.
 *			},
 *       ...(more of them)
 *      }
 *  }
 *
 * }
 * ============== Add Tooltip to a cell =======================================
 *
 * -tooltips : it the property 'tooltips' is set, then tooltips will be added to the cells matching the given td selectors
 * example :
 *
 * tooltips = [
 * {tdSelector : "td.suites",
 *  value : "the value", function(row, data){return data["suitesTooltip"]}
 * }
 * ]
 *
 * ============== Add Buttons to a cell =======================================
 *
 * -buttons : if the property 'buttons' is set, then buttons will be added for each case described in the buttons table.
 * example :
 *
 * buttons = [
 *  {
 *  	tooltip : "tooltip",
		tdSelector : "td.run-step-button",
 *		cssclass : "classa",
 *		condition : function(row, data){return data["shouldDrawButton"];};
 *		disabled : function(row, data){return data["isDisabled"];};
 *		tdSelector : "td.run-step-button",
 *		onClick : function(table, cell){doThatWithTableAndCell(table, cell);} },
 *    tooltip : "tooltip",
 *}
 *
 * the buttons items properties are :
 *
 *{
 * 	tooltip : the button's tooltip
 *	cssclass : litteral or function(row, data). Define some css class added to the input button.
 *	uiIcon : litteral or function(row, data) if the button is to be a jqueryUi icon, set this property to the wanted icon name.
 *	condition : boolean or function(row, data). Says if the button is added to the row. if this property is not set
 *              the button will be added everywhere
 *	disabled : a boolean or a function(row, data). Return the boolean saying if the button needs to be disabled or not.
 *	tdSelector : the css selector to use to retrieve the cells where to put the button
 *	jquery : boolean. Tells whether this button needs to turn in a jquery button or not. Default is false.
 *	onClick : a function(table, cell) that will be called with the parameters table and clicked td
 *}
 *
 * ============== Autoindexing =======================================
 *
 *  - autonum : true|false, when true the table will take the first visible column and add pseudo indexes 1-based to it.
 *
 */

define(["jquery",
	"underscore",
	"squash.KeyEventListener",
	"squash.statusfactory",
	"squash.configmanager",
	"jquery.squash.oneshotdialog",
	"squash.translator",
	"squash.attributeparser",
	"datatables",
	"./squashtable.defaults",
	"./squashtable.pagination",
	"./squashtable.dnd",
	"./squashtable.datatype",
	"jquery.cookie"
], function ($, _, KeyEventListener, statusfactory, confman, oneshot, translator, attrparser) {
	// "use strict"; <- we're not there yet
	// crap alert : unscoped globals (eg. squashtm), invalid usage of "this"

	if (!!$.fn.squashTable) {
		return;
	}

	$.fn.DataTable.ext.legacy.ajax = true;

	function showTable(table) {
		table.removeClass('unstyled-table');
	}

	// for ajax-loaded tables, wait for full table init before showing it
	$(document).on("init.dt", function (event) {
		showTable($(event.target));
	});

	squashtm = squashtm || {};
	squashtm.keyEventListener = squashtm.keyEventListener || new KeyEventListener();

	/*******************************************************************************************************************
	 *
	 * The following functions assume that the instance of the datatable is 'this'.
	 *
	 * Note the '_' prefixing each of them.
	 *
	 * Typically when the squash datatable initialize it will also declare public methods that will access them. Those
	 * methods then have the same name, without the '_' prefix.
	 *
	 * In some of the functions here such methods belonging to 'this' are invoked. It's not a typo : it's the expected
	 * behaviour.
	 *
	 *
	 ******************************************************************************************************************/

	/*
	 * what : a function that must handle the row drop. param : an obect { itemIds : array of row ids, newIndex : the
	 * drop position } default : nothing
	 */
	function _dropHandler(dropData) {

	}

	/*
	 * what : a function fetching the id from the data param : what $().dataTable().fnGetData() would normally accept
	 * default : return aoData[0]; : the datatable expects the id to be first.
	 */
	function _getODataId(arg) {
		var key = this.squashSettings.dataKeys.entityId;
		var id = (!!this.fnGetData(arg)) ? this.fnGetData(arg)[key] : NaN;
		if ((!!id) && (!isNaN(id))) {
			return id;
		} else {
			return null;
		}
	}

	/**
	 * Enables DnD on the given table.
	 *
	 * As of TM 1.10.0, needs to be called once, at table creation time. Not anymore at each table content refresh.
	 *
	 * Note : we calculate the 'offset' because the first displayed element is not necessarily the first item of the
	 * table. For instance, if we are displaying page 3 and drop our rows at the top of the table view, the drop index
	 * is not 0 but (3*pagesize);
	 *
	 * @this: the datatable instance
	 */
	function _enableTableDragAndDrop() {
		if (!this.squashSettings.enableDnD) {
			return;
		}

		function arraysEq(arr1, arr2) {
			if (arr1.length !== arr2.length) {
				return false;
			}
			for (var i = 0; i < arr1.length; i++) {
				if (arr1[i] !== arr2[i]) {
					return false;
				}
			}
			return true;
		}

		var self = this;
		this.tableDnD({
			dragHandle: "drag-handle",
			onDragStart: function (table, rows) { // remember that we are
				// using our modified dnd :
				// rows is a jQuery object

				rows.find('.drag-handle').addClass('ui-state-active');
				var key = self.squashSettings.dataKeys.entityIndex;

				var offset = self.fnGetData(0)[key] - 1;
				self.data("offset", offset);

				var indexes = rows.map(function (i, e) {
					return e.rowIndex - 1;
				});
				self.data("indexes", indexes);

			},

			onDrop: function (table, rows) { // again, that is now a jQuery object

				var oldIndexes = self.data("indexes");
				var newIndexes = rows.map(function (i, e) {
					return e.rowIndex - 1;
				});

				if (!arraysEq(oldIndexes, newIndexes)) {

					var newInd = rows.get(0).rowIndex - 1;
					var offset = self.data("offset");

					// prepare the drop now
					var ids = [];
					rows.each(function (i, e) {
						var id = self.getODataId(e);
						ids.push(id);
					});

					self.dropHandler({
						itemIds: ids,
						newIndex: newInd + offset
					});
				}
			}

		});
	}

	/*
	 * For the current datatable, will bind hover coloring
	 */
	function _bindHover() {

		this.delegate('tr', 'mouseleave', function () {
			$(this).removeClass('ui-state-highlight');
		});

		this.delegate('tr', 'mouseenter', function () {
			var jqR = $(this);
			if (!jqR.hasClass('ui-state-row-selected')) {
				jqR.addClass('ui-state-highlight');
			}
		});

	}

	function _bindClickHandlerToSelectHandle() {
		var self = this;
		this.delegate('td.select-handle', 'click', function () {
			var row = this.parentNode;

			var ctrl = squashtm.keyEventListener.ctrl;
			var shift = squashtm.keyEventListener.shift;

			if (!ctrl && !shift) {
				_toggleRowAndDropSelectedRange.call(self, row);

			} else if (ctrl && !shift) {
				_toggleRowAndKeepSelectedRange.call(self, row);

			} else {
				_growSelectedRangeToRow.call(self, row);

			}

			_memorizeLastSelectedRow.call(self, row);
			clearRangeSelection();

			return true;
		});
	}

	/*
	 * that method programatically remove the highlight due to native range selection.
	 */
	function clearRangeSelection() {
		if (window.getSelection) {
			window.getSelection().removeAllRanges();
		} else if (document.selection) { // should come last; Opera!
			document.selection.empty();
		}
	}

	/* private */
	function _toggleRowAndDropSelectedRange(row) {
		var jqRow = $(row);
		jqRow.toggleClass('ui-state-row-selected').removeClass('ui-state-highlight');
		jqRow.parent().find('.ui-state-row-selected').not(row).removeClass('ui-state-row-selected');

	}

	/* private */
	function _toggleRowAndKeepSelectedRange(row) {
		$(row).toggleClass('ui-state-row-selected').removeClass('ui-state-highlight');
	}

	/* private */
	function _growSelectedRangeToRow(row) {
		var rows = this.$("tr");
		var range = this.computeSelectionRange.call(this, row);

		for (var i = range[0]; i <= range[1]; i++) {
			var r = rows[i];
			$(r).addClass('ui-state-row-selected');
		}

		$(row).removeClass('ui-state-highlight');
	}

	/**
	 * Computes the 0-based range of row that should be selected. Note : row._DT_RowIndex is a 0-based index.
	 */
	function _computeSelectionRange(row) {
		var baseRow = this.data("lastSelectedRow");
		var baseIndex = baseRow ? baseRow._DT_RowIndex : 0;
		var currentIndex = row._DT_RowIndex;

		var rangeMin = Math.min(baseIndex, currentIndex);

		var rangeMax = Math.max(baseIndex, currentIndex);

		return [rangeMin, rangeMax];
	}

	function _memorizeLastSelectedRow(row) {
		if ($(row).hasClass('ui-state-row-selected')) {
			this.data("lastSelectedRow", row);
		}
	}

	/**
	 * saves the ids of selected rows
	 */
	function _saveTableSelection() {
		var selectedIds = _getSelectedIds.call(this);
		this.data('selectedIds', selectedIds);
	}

	function _restoreTableSelection() {
		var selectedIds = this.data('selectedIds');
		if ((selectedIds instanceof Array) && (selectedIds.length > 0)) {
			_selectRows.call(this, selectedIds);
		}
	}


	function _selectRows(ids) {
		var rows = this.fnGetNodes();

		var self = this;
		$(rows).filter(function () {
			var rId = self.getODataId(this);
			return $.inArray(rId, ids) != -1;
		}).addClass('ui-state-row-selected');

	}

	// no arguments mean all rows
	function _deselectRows(ids) {
		var table = this;
		var rows = this.find('tbody tr');

		if (arguments.length > 0 && ids instanceof Array && ids.length > 0) {
			rows = rows.filter(function () {
				var rId = table.getODataId(this);
				return $.inArray(rId, ids) != -1;
			});
		}

		rows.removeClass('ui-state-row-selected');

	}

	/**
	 * @returns {Array} of ids of selected rows
	 */
	function _getSelectedIds() {
		var table = this;
		return table.getSelectedRows().map(function () {
			return table.getODataId(this);
		}).get();
	}

	/**
	 * @returns the data model corresponding to the given id
	 *
	 */
	function _getDataById(id) {
		var entityIdKey = this.squashSettings.dataKeys.entityId;
		var found = $.grep(this.fnGetData(), function (entry) {
			return entry[entityIdKey] == id;
		});
		if (found.length > 0) {
			return found[0];
		} else {
			return null;
		}
	}

	function _getSelectedRows() {
		var table = this;
		// note : we filter on the rows that are actually backed by a model
		return table.find('tbody tr.ui-state-row-selected').filter(function () {
			var found = true;
			try {
				found = (!!table.fnGetData(this));
			} catch (ex) {
				found = false;
			}
			return found;
		});
	}

	function _getRowsByIds(ids) {
		var table = this;
		return table.find('>tbody>tr').filter(function () {
			var id = table.getODataId(this);
			return (!!id && $.inArray(id, ids) !== -1);
		});
	}


	function _getColumnNameByIndex(idx) {
		var col = this.fnSettings().aoColumns[idx];
		return col.mDataProp;
	}

	function _getColumnIndexByName(name) {
		var cols = this.fnSettings().aoColumns;
		for (var i = 0; i < cols.length; i++) {
			if (cols[i].mDataProp === name) {
				return i;
			}
		}
		return -1; // if not found;
	}

	// reapped from the dataTable source :
	function _getAjaxParameters() {
		var settings = this.fnSettings();
		//gets the 'natural' parameters
		var parameters = $.fn.dataTableExt.oApi._fnAjaxParameters(settings);
		//process through callbacks chain
		$.fn.dataTableExt.oApi._fnCallbackFire(settings, 'aoServerParams', null, [parameters]);

		return parameters;
	}

	function _addHLinkToCellText(td, url, isOpenInTab) {
		var $td = $(td),
			link = $('<a></a>');

		link.attr('href', url);
		if (isOpenInTab) {
			link.attr('target', '_blank');
		}

		$td.contents().filter(function () {
			// IE doesn't define the constant Node so we'll use constant value
			// instead of Node.TEXT_NODE
			return this.nodeType == 3;
		}).wrap(link);

		return $td.find('a');
	}

	function _dereferenceNestedProperties(data, key) {
		var keys = key.split('.');
		var length = keys.length, nestedData = data;

		for (var i = 0; i < length; i++) {
			nestedData = nestedData[keys[i]];
		}

		return nestedData; // should be a scalar
	}

	function _resolvePlaceholders(input, data) {
		var pattern = /\{\S+\}/;
		var result = input;
		var match = pattern.exec(result);
		while (match) {
			var pHolder = match[0];
			var key = pHolder.substr(1, pHolder.length - 2);
			var value = _dereferenceNestedProperties(data, key);
			result = result.replace(pHolder, value);
			match = pattern.exec(result);
		}

		return result;
	}

	/*
	 * 'this' is the table. That function will be called as a draw callback.
	 */
	function _attachButtonsCallback() {

		var attachConf = this.squashSettings.attachments;

		var self = this;
		var cells = this.find('>tbody>tr>td.' + attachConf.cssMatcher);

		$(cells).each(function (i, cell) {

			var data = self.fnGetData(cell.parentNode);

			// first : set the proper icon
			var nbAttach = data[attachConf.aoDataNbAttach];
			var linkClass = (nbAttach > 0) ? "manage-attachments" : "add-attachments";

			// second : what url we navigate to when clicked.
			var url = _resolvePlaceholders.call(self, attachConf.url, data);

			// design the link and voila !
			var link = '<a href="' + url + '" class="' + linkClass + '"';

			if (attachConf.target !== undefined){
				link = link  + "target="+ attachConf.target;
			}
			link = link + '></a>';




			$(cell).html(link);
		});
	}

	/*
	 * again 'this' is the table instance.
	 *
	 * TODO : user squash.configmanager next time
	 */
	function _configureRichEditables() {
		_configureEditables.call(this, 'richEditable');
	}

	function _configureTextEditables() {
		_configureEditables.call(this, 'textEditable');
	}

	// editableType should be 'richEditable' ou 'textEditable'
	function _configureEditables(editableType) {
		var targets = this.squashSettings[editableType + 's'];
		var self = this;
		if (!targets) {
			return;
		}

		var baseconf = null;
		switch (editableType) {
			case 'richEditable' :
				baseconf = confman.getJeditableCkeditor();
				break;
			case 'textEditable' :
				baseconf = confman.getStdJeditable();
				break;
			default :
				throw "table '" + this + "' : unsupported editable type '" + editableType + "'";
		}

		var processCell = function (i, cell) {
			"use strict";
			var row = cell.parentNode;
			var data = self.fnGetData(row);
			var editableConf_url = _.isString(targets[css]) ? targets[css] : targets[css]['url'];
			var url = _resolvePlaceholders.call(self, editableConf_url, data);
			var finalConf = $.extend(true, {
				"url": url
			}, baseconf);

			if (!_.isString(targets[css])) {
				var evt = targets[css]['oncomplete'];
				finalConf.ajaxoptions = {
					complete: function () {
						self.trigger(evt, {
							id: self.getODataId(row),
							responseText: arguments[0].responseText
						});
					}
				};
			}

			$(cell)[editableType](finalConf);
		};

		for (var css in targets) {
			var cells = $('td.' + css, this);
			$(cells).each(processCell);
		}
	}


	function _configureExecutionStatus() {

		var cells = $('td.has-status', this);

		$(cells).each(function (i, cell) {

			var data = (cell.textContent) ? cell.textContent : cell.innerText;
			cell.innerHTML = statusfactory.getHtmlFor(data);
		});
	}

	function _bindButtons() {
		var buttons = this.squashSettings.buttons;
		var self = this;
		if (!buttons) {
			return;
		}
		$(buttons).each(function (i, button) {
			self.delegate(button.tdSelector + " > .table-button", "click", function () {
				if (!!button.onClick) {
					button.onClick(self, this);
				}
			});
		});
	}

	function _configureButtons() {
		var self = this;
		var buttons = this.squashSettings.buttons;

		if (!buttons) {
			return;
		}

		var cellProcessor = function (template) {
			return function (i, cell) {
				"use strict";
				var instance = template.clone(),
					$cell = $(cell),
					row = $cell.parent("tr")[0],
					data = self.fnGetData(row);

				// should the button be displayed in the first place ?
				var rendered = ($.isFunction(button.condition) ) ? button.condition(row, data) : button.condition;
				if (rendered === false) {
					return "continue"; // returning whatever non-false means 'continue'
				}

				// is the button disabled ?
				var disabled = ($.isFunction(button.disabled)) ? button.disabled(row, data) : button.disabled;
				if (disabled) {
					template.prop('disabled', true);
				}

				// additional classes ?
				var classes = ($.isFunction(button.cssclass)) ? button.cssclass(row, data) : button.cssclass;
				instance.addClass(classes);

				// an icon maybe ?
				var icon = ($.isFunction(button.uiIcon)) ? button.uiIcon(row, data) : button.uiIcon;

				if (button.jquery) {
					instance.squashButton({
						disabled: disabled,
						text: false,
						icons: {
							primary: icon
						}
					});
				} else {
					instance.addClass(icon);
				}

				//append
				$cell.empty().append(instance);
			};
		};

		for (var i = 0, len = buttons.length; i < len; i++) {
			var button = buttons[i];

			var template = $("<a/>", {
				'class': 'table-button',
				'title': button.tooltip
			});

			var cells = self.find(button.tdSelector);

			cells.each(cellProcessor(template));
		}
	}

	function _configureIcons() {
		var self = this;
		var icons = this.squashSettings.icons;
		if (!icons) {
			return;
		}

		var processCell = function (i, cell) {
			"use strict";
			var $cell = $(cell),
				row = $cell.closest("tr")[0],
				data = self.fnGetData(row);

			// find value if function
			var value = ($.isFunction(icon.value) ) ? icon.value(row, data) : icon.value;

			self.drawIcon(value, $cell);
		};

		var len = icons.length;
		for (var i = 0; i < len; i++) {
			var icon = icons[i];
			var cells = self.find(icon.tdSelector);
			cells.each(processCell);
		}

	}

	function _drawIcon(value, cell) {
		value += " sq-icon ";
		cell.addClass(value);
	}

	function _configureTooltips() {
		var self = this;
		var tooltips = this.squashSettings.tooltips;
		if (!tooltips) {
			return;
		}

		var processCell = function (i, cell) {
			"use strict";
			var $cell = $(cell),
				row = $cell.parent("tr")[0],
				data = self.fnGetData(row);

			// find value if function
			var value = ($.isFunction(tooltip.value) ) ? tooltip.value(row, data) : tooltip.value;

			$cell.attr('title', value);
		};

		var len = tooltips.length;
		for (var i = 0; i < len; i++) {

			var tooltip = tooltips[i];
			var cells = self.find(tooltip.tdSelector);

			cells.each(processCell);
		}
	}

	function _configureCheckBox() {
		var self = this;
		["checkbox", "radio"].forEach(function (inputType) {
			$("td." + inputType, self).each(function (i, item) {

				var $item = $(item);
				// If the item has a text, store the text into val, used for initilization
				if ($item.text() !== "") {
					$item.val($item.text());
				}
				var data = $item.val();

				var template = '<input type="' + inputType + '"';
				if (data === "true") {
					template = template + 'checked="true"';
				}
				template = template + '/>';
				$item.html(template);
			});
		});

	}

	function _configureDeleteButtons() {
		var deleteConf = this.squashSettings.deleteButtons;
		if (!deleteConf) {
			return;
		}
		var template = '<a >' + deleteConf.tooltip + '</a>';

		var cells = $('td.delete-button', this);
		this.drawDeleteButton(template, cells);
	}

	function _drawDeleteButton(template, cells) {

		cells.html(template);
		cells.find('a').button({
			text: false,
			icons: {
				primary: "ui-icon-trash"
			}
		});
	}

	function _configureUnbindButtons() {
		var unbindConf = this.squashSettings.unbindButtons;
		if (!unbindConf) {
			return;
		}
		var template = '<a >' + unbindConf.tooltip + '</a>';
		var cellsUnbind = $('td.unbind-button', this);

		this.drawUnbindButton(template, cellsUnbind);
	}

	function _drawUnbindButton(template, cell) {
		cell.html(template);
		cell.find('a').button({
			text: false,
			icons: {
				primary: "ui-icon-minus"
			}
		});
	}


	function _bindUnbindButtons() {
		var self = this;
		var conf = self.squashSettings.unbindButtons;
		_bindUnbindOrDeleteButtons(conf, self, 'td.unbind-button > a');
	}

	function _bindDeleteButtons() {
		var self = this;
		var conf = self.squashSettings.deleteButtons;
		_bindUnbindOrDeleteButtons(conf, self, 'td.delete-button > a');
	}


	/*
	 * See documentation of 'deleteButtons' at the top of the file
	 */
	function _bindUnbindOrDeleteButtons(conf, self, target) {
		if (!conf) {
			return;
		}

		var deleteFunction = null;

		if (conf.delegate !== undefined) { // case 1 : delegate dialog
			deleteFunction = function () {
				//issue 7068 and related we should not remove previously selected rows
				self.deselectRows();
				var row = this.parentNode.parentNode;
				var jqRow = $(row);
				jqRow.addClass('ui-state-row-selected');

				var _delegate = $(conf.delegate);

				var _rowid = self.getODataId(jqRow.get(0));
				_delegate.data('entity-id', _rowid);

				var rowDatas = self.getDataById(_rowid);
				for (var rowData in rowDatas) {
					_delegate.data(rowData, rowDatas[rowData]);
				}

				// the following trick will open a dialog instance regardless of the actual
				// implementation used (the original jquery dialog or one of ours).
				var _data = _delegate.data();
				for (var _ppt in _data) {
					var _widg = _data[_ppt];
					if (_widg.uiDialog !== undefined && _widg.open !== undefined) {
						_widg.open();
						break;
					}
				}
			};

		} else { //case 2 : define a dialog
			deleteFunction = function () {
				//issue 7068 and related we should not remove previously selected rows
				self.deselectRows();
				var row = this.parentNode.parentNode;
				var jqRow = $(row);
				jqRow.addClass('ui-state-row-selected');

				oneshot.show(conf.tooltip || "", conf.popupmessage || "")
					.done(function () { // on click 'ok'
						var finalUrl = _resolvePlaceholders.call(self, conf.url, self.fnGetData(row));

						var dataType = (!!self.squashSettings.deleteButtons) ?
							self.squashSettings.deleteButtons.dataType :
							self.squashSettings.unbindButtons.dataType;

						dataType = dataType || "text";

						// do the request
						$.ajax({
							type: 'delete',
							url: finalUrl,
							dataType: dataType
						}).done(conf.success)
							.fail(conf.fail);

					}).fail(function () { // on click 'cancel'
					jqRow.removeClass('ui-state-row-selected');
				});
			};
		}

		self.on('click', target, deleteFunction);
	}


	/**
	 * Wrap cell text with link tags according to the given settings : squashSettings.bindLinks More info on top of the
	 * page on "Squash additional settings" doc.
	 *
	 */
	function _configureLinks() {
		var linksConf = this.squashSettings.bindLinks;
		if (!linksConf) {
			return;
		}

		var self = this;

		var cellFilter = function () {
			// IE doesn't define the constant Node so we'll use constant
			// value
			// instead of Node.TEXT_NODE
			return this.nodeType == 3;
		};

		var cellProcessor = function (index, cell) {
			"use strict";
			var row = cell.parentNode; // should be the tr
			var $cell = $(cell);
			var finalUrl = _resolvePlaceholders(linkConf.url, self.fnGetData(row));
			var cellLink = $cell.find("a");
			cellLink.attr('href', finalUrl);
		};

		for (var i = 0; i < linksConf.list.length; i++) {
			var linkConf = linksConf.list[i];
			// 1. build link
			var link = $('<a></a>');
			if (linkConf.isOpenInTab) {
				link.attr('target', '_blank');
			}

			// 2. select required td and wrap their thext with the built link
			var cellSelector = (!!linkConf.targetClass) ?
			"td." + linkConf.targetClass :
			'td:nth-child(' + linkConf.target + ')';

			var cells = self.find('>tbody ' + cellSelector);

			cells.contents().filter(cellFilter).wrap(link);

			// 3. add it to cells
			$.each(cells, cellProcessor);

			// 4 : if defined, configure the hooks on the hyperlinks
			if (linkConf.beforeNavigate !== undefined) {
				_bindBeforeNavigate(self, cellSelector, linkConf.beforeNavigate);
			}
		}
	}

	// this code was inlined in section 4 of _configureLinks
	// it was moved in there to ensure that the function
	// remains in the closure of the 'on click' handler
	function _bindBeforeNavigate(table, selector, fn) {
		table.on('click', selector + ' a', function (evt) {
			var row = $(evt.currentTarget).closest('tr'),
				rdata = table.fnGetData(row.get(0));
			if (fn.call(table, row, rdata) === false) {
				evt.preventDefault();
				evt.stopImmediatePropagation();
			}
		});
	}

	/**
	 * Unlike the above, that function will not be a member of the squash datatable. This is a factory function that
	 * returns a method handling the corner case of initializing an object based datasource from the DOM (refer to the
	 * documentation above).
	 *
	 * See also the function just below ( _fix_mDataProp )
	 *
	 * some bits are taken from jquery.datatable.js, sorry for the copy pasta.
	 */
	// TODO : some bits of it are now in squash.attributeparser (signatures may change though)
	function _createObjectDOMInitFixer(property) {

		function exists(data, property) {
			var localD = data;
			var a = property.split('.');
			for (var i = 0, iLen = a.length - 1; i < iLen; i++) {
				localD = localD[a[i]];
				if (localD === undefined) {
					return false;
				}
			}
			return true;
		}

		function setValue(data, val, property) {
			var localD = data;
			var a = property.split('.');
			for (var i = 0, iLen = a.length - 1; i < iLen; i++) {
				var ppt = a[i];
				if (localD[ppt] === undefined) {
					localD[ppt] = {};
				}
				localD = localD[ppt];
			}
			localD[a[a.length - 1]] = val;
		}

		function getValue(data, property) {
			var localD = data;
			var a = property.split('.');
			for (var i = 0, iLen = a.length; i < iLen; i++) {
				localD = localD[a[i]];
			}
			return localD;
		}

		return function (data, operation, val) {
			if (operation == 'set' && exists(data, property) === false) {
				setValue(data, val, property);
			} else {
				return getValue(data, property);
			}
		};

	}

	/**
	 * this function will process the column defs, looking for mDataProp settings using a dotted object notation, to fix
	 * them when reading the DOM (read documentation above).
	 *
	 */
	function _fix_mDataProp(datatableSettings) {

		var columnDefs = datatableSettings.aoColumnDefs;

		var needsWrapping = function (rowDef) {
			var mDataProp = rowDef.mDataProp;
			return ((!!mDataProp) && (typeof mDataProp === 'string') && (mDataProp.indexOf('.') != -1));
		};

		var length = columnDefs.length;
		for (var i = 0; i < length; i++) {
			var rowDef = columnDefs[i];
			if (needsWrapping(rowDef) === true) {
				var attribute = rowDef.mDataProp;
				rowDef.mDataProp = _createObjectDOMInitFixer(attribute);
			}

		}
	}

	function _applyFilteredStyle() {
		var isFiltered = this.fnSettings() && this.fnSettings().oPreviousSearch && (this.fnSettings().oPreviousSearch.sSearch.length > 0);
		if (isFiltered) {
			this.find('th.datatable-filterable').addClass('datatable-filtered');
		} else {
			this.find('th.datatable-filterable').removeClass('datatable-filtered');
		}
	}


	function _configureToggableRows() {

		var toggleSettings = this.squashSettings.toggleRows || {};
		var table = this;

		var template = $('<div><span class="small-right-arrow"></span></div>');

		var drawCallback = function (selector) {
			"use strict";
			return function () {
				this.find(selector).each(function (idx, cell) {
					var $cell = $(cell);
					$cell.wrapInner('<span class="toggle-row-label"/>');
					var togspan = $cell.find('span');
					$cell.empty();
					template.clone().append(togspan).appendTo(cell);
				});
			};
		};

		var clickCallback = function (loader) {
			"use strict";
			return function () {
				var jqspan = $(this),
					icon = jqspan.prev(),
					ltr = jqspan.parents('tr').get(0);

				if (!icon.hasClass('small-down-arrow')) {

					var rowClass = ($(ltr).hasClass("odd")) ? "odd" : "even",
						$ltr = $(ltr),
						$newTr = $(table.fnOpen(ltr, "   ", ""));

					$newTr.addClass(rowClass);

					icon.removeClass('small-right-arrow').addClass('small-down-arrow');

					if (typeof loader === "string") {
						// content loader assumed to be an url
						$newTr.load(loader);
					} else {
						// content loader assumed to be a function. The (table, table,...) arguments is not a typo.
						loader.call(table, table, $ltr, $newTr);
					}

				} else {
					table.fnClose(ltr);
					icon.removeClass('small-down-arrow').addClass('small-right-arrow');
				}
			};
		};

		for (var selector in toggleSettings) {
			// adds a draw callback. It will be then executed every time the table is reloaded
			this.drawcallbacks.push(drawCallback(selector));

			// click handler (executed one time only).
			var loader = toggleSettings[selector];

			this.on('click', selector + '>div> span.toggle-row-label', clickCallback(loader));
		}
	}

	// ************************ autonum  *****************************

	// note that this is a row callback, and a draw callback
	function _autonum(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
		if (this.squashSettings.autonum) {
			$(nRow).find('td:first').text(iDisplayIndex + 1);
		}
	}

	// ************************ functions used by the static functions
	// *****************************

	// ******** used by the main init method

	// [Issue 4891]
	// that method will make sure that conflicting column definitions
	// between DOM annotations and programmatic configuration
	// will merge nicely
	function mergeColumnDefs(domConf, jsConf) {

		// fast pass if no work is needed
		if (domConf.aoColumnDefs === undefined ||
			domConf.aoColumnDefs.length === 0 ||
			jsConf.aoColumnDefs === undefined ||
			jsConf.aoColumnDefs.length === 0) {
			return;
		}

		// utility function
		function findByTarget(columnDefs, aTarget) {
			for (var i = 0; i < columnDefs.length; i++) {
				var def = columnDefs[i];
				// compare the targets
				var diff = _.difference((def.aTargets || []), aTarget);
				if (diff.length === 0) {
					// if found, remove the element then return it
					// Array.prototype.splice just does that for us, wonderfull isnt it ?
					return columnDefs.splice(i, 1)[0];
				}
			}
			// when nothing was found in the array we return a new element
			return {};
		}

		// init with a copy of the domConf columns
		var mergedColumns = domConf.aoColumnDefs.slice();

		for (var j = 0; j < jsConf.aoColumnDefs.length; j++) {
			var jsDef = jsConf.aoColumnDefs[j],
				domDef = findByTarget(mergedColumns, jsDef.aTargets);

			var finalConf = $.extend(true, domDef, jsDef);
			mergedColumns.push(finalConf);
		}

		// now : those merged columns become the jsConf columnDefs,
		// the domConf columnDefs are wiped
		delete domConf.aoColumnDefs;
		jsConf.aoColumnDefs = mergedColumns;

	}

	// ******** configurator


	function _loopConfiguration(defs, handlers, conf) {
		for (var defItem in defs) {
			var handler = handlers[defItem];
			if (!!handler) {
				handler(conf, defs[defItem]);
			}
		}
	}

	function _tableDefs($table, conf) {

		var defSeq = $table.data('def') || '';
		var defs = attrparser.parse(defSeq);

		var handlers = $.fn.squashTable.configurator._DOMExprHandlers.table;

		return _loopConfiguration(defs, handlers, conf);

	}

	function _colDefs($table, conf) {

		var defaultCol = {
			bVisible: true,
			bSortable: false,
			sClass: ''
		};

		var headers = $table.find('thead th'), handlers = $.fn.squashTable.configurator._DOMExprHandlers.columns;

		headers.each(function (index) {
			var td = $(this),
				defSeq = td.data('def');

			// [Issue 4891] prevent useless column definition if nothing was defined for it
			if (defSeq === undefined) {
				return;
			}

			var defs = attrparser.parse(defSeq);

			conf.current = $.extend({}, defaultCol);
			conf.current.aTargets = [index];

			_loopConfiguration(defs, handlers, conf);

			conf.table.aoColumnDefs = conf.table.aoColumnDefs || [];
			conf.table.aoColumnDefs.push(conf.current);
		});

	}

	function _bodyDefs($table, conf) {
		// nothing yet
	}

	function _fromDOM($table) {

		var conf = {
			table: {},
			squash: {}
		};

		// table level definition
		_tableDefs($table, conf);

		// column level definition
		_colDefs($table, conf);

		// body level definition
		_bodyDefs($table, conf);

		return conf;

	}

	// ******** decorator ************************

	function _fnRewriteData(aoData) {

		var i,
			length = aoData.length,
			match,
			dataprop_regexp = /mDataProp_(\d+)/,
			sortcol_regexp = /iSortCol_(\d+)/,
			sortdir_regexp = /sSortDir_(\d+)/,
			search_regexp = /sSearch_(\d+)/;

		for (i = 0; i < length; i++) {

			if ((match = aoData[i].name.match(dataprop_regexp)) !== null) {
				aoData[i].name = "mDataProp[" + match[1] + "]";
			}
			else if ((match = aoData[i].name.match(sortcol_regexp)) !== null) {
				aoData[i].name = "iSortCol[" + match[1] + "]";
			}
			else if ((match = aoData[i].name.match(sortdir_regexp)) !== null) {
				aoData[i].name = "sSortDir[" + match[1] + "]";
			}
			else if ((match = aoData[i].name.match(search_regexp)) !== null) {
				aoData[i].name = "sSearches[" + match[1] + "]";
			}
		}
	}


	/*******************************************************************************************************************
	 *
	 * now we can declare our plugin
	 *
	 ******************************************************************************************************************/

	var datatableDefaults = $.extend(true, {}, squashtm.datatable.defaults);

	var squashDefaults = {
		enableHover: true,
		dataKeys: {
			entityId: 'entity-id',
			entityIndex: 'entity-index'
		},
		attachments: {
			cssMatcher: "has-attachment-cell",
			aoDataNbAttach: "nb-attachments",
			aoDataListId: "attach-list-id"
		},
		confirmPopup: {
			oklabel: "ok",
			cancellabel: "cancel"
		}
	};

	// let's figure out if i18n messages are available
	try {
		squashDefaults.confirmPopup.oklabel = squashtm.message.confirm;
		squashDefaults.confirmPopup.cancellabel = squashtm.message.cancel;
	} catch (wasUndefined) {
		// well, no big deal
	}


	$.fn.squashTable = function (datatableSettings, squashSettings) {

		/* *************************************************************
		 *
		 * 0 - Getter ?
		 *
		 * are we in retrieve mode or init mode ? the answer is simple : no
		 * param means retrieve mode. Note that the key is the dome element.
		 *
		 * *********************************************************** */

		if (arguments.length === 0) {
			return this.data('squashtableInstance');
		}

		/* *************************************************************
		 *
		 * 0 - Constructor ?
		 *
		 * If the function was not invoked as a getter for an existing
		 * instance, then we will create a new one with the supplied
		 * arguments.
		 *
		 ***************************************************************/

		/* ******************************************************************
		 * 1 - Settings augmentation
		 *
		 * Here we we tune some more the datatable configuration by preconfiguring
		 * some callbacks. Those callbacks may also have been configured by the
		 * user, so we will wrap our own definition around those instead of bluntly
		 * overriding them.
		 *
		 * **************************************************************** */

		// ---------- merge programmatic and DOM-based configuration --------

		var domConf = $.fn.squashTable.configurator.fromDOM(this);

		/*
		 * [Issue 4891]
		 * aoColumnDefs array are not correctly merged with $.extend :
		 * the merge is done by matching the content by their index, however
		 * in this case the aoColumnDefs must be matched by targets.
		 * $.extend([{target: 0}, {target:1}], [{target: 1}]) == [{target: 1}, {target:1}]
		 * so we have to manually merge the aoColumnDefs
		 */
		mergeColumnDefs(domConf.table, datatableSettings);

		var datatableEffective = $.extend(true, {}, datatableDefaults, domConf.table, datatableSettings);
		var squashEffective = $.extend(true, {}, squashDefaults, domConf.squash, squashSettings);


		this.drawcallbacks = [];
		this.rowcallbacks = [];


		this.squashSettings = squashEffective;

		// ---------- table drag and drop, if the configuration states there is ---------

		_enableTableDragAndDrop.call(this);

		// ---------- serverparams (1) : a good time to save the table selection --------

		var oldFnServParam = datatableEffective.fnServerParams;
		datatableEffective.fnServerParams = function (aoData) {
			_saveTableSelection.call(this);
			if (!!oldFnServParam) {
				oldFnServParam.call(this, aoData);
			}
		};

		// --------------- serverparams (2) : actually rewrite the data --------

		$.fn.squashTable.decorator.rewriteSentData(datatableEffective);

		// ---------------- init complete callback ----------------

		var userInitCompleteCallback = datatableEffective.fnInitComplete;
		datatableEffective.fnInitComplete = function (oSettings) {
			if (userInitCompleteCallback) {
				userInitCompleteCallback.call(this, oSettings);
			}
		};

		//----------------- row and draw callback ------------------------

		var aDrawCallbacks = this.drawcallbacks;

		aDrawCallbacks.push(_attachButtonsCallback);
		aDrawCallbacks.push(_configureRichEditables);
		aDrawCallbacks.push(_configureTextEditables);
		aDrawCallbacks.push(_configureExecutionStatus);
		aDrawCallbacks.push(_configureButtons);
		aDrawCallbacks.push(_configureDeleteButtons);
		aDrawCallbacks.push(_configureUnbindButtons);
		aDrawCallbacks.push(_configureCheckBox);
		aDrawCallbacks.push(_configureLinks);
		aDrawCallbacks.push(_restoreTableSelection);
		aDrawCallbacks.push(_applyFilteredStyle);
		aDrawCallbacks.push(_configureTooltips);
		aDrawCallbacks.push(_configureIcons);

		var aRowCallbacks = this.rowcallbacks;
		aRowCallbacks.push(_autonum);


		var userDrawCallback = datatableEffective.fnDrawCallback;

		datatableEffective.fnDrawCallback = function (oSettings) {

			if (userDrawCallback) {
				userDrawCallback.call(this, oSettings);
			}

			var len = this.drawcallbacks.length;
			for (var i = 0; i < len; i++) {
				this.drawcallbacks[i].call(this, oSettings);
			}

		};

		var userRowCallback = datatableEffective.fnRowCallback;

		datatableEffective.fnRowCallback = function (nRow, aData, iDisplayIndex, iDisplayIndexFull) {
			if (userRowCallback) {
				userRowCallback.call(this, nRow, aData, iDisplayIndex, iDisplayIndexFull);
			}

			var len = this.rowcallbacks.length;
			for (var i = 0; i < len; i++) {
				this.rowcallbacks[i].call(this, nRow, aData, iDisplayIndex, iDisplayIndexFull);
			}

		};


		/* *****************************************************
		 *
		 * 2 - public methods definition
		 *
		 ***************************************************** */
		this.computeSelectionRange = _computeSelectionRange;
		this.dropHandler = _dropHandler;
		this.getODataId = _getODataId;
		this.saveTableSelection = _saveTableSelection;
		this.restoreTableSelection = _restoreTableSelection;
		this.getSelectedIds = _getSelectedIds;
		this.getSelectedRows = _getSelectedRows;
		this.getAjaxParameters = _getAjaxParameters;
		this.getDataById = _getDataById;
		this.addHLinkToCellText = _addHLinkToCellText;
		this.selectRows = _selectRows;
		this.deselectRows = _deselectRows;
		this.configureLinks = _configureLinks;
		this.getRowsByIds = _getRowsByIds;

		this.attachButtonsCallback = _attachButtonsCallback;
		this.configureRichEditables = _configureRichEditables;
		this.configureTextEditables = _configureTextEditables;
		this.configureExecutionStatus = _configureExecutionStatus;
		this.configureDeleteButtons = _configureDeleteButtons;
		this.drawDeleteButton = _drawDeleteButton;
		this.drawUnbindButton = _drawUnbindButton;
		this.configureCheckBox = _configureCheckBox;
		this.enableTableDragAndDrop = _enableTableDragAndDrop;
		this.restoreTableSelection = _restoreTableSelection;
		this.applyFilteredStyle = _applyFilteredStyle;
		this.drawIcon = _drawIcon;
		this.autonum = _autonum;

		this.getColumnNameByIndex = _getColumnNameByIndex;
		this.getColumnIndexByName = _getColumnIndexByName;

		if (squashSettings && squashSettings.bindDeleteButtons) {
			this.bindDeleteButtons = squashSettings.bindDeleteButtons;
		} else {
			this.bindDeleteButtons = _bindDeleteButtons;
		}


		this.refresh = function () {
			this.fnDraw(false);
		};

		this.refreshRestore = function () {
			this.saveTableSelection();
			this.refresh();
			this.restoreTableSelection();
		};


		if (squashEffective.functions) {
			$.extend(this, squashEffective.functions);
		}


		if (squashEffective.fixObjectDOMInit) {
			_fix_mDataProp(datatableEffective);
		}

		if (squashEffective.toggleRows) {
			_configureToggableRows.call(this);
		}


		/* **********************************************************
		 *
		 * 3 - Final leg : creation and events
		 *
		 ********************************************************** */

		// ---------------- store the new instance ---------------------

		this.data('squashtableInstance', this);

		// ---------------- now call the base plugin -------------------

		this.dataTable(datatableEffective);

		// ---------------- event binding ------------------------------

		_bindClickHandlerToSelectHandle.call(this);

		if (squashEffective.enableHover) {
			_bindHover.call(this);
		}

		if (squashEffective.deleteButtons) {
			_bindDeleteButtons.call(this);
		}

		if (squashEffective.unbindButtons) {
			_bindUnbindButtons.call(this);
		}

		if (squashEffective.buttons) {
			_bindButtons.call(this);
		}

		this.addClass("is-contextual");

		// also, if the table uses defered loading, we must render it immediately (ie remove the class unstyle-table
		showTable(this);

		return this;
	};


	// *********************** static methods ***************************

	$.fn.squashTable.configurator = {

		fromDOM: function (table) {
			var dTable = (typeof table === "string") ? $(table) : table;
			return _fromDOM(dTable);
		},

		_DOMExprHandlers: {
			table: {
				'ajaxsource': function (conf, value) {
					conf.table.sAjaxSource = value;
				},
				'deferloading': function (conf, value) {
					conf.table.iDeferLoading = value;
				},
				'pre-filled': function (conf, value) {
					conf.table.iDeferLoading = 0;
				},
				'filter': function (conf, value) {
					var cnf = conf.table;
					cnf.bFilter = value;
					cnf.sDom = 'ft<"dataTables_footer"lp>';
				},
				'language': function (conf, value) {
					conf.table.oLanguage = conf.table.oLanguage || {};
					conf.table.oLanguage.sUrl = value;
				},
				'hover': function (conf, value) {
					conf.squash.enableHover = value;
				},
				'datakeys-id': function (conf, value) {
					conf.squash.dataKeys = conf.squash.dataKeys || {};
					conf.squash.dataKeys.entityId = value;
				},
				'pagesize': function (conf, value) {
					conf.table.iDisplayLength = parseInt(value, 10);
				},
				'autonum': function (conf) {
					conf.squash.autonum = true;
				},
				'pre-sort': function (conf, value) {
					// value must be an expression as follow : <columnindex>[-<asc|desc>]. If unspecified or invalid,
					// the default sorting order will be 'asc'.
					//multiples values can be used, separated by |. ex : <columnindex 1>[-<asc|desc>]|<columnindex 2>[-<asc|desc>]

					var sortings = [];
					var values = value.split("|");
					_.each(values, function (val) {
						var sorting = /(\d+)(-(asc|desc))?/.exec(val);
						var colIndex = sorting[1];
						var order = (sorting[3] !== undefined) ? sorting[3] : 'asc';
						sortings.push([colIndex, order]);
					});

					conf.table.aaSorting = sortings;
				}
			},
			columns: {
				'invisible': function (conf, value) {
					conf.current.bVisible = !((value === true) || (value === "true"));
				},
				'visible': function (conf, value) {
					conf.current.bVisible = (value === true) || (value === "true");
				},
				'sortable': function (conf, value) {
					conf.current.bSortable = true;
				},
				'searchable': function (conf, value) {
					value = value === "false" ? false : value; // because of js's shit coercion rules
					conf.current.bSearchable = Boolean(value).valueOf(); // almost everything is coerced to true -- which is the default anyway
				},
				'narrow': function (conf, value) {
					conf.current.sWidth = '2em';
				},
				'double-narrow': function (conf, value) {
					conf.current.sWidth = '4em';
				},
				'sWidth': function (conf, value) {
					conf.current.sWidth = value;
				},
				'filter': function (conf, value) {
					conf.current.sClass += ' datatable-filterable';
				},
				'sClass': function (conf, value) {
					conf.current.sClass += ' ' + value;
				},
				'map': function (conf, value) {
					conf.current.mDataProp = (value !== '') ? value : null;
				},
				'unmapped': function (conf, value) {
					conf.current.mDataProp = null;
				},
				'select': function (conf, value) {
					conf.current.sWidth = '2em';
					conf.current.sClass += ' select-handle centered';
				},
				'center': function (conf, value) {
					conf.current.sClass += ' centered';
				},
				'target': function (conf, value) {
					conf.current.aTargets = [value];
				},
				'sType': function (conf, value) {
					conf.current.sType = value;
				},
				'delete-button': function (conf, value) {
					// the following attributes must always be defined
					var cls = 'delete-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' delete-button centered ' + cls;
					conf.current.sWidth = '2em';

					// additionally, if this flag got a value (ie 'delete-button=#some-delegate-selector') we must specify it as the
					// delegate
					// cautious : the following expression must read it as "was a delegate defined ?"
					if (value !== true) {
						conf.squash.deleteButtons = {
							delegate: value,
							tooltip: $(value).prev().find('span.ui-dialog-title').text()
						};
					}
				},
				'unbind-button': function (conf, value) {
					var cls = 'unbind-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' unbind-button centered ' + cls;
					conf.current.sWidth = '2em';

					// see comment for delete-button
					if (value !== true) {
						conf.squash.unbindButtons = {
							delegate: value,
							tooltip: $(value).prev().find('span.ui-dialog-title').text()
						};
					}
				},
				'tooltip': function (conf, value) {
					var cls = 'tooltip-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' ' + cls;
					conf.squash.tooltips = conf.squash.tooltips || [];
					conf.squash.tooltips.push({
						value: value,
						tdSelector: 'td.' + cls
					});
				},
				'tooltip-target': function (conf, value) {
					var cls = 'tooltip-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' ' + cls;
					conf.squash.tooltips = conf.squash.tooltips || [];
					conf.squash.tooltips.push({
						value: function (row, data) {
							return data[value];
						},
						tdSelector: 'td.' + cls
					});
				},
				'link': function (conf, value) {
					var cls = 'link-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' ' + cls;
					conf.squash.bindLinks = conf.squash.bindLinks || {
							list: []
						};
					conf.squash.bindLinks.list.push({
						url: value,
						targetClass: cls
					});
				},
				'link-new-tab': function (conf, value) {
					var cls = 'link-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' ' + cls;
					conf.squash.bindLinks = conf.squash.bindLinks || {
							list: []
						};
					conf.squash.bindLinks.list.push({
						url: value,
						targetClass: cls,
						isOpenInTab: true
					});
				},
				// 'link-cookie' requires that a column definition 'link' or 'link-new-tab' was defined beforehand
				// if so, 'link-cookie' will set the defined cookie before navigation occurs.
				'link-cookie': function (conf, value) {

					/*
					 * First we must retrieve the 'link' configuration object,
					 * in order to complement it with the hook that sets the cookie.
					 *
					 * We do so by :
					 * 	- first, find which css class was set by the 'link' clause in conf.current.sClass,
					 *  - second, look it up in conf.squash.bindLinks
					 *
					 *  TODO : do something to make it easier next time
					 */

					var sClass = conf.current.sClass || '';
					var classmatch = /\blink-\d{3}\b/.exec(sClass);
					if (classmatch === null) {
						return;
					}
					var linkClass = classmatch[0];

					var linkConf = _.find(conf.squash.bindLinks.list, function (c) {
						return (c.targetClass === linkClass);
					});

					// now we can create the hook
					var oCook = attrparser.parse(value);

					linkConf.beforeNavigate = function (row, data) {
						// sets each attribute as a cookie
						for (var cookiename in oCook) {
							var rawvalue = oCook[cookiename];
							var cookievalue = _resolvePlaceholders(rawvalue, data);
							$.cookie(cookiename, cookievalue, {path: "/"});
						}
						return true;	// true ~== go on and navigate
					};

				},
				'rich-edit': function (conf, value) {
					var cls = 'rich-ed-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' ' + cls;
					conf.squash.richEditables = conf.squash.richEditables || {};
					conf.squash.richEditables[cls] = value;
				},
				'text-edit': function (conf, value) {
					var cls = 'text-ed-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += ' ' + cls;
					conf.squash.textEditables = conf.squash.textEditables || {};
					conf.squash.textEditables[cls] = value;
				},
				'checkbox': function (conf, value) {
					var cls = 'checkbox-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += 'checkbox centered ' + cls;

				},
				'radio': function (conf, value) {
					var cls = 'radio-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += 'radio centered ' + cls;

				},
				'icon': function (conf, value) {
					var cls = 'icon-' + Math.random().toString().substr(2, 3);
					conf.current.sClass += cls;
					conf.squash.icons = conf.squash.icons || [];
					conf.squash.icons.push({
						value: function (row, data) {
							return data[value];
						},
						tdSelector: 'td.' + cls
					});
				}
			}
		}
	};

	$.fn.squashTable.decorator = {
		rewriteSentData: function (datatableSettings) {
			var oldfnServerParams = datatableSettings.fnServerParams;
			datatableSettings.fnServerParams = function (aoData) {
				if (oldfnServerParams !== undefined) {
					oldfnServerParams.call(this, aoData);
				}
				_fnRewriteData(aoData);
			};
		}

	};


	return $.fn.squashTable;

});


