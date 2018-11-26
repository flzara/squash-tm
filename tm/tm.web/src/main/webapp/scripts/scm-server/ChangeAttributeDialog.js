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
define(['jquery', 'backbone', 'squash.translator', 'workspace.routing', 'app/util/StringUtil', 'app/lnf/Forms'],
	function($, Backbone, translator, routing, StringUtils, Forms) {

	var ChangeAttributeDialog = Backbone.View.extend({

		el: '#change-attribute-popup',

		input: $('#change-attribute-input'),

		initialize: function(scmRepositoriesTable) {
			let self = this;
			let $el = this.$el

			self.ChangeAttributeDialog = $el.formDialog();
			self.table = scmRepositoriesTable;
			self.mode = null;
			self.repositoryId = null;

			$el.on('formdialogconfirm', function() {
				self.updateAttribute(function() {
					scmRepositoriesTable.refresh();
					$el.formDialog('close');
				});
			});

			$el.on('formdialogcancel', function() {
				$el.formDialog('close');
			});
		},
		/**
		* Prepare the configuration of the dialog for repository path modification.
		*/
		openForPath(event) {
			let self = this;
			let title = translator.get('title.ChangeRepositoryPath');
			let label = translator.get('label.RepositoryPath');

			let tableCell = event.currentTarget;
			let repositoryPath = $(tableCell).text();

			let row = tableCell.parentElement;
			// notify repository ID
			self.repositoryId = self.table.fnGetData(row)['repository-id'];
			// notify mode
			self.mode = 'path';

			self.adaptAndOpenDialog(title, label, repositoryPath);
		},
		/**
		* Prepare the configuration of the dialog for folder path modification.
		*/
		openForFolder(event) {
			let self = this;

			let title = translator.get('title.ChangeWorkingFolderPath');
			let label = translator.get('label.WorkingFolderPath');

			let tableCell = event.currentTarget;
			let folderPath = $(tableCell).text();

			let row = tableCell.parentElement;
			// notify repository ID
			self.repositoryId = self.table.fnGetData(row)['repository-id'];
			// notify mode
			self.mode = 'folder';

			self.adaptAndOpenDialog(title, label, folderPath);
		},
		/**
		* Prepare the configuration of the dialog for folder path modification.
		*/
		openForBranch(event) {
			let self = this;

			let title = translator.get('title.ChangeWorkingBranch');
			let label = translator.get('label.WorkingBranch');

			let tableCell = event.currentTarget;
			let branch = $(tableCell).text();

			let row = tableCell.parentElement;
			// notify repository ID
			self.repositoryId = self.table.fnGetData(row)['repository-id'];
			// notify mode
			self.mode = 'branch';

			self.adaptAndOpenDialog(title, label, branch);
		},
		/**
		* Adapt the dialog title and label according to the attribute being modified.
		* Also fill the only input with the current value of this attribute.
		*/
		adaptAndOpenDialog(title, label, value) {
			let self = this;
			// clear errors
			Forms.input(self.input).clearState();
			// title
			self.ChangeAttributeDialog.prev('.ui-dialog-titlebar').find('.ui-dialog-title').text(title);
			// label
			self.ChangeAttributeDialog.find('#change-attribute-label').text(label);
			// value
			self.input.val(value);
			// open
			self.ChangeAttributeDialog.formDialog('open');
		},
		/**
		* Check the validity of the one-input form. If any error, displays it in the dialog.
		* If everything is valid, update the attribute of the ScmRepository and execute the callback function.
		*/
		updateAttribute(callback) {
			let self = this;

			// clear errors
			Forms.input(self.input).clearState();

			let mode = self.mode;
			let value = self.input.val();
			let repositoryId = self.repositoryId;

			let data = {};
			data[mode] = value;

			if(mode !== 'folder' && StringUtils.isBlank(value)) {
				Forms.input(self.input).setState("error", translator.get("message.notBlank"));
			} else {
				self.doUpdateAttribute(repositoryId, data).success(callback);
			}
		},
		/**
		* Create the Ajax Post Request to update an attribute of the given ScmRepository.
		* Only one attribute can be update with the request. The attribute updated depends on what is contained
		* in the given data. Update supported are: path, folder and branch.
		* @param repositoryId: The Id of the ScmRepository to update.
		* @param data: A one-attribute object with the key (path, folder or branch) and the new value to put.
		* @return The Promise of the Post Request.
		*/
		doUpdateAttribute(repositoryId, data) {
			return $.ajax({
				url: routing.buildURL('administration.scm-repositories', repositoryId),
				type: 'POST',
				data: data
			});
		}

	});

	return ChangeAttributeDialog;

});
