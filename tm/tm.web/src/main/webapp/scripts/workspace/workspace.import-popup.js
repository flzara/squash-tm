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
 * settings : {
 *		formats : [array of supported file extensions, that will be checked on validation ]
 *
 * }
 *
 * -------------- API----------
 *
 * the following MUST be implemented :
 *
 * {
 *		createSummary : function(json) : populate the summary panel using the json response object
 *
 *		createFormatErrorsSummary : function(json) : populate the error panel using the json response object
 *
 * }
 *
 * The following methods have a default implementation but could be considered for overriding :
 *
 * {
 *		bindEvents : function() : event binding
 *		getForm : function() : returns the form that must be uploaded.
 * }
 *
 */
define([ "jquery", "underscore",  "app/ws/squashtm.notification", "squash.translator" ,"jquery.squash.formdialog", "jform" ], function($, _, WTF, translator) {
			"use strict";

			if (($.squash !== undefined) && ($.squash.importDialog !== undefined)) {
				// plugin already loaded
				return;
			}

			$.widget("squash.importDialog", $.squash.formDialog, {

			widgetEventPrefix : "importdialog",

			options : {
				_ticket : 0, // upload ticket (used internally, shouldn't be set by the user)
				formats : [ "you forgot to configure that" ]
			},

			// ********************** abstrat *******************************

			createSummary : function(xhr) {
				throw "importDialog : it seems this instance is an abstract instance :  it should have been subclassed and implement createSummary properly !";
			},
			createFormatErrorsSummary : function(xhr) {
				throw "importDialog :  it seems this instance is an abstract instance :  it should have been subclassed and implement createFormatErrorsSummary properly !";
			},
			_create : function() {
				this._super();
				this.bindEvents();
			},

			bindEvents : function() {
				var self = this;

				// ** radio **

				// ** buttons **

				this.onOwnBtn("import", function() {
					self.setState(self.validate() === true ? "confirm" : "error-type");
				});

				var selfClose = $.proxy(self.close, self);
				this.onOwnBtn("confirm", $.proxy(self.submit, self));

				this.onOwnBtn("ok", selfClose);
				this.onOwnBtn("okerrsize", selfClose);

				this.onOwnBtn("okerrformat", function() {
					self.setState("parametrization");
				});

				this.onOwnBtn("cancel-progression", function() {
					self.cancelUpload();
					self.close();
				});

				this.onOwnBtn("cancel", selfClose);

				//quick fix. Stateparam is the first state, so put the 'normal' title on this state, then change to 'summary' title when state is summary
				this.onOwnBtn("statechangeparametrization", function(){	self.changeTitle(translator.get("dialog.import-excel.title"));});
				this.onOwnBtn("statechangesummary", function(){	self.changeTitle(translator.get("dialog.import-excel.title.summary"));});

			},

			changeTitle : function(title){
				this.element.parent().find(".ui-dialog-titlebar").html(title);
			},
			open : function() {
				this._super();
				this.reset();
			},

			reset : function() {
				this.element.find("input:text").val("");
				this.setState("parametrization");
			},

			getForm : function() {
				return this.element.find("form");
			},

			validate : function() {
				var fileUploads = this.getForm().find("input[type='file']");
				var fileNames = _.map(fileUploads, function(item) {
					return item.value.toLowerCase();
				});

				var self = this;
				var nameToValidMapper = function(name) {
					return _.some(self.options.formats, function(ext) {
						return name.match("." + ext.toLowerCase() + "$");
					});
				};
				var validNames = _.map(fileNames, nameToValidMapper);

				return _.every(validNames);
			},

			// ***************** request submission code *******************

			submit : function() {
				this.setState("progression");
				this.doSubmit();
			},

			/**
			 * Submits the form. The submit url can be customizet through the params hash, which
			 * supports : <code>
			 * params = {
			 *   urlPostfix : "/something"
			 *   queryParams {
			 *     "foo": "bar"
			 *   }
			 * }
			 * </code>
			 *
			 * The resulting url shall be: <ode>formAction/something?upload-ticket=1&foo=bar</code>
			 */
			doSubmit : function(params) {
				var self = this;
				var form = this.getForm();

				var buildUrl = function() {
					var root = self.getForm().attr("action");
					var url = root;
					url += (!!params && !!params.urlPostfix) ? params.urlPostfix : "";
					url += "?upload-ticket=" + self.options._ticket;
					url += (!!params && !!params.queryParams) ? ("&" + $.param(params.queryParams)): "";
					return url;
				};

				form.ajaxSubmit({
					url : buildUrl(),
					type : "POST",
					success : function() {
					},
					error : function() {
					},
					complete : function(xhr) {
						self.options.xhr = xhr;
						var json ;
						try{
						json = $.parseJSON($(xhr.responseText).text());
						}catch(e){
							WTF.handleGenericResponseError(xhr);
							self.close();
						}
						if (json) {
							if (json.maxUploadError) {
								self.errMaxSize(json.maxUploadError.maxSize);
								self.setState("error-size");
							} else {
								if (json.status && json.status == "Format KO") {
									self.createFormatErrorsSummary(json);
									self.setState("error-format");
								}
								else if (json.actionValidationError) {
									self.createFormatErrorsSummary(json);
									self.setState("error-format");
								}

								else {
									self.createSummary(json);
									if(!!params.queryParams){
										self.setState("simulation-summary");
									}else {
										self.setState("summary");
									}
								}
							}
						}
					},

					target : self.element.find(".dump").attr("id")
				});
			},

			cancelUpload : function() {
				var state = this.getState();
				if (state === "progression") {
					this._cancelPoll();
					// we must also kill the submit itself, alas killing other pending
					// ajax requests.
					if (window.stop !== undefined) {
						window.stop();
					} else {
						/*
						 * IE-specific instruction document.execCommand("Stop"); wont prevent the
						 * file to be fully uploaded because it doesn't kill the socket, so we'll be
						 * even more blunt
						 */
						document.location.reload();
					}
				}
			},

			_startPoll : function() {
				// TODO
			},

			_cancelPoll : function() {
				// TODO
			},

			// ********************* errors *********************************

			errMaxSize : function(maxSize) {

				var size = (maxSize / 1048576).toFixed(3);

				var span = this.element.find(".error-size");
				var text = span.text();
				if (text.indexOf("{MAX-SIZE}") !== -1) {
					text = text.replace("{MAX-SIZE}", size);
					span.text(text);
				}

			}

		});

});
