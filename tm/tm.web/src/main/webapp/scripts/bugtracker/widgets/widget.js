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
 * 1/ This is the base widget, of which all other widget will inherit. 
 * 
 * 2/ It's also a documentation on what a widget should be.
 * 
 * A Widget is a jQuery widget, that is not registered yet : the calling registry needs to register it in its own context. That is why you should not 
 * register your widget yourself (eg, with $.widget('my.widget', <widget def>) : you must return that <widget def>. See the various documentation on creating widget for details.
 * 
 * As for any jQuery widget you can override methods and define your own, as long as you implements the API described below. 
 * 
 * The constructor of the widget will be invoked using an argument of type 'field', see below. The widget is editable only if 
 * field.rendering.operations[] is not empty. This array might contain "set", "add", "remove", that you may use if you like to. see method 
 * fieldvalue(fieldvalue) below.    
 * 
 * 
 * --------------
 * 
 * Possible arguments : 
 * - field : see 'options' below, or also org.squashtest.tm.bugtracker.advanceddomain.Field
 * - fieldvalue : see ../domain/FieldValue, or also org.squashtest.tm.bugtracker.advanceddomain.FieldValue
 */
 
define(["jquery", "../domain/DelegateCommand"], function($, DelegateCommand){
	
	return {
		
		options : {
			//defaults value for the field
			id : null,
			label : null,
			possibleValues : [],
			rendering : {
				operations : [],
				inputType : {
					name : "unknown",
					original : "unknown",
					dataType : null,
					fieldSchemeSelector : false,
					configuration : {}
				},
				required : false
			},
			_delegateurl : ""//used internally, you are not supposed to care about that one. But if you really want to know, WidgetFactory in advanced-field-view.js sets it 
					//when the widget instance is created.
		},
		
		_create : function(){
			//whatever you need. You will find the arguments in this.options
			if (! this.canEdit()){
				this.disable();
			}
		},
		
		canEdit : function(){
			return (this.options.rendering.operations.length!==0);
		},
		
		disable : function(){
			//if doesn't exist, you need to declare and implement it
			this.element.prop('disabled', true);
		},
		
		enable : function(){
			//same remark here
			if (this.canEdit()){
				this.element.prop('disabled', false);
			}
		},
		
		fieldvalue : function(fieldvalue){
			//if fieldvalue is null or undefined, acts as a getter. Else, it's a setter.
		},
		
		//if you have fileuploads, have them all in the <form/> object that will be returned by this function
		getForm : function(){
			return null;
		},
		
		createDom : function(field){
			/*
			 * create the dom element that best fits this field. This dom element is returned as a jquery object.
			 * The following attributes MUST be set : 
			 * - data-widgetname : the name of this widget
			 * - data-fieldid : the id of this field, ie field.id
			 */

		},
		
		
		/*
		 * Will ask Squash server to forward a DelegateCommand to the bugtracker connector, then invoke the callback with the result if success 
		 * or error with the xhr if error
		 * 
		 */
		sendDelegateCommand : function(command, callback, fnError){

			var url =this.options._delegateurl;
			
			if (url.length===0){
				if (window.console && window.console.log){
					console.log('bugtracker widget : no url for delegate command was supplied, request is aborted');
				}
				if (!!fnError){
					fnError(null);
				}
			}
			
			
			$.ajax({
				url : url,
				dataType : 'json', 
				contentType : 'application/json',
				data : JSON.stringify(command),
				type : 'POST'
			}).success(function(json){
				if (!!callback){
					callback(json);
				}
			}).error(function(xhr, err){
				if (window.console && window.console.log){
					console.log('command '+command.command+' : error : '+err);
				}
				if (!!fnError){
					fnError(xhr);
				}
			});
			
		},
		
		validate : function(){
			
			var messages = [];
			
			if(this.options.rendering.required){
				if(!this.fieldvalue().scalar && !this.fieldvalue().composite.length){
					messages[0] = "validation.error.fieldCannotBeEmpty";
				}
			}
			
			else{
				$(".issue-field-message-holder", this.element.parent().parent()).hide();
			}
			return messages;
		}
		
		
	};
	
});