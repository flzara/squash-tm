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
define(["jquery", "../domain/FieldValue", "jqueryui"], function($, FieldValue){

	//expects 'this' to be the widget instance
	function configureAutocomplete(){
		
		var command = this.options.rendering.inputType.configuration.onchange;
		
		if (!!command){
			
			var self=this;
			
			var autoconf = {
				source : function(search, callback){
					self.sendDelegateCommand( {command : command, argument : search.term}, 
                                            function(res){
                                                    proposal = new FieldValue(res);	//sort of "cast as" FieldValue
                                                    callback(proposal.getName().split(/,\s*/));
                                            }, 
                                            function(xhr){
                                                //nothing, we don't want the widget to fail
                                        });				
				}
			};
			
			this.element.autocomplete(autoconf);
			
		}
		
	}
	
	return {
		
		options : {
			rendering : {
				inputType : {
					name : "checkbox",
					configuration : {}
				}
				
			}
		},
		
		_create : function(){
			
			this._super();
			
			var configuration = this.options.rendering.inputType.configuration;
			
			if (!!configuration.onchange){
				configureAutocomplete.call(this);
			}
		},
		
		fieldvalue : function(fieldvalue){
			if (fieldvalue===null || fieldvalue === undefined){
				var value = this.element.eq(0).is(':checked');
				var typename = this.options.rendering.inputType.dataType;
				
				return new FieldValue("--", typename, value);
			}
			else{
				this.element.attr('checked', false);
			}
		}, 
		
		validate : function(){
			
			var messages = [];
			return messages;
		},
		
		createDom : function(field){
			var input = $('<input />', {
				'type' : 'checkbox',
				'data-widgetname' : 'checkbox',
				'data-fieldid' : field.id
			});

			
			
			return input;
		}
	};

});