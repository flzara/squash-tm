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
			$.squash.decorateButtons();
			var self = this;
			this.settings = options.settings;
			var urls = this.makeTableUrls(this.settings);
			this.initKeywordTestStepTable(this.settings);

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
			"click #add-keyword-test-step-btn" : "addKeywordTestStep",
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
			$("#action-word-input").val('');
			$(".action-word-input-error").text('');
		},

		addKeywordTestStep: function() {
			var self = this;
			var inputActionWord = $('#action-word-input').val();

			if(StringUtil.isBlank(inputActionWord)) {
				$('.action-word-input-error').text(translator.get("message.actionword.empty"));
				return;
			}

			var inputKeyword = $('#keyword-input').val();
			var objectData = {
				keyword : inputKeyword,
				actionWord : inputActionWord
			};

			$.ajax({
				type: "POST",
				url: "/squash/test-cases/"+this.settings.testCaseId+"/steps/add-keyword-test-step",
				contentType: 'application/json',
				data: JSON.stringify(objectData)
			}).done(function(id) {
				var displayDiv = $('#add-keyword-test-step-result');
				displayDiv.text("The keyword test step has been successfully created with id : "+id+" and name : "+inputActionWord);
				self.refresh();
				self.cleanInputs();
				eventBus.trigger('testStepsTable.stepAdded');
			});
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
		}
	});
	return KeywordTestStepTablePanel;
});
