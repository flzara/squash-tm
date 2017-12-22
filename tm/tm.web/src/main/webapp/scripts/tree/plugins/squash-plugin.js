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
define(['jquery', 'jstree', './tree-node', 'jquery.squash'], function($){
	
	

		
	/***************************************************************************
	* Library part
	**************************************************************************/

	/**
	 * Behaviour of a node when clicked or double clicked. There are two possible paths : 1) the node is not a container
	 * (files, resources) : a/ click events : proceed, b/ double click event (and further click event) : cancel that event
	 * and let the first one complete. 2) the node is a container (libraries, folders, campaigns) : a/ click event : start
	 * a timer. If the timer is not canceled, fire a specific click.jstree event. b/ double click event (and further click
	 * event) : toggle the node and stop event propagation.
	 * 
	 * Basically we'll stop the event propagation everytime except for case 1-a. The case 2-a actually do not let the
	 * event propagate : it fires a new 'click.jstree' event instead. The reason for this is because the following handler
	 * is bound to 'click' and we don't want it to be called again.
	 * 
	 * cases 1-a and 2-a are treated in handleNodeClick, while 1-b and 2-b are treated in handleNodeDblClick.
	 * 
	 * @params : - tree : the tree instance - clickEvent : the click event.
	 * 
	 */

	/**
	 * here we want to delay the event for folders, libraries and campaign (waiting for a possible dblclick), while
	 * letting the event through for the other kind of nodes.
	 */
	function handleNodeClick(tree, event) {
		var target = $(event.target).treeNode();
		var node = target.parent().treeNode();

		if (node.canContainNodes()) {
			if (event.ctrlKey || event.shiftKey) {
				return true;
			}
			event.stopImmediatePropagation();

			tree.data.squash.clicktimer = setTimeout(function() {
				target.trigger('click.jstree');
			}, tree.data.squash.timeout);
		}
	}

	/**
	 * here we handle dblclicks. basically we don't want the event to be processed twice, except for containers that
	 * will toggle their open-close status.
	 */
	function handleNodeDblClick(tree, event) {
		var target = $(event.target);
		var node = target.parent();

		event.stopImmediatePropagation();
		clearTimeout(tree.data.squash.clicktimer);
		tree.toggle_node(node);
	}
	
	/* **********************************************************************
					//Library part
	 ************************************************************************ */
	
	
	/* **********************************************************************
							Plugin definition
	 ************************************************************************ */
	
	return function(){

		$.jstree.plugin("squash", {
			
			__init : function() {

				var tree = this;
				var s = this._get_settings().squash;
				tree.data.squash.timeout = s.timeout;
				tree.data.squash.opened = s.opened || [];	
				tree.data.squash.isie = false;
				tree.data.squash.rootUrl = (s.rootUrl === undefined) ? '' : s.rootUrl;
				var container = this.get_container();
	
				/*
				 * we need our handlers to be bound first note that we are bound to 'click' and not 'click.jstree'. That detail
				 * matters in the handler just below.
				 */
	
				/*
				 * note about click events and browsers specificities : - ff, chrome : 2 clicks fire 2 click and 1 dblclick event.
				 * both event objects have a property .detail - ie 8 : fire click and dblclick alternately.
				 * 
				 * considering the discrepencies between those behavior the node click handling will branch wrt event.detail. FF
				 * and Chrome will simply use a clickhandler, while ie 8 will use both a click and a dblclick handler.
				 * 
				 */
	
				container.bindFirst('click', 'a', function(event, data) {
					if (event.detail && event.detail > 1) {
						event.stopImmediatePropagation(); // cancel the multiple click event for ff and chrome
					} else {
						handleNodeClick(tree, event);
					}
					return false; // returns false to prevent navigation in page (# appears at the end of the URL)
				});
	
				container.bindFirst('dblclick', 'a', function(event, data) {
					handleNodeDblClick(tree, event);
					return false; // returns false to prevent navigation in page (# appears at the end of the URL) 
				});
	
				
				// ripped from the 'cookie' plugin : override the initially selected nodes.
				if (!! tree.data.squash.opened && tree.data.squash.opened.length >0){
					this.data.ui.to_select = tree.data.squash.opened;
					$.cookie("jstree_select", this.data.ui.to_select.join(","), {});
				}
				
				/*
				 * CSS style now. that section is copied/pasted from the original themeroller plugin, kudos mate.
				 * 
				 */
				container.addClass("ui-widget-content").delegate("a", "mouseenter.jstree", function() {
					$(this).addClass(s.item_h);
	
				}).delegate("a", "mouseleave.jstree", function() {
					$(this).removeClass(s.item_h);
	
				}).bind("select_node.jstree", $.proxy(function(e, data) {
					data.rslt.obj.children("a").addClass(s.item_a);
					return true;
	
				}, this)).bind("deselect_node.jstree deselect_all.jstree",$.proxy(function(e, data) {
					this.get_container().find("." + s.item_a).removeClass(s.item_a).end().find(".jstree-clicked").addClass(
							s.item_a);
					return true;
				}, this));
				
			},
			_fn : {

	
				 /*accepts an object, or an array of object. the attributes of the object(s) will be tested against the dom attributes of the nodes
				 and returns those that match all the attributes of at least one of the objects.*/
				/*
				 * example : [ {resid : 10, restype : 'test-cases'}, {resid : 15, rel : 'folder'}]
				 */
				findNodes : function(descriptor) {
					var matchers;
	
					if (descriptor instanceof Array) {
						matchers = descriptor;
					} else {
						matchers = [ descriptor ];
					}
	
					
					var megaselector = "";
					for ( var index = 0; index < matchers.length; index++) {
						
						megaselector += "li";
						var propertiesset = matchers[index];
						for ( var ppt in propertiesset) {
							megaselector += "[" + ppt + "='" + propertiesset[ppt] + "']";
						}
						megaselector += ", ";
					}
					megaselector = megaselector.replace(/, $/,'');
					
					
					try {
						var nodes = this.get_container().find(megaselector).treeNode();
						return nodes;
					} catch (invalide_node) {
						return $();
					}
				},
				
	
				get_selected : function() {
					var selected = this.__call_old();
					if (selected.length > 0) {
						return selected.treeNode();
					} else {
						return selected;
					}
				},
	
				get_selected_ids : function(restype) {
					var self = this;
					var nodes = self.get_selected();
	
					var filtered = $(nodes).filter(function() {
						return $(this).attr('restype') == restype;
					});
	
					var ids = $.map(filtered, function(item) {
						return $(item).attr('resid');
					});
	
					return ids;
				}

	
			},
	
			defaults : {
				"item_h" : "ui-state-active",
				"item_a" : "ui-state-default",
				"timeout" : 500
			}
		});
	
	};
	
	
});