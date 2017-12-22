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
 * - method getSeries(), as per contract defined in abstractCustomReportChart,
 * - the method getCategories (aka the labels of the axes) -> array of String
 */

//TODO : move to dashboard/basic-objects when ready
define(["jquery", "./abstractCustomReportChart",
        "jqplot-core",  "jqplot-category", "jqplot-bar","jqplot-point-labels","jqplot-canvas-label","jqplot-canvas-ticks"],
		function($, JqplotView){

	return JqplotView.extend({

		getCategories : function(){
			throw "attempted to create an abstract CumulativeView !";
		},

		getConf : function(series){

			var ticks = this.getCategories();
			var axis = this.getAxis()[0];
			ticks = this.replaceInfoListDefaultLegend(ticks,axis);

			var finalConf = _.extend(this.getCommonConf(), {
				seriesDefaults : {
					rendererOptions : {
						animation: {
							speed: 1000
						},
						smooth: false
					},
					fill: true,
					pointLabels: {
						show: true,
						labelsFromSeries : true,
						formatString :'%d',
						textColor: "slategray",
						location : 'n',
						hideZeros : true
					}
				},
				legend : {
					show : false
				},
				axes : {
					xaxis : {
						renderer : $.jqplot.CategoryAxisRenderer,
						tickRenderer:$.jqplot.CanvasAxisTickRenderer,
						ticks : ticks,
						tickOptions:{
							angle : -30,
							showGridline: false
						},
						labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
						label : this.getXAxisLabel()
					},
					yaxis: {
						min : 0,
						label : this.getYAxisLabel(),
						labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
						tickOptions:{
							gridStyle : {
								lineDash : [5],
								strokeStyle : "#c3c3c3"
							},
							markStyle : {
								lineDash : [5],
								strokeStyle : '#c3c3c3'
							},
							fontSize : '12px'
						}
					}
				}
			});

			var vueConf = this.getVueConf();
			if (vueConf) {
				finalConf = _.extend(finalConf,vueConf);
			}

			return finalConf;

		}

	});
});
