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
 * Sorry if the license of Squash preceeds the original one, blame Maven for that.
 * 
 * 
 *  // ========================================================= \\
 *  ||															 ||
 * 	||							SQUASH TM						 ||
 * 	||															 ||
 * 	\\ ========================================================= //
 * 	
 * Customization of jquery.tableDnD.js by Denis Howlett, credits to his original
 * work.
 * 
 * The current extension add the support for multiline dnd, see the comments on 
 * 'adaptDragObject', 'makeDraggable' and 'mouseup' for details. 
 * 
 * The code that was added or modified will have a head commentary 
 * using the label right above ( a bit flashy but sure you wont miss it).
 * That way if we update to a newer version we can spot easily what must 
 * be ported to the new DnD code.
 * 
 * 
 * Note : as of TM 1.10.0 this script needs to be called ONLY ONCE, at 
 * table creation time (and not anymore once per table content refresht).
 * 
 */
/**
 * TableDnD plug-in for JQuery, allows you to drag and drop table rows You can
 * set up various options to control how the system will work Copyright (c)
 * Denis Howlett <denish@isocra.com> Licensed like jQuery, see
 * http://docs.jquery.com/License.
 * 
 * Configuration options:
 * 
 * onDragStyle This is the style that is assigned to the row during drag. There
 * are limitations to the styles that can be associated with a row (such as you
 * can't assign a border--well you can, but it won't be displayed). (So instead
 * consider using onDragClass.) The CSS style to apply is specified as a map (as
 * used in the jQuery css(...) function). onDropStyle This is the style that is
 * assigned to the row when it is dropped. As for onDragStyle, there are
 * limitations to what you can do. Also this replaces the original style, so
 * again consider using onDragClass which is simply added and then removed on
 * drop. onDragClass This class is added for the duration of the drag and then
 * removed when the row is dropped. It is more flexible than using onDragStyle
 * since it can be inherited by the row cells and other content. The default is
 * class is tDnD_whileDrag. So to use the default, simply customise this CSS
 * class in your stylesheet. onDrop Pass a function that will be called when the
 * row is dropped. The function takes 2 parameters: the table and the row that
 * was dropped. You can work out the new order of the rows by using table.rows.
 * onDragStart Pass a function that will be called when the user starts
 * dragging. The function takes 2 parameters: the table and the row which the
 * user has started to drag. onAllowDrop Pass a function that will be called as
 * a row is over another row. If the function returns true, allow dropping on
 * that row, otherwise not. The function takes 2 parameters: the dragged row and
 * the row under the cursor. It returns a boolean: true allows the drop, false
 * doesn't allow it. scrollAmount This is the number of pixels to scroll if the
 * user moves the mouse cursor to the top or bottom of the window. The page
 * should automatically scroll up or down as appropriate (tested in IE6, IE7,
 * Safari, FF2, FF3 beta dragHandle This is the name of a class that you assign
 * to one or more cells in each row that is draggable. If you specify this
 * class, then you are responsible for setting cursor: move in the CSS and only
 * these cells will have the drag behaviour. If you do not specify a dragHandle,
 * then you get the old behaviour where the whole row is draggable.
 * 
 * Other ways to control behaviour:
 * 
 * Add class="nodrop" to any rows for which you don't want to allow dropping,
 * and class="nodrag" to any rows that you don't want to be draggable.
 * 
 * Inside the onDrop method you can also call $.tableDnD.serialize() this
 * returns a string of the form <tableID>[]=<rowID1>&<tableID>[]=<rowID2> so
 * that you can send this back to the server. The table must have an ID as must
 * all the rows.
 * 
 * Other methods:
 * 
 * $("...").tableDnDUpdate() Will update all the matching tables, that is it
 * will reapply the mousedown method to the rows (or handle cells). This is
 * useful if you have updated the table rows using Ajax and you want to make the
 * table draggable again. The table maintains the original configuration (so you
 * don't have to specify it again).
 * 
 * $("...").tableDnDSerialize() Will serialize and return the serialized string
 * as above, but for each of the matching tables--so it can be called from
 * anywhere and isn't dependent on the currentTable being set up correctly
 * before calling
 * 
 * Known problems: - Auto-scoll has some problems with IE7 (it scrolls even when
 * it shouldn't), work-around: set scrollAmount to 0
 * 
 * Version 0.2: 2008-02-20 First public version Version 0.3: 2008-02-07 Added
 * onDragStart option Made the scroll amount configurable (default is 5 as
 * before) Version 0.4: 2008-03-15 Changed the noDrag/noDrop attributes to
 * nodrag/nodrop classes Added onAllowDrop to control dropping Fixed a bug which
 * meant that you couldn't set the scroll amount in both directions Added
 * serialize method Version 0.5: 2008-05-16 Changed so that if you specify a
 * dragHandle class it doesn't make the whole row draggable Improved the
 * serialize method to use a default (and settable) regular expression. Added
 * tableDnDupate() and tableDnDSerialize() to be called when you are outside the
 * table
 */

