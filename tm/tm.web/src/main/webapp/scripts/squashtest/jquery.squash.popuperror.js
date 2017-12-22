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
define(["jquery", "jqueryui"], function($) {
	$.widget("squash.popupError", {

		_create : function() {
			var jqElt = this.element;

			jqElt.wrap('<div class="error-overlay error-hidden-state" />');
			jqElt.addClass('error-style');

			jqElt.each(function() {
				var jE = $(this);
				var html = jE.html();
				jE.html('<div class="error-inner">' + html + '</div>');

				var parentPop = jE.parents(".ui-dialog.ui-widget");
				if (parentPop.length == 1) {
					jE.parent().prependTo(parentPop.get(0));
				}

			});

			var self = this;
			jqElt.click(function() {
				self.hide();
			});

			return this;

		},

		show : function() {
			this.element.parent().removeClass('error-hidden-state');
			this.element.parents('.ui-dialog.ui-widget').find('input:focus')
					.blur();
		},

		hide : function() {
			this.element.parent().addClass('error-hidden-state');
		},

		hangTo : function(newParent) {
			this.element.parent().prependTo(newParent);
		}

	});
	
	return $.fn.popupError;
});