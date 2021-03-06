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
 			"use strict";

 		var DeleteScmServerDialog = Backbone.View.extend({

 			el: "#delete-scm-server-popup",

 			initialize: function(scmServersTable) {
 				var self = this;
 				var $el = this.$el;
      	$el.formDialog();

				$el.on('formdialogopen', function() {
					$el.formDialog('setState', 'wait');
					var serverId = $el.data('entity-id');
					self.doCheckIfServerIsBoundToProject(serverId).success(function(isBound) {
						if(isBound) {
							$el.formDialog('setState', 'bound-to-project');
						} else {
							$el.formDialog('setState', 'default');
						}
					});
				});

      	$el.on('formdialogconfirm', function() {
        	self.deleteOneScmServer(function() {
          	scmServersTable.refresh();
            $el.formDialog('close');
          });
        });

      	$el.on('formdialogcancel', function() {
        	$el.formDialog('close');
        });
 			},

 			deleteOneScmServer: function(callback) {
 				var serverId = this.$el.data('entity-id');
 				this.doDeleteOneScmServer(serverId).success(callback);
 			},
 			/**
 			* Send Ajax Delete Request to delete the ScmServer with the given Id.
 			*	@param serverId: The Id of the ScmServer to delete.
 			*	@return Promise of Delete Request.
 			*/
 			doDeleteOneScmServer: function(serverId) {
 				return $.ajax({
 					url: routing.buildURL('administration.scm-servers.delete', serverId),
 					method: 'DELETE'
 				});
 			},
			/**
			* Send Ajax GET Request to check if the ScmServer with the given Id contains at least one ScmRepository which is
			* bound to a Project.
			* @param serverId: The Id of the ScmServer to check.
			* @return Promise of GET Request.
			*/
 			doCheckIfServerIsBoundToProject: function(serverId) {
 				return $.ajax({
 					url: routing.buildURL('administration.scm-servers.delete', serverId),
 					method: 'GET',
 					data: {
 						id: 'is-bound'
 					}
 				});
 			}

 		});

 		return DeleteScmServerDialog;
 });
