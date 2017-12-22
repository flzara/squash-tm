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
define(["moment"], function(moment) {
	
/*
 * This implementation is incomplete and WILL need improvement if we desire exotic formats like 'day in year' and such.
 * The target of the conversion is the momentjs convention (http://momentjs.com/docs/#/displaying/format/) 
 * 
 * Currently converts :
 *	
 *		java		|		js				|	notes on cardinality
 *		------------|-----------------------|-----------------------
 *		y			|		Y				|	cardinalities are the same
 *		d			|		D				|	cardinalities are the same
 *		m			|		M				|	cardinalities are the same
 *		'T'			|		T				|	cardinalities are the same
 *		Z			|		ZZ				|	1 java 'Z' = 2 js 'Z'. Note : this one is not the iso 8601 but the rfc 822 timezone indicator
 *
 *
 * For future, if we need to implement them some day : 
 *
 *		E			|		dd				|	not exactly true, the correct substitution is dd+d*(count(E)) but whatever
 *		u			|		d				|	only one 'd' js-side regardless the number of 'u' java side
 *		a			|		A				|	cardinalities are the same
 *
 */
	function _javaToJSFormat(javaFormat){
		if (javaFormat !== undefined){
			return javaFormat.replace(/y/g, 'Y').replace(/d/g, 'D').replace(/m/g, 'M').replace(/'T'/g, 'T').replace(/Z/g, 'ZZ');
		}
		else{
			return undefined;
		}
	}

/*
 * applies the expected localization to an instance of momentjs
 */
	function applyLocale(_momentInstance, locale){
		var _locale = locale;
	
		// if not supplied, defaults to the ... defaults.
		if (_locale === null || _locale === undefined){
			_locale = squashtm.app.locale;
		}
		
		// if still undefined, arbitrarily defaults to globish
		if (_locale === null || _locale === undefined){
			_locale = 'en';
		}
		
		_momentInstance.lang(_locale);
		
	}
	
	
	return {
		
		/*
		 * This format is public, hence uses the official Java format. 
		 * The corresponding moment.js-compatible format would be obtained using _javaToJSFormat on it.
		 */
		ISO_8601 : "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
		
		
		/*
		* Accepts : 
		*	1/ format(Unknown value, string format) : returns the given date as string using the given format.
		*	The date can be a numeric timestamp, a Date instance or a string. If String, the ATOM (ISO 8601) format is assumed. 
		*	
		* 2/ format(string value, string toFormat, String fromFormat) : convert the date
		*		given as String, parsed using fromFormat, and converted to toFormat
		*/
		format : function(value, toFormat, fromFormat) {
		
			var _fromFormat = _javaToJSFormat(fromFormat),
				_date = this.parse(value, _fromFormat),
				_toFormat = _javaToJSFormat(toFormat);
			
			var	_momentInstance = moment(_date);
			applyLocale(_momentInstance);
			
			if (!! toFormat){
				return _momentInstance.format(_toFormat);
			}
			else{
				return _momentInstance.toISOString();
			}
		},
		

		/*
		* @ params: 
		*  value : string value of the date, or numeric timestamp, or even a Date.
		*  format : string dateformat. if value is a string and the format is not specified, ATOM is assumed.
		* 
		*/
		parse : function(value, format) {
			
			var _date,
				_type = typeof value,
				_format = _javaToJSFormat(format);
		
			switch(_type){
			case "number" : _date = new Date(value); 
							break;
							
			case "object" : _date = value; 
							break;
							
			case "string" :
							var _instance = (!! _format) ? moment(value, _format) :
															moment(value);		//ATOM is assumed
							_date = _instance.toDate();
							break;
					
			default : throw "dateutils.format : cannot handle supplied argument";
			}
			
			return _date;
		},
		
		/*
		 * Checks whether this string represents a date that actually exists.
		 * This uses the fact that javascript will round an invalid date to its 
		 * nearest valid date.
		 * 
		 * thanks to http://michiel.wordpress.com/2007/07/02/how-to-validate-a-date-in-javascript/ 
		 * for the inspiration
		 * 
		 * @params
		 * value : string representation of a date
		 * format : the format for that value, defaults to ISO_8601 if not specified
		 */
		dateExists : function(value, format){
			var _fixedvalue = this.format(value, format, format);
			return (value === _fixedvalue);
		}
		
		
	};
});