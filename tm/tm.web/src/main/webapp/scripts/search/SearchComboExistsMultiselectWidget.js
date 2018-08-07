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

	var searchwidget = $.widget("search.searchComboExistsMultiselectWidget", {

		options : {

		},

		_create : function() {
			this._super();
		},

		fieldvalue : function(value) {

			if (!value) {

				var checked = $($(".combo-choice-checkbox"))
					.prop('checked');

				if (checked) {
					var combochoiceselect = $("select", $(".combo-choice")).val();
					var combotypeselect = $("select", $(".combo-type")).val();
					var id = $(this.element).attr("id");
					if(combochoiceselect == 1) {
						return {
							"type": "MULTILIST",
							"values": combotypeselect,
							"minValue" : 1,
							"maxValue" : null
						};
					}else {
						return {
							"type": "MULTILIST",
							"values": combotypeselect,
							"minValue" : null,
							"maxValue" : 0
						};
					}
				} else {
					return null;
				}
			} else {
				$("option", $(this.element.children()[0])).removeAttr("selected");
				if (!!value.values){
					for (var i=0, len = value.values.length; i<len;i++){
						$("option[value='"+value.values[i]+"']", $(this.element.children()[0])).attr("selected", "selected");
					}
				}
			}
		},

		createDom : function(id, options) {

			var selectcombochoice = $("select", $(".combo-choice"));

			var choiceopt;
			for ( var i = 0, len = 2; i < len; i++) {
				choiceopt = $('<option>', {
					'text' : options[i].value,
					'value' : options[i].code,
					'selected' : 'selected'
				});
				choiceopt.html(options[i].value);
				selectcombochoice.append(choiceopt);
			}

			var input = $("select", $(".combo-type"));
			input.html("");
			var typeopt;
			for ( var j = 2, len2 = options.length; j < len2; j++) {
				typeopt = $('<option>', {
					'text' : options[j].value,
					'value' : options[j].code,
					'selected' : 'selected'
				});
				typeopt.html(options[j].value);
				input.append(typeopt);
			}
		}
	});

	return searchwidget;
});
