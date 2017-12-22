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
/*
 * configuration an object as follow :
 *
 * {
 *		permissions : {
 *			editable : boolean, is the table content editable ?
 *			executable : boolean, can the content be executed ?
 *			reorderable : boolean, can the user reorder the content ?
 *		},
 *		basic : {
 *			iterationId : the id of the current iteration
 *			assignableUsers : [ { 'id' : id, 'login' : login } ]
 *		},
 *		messages : {
 *			executionStatus : {
 *				UNTESTABLE : i18n label,
 *				SETTLED : i18n label,
 *				BLOCKED : i18n label,
 *				FAILURE : i18n label,
 *				SUCCESS : i18n label,
 *				RUNNING : i18n label,
 *				READY : i18n label,
 *			},
 *			automatedExecutionTooltip : i18n label,
 *			labelOk : i18n label,
 *			labelCancel : i18n label,
 *			titleInfo : i18n label,
 *			messageNoAutoexecFound : i18n label
 *		},
 *		urls : {
 *			testplanUrl : base urls for test plan items,
 *			executionsUrl : base urls for executions
 *		}
 *	}
 *
 */

define(
	['jquery', 'squash.translator', '../../test-plan-panel/exec-runner', '../../test-plan-panel/sortmode', '../../test-plan-panel/filtermode',
		'squash.dateutils', 'squash.statusfactory',
		'test-automation/automated-suite-overview',
		'squash.configmanager', 'workspace.routing', "workspace.event-bus",
		'squashtable', 'jeditable', 'jquery.squash.buttonmenu'],
	function ($, translator, execrunner, smode, fmode, dateutils, statusfactory, autosuitedialog, confman, routing,eventBus) {
		"use strict";

		// ****************** TABLE CONFIGURATION **************

		function _rowCallbackReadFeatures($row, data, _conf) {

			// style for deleted test case rows
			if (data['is-tc-deleted'] === "true") {
				$row.addClass('test-case-deleted');
			}

			// execution mode icon
			var $exectd = $row.find('.exec-mode').text('');
			if (data['exec-mode'] === "M") {
				$exectd.append('<span class"exec-mode-icon exec-mode-manual"/>').attr('title', '');
			} else {
				$exectd.append('<span class="exec-mode-icon exec-mode-automated"/>').attr('title',
					_conf.autoexecutionTooltip);
			}

			// execution status (read, thus selected using .status-display or .status-display-short depending on the style we want)
			var status = data.status;
			var html;
			var $statustd = $row.find('.status-display');
			if ($statustd.is('.status-display-short')) {
				html = statusfactory.getIconFor(status);
			}
			else {
				html = statusfactory.getHtmlFor(status);
			}


			$statustd.html(html); // remember : this will insert a <span>
			// in the process

			//execution date
			var date = data['last-exec-on'],
				iterid = _conf.iterationId,
				execExist = data['exec-exists'],
				tpid = data['entity-id'],
				format = translator.get('squashtm.dateformat');

			if (!!date && !!execExist) {
				var exTxt = dateutils.format(date, format),
					exRef = routing.buildURL('iterations.testplan.lastexec', iterid, tpid);
				var exLnk = $('<a>', {'text': exTxt, 'href': exRef});
				$row.find('.exec-on').empty().append(exLnk);
			} else {
				$row.find('.exec-on').empty().text('-');
			}

			// assignee (read)
			var $assigneetd = $row.find('.assignee-combo');

			$assigneetd.wrapInner('<span/>');

			// dataset : we create the 'button' part of a menu, but not actual menu.
			if (data['dataset'].available.length > 0) {
				var $dstd = $row.find('.dataset-combo');
				$dstd.wrapInner('<span />');
			}
		}

		function _rowCallbackWriteFeatures($row, data, _conf) {

			// execution status (edit, thus selected as .status-combo).
			// Note : the children().first() thing
			// will return the span element.
			var statusurl = _conf.testplanUrl + data['entity-id'];
			var statusElt = $row.find('.status-combo').children().first();
			statusElt.addClass('cursor-arrow');
			statusElt.editable(
				statusurl, {
					type: 'select',
					data: _conf.jsonStatuses,
					name: 'status',
					onblur: 'cancel',
					callback: _conf.submitStatusClbk
				});

			// assignee (edit). Note : the children().first() thing will
			// return the span element.
			var assigneeurl = _conf.testplanUrl + data['entity-id'];
			var assigneeElt = $row.find('.assignee-combo').children().first();

			// Add to AssignableUsers a possible non assigned admin who did an iteration
			var listWithUnassignedUsers = JSON.parse(_conf.jsonAssignableUsers);

			//Issue 6319. The use strict broke the property assigniation in ""
			if (listWithUnassignedUsers === "") {
				listWithUnassignedUsers = {};
			}

			var property = data['assignee-id'].toString();
			var dataProperty = data['assignee-login'];
			if (listWithUnassignedUsers[property] === undefined) {
				listWithUnassignedUsers[property] = dataProperty;
			}

			var listWithAllUsers = JSON.stringify(listWithUnassignedUsers);

			assigneeElt.addClass('cursor-arrow');
			assigneeElt.editable(
				assigneeurl, {
					type: 'select',
					data: listWithAllUsers,
					name: 'assignee',
					onblur: 'cancel',
					callback: _conf.submitAssigneeClbk
				});

			// datasets : we build here a full menu. Note that the read features
			// already ensured that a <a class="buttonmenu"> exists.
			var $dsspan = $row.find('.dataset-combo').children().first(),
				dsInfos = data['dataset'],
				dsurl = _conf.testplanUrl + data['entity-id'];

			if (dsInfos.available.length > 0) {
				var jeditData = confman.toJeditableSelectFormat(dsInfos.available);
				$dsspan.addClass('cursor-arrow');
				$dsspan.editable(dsurl, {
					type: 'select',
					data: jeditData,
					name: 'dataset',
					onblur: 'cancel',
					callback: function (value, settings) {
						$(this).html(settings.data[value]);
					}
				});
			}
		}

		function _rowCallbackExecFeatures($row, data, _conf) {

			// add the execute shortcut menu
			var isTcDel = data['is-tc-deleted'],
				isManual = (data['exec-mode'] === "M");

			var tpId = data['entity-id'],
				$td = $row.find('.execute-button'),
				strmenu = $("#shortcut-exec-menu-template").html()
					.replace(/#placeholder-tpid#/g, tpId);

			$td.empty();
			$td.append(strmenu);

			// if the test case is deleted : just disable the whole thing
			if (isTcDel) {
				$td.find('.execute-arrow').addClass('disabled-transparent');
			}

			// if the test case is manual : configure a button menu,
			// althgouh we don't want it
			// to be skinned as a regular jquery button
			else if (isManual) {
				$td.find('.buttonmenu').buttonmenu({
					anchor: "right"
				});
				$td.on('click', '.run-menu-item', _conf.manualHandler);
			}

			// if the test case is automated : just configure the button
			else {
				$td.find('.execute-arrow').click(_conf.automatedHandler);
			}

		}

		function createTableConfiguration(initconf) {

			// conf objects for the row callbacks
			var _readFeaturesConf = {
				statuses: initconf.messages.executionStatus,
				autoexecutionTooltip: initconf.messages.automatedExecutionTooltip,
				iterationId: initconf.basic.iterationId
			};

			var _writeFeaturesConf = {

				testplanUrl: initconf.urls.testplanUrl,

				jsonStatuses: JSON.stringify(initconf.basic.statuses),

				submitStatusClbk: function (json, settings) {

					// must update the execution status, the execution date and the assignee
					var itp = JSON.parse(json);

					// 1/ the status
					var $statusspan = $(this),
						statuses = JSON.parse(settings.data);

					$statusspan.attr('class', 'cursor-arrow exec-status-label exec-status-' + itp.executionStatus.toLowerCase());
					$statusspan.html(statuses[itp.executionStatus]);

					// 2/ the date format
					var format = translator.get('squashtm.dateformat'),
						$execon = $statusspan.parents('tr:first').find("td.exec-on");

					var newdate = dateutils.format(itp.lastExecutedOn, format);
					$execon.text(newdate);

					// 3/ user assigned
					$statusspan.parents('tr:first')
						.find('td.assignee-combo')
						.children().first().text(itp.assignee);

					//update the tree
					eventBus.trigger('iteration.itpi-execution-status-modified');
				},


				jsonAssignableUsers: JSON.stringify(initconf.basic.assignableUsers),

				submitAssigneeClbk: function (value, settings) {
					var assignableUsers = JSON.parse(settings.data);
					$(this).text(assignableUsers[value]);
				}
			};

			var _execFeaturesConf = {

				manualHandler: function () {

					var $this = $(this),
						tpid = $this.data('tpid'),
						ui = ($this.is('.run-popup')) ? "popup" : "oer",
						newurl = initconf.urls.testplanUrl + tpid + '/executions/new';

					$.post(newurl, {
						mode: 'manual'
					}, 'json').done(function (execId) {
						var execurl = initconf.urls.executionsUrl + execId + '/runner';
						if (ui === "popup") {
							execrunner.runInPopup(execurl);
						} else {
							execrunner.runInOER(execurl);
						}

					});
				},

				automatedHandler: function () {
					var row = $(this).parents('tr').get(0);
					var table = $("#iteration-test-plans-table").squashTable();
					var data = table.fnGetData(row);

					var tpiIds = [];
					var tpiId = data['entity-id'];
					tpiIds.push(tpiId);

					var url = window.squashtm.app.contextRoot + "/automated-suites/new";

					var formParams = {};
					var ent = window.squashtm.page.identity.restype === "iterations" ? "iterationId" : "testSuiteId";
					formParams[ent] = window.squashtm.page.identity.resid;
					formParams.testPlanItemsIds = tpiIds;

					$.ajax({
						url: url,
						dataType: 'json',
						type: 'post',
						data: formParams,
						contentType: "application/x-www-form-urlencoded;charset=UTF-8"
					}).done(function (suite) {
						window.squashtm.context.autosuiteOverview.start(suite);
					});
					return false;

				}
			};

			// basic table configuration. Much of it is in the DOM of the
			// table.
			var tableSettings = {

				bFilter: true,

				fnPreDrawCallback: function (settings) {

					/*
					 * The column dataset.selected.name is visible if :
					 * 1/ the dataset column is being filtered (we want to see the filter) or
					 * 2/ at least one row contains a non empty dataset
					 *
					 */
					var alldata = this.fnGetData();

					var dsFilterOn = this.data('filtermode').isFiltering('dataset.selected.name'),
						rowsHavingDataset = $.grep(alldata, function (model) {
							return model.dataset.available.length !== 0;
						});


					var dsColIdx = this.getColumnIndexByName('dataset.selected.name'),
						dsColVis = (dsFilterOn || rowsHavingDataset.length !== 0);


					this.fnSetColumnVis(dsColIdx, dsColVis, false);
					this.data('showDatasets', dsColVis);

				},


				fnRowCallback: function (row, data, displayIndex) {

					var $row = $(row);

					// add read-only mode features (always applied)
					_rowCallbackReadFeatures($row, data, _readFeaturesConf);

					// add edit-mode features
					if (initconf.permissions.editable) {
						_rowCallbackWriteFeatures($row, data,
							_writeFeaturesConf);
					}

					// add execute-mode features
					if (initconf.permissions.executable) {
						_rowCallbackExecFeatures($row, data,
							_execFeaturesConf);
					}

					// done
					return row;
				},

				fnDrawCallback: function () {
					// make all <select> elements autosubmit on selection
					// change.
					this.on('change', 'select', function () {
						$(this).submit();
					});

					// update the sort mode
					this.data('sortmode').update();
				}
			};

			var squashSettings = {

				buttons: [{
					tdSelector: '>tbody>tr>td.unbind-or-delete',
					jquery: true,
					tooltip: translator.get('dialog.unbind-testcase.tooltip'),
					uiIcon: function (row, data) {
						return (data['last-exec-on'] !== null) ? 'ui-icon-trash' : 'ui-icon-minus';
					},
					/*
					 * the delete button must be drawn if
					 * - the user can delete and the item was not executed or
					 * - the user can extended delete and item was executed
					 */
					condition: function (row, data) {
						return (data['last-exec-on'] === null) ?
							initconf.permissions.deletable :
							initconf.permissions.extendedDeletable;
					},
					onClick: function (table, cell) {
						var dialog = $('#iter-test-plan-delete-dialog');
						var id = table.getODataId($(cell).closest('tr'));
						dialog.data('entity-id', id);
						dialog.formDialog('open');
					}
				}],

				toggleRows: {
					'td.toggle-row': function (table, jqold, jqnew) {

						var data = table.fnGetData(jqold.get(0)),
							url = initconf.urls.testplanUrl + data['entity-id'] + '/executions',
							showDs = table.data('showDatasets');

						jqnew.load(url, function () {

							// styling
							if (!showDs) {
								jqnew.find('.tp-row-dataset').hide();
							}

							// the delete buttons
							if (initconf.permissions.deletable) {
								jqnew.find('.delete-execution-table-button').button({
									text: false,
									icons: {
										primary: "ui-icon-trash"
									}
								}).on('click', function () {
									var dialog = $("#iter-test-plan-delete-execution-dialog");
									dialog.data('origin', this);
									dialog.confirmDialog('open');
								});
							}

							if (initconf.permissions.executable) {
								// the new execution buttons
								jqnew.find('.new-exec').squashButton()
									.on('click', function () {
										var url = $(this).data('new-exec');
										$.post(url, {mode: 'manual'}, 'json')
											.done(function (id) {
												document.location.href = initconf.urls.executionsUrl + id;
											});
										return false;
									});

								jqnew.find('.new-auto-exec').squashButton()
									.on('click', function () {
										var tpiId = $(this).data('tpi-id');

										var formParams = {};
										var idPrmName = window.squashtm.page.identity.restype === "iterations" ? "iterationId" : "testSuiteId";
										formParams[idPrmName] = window.squashtm.page.identity.resid;
										formParams.testPlanItemsIds = [tpiId];


										var url = window.squashtm.app.contextRoot + "/automated-suites/new";
										$.ajax({
											url: url,
											dataType: 'json',
											type: 'post',
											data: formParams,
											contentType: "application/x-www-form-urlencoded;charset=UTF-8"
										}).done(function (suite) {
											window.squashtm.context.autosuiteOverview.start(suite);
										});

										return false;
									});
							}
						});
					}
				}
			};

			// more conf if editable

			if (initconf.permissions.reorderable) {

				squashSettings.enableDnD = true;
				squashSettings.functions = {};
				squashSettings.functions.dropHandler = function (dropData) {
					var ids = dropData.itemIds.join(',');
					var url = initconf.urls.testplanUrl + '/' + ids + '/position/' + dropData.newIndex;
					$.post(url, function () {
						$("#iteration-test-plans-table").squashTable()
							.refresh();
					});
				};

			}

			return {
				tconf: tableSettings,
				sconf: squashSettings
			};

		}

		// **************** MAIN ****************

		return {
			init: function (enhconf) {

				var tableconf = createTableConfiguration(enhconf);

				var sortmode = smode.newInst(enhconf);
				var filtermode = fmode.newInst(enhconf);

				tableconf.tconf.aaSorting = sortmode.loadaaSorting();
				tableconf.tconf.searchCols = filtermode.loadSearchCols();

				var $table = $("#iteration-test-plans-table");

				$table.data('sortmode', sortmode);
				$table.data('filtermode', filtermode);


				var sqtable = $table.squashTable(tableconf.tconf, tableconf.sconf);

				// glue code between the filter and the sort mode
				function toggleSortmode(locked) {
					if (locked) {
						sortmode.disableReorder();
					}
					else {
						sortmode.enableReorder();
					}
				}

				toggleSortmode(filtermode.isFiltering());

				sqtable.toggleFiltering = function () {
					var isFiltering = filtermode.toggleFilter();
					toggleSortmode(isFiltering);
				};


			}
		};

	});
