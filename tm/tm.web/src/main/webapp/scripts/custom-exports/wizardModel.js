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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil"], function($, Backbone, _,stringUtil) {
	"use strict";

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
				CAMPAIGN_MILESTONE: 'label.Milestones',
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
				TEST_CASE_MILESTONE: 'label.Milestones',
				TEST_CASE_LABEL: 'label.Label',
				TEST_CASE_ID: 'label.id',
				TEST_CASE_REFERENCE: 'label.Reference',
				TEST_CASE_DESCRIPTION: 'label.Description',
				TEST_CASE_STATUS: 'label.Status',
				TEST_CASE_IMPORTANCE: 'label.Importance',
				TEST_CASE_NATURE: 'chart.column.TEST_CASE_NATURE',
				TEST_CASE_TYPE: 'label.Type',
				TEST_CASE_DATASET: 'label.dataset',
				TEST_CASE_PREREQUISITE: 'generics.prerequisite.title',
				TEST_CASE_LINKED_REQUIREMENTS_IDS: 'custom-export.column.LINKED_REQUIREMENTS_IDS'
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
				EXECUTION_STEP_LINKED_REQUIREMENTS_IDS: 'custom-export.column.LINKED_REQUIREMENTS_IDS'
			}
		},
		ISSUE: {
			iconClass: "icon-chart-issue",
			attributes: {
				ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES: 'custom-export.wizard.attributes.ISSUE.ALL_LINKED_ISSUES',
				ISSUE_EXECUTION_ISSUES: 'custom-export.wizard.attributes.ISSUE.STEP_LINKED_ISSUES'
			}
		}
	};

	return Backbone.Model.extend({

		initialize : function(data) {
			var self = this;

			var customExportDef = data.customExportDef;

			this.set({ entityMap: entityMap });
			this.set({ entityWithCuf: ["CAMPAIGN", "ITERATION", "TEST_SUITE", "TEST_CASE", "EXECUTION", "EXECUTION_STEP"]});

			this.set({ parentId: squashtm.customExport.parentId });

			// Reload customExportDef into this model if it is a modification of an existing CustomExport
			if (customExportDef) {
				// put the name of the scope campaign in the scope attribute
				customExportDef.scope[0].name = customExportDef.scopeCampaignName;
				this.set({ scope: customExportDef.scope });
				this.set({ selectedTreeNodes: [{ id: "Campaign-" + customExportDef.scope[0].id }] });

				var loadedStandardAttributes = [];
				var loadedCufAttributes = [];
				var loadedAllAttributes = [];

				// Iterate on the loaded columns and fill the different Arrays
				_.each(customExportDef.columns, function(column) {
					if(column.cufId == null) {
						loadedStandardAttributes.push(column.label);
						loadedAllAttributes.push(column.label);
					} else {
						var computedCufLabel = column.label + '-' + column.cufId;
						loadedCufAttributes.push(computedCufLabel);
						loadedAllAttributes.push(computedCufLabel);
					}
				});

				this.set({ selectedAttributes: loadedStandardAttributes });
				this.set({ selectedCufAttributes: loadedCufAttributes });
				this.set({ selectedAllAttributes: loadedAllAttributes });
				this.set({ selectedEntities: this.deduceSelectedEntities() });

				var cufMap = squashtm.customExport.availableCustomFields;
				this.set({ availableCustomFields: this.computeAvailableCustomFields(cufMap) });

				this.set({ name: customExportDef.name });
			}
		},

		computeAvailableCustomFields: function(cufMap) {
			return _.chain(cufMap).pick(this.get('entityWithCuf')).mapObject(function(cufList) {
				return _.map(cufList, function(cufBinding) {
						return {
									id: cufBinding.boundEntity.enumName + "_CUF-" + cufBinding.customField.id,
									label: cufBinding.customField.label,
									code: cufBinding.customField.code,
									type: cufBinding.customField.inputType.friendlyName
						};
				});
			}).value();
		},

		deduceSelectedEntities: function() {
			var selectedAttributes = this.get("selectedAttributes");
			var filteredMap = _.pick(entityMap, function(value) {
				var labelKeys = _.keys(value.attributes);
				var intersection = _.intersection(selectedAttributes, labelKeys);
				return intersection.length > 0;
			});
			var selectedEntities = _.keys(filteredMap);
			return selectedEntities;
		},

		toJson: function() {
			var self = this;
			return JSON.stringify({
				scope: _.map(this.get("scope"), function(entity) {
					return { id: entity.id, type: entity.type };
				}),
				columns: self.extractColumns(),
				name: this.get("name")

			});
		},

		extractColumns: function() {
			var selectedAllAttributes = this.get("selectedAllAttributes");

			return _.map(selectedAllAttributes, function(attr) {
				// Split ColumnLabel and CufId (if it exists)
				var splitAttr = attr.split('-');
				return {
					label: splitAttr[0],
					cufId: splitAttr[1]
				}
			});
		}

	});

});
