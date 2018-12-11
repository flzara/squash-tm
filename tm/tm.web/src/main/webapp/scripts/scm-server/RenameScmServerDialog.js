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
 define(['jquery', 'backbone', "workspace.routing", 'jquery.squash.formdialog'],
 		function($, Backbone, routing) {
 			"use strict";

 		var RenameScmServerDialog = Backbone.View.extend({

 			el: "#rename-scm-server-popup",

 			input: $('#rename-scm-server-input'),

 			initialize: function(nameLabel) {
 				var self = this;
 				var $el = this.$el;
      	$el.formDialog();

				$el.on('formdialogopen', function() {
					self.input.val(squashtm.pageConfiguration.scmServerName);
				});

      	$el.on('formdialogconfirm', function() {
        	self.renameScmServer(function(newName) {
        		nameLabel.text(newName);
        		squashtm.pageConfiguration.scmServerName = newName;
            $el.formDialog('close');
          });
        });

      	$el.on('formdialogcancel', function() {
        	$el.formDialog('close');
        });
 			},

 			renameScmServer: function(callback) {
 				var newName = this.input.val();
 				this.doRenameScmServer(newName).success(callback);
 			},
 			/**
 			* Send Ajax POST Request to rename the ScmServer with the given name.
 			*	@param newName: The new name to give to the ScmServer.
 			*	@return Promise of POST Request.
 			*/
 			doRenameScmServer: function(newName) {
 				return $.ajax({
 					url: squashtm.pageConfiguration.url,
 					method: 'POST',
 					data: {
 						name: newName
 					}
 				});
 			},

 			open: function() {
 				this.$el.formDialog('open');
 			}

 		});

 		return RenameScmServerDialog;
 });
