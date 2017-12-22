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
 * How to :
 *
 * the method 'get' accepts a simple javascript object, which values are either string or other simple javascript objects.
 * See it as a composite map of which the leaves are strings.
 *
 *  Those strings are keys for i18n properties.
 *
 *  The function returns the resolved object, of which the keys has been replaced by the corresponding value.
 *  
 *
 * Example :
 *
 *  {
 *		title : 'widget.title',
 *		browse : {
 *			back : 'widget.back',
 *			forth : 'widget.forth'
 *		},
 *		buttons : {
 *			cancel : 'label.Cancel',
 *			confirm : 'label.Confirm'
 *		}
 *	}
 *
 * 
 * ------------ support for parameterized messages : ----------------
 *
 * Additionally to the 'get(object)' signature, it also supports get(string, param1,...). When used that way, the string argument will have 
 * its placeholders filled with the extra parameters, provided the resulting string contains placeholders like {0}, {1} etc. Parameter 
 * substitution is positional : param1 will be substituted to placeholder {0}, param2 to {1} etc.
 *
 *
 *
 * The method `load()` takes the same *object* arg as `get()` and performs an *asunc* request to fill the cache if needed. It does not return anything.
 */
define(["jquery", "underscore", "workspace.storage"], function($, _, storage){
	"use strict";

	var serviceURL = window.squashtm.app.contextRoot+"/localization/filler";
	var ajaxDefaults = {
			url : serviceURL,
			headers : {
				'Content-Type' : 'application/json'
			},
			dataType : 'json',
			type : 'POST'
		};

	//initialization
	window.squashtm = window.squashtm || {};
	window.squashtm.message = window.squashtm.message || {};
	// sometimes, there's no w.s.a.locale, we fall back on navigator.language. Note that IE<11 use another prop but it somewhat works anyway.
	var locale = window.squashtm.app.locale || window.navigator.language;
	var KEY = "squashtm.message-"+ locale;
	window.squashtm.message.cache = storage.get(KEY) || window.squashtm.message.cache || {};


	// ************ ajax functions *************

	function _ajax(object){
		var result = "lol";
		var params = _.defaults({
			data : JSON.stringify(object),
			async : false,
			success: function(json) {
				result = json;
			},
			error: function() {
				window.console.log("Error while fetching messages", object, arguments);
			}
		}, ajaxDefaults);

		$.ajax(params);

		return result;
	}

	// ************** caching function **************


	/*
	 * returns an array of two objects. The first object is the part that could be found in cache. The second one is the object that remains to
	 * be looked up using ajax. If nothing remains to be looked up, null will be returned instead.
	 */

	function _split(oQuery){

		var cached = {};
		var remains = {};

		var _i18nkey = null;

		//let's iterate over the properties of the object query
		for (var ppt in oQuery){

			_i18nkey = oQuery[ppt];

			//case 1 : the property is a string, and is expected to be a _18nkey
			if (typeof _i18nkey === "string"){
				var _cachedValue = window.squashtm.message.cache[_i18nkey];
				if (_cachedValue!==undefined){
					cached[ppt] = _cachedValue;	//value is found : the translation is attached to the 'cached' object
				}
				else{
					remains[ppt] = _i18nkey;	//value not found : the key remains attached to the 'remains' object
				}
			}
			//case 2 : the property is an object that we must inspect thoroughly, and of which the split results must be appended to their respective result objects.
			else if (_.isObject(_i18nkey) && ! _.isFunction(_i18nkey)){
				var _sub = _split(_i18nkey);
				var _subcached = _sub[0];
				var _subremains = _sub[1];

				if (! _.isEmpty(_subcached)){
					cached[ppt] = _subcached;
				}

				if (! _.isEmpty(_subremains)){
					remains[ppt] = _subremains;
				}
			}
		}

		return [cached, remains];
	}


	//the structure of those objects are expected to be rigorously identical.
	// the parameter _shouldstore is used internally.
	function _cache(keys, values, _shouldstore){

		var _i18nkey;
		var _i18nvalue;

		for (var ppt in keys){
			_i18nkey = keys[ppt];

			//if the property is a string : it is a i18key. The value is stored in the 'value' argument, indexed at the same property.
			if (typeof _i18nkey === "string"){
				_i18nvalue = values[ppt];
				window.squashtm.message.cache[_i18nkey] = _i18nvalue;
			}
			//if it's an object, let's cache its properties
			else{
				_cache(keys[ppt], values[ppt], false);
			}
		}

		if (_shouldstore !== false){
			storage.set(KEY, window.squashtm.message.cache);
		}
	}



	// ************ main functions ******************

	function getAsObject(object){
		var sorted = _sortMissing(object);

		//if there isn't anything left to lookup through ajax, return the result of the cache lookup
		if (_.isEmpty(sorted.missing)){
			return sorted.cached;
		}

		//if not, let's query the missing values
		var remainingValues = _ajax(sorted.missing);

		//store the result
		_cache(sorted.missing, remainingValues);

		//merge with what was fetched from cache earlier and return
		return $.extend(true, sorted.cached, remainingValues);
	}

	/**
	 * Sorts out che cached messages and the missing keys
	 * @return { cached: {hash of messages}, missing: {hash of keys} }
	 */
	function _sortMissing(keys) {
		var splitObject = _split(keys);

		return { cached: splitObject[0], missing: splitObject[1] };
	}

	/**
	 * Async loads missing keys into cache.
	 */
	function _load(keys) {
		var sorted = _sortMissing(keys);

		if (_.isEmpty(sorted.missing)) {
			return;
		}

		var params = _.defaults({
				data : JSON.stringify(sorted.missing)
			},
			ajaxDefaults);

		$.ajax(params).success(function(json){
			_cache(sorted.missing, json);
		});
	}

	function getAsString(/*string key, varargs string params*/){
		var str = arguments[0];
		var res = getAsObject({ query : str });
		
		var translated = res.query;
		if (arguments.length > 1){
			// here we match placeholder {0} pos with arguments[pos+1]
			var pos = 0,
				needle = null;
			
			while (pos < arguments.length -1){
				needle = new RegExp('\\\{'+pos+'\\\}', 'g');
				translated = translated.replace(needle, arguments[pos+1]);
				pos++;
			}
		}
		
		return translated;
	}

	return {
		get : function(){
			if (typeof arguments[0] === "string"){
				return getAsString.apply(this, arguments);
			} else {
				return getAsObject.apply(this, arguments);
			}
		},

		load: _load
	};


});
