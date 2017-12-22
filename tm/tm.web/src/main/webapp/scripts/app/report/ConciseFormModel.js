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
define([ "backbone", "underscore", "jquery", "jeditable.datepicker" ], function(Backbone, _, $) {
	"use strict";

	var PROJECT_PICKER = "PROJECT_PICKER";
	var TREE_PICKER = "TREE_PICKER";
	var RADIO_BUTTONS_GROUP = "RADIO_BUTTONS_GROUP";
	var EVERYTHING = "EVERYTHING";
	var MILESTONE_PICKER = "MILESTONE_PICKER";
	var TAG_PICKER = "TAG_PICKER";

	var dateChecker = function(val) {
		if (val === "--") {
			return true;
		}

		var caught = false;
		try {
			$.datepicker.parseDate($.datepicker.ATOM, val);
		} catch (ex) {
			caught = true;
		}
		return !caught;
	};

	var valueTypeChecker = {
		"TEXT": _.isString,
		"PASSWORD": _.isString,
		"DROPDOWN_LIST": _.isString,
		"RADIO_BUTTONS_GROUP": _.isString,
		"DATE": dateChecker,
		"CHECKBOX": _.isBoolean,
		"CHECKBOXES_GROUP": _.isArray,
		"TREE_PICKER": _.isArray,
		"PROJECT_PICKER": _.isArray,
		"MILESTONE_PICKER" : _.isArray,
		"TAG_PICKER" : _.isArray
	};

	/**
	 * Defines the model for the report form.
	 */
	var ConciseFormModel = Backbone.Model.extend({
		initialize: function(options) {
			_.bindAll(this, "applyFormerState", "setVal");
		},

		applyFormerState: function(state) {
			var self = this;

			_.chain(state).pairs().map(function(pair) {
				return {name: pair[0], value: pair[1]};

			}).filter(function(item) {
				return _.has(item.value, "type") && _.has(item.value, "val");

			}).filter(function(item) {
				var attr = self.get(item.name);
				return !attr || attr.type === item.value.type;

			}).filter(function(item) {
				return valueTypeChecker[item.value.type](item.value.val);

			}).each(function(item) {
				self.set(item.name, item.value);
			});
		},

		setVal: function(key, value, options) {
			var cur = this.get(key);

			if(_.isUndefined(cur)) {
				throw "Attribute " + key + " undefined, did you forget to initialize it ?";
			}
			if(!_.has(cur, "val")) {
				throw "Attribute " + key + " has no val property, did you forget to initialize it ?";
			}

			var next = _.clone(cur);
			next.val = value;
			this.set(key, next, options); // so that events are triggered
		},

		/**
		 * somewhat broken ckeck that a boundary has been selected
		 */
		hasBoundary: function() {
			var boundary = false;

			var boundarySelector =  _.find(this.values(), function(attr) {
				return attr.type === RADIO_BUTTONS_GROUP && (attr.val === PROJECT_PICKER || attr.val === TREE_PICKER || attr.val === MILESTONE_PICKER);
			});

			var pickedBoundary = function(pickerType) {
				return function(attr) {
					return attr.type === pickerType && _.isArray(attr.val) && attr.val.length > 0;
				};
			};

			if(!_.isUndefined(boundarySelector)) {
				boundary = _.some(this.values(), pickedBoundary(boundarySelector.val));
			} else {
				boundary = _.some(this.values(), pickedBoundary(PROJECT_PICKER)) ||
				_.some(this.values(), pickedBoundary(TREE_PICKER)) ||
				_.some(this.values(), pickedBoundary(MILESTONE_PICKER)) ||
				_.some(this.values(), pickedBoundary(TAG_PICKER)) ||
				_.some(this.values(), function(attr) { return attr.type === RADIO_BUTTONS_GROUP && attr.val === EVERYTHING; });
			}

			return boundary;
		}

	});

	ConciseFormModel.Input = function(type, val) {
		if (!(this instanceof ConciseFormModel.Input)) {
			return new ConciseFormModel.Input(type, val);
		}
		this.type = type;
		this.val = val;
	};

	return ConciseFormModel;
});
