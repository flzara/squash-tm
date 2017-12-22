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
define(["dashboard/basic-objects/pie-view"], function(PieView){

	return PieView.extend({
		
		getSeries : function(){
			var stats = this.model.get('boundTestCasesStatistics');
			return [ stats.zeroTestCases, stats.oneTestCase, stats.manyTestCases ];
		},
		/* The getCategories function enables to know which categories are displayed on the chart
		 * It is used specifically in the research, because when the pie is full, 
		 * the pointIndex of the click is always 0 and distort the research */
		getCategories : function() {
			var possibleCategories = ['zeroTestCases', 'oneTestCase', 'manyTestCases'];
			var categories = [];
			
			var stats = this.model.get('boundTestCasesStatistics');
			var totalsArray = [ stats.zeroTestCases, stats.oneTestCase, stats.manyTestCases ];
			
			for(var i=0, length=totalsArray.length; i < length; i++) {
				if(totalsArray[i] > 0) {
					categories.push(possibleCategories[i]);
				}
			}
			
			return categories;
		}
	});

});