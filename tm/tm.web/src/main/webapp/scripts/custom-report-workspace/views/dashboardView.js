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
 * This view contains logic and data to create and upadate custom report dashboard.
 * The main component is a grid based on a js library : Gridster.js
 * The version used is a maintened fork : https://github.com/dsmorse/gridster.js
 * The library used for data plotting is the same as classic dashboard JqPlot
 */
define(["jquery", "underscore", "backbone", "squash.translator", "handlebars", "tree", "workspace.routing", "../charts/chartFactory", "isIE", "squash.dateutils", "jquery.gridster"],
	function ($, _, Backbone, translator, Handlebars, tree, urlBuilder, main, isIE, dateutils) {
		"use strict";

		var View = Backbone.View.extend({

            //here we have 'class' variable. ie, all instance share the same variable
			el: "#contextual-content-wrapper",
			tpl: "#tpl-show-dashboard",
			tplChart: "#tpl-chart-in-dashboard",
			tplNewChart: "#tpl-new-chart-in-dashboard",
			tplChartDisplay: "#tpl-chart-display-area",
			tplDashboardDoc: "#tpl-dashboard-doc",
			widgetPrefixSelector: "#widget-chart-binding-",
			gridCol: 4,
			gridRow: 3,
			gridAdditionalRow: 10,
			gridColMargin: 10,
			gridRowMargin: 10,
			newChartSizeX: 1,
			newChartSizeY: 1,
			maxChartSizeX: 4,
			maxChartSizeY: 3,
			cssStyleTagId: "gridster-stylesheet-squash",
			// xSizeWidget: null,//this attribute will be computed by calculateWidgetDimension
			// ySizeWidget: null,//this attribute will be computed by calculateWidgetDimension
			secureBlank: 5,//in pixel, a margin around widget to prevent inesthetics scrollbars
            
            //Instance variable are initialised in initialize function
			initialize: function (options) {
				this.options = options;
				var self = this;
				this.xSizeWidget = null,//this attribute will be computed by calculateWidgetDimension
				this.ySizeWidget = null,//this attribute will be computed by calculateWidgetDimension
				//fetching the acls so we can adapt the view with user rights
				//Initial data that will be set by ajax request, contains dashboard attributes and bindings. NOT UPDATED by user actions. Will be reinitialized on refresh.
                this.dashboardInitialData = null;
                //Map of the charts drawn on the grid. Each object inside this map is a backbone view.
                this.dashboardChartViews = {};
                //Map of the binding, ie all the CustomReportChartBinding for this dashboard. UPDATED by user actions and synchronized with server.
                //Initialy set with CustomReportChartBinding presents inside initial data
                this.dashboardChartBindings = {};
                this.gridster = null;
                this.i18nString = translator.get({
                    "dateFormat": "squashtm.dateformat",
                    "dateFormatShort": "squashtm.dateformatShort"
                });

                _.bindAll(this,"initializeData","render","initGrid","initListenerOnTree","dropChartInGrid","generateGridsterCss","redrawDashboard","serializeGridster","canWrite");
                this.initializeData();
                this.initListenerOnTree();
                this.initListenerOnWindowResize();
                this.refreshCharts = _.throttle(this.refreshCharts, 1000);//throttle refresh chart to avoid costly redraw of dashboard on resize
				
			},

			events: {
				"click .delete-chart-button": "unbindChart",
				"click .favorite-select": "chooseFavoriteDashboard",
				"transitionend #dashboard-grid": "refreshCharts",
				"webkitTransitionEnd #dashboard-grid": "refreshCharts",
				"oTransitionEnd #dashboard-grid": "refreshCharts",
				"MSTransitionEnd #dashboard-grid": "refreshCharts",
				"click #toggle-expand-left-frame-button": "toggleDashboard",
				"click #rename-dashboard-button": "rename"
			},

			/**
			 * Initialize dashboard data from server.
			 */
			initializeData: function () {
				var url = urlBuilder.buildURL("custom-report-dashboard-server", this.model.get('id'));
				var self = this;

				this.options.acls.fetch({})
					.then(function () {
						//1.15 favorite dashboard in classic workspaces. In that case we must pass the model (ie tree selection to server)
						//I choose to do a POST request to avoid the nasty URL param size limit witch is causing serious unresolved bug in research workspace.
						//Even if the http semantic suggest to do a GET in that kind of request...
						if(self.options.model.get("showInClassicWorkspace")){
							var scope = self.options.model.get("dynamicScopeModel");
							return $.ajax({
								'type' : 'POST',
								'contentType' : 'application/json',
								'url':url,
								'data': JSON.stringify(scope)
								});
							}

						return $.ajax({
							url: url,
							type: 'GET',
							dataType: 'json'
						});
					}).then(function (response) {
					self.buildBindingData(response)
						.render()
						.generateGridsterCss()
						.initGrid()
						.buildDashBoard()//templating first, then init gridster css, then init gridster, then add charts into widgets, then init button
						.initFavoriteButton();
				});
			},

			render: function () {
				this.$el.html("");
				var source = $(this.tpl).html();
				var template = Handlebars.compile(source);
				Handlebars.registerPartial("chart", $(this.tplChart).html());
				Handlebars.registerPartial("dashboardDoc", $(this.tplDashboardDoc).html());

				//this bolean is used to allow cleaner html code... i prefer avoid complex test in handlebars templates
				if (this.dashboardInitialData.chartBindings.length === 0) {
					this.dashboardInitialData.emptyDashboard = true;
				} else {
					this.dashboardInitialData.emptyDashboard = false;
				}
                
				this.$el.append(template(this.dashboardInitialData));
				return this;
			},

			/**
			 * init Gridster. Note that the css auto generation is turned off.
			 * It wasn't suited for our needs especially for resize window/container.
			 * @return {[this]} [for chaining]
			 */
			initGrid: function () {
				var self = this;
				var resizeable = self.canWrite();
				this.gridster = this.$("#dashboard-grid").gridster({
					widget_margins: [self.gridColMargin, self.gridRowMargin],
					widget_base_dimensions: [self.xSizeWidget, self.ySizeWidget],
					widget_selector: ".dashboard-graph",
					min_rows: self.gridRow,
					min_cols: self.gridCol,
					extra_rows: 0,
					max_rows: self.gridRow,
					max_cols: self.gridCol,
					shift_larger_widgets_down: false,
					autogenerate_stylesheet: false,//Turned off
					serialize_params: function ($w, wgd) {
						var chartBindingId = $w.find(".chart-display-area").attr("data-binding-id");
						return {
							id: chartBindingId,
							col: wgd.col,
							row: wgd.row,
							sizeX: wgd.size_x,
							sizeY: wgd.size_y
						};
					},
					resize: {
						enabled: resizeable,
						max_size: [self.maxChartSizeX, self.maxChartSizeY],
						start: function (e, ui, $widget) {
							self.cacheWidgetOriginalSize(e, ui, $widget);
						},
						resize: function (e, ui, $widget) {
							self.resizeChart(e, ui, $widget);
						},
						stop: function (e, ui, $widget) {
							self.resolveConflict($widget);
							self.resizeChart(e, ui, $widget);
						}
					},
					draggable: {
						stop: function (e, ui, $widget) {
							self.serializeGridster();
						}
					}
				}).data('gridster');

				if (!this.canWrite()) {
					this.gridster.disable();
				}

				this.getGridScreenDimension();
				return this;
			},

			/**
			 * Update all charts. The binding are given by this.dashboardChartBindings
			 */
			refreshCharts: function () {
				var bindings = _.values(this.dashboardChartBindings);
				for (var j = 0; j < bindings.length; j++) {
					var binding = bindings[j];
					this.changeBindedChart(binding.id, binding);
				}
			},

			toggleDashboard: function () {
				var self = this;
				_.delay(function () {
					self.redrawDashboard();
				}, 50);
			},

			canWrite: function () {
				return _.contains(this.options.acls.get("perms"), "WRITE") && squashtm.app.customReportWorkspaceConf;
			},

			/**
			 * Override of gridster generated css. The main goal is to support dynamic resize of widget size,
			 * as the parent container size change.
			 * The generated css is injected in document head before the grid is resized
			 * @return {[this]} [for chaining]
			 */
			generateGridsterCss: function () {
				var boundingRect = this.getGridScreenDimension();
				var xSizeWidget = this.calculateWidgetDimension(boundingRect.width, this.gridCol, this.gridColMargin, this.secureBlank);
				var ySizeWidget = this.calculateWidgetDimension(boundingRect.height, this.gridRow, this.gridRowMargin, this.secureBlank);
				var xPosStyle = this.generateColPositionCss(xSizeWidget);
				var widthStyle = this.generateColWidthCss(xSizeWidget);
				var yPosStyle = this.generateRowPositionCss(ySizeWidget);
				var heightStyle = this.generateRowHeightCss(ySizeWidget);
				this.injectCss(xPosStyle, widthStyle, yPosStyle, heightStyle);
				this.xSizeWidget = xSizeWidget;
				this.ySizeWidget = ySizeWidget;
				return this;
			},

			getGridScreenDimension: function () {
				var boundingRect = document.getElementById('dashboard-grid').getBoundingClientRect();
				return boundingRect;
			},

			calculateWidgetDimension: function (totalDimension, nbWidget, margin, secureBlank) {
				var dimWidget = totalDimension / nbWidget - margin - secureBlank;
				return Math.round(dimWidget);
			},

			generateColPositionCss: function (xSizeWidget) {
				var xPosStyle = "";
				for (var i = 1; i <= this.gridCol; i++) {//generating css for gridCol + 1 column
					xPosStyle = xPosStyle + this.getColumnPositionCss(i, xSizeWidget);
				}
				return xPosStyle;
			},

			generateColWidthCss: function (xSizeWidget) {
				var xWidthStyle = "";
				for (var i = 1; i <= this.maxChartSizeX; i++) {
					xWidthStyle = xWidthStyle + this.getColumnWidthCss(i, xSizeWidget);
				}
				return xWidthStyle;
			},

			getColumnPositionCss: function (indexCol, xSizeWidget) {
				var xPosition = this.gridColMargin * indexCol + xSizeWidget * (indexCol - 1);
				var xStyle = '[data-col="' + indexCol + '"] { left:' + Math.round(xPosition) + 'px; }';
				return xStyle;
			},

			getColumnWidthCss: function (xSize, xSizeWidget) {
				var width = xSize * xSizeWidget + (xSize - 1) * this.gridColMargin;//don't forget to add xSize-1 margin
				var xStyle = '[data-sizex="' + xSize + '"] { width:' + Math.round(width) + 'px; }';
				return xStyle;
			},

			generateRowPositionCss: function (ySizeWidget) {
				var yPosStyle = "";
				var nbRow = this.gridRow + this.gridAdditionalRow;
				for (var i = 1; i <= nbRow; i++) {//generating css for some additional rows if user expands to much a chart
					yPosStyle = yPosStyle + this.getRowPositionCss(i, ySizeWidget);
				}
				return yPosStyle;
			},

			generateRowHeightCss: function (ySizeWidget) {
				var heightStyle = "";
				for (var i = 1; i <= this.maxChartSizeY; i++) {
					heightStyle = heightStyle + this.getRowHeightCss(i, ySizeWidget);
				}
				return heightStyle;
			},

			getRowPositionCss: function (indexRow, ySizeWidget) {
				var yPosition = this.gridRowMargin * indexRow + ySizeWidget * (indexRow - 1);
				var yStyle = '[data-row="' + indexRow + '"] { top:' + Math.round(yPosition) + 'px; }';
				return yStyle;
			},

			getRowHeightCss: function (ySize, ySizeWidget) {
				var height = ySize * ySizeWidget + (ySize - 1) * this.gridRowMargin;//don't forget to add ySize-1 margin
				var yStyle = '[data-sizey="' + ySize + '"] { height:' + Math.round(height) + 'px; }';
				return yStyle;
			},

			injectCss: function (xPosStyle, widthStyle, yPosStyle, heightStyle) {
				var fullStyle = xPosStyle + widthStyle + yPosStyle + heightStyle;
				var cssStyleTagSelector = '#' + this.cssStyleTagId;
				var styleTag = $(cssStyleTagSelector);//global jquery, as we have to inject css in head...
				if (styleTag.length === 0) {
					$("head").append('<style id="' + this.cssStyleTagId + '" type="text/css"></style>');
					styleTag = $(cssStyleTagSelector);
				}
				styleTag.html("");
				styleTag.html(fullStyle);
			},

			initListenerOnTree: function () {
				var wreqr = squashtm.app.wreqr;
				var self = this;
				wreqr.on("dropFromTree", function (data) {
					if (self.canWrite()) {
						var idTarget = data.r.attr('id');
						if (idTarget === 'dashboard-grid') {
							self.dropChartInGrid(data);
						}
						else {
							self.dropChartInExistingChart(data);
						}
					}
				});
			},


			initListenerOnWindowResize: function () {
				var lazyInitialize = _.throttle(this.redrawDashboard, 500);
				var self = this;
				//adding a namespace to resize event to avoid conflict with other resize handler, and allow proper event removing
				$(window).on('resize.dashboard', function () {
					lazyInitialize();
				});
			},

			initFavoriteButton : function () {
				$("#change-favorite-dashboard-button").buttonmenu();
				return this;
			},

			//create a new customReportChartBinding in database and add it to gridster in call back
			dropChartInGrid: function (data) {
				var cell = this.getCellFromDrop();

				var ajaxData = {
					dashboardNodeId: this.model.id,
					chartNodeId: data.o.getResId(),
					sizeX: this.newChartSizeX,
					sizeY: this.newChartSizeY,
					col: cell.col,
					row: cell.row
				};

				var url = urlBuilder.buildURL("custom-report-chart-binding");
				var self = this;
				$.ajax({
						headers: {
							'Accept': 'application/json',
							'Content-Type': 'application/json'
						},
						url: url,
						type: 'post',
						'data': JSON.stringify(ajaxData)
					})
					.success(function (response) {
						self.addNewChart(response);
					});

			},

			dropChartInExistingChart: function (data) {
				var chartNodeId = data.o.getResId();
				var bindingId = $(data.r).parents(".chart-display-area").attr("data-binding-id");//id of binding on wich new chart is dropped

				var url = urlBuilder.buildURL("custom-report-chart-binding-replace-chart", bindingId, chartNodeId);
				var self = this;
				$.ajax({
						headers: {
							'Accept': 'application/json',
							'Content-Type': 'application/json'
						},
						url: url,
						type: 'post'
					})
					.success(function (response) {
						self.changeBindedChart(bindingId, response);
					});
			},

			redrawDashboard: function () {
				console.log("REDRAW !!!!");
				var bindings = _.values(this.dashboardChartBindings);
				var self = this;
				//update initial data with changes done by user since initialization
				this.dashboardInitialData.chartBindings = bindings;
				this.gridster.destroy();
				this.render().generateGridsterCss().initGrid();
				//As ie don't support css animations we fallback on traditionnal js with delay to wait end of transition
				//As squash 1.15 we also need it for classic workspace (req, tc and campaign) as the events don't seems to propagate inside contextual content...
				if (isIE() || this.options.model.get("showInClassicWorkspace")) {
					console.log("classic refresh");
					_.delay(function () {
						self.refreshCharts();
					}, 1000);
				}
			},

			buildBindingData: function (response) {
				this.dashboardInitialData = response;//We need to cache the initial data for templating, ie render()
                this.dashboardInitialData.canWrite = this.canWrite;//copy rights to allow effcient templating
				this.dashboardInitialData.generatedDate = this.i18nFormatDate(new Date());
				this.dashboardInitialData.generatedHour = this.i18nFormatHour(new Date());
				var bindings = response.chartBindings;
				for (var i = 0; i < bindings.length; i++) {
					var binding = bindings[i];
					var id = binding.id;
					this.dashboardChartBindings[id] = binding;
				}
				return this;
			},

			i18nFormatDate: function (date) {
				return dateutils.format(date, this.i18nString.dateFormatShort);
			},

			i18nFormatHour: function (date) {
				return dateutils.format(date, "HH:mm");
			},

			buildChart: function (binding) {
				var id = binding.id;
				var selector = "#chart-binding-" + id;
				this.dashboardChartViews[id] = main.buildChart(selector, binding.chartInstance);
			},

			//rebuild an existing chart
			redrawChart: function (id) {
				var view = this.dashboardChartViews[id];
				view.render();
			},

			buildDashBoard: function () {
				var bindings = _.values(this.dashboardChartBindings);
				for (var i = 0; i < bindings.length; i++) {
					var binding = bindings[i];
					this.buildChart(binding);
				}
				return this;
			},

			resizeChart: function (e, ui, $widget) {
				var chartBindingId = $widget.attr("data-binding-id");//get binding id
				this.redrawChart(chartBindingId);
			},

			serializeGridster: function () {
				var gridData = this.gridster.serialize();
				var url = urlBuilder.buildURL("custom-report-chart-binding");
				var self = this;
				$.ajax({
					headers: {
						'Accept': 'application/json',
						'Content-Type': 'application/json'
					},
					url: url,
					type: 'put',
					'data': JSON.stringify(gridData)
				}).success(function (response) {
					//updating bindings
					_.each(gridData, function (widgetData) {//as resize or move can alter several widgets, all bindings must be updateds
						var binding = self.dashboardChartBindings[widgetData.id];
						binding.row = widgetData.row;
						binding.col = widgetData.col;
						binding.sizeX = widgetData.sizeX;
						binding.sizeY = widgetData.sizeY;
					});
				});
			},

			addNewChart: function (binding) {
				if (_.size(this.dashboardChartBindings) === 0 && this.$el.find("#dashboard-doc").length === 1) {
					this.$el.find("#dashboard-doc").remove();
				}
				var source = $(this.tplNewChart).html();
				var template = Handlebars.compile(source);
				var html = template(binding);
				this.gridster.add_widget(html, binding.sizeX, binding.sizeY, binding.col, binding.row);
				this.buildChart(binding);
				this.addNewBindingInMap(binding);
			},

			unbindChart: function (event) {
				//Get id of the suppressed chart
				if (this.canWrite()) {
					var id = event.currentTarget.getAttribute("data-binding-id");
					var url = urlBuilder.buildURL("custom-report-chart-binding-with-id", id);
					var self = this;
					//Suppress on server and if success, update gridster and update maps properties
					$.ajax({
						url: url,
						type: 'delete'
					}).success(function (response) {
						self.removeChart(id);
						self.removeWidget(id);
					});
				}
			},

			removeAllCharts: function () {
				var views = this.dashboardChartViews;
				_.each(views, function (view) {
					view.remove();
				});
				this.dashboardChartViews = {};
			},

			removeChart: function (bindingId) {
				this.dashboardChartViews[bindingId].remove();//remove backbone view
				delete this.dashboardChartViews[bindingId];
				delete this.dashboardChartBindings[bindingId];
			},

			removeWidget: function (bindingId) {
				var widgetSelector = this.widgetPrefixSelector + bindingId;
				this.gridster.remove_widget(widgetSelector, this.serializeGridster);//after suppressing widget, serialize to update position on server if the grid reorganize itself after widget suppression
			},

			changeBindedChart: function (bindingId, binding) {
				this.removeChart(bindingId);
				var source = $(this.tplChartDisplay).html();
				var template = Handlebars.compile(source);
				var html = template(binding);
				var widgetSelector = this.widgetPrefixSelector + bindingId;
				$(widgetSelector).html(html);
				this.buildChart(binding);
				this.addNewBindingInMap(binding);
			},

			addNewBindingInMap: function (binding) {
				this.dashboardChartBindings[binding.id] = binding;
			},

			//Return the first empty cell.
			getCellFromDrop: function () {
				for (var i = 1; i <= this.gridRow; i++) {
					for (var j = 1; j <= this.gridCol; j++) {
						if (this.gridster.is_empty(j, i)) {
							return {col: j, row: i};
						}
					}
				}
			},

			cacheWidgetOriginalSize: function (e, ui, $widget) {
				this.widgetInitialSizeX = parseInt($widget.attr("data-sizex"));
				this.widgetInitialSizeY = parseInt($widget.attr("data-sizey"));
			},

			resolveConflict: function ($widget) {
				var coords = this.gridster.dom_to_coords($widget);
				if (coords.size_x === this.widgetInitialSizeX && coords.size_y === this.widgetInitialSizeY) {
					//no change, return. Can happend if widget resize go above widget size limit.
					return;
				}
				if (this.hasExeededRowLimit()) {
					this.gridster.resize_widget($widget, this.widgetInitialSizeX, this.widgetInitialSizeY);
					this.redrawDashboard();
				}
				else {
					this.serializeGridster();
				}
			},

			hasExeededRowLimit: function () {
				var overMaxRow = this.gridRow + 1;
				for (var i = 1; i <= this.gridCol; i++) {
					if (this.gridster.is_occupied(i, overMaxRow)) {
						return true;
					}
				}
				return false;
			},

			remove: function () {
				if (this.gridster && this.gridster != null) {
					this.gridster.destroy();
				}
				this.removeAllCharts();
				$(window).off("resize.dashboard");
				Backbone.View.prototype.remove.call(this);
			},

			rename: function () {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("renameNode");
			},
            
            chooseFavoriteDashboard : function (event) {
                var id = this.model.get('id');
				var workspace = event.target.getAttribute("name");
                var url = urlBuilder.buildURL("custom-report-dashboard-favorite",workspace, id);
                $.ajax({
					url: url,
					type: 'post'
				});
            }
		});

		return View;
	});
