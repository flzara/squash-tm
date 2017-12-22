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
 * Registers handlebars helpers :
 * {{selected}}
 * {{checked}}
 * {{oddity}}
 */
define(["handlebars", "underscore", "squash.translator"], function (Handlebars, _, translator) {
	"use strict";

	function propertyHelper(propName) {
		return function () {
			if (this[propName] === true) {
				return new Handlebars.SafeString(propName + '="' + propName + '"');
			}
			return "";
		};
	}

	/**
	 * Substitutes {{selected}} with selected="selected" when this.selected === true
	 */
	Handlebars.registerHelper("selected", propertyHelper("selected"));
	/**
	 * Substitutes {{checked}} with checked="checked" when this.checked === true
	 */
	Handlebars.registerHelper("checked", propertyHelper("checked"));
	/**
	 * Substitutes {{oddity}} with 'even' or 'odd' in 'for' loops
	 */
	Handlebars.registerHelper("oddity", function (index) {
		return (index % 2 === 0) ? "even" : "odd";
	});


	Handlebars.registerHelper("equal", function (lvalue, rvalue, options) {
		if (arguments.length < 3) {
			throw new Error("Handlebars Helper equal needs 2 parameters");
		}
		if (lvalue != rvalue) {
			return options.inverse(this);
		} else {
			return options.fn(this);
		}
	});

	Handlebars.registerHelper("contains", function (collection, item) {

		for (var prop in collection) {
			if (collection.hasOwnProperty(prop)) {
				if (collection[prop] == item) {
					return true;
				}
			}
		}
		return false;
	});


	Handlebars.registerHelper({
		eq: function (v1, v2) {
			return v1 === v2;
		},
		ne: function (v1, v2) {
			return v1 !== v2;
		},
		lt: function (v1, v2) {
			return v1 < v2;
		},
		gt: function (v1, v2) {
			return v1 > v2;
		},
		lte: function (v1, v2) {
			return v1 <= v2;
		},
		gte: function (v1, v2) {
			return v1 >= v2;
		},
		and: function (v1, v2) {
			return v1 && v2;
		},
		or: function (v1, v2) {
			return v1 || v2;
		}
	});

	Handlebars.registerHelper("intersect", function (col1, col2) {
		return _.intersection(col1, col2);
	});

	Handlebars.registerHelper("union", function (col1, col2) {
		return _.union(col1, col2);
	});

	Handlebars.registerHelper("i18n", function (key, options) {

		var prefix = options.hash.prefix || "";
		var sufix = options.hash.sufix || "";
		var result = prefix + key + sufix;

		return translator.get(result);
	});


	Handlebars.registerHelper("inc", function (value, options) {
		return parseInt(value, 10) + 1;
	});

	/**
	 * if-then-else aka ternary operator
	 * @param test
	 * @param valtrue
	 * @param valfalse
	 * @returns {*}
	 */
	function ifte(test, valtrue, valfalse) {
		return test === true ? valtrue : valfalse;
	}

	Handlebars.registerHelper("ifte", ifte);
	/**
	 * if-then-else with a default else to empty string
	 */
	Handlebars.registerHelper("ift", function (test, valtrue) {
		return ifte(test, valtrue, '');
	});

	return Handlebars;
});
