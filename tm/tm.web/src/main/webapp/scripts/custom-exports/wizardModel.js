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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil"], function($, Backbone, _,stringUtil) {
	"use strict";

return Backbone.Model.extend({

	initialize : function(data) {

		var self = this;
		var customExportDef = data.customExportDef;

		if (customExportDef) {
			// Reload customExportDef into this model
		} else {
			this.set({ parentId: squashtm.customExport.parentId });
			// Else, initialize the model with the name
			this.set({ name : "" });
		}
	},

	toJson: function(name) {
		return JSON.stringify({
			name: this.get("name") || param
		});
	}

	});
});