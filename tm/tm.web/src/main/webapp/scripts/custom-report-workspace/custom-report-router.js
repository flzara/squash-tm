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
define(["jquery", 'backbone', "workspace.routing", "./views/libraryView", "./views/folderView", "./views/dashboardView", "./views/chartView", "./views/reportView"],
	function ($, Backbone, urlBuilder, libraryView, folderView, dashboardView, chartView, reportView) {
		"use strict";

		var LibraryModel = Backbone.Model.extend({
			urlRoot: urlBuilder.buildURL("custom-report-library-server"),

			parse: function (response) {//flattening the embeded project from server in backbone model...
				var attr = response && _.clone(response) || {};
				if (response.project) {
					for (var key in response.project) {
						if (response.project.hasOwnProperty(key)) {
							attr["project-" + key] = response.project[key];
						}
					}
					delete attr.project;
				}
				return attr;
			}
		});

		var FolderModel = Backbone.Model.extend({
			urlRoot: urlBuilder.buildURL("custom-report-folder-server")
		});

		/**
		 * AclModel should be initialized with dash-style resource type and id : {type: "test-case", id: 10}
		 */
		var AclModel = Backbone.Model.extend({
			defaults: {
				type: undefined
			},
			urlRoot: function () {
				return urlBuilder.buildURL("acls") + "/" + this.attributes.type;
			}
		});

		var router = Backbone.Router.extend({

			activeView: null,

			initialize: function () {
			},

			routes: {
				"": "cleanContextContent",
				"custom-report-library/:query": "showLibraryDetails",
				"custom-report-folder/:query": "showFolderDetails",
				"custom-report-dashboard/:query": "showDashboardDetails",
				"custom-report-chart/:query": "showChartDetails",
				"custom-report-report/:query": "showReportDetails"
			},

			showLibraryDetails: function (id) {
				this.cleanContextContent();

				var activeModel = new LibraryModel({id: id});

				this.activeView = new libraryView({
					model: activeModel
				});

			},

			showFolderDetails: function (id) {
				this.cleanContextContent();

				var activeModel = new FolderModel({id: id});
				var acls = new AclModel({type: "custom-report-library-node", id: id});

				this.activeView = new folderView({
					model: activeModel,
					acls: acls
				});
			},

			showDashboardDetails: function (id) {
				this.cleanContextContent();
				var modelDef = Backbone.Model.extend({
					defaults: {
						id: id
					}
				});

				var activeModel = new modelDef();
                var acls = new AclModel({type: "custom-report-library-node", id: id});

				this.activeView = new dashboardView({
					model: activeModel,
                    acls: acls
				});
			},

			showChartDetails: function (id) {
				this.cleanContextContent();
				var modelDef = Backbone.Model.extend({
					defaults: {
						id: id
					}
				});

				var activeModel = new modelDef();
				var acls = new AclModel({type: "custom-report-library-node", id: id});

				this.activeView = new chartView({
					model: activeModel,
					acls: acls
				});
			},

			showReportDetails: function (id) {
				this.cleanContextContent();
				var modelDef = Backbone.Model.extend({
					defaults: {
						id: id
					}
				});

				var activeModel = new modelDef();
				var acls = new AclModel({type: "custom-report-library-node", id: id});

				this.activeView = new reportView({
					model: activeModel,
					acls: acls
				});
			},

			//Only for forcing router to reload page after updates on selected node
			//To navigate inside workspace and have a correct history please use router.navigateTo()
			// TODO (GRF) could not find usage - to be removed ?
			showNodeDetails: function (nodeType, nodeId) {
				switch (nodeType) {
					case "drive":
						this.showLibraryDetails(nodeId);
						break;
					case "folder":
						this.showFolderDetails(nodeId);
						break;
					case "dashboard":
						this.showDashboardDetails(nodeId);
						break;
					case "chart":
						this.showChartDetails(nodeId);
						break;
					case "report":
						this.showReportDetails(nodeId);
						break;
					default:

				}
			},

			//Will clean the contextual part and restore the contextual div
			cleanContextContent: function () {
				if (this.activeView !== null) {
					window.squashtm.app.wreqr.off('dropFromTree');
					this.activeView.remove();
					this.activeView = null;
				}
				//recreating the context div to allow new view to target the context div as el
				$("#contextual-content").html("<div id='contextual-content-wrapper' style='height: 100%; width:98%; overflow: auto;'></div>");
			}
		});

		function init() {
			return new router();
		}

		// TODO simply return the Router function
		return {
			init: init
		};
	});
