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
 * Controller for test-suite-execution-button.tag
 * Clients should require this module. The module subscribes to the "reload.exec-btns-panel" event
 * to initialize the panel.
 */
define(["jquery", "../app/pubsub", "jquery.squash.buttonmenu", "jquery.squash.confirmdialog", "jquery.squash"], function($, ps) {
	"use strict";

	function runnerUrl() {
		return $("#test-suite-execution-button").data("runner-url");
	}

	function checkTestSuiteExecutionDoable() {
		console.log("checkTestSuiteExecutionDoable");
		return $.ajax({
			type : 'post',
			data : {
				'mode' : 'start-resume',
				"dry-run": true
			},
			dataType : "json",
			url : runnerUrl()
		});
	}

	function classicExecution() {
		var url = runnerUrl() + '?optimized=false&mode=start-resume';
		window.open(url, "classicExecutionRunner", "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised");
	}

	function optimizedExecution() {
		var url = runnerUrl() + '?optimized=true&mode=start-resume&suitemode=true';
		var win = window.open(url, "_blank");
		win.focus();
	}

	$(document).on("click", "#start-suite-optimized-button", function() {
		checkTestSuiteExecutionDoable().done(optimizedExecution);
	});

	$(document).on("click", "#start-suite-classic-button", function() {
		checkTestSuiteExecutionDoable().done(classicExecution);
	});

	ps.subscribe("reload.exec-btns-panel", function() {
		// ****** start-resume menu ********
		var $startResumeBtn = $("#start-resume-button");
		if ($startResumeBtn.length>0){
			$("#start-resume-button").buttonmenu({
				anchor : 'right'
			});

		}

		// ******* restart menu *********
		var $restartBtn = $("#restart-button");
		if ($restartBtn.length>0){
			$restartBtn.buttonmenu({
				anchor : 'right'
			});

			var $restartDialog = $("#confirm-restart-dialog");

			$("#restart-suite-optimized-button").on("click", function(){
				console.log("click", "#restart-suite-optimized-button");
				$restartDialog.data('restart-mode', 'optimized');
				$restartDialog.confirmDialog('open');
			});

			$("#restart-suite-classic-button").on("click", function(){
				console.log("click", "#restart-suite-classic-button");
				$restartDialog.data('restart-mode', 'classic');
				$restartDialog.confirmDialog('open');
			});

			$restartDialog.confirmDialog({
				confirm : function (){
					console.log("confirmDialog");
					$.ajax({
						type : 'delete',
						url : $("#test-suite-execution-button").data("execs-url")
					})
					.then(function(){
						return checkTestSuiteExecutionDoable();
					})
					.done(function(){
						$restartDialog.confirmDialog('close');
						var mode = $restartDialog.data('restart-mode');
						if (mode === 'classic'){
							classicExecution();
						} else {
							optimizedExecution();
						}
					});
				}
			});
		}
	});
});