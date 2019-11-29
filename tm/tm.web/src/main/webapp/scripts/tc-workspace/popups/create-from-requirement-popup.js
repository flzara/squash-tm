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
define(['jquery', 'tree', 'workspace.event-bus', '../permissions-rules', "workspace.tree-node-copier", 'jquery.squash.formdialog'],
	function ($, zetree, eventBus, rules, copier) {

		function init() {

			var dialog = $("#create-from-requirement-dialog").formDialog();

			var tree = zetree.get();

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


		return {
			init: init
		};

	});
