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
 * like the rest of workspace.<elt>, this a a singleton that will be instanciated the first time the module is required,
 * and subsequent calls will return that instance.
 *
 */

define([ "app/pubsub", "jquery", "workspace.event-bus", "jqueryui" ], function(ps, $, eventBus) {

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};

	if (squashtm.workspace.contextualContent !== undefined) {
		return squashtm.workspace.contextualContent;
	} else {
		$.fn.contextualContent = function() {

			this.currentUrl = "";
			this.eventBus = eventBus;
			this.currentXhr = {
				readyState : 4,
				abort : function() {
				}
			};


			/* **************** super private ************* */

			var _cleanPopups = function() {
				$(".ui-dialog-content.is-contextual").dialog("destroy").remove();
			};

			/* ******************* private **************** */

			var cleanContent = $.proxy(function() {
				// notify the handlers that we're moving to another content
				this.eventBus.clearContextualListeners();

				// clean the content
				_cleanPopups();

				this.empty();

			}, this);

			var abortIfRunning = $.proxy(function() {
				if (this.currentXhr.readyState != 4) {
					this.currentXhr.abort();
				}
			}, this);

			/* ******************* public **************** */

			this.loadWith = function(url, params) {
				// [Issue 4168] Hide datepicker if there is one unclosed
				$('.ui-datepicker').hide();

				var defer = $.Deferred();
				var self = this;
				//firing an event with wreqr for custom dashboard
				var wreqr = squashtm.app.wreqr;
				if(wreqr){
					wreqr.trigger("contextualContent.loadWith");
				}

				if (url == this.currentUrl) {
					defer.reject();
					return defer.promise();
				} else {
					abortIfRunning();

					this.currentUrl = url;
					this.currentXhr = $.get(url, params, 'html').success(function(html) {
						cleanContent();
						self.html(html);

					});

					return this.currentXhr;
				}

			};

			this.unload = function() {
				cleanContent();
				this.currentUrl = "";
				abortIfRunning();
			};

			return this;

		};

		squashtm.workspace.contextualContent = $("#contextual-content, #information-content").contextualContent();

		return squashtm.workspace.contextualContent;

	}

});
