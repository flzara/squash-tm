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
define(["jquery", "squash.translator"],
			function($, translator) {
	if ($.squash !== undefined && $.squash.messageDialog !== undefined) {
		// already loaded -> bail out
		return;
	}
	/**
	 * MessageDialog widget. A message dialog is a preconfigured modal dialog which shows a message and only has a close
	 * button.
	 *
	 * If the div used to generate the dialog contains an <input type="button" /> element, its value is used as the
	 * message dialog's ok button.
	 *
	 * cf example below
	 *
	 * @author Gregory Fouquet
	 */
	$.widget("squash.messageDialog", $.ui.dialog, {
		options : {
			autoOpen : false,
			resizable : false,
			modal : true,
			width : 600,
			// position : [ 'center', 100 ],
			closeOnEscape : true,
			closeOnEnter : true,
			buttons : [ {
				text : translator.get('label.Close'),
				click : function() {
					$(this).messageDialog("close");
				}
			} ]
		},

		_create : function() {

			var parent = this.element.eq(0).parent();

			// we need to invoke prototype creation
			$.ui.dialog.prototype._create.apply(this);

			var self = this;

			self.uiDialog.addClass("popup-dialog")
			// allow closing by pressing the enter key
			.keydown(
					function(event) {
						if (self.options.closeOnEnter && !event.isDefaultPrevented() && event.keyCode &&
								event.keyCode === $.ui.keyCode.ENTER) {
							self.close(event);
							event.preventDefault();
						}
					});

			self.element.removeClass("not-visible");

			// autoremove when parent container is removed
			parent.on('remove', function() {
				self.element.messageDialog('destroy');
				self.element.remove();
			});

		},

		_createButtons : function(buttons) {
			var self = this;
			var okButton = $("input:button", self.element);

			if (okButton.length) {
				buttons[0].text = translator.get('label.Close');
			}

			$.ui.dialog.prototype._createButtons.apply(this, arguments);

			okButton.remove();
		},

		_setOption : function(key, value) {
			// In jQuery UI 1.8, you have to manually invoke the
			// _setOption method from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
		},

		_trigger : function(type, event, data) {
			if (type == 'open') {
				var self = this;

				if (self.overlay) {
					// allow closing by pressing the enter key
					$(document).bind(
							'keydown.dialog-overlay',
							function(event) {
								if (self.options.closeOnEnter && !event.isDefaultPrevented() && event.keyCode &&
										event.keyCode === $.ui.keyCode.ENTER) {

									self.close(event);
									event.preventDefault();
								}
							});
				}
			}
			// we need this otherwise events won't bubble
			$.Widget.prototype._trigger.apply(this, arguments);
		},

		destroy : function() {
			// root dialog widget removed the title of the
			// original elemnt. we put it
			// back.
			if (this.originalTitle !== "") {
				this.element.attr("title", this.originalTitle);
			}

			// In jQuery UI 1.8, you must invoke the destroy
			// method from the
			// base widget
			$.Widget.prototype.destroy.call(this);
		}
	});

	/**
	 * Opens a messageDialog created on the fly and discards it afterwards. eg :
	 * $('#dialogDef").openMessage().done(function () { console.log('closed') })
	 *
	 * @return a promise
	 */
	$.fn.openMessage = function(size) {
		var self = this;

		var deferred = $.Deferred();

		var close = function() {
			self.messageDialog('destroy');
			deferred.resolve();
		};

		if (size !== null) {
			self.messageDialog({
				width : size
			}).bind('messagedialogclose', close).messageDialog('open');
		} else {
			self.messageDialog().bind('messagedialogclose', close).messageDialog('open');
		}

		return deferred.promise();
	};
	
	/**
	 * Adds functions in the $.squash namespace
	 */
	$.extend($.squash, {
		/**
		 * Creates a modal message dialog out of the blue using the given title and message. Created DOM are discarded
		 * when dialog is closed.
		 *
		 * @param title
		 *            text title of message dialog
		 * @param html
		 *            chunk used as the body of the dialog.
		 * @return a promise
		 */
		openMessage : function(title, htmlMessage, size) {
			var dialog = $('<div></div>');
						
			dialog.attr('title', title);

			dialog.append('<div class="centered" style="margin-top:15px;">'+htmlMessage+'</div>');
			$(document.body).append(dialog);

			var discardDialog = function() {
				dialog.remove();
			};


			dialog.bind('destroy', discardDialog);

			return dialog.openMessage(size);
		}
	});
	
});
