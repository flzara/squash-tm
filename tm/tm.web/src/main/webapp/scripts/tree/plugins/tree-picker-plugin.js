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
define(['jquery'], function($){
		
	return function(){
		
		/*
		 * specialization for tree-pickers. will maintain the order in which nodes were selected and redefine get_selected to
		 * return the nodes in that order.
		 */
		$.jstree.plugin("treepicker", {
			__init : function() {
				this.data.treepicker.counter = 0;
				this.data.treepicker.ordering = $();
				var container = this.get_container();

				container.bind("select_node.jstree", $.proxy(function(e, data) {
					var id = data.rslt.obj.attr('resid');
					var counter = this.data.treepicker.counter++;
					data.rslt.obj.attr('order', counter);
				}, this));
				
				container.bind("open_node.jstree", $.proxy(function(e, data) {
					var children = $("li",data.rslt.obj);
					var typePluginConf = data.inst.get_settings().types.types;
					for(var i=0; i<children.size(); i++){
						var domType = $(children[i]).attr('rel');
						var config = typePluginConf[domType];
						if(config === undefined || config.valid_children === 'none'){
							$(children[i]).removeClass("jstree-open").removeClass("jstree-closed").addClass("jstree-leaf");
						}
					}
				}, this));
			},
			_fn : {
				get_selected : function() {
					var selected = this.__call_old();
					var sorted = selected.sort(function(a, b) {
						var order_a = $(a).attr('order');
						var order_b = $(b).attr('order');
						return order_a - order_b;
					});

					return sorted;
				},
				
				/*
				 * that function will prevent drop of a node in the same tree (that should be 
				 * possible only for workspace tree).
				 * 
				 * The actual handler for the drop is declared in the common-conf of the 
				 * package tree-picker
				 */ 
				check_move : function(){
					return false;
				}
			}

		});		
		
		
	};
	
});