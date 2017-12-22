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
 * This dialog uploads files using xhr2 API for Chrome and FF,
 * and a shitty workaround for IE9
 *
 */

define(
	["jquery", "squash.attributeparser", "handlebars", "jquery.squash.formdialog",
		"./jquery.squash.multi-fileupload"],
	function ($, attrparser, Handlebars) {

		if (($.squash !== undefined) && ($.squash.attachmentsDialog !== undefined)) {
			// plugin already loaded
			return;
		}

		$.widget("squash.attachmentsDialog", $.squash.formDialog, {

			options: {
				width: 400,
				url: undefined
			},

			_create: function () {

				this._super();

				// main form init
				var template = this.element.find('.add-attachments-templates > .attachment-item');
				this.options._form = this.element.find('.attachment-upload-form').multiFileupload(
					template);

				// progressbar init
				this.options.bar = this.element.find('.attachment-progressbar').progressbar({
					value: 0
				});
				this.options.percent = this.element.find('.attachment-progress-percentage');

				// summary init
				var summaryItemTpl = '<div class="display-table-row" >'
					+ '<label class="display-table-cell" style="font-weight:bold;">{{name}}</label>'
					+ '<span class="display-table-cell">{{status}}</span>' + '</div>';
				this.options.summaryitem = Handlebars.compile(summaryItemTpl);

				// error init
				var errSpan = this.element.find('.attachment-upload-error-message');
				this.options._sizeexceeded = errSpan.text();
				errSpan.text('');

				this._bindEvents();
			},

			_bindEvents: function () {
				var self = this;

				this.onOwnBtn('cancel', function () {
					self.close();
				});

				this.onOwnBtn('done', function () {
					self.close();
				});

				this.onOwnBtn('submit', function () {
					self.submitAttachments();
				});
			},

			open: function () {
				this._super();
				this.options._form.clear();
				this.setState('selection');
			},

			close: function () {
				this._super();
				if (this.options._xhr && this.options.preventAbortion !== true) {
					this.options._xhr.abort();
				}
			},

			// ****************** files submission ***********************

			supportProgressBar: function supportAjaxUploadWithProgress() {

				return supportFileAPI() && supportAjaxUploadProgressEvents();

				function supportFileAPI() {
					var fi = document.createElement('INPUT');
					fi.type = 'file';
					return 'files' in fi;
				}

				function supportAjaxUploadProgressEvents() {
					var xhr = new XMLHttpRequest();
					return !!(xhr && ('upload' in xhr) && ('onprogress' in xhr.upload));
				}
			},

			setShowProgress : function(show){
				var fn = (show) ? "show" : "hide";
				$(".attachment-upload-uploading").children()[fn]();
			},

			submitAttachments: function () {
				var self = this;
				var url = this.options.url;


				function onprogressHandler(evt) {
					var percent = evt.loaded / evt.total * 100;
					console.log('Upload progress: ' + percent.toFixed(0) + '%');
					self.refreshBar(percent);
				}

				self.setState('uploading');
				self.refreshBar(0);

				if (self.supportProgressBar()) {

					self.setShowProgress(true);

					var xhr = new XMLHttpRequest();
					self.options._xhr = xhr;
					xhr.upload.addEventListener('progress', onprogressHandler, false);
					xhr.addEventListener('readystatechange', function (e) {
						if (this.readyState === 4) {
							self.submitComplete(xhr);
						}
					});

					xhr.open('POST', url, true);
					xhr.setRequestHeader('Accept', 'application/json');

					var attach = $('input:file[name="attachment[]"]');
					var formData = new FormData();

					if (attach.length > 2) {
						for (i = 1; i < attach.length - 1; i++) {
							var file = attach[i].files[0];
							formData.append("attachment[]", file);
						}

						// Catch if there's a JDBC Exception

						xhr.onreadystatechange = function () {
							if (xhr.readyState === 4) {
								if (xhr.status !== 200) {
									self.setShowProgress(false);
								}
							}
						};
						xhr.send(formData);
					} else { //no attach

						self.close();
					}
				} else {
					// for browser that don't support xhr2, like IE9.

					self.options._form.ajaxSubmit({
						url: url,
						type: "post",
						dataType: "application/json",
						beforeSend: function (xhr) {
							self.options._xhr = xhr;
							$(".attachment-progressbar").hide();
							$(".attachment-progress-percentage").hide();
						},
						success: function () {
						},
						error: function () {
						},
						complete: function (xhr) {
							self.submitComplete(xhr);
						},
						target: "#dump"

					});
				}
			},


			submitComplete: function (xhr) {
				this.options._xhr = xhr;
				// Kind of a hack to prevent the thisDialog.close hook to try and abort the xhr,
				// which leads to an infinite loop in IE.
				// TODO Gotta find a more elegant way
				this.options.preventAbortion = true;
				var text;

				try {
					text = $(xhr.responseText).text();
				} catch (e) {
					text = xhr.responseText;
				}

				// if text contains html, and HTML ERROR, display an error
				if (text.indexOf("HTTP ERROR") >= 0) {
					this.displayErrorJDBC(text);
				}

				try{	// try json
					var json = $.parseJSON(text);

					if (json.maxUploadError === undefined) {
						this.displaySummary(json);
					} else {
						this.displayError(json.maxUploadError.maxSize);
					}
				}
				catch(nojson){	// try html
					this.displayError(text);
				}
			},

			// ********************* upload progress ******************

			refreshBar: function (percentage) {
				this.options.bar.progressbar('option', 'value', percentage);
				this.options.percent.text(percentage.toFixed(0).toString() + '%');

			},

			// ******************** upload summary ********************

			displaySummary: function (json) {
				if (json !== null && !this.allSuccessful(json)) {
					this.populateSummary(json);
					this.setState('summary');
				} else {
					this.close();
					this._trigger('done');
				}

			},

			allSuccessful: function (summaries) {
				for (var i = 0; i < summaries.length; i++) {
					if (summaries[i].iStatus !== 0) {
						return false;
					}
				}
				return true;
			},

			populateSummary: function (summaries) {
				var i = 0,
				summarydiv = this.element.find('.attachment-upload-summary'),
				summaryItemTpl = this.options.summaryitem;

				summarydiv.empty();
				for (i = 0; i < summaries.length; i++) {
					var item = summaries[i];
					var line = summaryItemTpl(item);
					summarydiv.append(line);
				}

			},

			// ***************** errors ***********************

			displayError: function (sizeOrMsg) {

				if (typeof sizeOrMsg === "string"){
					this.element.find('.attachment-upload-error-message').text(sizeOrMsg);
				}
				else{
					var s = (sizeOrMsg / 1048576).toFixed(3);

					var errMessage = this.options._sizeexceeded.replace('#size#', s);
					this.element.find('.attachment-upload-error-message').text(errMessage);
				}
				this.setState('error');

			},

			displayErrorJDBC: function (text) {

				var errMessage = text;
				this.element.find('.attachment-upload-error-message').text(errMessage);
				this.setState('error');
			}

		});

	});
