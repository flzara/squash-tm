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
define(["jquery", "app/util/ComponentUtil", "squash.statusfactory", "app/ws/squashtm.notification", "squash.translator", "squash.dateutils", "jquery.squash.formdialog"], function ($, ComponentUtil, statusfactory, notification, translator, dateutils) {
	"use strict";
	/**
	 * Initializes a batch status editor dialog, to be used in a test plan view
	 * options are :
	 * {
	 *   el : the jquery selector for the dialog
	 *   urlRoot : the url to which the app should send a post request after appending the test plan item ids
	 * }
	 *
	 * @param options { el, urlRoot }
	 * @constructor
	 */
	function BatchEditStatusDialog(options) {

		var batchEditStatusDialog = $(options.el);
		batchEditStatusDialog.formDialog();

		var cbox = batchEditStatusDialog.find(".execution-status-combo-class");

		ComponentUtil.updateStatusCboxIconOnChange(cbox);

		batchEditStatusDialog.on("formdialogopen", function () {
			var selIds = $(".test-plan-table").squashTable().getSelectedIds();
			var cbox = $(this).find(".execution-status-combo-class");

			ComponentUtil.updateStatusCboxIcon(cbox);

			if (selIds.length === 0) {
				$(this).formDialog("close");
				notification.showError(translator.get("message.EmptyExecPlanSelection"));
			} else {
				$(this).formDialog("setState", "edit");
			}

		});

		batchEditStatusDialog.on("formdialogconfirm", function () {

			var table = $(".test-plan-table").squashTable(), select = $(".execution-status-combo-class",
				this);

			var rowIds = table.getSelectedIds(), statusCode = select.val();

			var url = options.urlRoot + rowIds.join(",");

			$.post(url, {
				status: statusCode
			}, function (itp) {

				// must update the execution status, the execution date and the assignee

				// 1/ the status
				var $statusspans = table.getSelectedRows().find("td.status-combo span");
				for (var i = 0; i < $statusspans.length; i++) {
					var $statusspan = $($statusspans[i]);
					$statusspan.attr("class", "cursor-arrow exec-status-label exec-status-" +
						itp.executionStatus.toLowerCase());
					$statusspan.html(statusfactory.translate(itp.executionStatus));

					// 2/ the date format
					var format = translator.get("squashtm.dateformat"), $execon = $statusspan.parents("tr:first").find(
						"td.exec-on");

					var newdate = dateutils.format(itp.lastExecutedOn, format);
					$execon.text(newdate);

					// 3/ user assigned
					$statusspan.parents("tr:first").find("td.assignee-combo").children().first().text(itp.assignee);
				}
			});

			$(this).formDialog("close");
		});

		batchEditStatusDialog.on("formdialogcancel", function () {
			$(this).formDialog("close");
		});

	}

	return BatchEditStatusDialog;
});
