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
 * But was replaced by a donut chart: validation-donut.js
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
			/* validationStatistics: {
			 *	conclusiveUndefined, conclusiveMinor, conclusiveMajor, conclusiveCritical,
			 *	inconclusiveUndefined, inconclusiveMinor, inconclusiveMajor, inconclusiveCritical,
			 *	undefinedUndefined, undefinedMinor, undefinedMajor, undefinedCritical
			}*/
			return this.model.get('validationStatistics');
		},
		_getCompleteSeriesArray: function() {
			
			var stats = this._getCompleteStats();
			return [[stats.conclusiveUndefined, stats.inconclusiveUndefined, stats.undefinedUndefined],
			        [stats.conclusiveMinor, stats.inconclusiveMinor, stats.undefinedMinor],
			        [stats.conclusiveMajor, stats.inconclusiveMajor, stats.undefinedMajor],
					[stats.conclusiveCritical, stats.inconclusiveCritical, stats.undefinedCritical]];
		},
		_selectNotEmptySeries: function(seriesArray) {
			for(var i=0; i < seriesArray.length; i++) {
				var totalTestCases = seriesArray[i][0] + seriesArray[i][1] + seriesArray[i][2];
				if( totalTestCases === 0 ) {
					seriesArray.splice(i, 1);
					i--;
				}
			}
			return seriesArray;
		},
		_computeRatioSeriesAndLabels: function(series) {
			var ratioSeries = [[], [], []];
			for(var i=0, length=series.length; i < length; i++) {

				var conclusiveCount = series[i][0];
				var inconclusiveCount = series[i][1];
				var undefinedCount = series[i][2];
				var totalCount = conclusiveCount + inconclusiveCount + undefinedCount;
				
				var conclusiveRatio = conclusiveCount / totalCount * 100;
				var inconclusiveRatio = inconclusiveCount / totalCount * 100;
				var undefinedRatio = undefinedCount / totalCount * 100;
				
				ratioSeries[0].push([i+1, 
				                     conclusiveRatio, 
				                     conclusiveCount !==0 ? "<div style='font-size:14px;text-align:center;'>" + conclusiveRatio.toFixed() + " %<br/>(" + conclusiveCount + "/" + totalCount + ")</div>" 
				                    		 : ""]);
				ratioSeries[1].push([i+1, 
				                     inconclusiveRatio, 
				                     inconclusiveCount !==0 ? "<div style='font-size:14px;text-align:center;'>" + inconclusiveRatio.toFixed() + " %<br/>(" + inconclusiveCount + "/" + totalCount + ")</div>" 
				                    		 : ""]);
				ratioSeries[2].push([i+1, 
				                     undefinedRatio, 
				                     undefinedCount !==0 ? "<div style='font-size:14px;text-align:center;'>" + undefinedRatio.toFixed() + " %<br/>(" + undefinedCount + "/" + totalCount + ")</div>" 
				                    		 : ""]);
			}
			return ratioSeries;
		},
		getCategories : function() {
			var possibleCategories = [translator.get("requirement.criticality.UNDEFINED"),
			                          translator.get("requirement.criticality.MINOR"),
			                          translator.get("requirement.criticality.MAJOR"),
			                          translator.get("requirement.criticality.CRITICAL")];  
			var categories = [];
			
			var seriesArray = this._getCompleteSeriesArray();
			
			for(var i=0, length=seriesArray.length; i < length; i++) {
				var conclusiveCount = seriesArray[i][0];
				var inconclusiveCount = seriesArray[i][1];
				var undefinedCount = seriesArray[i][2];
				var totalCount = conclusiveCount + inconclusiveCount + undefinedCount;
				if(totalCount > 0) {
					categories.push(possibleCategories[i]);
				}
			}
			return categories;
		},
	});
	
});