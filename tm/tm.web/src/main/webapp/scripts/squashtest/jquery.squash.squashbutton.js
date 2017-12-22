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
 * squashButton widget. Applies standard theme to buttons.
 *
 * If the button has a data-icon attribute, it will be used as primary icon. If
 * a primary icon is defined in options, it will override the attribute-defined
 * icon.
 *
 * If the button has a data-text="false" attribute, then no text is shown.
 *
 * @author Gregory Fouquet
 */
(function($) {
	$.widget("squash.squashButton", $.ui.button, {
		_trigger : function(type, event, data) {
			this._super(type, event, data);
			this.element.removeClass("ui-state-focus ui-state-hover");
			return this;
		},

		_create : function() {
			// note: icons defaults to {primary: null, secondary: null}
			var icons = this.options.icons;
			var $el = $(this.element);
			var dataIcon = $el.data("icon");
			var dataText = $el.data("text");

			if (!icons.primary && dataIcon) {
				icons.primary = dataIcon;
			}

			// note: options.text defaults to true.
			if (dataText === false) {
				this.options.text = false;
			}
			// else, dataText is either true or crap

			this._super();

			this.element.addClass('squash-button-initialized');
		},

		_setOption : function(key, value) {
			return this._super(key, value);
		}

	});

	function makeSquashButton(selectFn) {
		selectFn.call(this, "a.button, input:submit.button, input:button.button, button.button")
		.not('.squash-button-initialized').not(".sq-btn").not(".sq-icon-btn")
		.squashButton();
	}

	/**
	 * Adds functions in the $.squash namespace : $.squash.decorateButtons()
	 * will decorate all links and buttons whit the "button" class with the
	 * squashButton widget.
	 */
	$.extend($.squash, {
		decorateButtons : function() {
			makeSquashButton($);
		}
	});

	/**
	 * Adds methods to $() $().decorateButtons() will decorate all links and
	 * buttons whit the "button" class with the squashButton widget.
	 */
	$.fn.extend({
		decorateButtons : function() {
			makeSquashButton($(this).find);
		}
	});
}(jQuery));