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
 * Turns each elements in the matched set to a static custom field (meaning non editable). 
 * 
 *
 * .staticCustomfield({conf}) : the constructor, conf must be a CustomFieldModel.
 * 
 * .staticCustomfield("value") : gets the value
 * 
 * .staticCustomfield("value", value) : sets the value.
 * 
 * .staticCustomfield("destroy") : destroys the custom field.
 * 
 */
define(["jquery", "./cuf-values-utils", "underscore", "squash.configmanager"], function($, utils, _, confman){
	
	if ($.fn.staticCustomfield !== undefined){
		return;
	}
	
	
	var widgets = {
		
		'PLAIN_TEXT' : {
			_build : function(elt, def){
				
			},
			_set : function(elt, def, value){
				elt.text(value);
			},
			_get : function(elt, def){
				return elt.text();
			},
			_destroy : function(elt, def){
				
			}
	
		},

		'NUMERIC' : {
			_build : function(elt, def){
				
			},
			_set : function(elt, def, value){
				elt.text(value);
			},
			_get : function(elt, def){
				return elt.text();
			},
			_destroy : function(elt, def){
				
			}
	
		},
		
		'CHECKBOX' : {
			_build : function(elt, def){
				var checked = (elt.text().toLowerCase() === "true") ? true : false;
				elt.empty();
				var chkbx = $('<input type="checkbox"/>');
				chkbx.prop('checked', checked);
				elt.append(chkbx);
				chkbx.enable(false);
			},
			_set : function(elt, def, value){
				var chkbx = elt.find('input[type="checkbox"]'); 
				if (value === true || value === "true"){
					chkbx.prop("checked", true);
				}
				else{
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
				
			},
			_set : function(elt, def, value){
				elt.text(value);
			},
			_get : function(elt, def){
				return elt.text();
			},
			_destroy : function(elt, def){
				
			}
		},
		
		'DATE_PICKER' : {
			_build : function(elt, def){
				var date = elt.text();
				var formatted = utils.convertStrDate($.datepicker.ATOM, def.format, date);
				elt.text(formatted);
			},
			_set : function(elt, def, value){
				var formatted = utils.convertStrDate($.datepicker.ATOM, def.format, value);
				elt.text(formatted);
			},
			_get : function(elt, def){
				var content = elt.text();
				return utils.convertStrDate(def.format, $.datepicker.ATOM, content);
			},
			_destroy : function(elt, def){
				var date = this._get(elt, def);
				elt.text(date);
			}
		},
	
		'RICH_TEXT' : {
			_build : function(elt, def){
				
			},
			_set : function(elt, def, value){
				elt.html(value);
			},
			_get : function(elt, def){
				elt.html();
			},
			_destroy : function(elt, def){
				
			}
		},
		
		'TAG' : {
			_build : function(elt, def){
				var conf = confman;
				elt.squashTagit(conf);
				elt.squashTagit('disable');
			},
			
			_set : function(elt, def, value){
				var i;
				var ul = elt.find('>ul');
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
				return elt.find('>ul').squashTagit('assignedTags');
			},
			
			_destroy : function(elt, def){
				elt.squashTagit('destroy');
			}
		} 
			
	};
	
	
	$.fn.staticCustomfield = function(){
		
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
				$(this).data("cufdef", def);
				
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