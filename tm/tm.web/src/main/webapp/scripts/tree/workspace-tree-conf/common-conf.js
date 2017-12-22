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
define(["underscore", "../tree-conf/common-common-conf"], function (_, commonConf) {
	"use strict";

	var baseURL = window.squashtm.app.contextRoot;

	return {
		generate: function (settings) {

			var workspace = settings.workspace;
			var treepref = localStorage[workspace + "-tree-pref"];
			
			var cfg = commonConf(_.extend({}, settings, {baseURL: baseURL}));

			//Load sort by default for custom-report-workspace as we haven't positionnal select
			if (treepref != 1 || workspace == "custom-report") {
				cfg.plugins.push("sort");
			}

			return cfg;
		}
	};
});
