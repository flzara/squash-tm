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
define(["../basic-objects/donut-view"], function (DonutView) {
	"use strict";

	/**
	 * This view should be passed options = { fetchStats: function, ... } where fetchStats is a
	 * function which fetches the stats from a model object.
	 */
	return DonutView.extend({
		/** Originally, since it was a bar chart showing only covered requirements, 
		 * the model needed the number of covered requirements and the number of total requirements.
		 * That's why there are ugly subtractions in the functions.
		 */
		getSeries: function () {
			var stats = this.options.fetchStats(this.model);
			return [[["", stats.critical], ["", stats.totalCritical - stats.critical]],
				[["", stats.major], ["", stats.totalMajor - stats.major]],
				[["", stats.minor], ["", stats.totalMinor - stats.minor]],
				[["", stats.undefined], ["", stats.totalUndefined - stats.undefined]]
			];
		},

		render: function () {

			if (!this.model.isAvailable()) {
				return;
			}

			DonutView.prototype.render.call(this);
			this._renderSubplot();

			// also, take care that the subplot and legend do not overlap
			this.adjustFontsize();

		},

		_renderSubplot: function () {

			var model = this.options.fetchStats(this.model);

			var totalCovered = this._sumAllCovered(model),
				totalNotCovered = this._sumAllNotCovered(model),
				total = totalCovered + totalNotCovered;

			var percentCovered = (total !== 0 ) ? totalCovered * 100 / total : 0,
				percentNotCovered = (total !== 0) ? totalNotCovered * 100 / total : 0;

			this.$el.find('.success-rate-total-success').text(percentCovered.toFixed(0) + '%');
			this.$el.find('.success-rate-total-failure').text(percentNotCovered.toFixed(0) + '%');
		},

		_sumAllCovered: function (model) {
			return model.critical + model.major + model.minor + model.undefined;
		},

		_sumAllNotCovered: function (model) {
			return model.totalCritical - model.critical +
				   model.totalMajor - model.major + 
				   model.totalMinor - model.minor + 
				   model.totalUndefined - model.undefined;
		},

		// ************** code managing font size for the legend and subplot ***************

		adjustFontsize: function () {

			var meta = this.$el.find('.dashboard-item-meta'),
				subplot = this.$el.find('.dashboard-item-subplot'),
				legend = this.$el.find('.dashboard-item-legend');

			meta.find('div').css({'font-size': '1.0em'});

			var adjusted = true,
				newsize = 0.98;

			while (adjusted === true && newsize > 0.75) {
				adjusted = this._resizeIfNeeded(meta, subplot, legend, newsize + 'em');
				newsize = newsize - 0.02;
			}

		},

		_resizeIfNeeded: function (meta, subplot, legend, size) {
			var changed = true;

			var bottomSubplot = subplot.position().top + subplot.height();
			var legendTop = legend.position().top;

			if (legendTop < bottomSubplot) {
				meta.find('div').css({'font-size': size});
				changed = true;
			}
			else {
				changed = false;
			}

			return changed;
		}


	});
});
