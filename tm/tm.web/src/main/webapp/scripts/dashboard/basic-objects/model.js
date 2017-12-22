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
 * this.options : {
 *	urlRoot :	the url where to fetch the data. Note that it may contain predefined query string arguments. 
 *				NB : this is a native Backbone ctor parameter.
 *	model :		a javascript object being the model. If left undefined, will attempt to read from the cache (if configured to do so). 
 *				NB : this is a native Backbone ctor parameter.
 *	cacheKey : string. If defined, will use a cache. Contrary to what you might think, this will not shortcircuit ajax calls : we still want to 
 *				update our data from the server on user request. This cache will be used when the model is initialized only : when no 'model' 
 *				is supplied with the init options, it will attempt to lookup the cache instead. Besides this, whenever the model is updated after 
 *				successful remote call the cache will be updated accordingly.
 *	includeTreeSelection : if defined and true, will include the node selected in the tree in the query string when fetching the model.
 *	syncmode : "passive" or "tree-listener". Default is "passive". See below for details.
 *	}
 *
 * ----
 *
 *	syncmode :
 *	"passive" : the model will be synchronized only when requested to.  
 *	"tree-listener" : Will listen to the tree and trigger synchronization everytime the node selection changes. Incidentally, will force 'includeTreeSelection' to true.
 */
define(["jquery", "backbone", "underscore", 'workspace.storage', "tree", "workspace.event-bus"], 
		function($, Backbone, _, cache, zetree, eventBus){

	return Backbone.Model.extend({
		
		initialize : function(_attributes, options){
			
			if (options.url === undefined){
				throw "dashboard : cannot initialize the model because no url was provided";
			}

			this.urlRoot = options.url;
			this.options = options;
			this.tree = zetree.get();
			this.eventBus = eventBus;
		
			this._configure(_attributes);

			this._bindEvents();

		},
		
		_configure : function(_attributes){

			
			/*
			 * 1/ configure attributes from supplied model, or from cache.
			 */
			var attributes = _attributes;
			
			// fetch the model from the cache if no model is supplied.
			if (attributes === undefined && this.options.cacheKey !==undefined){
				attributes = cache.get(this.options.cacheKey);
			}
			
			//if attributes is eventually defined but has no timestamp, add one.
			if (!! attributes && attributes.timestamp===undefined){
				attributes.timestamp = new Date();
			}
			
			this.set(attributes);		
			
			/*
			 * 2/ resolve the dependencies among parameters.  
			 */
			
			// force 'includeTreeSelection' if 'syncmode' is 'tree-listener'
			if (this.options.syncmode==="tree-listener"){
				this.options.includeTreeSelection = true;
			}
			
		},
		
		
		
		_bindEvents : function(){
			if (this.options.syncmode === "tree-listener"){
				
				var self = this;
				
				/* 
				 * Synchronize when tree selection changes
				 * we debounce the function so that it will fire only when the user is done with selecting nodes in the tree.
				 */
				var syncOnSelect = _.debounce(function(){
					self.fetch();
				}, 500);
				
				/* 
				 * Unbind events when the model is destroyed.
				 */ 
				var unbindOnClear = function(){
					self.tree.off('select_node.jstree deselect_node.jstree', syncOnSelect);
				};
				
				this.tree.on('select_node.jstree deselect_node.jstree', syncOnSelect);
				this.eventBus.onContextual('contextualcontent.clear', unbindOnClear);
				
			}
		},
		
		sync : function(method, model, options){
			
			if (method !== "read"){
				return;	//this is a read-only operation
			}
			
			/* 
			 * override the success handler and automatically add the 
			 * timestamp of this model on completion and store to cache if needed.
			 * 
			 * There were more elegant ways to do so but this one ensures
			 * that the timestamp will be set before any other callback is 
			 * triggered.
			 */ 
			var cacheKey = this.options.cacheKey; 
			
			var oldsuccess = options.success;
			options.success = function(data, status, xhr){
				
				model.set('timestamp', new Date());
				
				if (oldsuccess!==undefined){
					oldsuccess.call(this, data, status, xhr);
				}
				
				if (!! cacheKey){
					cache.set(cacheKey, model.toJSON(), 24);	//expires after 24 hours
				}
			};
			
			// includes tree parameters if requested so
			if (this.options.includeTreeSelection === true){
				options.data = this._treeParams();
			}
			
			// last, include a timestamp to prevent aggressive caching from IE
			options.data = options.data || {};
			options.data._time = new Date().getTime();
			
			return Backbone.Model.prototype.sync.call(this, method, model, options);
		},
		
		//tells whether the model is ready of needs to be loaded.
		isAvailable : function(){
			return ! _.isEmpty ( this.toJSON() );
		},
		
		
		_treeParams : function(){
			
			var selectedNodes = this.tree.jstree('get_selected');
			
			var libIds = selectedNodes.filter(':library').map(function(i,e){
				return $(e).attr('resid');
			}).get();
		
			var nodeIds = selectedNodes.not(':library').map(function(i,e){
				return $(e).attr('resid');
			}).get();
			
			return {
				libraries : libIds.join(','),
				nodes : nodeIds.join(',')
			};			
		}
		
	});
	
});