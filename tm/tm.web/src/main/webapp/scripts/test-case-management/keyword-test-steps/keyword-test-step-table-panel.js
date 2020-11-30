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
			conf.isAutocompleteActive = this.settings.isAutocompleteActive;
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
			"click #preview-generated-script-button": "generateScript",
			"click #show-details-button": "initTableDetails"
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
						var datatableLabel = translator.get('testcase.bdd.step.datatable.label');

						jqnew.html(
							'<td colspan="2"></td>' +
							'<td colspan="1">'+
								'<div class="display-table-row controls control-group">' +
									'<label class="control-label display-table-cell" style="vertical-align:top;">'+datatableLabel+'</label>'+
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
								data['step-datatable'] = $(jqnew.find('textarea')[0]).val();
							};

							jqnew.find('.step-datatable').customTextEditable(textEditSettings).addClass("editable").addClass("custom-text-editable");
							jqnew.find('.step-datatable').on('click', function() {
								var $area = $(jqnew.find('textarea')[0]);
								if ($area.val() !== '') {
									$area.val(StringUtil.unescape($area.val()));
								} else {
									if (data['step-datatable'] == null || data['step-datatable'] === '') {
										var defaultValue = translator.get('testcase.bdd.step.datatable.default-value');
										$area.val(defaultValue);
									}
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

				if (rowModel['step-datatable'] != null && rowModel['step-datatable'] !== '' && $(toggleCell.find('span')[0]).hasClass('small-right-arrow')) {
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
			var self = this;
			var targetTestStepIndex = -1;
			var $table = $(".test-steps-table");
			var selectedIds = $table.squashTable().getSelectedIds();
			if(selectedIds.length > 0){
				var idTargetStep = selectedIds[selectedIds.length - 1];
				targetTestStepIndex = $table.squashTable().getDataById(idTargetStep)["step-index"];
			}

			$(".action-word-input-error").text('');
			var inputActionWord = this.actionWordInput.val();
			if (this.settings.isAutocompleteActive) {
				self.retrieveAllDuplicatedActionWithProject(inputActionWord)
					.done(function(data) {
						if (Object.keys(data).length > 0) {
							self.generateDuplicatedActionDialog(data);
						} else {
							self.addKeywordTestStep(inputActionWord, targetTestStepIndex);
						}
				});
			} else {
				self.addKeywordTestStep(inputActionWord, targetTestStepIndex);
			}
			self.initTableStyle();
		},

		addKeywordTestStep: function (inputActionWord, index) {
			var self = this;
			if (this.isInputActionWordBlank(inputActionWord)) {
				return;
			}
			var inputKeyword = this.keywordInput.val();
			this.doAddKeywordTestStep(inputKeyword, inputActionWord, index)
				.done(function (testStepId) {
					self.afterKeywordTestStepAdd(testStepId);
				});
		},

		retrieveAllDuplicatedActionWithProject: function(inputActionWord) {
			var projectId = this.settings.projectId;
			return $.ajax({
				type: 'GET',
				url: squashtm.app.contextRoot + "keyword-test-cases/duplicated-action",
				contentType: 'application/json',
				data: {
					projectId: projectId,
					inputActionWord: inputActionWord
				}
			});
		},

		retrieveAllDuplicatedActionWithProjectInTestStep: function(stepId, inputActionWord) {
			var projectId = this.settings.projectId;
			return $.ajax({
				type: 'GET',
				url: this.settings.testCaseUrl + "/steps/" + stepId + "/duplicated-action",
				contentType: 'application/json',
				data: {
					projectId: projectId,
					inputActionWord: inputActionWord
				}
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
			return $.ajax(
			{
				type: 'POST',
				url: this.settings.testCaseUrl + "/steps/add-keyword-test-step",
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

		generateDuplicatedActionDialog: function(data) {
			$('#duplicated-action-dialog').formDialog('open');
			Object.keys(data).sort().forEach(function(key) {
				$('#duplicated-action-projects').append('' +
					'<div style="padding:2px"><input type="radio" id="duplicated-action-'+data[key]+'" name="duplicatedAction" value="'+data[key]+'">' +
					'<label for="duplicated-action-'+data[key]+'">'+key+'</label></div>');
			});
			$('input[name="duplicatedAction"]:first').attr('checked', true);
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
						url: squashtm.app.contextRoot + 'keyword-test-cases/autocomplete',
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
						stepId = rowModel['entity-id'],
						actionWordCell = $(row).find('td.step-action-word');

				if (self.settings.isAutocompleteActive) {
					self.retrieveAllDuplicatedActionWithProjectInTestStep(stepId, value)
						.done(function(data) {
							if (Object.keys(data).length > 0) {
								self.initDuplicatedActionInTestStep(data, baseUrl, value, stepId, editableSettings, actionWordCell);
							} else {
								self.doModifyActionWord(baseUrl, value, stepId, editableSettings, actionWordCell);
							}
						})
				} else {
					self.doModifyActionWord(baseUrl, value, stepId, editableSettings, actionWordCell);
				}
			};
		},

		initDuplicatedActionInTestStep: function(data, baseUrl, value, stepId, editableSettings, actionWordCell) {
			var self = this;
			var duplicatedActionDialog = $("#test-step-duplicated-action-dialog");
			duplicatedActionDialog.formDialog();

			duplicatedActionDialog.on('formdialogopen', function() {
				$('#test-step-duplicated-action-projects').empty();
				Object.keys(data).sort().forEach(function(key) {
					$('#test-step-duplicated-action-projects').append('' +
						'<div style="padding:2px"><input type="radio" id="test-step-duplicated-action-'+data[key]+'" name="testStepDuplicatedAction" value="'+data[key]+'">' +
						'<label for="test-step-duplicated-action-'+data[key]+'">'+key+'</label></div>');
				});
				$('input[name="testStepDuplicatedAction"]:first').attr('checked', true);
			});

			duplicatedActionDialog.on('formdialogconfirm', function() {
				var actionWordId = $('input[name="testStepDuplicatedAction"]:checked').val();
				$.ajax({
					type: 'GET',
					url: baseUrl + '/steps/' + stepId + '/action-word-with-id',
					contentType: 'application/json',
					data: {
						actionWord: value,
						actionWordId: actionWordId
					}
				}).done(function() {
					self.saveNewActionWordInSettings(baseUrl, stepId, editableSettings);
					self.renderActionWordCell(baseUrl, stepId, actionWordCell);
				}).fail(function() {
					self.renderActionWordCell(baseUrl, stepId, actionWordCell);
				});
				$(this).formDialog('close');
			});

			duplicatedActionDialog.on('formdialogcancel', function() {
				$(this).formDialog('close');
			});

			duplicatedActionDialog.on("formdialogclose", function() {
				self.renderActionWordCell(baseUrl, stepId, actionWordCell);
			});

			duplicatedActionDialog.formDialog('open');
		},

		doModifyActionWord: function(baseUrl, value, stepId, editableSettings, actionWordCell) {
			var self = this;
			var actionWordUrl = baseUrl + '/steps/' + stepId + '/action-word';

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
