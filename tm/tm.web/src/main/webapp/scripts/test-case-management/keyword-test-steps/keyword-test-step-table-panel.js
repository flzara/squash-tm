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
define(["jquery", "backbone", "underscore", "squash.basicwidgets", "squash.configmanager", 'workspace.event-bus', "./popups", "app/util/StringUtil", "squash.translator", "squashtable"],
	function ($, Backbone, _, basic, confman, eventBus, popups, StringUtil, translator) {

	var KeywordTestStepTablePanel = Backbone.View.extend({

		el: "#tab-tc-keyword-test-steps",

		initialize: function (options) {
			var self = this;

			$.squash.decorateButtons();
			this.settings = options.settings;
			var urls = this.makeTableUrls(this.settings);
			this.initKeywordTestStepTable(this.settings);
			if (this.settings.permissions.isWritable) {
				this.initTableStyle(this.settings);
			}
			this.initTableDetails();
			this.basicInit();
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
						self.correctAutocompleteWidth(searchInput);
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

		basicInit : function() {
			basic.init();

		},

		events: {
			"click #add-keyword-test-step-btn": "addKeywordTestStepFromButton",
			"click #delete-all-steps-button": "deleteSelectedTestSteps",
			"click #preview-generated-script-button": "generateScript"
		},

		initKeywordTestStepTable: function (settings) {
			var self = this,
				  table = $("#keyword-test-step-table"),
				  postModifyActionWordFunction = self.postModifyActionWordFunction(settings.testCaseUrl, table),
					dragClass = '', deleteClass = '', squashSettings = {};

			if (settings.permissions.isWritable) {
				dragClass = 'drag-handle';
				deleteClass = 'delete-button';
				squashSettings = {
					dataKeys: {
						entityIndex: "step-index"
					},
					deleteButtons: {
						delegate: "#delete-keyword-test-step-dialog",
						tooltip: settings.language.deleteTitle
					},
					enableDnD: true,
					functions: {
						dropHandler: self.stepDropHandlerFactory(settings.testCaseUrl + '/steps/move')
					}
				};
			}

			var moreSettings = {
				toggleRows : {
					'td.toggle-row': function (table, jqold, jqnew) {

						var data = table.fnGetData(jqold.get(0));
						var datatable = data['step-datatable'] != null ? data['step-datatable'] : "";

						jqnew.html(
							'<td colspan="1"></td>' +
							'<td colspan="2">'+
								'<div class="display-table-row controls control-group">' +
									'<label class="control-label display-table-cell" style="vertical-align:top;">Datatable</label>'+
									'<span class="display-table-cell step-datatable" style="white-space: pre-line">'+datatable+'</span>'+
								'</div>' +
							'</td>' +
							'<td colspan="2"></td>'
						);

						if (settings.permissions.isWritable) {

							var textEditSettings = confman.getStdJeditable();
							textEditSettings.url = settings.testCaseUrl + '/steps/' + data['entity-id'] + '/datatable';
							textEditSettings.type = "textarea";
							textEditSettings.rows = 10;
							textEditSettings.cols = 80;
							textEditSettings.onsubmit = function(settings, original) {
								var area = $('textarea', original);
								data['step-datatable'] = jqnew.find('.step-datatable').val();
							};

							jqnew.find('.step-datatable').customTextEditable(textEditSettings).addClass("editable").addClass("custom-text-editable");
							jqnew.find('.step-datatable').on('click', function() {
								var $area = $(jqnew.find('textarea')[0]);
								if ($area.val() !== '') {
									$area.val(StringUtil.unescape($area.val()));
								} else {
									var defaultValue = "| param1 | param2 | param3 |\n| value1 | value2 | value3 |";
									$area.val(defaultValue);
								}
							});
						}
					}
				}
			};

			$.extend(squashSettings, moreSettings);

			table.squashTable(
				{
					bServerSide: true,
					aoColumnDefs: [
						{
							bVisible: false,
							bSortable: false,
							aTargets: [0],
							mDataProp: "entity-id"
						}, {
							bVisible: true,
							bSortable: false,
							aTargets: [1],
							mDataProp: 'step-index',
							sClass: 'select-handle centered '+ dragClass,
							sWidth: '2em'
						}, {
							bVisible: true,
							bSortable: false,
							aTargets: [2],
							mDataProp: 'step-keyword',
							sClass: 'step-keyword',
							sWidth: '25%'
						}, {
							bVisible: true,
							bSortable: false,
							aTargets: [3],
							mDataProp: "step-action-word",
							sClass: "step-action-word"
						}, {
							bVisible: true,
							bSortable: false,
							aTargets: [4],
							mDataProp: "toggle-step-details",
							sClass: 'centered toggle-row',
							sWidth: '2em'
						}, {
							bVisible: true,
							bSortable: false,
							aTargets: [5],
							mDataProp: 'empty-delete-holder',
							sClass: 'centered ' + deleteClass,
							sWidth: '2em'
						}
					],
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
								actionWordCell.editable(postModifyActionWordFunction, edconf);

								self.manageAutocompleteOnActionWordCell(actionWordCell, settings, $row);
							}
						});
					}
				}, squashSettings);
		},

		// SQUASH-1450
		initTableStyle: function(settings) {
			if ($($('.action-word-input-error')[0]).text() === "") {
				$('.table-tab-wrap').css('margin-top', '25px');
			} else {
				$('.table-tab-wrap').css('margin-top', '48px');
			}
		},

		initTableDetails: function() {
			var table = $("#keyword-test-step-table").squashTable();
			var rows = table.fnGetNodes();
			rows.forEach(function(row) {
				var $row = $(row),
					toggleCell = $row.find('td.toggle-row'),
					rowModel = table.fnGetData($row);

				if (rowModel['step-datatable'] != null) {
					$(toggleCell.find('span')[1]).click();
				}
			});
		},

		stepDropHandlerFactory: function(dropUrl) {
			var self = this;
			return function stepHandler(dropData) {
				dropData.newIndex = self.calculateRealNewIndex(dropData.newIndex);
				$.post(dropUrl, dropData, function() {
					self.refresh();
				});
			};
		},

		calculateRealNewIndex: function(rowIndex) {
			var rows = $("#keyword-test-step-table tr");
			var detailsRowNumber = 0;
			for (var i = 1 ; i <= rowIndex ; i++) {
				if ($(rows[i]).attr('role') !== 'row') {
					detailsRowNumber++;
				}
			}
			return parseInt(rowIndex - detailsRowNumber);
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
			var targetTestStepIndex = -1;
			var $table = $(".test-steps-table");
			var selectedIds = $table.squashTable().getSelectedIds();
			if(selectedIds.length > 0){
				var idTargetStep = selectedIds[selectedIds.length - 1];
				targetTestStepIndex = $table.squashTable().getDataById(idTargetStep)["step-index"];
			}

			$(".action-word-input-error").text('');
			var inputActionWord = this.actionWordInput.val();
			this.addKeywordTestStep(inputActionWord, targetTestStepIndex);
			this.initTableStyle();
		},

		addKeywordTestStep: function (inputActionWord, index) {
			var self = this;
			if (this.isInputActionWordBlank(inputActionWord)) {
				return;
			}
			var inputKeyword = this.keywordInput.val();
			this.doAddKeywordTestStep(inputKeyword, inputActionWord, index)
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

		doAddKeywordTestStep: function (keyword, actionWord, index) {
			var objectData = {
				keyword: keyword,
				actionWord: actionWord,
				index: index
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

		generateScript: function() {
			$('#preview-generated-script-dialog').formDialog('open');
		},

		deleteSelectedTestSteps: function () {
			$("#delete-keyword-test-step-dialog").formDialog('open');
		},

		makeTableUrls: function (conf) {
			var tcUrl = conf.testCaseUrl;

			return {
				deleteStep: tcUrl + "/steps"
			};
		},

		manageAutocompleteOnActionWordCell: function(actionWordCell, settings, $row) {
			var self = this;
			actionWordCell.on('click', function() {
				if (settings.isAutocompleteActive) {
					actionWordCell.on('keyup', function (event) {
						// not perform autocomplete if arrows are pressed
						if (!_.contains([37, 38, 39, 40], event.which)) {
							var projectId = settings.projectId;
							var searchInput = $row.find('td.step-action-word input');
							searchInput.autocomplete();
							self.performAutocomplete(searchInput, projectId);
							self.correctAutocompleteWidth(searchInput);
						}
					});
				}
			});
		},

		performAutocomplete: function (searchInput, projectId) {
			searchInput.autocomplete('close');
			searchInput.autocomplete('disable');

			var searchInputValue = searchInput.val();

			searchInput.autocomplete({
				delay : 500,
				source: function(request, response) {
					$.ajax({
						type: 'GET',
						url: '/squash/keyword-test-cases/autocomplete',
						data: {
							projectId: projectId,
							searchInput: searchInputValue
						},
						success: function(data) {
							response(data);
						}
					});
				},
				minLength: 1
			});
			searchInput.autocomplete('enable');
		},

		// SQUASH-1303
		correctAutocompleteWidth: function(searchInput) {
			var autocompleteMaxWidth = window.innerWidth - searchInput.offset().left - 15;
			$('.ui-autocomplete').css({'max-width': autocompleteMaxWidth +'px', 'margin-right':'10px'});
		},

		postModifyActionWordFunction: function(baseUrl, table) {
			var self = this;
			return function(value, editableSettings) {
				var td = this,
						row = td.parentNode,
						rowModel = table.fnGetData(row),
						actionWordCell = $(row).find('td.step-action-word'),
						stepId = rowModel['entity-id'],
						actionWordUrl = baseUrl + '/steps/' + stepId + '/action-word';

				$.ajax({
					url: actionWordUrl,
					type: 'POST',
					data: { value: value }
				}).done(function() {
					self.saveNewActionWordInSettings(baseUrl, stepId, editableSettings);
					self.renderActionWordCell(baseUrl, stepId, actionWordCell);
				}).fail(function() {
					self.renderActionWordCell(baseUrl, stepId, actionWordCell);
				});
			};
		},

		renderActionWordCell: function(baseUrl, stepId, actionWordCell) {
			this.getActionWordHtmlFormat(baseUrl, stepId)
				.done(function(actionWordHtml) {
					actionWordCell.html(actionWordHtml);
				});
		},

		saveNewActionWordInSettings: function(baseUrl, stepId, editableSettings) {
			this.getActionWordUnstyledFormat(baseUrl, stepId)
				.done(function(actionWordUnstyled) {
					editableSettings.data = actionWordUnstyled;
				});
		},

		// with html tags for parameters
		getActionWordHtmlFormat: function(baseUrl, stepId) {
			var actionWordHtmlUrl = baseUrl + '/steps/' + stepId + '/action-word-html';
			return $.ajax({
				url: actionWordHtmlUrl,
				type: 'GET'
			});
		},

		// ex: I have some "apples", the format to display when modifying
		getActionWordUnstyledFormat: function(baseUrl, stepId) {
			var actionWordUnstyledUrl = baseUrl + '/steps/' + stepId + '/action-word-unstyled';
			return $.ajax({
				url: actionWordUnstyledUrl,
				type: 'GET'
			});
		}
	});
	return KeywordTestStepTablePanel;
});
