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
define(["jquery", "backbone", "underscore", "squash.configmanager", 'workspace.event-bus', "./popups", "app/util/StringUtil", "squash.translator"],
	function ($, Backbone, _, confman, eventBus, popups, StringUtil, translator) {

	var KeywordTestStepTablePanel = Backbone.View.extend({

		el: "#tab-tc-keyword-test-steps",

		initialize: function (options) {
			var self = this;

			$.squash.decorateButtons();
			this.settings = options.settings;
			var urls = this.makeTableUrls(this.settings);
			this.initKeywordTestStepTable(this.settings);

			this.actionWordInput = $('#action-word-input');
			this.keywordInput = $('#keyword-input');

			this.keyupTimer = undefined;
			if (this.settings.isAutocompleteActive) {
				this.actionWordInput.on('keydown', function (event) {
					var searchInput = $(event.currentTarget);
					var isAutoCompleteVisible = (searchInput.autocomplete('widget')).is(':visible');

					//launch the addition when Enter is pressed and released
					if (_.contains([13], event.which)) {
						if (isAutoCompleteVisible) {
							searchInput.autocomplete('close');
						} else{
							self.addKeywordTestStepFromButton();
						}
					}
				});
				this.actionWordInput.on('keyup', function (event) {
					// not perform autocomplete if arrows are pressed
					if (!_.contains([37, 38, 39, 40], event.which)) {
						var projectId = self.settings.projectId;
						var searchInput = $(event.currentTarget);
						self.performAutocomplete(searchInput, projectId);
					}
				});
				this.actionWordInput.autocomplete({
					select: function (event, ui) {}
				});
			}

			//the popups
			var conf = {};
			conf.permissions = {};
			conf.urls = {};
			conf.urls.testCaseStepsUrl = urls.deleteStep;
			conf.testCaseId = this.settings.testCaseId;
			conf.permissions.writable = this.settings.permissions.isWritable;
			conf.stepsTablePanel = this;
			popups.init(conf);

			// refresh the steps table when a parameter is renamed
			eventBus.onContextual('parameter.name.update', self.refresh);
		},

		events: {
			"click #add-keyword-test-step-btn": "addKeywordTestStepFromButton",
			"click #delete-all-steps-button": "deleteSelectedTestSteps"
		},

		initKeywordTestStepTable: function (settings) {
			var self = this,
				  testCaseId = settings.testCaseId,
				  table = $("#keyword-test-step-table"),
				  postActionWordFunction = self.postActionWordFunction(settings.testCaseUrl, table);

			table.squashTable(
				{
					bServerSide: true,
					aoColumnDefs: [{aTargets: [1], sWidth: '25%'}],
					aaData: settings.stepData,
					iDeferLoading: settings.stepData.length,
					sAjaxSource: settings.testCaseUrl + '/steps/keyword-test-step-table',
					fnDrawCallback: function() {
						var rows = table.fnGetNodes();
						rows.forEach(function(row) {
							var $row = $(row),
								  keywordCell = $row.find('td.step-keyword'),
									actionWordCell = $row.find('td.step-action-word');

							keywordCell.text(settings.keywordMap[keywordCell.text()]);
							if (settings.permissions.isWritable) {
								// keyword editable configuration
								var sconf = confman.getJeditableSelect();
								sconf.data = settings.keywordMap;
								var rowModel = table.fnGetData($row);
								var keywordUrl = settings.testCaseUrl + '/steps/' + rowModel['entity-id'] + '/keyword';
								keywordCell.editable(keywordUrl, sconf);

								// action-word editable configuration
								var edconf = confman.getStdJeditable();
								edconf.data = rowModel['step-action-word-unstyled'];
								actionWordCell.editable(postActionWordFunction, edconf);
							}
						});
					}
				}, {
					deleteButtons: {
						delegate: "#delete-keyword-test-step-dialog",
						tooltip: settings.language.deleteTitle
					}
				});
		},

		refresh: function () {
			$("#keyword-test-step-table").squashTable().refreshRestore();
		},

		cleanInputs: function () {
			$("#keyword-input").val($("#keyword-input option:first").val());
			this.actionWordInput.val('');
			$(".action-word-input-error").text('');
		},

		addKeywordTestStepFromButton: function () {
			$(".action-word-input-error").text('');
			var inputActionWord = this.actionWordInput.val();
			this.addKeywordTestStep(inputActionWord);
		},

		addKeywordTestStep: function (inputActionWord) {
			var self = this;
			if (this.isInputActionWordBlank(inputActionWord)) {
				return;
			}
			var inputKeyword = this.keywordInput.val();
			this.doAddKeywordTestStep(inputKeyword, inputActionWord)
				.done(function (testStepId) {
					self.afterKeywordTestStepAdd(testStepId, inputActionWord);
				});
		},

		isInputActionWordBlank: function (inputActionWord) {
			if (StringUtil.isBlank(inputActionWord)) {
				$('.action-word-input-error').text(translator.get("message.actionword.empty"));
				this.actionWordInput.val("");
				return true;
			} else {
				return false;
			}
		},

		doAddKeywordTestStep: function (keyword, actionWord) {
			var objectData = {
				keyword: keyword,
				actionWord: actionWord
			};
			return $.ajax({
				type: 'POST',
				url: "/squash/test-cases/" + this.settings.testCaseId + "/steps/add-keyword-test-step",
				contentType: 'application/json',
				data: JSON.stringify(objectData)
			});
		},

		afterKeywordTestStepAdd: function (testStepId) {
			this.refresh();
			this.cleanInputs();
			eventBus.trigger('testStepsTable.stepAdded');
		},

		deleteSelectedTestSteps: function () {
			$("#delete-keyword-test-step-dialog").formDialog('open');
		},

		makeTableUrls: function (conf) {
			var tcUrl = conf.testCaseUrl;
			var ctxUrl = conf.rootContext;

			return {
				deleteStep: tcUrl + "/steps",
			};
		},

		performAutocomplete: function (searchInput, projectId) {
			searchInput.autocomplete('close');
			searchInput.autocomplete('disable');

			var searchInputValue = searchInput.val();

			searchInput.autocomplete({
				delay : 500,
				source: function (request, response) {
					$.ajax({
						type: 'GET',
						url: '/squash/keyword-test-cases/autocomplete',
						data: {
							projectId: projectId,
							searchInput: searchInputValue
						},
						success: function (data) {
							response(data);
						},
						error: function () {
							alert("error handler!");
						}
					});
				},
				minLength: 1
			});
			searchInput.autocomplete('enable');
		},

		postActionWordFunction: function(baseUrl, table) {
			return function(value, editableSettings) {
				var td = this,
						row = td.parentNode,
						rowModel = table.fnGetData(row),
						actionWordCell = $(row).find('td.step-action-word'),
						actionWordUrl = baseUrl + '/steps/' + rowModel['entity-id'] + '/action-word';
				$.ajax({
					url: actionWordUrl,
					type: 'POST',
					data: {value: value}
				}).done(function() {
					var actionWordHtmlUrl = baseUrl + '/steps/' + rowModel['entity-id'] + '/action-word-html';
					$.ajax({
						url: actionWordHtmlUrl,
						type: 'GET'
					}).done(function(actionWordHtml) {
						actionWordCell.html(actionWordHtml);
					});
					var actionWordUnstyledUrl = baseUrl + '/steps/' + rowModel['entity-id'] + '/action-word-unstyled';
					$.ajax({
						url: actionWordUnstyledUrl,
						type: 'GET'
					}).done(function(actionWordUnstyled) {
						editableSettings.data = actionWordUnstyled;
					});
				});
			};
		}
	});
	return KeywordTestStepTablePanel;
});
