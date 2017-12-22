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
 * This view can be subclassed for fun and profit by any view based on JqPlot. It supplies convenient and predefined
 * mechanisms that will handle the conditions under which a view can be actually rendered.
 * 
 * Indeed a jqplot view can only be successfully rendered when the HTML container has non null dimensions, however isn't 
 * possible when that container is not displayed. To work around this, the following rules apply :
 * 
 *  - when the model changes or the window is resized, the view "requests" a rendering.
 *  - when the view is visible or becomes visible, the view "performs" the rendering if a "request" was emitted since the last "perform".
 * 
 * See ._bindEvents() for details regarding implementation. Also, when a "request" is issued, if the view is visible, the rendering is 
 * immediately "performed".
 * 
 * =================================================
 * 
 *  Subclasses must implement :
 *  - getSeries : must return a series of data as jqplot expects it to be,
 *  - getConf : a configuration object that will customize the rendering.
 *  
 *  Services supplied are : 
 *  - embedded support for 'data-def' clauses,
 *  - binding/unbinding update and destroy events,
 *  - safe rendering methods.
 *  
 *  Events listened to (on the event bus) : 
 *  - dashboard.appear : when an external sources triggers that event, the view will perform the rendering
 * 
 */
define(["jquery", "backbone", "squash.attributeparser", "workspace.event-bus", "underscore"],
		function($, Backbone, attrparser, eventbus, _){
	
	return Backbone.View.extend({
		
		// ************************* abstract functions *****************
		
		getSeries : function(){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},
		
		getConf : function(series){
			throw "dashboard : attempted to instanciate an abstract jqplot view !";
		},
		
		
		
		// ************************* core functions *********************

		initialize : function(options){
			
			// reassign this.options because they'll all be shared across instances
			this.options = options;
			
			//configure
			this._readDOM();
			
			//create. This may abort if the model is not available yet.
			this._requestRender();
			
			// events
			this._bindEvents();
		},
		
		
		_readDOM : function(){
			
			//reads the data-def from the master element
			var strconf = this.$el.data('def');
			var domconf = attrparser.parse(strconf);
			$.extend(this.options, domconf);
			
		},
		
		_bindEvents : function(){
			
			// 1) request rendering on resize. 
			// Note : uses a debounced and proxied version of _requestRender, 
			// to limit the firing rate of 'resize' event.
			var proxRequestRender =  $.proxy(this._requestRender, this);
			
			this._wrappedRequestRender = _.debounce(proxRequestRender, 250);
			$(window).on('resize', this._wrappedRequestRender);
			
			// 2) request rendering on model changed. 
			var modelchangeevt = "change";
			if (this.options['model-attribute']!== undefined){
				modelchangeevt+=":"+this.options['model-attribute'];
			}			
			this.listenTo(this.model, modelchangeevt, this._requestRender);
			
			// 3) render when eventually possible
			eventbus.onContextual("dashboard.appear", proxRequestRender);
			
			// 4) destroys itself properly when the content is removed
			var removeOnClear = $.proxy(function() {
				this.remove();
			}, this);
			eventbus.onContextual('contextualcontent.clear', removeOnClear);
			
		},
		
		_requestRender : function(){
			this.options.requestRendering = true;
			
			if (this.$el.is(':visible')){
				this._performRender();
			}
		},
		
		_performRender : function(){
			if (this.options.requestRendering === true){
				this.render();
				this.options.requestRendering = false;
			}
		},
		
		render : function(){
			
			if (! this.model.isAvailable()){
				return;
			}

			var series = this.getSeries();
			var conf = this.getConf(series);		
			
			this.draw(series, conf);

		},
		
		draw : function(series, conf){
			
			if (this.plot === undefined){	
				var viewId = this.$el.find('.dashboard-item-view').attr('id');
				this.plot = $.jqplot(viewId, series, conf);
			}
			
			else{
				conf.data = series;
				this.plot.replot(conf);
			}
			
		},
		
		remove : function(){
			this.undelegateEvents();
			if (!! this.plot){
				this.plot.destroy();
			}
			$(window).off('resize', this._wrappedRequestRender);
			Backbone.View.prototype.remove.call(this);
		}

		
	});
	
});