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
/**
 * (12/10/2016: SquashTM-1.15.0.IT5)
 * This script is now DEPRECATED and unused in the application.
 * It was used in the first specification of the requirement dashboard.
 * But was replaced by a donut chart: coverage-donut.js
 */
define(["dashboard/basic-objects/bar-view", "squash.translator"], function(BarView, translator) {

	return BarView.extend({
		
		getSeries : function() {

			var seriesArray = this._getCompleteSeriesArray();
			var series = this._selectNotEmptySeries(seriesArray);
			
			if(series.length > 0) {
				series = this._computeRatioSeriesAndLabels(series);
			}
			return series;
		},
		_getCompleteStats: function() {
			/* coverageStatistics: {
			 *	totalUndefined, totalMinor, totalMajor, totalCritical,
			 *	undefined, minor, major, critical
			}*/
			return this.model.get('coverageStatistics');
		},
		_getCompleteSeriesArray: function() {
			var stats = this._getCompleteStats();
			return [[stats.totalUndefined, stats.undefined], 
					[stats.totalMinor, stats.minor], 
					[stats.totalMajor, stats.major], 
					[stats.totalCritical, stats.critical]];
		},
		_selectNotEmptySeries: function(seriesArray) {
			for(var i=0; i < seriesArray.length; i++) {
				if( seriesArray[i][0] === 0) {
					seriesArray.splice(i, 1);
					i--;
				}
			}
			return seriesArray;
		},
		_computeRatioSeriesAndLabels: function(series) {
			var ratioSeries = [[]];
			for(var i=0, length=series.length; i < length; i++) {
				var totalCount = series[i][0];
				var coveredCount = series[i][1];
				var ratio = coveredCount / totalCount * 100;
				var label = 
					coveredCount === 0 ? 
							"<div style='font-size:14px;text-align:center;'>" + ratio.toFixed() + " % (" + coveredCount + "/" + totalCount + ")</div>"
							: "<div style='font-size:14px;text-align:center;'>" + ratio.toFixed() + " %<br/>(" + coveredCount + "/" + totalCount + ")</div>";
				ratioSeries[0].push([i+1,
				                     ratio, 
				                     label]);
			}
			return ratioSeries;
		},
		getCategories : function() {
			
			var possibleCategories = [translator.get("requirement.criticality.UNDEFINED"),
			                          translator.get("requirement.criticality.MINOR"),
			                          translator.get("requirement.criticality.MAJOR"),
			                          translator.get("requirement.criticality.CRITICAL")]; 
			var categories = [];
			
			var stats = this._getCompleteStats();
			var totalsArray = [stats.totalUndefined, stats.totalMinor, stats.totalMajor, stats.totalCritical];
			
			for(var i=0, length=totalsArray.length; i < length; i++) {
				if(totalsArray[i] > 0) {
					categories.push(possibleCategories[i]);
				}
			}
			return categories;
		},
		// Override of getColors function
		// This figure has a unique color
		getColors: function() {
			return ["#A3D86E"];
		}
	});
	
});