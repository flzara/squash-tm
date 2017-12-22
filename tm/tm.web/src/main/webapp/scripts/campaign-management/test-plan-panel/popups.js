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
define(['jquery', 'underscore', 'handlebars', 'squash.translator', 'app/ws/squashtm.notification', 'test-plan-management/BatchAssignUsersDialog', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog'],
	function ($, _, Handlebars, translator, notification, BatchAssignUsersDialog) {
		"use strict";

		function _initBatchAssignUsers(conf) {
			new BatchAssignUsersDialog({el: "#camp-test-plan-batch-assign", urlRoot: conf.urls.testplanUrl});
		}

		function _initReorderTestPlan(conf) {
			var dialog = $("#camp-test-plan-reorder-dialog");

			dialog.confirmDialog();

			dialog.on('confirmdialogconfirm', function () {
				var table = $("#campaign-test-plans-table").squashTable();
				var drawParameters = table.getAjaxParameters();

				var url = conf.urls.testplanUrl + '/order';
				$.post(url, drawParameters, 'json')
					.success(function () {
						table.data('sortmode').resetTableOrder(table);
						table.refresh();
					});
			});

			dialog.on('confirmdialogcancel', closeDialog);
		}

		/**
		 * Creates an unbind function with the given configuration
		 * @param conf the configuration for the created function
		 *
		 * @return function which posts an unbind request for the given id(s)
		 * @param ids either an id or an array of ids
		 * @return a promise (the xhr's)
		 */
		function unbindCallback(conf) {
			return function (ids) {
				var url = conf.urls.testplanUrl + (_.isArray(ids) ? ids.join(',') : ids);

				return $.ajax({
					url: url,
					type: 'DELETE',
					dataType: 'json'
				});
			};
		}

		function dialogSucceed(self) {
			return function () {
				$("#campaign-test-plans-table").squashTable().refresh();
				$(self).formDialog('close');
			};
		}

		function closeDialog() {
			/*jshint validthis: true */
			$(this).formDialog('close');
		}

		function _initBatchRemove(conf) {
			// 1. we prepare the dom so that it meets our future requirements
			var tpl = Handlebars.compile($("#delete-dialog-tpl").html());
			var dlgs = tpl({dialogId: "delete-multiple-test-cases-dialog"}) + tpl({dialogId: "unbind-test-case-dialog"});
			$("body").append(dlgs);
			var unbind = unbindCallback(conf);

			// 2.
			var $batch = $("#delete-multiple-test-cases-dialog");
			$batch.formDialog();
			$batch.on("formdialogopen", function () {
				// read the ids from the table selection
				var ids = $("#campaign-test-plans-table").squashTable().getSelectedIds();

				if (ids.length === 0) {
					$(this).formDialog('close');
					notification.showError(translator.get('message.EmptyExecPlanSelection'));

				} else if (ids.length === 1) {
					$(this).formDialog("setState", "confirm-deletion");

				} else {
					$(this).formDialog("setState", "multiple-tp");
				}
			});

			var batchSucceed = dialogSucceed($batch);
			$batch.on('formdialogconfirm', function () {
				var ids = $("#campaign-test-plans-table").squashTable().getSelectedIds();
				if (ids.length > 0) {
					unbind(ids).done(batchSucceed);
				}
			});

			$batch.on('formdialogcancel', function () {
				$(this).formDialog('close');
			});

			var $single = $("#unbind-test-case-dialog");
			$single.formDialog();

			$single.on("formdialogopen", function () {
				var id = $(this).data('entity-id');

				if (id === undefined) {
					$(this).formDialog('close');
					notification.showError(translator.get('message.EmptyExecPlanSelection'));
				} else {
					$(this).formDialog("setState", "confirm-deletion");
				}
			});

			// 3.
			var singleSucceed = dialogSucceed($single);
			$single.on('formdialogconfirm', function () {
				var self = this;
				var id = $(this).data('entity-id');

				if (id !== undefined) {
					unbind(id).done(function () {
						singleSucceed();
						$(self).data('entity-id', null);
					});
				}
			});

			$single.on('formdialogcancel', function () {
				$(this).formDialog('close');
				$(this).data('entity-id', null);
			});
		}

		return {
			init: function (conf) {
				if (conf.features.editable) {
					_initBatchAssignUsers(conf);
				}
				if (conf.features.reorderable) {
					_initReorderTestPlan(conf);
				}
				if (conf.features.linkable) {
					_initBatchRemove(conf);
				}
			}
		};

	});
