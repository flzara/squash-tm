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
require(["common"], function() {
	require(["jquery", "app/ws/squashtm.workspace", "jquery.squash.confirmdialog"], function($) {
		"use strict";
		$.ajaxPrefilter(function (options, originalOptions, jqXHR) {
			var token = $("meta[name='_csrf']").attr("content");
			var header = $("meta[name='_csrf_header']").attr("content");
			jqXHR.setRequestHeader(header, token);
		});

		$(function() {

			var warningPopup = $('#clean-automated-suites-popup');

			warningPopup.confirmDialog();

			warningPopup.on('confirmdialogconfirm', function () {
				$.ajax({
					url: squashtm.appRoot + "administration/cleaning",
					method: 'POST'
				});
			});

			warningPopup.on('confirmdialogcancel', function () {
				warningPopup.confirmDialog('close');
			});

			$("#delete-automated-suites-and-executions").on("click", function() {
				$.ajax({
					url: squashtm.appRoot + "administration/cleaning/count",
					method: 'GET'
				}).then(function(automationDeletionCount) {
					var suiteCount = automationDeletionCount.oldAutomatedSuiteCount;
					var execCount = automationDeletionCount.oldAutomatedExecutionCount;
					$('#automated-suites-count').text(suiteCount);
					$('#automated-executions-count').text(execCount);
				});
				warningPopup.confirmDialog('open');
			});

		});
	});
});
