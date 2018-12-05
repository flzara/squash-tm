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
define(['jquery', 'backbone', "squash.translator", "workspace.routing", 'jquery.squash.formdialog'],
	function($, Backbone, translator, routing) {

	var DeleteScmRepositoryDialog = Backbone.View.extend({

		el: '#delete-scm-repository-popup',

		initialize: function(scmRepositoriesTable) {
			var self = this;
			var $el = this.$el;

			$el.formDialog();

			$el.on('formdialogopen', function() {
				// TODO: Check if the Repository is associated with one or more SquashTM Projects.
				// Display the coresponding state.
				$el.formDialog('setState', 'default');
			});

			$el.on('formdialogconfirm', function() {
				self.deleteOneScmRepository(function() {
					scmRepositoriesTable.refresh();
					$el.formDialog('close');
				});
			});

			$el.on('formdialogcancel', function() {
				$el.formDialog('close');
			});
		},

		deleteOneScmRepository: function(callback) {
			var repositoryId = this.$el.data('entity-id');
			this.doDeleteOneScmRepository(repositoryId).success(callback);
		},
		/**
		* Send Ajax Delete Request to delete the ScmRepository with the given Id.
		*	@param repositoryId: The Id of the ScmRepository to delete.
		*	@return Promise of Delete Request.
		*/
		doDeleteOneScmRepository: function(repositoryId) {
			return $.ajax({
				url: routing.buildURL('administration.scm-repositories', repositoryId),
				method: 'DELETE'
			});
		}

	});

	return DeleteScmRepositoryDialog;
});
