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
		"./VerifiedRequirementsTable","squash.translator", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil,
		VerifiedRequirementsTable, translator) {
	var VRTS = squashtm.app.verifiedRequirementsTableSettings;
	var TestCaseDirectVerifiedRequirementsTable = VerifiedRequirementsTable
			.extend({

				initialize : function(options) {
					VerifiedRequirementsTable.prototype.initialize.apply(this);
				},

				events : {},

				squashSettings : function(self) {

					return {
						buttons : [ {
							tooltip : translator.get('label.DisassociateRequirement'),
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
				}

			});
	return TestCaseDirectVerifiedRequirementsTable;
});
