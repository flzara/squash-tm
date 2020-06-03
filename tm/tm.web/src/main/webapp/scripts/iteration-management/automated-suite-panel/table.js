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
	['jquery', 'squash.statusfactory', 'squashtable'],
	function ($, statusfactory) {
		"use strict";

		function _rowCallbackReadFeatures($row, data, _conf) {

			// execution status (read, thus selected using .status-display)
			var status = data['status'],
				$statustd = $row.find('.status-display'),
				html = statusfactory.getHtmlFor(status);

			$statustd.html(html); // remember : this will insert a <span>
			// in the process
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
