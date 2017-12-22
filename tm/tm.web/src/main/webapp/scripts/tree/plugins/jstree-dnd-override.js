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
 * 
 * Override of jstree.dnd.dnd_show. We want it to always target "inside" when the target is a container.
 * 
 * For that purpose we need to modify the "marker". The marker represents the position where the node will be
 * inserted, both graphically and internally. The marker can be set to 3 positions : before, inside and after.
 * 
 * The marker can be set to one of these positions if the node being browsed supports it. Let's call it the "drop
 * profile" of the node. That profile is held by the this.data.dnd object. - For libraries (aka drives) and folders,
 * the drop profile is true, true, true. - For leaves, the profile is true, false, true.
 * 
 * The drop profile is unfortunately not configurable. Also, comparing the drop profile is the only way to
 * differentiate a leaf from the rest.
 * 
 * The present function will identify the hovered node and return the following marker position : - for leaves -> no
 * modification - for folders, libraries, other nodes that can contain nodes -> force 'inside'.
 * 
 */

/*
 * Override of jstree.dnd.start_drag. In order to handle vertical and horizontal scrolling the plugin needs to initialize 
 * some variables and that phase is buggy.  * 
 */
define(['jquery'], function($){
	
	return function(settings){
		
		var overridenM;

		$.jstree._fn.dnd_show = function() {

			// that variable in the vanilla tree is part of the closure context
			// we cannot access here, so we must fetch it by other means.
			if (overridenM === undefined) {
				overridenM = $("div#jstree-marker");
			}
			
			if(!this.data.dnd.prepared) { return; }

			var o = [ "before", "inside", "after" ], r = false, rtl = this._get_settings().core.rtl, pos;

			if (this.data.dnd.w < this.data.core.li_height / 3) {
				o = [ "before", "inside", "after" ];
			} else if (this.data.dnd.w <= this.data.core.li_height * 2 / 3) {
				o = this.data.dnd.w < this.data.core.li_height / 2 ? [ "inside", "before", "after" ] : [ "inside", "after",
						"before" ];
			} else {
				o = [ "after", "inside", "before" ];
			}
			$.each(o, $.proxy(function(i, val) {
				if (this.data.dnd[val]) {
					$.vakata.dnd.helper.children("ins").attr("class", "jstree-ok");
					r = val;
					return false;
				}
			}, this));

			if (r === false) {
				$.vakata.dnd.helper.children("ins").attr("class", "jstree-invalid");
			}

			var pref = this.data.squash.pref;
			
			var workspace = settings.workspace;
			var treepref = localStorage[workspace+"-tree-pref"];
			
			if(treepref != 1){
			// here we override the function. if the profile matches the one of
			// a container, we force r to "inside"
				if (this.data.dnd.before && this.data.dnd.inside && this.data.dnd.after) {
					r = "inside";
				}
			}

			pos = rtl ? (this.data.dnd.off.right - 18) : (this.data.dnd.off.left + 10);

			switch (r) {
			case "before":
				overridenM.css({
					"left" : pos + "px",
					"top" : (this.data.dnd.off.top - 6) + "px"
				}).show();
				break;
			case "after":
				overridenM.css({
					"left" : pos + "px",
					"top" : (this.data.dnd.off.top + this.data.core.li_height - 7) + "px"
				}).show();
				break;
			case "inside":
				overridenM.css({
					"left" : pos + (rtl ? -4 : 4) + "px",
					"top" : (this.data.dnd.off.top + this.data.core.li_height / 2 - 5) + "px"
				}).show();
				break;
			default:
				overridenM.hide();
				break;
			}
			return r;

		};
		
		var old_start_drag = $.jstree._fn.start_drag;
		
		$.jstree._fn.start_drag =  function (obj, e) {
			
			old_start_drag.call(this, obj, e);
			
			/*
			 * The following code from the "super" method is changed. See comments that goes along.
			 * 
			 * this.data.dnd.cof = cnt.children("ul").offset();
			 * this.data.dnd.cw = parseInt(cnt.width(),10);
			 * this.data.dnd.ch = parseInt(cnt.height(),10);
			 */
			
			var cnt = this.get_container();
			
			this.data.dnd.cof = cnt.offset();		// because the children("ul") could be hidden because of the scrolling down, leading to unreachable offset values
			this.data.dnd.cw = parseInt(cnt.width(),10) - 15; //this margin of 15 compensates for the space eaten by scrollbars when they are visible  
			this.data.dnd.ch = parseInt(cnt.height(),10)- 15; //this margin of 15 compensates for the space eaten by scrollbars when they are visible  
			this.data.dnd.active = true;
		
		};
	};
});