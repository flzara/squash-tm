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
 * Turns each elements in the matched set to an editable custom field (always displayed in edit mode).
 *
 *
 * .editableCustomfield({conf}) : the constructor, conf must be a CustomFieldModel.
 *
 * .editableCustomfield("value") : gets the value
 *
 * .editableCustomfield("value", value) : sets the value
 *
 * .editableCustomfield("destroy") : destroys the custom field.
 *
 */
define([ "jquery", "underscore", "ckeditor",  "squash.configmanager", "./cuf-values-utils", "jquery.squash.jeditable", "jquery.generateId", "jquery.squash.tagit" ],
		function($, _, CKEDITOR, confman, utils) {
			"use strict";

	if ($.fn.editableCustomfield !== undefined){
		return;
	}

	var widgets = {

		'PLAIN_TEXT' : {
			_build : function(elt, def){
				var content = elt.text();
				var input = $('<input type="text" value="'+content+'" />');
				elt.empty();
				elt.append(input);
			},
			_set : function(elt, def, value){
				elt.find('input').val(value);
			},
			_get : function(elt, def){
				return elt.find('input').val();
			},
			_destroy : function(elt, def){
				var content = elt.find('input').val();
				elt.empty();
				elt.text(content);
			}

		},

		'NUMERIC' : {
			_build : function(elt, def){
				var content = elt.text();
				var input = $('<input type="text" value="'+content+'" />');
				elt.empty();
				elt.append(input);
			},
			_set : function(elt, def, value){
				elt.find('input').val(value);
			},
			_get : function(elt, def){
				return elt.find('input').val();
			},
			_destroy : function(elt, def){
				var content = elt.find('input').val();
				elt.empty();
				elt.text(content);
			}

		},

		'CHECKBOX' : {
			_build : function(elt, def){
				var checked = (elt.text().toLowerCase() === "true") ? true : false;
				elt.empty();
				var chkbx = $('<input type="checkbox"/>');
				chkbx.prop('checked', checked);
				elt.append(chkbx);
			},
			_set : function(elt, def, value){
				var chkbx = elt.find('input[type="checkbox"]');
				if (value === true || value === "true"){
					chkbx.prop("checked", true);
				} else {
					chkbx.prop("checked", false);
				}
			},
			_get : function(elt, def){
				var chkbx =  elt.find('input[type="checkbox"]');
				return chkbx.prop('checked');
			},
			_destroy : function(elt, def){
				var chkbx =  elt.find('input[type="checkbox"]');
				var checked = chkbx.prop('checked');
				elt.empty();
				elt.text(checked);
			}
		},

		'DROPDOWN_LIST' : {
			_build : function(elt, def){
				var content = elt.text();

				var select = $("<select>");
				var options = def.options;
				utils.addEmptyValueToDropdownlistIfOptional(def);
				for (var i in def.options){
					var attrs = {
						'text' : options[i].label,
						'value' : options[i].label
					};
					if (options[i].label === content){
						attrs.selected = "selected";
					}
					var opt = $('<option/>',attrs);
					select.append(opt);
				}


				elt.empty();
				elt.append(select);
			},
			_set : function(elt, def, value){
				elt.find('select').val(value);
			},
			_get : function(elt, def){
				return elt.find('select').val();
			},
			_destroy : function(elt, def){
				var selected = elt.find('select').val();
				elt.empty();
				elt.text(selected);
			}
		},

		// datepickers are special : they are always jeditable, except that
		// we don't want them to post to the server but rather keep the value
		// locally (hence the dumb submit function)
		'DATE_PICKER' : {
			_build : function(elt, def){
				var date = elt.text();
				var formatted = utils.convertStrDate($.datepicker.ATOM, def.format, date);

				var input = $('<input type="text" value="'+formatted+'"/>');

				elt.empty();
				elt.append(input);

				var conf = confman.getStdDatepicker();

				input.datepicker(conf);
			},
			_set : function(elt, def, value){
				var formatted = utils.convertStrDate($.datepicker.ATOM, def.format, value);
				var input = elt.find('input');
				input.val(formatted);
			},
			_get : function(elt, def){
				var input = elt.find('input');
				var txt = input.val();
				return utils.convertStrDate(def.format, $.datepicker.ATOM, txt);
			},
			_destroy : function(elt, def){
				var input = elt.find('input');
				var date = this._get(elt, def);
				input.datepicker("destroy");
				elt.empty();
				elt.text(date);
			}
		},

		'RICH_TEXT' : {
			_build : function(elt, def){
				var html = elt.html();
				var area = $("<textarea/>");
				area.generateId();
				elt.empty();
				elt.append(area);
				area.html(html);

				var conf = confman.getStdCkeditor();
						area.ckeditor(function() {
						}, conf);
			},
			_set : function(elt, def, value){
				var cked = CKEDITOR.instances[elt.find('>textarea').attr('id')];
				if (!! cked){
					setTimeout(function() {
						cked.setData(value);
					}, 0);
				}
			},
			_get : function(elt, def){
				var cked = CKEDITOR.instances[elt.find('>textarea').attr('id')];
				if (!! cked){
					return cked.getData();
				}
			},
			_destroy : function(elt, def){
				var cked = CKEDITOR.instances[elt.find('>textarea').attr('id')];
				var content = cked.getData();
				if (!! cked){
					cked.destroy(true);
				}
				elt.empty();
				elt.html(content);
			}
		},

		'TAG' : {
			_build : function(elt, def){
				var conf = confman.getStdTagit();
				$.extend(true, conf, {
					availableTags : _.map(def.options, function(o){
						return o.label;
					})
				});
				var ul = this._findUL(elt);
				ul.squashTagit(conf);
			},

			_set : function(elt, def, value){
				var i;
				var ul = this._findUL(elt);
				if (ul.data('squashTagit') === undefined){
					ul.empty();
					for (i=0;i<value.length;i++){
						ul.append('<li>'+ value[i] +'</li>');
					}
				}
				else{
					ul.squashTagit('removeAll');
					for (i=0;i<value.length;i++){
						ul.squashTagit('createTag', value[i]);
					}
				}
			},

			_get : function(elt, def){
				return this._findUL(elt).squashTagit('assignedTags');
			},

			_destroy : function(elt, def){
				this._findUL(elt).squashTagit('destroy');
			},

			_findUL : function(elt){
				return (elt.is('ul')) ? elt : elt.find('ul');
			}
		}

	};

	$.fn.editableCustomfield = function(){

		if (arguments.lenghth === 0){
			throw "cannot invoke staticCustomfield with no arguments";
		}

		var def, widg;

		// case constructor
		if (arguments.length === 1 && _.isObject(arguments[0])){
			def= arguments[0];
			this.each(function(idx, elt){

				var $this = $(elt);

				// save the configuration
				$this.data("cufdef", def);

				widg = widgets[def.inputType.enumName];
				widg._build($this, def);

			});
		}

		// case getter
		else if (arguments.length === 1 && arguments[0] === "value") {
			def = this.data('cufdef');
			widg = widgets[def.inputType.enumName];
			return widg._get(this, def);
		}

		// case setter
		else if (arguments.length === 2 && arguments[0] === "value"){
			def = this.data("cufdef");
			widg = widgets[def.inputType.enumName];
			widg._set(this, def, arguments[1]);
		}

		// case destroy
		else if (arguments.length === 1 && arguments[0] === "destroy"){
			this.each(function(idx, elt){
				var $this = $(elt);
				def = $this.data('cufdef');
				widg = widgets[def.inputType.enumName];
				widg._destroy($this, def);
			});
		}
	};

});
