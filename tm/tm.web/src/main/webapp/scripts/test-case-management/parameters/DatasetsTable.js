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
define([ "jquery", "backbone", "jeditable.simpleJEditable", "app/util/StringUtil", "jquery.squash.confirmdialog",
		"jquery.squash.messagedialog", "squashtable" ],
		function($, Backbone, SimpleJEditable, StringUtil) {
			var DatasetsTable = Backbone.View.extend({

				el : "#datasets-table",

				initialize : function(options) {
					this.settings = options.settings;

					// functions called on this
					this.removeRowDataset = $.proxy(this._removeRowDataset, this);
					this.confirmRemoveDataset = $.proxy(this._confirmRemoveDataset, this);
					this.removeDataset = $.proxy(this._removeDataset, this);
					this.refresh = $.proxy(this._refresh, this);
					this.reDraw = $.proxy(this._reDraw, this);
					this.updateTableDom = $.proxy(this._updateTableDom, this);
					this.datasetsTableRowCallback = $.proxy(this._datasetsTableRowCallback, this);
					this.createSimpleJEditable = $.proxy(this._createSimpleJEditable, this);

					// init actions
					this.initDataTableSettings(this);
					this.initSquashSettings(this);
					this.configureTable.call(this);
					this.configureRemoveDatasetDialog.call(this);

				},

				events : {

				},

				initDataTableSettings : function(self) {
					self.dataTableSettings = {
						"sAjaxSource" : self.settings.basic.testCaseDatasetsUrl,
						"bPaginate" : false,
						"aaSorting" : [ [ 2, 'asc' ] ],
						"aoColumnDefs" : JSON.parse(self.settings.datasetsAoColumnDefs),
						"fnRowCallback" : self.datasetsTableRowCallback
					};
				},

				initSquashSettings : function(self) {

					self.squashSettings = {};

					if (self.settings.permissions.isWritable) {
						self.squashSettings = {
							buttons : [ {
								tooltip : self.settings.language.remove,
								tdSelector : "td.delete-button",
								uiIcon : "ui-icon-trash",
								jquery : true,
								onClick : this.removeRowDataset
							} ]
						};
					}

				},

				configureTable : function() {
					var self = this;
					$(this.el).squashTable(self.dataTableSettings, self.squashSettings);
					this.table = $(this.el).squashTable();
				},

				// call datasetsTableRowCallback instead
				_datasetsTableRowCallback : function(row, data, displayIndex) {
					if (this.settings.permissions.isWritable) {
						this.addSimpleJEditableToName(row, data);
						this.addSimpleJEditableToParamValues(row, data);
					} else {
						this.initRow(row);
					}

					return row;
				},

				// Issue 6800 - For Test Runner & Guest, the row data was "id=?, value=?", but we only want the value
				// TODO: code refactoring ==> Duplicated code from 'addSimpleJEditableToParamValues'
				initRow : function(row) {
					var self = this;
					var components = $('td.parameter', row);
					$.each(components, function(index, value) {
						var $value = $(value);
						var cellText = $value.text();
						var textAttrs = StringUtil.parseSequence(cellText);
						var valuePrefix = "value=";
						var indexWhereValueStarts = cellText.indexOf(valuePrefix) + valuePrefix.length;
						var valueText = cellText.slice(indexWhereValueStarts, cellText.length);
						$value.text(valueText);
					});
				},

				addSimpleJEditableToName : function(row, data) {
					var self = this;
					var urlPost = self.settings.basic.datasetsUrl + '/' + data["entity-id"] + "/name";
					var component = $('td.dataset-name', row);
					self.createSimpleJEditable(urlPost, component);
				},

				addSimpleJEditableToParamValues : function(row, data) {
					var self = this;
					var urlPostStart = self.settings.basic.parameterValuesUrl + "/";
					var components = $('td.parameter', row);
					$.each(components, function(index, value) {

						// PARSE CELL'S TEXT INFORMATIONS
						// each parameter value cell's text must be structured as follow "id=<id>,value=<value>" (see
						// DatasetsDataTableModelHelper)
						// first we extract the cell's text infos
						var $value = $(value);
						var cellText = $value.text();
						var textAttrs = StringUtil.parseSequence(cellText);
						// then we fix the cell's text to show only it's value
						// var valueText = StringUtil.getParsedSequenceAttribute(textAttrs, "value");
						// code comented above doesn't work because of [Issue 3820]
						var valuePrefix = "value=";
						var indexWhereValueStarts = cellText.indexOf(valuePrefix) + valuePrefix.length;

						var valueText = cellText.slice(indexWhereValueStarts, cellText.length);
						$value.text(valueText);
						// and we build the url used for the SimpleJEditable
						var cellId = StringUtil.getParsedSequenceAttribute(textAttrs, "id");
						var urlPost = urlPostStart + cellId + "/param-value";

						// CREATE JEDITABLE
						self.createSimpleJEditable(urlPost, $(value));

					});
				},

				// call createSimpleJEditable instead
				_createSimpleJEditable : function(url, component) {
					var self = this;
					new SimpleJEditable({
						targetUrl : url,
						component : component,
						jeditableSettings : {}
					});
				},

				// call removeRowDataset instead//
				_removeRowDataset : function(table, cell) {
					var row = cell.parentNode.parentNode;
					this.confirmRemoveDataset(row);
				},

				// call confirmRemoveDataset instead//
				_confirmRemoveDataset : function(row) {
					var self = this;
					var paramId = self.table.getODataId(row);
					self.toDeleteId = paramId;
					self.confirmRemoveDatasetDialog.confirmDialog("open");
				},

				// call removeDataset instead
				_removeDataset : function() {
					var self = this;
					var id = this.toDeleteId;
					$.ajax({
						url : self.settings.basic.datasetsUrl + '/' + id,
						type : 'delete'
					}).done(self.refresh);
				},

				configureRemoveDatasetDialog : function() {
					var self = this;
					this.confirmRemoveDatasetDialog = $("#remove-dataset-confirm-dialog").confirmDialog();

					this.confirmRemoveDatasetDialog.on("confirmdialogconfirm", $.proxy(self._removeDataset, self));
					this.confirmRemoveDatasetDialog.on("close", $.proxy(function() {
						this.toDeleteId = null;
					}, this));

				},

				// call refresh instead
				_refresh : function() {
					this.table.fnDraw(false);
				},

				// call reDraw instead
				_reDraw : function() {
					var self = this;
					self.table.fnDestroy();
					// load aoColumnDefs
					$.ajax({
						url : self.settings.basic.testCaseDatasetsUrl + "/table/aoColumnDef",
						type : 'get'
					}).done(function(json) {
						self.dataTableSettings.aoColumnDefs = json;
						self.dataTableSettings.bDestroy = true;
						// modify table dom
						$.ajax({
							url : self.settings.basic.testCaseDatasetsUrl + "/table/param-headers",
							type : "get"
						}).done(self.updateTableDom).then(function() {
							// redraw table
							self.configureTable.call(self);
						});
					});

				},

				// call updateTableDom instead
				_updateTableDom : function(paramHeaders) {
					this.$("tbody tr").remove();
					this.$("thead th.parameter").remove();

					var thAfter = this.$("thead tr th.dataset-name ");
					for ( var i = 0; i < paramHeaders.length; i++) {
						var th = $("<th/>", {
							"class" : "parameter"
						});
						th.text(paramHeaders[i]['name']);
						th.attr('title', paramHeaders[i]['description']);
						thAfter.after(th);
						thAfter = th;
					}
				},

				refreshDataSetParameterName : function(parameterId, parameterName){
					var th = this.$("[data-id=" + parameterId + "]");
					th.attr('aria-label', parameterName);
					th = this.$("[data-id=" + parameterId + "] div");
					th.text(parameterName);
				},

				refreshDataSetParameterDescription : function(parameterId, parameterDescription){
					var th = this.$("[data-id=" + parameterId + "]");
					th.attr('title', parameterDescription);
				}
			});

			return DatasetsTable;

		});
