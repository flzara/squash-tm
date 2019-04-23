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
define(["jquery", "backbone", "squash.translator", "./perimeterStepView", "./attributesStepView", "./nameStepView", "./sideView", "jquery.squash.togglepanel", "jquery.squash.confirmdialog"],
	function($, Backbone, translator, PerimeterStepView, AttributesStepView, NameStepView, SideView) {

		"use strict";

		var steps = [
			{
				name: "perimeter",
				prevStep: "",
				nextStep: "attributes",
				viewTitle: "custom-export.wizard.step.perimeter.title",
				stepNumber: 1,
				buttons: ["next"],
				clickable: true
			},
			{
				name: "attributes",
				prevStep: "perimeter",
				nextStep: "name",
				viewTitle: "custom-export.wizard.step.attributes.title",
				stepNumber: 2,
				neededStep: ["perimeter"],
				buttons: ["previous", "next"],
				clickable: true
			},
			{
				name : "name",
				prevStep : "attributes",
				nextStep : "",
				viewTitle : "custom-export.wizard.step.name.title",
				stepNumber : 3,
				neededStep: ["perimeter", "attributes"],
				buttons : ["previous", "save"],
				clickable: true
		}
		];

		var validation = [
			{
			name : "perimeter",
			validationParam : "scope"
			},
			{
				name: "attributes",
				validationParam: "selectedAllAttributes"
			},
			{
				name: "name",
				validationParam: "name"
			}];

		var wizardView = Backbone.View.extend({

			el : "#wizard",

			initialize : function(options) {
				this.model = options.model;
				this.model.set({
					steps: steps,
					validation : validation
				});
				// - load i18n keys
			},

			events : {
				"click #next" 		: "navigateNext",
				"click #previous" : "navigatePrevious",
				"click #generate" : "generate",
				"click #save" 		: "save"
			},

			navigateNext : function (){
				this.currentView.navigateNext();
			},

			navigatePrevious : function (){
				this.currentView.navigatePrevious();
			},

			generate : function (){
				this.currentView.generate();
			},

			save : function() {
				this.currentView.save();
			},

			// - flatten()

			// - addPrefix()

			showSideView : function(){
				this.resetSideView();
				this.currentSideView = new SideView(this.model);
			},

			showNewStepView : function (View, wizrouter) {
				if (this.currentView !== undefined) {
					this.currentView.updateModel();
				}
				this.resetView();
				this.currentView = new View(this.model, wizrouter);
				this.showSideView();
			},

			showPerimeterStep: function(wizrouter) {
				this.showNewStepView(PerimeterStepView, wizrouter);
			},
			showAttributesStep: function(wizrouter) {
				this.showNewStepView(AttributesStepView, wizrouter);
			},
			showNameStep : function(wizrouter) {
				this.showNewStepView(NameStepView, wizrouter);
			},

			resetView : function() {
				if (this.currentView !== undefined) {
					this.currentView.destroy_view();
					$("#current-step-container").html('<span id="current-step" />');
				}
			},

			resetSideView : function() {

				if (this.currentSideView !== undefined) {
					this.currentSideView.destroy_view();
					$("#current-side-view-container").html('<span style="display : table; height:100%" id="side-view" />');
				}
			}

		});

		return wizardView;

	});
