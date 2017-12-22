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
 * settings : {
 *	master : a css selector that identifies the master dom element initialization,
 *	model : a javascript object, workspace-dependent, containing the data that will be plotted (optional, may be undefined),  
 *	
 *  url : the url where to use fetch the data
 *	rendering : one of "toggle-panel", "plain". This is a hint that tells how to render the dashboard master,
 *	listenTree : boolean. If true, the model will listen to the tree. It false, it won"t. Default is false.
 *	cacheKey : string. If defined, will use the cache with that key.
 * }
 * 
 * Note : "master" and "model" must be provided as javascript object. The other data such as "url", "rendering", "listenTree" etc 
 * can be read from the DOM, using a "data-def" clause on the master dom element.  
 * 
 */
define([ "jquery", "underscore", "squash.attributeparser", "./basic-objects/model",
		"./basic-objects/timestamp-label", "./basic-objects/figleaf-view", "backbone","user-account/user-prefs" ], function($, _, attrparser, StatModel,
		Timestamp, FigLeafView, Backbone, userPrefs) {
/**
 * When creating a SuperMasterView, one can pas as an option an initCharts function :
 * <code> 
 * new View({
 *   el: ..., 
 *   model: ..., 
 *   initCharts: function() {
 *     // create chart views
 *     ...
 *     
 *     return [ ... ]; // array of created views
 *   });
 *  </code>
 *  
 *  `initCharts` shall be called when this view is initialized with `this` bound to this view. 
 *  It should return an array of subviews which will be appended to this high level view's subviews.
 */
	var SuperMasterView = Backbone.View.extend({
		options: {
			initCharts: function() { return []; }
		}, 

		initialize : function(options) {
			// read the conf elements from the dom
			var domconf = attrparser.parse(this.$el.data("def"));
			var modelconf = $.extend(true, {}, options.modelSettings, domconf);
			
			this.options.initCharts = options.initCharts || this.options.initCharts; 
			
			// coerce string|boolean to boolean
			var isTreeListener = (modelconf.listenTree === "true") || (modelconf.listenTree === true);
			
			// create the model
			this.model = new StatModel(modelconf.model, {
				url : modelconf.url,
				includeTreeSelection : isTreeListener,
				syncmode : (isTreeListener) ? "tree-listener" : "passive",
						cacheKey : modelconf.cacheKey
			});
			
			this.initFigleaves();
			this.initViews();
		},
		
		events : {
			"click .dashboard-refresh-button" : "syncmodel",
			"click .show-favorite-dashboard-button" : "showFavoriteDashboard",
			"click .show-milestone-favorite-dashboard-button" : "showMilestoneFavoriteDashboard"
		},

		getBasicViews : function() {
			var self = this;
			return [ new Timestamp({
				el : self.$(".dashboard-timestamp").get(0),
				model : self.model
			}) ];
		},

		initFigleaves : function() {
			var self = this;
			var panels = this.$(".dashboard-figleaf");
			panels.each(function(index, panel) {
				new FigLeafView({
					el : panel,
					model : self.model
				});
			});
		},

		initViews : function() {
			this.views = _.extend({}, this.getBasicViews(), this.options.initCharts.apply(this, arguments));
		},

		syncmodel : function() {
			this.model.fetch();
		},

		showFavoriteDashboard : function () {
			var callback = function() {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("favoriteDashboard.showFavorite");
			};

			userPrefs.chooseFavoriteDashboardInWorkspace(callback);
		},

		showMilestoneFavoriteDashboard : function() {
			var callback = function() {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("favoriteDashboard.milestone.showFavorite");
			};

			userPrefs.chooseFavoriteDashboardInWorkspace(callback);
		}
	});
	return SuperMasterView;

});