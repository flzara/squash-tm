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
				CAMPAIGN_MILESTONE: 'label.Milestone',
				CAMPAIGN_SCHEDULED_START: 'dialog.label.campaign.scheduled_start.label',
				CAMPAIGN_SCHEDULED_END: 'dialog.label.campaign.scheduled_end.label',
				CAMPAIGN_ACTUAL_START: 'dialog.label.campaign.actual_start.label',
				CAMPAIGN_ACTUAL_END: 'dialog.label.campaign.actual_end.label'
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
				ITERATION_SCHEDULED_START: 'dialog.label.campaign.scheduled_start.label',
				ITERATION_SCHEDULED_END: 'dialog.label.campaign.scheduled_end.label',
				ITERATION_ACTUAL_START: 'dialog.label.campaign.actual_start.label',
				ITERATION_ACTUAL_END: 'dialog.label.campaign.actual_end.label'
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
				TEST_CASE_LINKED_REQUIREMENTS_NUMBER: 'custom-export.column.LINKED_REQUIREMENTS_COUNT',
				TEST_CASE_LINKED_REQUIREMENTS_IDS: 'custom-export.column.LINKED_REQUIREMENTS_IDS'
			}
		},
		EXECUTION: {
			iconClass: "icon-chart-execution",
			attributes: {
				EXECUTION_ID: 'label.id',
				EXECUTION_EXECUTION_MODE: 'label.ExecutionMode',
				EXECUTION_STATUS: 'label.Status',
				EXECUTION_SUCCESS_RATE: 'iteration.executions.table.column-header.succesPercent.label',
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
				EXECUTION_STEP_LINKED_REQUIREMENTS_NUMBER: 'custom-export.column.LINKED_REQUIREMENTS_COUNT',
				EXECUTION_STEP_LINKED_REQUIREMENTS_IDS: 'custom-export.column.EXECUTION_STEP_LINKED_REQUIREMENTS_IDS',
				//TEST_STEP_CUF: ''

				//ajouter colonne TEST_STEP CUF + label

			}
		},
		ISSUE: {
			iconClass: "icon-chart-issue",
			attributes: {
				ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_NUMBER: 'custom-export.wizard.attributes.ISSUE.ALL_LINKED_ISSUES_COUNT',
				ISSUE_EXECUTION_AND_EXECUTION_STEP_ISSUES_IDS: 'custom-export.wizard.attributes.ISSUE.ALL_LINKED_ISSUES',
				ISSUE_EXECUTION_STEP_ISSUES_NUMBER: 'custom-export.wizard.attributes.ISSUE.STEP_LINKED_ISSUES_COUNT',
				ISSUE_EXECUTION_STEP_ISSUES_IDS: 'custom-export.wizard.attributes.ISSUE.STEP_LINKED_ISSUES'
			}
		}
	};

	return Backbone.Model.extend({

		initialize : function(data) {
			var self = this;

			var customExportDef = data.customExportDef;

			this.set({ entityMap: entityMap });
			this.set({ entityWithCuf: ["CAMPAIGN", "ITERATION", "TEST_SUITE", "TEST_CASE", "EXECUTION", "EXECUTION_STEP", "TEST_STEP"]});

			this.set({ parentId: squashtm.customExport.parentId });

			// Reload customExportDef into this model if it is a modification of an existing CustomExport
			if (customExportDef) {
				// put the name of the scope campaign in the scope attribute
				customExportDef.scope[0].name = customExportDef.scopeEntityName;
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
				this.set({ allSelectedAttributes: loadedAllAttributes });
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
			// Deduce from standard attributes
			var selectedStandardAttributes = this.get("selectedAttributes");
			// Filter entityMap keeping only the entities which have at least a selected attribute
			var filteredMap = _.pick(entityMap, function(value) {
				var allLabelKeys = _.keys(value.attributes);
				var selectedLabels = _.intersection(selectedStandardAttributes, allLabelKeys);
				return selectedLabels.length > 0;
			});
			var selectedEntitiesFromStandard = _.keys(filteredMap);

			// Deduce from cuf attributes
			var selectedCufAttributes = this.get("selectedCufAttributes");
			var selectedEntitiesFromCufs =
				_.chain(selectedCufAttributes)
					.map(function(attr) {
						// Deduce entity name from the computed cuf name
						return attr.split('-')[0].split('_')[0];
					}).uniq()
					.value();
			return _.union(selectedEntitiesFromStandard, selectedEntitiesFromCufs);
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
			var allSelectedAttributes = this.get("allSelectedAttributes");

			return _.map(allSelectedAttributes, function(attr) {
				// Split ColumnLabel and CufId (if it exists)
				var splitAttr = attr.split('-');
				return {
					label: splitAttr[0],
					cufId: splitAttr[1]
				};
			});
		}

	});

});
