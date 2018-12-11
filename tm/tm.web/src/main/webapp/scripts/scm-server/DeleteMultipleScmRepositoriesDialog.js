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
define(['jquery', 'backbone', "squash.translator", "workspace.routing", 'jquery.squash.formdialog', "jquery.squash.messagedialog"],
 		function($, Backbone, translator, routing) {
 			"use strict";

 		var DeleteMultipleScmRepositoriesDialog = Backbone.View.extend({

 			el: "#delete-scm-repositories-popup",

 			initialize: function(scmRepositoriesTable) {
 				var self = this;
 				var $el = this.$el;
 				this.scmRepositoriesTable = scmRepositoriesTable;

      	$el.formDialog();
      	this.errorDialog = $('#generic-error-dialog').messageDialog();

				$el.on('formdialogopen', function() {
					var repositoryIds = self.scmRepositoriesTable.getSelectedIds();
					self.doCheckIfOneRepositoryIsBoundToProject(repositoryIds).success(function(isBound) {
						if(isBound) {
							$el.formDialog('setState', 'bound-to-project');
						} else {
							$el.formDialog('setState', 'default');
						}
					});
					// Display the corresponding state.
					$el.formDialog('setState', 'default');
				});

      	$el.on('formdialogconfirm', function() {
        	self.deleteScmRepositories(function() {
          	scmRepositoriesTable.refresh();
            $el.formDialog('close');
          });
        });

      	$el.on('formdialogcancel', function() {
        	$el.formDialog('close');
        });
 			},

 			/**
 			* Open this dialog.
 			*/
 			open : function() {
				var repositoryIds = this.scmRepositoriesTable.getSelectedIds();
				if(repositoryIds.length === 0) {
					this.errorDialog.messageDialog('open');
				} else {
					this.$el.formDialog('setState', 'wait');
					this.$el.formDialog('open');
				}
 			},

 			deleteScmRepositories: function(callback) {
 				var repositoryIds = this.scmRepositoriesTable.getSelectedIds();
 				this.doDeleteScmRepositories(repositoryIds).success(callback);
 			},
 			/**
 			* Send Ajax Delete Request to delete the ScmRepositories with the given Ids.
 			*	@param repositoryIds: The Ids of the ScmRepositories to delete.
 			*	@return Promise of Delete Request.
 			*/
 			doDeleteScmRepositories: function(repositoryIds) {
 				return $.ajax({
 					url: routing.buildURL('administration.scm-repositories', repositoryIds),
 					method: 'DELETE'
 				});
 			},
			/**
  		* Send Ajax GET Request to check if at least one of the ScmRepositories with the given Ids is bound to a Project.
  		* @param repositoryId: The Ids of the ScmRepositories to check.
  		* @return Promise of GET Request.
  		*/
  		doCheckIfOneRepositoryIsBoundToProject: function(repositoryIds) {
  			return $.ajax({
  				url: routing.buildURL('administration.scm-repositories', repositoryIds),
  				method: 'GET',
  				data: {
  					id: 'is-bound'
  				}
  			});
  		}

 		});

 		return DeleteMultipleScmRepositoriesDialog;
 });
