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
define(["underscore","backbone","squash.translator","handlebars"],
		function(_,Backbone, translator,Handlebars) {
	var View = Backbone.View.extend({

    el : "#contextual-content-wrapper",
		tpl : "#tpl-show-library",

		initialize : function(){
			_.bindAll(this, "render");
			this.model.fetch({
      success: this.render
     });
		},

		events : {
		},

		render : function(){
			var source = $("#tpl-show-library").html();
			var template = Handlebars.compile(source);
			this.$el.append(template(this.model.toJSON()));
			
		},

  });

	return View;
});
