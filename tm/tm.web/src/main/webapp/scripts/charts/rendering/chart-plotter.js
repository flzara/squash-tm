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
define(["handlebars", "./chart-dom-factory","./chart-instance-factory", './chart-render-utils'], 
		function(Handlebars, DOMFactory, instanceFactory, utils){
	
	var chartNum = 0;
	
	
	function buildChart(divSelector, jsonChart){
		
		var chart = utils.toChartInstance(jsonChart);
		
		var viewID = "chart-"+(chartNum++);
		
		var html = DOMFactory.generateViewDOM(viewID, chart);
		
		// TODO : make the selector for the container area configurable too
		// TODO : destroy previous charts and save memory 
		// everytime a new chart is loaded
		$(divSelector).html(html); 
		
		instanceFactory.generateChartInView("#"+viewID, chart);
	}
	
	return {
		buildChart : buildChart
	};
});