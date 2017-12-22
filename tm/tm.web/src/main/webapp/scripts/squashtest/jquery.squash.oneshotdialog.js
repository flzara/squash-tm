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
 *
 * 1/ What is it ?
 *
 * Returns a module that generates and displays on the fly a message dialog with the buttons OK and CANCEL, with the
 * title and message of your choice.
 *
 * 2/ How does it work ?
 *
 * the method 'show' accepts 2 mandatory options and 3 optional parameters. It returns a promise, that is resolved when
 * the user clicks on the button 1 and is rejected when clicks the button 2. Usually the buttons 1 and 2 are 'OK' and
 * 'CANCEL' respectively.
 *
 * According to the various parameters description, they can be of type :
 *  - string : the parameter will be displayed as is. Be sensible and don't use HTML where there should be.
 *  - i18nKey : a i18n key that will be translated before use. The parameter will be recognized as a i18n key is it
 * matches /^([\w\-_]+\.)+[\w\-_]+$/, in layman terms a sequence of characters, dashes, underscore and dots (consecutive
 * dots are forbidden though).
 *  - HTML : a string representing HTML, that will be displayed as is. The parameter will be recognised as HTML if it
 * matches /^<([\w]+).*<\/\1>$/, in layman terms anything that start with a html tag and ends with the same tag.
 *  - a plain javascript object.
 *
 *
 * The parameters are :
 *
 * [Mandatory] title : represents the title. It may be a string or i18n key. [Mandatory] message : the content of the
 * dialog, may be string or i18n key. [Optional] dialogOpts : parameters for the dialog, just like for a jQuery dialog,
 * plain js object only. [Optional] bnt1Label : if defined and non null, will replace the label 'OK'. String or i18n
 * key. [Optional] btn2Label : if defined and non null, will replace the label 'Cancel'. String or i18n key.
 *
 *
 */

define([ "jquery", "squash.translator", "underscore", "jqueryui" ], function($, translator, _) {
	"use strict";
	var _i18nReg = /^([\w\-_]+\.)+[\w\-_]+$/;

	function _normalize(title, message, btn1Label, btn2Label) {
		var args = {title: title, message: message, btn1Label: btn1Label, btn2Label: btn2Label };
		_.defaults(args, {btn1Label: "label.Confirm", btn2Label: "label.Cancel"});

		// we sort out message keys from actual messages
		var reducer = function(memo, pair) {
			var recipient = pair[1].match(_i18nReg) === null ? memo.definitive : memo.remotefetch;
			recipient[pair[0]] = pair[1];
			return memo;
		};
		var sorted = _.chain(args).pairs().reduce(reducer, {definitive: {}, remotefetch: {}}).value();

		// now get the i18n, merge the data then return
		var translated = translator.get(sorted.remotefetch);

		return _.extend(sorted.definitive, translated);
	}

	return {
		show : function(title, message, dialogOpts, btn1Label, btn2Label) {
			var content = _normalize(title, message, btn1Label, btn2Label);

			var defer = $.Deferred();

			var conf = {
				width : '300px',
				resizable : false,
				title : content.title,
				modal : true,
				buttons : [ {
					'text' : content.btn1Label,
					'click' : function() {
						var jqDialog = $(this);
						jqDialog.dialog('close');
						jqDialog.dialog('destroy');
						oneShotPopup.remove();
						defer.resolve();
					}
				}, {
					'text' : content.btn2Label,
					'click' : function() {
						var jqDialog = $(this);
						jqDialog.dialog('close');
						jqDialog.dialog('destroy');
						oneShotPopup.remove();
						defer.reject();
					}
				} ]

			};

			if (!!dialogOpts) {
				$.extend(conf, dialogOpts);
			}

			var oneShotPopup = $("<div/>");
			$(document.body).append(oneShotPopup);

			oneShotPopup.append(content.message);
			oneShotPopup.keypress(function(evt) {
				if (evt.which == '13') {
					oneShotPopup.find('button:first').trigger('click');
				}
			});

			oneShotPopup.dialog(conf);

			oneShotPopup.dialog('open');

			return defer.promise();

		}
	};

});
