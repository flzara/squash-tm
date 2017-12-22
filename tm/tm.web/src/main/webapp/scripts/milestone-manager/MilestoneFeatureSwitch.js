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
define([ "backbone", "jquery", "handlebars", "underscore", "jquery.squash.oneshotdialog", "squash.translator" ],
	function(Backbone, $, Handlebars, _, confirm, msg) {
		"use strict";

		/**
		 * @param state {
		 *          enabled: true|false }
		 * @returns the selector of the button matching the given state
		 */
		function selector(state) {
			return state.enabled === true ? "#milestone-feat-on" : "#milestone-feat-off";
		}
		/**
		 * @returns the inverse state of the given one
		 */
		function inverse(state) {
			return {
				enabled : !state.enabled
			};
		}

		/**
		 * returns handlebars template for enable confirmation dialog
		 */
		function enableTpl() {
			enableTpl.tpl = enableTpl.tpl || Handlebars.compile($("#confirm-milestone-switch-tpl").html());
			return enableTpl.tpl;
		}

		/**
		 * returns handlebars template for enable confirmation dialog
		 */
		function disableTpl() {
			return enableTpl();
		}
		/**
		 * pops up the activate confirmation dialog
		 *
		 * @returns a promise resolved when user clicks OK
		 */
		function confirmActivate() {
			var text = msg.get({
				first : "message.milestone.feature.enable.first",
				second : "message.milestone.feature.enable.second",
				third : "message.milestone.feature.enable.third",
				fourth : "message.milestone.feature.enable.fourth",
				title : "title.ActivateMilestoneFeature"
			});

			return confirm.show(text.title, enableTpl()(text));
		}

		/**
		 * pops up the nuclear warfare confirmation dialog
		 *
		 * @returns a promise resolved when user clicks OK
		 */
		function confirmDeactivate() {
			var text = msg.get({
				first : "message.milestone.feature.disable.first",
				second : "message.milestone.feature.disable.second",
				third : "message.milestone.feature.disable.third",
				fourth : "message.milestone.feature.disable.fourth",
				title : "title.DeactivateMilestoneFeature"
			});

			return confirm.show(text.title, disableTpl()(text));
		}

		/**
		 * name of event to trigger when state has been commited
		 */
		function doneEventName(state) {
			return "milestonefeatureswitch:" + (state.enabled === true ? "activated" : "deactivated");
		}

		/**
		 * name of the event to trigger when state is being commited
		 */
		function pendingEventName(state) {
			return "milestonefeatureswitch:" + (state.enabled === true ? "activating" : "deactivating");
		}

		/**
		 * factory fr a statte object
		 */
		function state(enabled) {
			return {enabled: enabled};
		}

		/**
		 * factory for an app event object
		 */
		function event(state, view, source) {
			return { state: state, view: view, source: source };
		}

		msg.load([ "message.milestone.feature.enable.first", "message.milestone.feature.enable.second",
				"message.milestone.feature.enable.third", "message.milestone.feature.enable.fourth",
				"message.milestone.feature.disable.first", "message.milestone.feature.disable.second",
				"message.milestone.feature.disable.third", "message.milestone.feature.disable.fourth",
				"title.ActivateMilestoneFeature", "title.DeactivateMilestoneFeature" ]);

		/**
		 * Defines a backbone view which handles the milesones feature power-switch
		 * The root el must have a `data-api` attribute which holds the url where we should post the feature's state.
		 */
		var View = Backbone.View.extend({
			el : "#milestone-feat-switch",

			events : {
				"click #milestone-feat-on:not(.active)" : "onActivateMilestonesFeat",
				"click #milestone-feat-off:not(.active)" : "onDeactivateMilestonesFeat"
			},

			initialize : function() {
				this.apiUrl = this.$el.data("api");

			},

			onActivateMilestonesFeat : function(event) {
				this._onSwitchMilestoneFeat(event, state(true));
			},

			onDeactivateMilestonesFeat : function(event) {
				this._onSwitchMilestoneFeat(event, state(false));
			},

			_onSwitchMilestoneFeat : function(event, state) {
				var self = this;
				(state.enabled === true ? confirmActivate : confirmDeactivate)()
					.done(function() {
						self._applyState(state)
							.done(self._commitStateCallback(state))
							.fail(self._rollbackStateCallback(state));
				});
			},

			/**
			 * posts the new state to the server
			 * @param state
			 * @returns a promise resolved when request is done
			 */
			_applyState : function(state) {
				this.$("button").prop("disabled", true);
				squashtm.vent.trigger(pendingEventName(state), event(state, this));
				return $.post(this.apiUrl, state);
			},

			/**
			 * creates a callback to be called when state has been applied server-side. notifies the app of the new state
			 * @param state
			 * @returns {Function}
			 */
			_commitStateCallback : function(state) {
				var self = this;
				return function(evt) {
					var inv = inverse(state);
					self.$(selector(inv)).removeClass("active");
					self.$(selector(state)).addClass("active");
					self.$("button").prop("disabled", false);

					squashtm.vent.trigger(doneEventName(state), event(state, self, evt));
				};
			},

			/**
			 * creates a callback to be called when state has failed to be applied server-side. notifies the app of the former state
			 * @param state
			 * @returns {Function}
			 */
			_rollbackStateCallback : function(state) {
				return this._commitStateCallback(inverse(state));
			}

		});

		return View;
		});
