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
define(["jquery", "../domain/FieldValue"], function($, FieldValue){

	return {
		
		options : {
			rendering : {
				inputType : {
					name : "dropdown_list"
				}
				
			}
		},
		
		fieldvalue : function(fieldvalue){
			if (fieldvalue===null || fieldvalue === undefined){
				var opt = this.element.find('option:selected');
				var typename = this.options.rendering.inputType.dataType;
				
				return new FieldValue(opt.val(), typename, opt.text());
			}
			else{
				this.element.val(fieldvalue.id);
			}
		}, 
		
		createDom : function(field){
			var select = $('<select />', {
				'data-widgetname' : 'dropdown_list',
				'data-fieldid' : field.id
			});
			
			var options = field.possibleValues;
			var opt;
			for (var i=0, len = options.length; i<len;i++){
				opt = $('<option>', {
					'text' : options[i].scalar,
					'value' : options[i].id
				});
				
				select.append(opt);
			}
			
			return select;
		}
	};

});
