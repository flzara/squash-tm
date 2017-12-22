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
define([ "jquery", "backbone", "workspace.routing", "squash.translator", "./perimeterStepView", "./filterStepView", "./typeStepView", "./axisStepView", "./previewStepView", "./sideView", "./attributeStepView", "jquery.squash.togglepanel", "jquery.squash.confirmdialog" ], function($, Backbone,
		router, translator, PerimeterStepView, FilterStepView, TypeStepView , AxisStepView, PreviewStepView, SideView, AttributeStepView) {

	"use strict";

	

	var validation =
		[{
			name : "perimeter",
			validationParam : "scope"
		},{
			name :"attributes",
			validationParam : "selectedAttributes"
		},{
			name :"axis",
			validationParam : "operations"
		},{
			name :"filter",
			validationParam : "filtered"
		},{
			name :"type",
			validationParam : "chartData"
		}
		];
	var steps = [{
		name : "perimeter",
		prevStep : "",
		nextStep : "attributes",
		viewTitle : "chart.wizard.creation.step.perimeter",
		stepNumber : 1,
		buttons : ["next"],
		clickable : true
	}, {
		name : "attributes",
		prevStep : "perimeter",
		nextStep : "filter",
		viewTitle : "chart.wizard.creation.step.attributes",
		stepNumber : 2,
		neededStep : ["perimeter"],
		buttons : ["previous", "next"],
		clickable : true
	},{
		name : "filter",
		prevStep  : "attributes",
	    nextStep : "axis",
		viewTitle : "chart.wizard.creation.step.filter",
		stepNumber : 3,
		neededStep : ["perimeter", "attributes"],
		buttons : ["previous", "next"],
		clickable : true
	}, {
		name : "axis",
		prevStep : "filter",
		nextStep : "type",
		viewTitle : "chart.wizard.creation.step.axis",
		stepNumber : 4,
		neededStep : ["perimeter", "attributes"],
		buttons : ["previous", "next"],
		clickable : true
	},{
		name : "type",
		prevStep : "axis",
		nextStep : "preview",
		viewTitle : "chart.wizard.creation.step.type",
		stepNumber : 5,
		neededStep : ["perimeter", "attributes", "axis"],
		buttons : ["previous", "generate"],
		clickable : true

	},{
		name : "preview",
		prevStep : "type",
		nextStep : "",
		viewTitle : "chart.wizard.creation.step.preview",
		stepNumber : 6,
		neededStep : ["perimeter", "attributes", "axis"],
		buttons : ["previous", "save"],
		clickable : false
	}
	];


	var wizardView = Backbone.View.extend({
		el : "#wizard",
		initialize : function(options) {

			this.model = options.model;
			this.model.set({
				steps: steps,
			    perimSelect :[{text:"label.requirements" , name:"REQUIREMENT"}, {text:"label.testCase" , name:"TEST_CASE"}, {text:"label.campaigns" , name:"CAMPAIGN"}],
				validation : validation
			});
			this.additionnalI18nKeys = [
				"label.customField","squashtm.dateformatShort"
			];
			this.loadI18n();
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

		loadI18n : function (){

			var chartTypes = this.addPrefix(this.model.get("chartTypes"), "chartType.");
      		var chartTypeExplanation = this.addPrefix(this.model.get("chartTypes"), "chartType.explanation.");
			var entityTypes = this.addPrefix(_.keys(this.model.get("entityTypes")), "entityType.");
			var operation = this.addPrefix(_.uniq(this.flatten(this.model.get("dataTypes"))), "operation.");
			var columnsWithoutCuf = _.map(this.model.get("columnPrototypes"),function (protosForEntityType) {
				return _.filter(protosForEntityType, function (proto) {
				return proto.columnType != "CUF";
				});
			});
			var columns = this.addPrefix(_.pluck(this.flatten(columnsWithoutCuf), "label") ,"column.");

			var keys = chartTypes.concat(chartTypeExplanation,entityTypes, operation, columns);

			var result = this.addPrefix(keys, "chart.");

			result = result.concat(this.additionnalI18nKeys);

			translator.load(result);

		},

		flatten : function (col) {
			return _.reduce(col, function(memo, val) {return memo.concat(val);}, []);
		},

		addPrefix : function(col, prefix){
			return _.map(col, function (obj){
				return prefix + obj;
			});

		},

		showSideView : function(){
			this.resetSideView();
			this.currentSideView = new SideView(this.model);
		},

		showNewStepView : function (View, wizrouter){
			if (this.currentView !== undefined) {
			this.currentView.updateModel();
			}

			this.resetView();
			this.currentView = new View(this.model, wizrouter);
			this.showSideView();
			

		},

		showPerimeterStep : function(wizrouter) {
			this.showNewStepView(PerimeterStepView, wizrouter);
		},

		showFilterStep : function(wizrouter) {
			this.showNewStepView(FilterStepView, wizrouter);
		},

		showTypeStep : function(wizrouter) {
			this.showNewStepView(TypeStepView, wizrouter);
		},

		showAxisStep : function(wizrouter) {
			this.showNewStepView(AxisStepView, wizrouter);
		},

		showPreviewStep :  function(wizrouter) {
			this.showNewStepView(PreviewStepView, wizrouter);
		},

		showAttributesStep : function(wizrouter){
			this.showNewStepView(AttributeStepView, wizrouter);
		},
		resetView : function() {
			console.log(this.model);
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
