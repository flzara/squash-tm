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
define(
		[ "jquery", "./cuf-values-utils", "squash.configmanager", "underscore", "workspace.event-bus",
				"squash.translator", "app/ws/squashtm.notification", "app/util/StringUtil", "jqueryui", "jquery.squash.jeditable", "jeditable.datepicker",
				"datepicker/jquery.squash.datepicker-locales", "jquery.squash.tagit" ],
		function($, utils, confman, _, eventBus, translator, notification, StringUtils) {

			/* ***************************************************************************************************
			 *
			 * The following is a builder of postfunction for a jeditable. Its purpose is to
			 *
			 * It accepts three parameters :
			 * - idOrURLOrPostFunction : can be either
			 *		- the id of a custom field,
			 *		- an url that will be used as is,
			 *		- a function, that will be used as is,
			 *		- nothing, in which case the value of an attribute data-value-id on the element will be used
			 *
			 * - postProcess : if defined, postProcess will be invoked upon xhr completion
			 * - isDernomalized : if defined and true, the custom field will be treated as a denormalized cuf.
			 *
			 * ***************************************************************************************************/

			function buildPostFunction(idOrURLOrPostfunction, postProcess, isDenormalized) {

				var postProcessFn = postProcess || function(value) {
					return value;
				};

				var baseURL  = squashtm.app.contextRoot;
					baseURL += (isDenormalized) ? "/denormalized-fields/values/" : "/custom-fields/values/";

				var ajaxconf = {
					data : {},
					type : 'POST',
					contentType : "application/json;charset=UTF-8"

				};

				var postFunction;


				switch(typeof idOrURLOrPostfunction){

				// case : the argument is already a post function in its own rights
				case "function" :
					postFunction = idOrURLOrPostfunction;
					break;

				// case : the argument is a url. It will be used as is, along with the usual parameters
				case "string" :
					postFunction = function(value) {
						ajaxconf.url = idOrURLOrPostfunction;
						ajaxconf.data = JSON.stringify(value);
						return $.ajax(ajaxconf);
					};
					break;

				// case empty : the element must define an attribute 'data-value-id' and that ID will be used
				// just like in the default clause.
				case undefined :
					postFunction = function(value) {
						var id = $(this).data('value-id');
						ajaxconf.url = baseURL + id;
						ajaxconf.data = JSON.stringify(value);
						return $.ajax(ajaxconf);
					};
					break;

				// case : the argument is assumed to be a number, specifically the ID. We can then
				// define at which url we need to post.
				default :
					postFunction = function(value) {
						ajaxconf.url = baseURL + idOrURLOrPostfunction;
						ajaxconf.data = JSON.stringify(value);
						return $.ajax(ajaxconf);
					};
					break;

				}

				return function(value, settings) {
					var data = postProcessFn(value, settings);
					postFunction.call(this, data);
					return value;
				};

			}

			/* *********************************************************************
			 *
			 *		Define the custom fields now
			 *
			 * *********************************************************************/

			function getBasicConf() {

				return confman.getStdJeditable();
			}


			function initAsDatePicker(elts, cufDefinition, idOrURLOrPostfunction) {

				var conf = getBasicConf();

				var format = cufDefinition.format;
				var locale = cufDefinition.locale;

				conf.type = 'datepicker';
				conf.datepicker = confman.getStdDatepicker();

				_bindEmptyMandatoryCufErrorHandler(cufDefinition, conf, 'datePicker', false);

				elts.each(function(idx, el){
					var $el = $(el);
					var raw = $el.text();
					var formatted = utils.convertStrDate($.datepicker.ATOM, format, raw);
					$el.text(formatted);
				});

				var postProcess = function(value, settings) {
					return utils.convertStrDate(format, $.datepicker.ATOM,
							value);
				};

				var postFunction = buildPostFunction(idOrURLOrPostfunction,	postProcess, cufDefinition.denormalized);

				elts.editable(postFunction, conf);

			}


			function initAsList(elts, cufDefinitions, idOrURLOrPostfunction) {
				if (elts.length === 0){
					return;
				}



				utils.addEmptyValueToDropdownlistIfOptional(cufDefinitions);

				var prepareSelectData = function(options, selected) {

					var i = 0, length = options.length;
					var result = {};

					var opt;
					for (i = 0; i < length; i++) {
						opt = options[i].label;
						result[opt] = opt;
					}

					result.selected = selected;
					return result;

				};

				elts.each(function() {

					var jqThis = $(this);
					var selected = jqThis.text();

					var conf = getBasicConf();
					conf.type = 'select';
					conf.data = prepareSelectData(
							cufDefinitions.options, selected);

					var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinitions.denormalized);

					jqThis.editable(postFunction, conf);

				});
			}


			function initAsPlainText(elts, cufDefinition, idOrURLOrPostfunction) {

				var conf = getBasicConf();
				conf.type = 'text';

				_bindEmptyMandatoryCufErrorHandler(cufDefinition, conf, 'plainText', true);

				var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinition.denormalized);

				elts.editable(postFunction, conf);

			}


			function initAsCheckbox(elts, cufDefinition, idOrURLOrPostfunction) {

				if (elts.length === 0){
					return;
				}

				var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinition.denormalized);

				var clickFn = function() {
					var jqThis = $(this);
					var checked = jqThis.prop('checked');
					postFunction.call(jqThis, checked);
				};

				elts.each(function() {

					var jqThis = $(this);
					var chkbx;

					if (jqThis.is('input[type="checkbox"]')) {
						chkbx = jqThis;
					} else if (jqThis.find('input[type="checkbox"]').length > 0) {
						chkbx = jqThis.find('input[type="checkbox"]');
					} else {
						var checked = (jqThis.text().toLowerCase() === "true") ? true : false;
						jqThis.empty();
						chkbx = $('<input type="checkbox"/>');
						chkbx.prop('checked', checked);
						jqThis.append(chkbx);
					}

					chkbx.enable(true);
					chkbx.click(clickFn);

				});

			}


			function initAsRichtext(elts, cufDefinition, idOrURLOrPostfunction) {

				if (elts.length === 0){
					return;
				}

				var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinition.denormalized);

				var conf = confman.getJeditableCkeditor();

				_bindEmptyMandatoryCufErrorHandler(cufDefinition, conf, 'richText', true);

				elts.editable(postFunction, conf);

			}

			// a jeditable Tags isn't really jeditable : it's always in an editable state.
			// as such, to some extent it looks a lot like the initialization of
			// 'editableCustomfield'.
			function initAsTag(elts, cufDefinition, idOrURLOrPostfunction){

				var addEvtname = "cuf.tag-added";

				if (elts.length === 0){
					return;
				}

				var postFunction = buildPostFunction(idOrURLOrPostfunction, undefined, cufDefinition.denormalized);

				var conf = confman.getStdTagit();

				$.extend(true, conf, {
					availableTags : _.map(cufDefinition.options, function(elt){
						return elt.label;
					})
				});

				elts.squashTagit(conf);

				_bindEmptyMandatoryTagCufErrorHandler(cufDefinition, elts);

				elts.on('squashtagitaftertagadded squashtagitaftertagremoved', function(evt, ui){
					var elt = $(evt.currentTarget);
					var tags = elt.squashTagit('assignedTags');
					if (elt.squashTagit("validate", evt, ui)){
						postFunction.call(elt, tags);
					}
				});

				/* **************************************
				 *  Autocompletion list management
				 * **************************************/

				/*
				 * when a new tag is aded, notify other instances of this cuf
				 * that the tag list has changed
				 */
				elts.on('squashtagitaftertagadded', function(evt, ui){
					var elt = $(evt.currentTarget);
					var availableTags = elts.squashTagit('option').availableTags;

					if (elt.squashTagit("validate", evt, ui) &&
						(! _.contains(availableTags, ui.tagLabel))){

						eventBus.trigger(addEvtname, {
							code : cufDefinition.code,
							tagLabel : ui.tagLabel
						});

					}
				});

				/*
				 * listen to new tag events and add the new tag to its
				 * autocompletion list, if it comes from another instance
				 * of this custom field
				 */
				eventBus.onContextual(addEvtname, function(evt, data){
					if (data.code !== cufDefinition.code){
						return;
					}

					// all 'elts' elements share the same source so we
					// dont need to iterate over all 'elts'.
					elts.squashTagit('option').availableTags.push(data.tagLabel);

				});

			}


			function _displayEmptyMandatoryCufErrorPopup() {

				notification.showError(translator.get('message.emptyMandatoryCuf'));
			}

			function _emptyCondition(type, input) {
				switch(type) {
					case 'plainText':
					case 'datePicker':
						var test1 = StringUtils.isBlank(input.value);
						return StringUtils.isBlank(input.value);
					case 'richText':
						var test2 = StringUtils.isBlank(CKEDITOR.instances[input.id].document.getBody().getChild(0).getText());
						return StringUtils.isBlank(CKEDITOR.instances[input.id].document.getBody().getChild(0).getText());
					case 'tag':
						return input.length < 2;
				}
			}

			function _resetEditable(span) {

				$(span)[0].reset();
			}


			function _bindEmptyMandatoryCufErrorHandler(cufDefinition, conf, editableType, resetIsNeeded) {
				if(!cufDefinition.optional) {
					conf.onsubmit = function(settings, span) {
						var input = this[0][0];
						if(_emptyCondition(editableType, input)) {
							_displayEmptyMandatoryCufErrorPopup();
							if(resetIsNeeded) { _resetEditable(span); }
							return false;
						}
						return true;
					};
        }
			}

			function _bindEmptyMandatoryTagCufErrorHandler(cufDefinition, elts) {

				if(!cufDefinition.optional) {
      		elts.on('squashtagitbeforetagremoved', function(evt, ui) {
      			var elt = $(evt.currentTarget);
      			var tags = elts.squashTagit('assignedTags');
      			if(_emptyCondition('tag', tags)) {
      				_displayEmptyMandatoryCufErrorPopup();
      				return false;
      			}
      			return true;
      		});
      	}
      }

			/* ***************************************************************************
			*
			*										MAIN
			*
			* ***************************************************************************/

			$.fn.jeditableCustomfield = function(cufDefinition, idOrURLOrPostfunction) {

				var type = cufDefinition.itype||cufDefinition.inputType.enumName;

				switch(type){
				case "DATE_PICKER" : initAsDatePicker(this, cufDefinition, idOrURLOrPostfunction); break;
				case "DROPDOWN_LIST" : initAsList(this, cufDefinition, idOrURLOrPostfunction); break;
				case "PLAIN_TEXT" : initAsPlainText(this, cufDefinition, idOrURLOrPostfunction); break;
				case "CHECKBOX" : initAsCheckbox(this, cufDefinition, idOrURLOrPostfunction); break;
				case "RICH_TEXT" : initAsRichtext(this, cufDefinition, idOrURLOrPostfunction); break;
				case "TAG" : initAsTag(this, cufDefinition, idOrURLOrPostfunction); break;
				case "NUMERIC" : initAsPlainText(this, cufDefinition, idOrURLOrPostfunction); break;
				default : throw "don't know cuf type "+type;

				}

			};

		});
