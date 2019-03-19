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
define(["jquery", "backbone", "underscore", "workspace.routing", "app/squash.handlebars.helpers", "workspace.projects", "./abstractStepView", "tree", "squash.translator", "../app/util/StringUtil", "is", "jquery.squash.confirmdialog", "jquery.squash.buttonmenu"],
	function ($, backbone, _, router, Handlebars, projects, AbstractStepView, tree, translator, StringUtil, is) {

		"use strict";

		var perimeterStepView = AbstractStepView.extend({

			initialize: function (data, wizrouter) {
				this.tmpl = "#perimeter-step-tpl";
				this.model = data;
				data.name = "perimeter";
				this._initialize(data, wizrouter);
			},

			events: {
			},

			updateModel: function () {
				this.model.set("perimeter", "fakePerimeter");
			},

			/**
			 * IE and FF add a trailing / to cookies...
			 * Chrome don't...
			 * So we need to put the good path to avoid two jstree_select cookies with differents path.
			 */
			getCookiePath : function () {
				var path = "/squash/custom-report-workspace";
				if (is.ie() || is.firefox()) {
					path = path + "/";
				}
				return path;
			}

		});

		return perimeterStepView;

	});
