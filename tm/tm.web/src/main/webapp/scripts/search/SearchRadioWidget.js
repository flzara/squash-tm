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

	var searchwidget = $.widget("search.searchRadioWidget", {
		
		options : {
			ignoreBridge : false
		},

		_create : function(){
			this._super();
		},

		fieldvalue : function(value){

			var name = $($("input",this.element)[0]).attr("name");
			if(!value){
				var val = $("input[name='"+name+"']:checked").val();
				
				if(!!val){
					return {"type" : "SINGLE",
					    "value" : val,
					    "ignoreBridge": this.options.ignoreBridge};
				} else {
					return null;
				}
			} else {

				$("input[name='"+name+"'][value='"+value.value+"']").attr('checked', true);
			}

		}, 

		createDom : function(id, options){
			
			var input = $(".search-line", this.element);
			var table = $('<table>');
			input.append(table);
			
			var opt;
			for (var i=0, len = options.length; i<len;i++){
				opt = $('<input>', {
					'type' : 'radio',
					'name' : id,
					'value' : options[i].code
				});
				if(options[i].selected){
					opt.attr("checked", true);
				}
				var row = $("<tr style='vertical-align:top'>");
				var td1 = $('<td>');
				var td2 = $('<td>');
				row.append(td1);
				row.append(td2);
				td1.append(opt);
				td2.append(" "+options[i].value);
				table.append(row);
			}
		}
	 });
	return searchwidget;
});
