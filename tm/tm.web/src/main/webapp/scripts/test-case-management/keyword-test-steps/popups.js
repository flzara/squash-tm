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
define(['jquery', 'workspace.event-bus', 'squash.translator', 'underscore', 'jqueryui', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog'],
	function ($, eventBus, translator, _) {


		function _initDeleteStep(conf) {

			var deleteStepDialog = $("#delete-keyword-test-step-dialog");

			deleteStepDialog.formDialog();

			deleteStepDialog.on('formdialogopen', function () {

				var entityId = $("#delete-keyword-test-step-dialog").data("entity-id");
				$("#delete-keyword-test-step-dialog").data("entity-id", null);

				var selIds = [];

				if (!entityId) {
					selIds = $("#keyword-test-step-table-" + conf.testCaseId).squashTable().getSelectedIds();
				}

				if (!!entityId) {
					selIds.push(entityId);
				}

				switch (selIds.length) {
					case 0 :
						$(this).formDialog('setState', 'empty-selec');
						break;
					case 1 :
						$(this).formDialog('setState', 'single-tp');
						break;
					default :
						$(this).formDialog('setState', 'multiple-tp');
						break;
				}

				this.selIds = selIds;
			});

			deleteStepDialog.on('formdialogconfirm', function () {
				var table = $("#delete-keyword-test-step-dialog" + conf.testCaseId).squashTable();
				var ids = this.selIds;
				var url = conf.urls.testCaseStepsUrl + "/" + ids.join(',');

				$.ajax({
					url: url,
					type: 'delete',
					dataType: 'json'
				})
					.done(function (testStepsSize) {
						conf.stepsTablePanel.refresh();
						if (testStepsSize == "0") {
							eventBus.trigger("testStepsTable.noMoreSteps");
						}
					});

				$(this).formDialog('close');
			});

			deleteStepDialog.on('formdialogcancel', function () {
				$(this).formDialog('close');
			});

		}


		/*
		 * needs :
		 *
		 * conf.permissions.writable
		 * conf.urls.testCaseStepsUrl
		 * conf.testCaseId
		 * conf.stepsTablePanel
		 */
		return {
			init: function (conf) {
				if (conf.permissions.writable) {
					_initDeleteStep(conf);
				}
			}
		};

	});
