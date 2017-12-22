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

		getSeries: function () {
			var stats = this.options.fetchStats(this.model);
			return [[["", stats.conclusiveCritical], ["", stats.inconclusiveCritical], ["", stats.undefinedCritical]],
				[["", stats.conclusiveMajor], ["", stats.inconclusiveMajor], ["", stats.undefinedMajor]],
				[["", stats.conclusiveMinor], ["", stats.inconclusiveMinor], ["", stats.undefinedMinor]],
				[["", stats.conclusiveUndefined], ["", stats.inconclusiveUndefined], ["", stats.undefinedUndefined]]
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

			var totalSuccess = this._sumAllSuccess(model),
				totalFailures = this._sumAllFailures(model),
				total = totalSuccess + totalFailures + this._sumAllOther(model);

			var percentSuccess = (total !== 0 ) ? totalSuccess * 100 / total : 0,
				percentFailures = (total !== 0) ? totalFailures * 100 / total : 0;

			this.$el.find('.success-rate-total-success').text(percentSuccess.toFixed(0) + '%');
			this.$el.find('.success-rate-total-failure').text(percentFailures.toFixed(0) + '%');
		},

		_sumAllSuccess: function (model) {
			return model.conclusiveCritical + model.conclusiveMajor + model.conclusiveMinor + model.conclusiveUndefined;
		},

		_sumAllFailures: function (model) {
			return model.inconclusiveCritical + model.inconclusiveMajor + model.inconclusiveMinor + model.inconclusiveUndefined;
		},

		_sumAllOther: function (model) {
			return model.undefinedCritical + model.undefinedMajor + model.undefinedMinor + model.undefinedUndefined;
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
