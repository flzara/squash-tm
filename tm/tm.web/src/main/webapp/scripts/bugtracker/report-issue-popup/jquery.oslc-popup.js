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
define(["jquery", "underscore", "workspace.storage", "jeditable.selectJEditable",
		"workspace.event-bus", "handlebars", "text!./oslc-view-template.html!strip",
		"jqueryui", "squashtest/jquery.squash.popuperror", "jquery.squash.formdialog"],
	function ($, _, storage, SelectJEditable, eventBus, Handlebars, source) {
		"use strict";

		squashtm.eventBus = eventBus;

		function init(settings) {
			/* jshint validthis: true */
			//init always called with a context object

			// turn to dialog
			this.formDialog({
				height: 650,
				width: 650
			});

			//issue model
			this.model = new Backbone.Model();
			this.mdlTemplate = null;

			//urls
			this.searchUrl = settings.searchUrl;
			this.bugTrackerId = settings.bugTrackerId;

			// project preferences
			this.projectNames = settings.projectNames;
			this.currentProjectId = settings.currentProjectId;

			var projectPrefs = storage.get("bugtracker.projects-preferences") || {};
			this.selectedProject = (projectPrefs[this.currentProjectId] === undefined) ?
				this.projectNames[0] :
				projectPrefs[this.currentProjectId];

			//check if the preference still exist, if not use the first project
			if (!_.contains(this.projectNames, this.selectedProject)) {
				this.selectedProject = this.projectNames[0];
			}

			//main panels of the popup
			this.pleaseWait = this.find(".pleasewait");
			this.content = this.find(".content");

			//the radio buttons
			this.attachRadio = this.find(".attach-radio");
			this.reportRadio = this.find(".report-radio");

			//the issue id (if any)
			this.idText = this.find(".id-text");


			this.issueCreate = this.find('#issue-create');
			this.issueSearch = this.find('#issue-search');
			//the error display
			this.error = this.find(".issue-report-error");
			this.error.popupError();


			//bind the spans standing for label for the radio buttons
			//(actual labels would have been preferable except for the default style)
			this.find(".issue-radio-label").click(function () {
				$(this).prev("input[type='radio']").click();
			});

			var self = this;
			var listener = function (e) {
				var HEADER = "oslc-response:";
				if (e.data.indexOf(HEADER) === 0) {
					self.submitIssue(e.data.substr(HEADER.length));
					self.formDialog("close");
				}
			};


			window.addEventListener('message', listener, false);

		}

		$.fn.btOslcIssueDialog = function (settings) {

			var self = this;

			this.formDialog({
				height: 650,
				width: 650
			});

			init.call(this, settings);

			/* ************** some events ****************** */

			this.attachRadio.click(function () {
				toAttachMode();			
			});

			this.reportRadio.click(function () {
				toReportMode();
			});


			/* ************* public popup state methods **************** */


			var toAttachMode = $.proxy(function () {
				flipToMain();
				this.issueCreate.hide();
				this.issueSearch.show();				
				this.issueSearch.html(this.template(this.model.attributes.selectDialog));	
			}, self);

			var toReportMode = $.proxy(function () {
				flipToMain();
				this.issueCreate.show();
				this.issueSearch.hide();
				this.issueCreate.html(this.template(this.model.attributes.createDialog));
			}, self);

			var flipToPleaseWait = $.proxy(function () {
				this.pleaseWait.show();
				this.content.hide();
			}, self);


			var flipToMain = $.proxy(function () {
				this.content.show();
				this.pleaseWait.hide();
			}, self);


			/* ********************** model and view management ************ */

			var setModel = $.proxy(function (newModel) {
				this.model.set(newModel);
				createViewForModel();
			}, self);


			var createViewForModel = $.proxy(function () {
				var template = Handlebars.compile(source);
				this.template = template;	

			}, self);


			var resetModel = $.proxy(function () {
				var resetModelJob = $.Deferred();
				getIssueModelTemplate()
					.done(function () {
						var copy = $.extend(true, {}, self.mdlTemplate);
						setModel(copy);
						resetModelJob.resolve();
					})
					.fail(bugReportError);
				return resetModelJob.promise();
			}, self);


			var getIssueModelTemplate = $.proxy(function () {

				var jobDone = $.Deferred();
				if (!this.mdlTemplate) {
					flipToPleaseWait();
					$.ajax({
							url: self.reportUrl,
							type: "GET",
							data: {"project-name": self.selectedProject},
							dataType: "json"
						})
						.done(function (response) {		
							self.mdlTemplate = response;
							flipToMain();
							jobDone.resolve();
						})
						.fail(function () {
							jobDone.reject();
							self.issueCreate.html('');
							self.issueSearch.html('');
						});
				}
				else {
					jobDone.resolve();
				}

				return jobDone.promise();
			}, self);


			// we let the usual error handling do its job here
			// if the error is a field validation error, let's display in the error display
			// we're doing it manually because in some context (ieo for instance) the general
			// error handler is not present.
			var bugReportError = $.proxy(function (jqXHR, textStatus, errorThrown) {
				try {
					var message = $.parseJSON(jqXHR.responseText).fieldValidationErrors[0].errorMessage;
					this.error.find('.error-message').text(message);
				} catch (ex) {
					// well maybe that wasn't for us after all
				}
				flipToMain();
				this.error.popupError('show');
			}, self);


			/* ************* events ************************ */

			this.changeBugTrackerProject = function (project) {
				var self = this;
				var projectPrefs = storage.get("bugtracker.projects-preferences") || {};
				projectPrefs[this.currentProjectId] = project;
				storage.set("bugtracker.projects-preferences", projectPrefs);
				self.selectedProject = project;
				self.mdlTemplate = null;
				resetModel().done(function(){
					self.issueSearch.html(self.template(self.model.attributes.selectDialog));
					self.issueCreate.html(self.template(self.model.attributes.createDialog));	
				});
			};

			this.submitIssue = function (data) {
				var issue = JSON.parse(data);
				var issueData = issue["oslc:results"][0];

				if (issueData !== undefined) {
					var issueId = issueData["rdf:resource"].split("/").pop();

					var json = {"issueId": issueId, "url": issueData["rdf:resource"]};


					$.ajax({
						url: this.reportUrl.replace(/new-issue/, "new-oslc-issue"),
						type: 'POST',
						data: issueId,
						contentType: 'application/json',
						dataType: 'json'
					}).done(function () {
						eventBus.trigger('context.bug-reported', json);
					});
				}

			};
			this.open = function (settings) {
				var self = this;
				this.reportUrl = settings.reportUrl;
				var data = [];
				this.projectNames.forEach(function (val) {
					data.push({code: val, value: val});
				});

				var template = Handlebars.compile(self.find("#project-selector-tpl").html());
				self.find("#project-selector").html(template({options: data}));
				resetModel().done(function(){
					self.reportRadio.click();	
				});
				self.formDialog("open");

				self.find("#project-selector").find("option").each(function (idx, opt) {
					if (opt.value == self.selectedProject) {
						$(opt).attr("selected", "selected");
					}
				});
			};

			//the opening of the popup :
			this.bind("formdialogclose", function () {
				self.mdlTemplate = null;
			});


			// the project selector
			this.find("#project-selector").on("change", function () {
				var selected = self.find("#project-selector").find(":selected").val();
				self.changeBugTrackerProject(selected);
			});

			return this;

		};

		//though loaded as a module, it doesn't produce anything. It's a jQuery plugin after all.
		return null;
	});
