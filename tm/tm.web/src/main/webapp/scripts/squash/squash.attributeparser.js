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
 * Will parse a string such as 'ppt1=val1, ppt2=val2, ...' into
 * {
 *	ppt1 : val1,
 *	ppt2 : val2
 * }
 * 
 * Note : the pptX can represent nested properties. For instance person.name.firstname = bob
 * would be parsed as 
 * {
 *	person : {
 *		name : {
 *			firstname : bob
 *		}
 *	}
 * }
 */
define(['jquery'], function($) {
	
	// ****************** properties-as-string parsing ***********************
	
	/*it is written that way so that only the first '=' will match 
	as a separator, allowing '=' to be a valid part of the value as well.
	It could have been written as 'atom.split(/\s*=(.+)?/,2);' but IE8 definitely 
	didn't want it. */
	function _parseAssignation(result, atom) {
		
		var path, value;
		
		var posequals = atom.indexOf('=');	//index of the first '='
		
		//'unary' assignation : set 'true' as value
		if (posequals === -1){
			path = $.trim(atom);
			value = true;
		}
		//'binary' assignation
		else{
			var members = [ atom.substr(0, posequals), atom.substr(posequals+1)];
			path = $.trim(members[0]);
			value = $.trim(members[1]);
		}
		
		// now parse the path (it may represent a nested attribute)
		var pathelts = path.split('.'),
			pathlength = pathelts.length,
			lastppt = pathelts[pathlength-1];
		
		var _pptpointer = result;
		for (var i = 0; i < pathlength -1; i++){
			var ppt = pathelts[i];
			if (_pptpointer[ppt] === undefined){
				_pptpointer[ppt] = {};
			}
			_pptpointer = _pptpointer[ppt];
		}
		_pptpointer[lastppt] = value;		

	}

	function _parseSequence(result, seq) {
		var statements = seq.split(/\s*,\s*/);
		var i = 0, length = statements.length;

		for (i = 0; i < length; i++) {
			var stmt = statements[i];
			var parser = (stmt.indexOf(',') !== -1) ? _parseSequence : _parseAssignation;
			parser(result, stmt);
		}

		return result;
	}
	
	// ************************* properties-in-object utilities *******************

	/**
	 * Given an 'object', will set its property 'property' to 'value'.
	 * The point of using this is that it can set nested properties, 
	 * provided that 'property' uses a dot separator.
	 * 
	 */
	function setValue(object, property, value) {
		var localO = object;
		var a = property.split('.');
		for ( var i = 0, iLen = a.length - 1; i < iLen; i++) {
			var ppt = a[i];
			if (localO[ppt] === undefined) {
				localO[ppt] = {};
			}
			localO = localO[ppt];
		}
		localO[a[a.length - 1]] = value;
	}

	/** 
	 * Given an 'object', will retrieve the value of 'property'. 
	 * The point of using this is that you can retrieve nested properties, 
	 * provided that 'property' uses a dot separator. 
	 * 
	 */
	function getValue(object, property) {
		var localO = object;
		var a = property.split('.');
		for ( var i = 0, iLen = a.length; i < iLen; i++) {
			localO = localO[a[i]];
		}
		return localO;
	}
	
	
	return {
		parse : function(seq){
			return _parseSequence({}, seq);
		},
		setValue : setValue,
		getValue : getValue
	};
	
});