define(['jquery'], function(jQuery){
	
	// forbid multiple inclusions
	if (!! jQuery.tableDnD){
		return;
	}

jQuery.tableDnD = {
	/** Keep hold of the current table being dragged */
	currentTable : null,
	/** Keep hold of the current drag object if any */
	dragObject : null,
	/** The current mouse offset */
	mouseOffset : null,
	/** Remember the old value of Y so that we don't do too much processing */
	oldY : 0,
	//deltaY : 0,

	/** Actually build the structure */
	build : function(options) {
		// Set up the defaults if any

		this.each(function() {
			// This is bound to each matching table, set up the defaults and
			// override with user options
			this.tableDnDConfig = jQuery.extend({
				onDragStyle : null,
				onDropStyle : null,
				// Add in the default class for whileDragging
				onDragClass : "tDnD_whileDrag",
				onDrop : null,
				onDragStart : null,
				scrollAmount : 5,
				serializeRegexp : /[^\-]*$/, // The regular expression to use
												// to trim row IDs
				serializeParamName : null, // If you want to specify another
											// parameter name instead of the
											// table ID
				dragHandle : null
			// If you give the name of a class here, then only Cells with this
			// class will be draggable
			}, options || {});
			// Now make the rows draggable
			jQuery.tableDnD.makeDraggable(this);
		});

		// Now we need to capture the mouse up and mouse move event
		// We can use bind so that we don't interfere with other event handlers
		
		/*
		 *  // ========================================================= \\
		 *  ||															 ||
		 * 	||							SQUASH TM						 ||
		 * 	||															 ||
		 * 	\\ ========================================================= //
		 * 
		 * Also narrowed the selector, the original was too broad and prone to 
		 * multiple registration. 
		 * This effectively disables the dnd across multiple tables but we don't do that anyway.
		 */
		var $table = jQuery(this);
		$table.on('mousemove', '>tbody>tr', jQuery.tableDnD.mousemove)
				.on('mouseup', '>tbody>tr', jQuery.tableDnD.mouseup);
				/*.on('mousewheel', '>tbody>tr', jQuery.tableDnD.handleMousewheel)*/


		
		// Don't break the chain
		return this;
	},


	/*
	 *  // ========================================================= \\
	 *  ||															 ||
	 * 	||							SQUASH TM						 ||
	 * 	||															 ||
	 * 	\\ ========================================================= //
	 * 
	 * Here is where happen most of the modifications :
	 * 
	 * 
	 * 1/ the .dragoObject is now a jQuery variable, holding all rows having the
	 * class ui-state-row-selected
	 * 
	 * 2/ jQuery.tableDnD.makeDraggable will call config.onDragStart with that new
	 * .dragObject. It does so whether configured to use a draghandle or not (the
	 * original plugin didn't). 
	 * 
	 * 3/ It uses explicitly a logic css class "nodrop" to prevent dropping in the middle
	 * of the selected lines
	 * 
	 * 4/ 1 method adaptDragObject was added to the plugin and aims to adapt the
	 * interface of the new .dragObject (a jQuery object) to the old one 
	 * (a regular javascript object), so that the rest of the plugin can use it normally.
	 * 
	 * 
	 * 
	 * Note : the original plugin sometimes checks if the current line is equal to
	 * .dragObject. That comparison now always fails safely.
	 * 
	 * 
	 * 
	 * Todo : possibly remove the makeDraggable part and just overwrite the
	 * mousedown event handler on the tr's it attached to them
	 * 
	 */

	adaptDragObject : function(allRows){
		// they all hopefully have the same parent
		var delegateParentNode = allRows.first().parent();

		delegateParentNode.insertBefore = function(jqElts, where) {

			// check 1 : if target is defined then we insert our elements before it,
			// else we insert at the end of this
			// (thus mimicking the regular insertBefore in javascript)
			if (where) {
				// check : the target (where) must have the same parent or nothing
				// should happen
				// note : remember that contains() is a $.fn extensions to cope with
				// .is() of jQuery 1.5 (the one in jq 1.6 is way better)
				if (this.children().contains(where)) {
					jqElts.not(where).insertBefore(where);
				}
			}

			else {
				this.append(jqElts);
			}

		};

		allRows.parentNode = delegateParentNode;
		return allRows;
	},
	

	/**
	 * This function makes all the rows on the table draggable apart from those
	 * marked as "NoDrag"
	 */
	/*
	 *  // ========================================================= \\
	 *  ||															 ||
	 * 	||							SQUASH TM						 ||
	 * 	||															 ||
	 * 	\\ ========================================================= //
	 * 
	 * Changed the event binding : instead of binding on each cells 
	 * the same event handler we bind only one on the table using the 
	 * '.on' jQuery event binder. 
	 * 
	 * Also rewritten a few things on the way. 
	 */
	makeDraggable : function(table) {

		var config = table.tableDnDConfig;
		var $table = jQuery(table);
		var selector = "";
		
		if (table.tableDnDConfig.dragHandle) {
			// We only need to add the event to the specified cells
			var selector = "td." + table.tableDnDConfig.dragHandle;
		}
		else{
			// For backwards compatibility, we add the event to the whole row
			var selector =  "tr:not(.nodrag)>td";
		}
			
		$table.on('mousedown', selector, function(ev){

			var allRows = $table.find(".ui-state-row-selected").add(this.parentNode);
			allRows.addClass('nodrop');
			document.body.style.cursor = "n-resize";
			
			jQuery.tableDnD.dragObject = jQuery.tableDnD.adaptDragObject(allRows);
			
			jQuery.tableDnD.currentTable = table;

			jQuery.tableDnD.mouseOffset = jQuery.tableDnD.getMouseOffset(this, ev);
			
			//jQuery.tableDnD.deltaY = 0;

			if (config.onDragStart) {
				// Call the onDrop method if there is one
				config.onDragStart(table, allRows);
			}
			return false;
		});
		
	},

	updateTables : function() {
		this.each(function() {
			// this is now bound to each matching table
			if (this.tableDnDConfig) {
				jQuery.tableDnD.makeDraggable(this);
			}
		});
	},

	/**
	 * Get the mouse coordinates from the event (allowing for browser
	 * differences)
	 */

	mouseCoords : function(ev) {
		var coords= null;
		
		if (ev.pageX || ev.pageY) {
			coords = {
				x : ev.pageX,
				y : ev.pageY
			};
		}
		else{
			coords = {
				x : ev.clientX + document.body.scrollLeft - document.body.clientLeft,
				y : ev.clientY + document.body.scrollTop - document.body.clientTop
			};
		}

		return coords;
	},

	/**
	 * Given a target element and a mouse event, get the mouse offset from that
	 * element. To do this we need the element's position and the mouse position
	 */
	getMouseOffset : function(target, ev) {
		ev = ev || window.event;

		var docPos = this.getPosition(target);
		var mousePos = this.mouseCoords(ev);
		
		return {
			x : mousePos.x - docPos.x,
			y : mousePos.y - docPos.y
		};
	},

	/**
	 * Get the position of an element by going up the DOM tree and adding up all
	 * the offsets
	 */
	getPosition : function(e) {
		var left = 0;
		var top = 0;
		/** Safari fix -- thanks to Luis Chato for this! */
		if (!e.offsetHeight) {
			/**
			 * Safari 2 doesn't correctly grab the offsetTop of a table row this
			 * is detailed here:
			 * http://jacob.peargrove.com/blog/2006/technical/table-row-offsettop-bug-in-safari/
			 * the solution is likewise noted there, grab the offset of a table
			 * cell in the row - the firstChild. note that firefox will return a
			 * text node as a first child, so designing a more thorough solution
			 * may need to take that into account, for now this seems to work in
			 * firefox, safari, ie
			 */
			e = e.firstChild; // a table cell
		}

		while (e.offsetParent) {
			left += e.offsetLeft;
			top += e.offsetTop;
			e = e.offsetParent;
		}

		left += e.offsetLeft;
		top += e.offsetTop;

		return {
			x : left,
			y : top
		};
	},

	mousemove : function(ev) {
		if (jQuery.tableDnD.dragObject === null) {
			return;
		}

		var dragObj = jQuery(jQuery.tableDnD.dragObject);
		var config = jQuery.tableDnD.currentTable.tableDnDConfig;
		var mousePos = jQuery.tableDnD.mouseCoords(ev);
		var y = mousePos.y - jQuery.tableDnD.mouseOffset.y;
		// auto scroll the window
		var yOffset = window.pageYOffset;
		if (document.all) {
			// Windows version
			// yOffset=document.body.scrollTop;
			if (typeof document.compatMode != 'undefined' && document.compatMode != 'BackCompat') {
				yOffset = document.documentElement.scrollTop;
			} else if (typeof document.body != 'undefined') {
				yOffset = document.body.scrollTop;
			}

		}

		if (mousePos.y - yOffset < config.scrollAmount) {
			window.scrollBy(0, -config.scrollAmount);
		} else {
			var windowHeight = window.innerHeight ? window.innerHeight
					: document.documentElement.clientHeight ? document.documentElement.clientHeight
							: document.body.clientHeight;
			if (windowHeight - (mousePos.y - yOffset) < config.scrollAmount) {
				window.scrollBy(0, config.scrollAmount);
			}
		}

		if (y != jQuery.tableDnD.oldY) {
			// work out if we're going up or down...
			var movingDown = y > jQuery.tableDnD.oldY;
			// update the old value
			jQuery.tableDnD.oldY = y;
			// update the style to show we're dragging
			if (config.onDragClass) {
				dragObj.addClass(config.onDragClass);
			} else {
				dragObj.css(config.onDragStyle);
			}
			// If we're over a row then move the dragged row to there so that
			// the user sees the
			// effect dynamically
			var currentRow = jQuery.tableDnD.findDropTargetRow(dragObj, y);
			if (currentRow) {
				// TODO worry about what happens when there are multiple TBODIES
				if (movingDown && jQuery.tableDnD.dragObject != currentRow) {
					jQuery.tableDnD.dragObject.parentNode.insertBefore(
							jQuery.tableDnD.dragObject, currentRow.nextSibling);
				} else if (!movingDown
						&& jQuery.tableDnD.dragObject != currentRow) {
					jQuery.tableDnD.dragObject.parentNode.insertBefore(
							jQuery.tableDnD.dragObject, currentRow);
				}
			}
		}

		return false;
	},
	
	/*
	 *  experimental. deactivated because non functional yet.
	 *  
	 *  TODO : search for 'deltaY'-related code elsewhere and uncomment it
	 *  the day we try again
	 *  
	 */ 
	handleMousewheel : function(ev){
		
		var dnd = jQuery.tableDnD;
		
		if (dnd.dragObject === null || dnd.currentTable === null) {
			return;
		}
		
		var delta = (ev.originalEvent.deltaY) ? ev.originalEvent.deltaY : (- ev.originalEvent.wheelDelta); 
		dnd.deltaY += delta;
		var y = dnd.mouseCoords(ev.originalEvent).y;
		var draggedRows = jQuery(dnd.dragObject);
	
		var insertRows = dnd.findDropTargetRow(draggedRows, y);

		if (insertRows) {
			draggedRows.insertBefore(insertRows);		
		}
		
		
	},

	/**
	 * We're only worried about the y position really, because we can only move
	 * rows up and down
	 */
	findDropTargetRow : function(draggedRow, y) {
		
		var rows = jQuery.tableDnD.currentTable.rows;
		for ( var i = 0; i < rows.length; i++) {
			var row = rows[i];
			
			var rowY = this.getPosition(row).y /*- jQuery.tableDnD.deltaY*/; 			
			var rowHeight = parseInt(row.offsetHeight,10) / 2;
			if (!row.offsetHeight) {
				rowY = this.getPosition(row.firstChild).y /*- jQuery.tableDnD.deltaY*/; 
				rowHeight = parseInt(row.firstChild.offsetHeight,10) / 2;
			}
			
			// Because we always have to insert before, we need to offset the
			// height a bit
			if ((y > rowY - rowHeight) && (y < (rowY + rowHeight))) {
				// that's the row we're over
				// If it's the same as the current row, ignore it
				if (row == draggedRow) {
					return null;
				}
				
				var config = jQuery.tableDnD.currentTable.tableDnDConfig;
				if (config.onAllowDrop) {
					if (config.onAllowDrop(draggedRow, row)) {
						return row;
					} else {
						return null;
					}
				} else {
					// If a row has nodrop class, then don't allow dropping
					// (inspired by John Tarr and Famic)
					var nodrop = jQuery(row).hasClass("nodrop");
					if (!nodrop) {
						return row;
					} else {
						return null;
					}
				}
				return row;
			}
		}
		return null;
	},

	/*
	 * Last bit of our modifications : when 
	 * releasing the button we must restore the DOM 
	 * and the 'nodrop' logic css class then execute 
	 * the initial code.
	 */
	mouseup : function(e) {
		
		// ADDED CUSTOM PART
		if (jQuery.tableDnD.dragObject) {
			jQuery.tableDnD.dragObject.removeClass('nodrop');
			document.body.style.cursor = "default";
		}
		// END ADDED CUSTOM PART
		
		if (jQuery.tableDnD.currentTable && jQuery.tableDnD.dragObject) {
			var droppedRow = jQuery.tableDnD.dragObject;
			var config = jQuery.tableDnD.currentTable.tableDnDConfig;
			// If we have a dragObject, then we need to release it,
			// The row will already have been moved to the right place so we
			// just reset stuff
			if (config.onDragClass) {
				jQuery(droppedRow).removeClass(config.onDragClass);
			} else {
				jQuery(droppedRow).css(config.onDropStyle);
			}
			jQuery.tableDnD.dragObject = null;
			if (config.onDrop) {
				// Call the onDrop method if there is one
				config.onDrop(jQuery.tableDnD.currentTable, droppedRow);
			}
			jQuery.tableDnD.currentTable = null; // let go of the table too
		}
	},

	serialize : function() {
		if (jQuery.tableDnD.currentTable) {
			return jQuery.tableDnD.serializeTable(jQuery.tableDnD.currentTable);
		} else {
			return "Error: No Table id set, you need to set an id on your table and every row";
		}
	},

	serializeTable : function(table) {
		var result = "";
		var tableId = table.id;
		var rows = table.rows;
		for ( var i = 0; i < rows.length; i++) {
			if (result.length > 0){
				result += "&";
			}
			var rowId = rows[i].id;
			if (rowId && table.tableDnDConfig
					&& table.tableDnDConfig.serializeRegexp) {
				rowId = rowId.match(table.tableDnDConfig.serializeRegexp)[0];
			}

			result += tableId + '[]=' + rowId;
		}
		return result;
	},

	serializeTables : function() {
		var result = "";
		this.each(function() {
			// this is now bound to each matching table
			result += jQuery.tableDnD.serializeTable(this);
		});
		return result;
	}

};

jQuery.fn.extend({
	tableDnD : jQuery.tableDnD.build,
	tableDnDUpdate : jQuery.tableDnD.updateTables,
	tableDnDSerialize : jQuery.tableDnD.serializeTables
});



});