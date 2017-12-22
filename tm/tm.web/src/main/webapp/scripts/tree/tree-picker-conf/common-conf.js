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
/*
 * Returns the workspace-independant part of the configuration of a "workspace" tree.
 *
 *
 * conf : {
 *  model : model object for that tree
 * }
 *
 */
define(function () {
	"use strict";

	return {
		generate: function (settings) {

			return {

				plugins: ["json_data", "sort", "themes", "types", "dnd", "cookies", "ui", "squash", "treepicker",
					'conditionalselect'],

				json_data: {
					ajax: {
						url: function (n) {
							var nodes = n.treeNode();
							if (nodes.canContainNodes()) {
								return nodes.getContentUrl();
							} else {
								return null;
							}
						}
					},
					data: settings.model
				},

				core: {
					animation: 0
				},

				dnd: {
					drop_finish: function (data) {
						// attention : extra lazy implementation
						// don't do the same at home kids !
						data.o.treeNode().select();
						$("#add-items-button").click();
					}
				},

				ui: {
                                        select_limit : settings.nodelimit ? settings.nodelimit : -1,
					select_multiple_modifier: "ctrl",
					select_range_modifier: "shift"
				},

				themes: {
					theme: "squashtest",
					dots: true,
					icons: true,
					url: window.squashtm.app.contextRoot + "/styles/squash.tree.css"
				},

				squash: {
					rootUrl: window.squashtm.app.contextRoot,
					opened: (!!settings.selectedNode) ? [settings.selectedNode] : []
				},
				conditionalselect: function (node) {

					if (settings.canSelectProject && $(node).is("[rel='drive']")) {
						return true;
					}

					return !$(node).is("[rel='drive']");


				}
			};
		}
	};

});
