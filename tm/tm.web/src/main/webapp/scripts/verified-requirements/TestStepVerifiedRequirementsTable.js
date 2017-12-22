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
define([ "jquery", "backbone", "underscore", "handlebars", "app/util/StringUtil", "./VerifiedRequirementsTable",
         "app/ws/squashtm.notification", "squash.translator",
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel", "squashtable",
		"jquery.squash.confirmdialog" ], function($,
		Backbone, _, Handlebars, StringUtil, VerifiedRequirementsTable, notification, translator) {
	var VRTS = squashtm.app.verifiedRequirementsTableSettings;
	var TestStepVerifiedRequirementsTable = VerifiedRequirementsTable.extend({
		initialize : function(options) {

			this.linkTemplate = Handlebars
					.compile('<label class="{{cssClass}} afterDisabled req-link-label"></label>');			
			
			this.constructor.__super__.initialize.apply(this, [ options ]);
			this.detachSelectedRequirements = $.proxy(this._detachSelectedRequirements, this);
			this.detachRequirements = $.proxy(this._detachRequirements, this);
			this.confirmDetachRequirements = $.proxy(this._confirmDetachRequirements, this);
			this.configureDetachRequirementDialog.call(this);

		},

		events : {
			'click .req-link-label' : '_changeLinkState'
		},

		_requirementsTableRowCallback : function(row, data, displayIndex) {
			if (VRTS.linkable && data.status != "OBSOLETE") {
				this.addSelectEditableToVersionNumber(row, data);
			}
			this.addLinkCheckboxToRow(row, data);
			return row;
		},

		addLinkCheckboxToRow : function(row, data, displayIndex) {

			// it is so because the information could be either a boolean or its string representation
			var checked = (data.verifiedByStep === "true" || data.verifiedByStep === true); 

			var cssClass = (checked) ? "ui-icon-link-dark-e-w" : "ui-icon-link-clear-e-w";

			var elt = this.linkTemplate({
				cssClass : cssClass
			});

			$('td.link-checkbox', row).append(elt);

		},

		_changeLinkState : function(evt) {
			if (VRTS.linkable) {
				var target = $(evt.currentTarget);
				var row = target.parents('tr:first').get(0);
				var data = this.table.fnGetData(row);
	
				var state = (data.verifiedByStep === "true" || data.verifiedByStep === true);
				var newState = !state;
				var id = data['entity-id'];
				var ajaxUrl = VRTS.stepUrl + '/' + id;
	
				var ajaxType = 'delete';
				if (newState) {
					ajaxType = 'post';
				}
				$.ajax({
					url : ajaxUrl,
					type : ajaxType
				}).success(function() {
					data.verifiedByStep = newState; // should use a setter to be clean
					target.toggleClass('ui-icon-link-dark-e-w').toggleClass('ui-icon-link-clear-e-w');
				}).fail(function() {
					// nothing, let the normal handler kick in
				});
			}
		},

		_detachSelectedRequirements : function() {
			var rows = this.table.getSelectedRows();
			this.confirmDetachRequirements(rows);
		},

		_confirmDetachRequirements : function(rows) {
			var self = this;
			this.toDetachIds = [];
			var rvIds = $(rows).collect(function(row) {
				return self.table.getODataId(row);
			});
			var hasRequirement = (rvIds.length > 0);
			if (hasRequirement) {
				this.toDetachIds = rvIds;
				this.confirmDetachRequirementDialog.confirmDialog("open");
			} else {
				notification.showError(translator.get('message.EmptyTableSelection'));
			}
		},

		_detachRequirements : function() {
			var self = this;
			var ids = this.table.getSelectedIds();	
			if (ids.length === 0 ) { 
				return;
			}
			$.ajax({
				url : VRTS.stepUrl + '/' + ids.join(','),
				type : 'delete'
			}).done(self.refresh);

		},

		configureDetachRequirementDialog : function() {
			this.confirmDetachRequirementDialog = $("#remove-verified-requirement-version-from-step-dialog").confirmDialog();
			this.confirmDetachRequirementDialog.width("600px");
			this.confirmDetachRequirementDialog.on("confirmdialogconfirm", $.proxy(this.detachRequirements, this));
			this.confirmDetachRequirementDialog.on("close", $.proxy(function() {
				this.toDetachIds = [];
			}, this));
		}

	});
	return TestStepVerifiedRequirementsTable;
});