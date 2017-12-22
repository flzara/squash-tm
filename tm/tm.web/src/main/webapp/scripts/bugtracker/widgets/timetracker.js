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
define([ "jquery", "../domain/FieldValue", "squash.translator", "handlebars" ], function($, FieldValue, translator, Handlerbars) {

	return {

		options : {
			rendering : {
				inputType : {
					name : "timetracker"
				}

			}
		},
		
		_create : function(){
			var self = this;
			this.element.bind('focusout', function(){
				self.autovalidate();
			});
		},
		
		getInput : function(num){
			return this.element.find('input:eq('+num+')');
		},
		
		fieldvalue : function(fieldvalue) {
			if (fieldvalue === null || fieldvalue === undefined) {
				
				var field = this.options;
				
				var original = this.getInput(0).val();
				var remaining = this.getInput(1).val();

				var originalValue = new FieldValue("originalEstimate", "string", original);
				var remainingValue = new FieldValue("remainingEstimate", "string", remaining);
				
				var allValues = [originalValue, remainingValue];
				 
				return new FieldValue(field.id, "composite", allValues);
				
			} else {
				this.element.val(fieldvalue.scalar);
			}
		},
		createDom : function(field) {
			
			var div = $('<span/>', {
				'type' : 'text',
				'data-widgetname' : 'timetracker',
				'data-fieldid' : field.id,
				'class' : 'full-width issue-field-control'
			});
			
			var label1 = $('<label/>', {'class' : 'issue-field-label'});
			var input1 = $('<input />');
			
			var label2 = $('<label/>', {'class' : 'issue-field-label'});
			var input2 = $('<input />');
			
			label1.text(translator.get("widget.timetracker.original-estimate"));
			label2.text(translator.get("widget.timetracker.remaining-estimate"));
			
			div.append(label1);
			div.append(input1);
			div.append("<br/>");
			div.append(label2);
			div.append(input2);
			
			return div;
		},
		
		validate : function(){
		
			var messages = [];
			
			var result1 = this.validateExpression(this.getInput(0).val());
			var result2 = this.validateExpression(this.getInput(1).val());
			
			if(!result1 || !result2){
				messages[0] = "validation.error.illformedTimetrackingExpression";
			}

			return messages;
		},
		
		autovalidate : function(){
			
			var messages = this.validate();
			
			$(".issue-field-message-holder", this.element.parent().parent()).text("");
			for(var i=0; i<messages.length; i++){
				$(".issue-field-message-holder", this.element.parent().parent()).append(translator.get(messages[i]));	
			}
			if(!!messages.length){
				$(".issue-field-message-holder", this.element.parent().parent()).show();
			}
		},
		
		/*
		 * The goal here is to check that :
		 *  1 - a blank string is fine, or
		 * 	2a - the expression is composed of digits immediately followed by one of w, d, h or m, and those sequences must be separated by a number of whitespaces of at least 1 
		 *  2b - and each letter may be used only once
		 *  
		 *  example of valid expression : 0w 1d 5h 2m
		 *  example of invalid expression : 0w 1w, 15k
		 */
		validateExpression : function(expression) {
			
			// the regular expressions
			
			var 
				// the "blank string" test
				test1 = /^\s*$/,
				// test2a must succeed, while test2b must fail
				test2a = /^(\d+[wdhm]\s+)*$/,
				test2b = /([wdhm]).*\1/;
			
			// preprocess the expression : 
			// remove extra spaces around, then add 1 at the end. 
			// This helps matching regex test1 while keeping it concise and readable
			var ex = $.trim(expression)+" ";
			
			var passed1 = test1.test(ex);
			var passed2a = test2a.test(ex);
			var passed2b = test2b.test(ex);
			
			return passed1 || ( passed2a && !passed2b);
			
		}
	};

});
