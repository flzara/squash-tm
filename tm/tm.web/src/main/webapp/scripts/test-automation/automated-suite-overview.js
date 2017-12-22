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
 */
define(["jquery", "underscore", "app/squash.handlebars.helpers", "../app/pubsub", "squash.statusfactory", "squash.translator",
        "jqueryui", "jquery.squash.formdialog"], function($, _, Handlebars, ps, statusfactory, translator) {
	"use strict";

	Handlebars.registerHelper("ballsyStatus", $.proxy(statusfactory.getHtmlFor, statusfactory));

	if ($.squash.autosuiteOverview === undefined) {

		$.widget("squash.autosuiteOverview", $.squash.formDialog, {

			// remember that some options are passed using dom conf, done in the super constructor
			options : {
				suite : null,
				intervalId : null
			},

			_create : function(){
				this._super();

				var self = this;

				this.options.executionRowTemplate = Handlebars.compile($("#exec-info-tpl").html());
				this.options.executionAutoInfos = $("#executions-auto-infos");

				// progressbar
				var executionProgressBar =  $("#execution-auto-progress-bar");
				executionProgressBar.progressbar({value : 0});
				executionProgressBar.find("div").addClass("ui-state-default");

				// events
				this.onOwnBtn("mainclose", function(){
					self.close();
				});

				this.onOwnBtn("warningok", function(){
					self.close();
				});

				this.onOwnBtn("warningcancel", function(){
					self.unclose();
				});
				this.onOwnBtn("submitNodes", function(){
					self._submitNodes();
				});
				this.onOwnBtn("discardNodes", function(){
					self._removeSuite();
				});
			},

			close : function(){

				// check : if the suite wasn't finished, we must let the user confirm
				var suite = this.options.suite;
				if (!! suite && suite.percentage < 100 && this.getState() !== "warning"){
					this.setState("warning");
					return false;
				}
				// else we actually close that dialog
				else{
					this._super();
					if (!! this.options.intervalId ){
						clearInterval(this.options.intervalId);
					}
					this._cleancontent();
				}

			},

			unclose : function (){
				this.setState("main");
			},


			_cleancontent : function(){
				var opts = this.options;

				clearInterval(opts.intervalId);
				opts.executionAutoInfos.empty();

				$("#execution-auto-progress-bar").progressbar("value", 0);
				$("#execution-auto-progress-amount").text(0 + "/" + 0);

				var table = $("table.test-plan-table");
				if (table.length > 0) {
					table.squashTable().refresh();
				}
				// TODO : replace the following function that doesn't exist anymore with an event published on the event
				// bus
				// refreshStatistics();
			},

			watch : function(suite){
				this.options.suite = suite;
				this._initWatch();
				this.setState("main");
				this.open();

				var self = this;

				if (suite.percentage < 100) {
					this.options.intervalId = setInterval(function() {
						self.update();
					}, 5000);
				}
			},

			start : function(suite) {
				if (suite.manualNodeSelection) {
					this._startManualSelectNodes(suite);
				} else {
					this._startAutoSelectNodes(suite);
				}
			},

			_startAutoSelectNodes : function(suite) {
				this._execAndWatch(suite.id, function() { return []; });
			},

			_execAndWatch: function(suiteId, dataMapper) {
				var runUrl = this.options.url + "/" + suiteId  + "/executor";
				var self = this;

				$.ajax({
					type : "POST",
					url : runUrl,
					data: JSON.stringify(dataMapper()),
					dataType : "json",
					contentType: "application/json"
				}).done(function(overview) {
					if (overview.executions.length === 0) {
						$.squash.openMessage(translator.get("popup.title.Info"), translator.get("dialog.execution.auto.overview.error.none"));
					} else {
						self.watch(overview);
					}
				});
			},

			_startManualSelectNodes : function(suite) {
				this.suiteId = suite.id;
				this.setState("node-selector");
				this.open();

				var template = Handlebars.compile($("#node-selector-pnl-tpl").html());
				var manuals = _.filter(suite.contexts, function(context) {
					return context.project.server.manualSlaveSelection;
				}) || {};
				$("#node-selector-pnl").html(template({contexts: manuals}));
			},

			_submitNodes : function() {
				var dataMapper = function() {
					var data = _.map($("#node-selector-pnl").find("fieldset"), function(item) {
						var $item = $(item);
						var node = $item.find("select").val();
						node = node === "" ? null : node;

						return {
							projectId : $item.data("proj-id"),
							node : node
						};
					});

					return data;
				};

				this._execAndWatch(this.suiteId, dataMapper);
			},

			update : function(){
				var self = this,
					opts = this.options;

				$.ajax({
					type : "GET",
					url : opts.url + "/" + opts.suite.suiteId + "/executions",
					dataType : "json"
				}).done(function(json){
					self.options.suite = json;
					self._updateWatch();
					if (json.percentage == 100) {
						clearInterval(self.options.intervalId);
					}
				});
			},

			_initWatch : function(){
				var data = this.options.suite;
				var executions = data.executions,
					progress = data.percentage;

				// the progressbar
				var executionComplete = progress / 100 * executions.length;
				$("#execution-auto-progress-bar").progressbar("value", progress);
				$("#execution-auto-progress-amount").text(Math.round(executionComplete) + "/" + executions.length);

				// the "table"
				var htmlRows = this.options.executionRowTemplate({execs : executions});
				this.options.executionAutoInfos.html(htmlRows);
			},

			_updateWatch : function(){
				this._initWatch();
			},

			_removeSuite: function() {
				var opts = this.options;
				var suiteId = this.suiteId;

				$.ajax({
					type : "DELETE",
					url : opts.url + "/" + suiteId,
				});

				this.close();
			},

			_destroy: function() {
				if (!!squashtm.context) {
					squashtm.context.autosuiteOverview = undefined;
				}
				this._super();
				console.log("autosuite overview dialog destroyed");

			}
		});

	}

	function init() {
		if (squashtm.context === undefined || squashtm.context.autosuiteOverview === undefined){
			squashtm.context = squashtm.context || {};

			var dialog = $("#execute-auto-dialog");
			dialog.autosuiteOverview();

			// note that we are storing the widget itself
			squashtm.context.autosuiteOverview = dialog.data("autosuiteOverview");
			console.log("inited auto-suite-overview");
		}
	}

	ps.subscribe("reload.auto-suite-overview-popup", function() {
		init();
	});

	var module = {
			// should not be useful anymore
		// init : init,

		get : function(){
			if (squashtm.context === undefined || squashtm.context.autosuiteOverview === undefined){
				this.init();
			}
			return squashtm.context.autosuiteOverview;
		}
	};

	return module;
});

