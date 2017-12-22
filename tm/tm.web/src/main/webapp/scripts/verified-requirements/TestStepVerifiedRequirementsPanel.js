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
define(["jquery", "backbone", "underscore", "app/util/StringUtil",
	"./TestStepVerifiedRequirementsTable",
	"jquery.squash", "jqueryui",
	"jquery.squash.togglepanel", "squashtable",
	"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
	"jquery.squash.confirmdialog","jquery.squash.buttonmenu"], function ($, Backbone, _, StringUtil,
																						TestStepVerifiedRequirementsTable) {
	var VRBS = squashtm.app.verifiedRequirementsBlocSettings;
	var TestStepVerifiedRequirementsPanel = Backbone.View.extend({

		el: "#verified-requirements-bloc-frag",

		initialize: function () {
			VRBS = squashtm.app.verifiedRequirementsBlocSettings;
			this.table = new TestStepVerifiedRequirementsTable();
			this.configureButtons.call(this);
			this.showSelectedRequirement.call(this);
		},

		events: {},

		configureButtons: function () {
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
			this.$("#remove-associated-requirements-button").buttonmenu();
			this.$("#remove-associated-requirements-button").on('click', function () {
				// check if current test step is associated to a requirement, if so, user can remove this requirement from test step.
				var nbAssociatedReqToStep = $(".ui-icon-link-dark-e-w").length;
				$("#remove-verified-requirements-from-step-button").toggleClass("ui-state-disabled", nbAssociatedReqToStep === 0);
				});
			this.$("#remove-verified-requirements-button").on('click',
				function () {
					self.table.removeSelectedRequirements();
				});
			this.$("#remove-verified-requirements-from-step-button").on(
				'click', function () {
					self.table.detachSelectedRequirements();
				});
			this.$("#add-verified-requirements-button").on('click',
				self.goToRequirementManager);
		},

		goToRequirementManager: function () {
			document.location.href = VRBS.stepUrl + "/manager";
		},

		showSelectedRequirement: function () {
			var self = this;
			self.table.$el.on('click', 'tbody>tr>td.select-handle', function () {
				var rowSelected = $(this).closest('tr');
				var data = self.table.$el.fnGetData(rowSelected);
				$("#requirement-version-id")[0].innerHTML = '[ID =' + data["entity-id"] + ']';
				$("#requirement-version-versionNumber")[0].innerHTML = data["versionNumber"];
				$("#requirement-version-status")[0].innerHTML = data["status-level"] + '-' + data["status"];
				$("#requirement-version-criticality")[0].innerHTML = data["criticality-level"] + '-' + data["criticality"];
				$("#requirement-version-category")[0].innerHTML = data["category"];
				$("#requirement-version-category-icon")[0].className = 'sq-icon sq-icon-' + data["category-icon"];
				$("#requirement-version-description")[0].innerHTML = data["description"];
				localStorage.setItem("selectedRow", data["entity-index"] - 1);

				if (!rowSelected[0].classList.contains('ui-state-row-selected')){
					$("#requirement-version-id").empty();
					$("#requirement-version-versionNumber").empty();
					$("#requirement-version-status").empty();
					$("#requirement-version-criticality").empty();
					$("#requirement-version-category").empty();
					$("#requirement-version-category-icon")[0].className = '';
					$("#requirement-version-description").empty();
					localStorage.removeItem("selectedRow");
				}
			});

			// When user toggles between adjacent steps, the description of attached
			// requirement will be displayed by default, in case there is no attached requirement,
			// last selected requirement will be displayed

			var tableData = self.table.$el.fnGetData();

			if(!_.isEmpty(tableData)) {

				var targetRequirementRows = $('.ui-icon-link-dark-e-w').closest('tr');
				var targetSelectHandle = targetRequirementRows.find('.select-handle');
				var previousSelectedRow = localStorage.getItem("selectedRow");
				if (previousSelectedRow != null) {
					var target = $('tbody>tr').eq(previousSelectedRow);
					var isLinked = target.find('.link-checkbox')[0].children[0].classList.contains('ui-icon-link-dark-e-w');
					if (isLinked || targetSelectHandle.length === 0) {
						targetSelectHandle = target.find('.select-handle');
					}
				}
				if(targetSelectHandle.length !== 0){
					targetSelectHandle[0].click();
				}
			}


		}
	});
	return TestStepVerifiedRequirementsPanel;
});
