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
define(["jquery", "jqueryui"], function($){

	var searchwidget = $.widget("search.searchExistsWidget", {
		
		options : {

		},
		
		_create : function(){
			this._super();
		},
		
		fieldvalue : function(value){
			
			if(!value){
			var checked = $($(this.element.children()[0]).children()[0]).prop('checked');
			var selected = $($(this.element.children()[0]).children()[1]).val();
			if(checked){
				if(selected == 1){
					return {"type" : "RANGE",
					    "minValue" : 1,
						"maxValue" : null};
				} else {
					return {"type" : "RANGE",
					    "minValue" : null,
						"maxValue" : 0};
				}
			} else {
				return null;
			}
		} else {
			if(value.minValue == 1){
				$($(this.element.children()[0]).children()[0]).attr('checked', 'checked');
				$("option[value=1]", $($(this.element.children()[0]).children()[1])).attr("selected", "selected");
			} 
			if(value.maxValue === 0){
				$($(this.element.children()[0]).children()[0]).attr('checked', 'checked');
				$("option[value=0]", $($(this.element.children()[0]).children()[1])).attr("selected", "selected");			
			}
		}

		}, 
		
		createDom : function(id, options){
			
			var input = $("select", this.element);
			
			var opt;
			for (var i=0, len = options.length; i<len;i++){
				opt = $('<option>', {
					'text' : options[i].value,
					'value' : options[i].code
				});
				opt.html(options[i].value);
				input.append(opt);
			}
		}
	 });
	return searchwidget;
});
