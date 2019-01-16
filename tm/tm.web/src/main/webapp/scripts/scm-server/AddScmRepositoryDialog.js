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
 define(['jquery', 'backbone', 'underscore', 'app/util/StringUtil', 'squash.translator', 'workspace.routing',
 	'app/lnf/Forms', 'jquery.squash.formdialog', 'jquery.squash.confirmdialog'],
 	function($, Backbone, _, StringUtils, translator, routing, Forms) {
	"use strict";

	var AddScmRepositoryDialog = Backbone.View.extend({

		el: '#add-scm-repository-popup',

		inputs: {
			name: $('#name'),
			path: $('#path'),
			folder: $('#folder'),
			branch: $('#branch'),
		},

		confirmPopup: $("#add-scm-confirm-dialog"),

		errorPopup: $('.error-frame'),

		initialize: function(scmRepositoriesTable) {
			var self = this;

			self.errorPopup.popupError();
			self.confirmPopup.confirmDialog();

			var $el = self.$el;
			$el.formDialog();

			$el.on('formdialogconfirm', function() {
				self.addNewScmRepository(function() {
					scmRepositoriesTable.refresh();
					$el.formDialog('close');
				});
			});

			$el.on('formdialogaddanother', function() {
				self.addNewScmRepository(function() {
					scmRepositoriesTable.refresh();
					$el.formDialog('cleanup');
				});
			});

      $el.on('formdialogcancel', function() {
      	$el.formDialog('close');
      });
		},
		/**
		* Open confirm dialog and execute the confirmHandler function when 'confirm' is pressed.
		*/
		openConfirmDialog: function(newPath, newFolder, confirmHandler) {
			this.confirmPopup.confirmDialog({confirm: confirmHandler});
			this.confirmPopup.find("#confirm-repository-path").html(newPath);
			this.confirmPopup.find("#confirm-repository-folder").html(newFolder);
			this.confirmPopup.confirmDialog('open');
		},
		/**
		* Check the validity of the form. If any error, display the errors in the dialog.
		* If everything is valid, create the new ScmRepository to the database and then execute the callback function.
		*/
		addNewScmRepository: function(callback) {
			var self = this;
			// clear errors
			self.clearErrorMessages();
			// if no blanks (except folder)
			if(!self.checkBlankInputsAndDisplayErrors()) {
				// retrieve parameters
				var newScmRepository = self.retrieveNewScmRepositoryParams();
				// create the repository
				self.openConfirmDialog(newScmRepository.repositoryPath, newScmRepository.workingFolderPath,
					function() {
						self.doAddNewScmRepository(newScmRepository)
							.success(callback)
							.error(function(xhr) {
								var scmExceptions = xhr.responseJSON.fieldValidationErrors;
								if(!!scmExceptions && scmExceptions[0].fieldName === 'scm') {
									self.displayErrorPopup(scmExceptions[0].errorMessage);
								}
							});
					});
			}
		},
		/**
		* Open the embedded error frame and display the given message.
		*/
		displayErrorPopup: function(message) {
			this.errorPopup.find('.error-message').html(message);
			this.errorPopup.popupError("show");
		},
		/**
		* Create Ajax Post Request to create the new ScmRepository.
		*	@param newScmRepository : {
		* 	path: 'path',
		* 	folder: 'folder',
		* 	branch: 'branch'
		*	}
		*	@return Promise of Post Request.
		*/
		doAddNewScmRepository : function(newScmRepository) {
			return $.ajax({
				url : routing.buildURL('administration.scm-server.repositories', squashtm.pageConfiguration.scmServerId),
				type : 'POST',
				dataType: 'json',
				data : newScmRepository
			});
    },
		/**
		* Retrieve the values in the form and return them as an object.
		* @return newScmRepository: {
		*		path: 'path',
		*		folder: 'folder',
		*		branch: 'branch'
		* }
		*/
		retrieveNewScmRepositoryParams : function() {
			var name = this.inputs['name'].val();
			var path = this.inputs['path'].val();
			var folder = this.inputs['folder'].val();
			var branch = this.inputs['branch'].val();
			return {
				"name": name,
				"repositoryPath": path,
				"workingFolderPath": folder,
				"workingBranch": branch
			};
		},

		/**
		* Clear all error messages in this dialog.
		*/
		clearErrorMessages: function() {
			for(var key in this.inputs) {
				var element = this.inputs[key];
				Forms.input(element).clearState();
			}
		},
		/**
		*	Check the validity of the entered parameters, displays errors for the ones which are left blank and can't be.
		* @return True if at least one entry is left blank (except folder), else returns False.
		*/
		checkBlankInputsAndDisplayErrors: function() {
			var oneInputIsBlank = false;
			var constrainedInputs = _.omit(this.inputs, 'folder');
			for(var key in constrainedInputs) {
				var element = constrainedInputs[key];
				var value = element.val();
				if(StringUtils.isBlank(value)) {
					oneInputIsBlank = true;
					Forms.input(element).setState("error", translator.get("message.notBlank"));
				}
			}
			return oneInputIsBlank;
		},

		open: function() {
			this.clearErrorMessages();
			this.$el.formDialog('open');
		}

	});

	return AddScmRepositoryDialog;

});
