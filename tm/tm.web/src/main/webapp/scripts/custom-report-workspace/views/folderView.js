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
define(["underscore", "backbone", "squash.translator", "handlebars", "squash.basicwidgets", "app/squash.handlebars.helpers"],
	function (_, Backbone, translator, Handlebars, basicWidgets) {
		"use strict";

		var View = Backbone.View.extend({

			el: "#contextual-content-wrapper",
			tpl: "#tpl-show-folder",

			initialize: function (options) {
				this.options = options;
				_.bindAll(this, "render");
				this.model.fetch({})
					.then(function() {
						return options.acls.fetch({});
					}).then(this.render);
			},

			events: {},

			render: function () {
				// TODO template should be compiled only once
				var source = $("#tpl-show-folder").html();
				var template = Handlebars.compile(source);
				var props = this.model.toJSON();
				props.acls = this.options.acls.toJSON();
				this.$el.append(template(props));
				basicWidgets.init();
			}

		});

		return View;
	});

