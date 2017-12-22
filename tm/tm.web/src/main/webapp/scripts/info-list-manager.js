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
require([ "common" ], function(common) {
	require([ "app/pubsub", "backbone.wreqr", "moment", "./info-list-manager/InfoListsTable", "./info-list-manager/NewInfoListPanel", "./info-list-manager/InfoListModel", "app/ws/squashtm.workspace" ],
			function(ps, Wreqr, moment, InfoListsTable, NewInfoListPanel, InfoListModel, WS) {
		"use strict";

		squashtm = squashtm || {};
		squashtm.vent = squashtm.vent || new Wreqr.EventAggregator();
		squashtm.reqres = new Wreqr.RequestResponse();

		moment.locale(window.navigator.userLanguage || window.navigator.language);

		ps.subscribe("loaded.itemsTable", function() {
			console.log("loaded.itemsTable");
			var tableView = new InfoListsTable();

			$(document).on("click", "#remove-selected-items", tableView.removeSelectedItems);
		});

		ps.subscribe("loaded.newItemDialog", function() {
			$(document).on("click", "#add-item", function(event) {
				var apiRoot = $(event.target).data("api-url");
				var model = new InfoListModel({}, { apiRoot: apiRoot });

				new NewInfoListPanel({ model: model });
			});
		});

		squashtm.vent.on("newinfolist:cancelled newinfolist:confirmed", function(event) {
			event.view.remove();
		});

		$(function() {
			WS.init();
		});
	});
});
