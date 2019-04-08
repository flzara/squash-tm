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
				buttons: ["previous", "next"],
				clickable: true
			},
			{
				name : "name",
				prevStep : "attributes",
				nextStep : "",
				viewTitle : "custom-export.wizard.step.name.title",
				stepNumber : 3,
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
				validationParam: "selectedAttributes"
			},
			{
				name: "name",
				validationParam: "name"
			}];

		var entityMap = {
			CAMPAIGN: {
				iconClass: "icon-chart-campaign",
				attributes: {
					CAMPAIGN_LABEL: 'label.Label',
					CAMPAIGN_ID: 'label.id',
					CAMPAIGN_REFERENCE: 'label.Reference',
					CAMPAIGN_DESCRIPTION: 'label.Description',
					CAMPAIGN_STATE: 'label.State',
					CAMPAIGN_PROGRESS_STATUS: 'campaign.progress_status.label',
					CAMPAIGN_MILESTONE: 'campaign.progress_status.label',
					CAMPAIGN_SCHEDULED_START: 'chart.column.CAMPAIGN_SCHED_START',
					CAMPAIGN_SCHEDULED_END: 'chart.column.CAMPAIGN_SCHED_END',
					CAMPAIGN_ACTUAL_START: 'chart.column.CAMPAIGN_ACTUAL_START',
					CAMPAIGN_ACTUAL_END: 'chart.column.CAMPAIGN_ACTUAL_END'
				}
			},
			ITERATION: {
				iconClass: "icon-chart-iteration",
				attributes: {
					ITERATION_LABEL: 'label.Label',
					ITERATION_ID: 'label.id',
					ITERATION_REFERENCE: 'label.Reference',
					ITERATION_DESCRIPTION: 'label.Description',
					ITERATION_STATE: 'label.State',
					ITERATION_SCHEDULED_START: 'chart.column.CAMPAIGN_SCHED_START',
					ITERATION_SCHEDULED_END: 'chart.column.CAMPAIGN_SCHED_END',
					ITERATION_ACTUAL_START: 'chart.column.CAMPAIGN_ACTUAL_START',
					ITERATION_ACTUAL_END: 'chart.column.CAMPAIGN_ACTUAL_END'
				}
			},
			TEST_SUITE: {
				iconClass: "icon-chart-test-suite",
				attributes: {
					TEST_SUITE_LABEL: 'label.Label',
					TEST_SUITE_ID: 'label.id',
					TEST_SUITE_DESCRIPTION: 'label.Description',
					TEST_SUITE_EXECUTION_STATUS: 'chart.column.EXECUTION_STATUS',
					TEST_SUITE_PROGRESS_STATUS: 'test-suite.progress_status.label'
				}
			},
			TEST_CASE: {
				iconClass: "icon-chart-test-case",
				attributes: {
					TEST_CASE_PROJECT: 'label.project',
					TEST_CASE_MILESTONE: 'label.Milestone',
					TEST_CASE_LABEL: 'label.Label',
					TEST_CASE_ID: 'label.id',
					TEST_CASE_REFERENCE: 'label.Reference',
					TEST_CASE_DESCRIPTION: 'label.Description',
					TEST_CASE_STATUS: 'label.Status',
					TEST_CASE_IMPORTANCE: 'label.Importance',
					TEST_CASE_NATURE: 'chart.column.TEST_CASE_NATURE',
					TEST_CASE_TYPE: 'label.Type',
					TEST_CASE_DATASET: 'label.Dataset',
					TEST_CASE_PREREQUISITE: 'generics.prerequisite.title',
					TEST_CASE_LINKED_REQUIREMENTS_IDS: 'custom-export.column.TEST_CASE.LINKED_REQUIREMENTS_IDS'
				}
			},
			EXECUTION: {
				iconClass: "icon-chart-execution",
				attributes: {
					EXECUTION_EXECUTION_MODE: 'label.ExecutionMode',
					EXECUTION_STATUS: 'label.Status',
					EXECUTION_SUCCESS_RATE: 'shortLabel.SuccessRate',
					EXECUTION_USER: 'label.User',
					EXECUTION_EXECUTION_DATE: 'iteration.executions.table.column-header.execution-date.label',
					EXECUTION_COMMENT: 'executions.steps.table.column-header.comment.label'
				}
			},
			EXECUTION_STEP: {
				iconClass: "icon-chart-execution-step",
				attributes: {
					EXECUTION_STEP_STEP_NUMBER: 'custom-export.column.EXECUTION_STEP.EXECUTION_STEP_NUMBER',
					EXECUTION_STEP_ACTION: 'label.action',
					EXECUTION_STEP_RESULT: 'custom-export.column.EXECUTION_STEP.RESULT',
					EXECUTION_STEP_STATUS: 'label.Status',
					EXECUTION_STEP_USER: 'label.User',
					EXECUTION_STEP_EXECUTION_DATE: 'iteration.executions.table.column-header.execution-date.label',
					EXECUTION_STEP_COMMENT: 'executions.steps.table.column-header.comment.label',
					EXECUTION_STEP_LINKED_REQUIREMENTS_IDS: 'custom-export.column.EXECUTION_STEP.STEP_LINKED_REQUIREMENTS_IDS'
				}
			},
			ISSUE: {
				iconClass: "icon-chart-issue",
				attributes: {
					ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES: 'custom-export.column.ISSUE.ALL_LINKED_ISSUES',
					ISSUE_EXECUTION_ISSUES: 'custom-export.column.ISSUE.STEP_LINKED_ISSUES'
				}
			}
		};

		var wizardView = Backbone.View.extend({

			el : "#wizard",

			initialize : function(options) {
				this.model = options.model;
				this.model.set({
					steps: steps,
					validation : validation,
					entityMap: entityMap,
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
