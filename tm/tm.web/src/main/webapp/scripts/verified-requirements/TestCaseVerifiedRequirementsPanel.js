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
		"./TestCaseVerifiedRequirementsTable", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil,
		VerifiedRequirementsTable) {
	var VRBS = squashtm.app.verifiedRequirementsBlocSettings;
	var VerifiedRequirementsPanel = Backbone.View.extend({

		el : "#verified-requirements-bloc-frag",

		initialize : function() {
			VRBS = squashtm.app.verifiedRequirementsBlocSettings;
			this.table = new VerifiedRequirementsTable();
			this.configureButtons.call(this);
			
		},

		events : {},

		configureButtons : function() {
			var self = this;
			// ===============toogle buttons=================
			// this line below is here because toggle panel
			// buttons cannot be bound with the 'events'
			// property of Backbone.View.
			// my guess is that the event is bound to the button
			// before it is moved from it's "span.not-displayed"
			// to the toggle panel header.
			// TODO change our way to make toggle panels buttons
			// =============/toogle buttons===================
			this.$("#remove-verified-requirements-button").on('click',
					function() {
						self.table.removeSelectedRequirements();
					});
			this.$("#add-verified-requirements-button").on('click',
					self.goToRequirementManager);
		},

		goToRequirementManager : function() {
			document.location.href = VRBS.url + "/manager";
		}

	});
	return VerifiedRequirementsPanel;
});