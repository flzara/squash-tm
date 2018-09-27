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
define(["jquery", "backbone", "squash.translator", "../dashboard-utils"], function ($, Backbone, translator, utils) {

	return Backbone.View.extend({

		initialize: function () {

			var template = this.$el.text().split('|');

			this.hasItemsMsg = template[0];
			this.zeroItemsMsg = template[1];

			this.render();
			this.listenTo(this.model, 'change:boundRequirementsStatistics', this.render);
		},

		// dirty but effective way to know how many test cases we have here.
		render: function () {

			if (!this.model.isAvailable()) {
				return;
			}

			var stats = this.model.get('boundRequirementsStatistics');
			var nbtc = stats.zeroRequirements + stats.oneRequirement + stats.manyRequirements;

			var todisplay;
			if (nbtc === 0) {
				todisplay = this.zeroItemsMsg;
			}
			else {
				todisplay = this.hasItemsMsg.replace('{placeholder}', '<span style="font-weight:bold;color:black;">' + nbtc + '</span>').replace('{details}', '<span class="to-search"><b><a href="#">' + translator.get("dashboard.test-cases.detail") + '</b></a></span>');

			}

			this.$el.html(todisplay);
		},

		events: {
			"click .to-search": "redirectToSearchResult"
		},

		redirectToSearchResult: function () {
			var ids = this.model.get('selectedIds');

			var search = {
				fields: {
					id: {type: "LIST", values: ""}
				}
			};

			search.fields.id.values = ids.toString().split(",");

			var token = $("meta[name='_csrf']").attr("content");
			utils.post(squashtm.app.contextRoot + "advanced-search/results?searchDomain=test-case", {
				searchModel: JSON.stringify(search),
				_csrf: token
			});
		}
	});
});
