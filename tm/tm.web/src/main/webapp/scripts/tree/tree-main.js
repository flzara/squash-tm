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
 * settings : {
 * workspace : one of ['test-case', 'requirement', 'campaign']
 * treeselector : the tree selector,
 * model : the data model for that tree.
 * selectedNode :
 * }
 */

define(["jquery",
	"./simple-tree-conf/conf-factory",
	"./workspace-tree-conf/conf-factory",
	"./tree-picker-conf/conf-factory",
	"./plugins/plugin-factory",
	"./search-tree-conf/conf-factory",
	"workspace.contextual-content",
	"jstree"], function ($, simpleConf, wkspConf,
											 pickerConf, pluginsFactory, searchConf, ctxtcontent) {
	"use strict";

	window.squashtm = window.squashtm || {};
	window.squashtm.tree = window.squashtm.tree || undefined;

	return {

		initWorkspaceTree: function (settings) {
			pluginsFactory.configure("workspace-tree", settings);
			this.initWorkspaceTreeCommon(settings);
		},

		initCustomReportWorkspaceTree: function (settings) {
			pluginsFactory.configure("custom-report-workspace-tree", settings);
			this.initWorkspaceTreeCommon(settings);
		},

		// TODO : move the extra event bindings into workspace-tree-plugin.js
		initWorkspaceTreeCommon: function (settings) {
			var conf = wkspConf.generate(settings);
			var treeDiv = $(settings.treeselector);
			//trick for [Issue 2886]
			treeDiv.bind("loaded.jstree", function (event, data) {
				treeDiv.jstree("save_cookie", "open_node");
			});
			window.squashtm.tree = treeDiv.jstree(conf);

			treeDiv.bind("select_node.jstree", function (e, data) {
				data.rslt.obj.parents('.jstree-closed').each(function () {
					data.inst.open_node(this);
				});
			});

			$('#tree').bind("select_node.jstree", function (e, data) {
				data.rslt.obj.parents('.jstree-closed').each(function () {
					data.inst.open_node(this);
				});
			});
		},
		initSearchTree: function (settings) {
			pluginsFactory.configure("search-tree");
			var conf = searchConf.generate(settings);
			var instance = $(settings.treeselector).jstree(conf);

			instance.on("select_node.jstree", function (event, data) {
				var prevSelect = $($(event.target).jstree('get_selected')[0]).attr('restype');

				if (prevSelect !== undefined && prevSelect !== data.rslt.obj.attr('restype')) {
					$(event.target).jstree('deselect_all');
				}

				return true;
			});

			window.squashtm.tree = instance;

		},
		initLinkableTree: function (settings) {
			pluginsFactory.configure("tree-picker");
			var conf = pickerConf.generate(settings);
			window.squashtm.tree = $(settings.treeselector).jstree(conf);
		},

		initCallStepTree: function (settings) {
			pluginsFactory.configure("simple-tree");
			var conf = simpleConf.generate(settings);
			var instance = $(settings.treeselector).jstree(conf);

			instance.on("select_node.jstree", function (event, data) {
				var resourceUrl = $(data.rslt.obj).treeNode().getResourceUrl();
				ctxtcontent.loadWith(resourceUrl);
				return true;
			});

			window.squashtm.tree = instance;
		},

		get: function (arg) {
			if (arg === undefined) {
				var zetree = $("#tree");
				
				// if not found, try to find if there is one matching .tree.jstree
				if (zetree.length === 0){
					zetree = $(".tree.jstree");
				}
				
				// if 0 or more instances were found, that's no good 
				// return null instead to fail-fast
				if (zetree.length !== 1){
					zetree = null;
				}
				
				return zetree;
			}
			else {
				return $(arg);
			}
		}
	};

});
