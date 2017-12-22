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
define(["jquery", "app/ws/squashtm.notification", "squash.translator", "jquery.squash.formdialog"], function ($, notification, translator) {
	"use strict";
	/**
	 * Initializes a batch assignment dialog, to be used in a test plan view
	 * options are :
	 * {
	 *   el : the jquery selector for the dialog
	 *   urlRoot : the url to which the app should send a post request after appending the test plan item ids
	 * }
	 *
	 * @param options { el, urlRoot }
	 * @constructor
	 */
	function BatchAssignUsersDialog(options) {
		var batchAssignUsersDialog = $(options.el);

		batchAssignUsersDialog.formDialog();

		batchAssignUsersDialog.on("formdialogopen", function () {
			var selIds = $(".test-plan-table").squashTable().getSelectedIds();

			if (selIds.length === 0) {
				$(this).formDialog("close");
				notification.showError(translator.get("message.EmptyExecPlanSelection"));
			} else {
				$(this).formDialog("setState", "assign");
			}

		});

		batchAssignUsersDialog.on("formdialogconfirm", function () {

			var table = $(".test-plan-table").squashTable(), select = $(".batch-select", this);

			var rowIds = table.getSelectedIds(), assigneeId = select.val(), assigneeLogin = select.find(
				"option:selected").text();

			var url = options.urlRoot + rowIds.join(",");

			$.post(url, {
				assignee: assigneeId
			}, function () {
				table.getSelectedRows().find("td.assignee-combo span").text(assigneeLogin);
			});

			$(this).formDialog("close");
		});

		batchAssignUsersDialog.on("formdialogcancel", function () {
			$(this).formDialog("close");
		});
	}

	return BatchAssignUsersDialog;
});
