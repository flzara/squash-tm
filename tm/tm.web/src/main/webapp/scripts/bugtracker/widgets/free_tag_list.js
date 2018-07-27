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
 * That widget is awkward because it delegates almost all of its behavior to $.squashTagit. Separating the btwidget and the squashTagit widget is 
 * a good way to prevent undesirable clashes with the type definition
 * 
 */

define(["jquery", "../domain/FieldValue", "jqueryui", "jquery.squash.tagit"], function($, FieldValue){

	return {
		
		options : {
			
			id : null,
			possibleValues : [],
			rendering : {
				operations : [],
				inputType : {
					name : "free_tag_list"
				},
				required : false
			}
			
		},
		
		_create : function(){
					
			var delegate = this._createDelegate();
			
			var tags = this._createTags();
			
			var config = {
				singleFieldNode : this.element
			};
			
			delegate.squashTagit(config);
	
			if (! this.canEdit()){
				this.disable();
			}
			
		},		
		
		canEdit : function(){
			return (this.options.rendering.operations.length!==0);
		},
		
		_getDelegate : function(){
			return this.element.next('ul.bt-delegate');
		},
		
		_createDelegate : function(){
			var elt = $("<ul/>",{ 'class' : 'bt-delegate' });
			this.element.after(elt);			
			return elt;
		},
		
		_createTags : function(){

			var possibleValues = this.options.possibleValues;
			
			//build the autocomplete source
			var tags = [];
			for (var i=0, len = possibleValues.length ; i < len; i++){
				tags.push(possibleValues[i].scalar);
			}			
			
			return tags;
		},
		
		fieldvalue : function(fieldvalue){
			var i;
			var delegate = this._getDelegate();
			
			if (fieldvalue===null || fieldvalue === undefined){
				
				var field = this.options;
				var typename = field.rendering.inputType.dataType;
	
				var selected = delegate.squashTagit('assignedTags');
				var allValues = [];
				
				for (i=0;i<selected.length;i++){					
					var label = selected[i];					
					var value = this.findValueByLabel(label);					
					if (value===null){
						//value not found, let's create a new one
						value = new FieldValue("--", typename, label);
					}					
					allValues.push(value);
				}

				return new FieldValue(field.id, "composite", allValues);
			}
			else{
				delegate.squashTagit('removeAll');
				var values = fieldvalue.composite;
				for (i=0;i<values.length;i++){
					delegate.squashTagit('createTag', values[i].scalar);
				}				
			}
			
		},
		
		//search the label among the possible values, returns null if value was not found
		findValueByLabel : function(label){
			
			var values = $.grep(this.options.possibleValues, function(item){
				return (item.scalar === label);
			});
			
			if (values.length>0){
				return values[0];
			}
			else{
				return null;
			}		
			
		},
		

		disable : function(){
			this._getDelegate().squashTagit('disable');
		},
		
		enable : function(){
			this._getDelegate().squashTagit('enable');
		},
		
		createDom : function(field){
			var elt = $('<input/>',{
				'data-widgetname' : 'free_tag_list',
				'data-fieldid' : field.id,
				'type' : 'text',
				'id' : "bttaglist-"+field.id,
				'class' : 'not-displayed'
			});
			
			return elt;
		}
		
		
	};
	
});