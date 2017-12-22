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
define(['jquery', 'workspace.event-bus', 'app/util/ComponentUtil', 'squash.statusfactory', 'squash.translator',
	'squash.dateutils', 'app/ws/squashtm.notification', 'test-plan-management/DeleteExecutionDialog', 'test-plan-management/BatchEditStatusDialog',
	'test-plan-management/BatchAssignUsersDialog', 'jqueryui', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog'], function ($,
	eventBus, ComponentUtil, statusfactory, translator, dateutils, notification, DeleteExecutionDialog, BatchEditStatusDialog, BatchAssignUsersDialog) {

	function _initDeleteExecutionPopup(conf) {
		new DeleteExecutionDialog({el: "#iter-test-plan-delete-execution-dialog", urlRoot: conf.urls.executionsUrl});
	}

	function _initDeleteItemTestplan(conf) {

		var deleteItemTestplanDialog = $("#iter-test-plan-delete-dialog");

		deleteItemTestplanDialog.formDialog();

		deleteItemTestplanDialog.on('formdialogopen', function () {

			var $this = $(this),
				$table = $("#iteration-test-plans-table").squashTable();

			var entityId = $this.data("entity-id");
			$this.data("entity-id", null);

			var selIds = [];

			if (!entityId) {
				selIds = $table.getSelectedIds();
			}

			if (!!entityId) {
				selIds.push(entityId);
			}

			switch (selIds.length) {
				case 0:
					$this.formDialog('close');
					notification.showError(translator.get('message.EmptyExecPlanSelection'));
					break;
				case 1:
					var row = $table.getRowsByIds(selIds)[0];
					var wasexecuted = (!!$table.fnGetData(row)['last-exec-on']);
					if (wasexecuted) {
						$this.formDialog('setState', 'delete-single-tp');
					}
					else {
						$this.formDialog('setState', 'unbind-single-tp');
					}
					break;
				default:
					$this.formDialog('setState', 'multiple-tp');
					break;
			}

			this.selIds = selIds;
		});

		deleteItemTestplanDialog.on('formdialogconfirm', function () {
			var table = $("#iteration-test-plans-table").squashTable();
			var ids = this.selIds;
			var url = conf.urls.testplanUrl + ids.join(',');

			$.ajax({
				url: url,
				type: 'delete',
				dataType: 'json'
			}).done(function (partiallyUnauthorized) {
				/*
				 * When a user can delete a planned test case unless executed,
				 * and that a multiple selection encompassed both cases,
				 * the server performs the operation only on the item it is allowed to.
				 *
				 *  When this happens, the used must be notified.
				 */
				if (partiallyUnauthorized) {
					squashtm.notification.showWarning(conf.messages.unauthorizedTestplanRemoval);
				}
				eventBus.trigger('context.content-modified');
			});

			$(this).formDialog('close');
		});

		deleteItemTestplanDialog.on('formdialogcancel', function () {
			$(this).formDialog('close');
		});

	}

	function _initBatchAssignUsers(conf) {
		new BatchAssignUsersDialog({el: "#iter-test-plan-batch-assign", urlRoot: conf.urls.testplanUrl});
	}

	function _initBatchEditStatus(conf) {
		new BatchEditStatusDialog({el: "#iter-test-plan-batch-edit-status", urlRoot: conf.urls.testplanUrl});
	}

	function _initReorderTestPlan(conf) {
		var dialog = $("#iter-test-plan-reorder-dialog");

		dialog.confirmDialog();

		dialog.on('confirmdialogconfirm', function () {
			var table = $("#iteration-test-plans-table").squashTable();
			var drawParameters = table.getAjaxParameters();

			var url = conf.urls.testplanUrl + '/order';
			$.post(url, drawParameters, 'json').success(function () {
				table.data('sortmode').resetTableOrder(table);
				eventBus.trigger('context.content-modified');
			});
		});

		dialog.on('confirmdialogcancel', function () {
			$(this).confirmDialog('close');
		});
	}

	return {
		init: function (conf) {
			if (conf.permissions.linkable) {
				_initDeleteItemTestplan(conf);
			}
			if (conf.permissions.editable) {
				_initBatchAssignUsers(conf);
				_initBatchEditStatus(conf);
			}
			if (conf.permissions.executable) {
				_initDeleteExecutionPopup(conf);
			}
			if (conf.permissions.reorderable) {
				_initReorderTestPlan(conf);
			}
		}
	};

});
