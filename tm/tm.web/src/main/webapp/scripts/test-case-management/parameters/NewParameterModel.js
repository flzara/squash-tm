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
define([ "jquery", "backbone", "app/util/StringUtil" ], function($, Backbone, StringUtil) {

	function isBlank(val) {
		return StringUtil.isBlank(val);
	}

	/*
	 * Defines the model for a new Custom Field
	 */
	var NewParameterModel = Backbone.Model.extend({

		defaults : {
			name : "",
			description : ""
		},

		namePatternValid : function(name) {
			// first condition specific for IE8 that does not
			// understand "^" and "$"
			// as "from start to end" of the string
			// hence without the following line "mqlskd slqk"
			// would be validated even
			// thought it has white spaces.
			return ((!name.match(/\s+/)) && name.match(/^[A-Za-z0-9\_\-]*$/));
		},

		validateAll : function() {
			var attrs = this.attributes, errors = null;

			if (isBlank(attrs.name)) {
				errors = errors || {};
				errors.name = "message.notBlank";
			} else if (!this.namePatternValid(attrs.name)) {
				errors = errors || {};
				errors.name = "message.parameterInvalidPattern";
			}

			return errors;
		}

	});

	return NewParameterModel;
});