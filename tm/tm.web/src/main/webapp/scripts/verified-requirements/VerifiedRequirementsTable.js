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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil",
         "app/ws/squashtm.notification", "squash.translator",
         "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.confirmdialog", "jeditable" ],
		function($, Backbone, _, StringUtil, notification, translator) {
	var VRTS = squashtm.app.verifiedRequirementsTableSettings;
	var VerifiedRequirementsTable = Backbone.View.extend({

		el : "#verified-requirements-table",

		initialize : function() {
			VRTS = squashtm.app.verifiedRequirementsTableSettings;
			this.removeRequirements = $.proxy(this._removeRequirements, this);
			this.removeRowRequirementVersion = $.proxy(this._removeRowRequirementVersion, this);
			this.requirementsTableDrawCallback = $.proxy(this._requirementsTableDrawCallback, this);
			this.requirementsTableRowCallback = $.proxy(this._requirementsTableRowCallback, this);
			this.removeSelectedRequirements = $.proxy(this._removeSelectedRequirements, this);
			this.confirmRemoveRequirements = $.proxy(this._confirmRemoveRequirements, this);
			this.addSelectEditableToVersionNumber = $.proxy(this._addSelectEditableToVersionNumber, this);
			this.refresh = $.proxy(this._refresh, this);
			this.refreshRestore = $.proxy(this._refreshRestore, this);
			this.getRowNumber = $.proxy(this._getRowNumber, this);
			this.configureTable.call(this);
			this.configurePopups.call(this);
		},

		events : {},

		configurePopups : function() {
			this.configureRemoveRequirementDialogs.call(this);
		},
		dataTableSettings : function(self) {
			return {
				// has Dom configuration
				"aaSorting" : [ [ 4, 'asc' ] ],
				"fnRowCallback" : this.requirementsTableRowCallback,
				"fnDrawCallback" : this.requirementsTableDrawCallback
			};
		},

		squashSettings : function(self) {

			var settings = {};

			settings.unbindButtons = {
				delegate : "#remove-verified-requirement-version-dialog",
				tooltip : translator.get('dialog.unbind-ta-project.tooltip')
			  };

			if (VRTS.linkable) {
				settings.buttons = [ {
					tooltip : VRTS.messages.remove,
					tdSelector : "td.unbind-button",
					uiIcon : "ui-icon-minus",
					jquery : true,
					delegate : "#remove-verifying-test-case-dialog",
					onClick : this.removeRowRequirementVersion
				} ];
			}

			return settings;

		},

		configureTable : function() {
			var self = this;
			this.table = this.$el.squashTable(self.dataTableSettings(self), self.squashSettings(self));
		},

		_requirementsTableDrawCallback : function() {
			if (this.table) {
				// We do not restore table
				// selection for first drawing
				// on pre-filled tables.
				this.table.restoreTableSelection();
				this.trigger("verifiedrequirementversions.tableDrawn");
			}
		},

		_requirementsTableRowCallback : function(row, data, displayIndex) {
			if (VRTS.linkable && data.status != "OBSOLETE") {
				this.addSelectEditableToVersionNumber(row, data);
			}
			return row;
		},

		_removeRowRequirementVersion : function(table, cell) {
			var row = cell.parentNode.parentNode;
			this.confirmRemoveRequirements([ row ]);
		},

		_removeSelectedRequirements : function() {
			var rows = this.table.getSelectedRows();
			this.confirmRemoveRequirements(rows);
		},

		_confirmRemoveRequirements : function(rows) {
			var self = this;
			this.toDeleteIds = [];
			var rvIds = $(rows).collect(function(row) {
				return self.table.getODataId(row);
			});
			var hasRequirement = (rvIds.length > 0);
			if (hasRequirement) {
				this.toDeleteIds = rvIds;
				var obsoleteStatuses = $(rows).not(function(index, row) {
					var data = self.table.fnGetData(row);
					return data.status != "OBSOLETE";
				});
				if (obsoleteStatuses.length > 0) {
					this.confirmRemoveObsoleteRequirementDialog.confirmDialog("open");
				} else {
					this.confirmRemoveRequirementDialog.confirmDialog("open");
				}
			} else {
				notification.showError(translator.get('message.EmptyTableSelection'));
			}

		},

		_removeRequirements : function() {
			var self = this;
			// Issue 4948 : Selected Id(s) can be a number or an array
			var ids =  (this.toDeleteIds !== undefined) ? this.toDeleteIds : this.$el.getSelectedIds() ;
			if (ids.length === 0) {
				return;
			}
			$.ajax({
				url : VRTS.url + '/' + ids.join(','),
				type : 'delete'
			}).done(self.refresh);
			localStorage.removeItem("selectedRow");
		},

		configureRemoveRequirementDialogs : function() {
			// confirmRemoveRequirementDialog
			this.confirmRemoveRequirementDialog = $("#remove-verified-requirement-version-dialog").confirmDialog();
			this.confirmRemoveRequirementDialog.width("600px");
			this.confirmRemoveRequirementDialog.on("confirmdialogconfirm", $.proxy(this.removeRequirements, this));
			this.confirmRemoveRequirementDialog.on("close", $.proxy(function() {
				this.toDeleteIds = [];
			}, this));
			// confirmRemoveObsoleteRequirementDialog
			this.confirmRemoveObsoleteRequirementDialog = $("#remove-obsolete-verified-requirement-version-dialog")
					.confirmDialog();
			this.confirmRemoveObsoleteRequirementDialog.width("600px");
			this.confirmRemoveObsoleteRequirementDialog.on("confirmdialogconfirm", $.proxy(this.removeRequirements,
					this));
			this.confirmRemoveObsoleteRequirementDialog.on("close", $.proxy(function() {
				this.toDeleteIds = [];
			}, this));
		},

		// =====================================================

		_addSelectEditableToVersionNumber : function(row, data) {
			var self = this;
			var urlPOST = VRTS.url + '/' + data["entity-id"];
			var urlGET = squashtm.app.contextRoot + '/requirement-versions/' + data["entity-id"] + '/version-numbers';

			// the table needs to be redrawn after each return
			// of the POST so we implement the posting workflow
			//TODO use SelectJEditable obj
			$('td.versionNumber', row).editable(function(value, settings) {
				var innerPOSTData;
				$.post(urlPOST, {
					value : value
				}, function(data) {
					innerPOSTData = data;
					self.refresh();
				});
				return (innerPOSTData);
			}, {
				type : 'select',
				submit : VRTS.messages.ok,
				cancel : VRTS.messages.cancel,
				onblur : function() {
				}, // prevents the widget to return to
				// unediting state on blur event
				// --%>
				loadurl : urlGET,
				onsubmit : function() {
				} // - do nothing for now
			});

		},

		_refresh : function() {
			var self = this;
			this.table.refresh();
			self.trigger("verifiedrequirementversions.refresh");
		},

		_refreshRestore : function() {
			var self = this;
			this.table.refreshRestore();
			self.trigger("verifiedrequirementversions.refresh");
		},

		_getRowNumber : function(){
			return this.table._fnGetTrNodes().length;
		}

	});
	return VerifiedRequirementsTable;
});
