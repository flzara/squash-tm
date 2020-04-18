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
define([ "jquery", "backbone", "underscore", 'workspace.event-bus', "./popups", "app/util/StringUtil", "squash.translator"], function($, Backbone, _, eventBus, popups, StringUtil, translator) {

	var KeywordTestStepTablePanel = Backbone.View.extend({

		el : "#tab-tc-keyword-test-steps",

		initialize : function(options) {
			var self = this;

			$.squash.decorateButtons();
			this.settings = options.settings;
			var urls = this.makeTableUrls(this.settings);
			this.initKeywordTestStepTable(this.settings);

			this.actionWordInput = $('#action-word-input');
			this.keywordInput = $('#keyword-input');

			this.keyupTimer = undefined;
			if(this.settings.isAutocompleteActive) {
				this.actionWordInput.on('keyup', function(event) {
					// not perform autocomplete if arrows are pressed
					if(!_.contains([37, 38, 39, 40], event.which)) {
						self.performAutocomplete(event, self);
					}
				});
			}
			this.actionWordInput.autocomplete({
				select: function(event, ui) {
					self.addKeywordTestStepFromAutocomplete(self, event, ui);
				}
			});

			//the popups
			var conf = {};
			conf.permissions = {};
			conf.urls = {};
			conf.urls.testCaseStepsUrl = urls.deleteStep;
			conf.testCaseId = this.settings.testCaseId;
			conf.permissions.writable = this.settings.permissions.isWritable;
			conf.stepsTablePanel = this;
			popups.init(conf);
		},

		events : {
			"click #add-keyword-test-step-btn" : "addKeywordTestStepFromButton",
			"click #delete-all-steps-button" : "deleteSelectedTestSteps"
		},

		initKeywordTestStepTable : function(settings) {
			var testCaseId = settings.testCaseId;
			var table = $("#keyword-test-step-table-" + testCaseId);
			table.squashTable(
				{
					bServerSide: true,
					aaData : settings.stepData,
					iDeferLoading: settings.stepData.length,
					sAjaxSource: '/squash/test-cases/'+testCaseId+'/steps/keyword-test-step-table'
				}, {
					deleteButtons: {
						delegate: "#delete-keyword-test-step-dialog",
						tooltip: settings.language.deleteTitle
					}
				});
		},

		refresh: function () {
			$("#keyword-test-step-table-" + this.settings.testCaseId).squashTable().refreshRestore();
		},

		cleanInputs: function() {
			$("#keyword-input").val('GIVEN');
			this.actionWordInput.val('');
			$(".action-word-input-error").text('');
		},

		addKeywordTestStepFromButton: function() {
			var inputActionWord = this.actionWordInput.val();
			this.addKeywordTestStep(inputActionWord);
		},

		addKeywordTestStep: function(inputActionWord) {
			var self = this;
			if(this.isInputActionWordBlank(inputActionWord)) {
				return;
			}
			var inputKeyword = this.keywordInput.val();
			this.doAddKeywordTestStep(inputKeyword, inputActionWord)
				.done(function(testStepId) {
					self.afterKeywordTestStepAdd(testStepId, inputActionWord);
				});
		},

		isInputActionWordBlank: function(inputActionWord) {
			if(StringUtil.isBlank(inputActionWord)) {
				$('.action-word-input-error').text(translator.get("message.actionword.empty"));
				this.actionWordInput.val("");
				return true;
			} else {
				return false;
			}
		},

		doAddKeywordTestStep: function(keyword, actionWord) {
			var objectData = {
				keyword : keyword,
				actionWord : actionWord
			};
			return $.ajax({
				type: 'POST',
				url: "/squash/test-cases/"+this.settings.testCaseId+"/steps/add-keyword-test-step",
				contentType: 'application/json',
				data: JSON.stringify(objectData)
			});
		},

		afterKeywordTestStepAdd: function(testStepId, inputActionWord) {
			var displayDiv = $('#add-keyword-test-step-result');
			displayDiv.text("The keyword test step has been successfully created with id : " + testStepId + " and name : "+ inputActionWord);
			this.refresh();
			this.cleanInputs();
			eventBus.trigger('testStepsTable.stepAdded');
		},

		deleteSelectedTestSteps: function() {
			$("#delete-keyword-test-step-dialog").formDialog('open');
		},

		makeTableUrls: function(conf){
			var tcUrl =  conf.testCaseUrl;
			var ctxUrl = conf.rootContext;

			return {
				deleteStep: tcUrl + "/steps",
			};
		},

		performAutocomplete: function(event,  self) {
			var searchInput = $(event.currentTarget);
			var searchInputValue = searchInput.val();
			searchInput.autocomplete('close');
			searchInput.autocomplete('disable');
			clearTimeout(self.keyupTimer);
			self.keyupTimer = setTimeout(function() {
				$.ajax({
					type: 'GET',
					url: '/squash/keyword-test-cases/autocomplete',
					data: { searchInput: searchInputValue }
				}).done(function(actionWords) {
					searchInput.autocomplete('enable');
					searchInput.autocomplete('option', 'source', actionWords);
					searchInput.autocomplete('search');
				});
			}, 300);
		},

		addKeywordTestStepFromAutocomplete: function(self, event, ui) {
			event.preventDefault();
			var inputActionWord = ui.item.value;
			self.addKeywordTestStep(inputActionWord);
		},
	});
	return KeywordTestStepTablePanel;
});
