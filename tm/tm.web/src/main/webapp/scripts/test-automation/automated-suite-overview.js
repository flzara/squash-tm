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
/**
 * This module listens to the "reload.auto-suite-overview-popup" event and self initializes accordingly.
 *
 * Module api : get() : returns the dialog widget instance.
 * 
 * 
 * That dialog leads the user in a conversation which has the following stages :
 * 
 * 1/ Preview : if the automated suite to generate needs parameterization (slave nodes), the preview is shown.
 * 	When the user is happy, he can proceed to the Preparation
 * 
 * 2/ Preparation : if the user proceeded through Preview, or if no parameterization was required, 
 * the Preparation phase is just a waiting screen while the Automated Suite is being created.
 * Once the creation is over, the dialog automatically goes to Processing
 * 
 * 3/ Processing : that phase shows the advancement of the execution status.
 *
 */
define(["jquery", "underscore", "app/squash.handlebars.helpers", "../app/pubsub", "squash.statusfactory", "squash.translator",
	"jqueryui", "jquery.squash.formdialog"], function ($, _, Handlebars, ps, statusfactory, translator) {
	"use strict";

	Handlebars.registerHelper("ballsyStatus", $.proxy(statusfactory.getHtmlFor, statusfactory));

	if ($.squash.autosuiteOverview === undefined) {

		$.widget("squash.autosuiteOverview", $.squash.formDialog, {

			// remember that some options are passed using dom conf, done in the super constructor
			options: {
				
				// the preview object is the object shown in the Preview stage
				preview: null,
				
				// the overview object is the object shown in the Processing stage
				overview: null,
				
				// this is the id of the routine that updates the Processing view
				refreshIntervalId: null,
				
				// The logic of closing the dialog partly rely on 
				// which stage the dialog was in.
				// Caution : the stage is not exactly same as the formdialog state. 
				// For instance the dialog could be at stage 'processing' but in the state 'quit'  
				stage: "preview",
				
				// any ongoing xhr we could need to cancel
				xhr : null
			},
			

			// **************** lifecycle ******************************* 

			_create: function () {
				this._super();

				var self = this;

				this.options.executionRowTemplate = Handlebars.compile($("#exec-info-tpl").html());
				this.options.executionAutoInfos = $("#executions-auto-infos");

				// progressbar
				var executionProgressBar = $("#execution-auto-progress-bar");
				executionProgressBar.progressbar({value: 0});
				executionProgressBar.find("div").addClass("ui-state-default");

				// events
				// about self.close(), see comments on the function 'close'
								
				this.onOwnBtn("previewConfirm", function () {
					self._previewProceed();
				});
				
				this.onOwnBtn("previewCancel", function () {
					self.close();
				});
				
				this.onOwnBtn("preparationClose", function(){
					self.close();
				});
				
				this.onOwnBtn("processingClose", function(){
					self.close();
				});				

				this.onOwnBtn("quitConfirm", function () {
					self.close();
				});

				this.onOwnBtn("quitCancel", function () {
					self._resume();
				});

								
				$('#node-selector-pnl').on('click', '.sq-tl .tl-head', function(evt){
					self._loadTestList(evt.currentTarget);
				});
			},

			
			// that function is a predicate that says whether, on user's request to close the popup,
			// whether he should be redirected to the "quit" pane first. 
			_shouldShowQuit : function(){
				var opts = this.options;
				
				// check : if we've been gone beyond the preview phase 
				// and the execution is not complete,
				// ask for confirmation
				var isRunning = (opts.stage === "preparation") || (opts.stage === "processing");
				var notComplete = (! opts.overview) || (opts.overview.percentage < 100);
				var alreadyQuitting = ( this.getState() == "quit");
				
				return (isRunning && notComplete && (! alreadyQuitting));
			},
			
			/*
			 * The function 'close' closes the dialog unless the user should be shown the 'quit' pane first.
			 * 
			 * Ideally those two outcomes should not be the business of the function 'close' : it should close 
			 * the dialog, period. However there are multiple ways to ask a dialog to close, some of which we 
			 * have no control. So we have to centralize our logic in this place to make sure it will always 
			 * be applied.
			 */
			close: function () {
								
				if (this._shouldShowQuit()){
					this.setState("quit");
					return false;
				} 
				
				// else we actually close that dialog
				else {
					this._super();
					this._cleancontent();
					this._refreshStakeholders();
				}

			},
			
			_cleancontent: function () {
				var opts = this.options;

				// if quitting while in preparation phase
				if (!! opts.xhr){
					opts.xhr.abort();
				}
				
				// if an interval was set
				if (!! opts.refreshIntervalId) {
					clearInterval(this.options.refreshIntervalId);
				}
				
				// clean the processing pane content
				opts.executionAutoInfos.empty();
				$("#execution-auto-progress-bar").progressbar("value", 0);
				$("#execution-auto-progress-amount").text(0 + "/" + 0);
			},
			
			_refreshStakeholders : function(){
				
				/*
				 * We do refresh the tables only at the processing phase, but do not so 
				 * at the preview or preparation phase.
				 * 
				 *  Refreshing a the preview phase makes no sense because nothing has changed.
				 *  
				 *  Refreshing a preparation phase has unpleasant effects. Indeed  the 
				 * automated suite is still under creation, and thus is modifying the 
				 * test plan (adding new executions etc). Refreshing the table then 
				 * have undesirable effects :
				 * - the server takes sweet long time to read the items while they are being 
				 * rewritten,
				 * - and in the mean time the user sees the ajax spinner at the 
				 * top of the page for no apparent reasons.
				 * 
				 *   Therefore, we do refresh the test plan only at processing phase 
				 *   where the server is under much lighter load. 
				 */
				if (this.options.stage === "processing"){
					var table = $("table.test-plan-table");
					if (table.length > 0) {
						table.squashTable().refresh();
					}
				}
			},

			_resume: function () {
				var stage = this.options.stage;
				this.setState(stage);
			},

			start: function (preview) {
				this.options.preview = preview;
				
				// should the user define manually the slave nodes ?
				if (preview.isManualSlaveSelection) {
					this._showPreview();
				} 
				// else skip directly to execution preparation
				else {
					this._showPreparation(preview);
				}
				

				this.open();
			},
		
			_destroy: function () {
				if (!!squashtm.context) {
					squashtm.context.autosuiteOverview = undefined;
				}
				this._super();
				console.log("autosuite overview dialog destroyed");

			},
			
			// ******* preview (optional view) ***********
			
			/*
			 * The preview phase is optional and is only shown if one or several automated projects 
			 * may run on slaves. It allows the user to specify which slave it is.  
			 */
			_showPreview: function () {

				this.options.stage = "preview";
				this.setState("preview");
				
				var preview = this.options.preview;
				
				var template = Handlebars.compile($("#node-selector-pnl-tpl").html());
				
				var manualSelect = _.filter(preview.projects, function (project) {
					return project.nodes.length > 0;
				}) || [];
				
				$("#node-selector-pnl").html(template({projects : manualSelect}));

			},
			
			_loadTestList: function (paneHead) {

				var $pane = $(paneHead).parent();
				// abort if the test list is loaded already
				if ($pane.data('loaded') === true){
					return;
				}
				
				// else we have to load it
				var listTemplate = Handlebars.compile('<ul>{{#each this}}<li>{{this}}</li>{{/each}}</ul>');
				
				var spec = this.options.preview.specification;
				var autoProjId = $($pane).parents('fieldset').data('proj-id');
				
				$.ajax({
					url : this.options.url + '/preview/test-list?auto-project-id=' + autoProjId,
					type : 'POST',
					data : JSON.stringify(spec),
					contentType : 'application/json',
					dataType: 'json'
				})
				.done(function(testList){					
					var html = listTemplate(testList);
					$pane.find('.tl-body').html(html);
					$pane.data('loaded', true);
				});
			},
			
			_previewProceed : function(){
				
				var specification = this.options.preview.specification;
				specification.executionConfigurations = _.map($("#node-selector-pnl").find("fieldset"), function (item) {
					var $item = $(item);
					var node = $item.find("select").val();
					node = node === "" ? null : node;

					return {
						projectId: $item.data("proj-id"),
						node: node
					};
				});
				
				// now on to preparation
				this._showPreparation();
				
			},
			
			
			// ************** execution preparation ******************************
			

			/*
			 * Preparation phase is the phase where the automated suite is under creation.
			 * It ends when it is created and the processing is running.
			 */
			_showPreparation: function () {
				
				this.options.stage = "preparation";
				this.setState('preparation');
				
				// reset the overview
				this.options.overview = null;
				
				var self = this;
				
				// store the xhr in the options in case we need to cancel it
				// remember that canceling an xhr merely means that we are no 
				// longer interested in the result, but it won't certainly cancel 
				// the job on the server.
				this.options.xhr = $.ajax({
					url : this.options.url + '/create-and-execute',
					type : 'POST',
					contentType : 'application/json',
					dataType : 'json',
					data : JSON.stringify(this.options.preview.specification)
				})
				.done(function(overview){
					if (overview.executions.length === 0) {
						$.squash.openMessage(translator.get("popup.title.Info"), translator.get("dialog.execution.auto.overview.error.none"));
					}
						
					self._showProcessing(overview);

					// unset the xhr
					self.options.xhr = null;
				});
				
				
			},
			
			// ************** execution processing watchdog **********************
			

			/*
			 * Processing phase is the phase where the automated suite is created and is now running.
			 */
			_showProcessing: function(overview) {
				this.options.stage = "processing";
				this.options.overview = overview;
				this._repaintProcessing();
				this.setState("processing");
				this.open();

				var self = this;

				if (overview.percentage < 100) {
					this.options.refreshIntervalId = setInterval(function () {
						self.update();
					}, 5000);
				}
			},
			

			update: function () {
				var self = this,
					opts = this.options;

				$.ajax({
					type: "GET",
					url: opts.url + "/" + opts.overview.suiteId + "/executions",
					dataType: "json"
				}).done(function (newOverview) {
					self.options.overview = newOverview;
					self._repaintProcessing();
					if (newOverview.percentage == 100) {
						clearInterval(self.options.refreshIntervalId);
					}
				});
			},

			_repaintProcessing: function () {
				var data = this.options.overview;
				var executions = data.executions,
					progress = data.percentage;

				// the progressbar
				var executionComplete = progress / 100 * executions.length;
				$("#execution-auto-progress-bar").progressbar("value", progress);
				$("#execution-auto-progress-amount").text(Math.round(executionComplete) + "/" + executions.length);

				// the "table"
				var htmlRows = this.options.executionRowTemplate({execs: executions});
				this.options.executionAutoInfos.html(htmlRows);
			}
		});

	}

	
	// ************************ PAGE LOAGING CODE *********************************
	
	
	function init() {
		if (squashtm.context === undefined || squashtm.context.autosuiteOverview === undefined) {
			squashtm.context = squashtm.context || {};

			var dialog = $("#execute-auto-dialog");
			dialog.autosuiteOverview();

			// note that we are storing the widget itself
			squashtm.context.autosuiteOverview = dialog.data("autosuiteOverview");
			console.log("inited auto-suite-overview");
		}
	}

	ps.subscribe("reload.auto-suite-overview-popup", function () {
		init();
	});

	var module = {
		// should not be useful anymore
		// init : init,

		get: function () {
			if (squashtm.context === undefined || squashtm.context.autosuiteOverview === undefined) {
				this.init();
			}
			return squashtm.context.autosuiteOverview;
		}
	};

	return module;
});

