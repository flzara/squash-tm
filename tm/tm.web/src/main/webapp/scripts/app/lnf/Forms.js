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
 * This module handles form messages.
 */
define([ "jquery", "underscore" ], function($, _) {
	"use strict";

	function clearState($help, $controlGroup) {
		return function() {
			$help.hide().addClass("not-displayed").html("&nbsp;");
			$controlGroup.removeClass("error").removeClass("warning").removeClass("success").removeClass("xsuccess");

			return this;
		};
	}

	/** prototype for the helper object which will be returned by this module. Some functions are added later. */
	function Forms() {}

	/**
	 * input has 2 methods : clearState setState(cssClass, messageKey)
	 */
	Forms.prototype.input = function input($dom) {
		var $input = $dom;
		var $controlGroup = $input.closest(".control-group");

		return _.extend({
			$el : $input
		}, this.control($controlGroup));
	};

	Forms.prototype.form = function form($dom) {
		var $form = $dom;
		var $controlGroup = $form.find(".control-group");
		var $help = $form.find(".help-inline");

		return {
			clearState : clearState($help, $controlGroup),
			input : Forms.prototype.input
		};
	};

	Forms.prototype.control = function control($controlGroup) {
		var $help = $controlGroup.find(".help-inline");
		var clear = clearState($help, $controlGroup);

		/**
		 * Shows the message read from squashtm.app.messages using the given css class
		 */
		var setState = function(state, messageKey) {
			var message = messageKey;
			if (!! window.squashtm.app.messages) {
				message = window.squashtm.app.messages[messageKey] || messageKey;
			}
			clear();
			$controlGroup.addClass(state);

			$help.html(message).fadeIn("slow", function() {
				$(this).removeClass("not-displayed");
			});

			return this;
		};

		return {
			clearState : clear,
			setState : setState,
			hasHelp : $help.length !== 0
		};
	};

	return new Forms();
});