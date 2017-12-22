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
 * settings : 
 * {
 *	baseURL : the base url where files manipulation occur
 *	aaData : table data (optional)
 *	
 * 
 * }
 * 
 */

define(["jquery", "squash.translator", "app/ws/squashtm.notification", "./jquery.squash.attachmentsDialog",
		"jquery.squash.confirmdialog", "squashtable"],
	function ($, translator, notification) {

		function getMessages() {
			return translator.get({
				nothingSelected: 'message.EmptyTableSelection',
				renameImpossible: 'message.CanRenameOnlyOneAttachment'
			});
		}

		function initDialogs(settings) {

			var table = $("#attachment-detail-table").squashTable();

			// ******** delete dialog init ***********

			var deleteDialog = $("#delete-attachment-dialog");

			deleteDialog.confirmDialog();

			deleteDialog.on('confirmdialogconfirm', function () {

				var removedIds = table.getSelectedIds().join(',');
				var url = settings.baseURL + "/" + removedIds;

				$.ajax({
					type: 'DELETE',
					url: url
				}).done(function () {
					deleteDialog.confirmDialog('close');
					table.refresh();
				});
			});

			// ******* rename dialog init ***************

			var renameDialog = $("#rename-attachment-dialog");

			renameDialog.confirmDialog();

			renameDialog.on("confirmdialogconfirm", function () {

				var id = renameDialog.data("attachmentId");
				var url = settings.baseURL + "/" + id + "/name";
				var newName = $("#rename-attachment-input").val();

				$.ajax({
					url: url,
					type: 'POST',
					data: {name: newName}
				})
					.done(function () {
						renameDialog.confirmDialog('close');
						table.refresh();
					});
			});

			// ******************* upload dialog settings **********

			var uploadDialog = $("#add-attachments-dialog");
			uploadDialog.attachmentsDialog({
				url: settings.baseURL + "/upload"
			});

			uploadDialog.on('attachmentsdialogdone', function () {
				table.refresh();
			});
			
		}


		function initButtons(settings) {

			var table = $("#attachment-detail-table").squashTable();

			var deleteButton = $("#delete-attachment-button");
			var renameButton = $("#rename-attachment-button");
			var uploadButton = $("#add-attachment-button");

			deleteButton.on('click', function () {
				if (table.getSelectedRows().size() > 0) {
					$("#delete-attachment-dialog").confirmDialog('open');
				}
				else {
					var messages = getMessages();
					notification.showError(messages.nothingSelected);
				}
			});


			renameButton.on('click', function () {
				var selectedIds = table.getSelectedIds();
				if (selectedIds.length !== 1) {
					var messages = getMessages();
					notification.showError(messages.renameImpossible);
					return false;
				}
				else {
					var id = selectedIds[0],
						name = table.getDataById(id).name;

					var index = name.lastIndexOf('.');
					$("#rename-attachment-input").val(name.substring(0, index));

					$("#rename-attachment-dialog").data('attachmentId', id)
						.confirmDialog('open');

				}
			});

			
			uploadButton.on('click', function () {
				$("#add-attachments-dialog").attachmentsDialog('open');
			});
			
		}


		function init(settings) {

			var dtSettings = {};
			if (settings.aaData) {
				dtSettings.aaData = settings.aaData;
			}

			// Added parameters there to allow tooltips on buttons
			$("#attachment-detail-table").squashTable(dtSettings, {
				deleteButtons: {
					delegate: "#delete-attachment-dialog",
					tooltip: translator.get('title.RemoveAttachment')
				}
			});

			initDialogs(settings);
			initButtons(settings);

		}


		return {
			init: init
		};

	});
