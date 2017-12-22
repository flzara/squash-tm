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
define(["jquery", "handlebars"], function($, Handlebars){
	
	var colors = 	["#4D4D4D",
	             	"#5DA5DA",
	            	"#FAA43A",
	            	"#60BD68",
	            	"#F17CB0",
	            	"#B2912F",
	            	"#B276B2",
	            	"#DECF3F",
	            	"#F15854"];

	
	function generateBarViewDOM(viewID, jsonChart){
		var strTemplate = $("#chart-view-barchart-template").html();
		var template = Handlebars.compile(strTemplate);
		
		var title = jsonChart.name;
		
		var templateModel = {
			id : viewID, 
			additionalClasses : 'dashboard-bar',	//class 'dashboard-bar' doesn't define any css really
			title : title
		};
				
		var html = template(templateModel);
		return html;
	}
	
	
	function generatePieViewDOM(viewID, jsonChart){
		var strTemplate = $("#chart-view-piechart-template").html();
		var template = Handlebars.compile(strTemplate);
		
		var title = jsonChart.name;
		
		var templateModel = {
			id : viewID, 
			additionalClasses : 'dashboard-pie',	//class 'dashboard-pie' doesn't define any css really
			title : title
		};
		
		templateModel.legend = [];
		var abscissa = jsonChart.abscissa;
		
		for (var i=0; i < abscissa.length; i++){
			templateModel.legend.push({
				color : colors[i],
				label : abscissa[i][0]
			});
		}
		
		var html = template(templateModel);
		return html;
	}
	
	
	// TODO : use dashboad/basic-objects/table-view
	// allows for indiscriminate reference to a serie by name or index
	// for the sake of consistency
	
	// Also see how we can support multiple axis and/or measures
	function generateTableViewDOM(viewID, jsonChart){
		var strTemplate = $("#chart-view-singletablechart-template").html();
		var template = Handlebars.compile(strTemplate);
		
		var title = jsonChart.name;
		
		var templateModel = {
			id : viewID,
			additionalClasses : 'dashboard-table',
			title : title
		};
		
		var headers = jsonChart.abscissa,
			serie = jsonChart.series
		templateModel.headers = [];
		
		for (var i=0; i< serie.length; i++){
			templateModel.headers.push(serie[i][0]);
		}
		
		templateModel.values = [];
		for (var i=0;i< serie.length; i++){
			templateModel.values.push(serie[i][1]);
		}
		
		var html = template(templateModel);
		return html;
		
	}
	
	function generateViewDOM(viewID, jsonChart){
		var viewDOM = "";
		
		switch(jsonChart.type){
		case 'PIE' : viewDOM = generatePieViewDOM(viewID, jsonChart); break;
		case 'TABLE' : viewDOM = generateTableViewDOM(viewID, jsonChart); break;
		case 'BAR' : viewDOM = generateBarViewDOM(viewID, jsonChart); break;
		default : throw jsonChart.chartType+" not supported yet";
		}
		
		return viewDOM;
		
	}
	
	return {
		generateViewDOM : generateViewDOM
	};
	
});