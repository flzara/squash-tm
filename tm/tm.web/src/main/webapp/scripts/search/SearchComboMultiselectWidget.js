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
define([ "jquery", "jqueryui" ], function($) {

	var searchwidget = $.widget("search.searchComboMultiSelectWidget", {

		options : {

		},

		_create : function() {
			this._super();
		},

		fieldvalue : function(value) {

			if (!value) {

				var checked = $($(this.element.children()[0]).children()[0])
						.prop('checked');

				if (checked) {
					var text = $("select", this.element.children()).val();
					var id = $(this.element).attr("id");
					return {
						"type" : "LIST",
						"values" : text
					};
				} else {
					return null;
				}
			} else {
				$("option", $("select", this.element.children())).removeAttr(
						"selected");
				if (!!value.values) {
					for ( var i = 0, len = value.values.length; i < len; i++) {
						$("option[value='" + value.values[i] + "']",
								$("select", this.element.children())).attr("selected",
								"selected");
					}
					$($(this.element.children()[0]).children()[0]).attr('checked', true);
				}
			}
		},

		createDom : function(id, options) {

			var input = $("select", this.element);

			var opt;
			for ( var i = 0, len = options.length; i < len; i++) {
				opt = $('<option>', {
					'text' : options[i].value,
					'value' : options[i].code,
					'selected' : 'selected'
				});
				opt.html(options[i].value);
				input.append(opt);
			}
		}
	});
	return searchwidget;
});
