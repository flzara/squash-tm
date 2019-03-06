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
define(["jquery", "backbone", "squash.translator", "./nameStepView", "jquery.squash.togglepanel", "jquery.squash.confirmdialog"],
				function($, Backbone, translator, NameStepView) {

	"use strict";

	var validation = [{
			name : "name",
			validationParam : "name"
		}];

	var steps = [{
		name : "name",
		prevStep : "",
		nextStep : "",
		viewTitle : "custom-export.wizard.step.name.title",
		stepNumber : 1,
		buttons : ["save"]
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
			"click #next" : "navigateNext",
			"click #previous" : "navigatePrevious",
		  "click #generate" : "generate",
			"click #save" : "save"
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

		// - showSideView()

		showNewStepView : function (View, wizrouter) {
			if (this.currentView !== undefined) {
				this.currentView.updateModel();
			}
			this.resetView();
			this.currentView = new View(this.model, wizrouter);
			// - show side view
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

		// - resetSideView()

	});

	return wizardView;

});
