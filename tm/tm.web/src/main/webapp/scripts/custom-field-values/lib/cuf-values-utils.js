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
define([ "jquery", "squash.configmanager", "squash.translator", "jqueryui" ], 
		function($, confman, translator) {


	var fromTestCase = " ("+translator.get("label.fromTestCase")+") ";
	
	function convertStrDate(fromFormat, toFormat, strFromValue) {
		var date = $.datepicker.parseDate(fromFormat, strFromValue);
		return $.datepicker.formatDate(toFormat, date);
	}
	
	function addEmptyValueToDropdownlistIfOptional(cufDefinitions){
		if (cufDefinitions.optional){
			cufDefinitions.options.push({label:""});
		}
	}
	
	function registerHandlebarHelpers(handlebars){

		handlebars.registerHelper('ifequals', function(cuftype, expected, options) {
		  return (cuftype === expected) ? options.fn(this) : options.inverse(this);
		});
		
		handlebars.registerHelper('cuflabel', function(value){
			var cuf = value.binding.customField,
				lbl = cuf.label;
			return (cuf.denormalized) ? lbl+fromTestCase : lbl; 
		});

		handlebars.registerHelper('cufid', function(value){
			var prefix = (value.binding.customField.denormalized) ? "denormalized-cuf-value-" : "cuf-value-";
			return prefix + value.id;
		});
		
		handlebars.registerHelper('cufclass', function(value){
			return (value.binding.customField.denormalized) ? "denormalized-custom-field" : "custom-field";
		});

	}
	
	
	return {
		convertStrDate : convertStrDate,
		addEmptyValueToDropdownlistIfOptional : addEmptyValueToDropdownlistIfOptional,
		registerHandlebarHelpers : registerHandlebarHelpers
	};

	
	
});