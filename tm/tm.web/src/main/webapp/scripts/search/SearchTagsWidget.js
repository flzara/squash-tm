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
define([ "jquery", "squash.configmanager", "jqueryui", "jquery.squash.tagit" ], function($, confman) {

	var searchwidget = $.widget("search.searchTagsWidget", {

		options : {

		},

		_create : function() {
			this._super();
			
			// configure initial state
			var operation = this.element.find('select'),
				ul = this.element.find('ul');
			
			if (!! this.options.state){
			
				this.element.find('select').val(this.options.state.operation);
				
				$.each(this.options.state.tags, function(idx, tag){
					ul.append('<li>'+tag+'</li>');
				});
			}
			
			// tagit configuration
			var conf = confman.getStdTagit();
			conf.constrained=true;
			conf.autocomplete.source= $.map(this.options.available, function(elt){ return elt.value; });

			this.element.find('ul').squashTagit(conf);
			

			
		},

		fieldvalue : function(value) {

			var ul = this.element.find('ul'),
				operation = this.element.find('select');
			
			// case : getter
			if (!value) {

				var tags = ul.squashTagit("assignedTags");
				
				if (tags.length>0){
					return{
						"type" : "TAGS",
						"tags" : tags,
						"operation" : operation.val()
					};
				}else{
					return null;
				}
			} 
			
			//case : setter
			else {
				
				operation.val(value.operation);
				
				$.each(value.tags, function(idx, tag){
					ul.squashTagit("createTag", tag);
				});				
			}
		},

		createDom : function(id, options) {

		}
	});
	return searchwidget;
});
