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
 * Subclasses must implement
 * - method getSeries(), as per contract defined in JqplotView,
 * - the method getCategories (aka the labels of the axes) -> array of String
 */

define(["jquery", "dashboard/basic-objects/jqplot-view",
        "jqplot-core",  "jqplot-category", "jqplot-bar", "jqplot-point-labels", "jqplot-canvas-ticks"],
        function($, JqplotView){

	return JqplotView.extend({

		getCategories : function() {
			throw "attempted to create an abstract BarView !";
		},
		getConf : function(series) {

			var ticks = this.getCategories(series);
			var colors = this.getColors();

			return {
				seriesColors: colors,
				stackSeries: true,
				seriesDefaults : {
					renderer : $.jqplot.BarRenderer,
					rendererOptions : {
						barWidth: 80
					},
					pointLabels: {
						show: true,
						escapeHTML: false,
						edgeTolerance: -20
					},
					shadow: false
				},
				legend : {
					show : false
				},
				axes : {
					yaxis : {
						min: 0.0,
						max: 100.0,
						tickInterval: 50,
						tickOptions: {
							fontSize: '14px'
						},
						showTicks: false
					},
					xaxis : {
						renderer : $.jqplot.CategoryAxisRenderer,
						ticks : ticks,
						tickOptions: {
							fontSize: '14px'
						}
					}
				},
				grid : {
					gridLineColor: 'transparent',
					drawBorder : false,
					borderColor : 'transparent',
					drawGridlines: false,
					background : '#FFFFFF',
					shadow : false,
					shadowColor : 'transparent'
				}
			};

		},
		getColors : function() {
			var legendcolors = this.$el.find('.dashboard-legend-sample-color');

			return legendcolors.map(function (i, e) {
				return $(e).css('background-color');
			}).get();
		},
		/* Override of the draw function from JqplotView
		 * Because the replot function causes a bug while extending configuration
		 * Since previous configuration can have more categories than the current one
		 * and the extension doesn't remove the extra categories
		 * Therefore the plot will be destroyed then redrawn every time */
		draw : function(series, conf){
				if (this.plot !== undefined) {
					this.plot.destroy();
				}
				if(series.length > 0) {
					var viewId = this.$el.find('.dashboard-item-view').attr('id');
					this.plot = $.jqplot(viewId, series, conf);
				}
		}

	});
});
