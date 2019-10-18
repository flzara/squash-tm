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
define(['jquery', 'tree', 'workspace.event-bus', '../permissions-rules', "workspace.tree-node-copier", 'squash.translator', 'jquery.squash.formdialog'],
	function ($, zetree, eventBus, rules, copier, translator) {

		function init() {

			var dialog = $("#create-from-requirement-dialog").formDialog();

			var tree = zetree.get();

			dialog.on('formdialogopen', function(){
				populateFormatSelect(dialog);
			});

			dialog.on('formdialogconfirm', function () {
				pasteFromReqToTcIfOk(tree);
				dialog.formDialog('close');
			});

			dialog.on('formdialogcancel', function () {
				dialog.formDialog('close');
			});

		}

		function pasteFromReqToTcIfOk(tree) {
			var tcKind = $('#create-from-requirement-format').val();
			var configuration = {
				tcKind: tcKind
			};

			var nodes = tree.jstree('get_selected');
			if (rules.CantCreateTcFromReq(nodes)) {
				copier.pasteNodesForTcFromCookie(configuration);
			} else {
				var why = rules.whyCantCreateTcFromReq(nodes);
				showError(why);
			}
		}

		function populateFormatSelect(dialog) {
			var formatList = ["STANDARD", dialog.data('test-case-script-language')];

			var testFormatSelect = dialog.find("#create-from-requirement-format");
			testFormatSelect.empty();

			formatList.forEach(function(format) {
				createOption(testFormatSelect, format);
			});

		}

		function createOption(testFormatSelect, tcFormat) {

			var i18nFormat = translator.get({
				"GHERKIN": "test-case.format.gherkin",
				"STANDARD": "test-case.format.standard",
				"ROBOT": "test-case.format.robot"
			});
			var formatOption = $("<option></option>").val(tcFormat);
			var formatLabel = $("<span></span>").text(i18nFormat[tcFormat]);
			formatOption.append(formatLabel);
			testFormatSelect.append(formatOption);
		}


		return {
			init: init
		};

	});
