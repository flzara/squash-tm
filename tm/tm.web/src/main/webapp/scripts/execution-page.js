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
define(['module', 'jquery', 'app/pubsub', 'squash.basicwidgets', 'app/ws/squashtm.workspace', 'squash.translator', 'app/ws/squashtm.notification', 'workspace.routing',
		'page-components/execution-information-panel', 'file-upload', 'custom-field-values', 'bugtracker/bugtracker-panel', 'squash.statusfactory', 'squash.dateutils',
		'jqueryui', 'jeditable.ckeditor', 'jquery.squash', 'jquery.squash.formdialog', 'jquery.squash.togglepanel'],
	function (module, $, pubsub, basicwidg, WS, translator, notification, routing, infopanel, upload, cufValuesManager, bugtracker, statusfactory, dateutils) {

		squashtm.execution = squashtm.execution || {};

		// event subscription
		pubsub.subscribe('reload.executions.toolbar', initToolbar);

		pubsub.subscribe('reload.executions.stepstable', initStepstable);

		pubsub.subscribe('reload.executions.bugtracker', initBugtracker);

		pubsub.subscribe('reload.executions.dialogs', initDialogs);

		pubsub.subscribe('reload.executions.attachments', initAttachments);

		pubsub.subscribe('reload.executions.complete', initFinalize);


		// ************** library *********************************

		function initToolbar() {

			// config
			var config = module.config();

			// the info panel
			infopanel.init();

			// the execute-execution button
			squashtm.execution.updateBtnlabelFromTable = function () {

				// Issue 2961
				// 99.9% of the time we want the btn to display "resume" when statuses are updated
				// because an execution rarely walks back to 'ready' status, so I'll be lazy here

				var btnlang = translator.get({
					resume: 'execution.execute.resume.button.label',
					resumeOER: 'execution.execute.IEO.resume.button.label'
				});

				$("#execute-execution-button").val(btnlang.resume);
				$("#ieo-execution-button").val(btnlang.resumeOER);

			}

			var runnerUrl = routing.buildURL('executions.runner', config.basic.executionId);
			var dryRunStart = function () {
				return $.ajax({
					url: runnerUrl,
					method: 'get',
					data: {
						'dry-run': ''
					}
				});
			};

			var startResumeClassic = function () {
				var url = runnerUrl + '?optimized=false';
				window.open(url, "classicExecutionRunner", "height=500, width=500, resizable, scrollbars, dialog, alwaysRaised");
			};

			var startResumeOptimized = function () {
				var url = runnerUrl + '?optimized=true&suitemode=false';
				var win = window.open(url, "_blank");
				win.focus();
			};

			$("#execute-execution-button").button().click(function () {
				dryRunStart().done(startResumeClassic);
			});

			$("#ieo-execution-button").button().click(function () {
				dryRunStart().done(startResumeOptimized);
			});


			// history

			$("#back").on('click', function () {
				history.back();
			});

			// cufs

			var displayMode = (config.permissions.editable) ? 'jeditable' : 'static';
			cufValuesManager.infoSupport.init("#test-case-attribut-table", config.basic.cufs.denoCufs, displayMode);
			cufValuesManager.infoSupport.init("#test-case-attribut-table", config.basic.cufs.normal, displayMode);

		}


		function initAttachments() {
			var config = module.config();
			upload.initAttachmentsBloc({
				baseURL: config.urls.attachmentsURL,
				workspace: "campaign"
			});
		}


		function initStepstable() {

			var config = module.config();

			var executionId = config.basic.executionId,
				executionURL = routing.buildURL('executions', executionId),
				stepsUrl = routing.buildURL('executions.steps', executionId),
				colDefs = config.basic.stepstable.colDefs,
				cufDefs = config.basic.stepstable.cufDefs,
				table = $("#execution-execution-steps-table");

			var _writeFeaturesConf = {

				submitStatusClbk: function (json, settings) {

					// must update the execution status, the execution date and the assignee
					var executionStep = JSON.parse(json);

					// 1/ the status
					var $statusspan = $(this);

					$statusspan.attr('class', 'cursor-arrow exec-status-label exec-status-' + executionStep.executionStatus.toLowerCase());
					$statusspan.html(config.basic.statuses[executionStep.executionStatus]);

					// 2/ the date format
					var format = translator.get('squashtm.dateformat'),
						$execon = $statusspan.parents('tr:first').find("td.exec-on");

					if (executionStep.executedOn === null) {
						$execon.text("-");
					} else {
						var newdate = dateutils.format(executionStep.executedOn, format);
						$execon.text(newdate);
					}

					// 3/ user assigned
					$statusspan.parents('tr:first')
						.find('td.assignee-combo')
						.text(executionStep.executedBy);

					infopanel.refresh();
				}
			};

			function _rowCallbackReadFeatures($row, data, _conf) {
				var status = data.status;
				var html;
				var $statustd = $row.find('.status-combo');
				if ($statustd.is('.status-display-short')) {
					html = statusfactory.getIconFor(status);
				}
				else {
					html = statusfactory.getHtmlFor(status);
				}

				$statustd.html(html);
			}

			function _rowCallbackEditableStatus($row, data, _conf) {
				// execution status (edit, thus selected as .status-combo).
				// Note : the children().first() thing
				// will return the span element.
				var statusUrl = stepsUrl + "/" + data['entity-id'];
				var statusElt = $row.find('.status-combo').children().first();
				statusElt.addClass('cursor-arrow');
				statusElt.editable(
					statusUrl, {
						type: 'select',
						data: JSON.stringify(config.basic.statuses),
						name: 'status',
						onblur: 'cancel',
						callback: _conf.submitStatusClbk
					});
			}

			var tableSettings = {
				sAjaxSource: stepsUrl,
				aoColumnDefs: colDefs,
				cufDefinitions: cufDefs,
				fnRowCallback: function (row, data, displayIndex) {
					var $row = $(row);

					_rowCallbackReadFeatures($row, data);

					if (config.permissions.editable) {
						_rowCallbackEditableStatus($row, data, _writeFeaturesConf);
					}
					return row;
				},

				fnDrawCallback: function () {
					// make all <select> elements autosubmit on selection
					// change.
					this.on('change', 'select', function () {
						$(this).submit();
					});

				}
			};


			// the cufs
			var cufColumnPosition = 2;
			var cufTableHandler = cufValuesManager.cufTableSupport;
			cufTableHandler.decorateDOMTable(table, cufDefs, cufColumnPosition);

			datatableSettings = cufTableHandler.decorateTableSettings(tableSettings, tableSettings.cufDefinitions, cufColumnPosition, true);


			// features that are always active
			var squashSettings = {
				enableHover: true,
				buttons: [
					// the 'execute' button
					{
						tdSelector: 'td.run-step-button',
						uiIcon: 'execute-arrow',
						tooltip: translator.get('label.run'),
						onClick: function (table, cell) {
							var row = cell.parentNode.parentNode;
							var executionStepId = table.getODataId(row);

							var url = routing.buildURL('execute.stepbyid', executionId, executionStepId);
							url += '?optimized=false';

							window.open(url, "classicExecutionRunner", "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised");
						}
					},

					// the 'report bug' button
					{
						tdSelector: 'td.bug-button',
						uiIcon: function (row, data) {
							return (data['bug-list'].length > 0) ? "has-bugs" : "table-cell-add";
						},

						onClick: function (table, btnElt) {

							var row = btnElt.parentNode.parentNode;
							var executionStepId = table.getODataId(row);

							// TODO : yes, that function is defined in the global scope in bugtracker-panel.jsp,
							// refactoring would be good here too
							// perhaps module serverauth/auth-manager would help for that
							checkAndReportIssue({
								reportUrl: routing.buildURL('bugtracker.execsteps.new', executionStepId),
								callback: function (json) {
									var btn = $(btnElt);
									btn.removeClass('table-cell-add')
										.addClass('has-bugs');
									issueReportSuccess(json);
								}
							});
						}
					}
				]
			};

			// features requiring editable permission
			if (config.permissions.editable) {

				$.extend(squashSettings, {

					richEditables: {
						'rich-editable-comment': stepsUrl + '/{entity-id}/comment'
					},

					attachments: {
						/*
					   * pay attention to the line below : we're building an URL
					   * where the 'id' placeholder is replaced by another placeholder,
					   * that the datatable will be able to handle
					   */
						url: routing.buildURL('attachments.manager', '{attach-list-id}') + '?workspace=campaign'
					}
				});

			}

			// refreshing table content while the execution is processed
			squashtm.execution.refresh = $.proxy(function () {
				$("#execution-execution-steps-table").squashTable().refresh();
				infopanel.refresh();
				//see execution-execute-button.tag
				squashtm.execution.updateBtnlabelFromTable();
			}, window);

			// now we go
			table.squashTable(tableSettings, squashSettings);

		}

		function initBugtracker() {
			var config = module.config();

			if (config.basic.hasBugtracker) {
				var conf = {
					url: routing.buildURL('bugtracker.execution', config.basic.executionId)
				}
				bugtracker.setBugtrackerMode(config.basic.bugtrackerMode);
				bugtracker.load(conf);
			}
		}


		function initDialogs() {


			var config = module.config();
			var executionURL = routing.buildURL('executions', config.basic.executionId);

			// the delete dialog

			var deldialog = $("#delete-execution-dialog");
			deldialog.formDialog();

			deldialog.on('formdialogconfirm', function () {
				$.ajax({
					url: executionURL,
					type: 'DELETE',
				}).success(function () {
					deldialog.formDialog('close');
					history.back();
				}).error(function (xhr) {
					notification.showError(xhr.statusText);
				});
			});

			deldialog.on('formdialogcancel', function () {
				deldialog.formDialog('close');
			});

			$('#delete-execution-button').on('click', function () {
				deldialog.formDialog('open');
			});

		}


		function initFinalize() {
			WS.init();
			basicwidg.init();
		}

	});
