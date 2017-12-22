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
define([ "jquery", "squash.translator", "datepicker/jquery.squash.datepicker-locales", "squash.dateutils", "underscore" ],
		function($, translator, regionale, dateutils, _) {

	function stdJeditable(){

		var lang = translator.get({
			submit : "label.Confirm",
			cancel : "label.Cancel",
			placeholder : "rich-edit.placeholder"
		});

		lang.placeholder = '<span class="small txt-discreet">' + lang.placeholder + '</span> ';

		return $.extend(lang, {
			width : '80%',
			maxlength : 255,
			indicator : '<div class="processing-indicator"/>',
			onblur : function() {
			},
			// abort edit if clicked on a hyperlink (being the tag itself or its content)
			onedit : function(settings, editable, evt){
				var $target = $(evt.target);
				var canOpen = ! ( $target.is('a') || $target.parents('a').length > 0);
				if (canOpen){
					// relay to any listener that the widget will enter the editable state
					$(editable).trigger('onedit.editable', { settings : settings, editable : editable, event : evt});
				}
				return canOpen;
			}
		});

	}

	function jeditableSelect(){
		var lang = translator.get({
			submit : "label.Confirm",
			cancel : "label.Cancel",
			placeholder : "rich-edit.placeholder"
		});

		return $.extend(lang, {
			type : 'select',
			width : '80%',
			maxlength : 255,
			indicator : '<div class="processing-indicator"/>',
			onblur : function() {},
			callback : function(value, settings){
				$(this).text(settings.data[value]);
			}
		});
	}


	function stdCkeditor() {
		var lang = translator.get('rich-edit.language.value');

		//[Issue 6013]
		CKEDITOR.on('dialogDefinition', function(ev) {
			try {
				var dialogName = ev.data.name;
				var dialogDefinition = ev.data.definition;
				if(dialogName == 'link') {
					var informationTab = dialogDefinition.getContents('target');
					var targetField = informationTab.get('linkTargetType');
					targetField['default'] = '_blank';
				}
			} catch(exception) {
				alert('Error ' + ev.message);
			}
		});

		return {
			customConfig : squashtm.app.contextRoot + '/styles/ckeditor/ckeditor-config.js',
			lang : lang/*,
			disallowedContent : 'img'*/
		};
	}


	function jeditableCkeditor(){
		var ckconf = stdCkeditor(),
			jedconf = stdJeditable();

		return $.extend(true,
			jedconf,
			{
				cols : 80,
				rows : 10,
				type : 'ckeditor',
				ckeditor : ckconf
			}
		);
	}

	function destroyCkeditor(target){
		var t = (target instanceof jQuery) ? target : $(target);
		if (t === undefined ||  t.length === 0) {
			console.log("WARN Cannot destroy ckeditor for unknown target", target);
		}
		try {
			CKEDITOR.instances[t.attr('id')].destroy(true);
		} catch (e) {
			// do nothing
		}
	}

	/*
	 * @params (optionals)
	 *	format : a string date format that datepicker understands
	 *	locale : a locale, used for datepicker internationalization
	 */
	function stdDatepicker(url, format, locale){

		// fetch the optional parameters if unspecified
		var fetchmeta = {},
			conf = {};

		if (!! format) { conf.format = format; } else { fetchmeta.format = 'squashtm.dateformatShort.datepicker'; }
		if (!! locale) { conf.locale = locale; } else { fetchmeta.locale = 'squashtm.locale'; }

		var translated = translator.get(fetchmeta);
		$.extend(conf, translated);

		// now configure the datepicker
		var language = regionale[conf.locale] || regionale;

		return $.extend(true, {}, {dateFormat : conf.format}, language);
	}


	function stdTagit(){
		return {
			allowSpaces : true,
			autocomplete: {
				delay: 0
			},
			validate : function(label){
				return (label.indexOf('|') === -1);
			}
		};
	}


/*
 *	Useful when creating the options for a jeditable select.
 *  This function will turn an array of [{ id : <id1>, name : <name1>}, {id : <id2>, name : <name2>}]
 *  into something acceptable for a jeditable select, like { <id1> : <name1>, <id2> : <name2> }.
 *
 *  A/ Two arguments : an array of data and a descriptor object
 *  -----------------------------------------------------------
 *
 *  The array of data is described above. The descriptor object must be of the form
 *  { "valuePropertyName" : "labePropertyName" }. This tells the function that it
 *  should build an option using the property "valuePropertyName" as the value
 *  and proprety "labelPropertyName" as the text displayed.
 *
 *
 *  B/ One arguments only : an array of data
 *  -----------------------------------------
 *
 *  In the absence of a descriptor object the value and label will be determined as follow :
 *
 *  - id : mandatory. Will be the 'value' of the 'option' object that'll be created from this object.
 *  - name : (optional) if found, will be used as a 'label' for that option.
 *  - value : (optional) if found, will be used as a 'label' for that option.
 *  - (anything else) : if none of 'name' or 'value' are found, that third property will be used instead.
 *						Works only of the objects have 2 own properties only (one of which being 'id'),
 *						and must not be an object nor an array.
 *
 *  if none of 'name', 'value' or the 'anything else' is satisfied the script will crash and tell you why.
 *
 */

	function _toJeditableSelectFormat(){
		var data;

		if (arguments.length === 1){
			data = _unaryJeditSelectFormat(arguments[0]);
		}
		else {
			data = _binaryJeditSelectFormat(arguments[0], arguments[1]);
		}

		// jeditable doesn't like much 'null' as a key
		if (data[null] !== undefined){
			data[""] = data[null];
			delete data[null];
		}
		return data;

	}

	function _unaryJeditSelectFormat(data){

		if (data.length===0){
			return {};
		}

		// let's analyze the input data and check if they are suitable.
		var sample = data[0],
			keys = _.keys(sample);

		var hasID = (sample.id !== undefined),
			usableLabel =	(sample.name !== undefined) ? 'name' :
							(sample.value !== undefined) ? 'value' :
							(keys.length === 2 && hasID) ? keys[ (_.indexOf(keys, 'id') +1) % 2] :
							undefined;

		if (hasID === false || usableLabel === undefined){
			throw "configmanager : unable to convert input data to jeditable select format. Please supply "+
				"an array of { id : <id>, value : <value> }";
		}

		// now we can work
		var result = {};
		for (var i=0; i<data.length; i++){
			var elt = data[i];
			result[elt['id']] = elt[usableLabel];
		}

		return result;

	}

	function _binaryJeditSelectFormat(data, descriptor){
		if (data.length===0){
			return {};
		}

		// analyze the descriptor. The descriptor is supposed to have only one property as
		// describe in the main comment section
		var valuePpt,
			labelPpt;

		for (var ppt in descriptor){
			valuePpt = ppt;
			labelPpt = descriptor[ppt];
		}

		// now build the output
		var result = {};
		for (var i=0; i<data.length; i++){
			var elt = data[i];
			result[elt[valuePpt]] = elt[labelPpt];
		}

		return result;
	}



	/* ***************************************************************************
	 * EXPERIMENTAL
	 * @params
	 *	postfunction : function(value) => xhr : a function that just posts the data and returns the xhr object(required)
	 *	format : a string date format that datepicker understands (optional)
	 *	locale : a locale, used for datepicker internationalization (optional);
	 * ***************************************************************************/

	function jeditableDatepicker(postfn, format, locale){

		var conf = stdJeditable();

		var datepickerconf = stdDatepicker(format, locale);

		conf.target = function(value){
			var date = $("form input", this).datepicker("getDate");
			var toAtom = dateutils.format(date);

			postfn(toAtom)
			.done(function(){
				return value;
			})
			.fail(function(){
				return self.revert;
			});
		};

		conf.type = "datepicker";
		conf.datepicker = datepickerconf;

		return conf;
	}



	return {
		getStdCkeditor : stdCkeditor,
		destroyCkeditor : destroyCkeditor,
		getStdJeditable : stdJeditable,
		getStdDatepicker : stdDatepicker,
		getJeditableCkeditor : jeditableCkeditor,
		getJeditableSelect : jeditableSelect,
		toJeditableSelectFormat : _toJeditableSelectFormat,
		getStdTagit : stdTagit,
		getJeditableDatepicker : jeditableDatepicker	// experimental
	};

});
