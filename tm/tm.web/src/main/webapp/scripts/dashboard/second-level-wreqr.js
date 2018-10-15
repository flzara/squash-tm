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
define(["backbone.wreqr", "workspace.contextual-content"], function (backboneWreqr, ctxcontent) {

	function init(currentUrl) {


		// In most cases, the wreqr will be initialised and its events will be defined in a workspace.
		// Here we will initialise it if it's not already done, otherwise we could interfere with what's done in init-actions among others

		if (!window.squashtm.app.wreqr) {
			window.squashtm.app.wreqr = new backboneWreqr.EventAggregator();

			//copy paste of what's in camp-workspace/init-actions.js but without the parts linked to the tree

			var wreqr = squashtm.app.wreqr;
			wreqr.on("favoriteDashboard.showDefault", function () {
				// in the case of an interface, we must select the correct tab after reloading the page
				if (_.contains(currentUrl, "iterations")) {
					squashtm.workspace.shouldShowFavoriteDashboardTab = true;
				}
				ctxcontent.unload();
				ctxcontent.loadWith(currentUrl);
			});

			wreqr.on("favoriteDashboard.showFavorite", function () {
				if (_.contains(currentUrl, "iterations")) {
					squashtm.workspace.shouldShowFavoriteDashboardTab = true;
				}
				ctxcontent.unload();
				ctxcontent.loadWith(currentUrl);
			});

			wreqr.on("favoriteDashboard.milestone.showDefault", function () {
				ctxcontent.unload();
				ctxcontent.loadWith(squashtm.app.contextRoot + "campaign-browser/dashboard-milestones");
			});

			wreqr.on("favoriteDashboard.milestone.showFavorite", function () {
				ctxcontent.unload();
				ctxcontent.loadWith(squashtm.app.contextRoot + "campaign-browser/dashboard-milestones");
			});
		}


	}

	return {
		init: init
	};

});
