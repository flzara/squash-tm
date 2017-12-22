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
define([ "jquery", "backbone", "underscore", "squashtable", "jqueryui" ], function($, Backbone, _) {

	var View = Backbone.View.extend({
		el : "#projects-table",
		initialize : function() {
			var tableConf = {
					"fnRowCallback" : this.projectTableRowCallback
				}, 
				squashConf = {};

			this.$el.squashTable(tableConf, squashConf);
			_.bindAll(this, "refresh");
		},

		hasTemplate : function() {
			return this.$el.find("td.type-template").length > 0;
		},

		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		},
		
		projectTableRowCallback : function(row, data, displayIndex) {
			// add template icon
			var type = data["raw-type"];
			$(row).find(".type").addClass("type-" + type).attr("title", squashtm.app.projectsManager.tooltips[type]);
			
			return row;
		}
	});

	return View;
});