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

 		var DeleteMultipleScmServerDialog = Backbone.View.extend({

 			el: "#multiple-delete-scm-server-popup",

 			initialize: function(scmServersTable) {
 				var self = this;
 				var $el = this.$el;
 				this.scmServersTable = scmServersTable;
      	$el.formDialog();
      	this.errorDialog = $('#generic-error-dialog').messageDialog();

				$el.on('formdialogopen', function() {
					// TODO: Check if the Servers are associated with one or more SquashTm Projects.
					// Display the corresponding state.
					$el.formDialog('setState', 'default');
				});

      	$el.on('formdialogconfirm', function() {
        	self.deleteScmServers(function() {
          	scmServersTable.refresh();
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
				var serverIds = this.scmServersTable.getSelectedIds();
				if(serverIds.length === 0) {
					this.errorDialog.messageDialog('open');
				} else {
					this.$el.formDialog('open');
				}
 			},

 			deleteScmServers: function(callback) {
 				var serverIds = this.scmServersTable.getSelectedIds();
 				this.doDeleteOneScmServer(serverIds).success(callback);
 			},
 			/**
 			* Send Ajax Delete Request to delete the ScmServers with the given Ids.
 			*	@param serverIds: The Ids of the ScmServers to delete.
 			*	@return Promise of Delete Request.
 			*/
 			doDeleteOneScmServer: function(serverIds) {
 				return $.ajax({
 					url: routing.buildURL('administration.scm-server', serverIds),
 					method: 'DELETE'
 				});
 			}

 		});

 		return DeleteMultipleScmServerDialog;
 });
