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
define(['jquery', 'workspace.event-bus', 'jqueryui', 'jquery.squash.confirmdialog',], function ($, eventBus) {
	'use strict';

	/**
	 * Initializes an execution deletion dialog, to be used in a test plan view
	 * options are :
	 * {
	 *   el : the jquery selector for the dialog
	 *   urlRoot : the url to which the app should send a delete request after  appending the execution id
	 * }
	 *
	 * @param options { el, urlRoot }
	 * @constructor
	 */
	function DeleteExecutionDialog(options) {
		var deleteExecutionDialog = $(options.el);

		deleteExecutionDialog.confirmDialog();

		deleteExecutionDialog.on('confirmdialogconfirm', function () {
			var execId = $(this).data('origin').id.substr('delete-execution-table-button-'.length);

			$.ajax({
				url: options.urlRoot + execId,
				type: 'DELETE',
				dataType: 'json'
			}).done(function (data) {
				if (config.identity.restype==="test-suites") {
					squashtm.execution.refreshTestSuiteInfo();
				}
				eventBus.trigger('context.content-modified', {
					newDates: data
				});

			});
		});
	}

	return DeleteExecutionDialog;
});
