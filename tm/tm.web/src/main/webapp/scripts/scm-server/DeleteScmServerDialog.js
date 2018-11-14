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

 		var AddScmServerDialog = Backbone.View.extend({

 			el: "#delete-scm-server-popup",

 			initialize: function(scmServersTable) {
 				var self = this;
 				var $el = this.$el;
      	$el.formDialog();

				$el.on('formdialogopen', function() {
					// TODO: Check if the Server is associated with one or more SquashTm Projects.
					// Display the corresponding state.
					$el.formDialog('setState', 'default');
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
 					url: routing.buildURL('administration.scm-server', serverId),
 					method: 'DELETE'
 				});
 			}

 		});

 		return AddScmServerDialog;
 });
