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
define(['jquery', 'backbone', "squash.translator", "app/util/StringUtil", "app/lnf/Forms", "workspace.routing", 'jquery.squash.formdialog'],
		function($, Backbone, translator, StringUtils, Forms, routing) {
			"use strict";

		var AddScmServerDialog = Backbone.View.extend({

					el : "#add-scm-server-popup",

          inputs : {
          	name: $("#name"),
          	kind: $("#kind"),
          	url: $("#url")
          },

					initialize : function() {
						var self = this;
						var $el = this.$el;

            $el.formDialog();

            $el.on('formdialogconfirm', function() {
            	self.addNewScmServer(function() {
            		$el.formDialog('close');
            	});
            });

            $el.on('formdialogaddanother', function() {
            	self.addNewScmServer();
            });

            $el.on('formdialogcancel', function() {
            	$el.formDialog('close')
            });
          },

					/**
					* Check the validity of the form. If any error, display the errors in the dialog.
					* If everything is valid, add the new ScmServer to the database and then execute the callback function.
					*/
					addNewScmServer : function(callback) {
						this.clearErrorMessages();

						if(!this.checkBlankInputsAndDisplayErrors()) {
							let newScmServer = this.retrieveNewScmServerParams();
							this.doAddNewScmServer(newScmServer)
								.success(callback);
						}
					},

					/**
					* Retrieve the values in the form and return them as an object.
					* @return newScmServer: {
					*		name: 'name',
					*		kind: 'kind',
					*		url: 'url'
					* }
					*/
					retrieveNewScmServerParams : function() {
						let name = this.inputs['name'].val();
						let kind = this.inputs['kind'].val();
						let url = this.inputs['url'].val();
						return {
							"name": name,
							"kind": kind,
							"url": url
						};
					},

					/**
          * Send Ajax Post Request to create the new ScmServer.
          *	@param newScmServer : {
          * 	name: 'name',
          *		kind: 'kind',
          *		url: 'url'
          *	}
          *	@return Promise of Post Request.
          */
          doAddNewScmServer : function(newScmServer) {
          	return $.ajax({
            	url : routing.buildURL("administration.scm-servers"),
              type : 'POST',
              dataType: 'json',
              data : newScmServer
            });
          },

					/**
					* Open this dialog.
					*/
					open : function() {
						this.clearErrorMessages();
						this.$el.formDialog('open');
					},

					/**
					* Clear all error messages in this dialog.
					*/
					clearErrorMessages : function() {
						for(let key in this.inputs) {
							let element = this.inputs[key];
							Forms.input(element).clearState();
						}
						this.$el.find('.url-error').text('');
					},

					/**
					*	Check the validity of the entered parameters, displays errors for the ones which are left blank.
					* @return True if at least one entry is left blank, else returns False.
					*/
					checkBlankInputsAndDisplayErrors : function() {

          	var oneInputIsBlank = false;

          	for(let key in this.inputs) {
          		let element = this.inputs[key];
          		let value = element.val();
          		if(StringUtils.isBlank(value)) {
          			oneInputIsBlank = true;
          			Forms.input(element).setState("error", translator.get("message.notBlank"));
          		}
          	}
          	return oneInputIsBlank;
          }
		});

		return AddScmServerDialog;

});
