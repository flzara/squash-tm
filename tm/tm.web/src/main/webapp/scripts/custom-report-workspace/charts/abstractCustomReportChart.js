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
 * This view is used to plot graph in custom report workspace. Derivated from jqplot-view.
 * The mains changes are :
 *     - No more reference to a model binded to tree. The original jqplot-view is made in a context where selection in tree
 *       can change chart.
 *     - No more reference to resize window event. Resize will be handled by dashboardView
 *			-	this view and derivated are meant to be instancied in callback after an ajax call for data. so series should never be undefined but can be empty...
 *
 *
 */
define(["jquery", "backbone", "squash.attributeparser", "workspace.event-bus", "underscore", "squash.translator", "handlebars", 
        "text!./empty-chart.html!strip","../utils", "squash.dateutils", "dashboard/jqplot-ext/jqplot.squash.stylableGridRenderer"],
	function ($, Backbone, attrparser, eventbus, _, translator, Handlebars, emptyChartTemplate, chartUtils, dateUtils) {
		"use strict";
		var squashtm = window.squashtm;

		translator.load(["squashtm.dateformatShort", "squashtm.dateformatMonthshort"]);

		return Backbone.View.extend({
			//************************** commons variables **************************
			legendsMaxLength: 15,

			// ************************* abstract functions *****************

			getSeries: function () {
				throw "dashboard : attempted to instanciate an abstract jqplot view !";
			},

			getConf: function (series) {
				throw "dashboard : attempted to instanciate an abstract jqplot view !";
			},

			getTitle: function () {
				return "<div><b>" + this.model.get('title') + "</b>";
			},

			getAxis: function () {
				return this.model.get('axis');
			},

			getXAxisLabel: function () {
				return this.model.get('xaxisLabel');
			},

			getYAxisLabel: function () {
				return this.model.get('yaxisLabel');
			},

			getVueConf: function () {
				return this.model.get('vueConf');
			},

			/**
			 * The JqPlot conf of all
			 * @return {[type]} [description]
			 */
			getCommonConf: function () {
				return {
					title: {
						textColor: "slategray",
						fontFamily: "Verdana,Arial,Helvetica,sans-serif",
						show: true,
						text: this.getTitle(),
						textAlign: "center",
						fontSize: "14px"
					},
					grid: {
						drawGridlines: true,
						background: '#eeeeee',
						drawBorder: false,
						borderColor: 'transparent',
						shadow: false,
						shadowColor: 'transparent',
						renderer: $.jqplot.StylableGridRenderer
					},
					highlighter: {
			    		  show: false
			    	}
				};
			},

			/**
			 * Traduce the legends for a given axis in a chart
			 * @param  {[Array]} legends [The gross legends from server]
			 * @param  {[Axis object]} axis [The axis of legends to retrive the Column Prototype inside Axis object]
			 * @return {[Array]}         [The legends localized]
			 */
			replaceInfoListDefaultLegend: function (legends, axis) {
				var protoLabel = axis.columnPrototype.label;
				var protoDatatype = axis.columnPrototype.dataType;

				if (protoDatatype === "DATE") {
					return this._formatDateLegend(legends, axis);
				}
				
				if (protoDatatype === "DATE_AS_STRING") {
					legends = _.map(legends, function(legend) { 
						return dateUtils.format(legend, "yyyyMMdd", "yyyy-MM-dd")
						});
					return this._formatDateLegend(legends, axis);
				}

				if (protoDatatype === "EXECUTION_STATUS") {
					return this._getI18nLegends(legends, squashtm.app.executionStatus);
				}

				switch (protoLabel) {
					case "TEST_CASE_NATURE":
					case "TEST_CASE_TYPE":
					case "REQUIREMENT_VERSION_CATEGORY":
					case "REQUIREMENT_CATEGORY":
						return this._getI18nInfoListLegends(protoLabel, legends, squashtm.app.defaultInfoList);
					case "TEST_CASE_IMPORTANCE":
						return this._getI18nLegends(legends, squashtm.app.testCaseImportance);
					case "TEST_CASE_STATUS":
						return this._getI18nLegends(legends, squashtm.app.testCaseStatus);
					case "REQUIREMENT_VERSION_CRITICALITY":
					case "REQUIREMENT_CRITICALITY":
						return this._getI18nLegends(legends, squashtm.app.requirementCriticality);
					case "REQUIREMENT_VERSION_STATUS":
					case "REQUIREMENT_STATUS":
						return this._getI18nLegends(legends, squashtm.app.requirementStatus);
					default:
						return this.truncateLegends(legends);
				}

			},

			_getI18nLegends: function (legends, i18nLegends) {
				return _.map(legends, function (legend) {
					if (i18nLegends[legend]) {
						return i18nLegends[legend];
					}
					return legend;
				});
			},

			_getI18nInfoListLegends: function (protoLabel, legends, i18nLegends) {
				return _.map(legends, function (legend) {
					if (i18nLegends[legend]) {
						return i18nLegends[legend];
					}
					return legend;
				});
			},

			_formatDateLegend: function (legends, axis) {
				var operation = axis.operation.name;
				switch (operation) {
					case "BY_DAY":
						return this._formatDate(legends, translator.get("squashtm.dateformatShort"));
					case "BY_MONTH":
					case "BY_WEEK" :
						return this._formatDate(legends, translator.get("squashtm.dateformatMonthshort"));
					case "BY_YEAR":
						return this.stripNullYear(legends);
					default:
						return legends;
				}
			},

			stripNullYear: function (legends) {
				return _.map(legends, function (legend) {
					if (!!legend) {
						return legend;
					}
					return translator.get("label.lower.Never");
				});
			},

			_formatDate: function (legends, outformat) {

				function splitdate(intDate) {
					var str = intDate.toString();
					var parsed = {};
					// by month components
					parsed.year = str.substring(0, 4);
					parsed.month = str.substring(4, 6);
					// by day components
					if (str.length === 8) {
						parsed.day = str.substring(6, 8);
					}
					return parsed;
				}

				var self = this;

				return _.map(legends, function (legend) {

					if (window.Array.isArray(legend)) {
						return self._formatDate(legend, outformat);
					}
					if (!!legend) {
						var d = splitdate(legend);
						return outformat.replace('yyyy', d.year).replace('MM', d.month).replace('dd', d.day);
					}
					else {
						return translator.get("label.lower.Never");
					}
				});
			},


			/**
			 * Convert an array of string [s1,s2,...] to an array of object like : [{label:s1},{label:s2}...]
			 * JqPlot needs this format to show series legens on multi axis charts
			 * @param  {[array]} legends [s1,s2,...]
			 * @return {[array]}         [{label:s1},{label:s2}...]
			 */
			objectifyLegend: function (legends) {
				return _.map(legends, function (legend) {
					var result = {};
					result.label = legend;
					return result;
				});
			},

			truncateLegends: function (legends) {
				var self = this;

				return _.chain(legends)
					.map(function (legend) { // replaces empty legends with something
						return ((!legend) || legend.length === 0) ? legend = translator.get("chart.label.noticks") : legend;

					}).map(function (legend) { // truncates legends
						return legend.toString().substring(0, self.legendsMaxLength);

					}).value();
			},

			calculateFontSize: function (legends, ticks) {
				var height = (this.$el.parent().height()) * 0.9;
				var nbLabelLegend = legends.length;
				var nbLabelTicks = ticks.length;
				return Math.min(this.calculateOneFontSize(height, nbLabelLegend), this.calculateOneFontSize(height, nbLabelTicks));
			},

			calculateOneFontSize: function (height, nbLabel) {
				var spaceForOnelabel = height / nbLabel;
				if (spaceForOnelabel > 30) {
					return 12;
				}
				else if (spaceForOnelabel > 27) {
					return 10;
				}
				else if (spaceForOnelabel > 24) {
					return 8;
				}
				return 0; //8px min.. so the 0 will be used for test value.
			},

			getResizeConf: function (legends, ticks) {
				var self = this;
				var fontSize = this.calculateFontSize(legends, ticks);
				return {
					legend: self.getResizeLegendConf(fontSize),
					fontSize: self.getResizeTickConf(fontSize)
				};
			},

			getResizeLegendConf: function (fontSize) {
				if (fontSize === 0) {
					return {show: false};
				}
				else {
					return {
						renderer: $.jqplot.EnhancedLegendRenderer,
						rendererOptions: {
							seriesToggle: false,
							fontSize: fontSize + 'px'
						},
						show: true,
						placement: 'outsideGrid',
						location: 'e',
						border: 'none'
					};
				}
			},

			getResizeTickConf: function (fontSize) {
				if (fontSize === 0) {
					return 'auto';
				}
				else {
					return fontSize + 'px';
				}
			},


			// ************************* core functions *********************

			initialize: function (options) {

				// reassign this.options because they'll all be shared across instances
				this.options = options;

				//configure
				//this._readDOM();

				//create. This may abort if the model is not available yet.
				this.render();

				// events
				this._bindEvents();

				_.bindAll(this, "draw");

			},


			_readDOM: function () {

				//reads the data-def from the master element
				var strconf = this.$el.data('def');
				var domconf = attrparser.parse(strconf);
				$.extend(this.options, domconf);

			},

			_bindEvents: function () {


			},

			_requestRender: function () {
				this.options.requestRendering = true;

				if (this.$el.is(':visible')) {
					this._performRender();
				}
			},

			_performRender: function () {
				if (this.options.requestRendering === true) {
					this.render();
					this.options.requestRendering = false;
				}
			},

			render: function () {

				var series = this.getSeries();
				var conf = this.getConf(series);

				this.draw(series, conf);

			},

			draw: function (series, conf) {

				var viewId = this.$el.attr('id');

				if (series.length === 0 || series[0].length === 0) {
					this.drawNoplot(viewId);
				}

				else if (this.plot === undefined) {
					this.plot = $.jqplot(viewId, series, conf);
				}

				else {
					conf.data = series;
					this.plot.replot(conf);
				}

			},

			remove: function () {
				this.undelegateEvents();
				if (!!this.plot) {
					this.plot.destroy();
				}
				Backbone.View.prototype.remove.call(this);
			},

			drawNoplot: function (viewId) {

				var view = $("#" + viewId);

				view.empty();

				var msg = translator.get('chart.label.empty');

				var html = Handlebars.compile(emptyChartTemplate)(msg);

				view.append(html);
			}

		});

	});
