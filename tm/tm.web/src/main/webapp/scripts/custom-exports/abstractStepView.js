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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "squash.translator", "squash.dateutils"],
				function($, backbone, _, Handlebars, translator, dateutils) {
	"use strict";

	var abstractStepView = Backbone.View.extend({
		el : "#current-step",

		_initialize : function(data, wizrouter) {
			this.router = wizrouter;
			this.registerHandlebarHelper();
			this.steps = this.model.get("steps");
			var currStep = _.findWhere(this.steps, { name : data.name });
			this.next = currStep.nextStep;
			this.previous = currStep.prevStep;
			this.showViewTitle(currStep.viewTitle, currStep.stepNumber);
			this.initButtons(currStep.buttons);
			var missingStepNames = this.findMissingSteps(data, currStep.neededStep);
			this.missingStepNames = missingStepNames;

			if (this.previousStepsAreValid()){
				this.render(data, $(this.tmpl));
			} else {
				var missingSteps = _.chain(this.steps)
				.filter(function(step){
					return _.contains(missingStepNames, step.name);
				})
				.sortBy("stepNumber")
				.value();

				var model = {steps : missingSteps, totalStep : this.steps.length};
				this.render(model, $("#missing-steps-tpl"));
			}
		},

		registerHandlebarHelper: function() {
			var genericCufLabel = translator.get("label.customField");
			var linkcuf = translator.get("label.fromTestCase");
			Handlebars.registerHelper("cuf-label", function(prototype) {
				var html;
				if(this.id.indexOf("TEST_STEP")){
						html = prototype +" <span class='small txt-discreet'> (" + genericCufLabel + ")</span>";
				} else {
						html = prototype + "<span class='small txt-discreet'> (" + linkcuf + ")</span>";
				}
				return new Handlebars.SafeString(html);
			});

		},

		showViewTitle : function(title, stepNumber) {
			var text = "[" + translator.get("wizard.steps.label") +" " + stepNumber + "/" + this.steps.length + "] " + translator.get(title);
			$("#step-title").text(text);
		},

		findMissingSteps: function(data, neededSteps) {
			var self = this;
			return _.filter(neededSteps, function(step) {
				var validationParams = _.chain(self.model.get("validation"))
					.find(function(val) { return val.name === step; })
					.result("validationParam")
					.value();

				return _.isEmpty(_.result(data.attributes, validationParams));
			});
		},

		previousStepsAreValid: function() {
			return _.isEmpty(this.missingStepNames);
		},

		initButtons: function (buttons) {

			var allButtons = ["previous", "next", "save", "generate"];

			_.each(buttons, function(button) {
				var select = $("#" + button);
				select.show();
			});

			_.chain(allButtons).difference(buttons).each(function(button) {
				var select = $("#" + button);
				select.hide();
			}
			);

		},

		render : function(data, tmpl) {
			var src = tmpl.html();
			this.template = Handlebars.compile(src);
			this.$el.append(this.template(data));
			return this;
		},

		destroy_view : function() {
			this.undelegateEvents();
			this.$el.removeData().unbind();
			this.remove();
			Backbone.View.prototype.remove.call(this);
		},

		navigateNext : function() {
			this.updateModel();
			this.router.navigate(this.next, {
				trigger : true
			});

		},

		updateModel : function() {
			// do in superclass
		},

		navigatePrevious : function() {
			this.router.navigate(this.previous, {
				trigger : true
			});
		},

		/**
		 * Check if we are updating an existing export.
		 */
		isUpdatingAnExistingExport : function () {
			return this.model.get("customExportDef") !== null;
		}

		// - Check if previous steps are valid

		// - Format date

	});

	abstractStepView.extend = function(child) {
		var view = Backbone.View.extend.apply(this, arguments);
		view.prototype.events = _.extend({}, this.prototype.events, child.events);
		return view;
	};

	return abstractStepView;

});
