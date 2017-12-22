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
define([ 'jquery', 'jstree' ], function($) {

	return function() {

		/*
		 * Thanks to Michael (yes, the unicorn).
		 * Well, thanks to https://code.google.com/p/jstree/source/diff?spec=svn197&old=196&r=197&format=unidiff&path=%2Ftrunk%2Fjquery.jstree.js
		 * Commit 197 from the jquery.jstree 
		 */
		"use strict";
		$.jstree.defaults.select_range_modifier = "shift" ;
		
		// Bind here maybe it'll work !
		$('#tree').bind("select_node.jstree", function (e, data) { 
      data.rslt.obj.parents('.jstree-closed').each(function () { 
        data.inst.open_node(this); 
      }); 
		}); 

		/*		$.jstree.plugin("continuousselect", {
			_fn : {
				
				_select_node :*/
		
		 $.jstree._fn.select_node  = function (obj, check, e) {
					obj = this._get_node(obj);
					if(obj == -1 || !obj || !obj.length) { return false; }
					var s = this._get_settings().ui,
					  is_multiple = (s.select_multiple_modifier == "on" || (s.select_multiple_modifier !== false && e && e[s.select_multiple_modifier + "Key"])),
					  is_range = (s.select_range_modifier !== false && e && e[s.select_range_modifier + "Key"] && this.data.ui.last_selected && this.data.ui.last_selected[0] !== obj[0] && this.data.ui.last_selected.parent()[0] === obj.parent()[0]),
					  is_selected = this.is_selected(obj),
						proceed = true,
						t = this;
					if(check) {
						if(s.disable_selecting_children && is_multiple && obj.parents("li", this.get_container()).children(".jstree-clicked").length) {
							return false;
						}
						proceed = false;
						switch(!0) {
							case (is_range):
									this.data.ui.last_selected.addClass("jstree-last-selected");
							    obj = obj[ obj.index() < this.data.ui.last_selected.index() ? "nextUntil" : "prevUntil" ](".jstree-last-selected").andSelf();
							    if(s.select_limit == -1 || obj.length < s.select_limit) {
								    this.data.ui.last_selected.removeClass("jstree-last-selected");
								    this.data.ui.selected.each(function () {
									    if(this !== t.data.ui.last_selected[0]) { t.deselect_node(this); }
									    });
								    is_selected = false;
								    proceed = true;
								    }
							    else {
								    proceed = false;
								    }
							    break;
							case (is_selected && !is_multiple): 
								this.deselect_all();
								is_selected = false;
								proceed = true;
								break;
							case (!is_selected && !is_multiple): 
								if(s.select_limit == -1 || s.select_limit > 0) {
									this.deselect_all();
									proceed = true;
								}
								break;
							case (is_selected && is_multiple): 
								this.deselect_node(obj);
								break;
							case (!is_selected && is_multiple): 
								if(s.select_limit == -1 || this.data.ui.selected.length + 1 <= s.select_limit) { 
									proceed = true;
								}
								break;
						}
					}
					if(proceed && !is_selected) {
						if(!is_range) { this.data.ui.last_selected = obj; }
						obj.children("a").addClass("jstree-clicked");
						if(s.selected_parent_open) {
							obj.parents(".jstree-closed").each(function () { t.open_node(this, false, true); });
						}
						this.data.ui.selected = this.data.ui.selected.add(obj);
						this.__callback({ "obj" : obj, "e" : e });
					}
				};
				};
		});
