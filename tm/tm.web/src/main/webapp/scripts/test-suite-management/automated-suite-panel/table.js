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
	['jquery', 'squash.translator', 'squash.statusfactory', 'app/ws/squashtm.notification', 'squashtable'],
	function ($, translator, statusfactory, notification) {
		"use strict";

		function _rowCallbackReadFeatures($row, data, _conf) {

			initExecutionToggle($row, data);

			initReportDisplay($row, data);

		}

		function initExecutionToggle($row, data) {
			var $exectoggle = $row.find('.exec-toggle');
			if (data['has-executions']) {
				$exectoggle.text(translator.get('automated-suite.execution-details'));
			} else {
				$exectoggle.removeClass("toggle-row");
				$exectoggle.text(translator.get('automated-suite.no-execution'));
			}

			var status = data['status'],
				$statustd = $row.find('.status-display'),
				html = statusfactory.getHtmlFor(status);

			$statustd.html(html); // remember : this will insert a <span> in the process
		}

		function initReportDisplay($row, data) {

			var resultURLList = data['result-urls'];

			var hasOneReport = resultURLList !== null && resultURLList.length ===1;
			var hasMultipleReport = resultURLList !== null && resultURLList.length > 1;

			if (hasOneReport) {
				createReportLink($row, resultURLList[0]);
			} else if (hasMultipleReport) {
				createReportListPopUp($row, resultURLList);
			} else {
				$row.find('.result-display').empty().text("/");
			}
		}

		function createReportLink($row, resultUrl) {
			var resultMessage = translator.get('automated-suite.result.label');
			var resultLink = $('<a>', {'text': resultMessage, 'href': resultUrl, 'target': '_blank'});
			$row.find('.result-display').empty().append(resultLink);
		}

		function createReportListPopUp($row, resultUrlList){
			var resultMessage = translator.get('automated-suite.result-list.label');
			var resultLink = $('<a>', {'id': 'result-list', 'text': resultMessage, 'href':''});
			$row.find('.result-display').empty().append(resultLink);

			$row.find('#result-list').click(function(evt){
				evt.preventDefault();
				var title = translator.get('automated-suite.result-list.title');

				var listNode = $('<ul>');
				resultUrlList.forEach(function(url){
					var urlNode = $('<li>').append($('<a>', {'text': url, 'href': url, 'target': '_blank'}));
					listNode.append(urlNode);
				});

				var list = listNode.prop('outerHTML');

				notification.showInfo(title + "\n" + list);
			});
		}

		function createTableConfiguration(initconf) {

			// conf objects for the row callbacks
			var _readFeaturesConf = {
				statuses: initconf.messages.executionStatus,
				testSuiteId: initconf.basic.testsuiteId
			};

			// basic table configuration. Much of it is in the DOM of the
			// table.
			var tableSettings = {

				fnRowCallback: function (row, data, displayIndex) {

					var $row = $(row);

					// add read-only mode features (always applied)
					_rowCallbackReadFeatures($row, data, _readFeaturesConf);

					// done
					return row;
				}
			};

			var squashSettings = {
				toggleRows: {
					'td.toggle-row': function (table, jqold, jqnew) {

						var data = table.fnGetData(jqold.get(0)),
							url = initconf.urls.automatedSuiteUrl + data['uuid'] + '/executions';

						jqnew.load(url, function () {});
					}
				}
			};

			return {
				tconf: tableSettings,
				sconf: squashSettings
			};

		}
		// **************** MAIN ****************

		return {
			init: function (origconf) {
				var $table = $("#iteration-automated-suites-table");
				var tableconf = createTableConfiguration(origconf);
				$table.squashTable(tableconf.tconf,tableconf.sconf);
			}
		};

	});
