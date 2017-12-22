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
/**
 * ConfirmDialog widget. A confirm dialog is a preconfigured modal dialog which
 * shows a message and has a ok and a cancel button.
 *
 * If the div used to generate the dialog contains up to 2 <input type="button" />
 * elements, they are used as the ok and cancel buttons labels.
 *
 * Usage example :
 *
 * <div id="confirm-dialog" class="not-displayed popup-dialog" title="title">
 * <strong>message</strong> <input:ok /> <input:cancel /> </div>
 *
 * <div id="confirm-dialog" class="not-displayed popup-dialog" title="title">
 * <strong>message</strong> <input type="button" value="not a confirm button" />
 * <div class="popup-dialog-buttonpane"> <input:ok /> <input:cancel /> </div>
 * </div>
 *
 * <script> $(function(){ var confirmHandler = function() {
 * actionAfterConfirm(); };
 *
 * var dialog = $( "#confirm-dialog" ); dialog.confirmDialog(
 * {confirm:confirmHandler} );
 *
 * $('#button').click(function(){ dialog.confirmDialog( "open" ); return false;
 * }); }); </script>
 *
 * The dialog triggers a confirmdialogconfirm when confirm button is clicked The
 * dialog triggers a confirmdialogcancel when cancel button is clicked
 *
 * @author Gregory Fouquet
 */
(function($) {
	if (($.squash !== undefined) && ($.squash.confirmDialog !== undefined)) {
		// plugin already loaded
		return;
	}

	var closeDialogHandler = function() {};

	$.widget("squash.confirmDialog", $.ui.dialog, {
		options : {
			autoOpen : false,
			resizable : false,
			modal : true,
			width : 600,
			position : [ 'center', 100 ],
			buttons : [ {
				text : "Ok", // OK button closes by default
				click : closeDialogHandler
			}, {
				text : "Cancel", // cancel button closes by default
				click : closeDialogHandler
			} ]
		},

		confirm : function(event) {
			if (false === this._trigger("validate", event)) {
				return;
			}

			if (!this.close()) {
				return;
			}

			this._trigger("confirm");
			return this;
		},

		cancel : function(event) {
			if (!this.close()) {
				return;
			}

			this._trigger("cancel");
			return this;
		},

		_create : function() {
			var self = this;

			var parent = this.element.eq(0).parent();

			function cancelOnEscConfirmOnEnter(event) {
				if (event.keyCode === $.ui.keyCode.ESCAPE) {
					self.cancel(event);
					event.preventDefault();
				}else if (event.keyCode === $.ui.keyCode.ENTER){
					self.confirm(event);
					event.preventDefault();
				}
			}


			// creates the widget
			self._super();

			// declares custom events
			self._on({
				"click .ui-dialog-buttonpane button:first" : self.confirm,
				"click .ui-dialog-buttonpane button:last" : self.cancel,
				"click .ui-dialog-titlebar-close" : self.cancel,
				"keydown" : cancelOnEscConfirmOnEnter
			});

			// autoremove when parent container is removed
			parent.on('remove', function() {
				self.element.confirmDialog('destroy');
				self.element.remove();
			});
		},

		_createButtons : function(buttons) {
			var self = this;

			function buttonsParent() {
				var popup = $(self.element);
				var buttonsPane = popup.find(".popup-dialog-buttonpane");
				return buttonsPane.length > 0 ? buttonsPane : popup;
			}

			var inputButtons = buttonsParent().find("input:button");

			if (inputButtons.length > 0) {
				var okLabel = inputButtons[0].value;
				buttons[0].text = okLabel;
			}
			if (inputButtons.length > 1) {
				var cancelLabel = inputButtons[1].value;
				buttons[1].text = cancelLabel;
			}

			$.ui.dialog.prototype._createButtons.apply(this, arguments);

			inputButtons.remove();
		},

		_setOption : function(key, value) {
			// In jQuery UI 1.8, you have to manually invoke the
			// _setOption method from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
		},

		_destroy : function() {
			this._off($(".ui-dialog-buttonpane button"), "click");
			this._off($(".ui-dialog-titlebar-close"), "click");
			this._super();
		}

	});
}(jQuery));
