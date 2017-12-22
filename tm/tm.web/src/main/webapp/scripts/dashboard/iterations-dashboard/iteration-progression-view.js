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
define([
	/* --------------- explicit modules ------------------ */
	'jquery',
	'../basic-objects/jqplot-view',
	'squash.translator',
	'handlebars',
	'squash.dateutils',
	/* -------------- implicit modules -------------------- */
	'jqplot-dates',
	'jqplot-highlight',
	'../jqplot-ext/jqplot.squash.iterationAxisRenderer',
	'../jqplot-ext/jqplot.squash.stylableGridRenderer',
	'../jqplot-ext/jqplot.squash.strippedTimeDateTickRenderer'
	],
	function($, JqplotView, translator, handlebars, dateutils){
	
	
	/* *********************************************************************************************
	*						MAIN VIEW
	* 
	* what : 
	*	this view is the local master of two elements : 
	*	- a plot, when everything is fine
	*	- an error panel, when some errors where detected in the model,
	*
	* uses :
	*	- a _axisHelper object that helps preparing the model for plotting (see more below),  
	*	- the iteration planning, defined elsewhere in the page (it's not part of the dashboard).
	*
	* DOM conf : 
	*	- model-attribute : the name of the attribute of interest in the model
	*	- dateformat : the format string for the dates in the plot. 
	*
	* Remember :
	*	that this view extends JqplotView, as such the parsing of the DOM conf attributes
	*	is performed in the superclass and merged with 'this.options'.
	*
	*********************************************************************************************** */
	
	
	var CampaignProgressionView =  JqplotView.extend({
		
		
		initialize : function(options){	
			options.highlight = this.configureHighlight();	
			JqplotView.prototype.initialize.call(this, options);

		},

		configureHighlight : function(){
			var highlight = {};
			
			highlight.messages = getMessage({
				dateLabel : 'label.Date',
				actualTestcount : 'dashboard.campaigns.progression.tooltip.actualcount',
				scheduledTestcount : 'dashboard.campaigns.progression.tooltip.scheduledcount'
			});
			
			highlight.template = handlebars.compile(
					'<div style="font-size:12px">' +
						'<p><label>{{dateLabel}}</label> {{dateValue}}</p>' +
						'<p><label>{{pointLabel}}</label> {{pointValue}} ({{progression}})</p>' +
					'</div>');
			
			return highlight;
		},
		render : function(){
			
			if (!this.model.isAvailable()){
				return;
			}
			
			var model = this._getModelData();
			
			if (!! model.errors ){
				this.handleErrors();
				this._swapTo('.dashboard-cumulative-progression-error');
			}
			else{
				this._swapTo('.dashboard-figures');
				JqplotView.prototype.render.call(this);
				
					var grid = this.$el.find('.jqplot-grid-canvas');
					var line = this.$el.find('.jqplot-series-canvas').last();
					grid.detach();
					line.after(grid);
			}
		},
		
		_swapTo : function(clazz){
			this.$el.find('.dashboard-alternative-content').hide();
			this.$el.find(clazz).show();
		},
		
		// *********************** PLOTTING SECTION *****************************
				
		_getModelData : function(){
			return this.model.get('iterationProgressionStatistics');
		},		
		
		getSeries : function(){
			var _model = this._getModelData();

			var scheduledIterations = _model.scheduledIteration;
			var executions = _model.cumulativeExecutionsPerDate.slice(0);
			
			// flatten all iterations data into one array
			var iterations = [];

			iterations = scheduledIterations.cumulativeTestsByDate;
		
			
			// fixes the dates
			_axisHelper.adjustDates(iterations, executions);
			_axisHelper.formatDates(iterations, executions);
			
			return [ iterations, executions ]; 
		},
		
		getConf : function(series){

			var bbview = this;
			
			// We need to explicitly compute and set the start and end of the axis to ensure that 
			// the x1axis and x2axis are synchronized.
			// To do so we set the boundaries to day1 -1 and daymax + 1
			var iterSeries = series[0];
			var axisStart = iterSeries[0][0].getTime() - (24*60*60*1000),
				axisEnd = iterSeries[iterSeries.length -1][0].getTime() + (24*60*60*1000);
			

			// format string for the xaxis. Because the $.jqplot.DateAxisRenderer has a slightly different formatting scheme than the civilized world
			// we have to make a little bit of translation (remember that the original format string comes from the DOM conf 
			// and that .replace means 'replace first occurence')			
			var xaxisFormatstring = this.options.dateformat.replace('d', '%').replace('M', '%').toLowerCase();
			
			// grid style
			var gridcolor = 'transparent';

			// return the conf object
			return {
				axes : {
					xaxis : {
						renderer : $.jqplot.DateAxisRenderer,
						tickOptions : {
							showGridline : false,
							fontSize : '12px',
							formatString: xaxisFormatstring
						},
						//the default tick renderer doesn't suit us either 
						tickRenderer: $.jqplot.StrippedTimeDateTickRenderer,
						min : new Date(axisStart),
						max : new Date(axisEnd)
					},
					yaxis :{
						min : 0,
						tickOptions :{
							fontSize : '12px',
							// Special stylableGridRenderer
							gridStyle : {
								lineDash : [3, 6],
								strokeStyle : '#999999'
							}
						}
					}
				},
				grid : {
					background : gridcolor,
					drawBorder : false,
					shadow : false,
					renderer : $.jqplot.StylableGridRenderer
				},
				seriesDefaults:{
					markerOptions:{ 
						size:6
					},
					fill : true,
					fillAndStroke : true
				},
				series: [
				         {color: '#AABEE6', fillColor : '#BDD3FF'},
				         {color: '#9C69E6', fillColor : '#AD75FF'}
				],
				highlighter : {
					tooltipAxes: 'y',
					tooltipLocation : 'n',					
					tooltipContentEditor : function(str, seriesIndex, pointIndex){
						
						// compute date and count to date
						var point = series[seriesIndex][pointIndex],
							currentDate = point[0],
							decimal = (seriesIndex === 0) ? 1 : 0,
							currentValue = point[1].toFixed(decimal),
							opts = bbview.options.highlight;
						
						// compute progression wrt the total planned test count when the campaign ends.
						var planned = series[0],
							totalTests = ( planned.length>0 ) ? planned[planned.length - 1][1] : NaN,
							progression =  currentValue * 100 / totalTests,
							strProgression = ( isFinite(progression) ) ? progression.toFixed(0)+'%' : '100%';							
							
						
						var model = {
							dateLabel : opts.messages.dateLabel,
							dateValue : dateutils.format(currentDate, bbview.options.dateformat),
							pointLabel : (seriesIndex===0) ? opts.messages.scheduledTestcount : opts.messages.actualTestcount,
							pointValue : currentValue,
							progression : strProgression
						};
						
						return opts.template(model);
					}
				}
			};
		},
		
		// ****************** ERROR HANDLING SECTION ******************************
		
		initErrorHandling : function(){
			this.iterPopup = $(".dashboard-cumulative-progression-iterpopup");
			this.iterPopup.dashboarditerDialog();
		},
		
		handleErrors : function(){
			
			if (!this.model.isAvailable()){
				return;
			}
			
			var model = this._getModelData();
			
			// 
			var msg = getMessage(model.errors[0]);
			this.$el.find('.cumulative-progression-errormsg').text(msg);
			
			/*
			 * if no planning popup is available, hide the <a>details</a> link
			 *
			 * note : the block of code below is useless because there is no 
			 * iteration planning popup here (unlike the same dashboard for campaigns) :
			 * we can just set the iteration dates in the same page directly. 
			 * However it stays there in case one day we add such popup.
			 */
			var detailLink = this.$el.find('.dashboard-cumulative-progression-details');
			if (document.getElementById("iteration-planning-popup") !== null){
				detailLink.show();
			}
			else{
				detailLink.hide();
			}
		},
		
		openDetails : function(){
			// see campaign-management/planning/iteration-planning-popup and the main right next to it
			$("#iteration-planning-popup").iterplanningDialog('open');
		}
		
	});
	
	
	/* *********************************************************************************************
	*						DATE UTILS
	*
	* what : 
	*	an object used by CampaignProgressionView to make the data model square and ready
	*	to plot. Note that this is NOT the squash.dateutils module.
	* 
	* uses :
	*	- nothing
	*
	* DOM conf : 
	*	- nothing
	*
	*
	*********************************************************************************************** */
	
	
	
	var _axisHelper = {
		// makes start dates and end dates be the same for the iterations series and executions series
		adjustDates : function(iterations, executions){
			
			this._adjustStartDates(iterations, executions);
			this._adjustEndDates(iterations, executions);
			
		},
		
		_adjustStartDates : function(iterations, executions){
			var startIter = iterations[0][0],
				startExecs = (executions.length>0) ? executions[0][0] : null;
			
			if (startExecs === startIter){
				return;	// nothing to adjust
			}
			
			var execdatesUnderflowed = (startExecs === null || startIter < startExecs);
			
			var fixedArray = (execdatesUnderflowed) ? executions : iterations;
			var datefix = (execdatesUnderflowed) ? startIter : startExecs;
			var valuefix = 0.0;
			
			fixedArray.unshift([datefix, valuefix]);		
		},
		
		_adjustEndDates : function(iterations, executions){
			var endIter = iterations[iterations.length-1][0],
				endExecs = (executions.length>0) ? executions[executions.length - 1][0] : null;
				
			if (endIter === endExecs){
				return ;	// nothing to do
			}
			
			var execdatesOverflowed = ( endExecs === null || endIter > endExecs );
			
			var fixedArray = (execdatesOverflowed) ? executions : iterations;
			var datefix = (execdatesOverflowed) ? endIter : endExecs;
			var valuefix = fixedArray[fixedArray.length -1][1];
				
			fixedArray.push([datefix, valuefix]);		
		},
		
		// this function must transform millis timestamp dates to Date objects
		formatDates : function(iterations, executions){
			
			var i=0,
				ilen = iterations.length;
			
			for (i=0;i<ilen;i++){
				iterations[i][0] = dateutils.parse(iterations[i][0]);
			}
			
			var k=0,
				klen = executions.length;
			
			for (k=0; k < klen; k++){
				executions[k][0] = dateutils.parse(executions[k][0]);
			}
		}			
	};
	
	
	
	

	
	//***************************** RETURN + STUFFS **************************************
	
	
	function getMessage(msgOrObj){
		return translator.get(msgOrObj);
	}
	
	
	return CampaignProgressionView;
	
});