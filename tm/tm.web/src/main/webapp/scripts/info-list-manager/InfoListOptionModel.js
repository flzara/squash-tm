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
define([ "backbone", "squash.translator", "../app/squash.backbone.validation", "app/squash.wreqr.init" ], function(Backbone, messages,
		Validation) {
	"use strict";
	messages.load(["message.optionCodeAlreadyDefined", "message.optionLabelAlreadyDefined"]);

	/**
	 * returns a validator function which checks this model's code unicity.
	 */
	function isCodeUnique(val, attr, computed) {
		var res = triggerRequest("list-option:code:validate", this);

		console.log("isCodeUnique", res);
		if (res !== undefined && res !== true) {
			return messages.get("message.optionCodeAlreadyDefined");
		} // when valid, should return `undefined`
	}

	function isLabelUnique(val, attr, computed) {
		var res = triggerRequest("list-option:label:validate", this);

		console.log("isLabelUnique", res);
		if (res !== undefined && res !== true) {
			return messages.get("message.optionLabelAlreadyDefined");
		} // when valid, should return `undefined`
	}

	function triggerRequest(name, model) {
		return squashtm.reqres.request(name, {
			model : model
		});
	}

	return Backbone.Model.extend({
		defaults : {
			label : "",
			code : "",
			isDefault : false,
			iconName : ""
		},

		validation : {
			label : {
				notBlank : true,
				maxLength : 100,
				fn: isLabelUnique
			},

			code : {
				notBlank : true,
				maxLength : 30,
				fn : isCodeUnique
			},
		}
	});
});
