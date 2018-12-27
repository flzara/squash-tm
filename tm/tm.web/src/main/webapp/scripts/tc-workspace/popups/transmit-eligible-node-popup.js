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
define(['jquery', 'tree', 'workspace.event-bus', 'squash.translator', 'underscore', 'jquery.squash.formdialog'],
	function ($, zetree, eventBus, translator, underscore) {

		function init() {

			var dialog = $("#transmit-eligible-node-dialog").formDialog();

			var tree = zetree.get();

			dialog.on('formdialogopen', function () {
				dialog.formDialog('setState', 'confirm');
			});

			dialog.on('formdialogconfirm', function () {
				var nodes = tree.jstree('get_selected');
				var eligibleNodes = nodes.filter(':test-case[automeligible="y"]');
				var tcIds = [];
				eligibleNodes.each(function(elt) {
					tcIds.push(eligibleNodes[elt].id.replace('TestCase-', ''));
				});
				if (tcIds.length !== 0) {
					$.ajax({
						url : squashtm.app.contextRoot + 'automation-requests/' + tcIds,
						type : 'POST',
						data : {
							'id' : 'automation-request-status',
							'value' : 'TRANSMITTED'
						}
					});
				}
				dialog.formDialog('close');

			});

			dialog.on('formdialogcancel', function () {
				dialog.formDialog('close');
			});

		}


		return {
			init: init
		};

	});
