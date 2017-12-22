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
         "./VerifiedRequirementsTable", "app/ws/squashtm.notification",
         "squash.translator", "jquery.squash",
		"jqueryui", "jquery.squash.togglepanel", "squashtable",
		"jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil,
		VerifiedRequirementsTable, notification, translator) {
	var VRTS = squashtm.app.verifiedRequirementsTableSettings;
	var TestCaseVerifiedRequirementsTable = VerifiedRequirementsTable.extend({

		initialize : function(options) {
			VerifiedRequirementsTable.prototype.initialize.apply(this);
		},

		events : {},

		squashSettings : function(self) {

			return {
				buttons : [ {
					tooltip : translator.get('label.DisassociateRequirement'),
					condition : function(row, data) {
						var verified = data.directlyVerified == "false" ? false : data.directlyVerified;
						return verified && VRTS.linkable;
					},
					tdSelector : "td.unbind-button",
					uiIcon : "ui-icon-minus",
					jquery : true,
					onClick : self.removeRowRequirementVersion
				} ],
				tooltips : [{
				tdSelector: "td.verif-req-description",
				value : function (row, data) {return data["short-description"];}
			}]
			};
		},

		_confirmRemoveRequirements : function(rows) {
			var self = this;
			this.toDeleteIds = [];
			var rvIds = $(rows).collect(function(row) {
				return self.table.getODataId(row);
			});
			var hasRequirement = (rvIds.length > 0);
			if (hasRequirement) {
				var indirects = $(rows).not(function(index, row) {
					var data = self.table.fnGetData(row);
					return data.directlyVerified == "false" ? false : data.directlyVerified;
				});
				if (indirects.length > 0) {
					notification.showWarning(translator.get('verified-requirements.table.indirectverifiedrequirements.removalattemptsforbidden.label'));
				} else {

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
				}
			} else {
				notification.showError(translator.get('message.EmptyTableSelection'));
			}

		},

		_requirementsTableRowCallback : function(row, data, displayIndex) {
			var verified = data.directlyVerified == "false" ? false : data.directlyVerified;
			if (VRTS.linkable && verified && data.status != "OBSOLETE") {
				this.addSelectEditableToVersionNumber(row, data);
			}
			this.discriminateDirectVerifications(row, data, displayIndex);
			this.addLinkToTestStep(row, data, displayIndex);
			return row;
		},

		// =====================================================

		discriminateDirectVerifications : function(row, data, displayIndex) {
			var verified = data.directlyVerified == "false" ? false : data.directlyVerified;
			if (!verified) {
				$(row).addClass("requirement-indirect-verification");
				$('td.delete-button', row).html(''); // remove the delete button
			} else {
				$(row).addClass("requirement-direct-verification");
			}

		},

		addLinkToTestStep : function(row, data, displayIndex) {
			var spans = $("span.verifyingStep", row);
			var span = $(spans[0]);
			if (span) {
				var stepIndex = span.text();
				var stepId = span.attr("dataId");
				var link = $("<a/>", {
					'href' : squashtm.app.contextRoot + "/test-steps/" + stepId,
					'target':'blank'
				});
				link.text(stepIndex);
				var cell = span.parent("td");
				cell.html(link);
			}
		}

	});
	return TestCaseVerifiedRequirementsTable;
});
