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
 * This will transform a <form> element into a multiple file upload.
 * 
 * It works works like a repeater, appending or removing lists of Items.
 * 
 * An Item is the following : 
 *	- a file browser,
 *	- a button that will remove it when clicked.
 * 
 * The form will always propose an empty Item for new inputs. When an item is not empty anymore, a new 
 * empty Item will be appended to the popup.
 * 
 * 
 * @author bsiri
 */


define(["jquery", "jqueryui", "jform", "jquery.generateId"], function($){
	if (!! $.fn.multiFileupload){
		return;
	}
	
	/*
	 * Here is an object wrapper for the JQuery version of an Item
	 * 
	 */

	$.fn.uploadItem = function(jqContainer) {

		// init
		this.getFile = getFile;
		this.getButton = getButton;
		this.initialize = item_initialize;
		this.container = jqContainer;

		// construction
		this.initialize();

		return this;

	};

	function item_initialize() {
		var myself = this;

		var myfile = this.getFile();
		myfile.change(function() {
			inputFileOnChange(myself);
		});

		var mybutton = this.getButton();

		$(mybutton).squashButton();

		mybutton.click(function() {
			myself.container.removeItem(myself);
		});

	}

	// returns the File Browser part of an Item
	function getFile() {
		return this.find("input[type='file']");
	}

	// returns the remove Button part of an Item
	function getButton() {
		return this.find("input[type='button']");
	}

	// this handler will manage the state of an Item.
	// params : jqItem : the jQuery object corresponding to the Item
	function inputFileOnChange(jqItem) {
		var wasEmpty = jqItem.getFile().data("wasEmpty");

		if (typeof (wasEmpty) == 'undefined') {
			jqItem.container.appendItem();
		}

		jqItem.getFile().data("wasEmpty", false);

	}
	
	// returns the last item of the list of files to upload
	function findLastItem() {
		var last = this.find(".attachment-item:last");
		return last;
	}

	// the function below will remove an Item only if at least one more is
	// available
	function removeItem(jqAttachItem) {
		// check if the item argument is actually different from the last one
		var lastItem = this.findLastItem();

		if ((isFileBrowserEmpty(lastItem)) && (!attachementAreSame(jqAttachItem, lastItem))) {
			jqAttachItem.remove();
		}
	}

	function appendItem() {
		var clone = this.itemTemplate.clone(true).generateId();
		var jqClone = clone.uploadItem(this);
		this.append(jqClone);
	}

	function attachementClear() {
		this.empty();
		this.appendItem();
	}

	/* ************** utility code ************** */

	function isFileBrowserEmpty(jqItem) {
		var content = jqItem.find("input [type='file']").val();

		if ((!content) || (typeof (content) == 'undefined')) {
			return true;
		} else {
			return false;
		}
	}

	function attachementAreSame(obj1, obj2) {
		if (obj1.attr('id') == obj2.attr('id')) {
			return true;
		} else {
			return false;
		}
	}
	

	/*
	 * the container is an object too. It job is to manage the various Items it contains.
	 * 
	 * 
	 */

	$.fn.multiFileupload = function(jqItemTemplate) {

		// init
		if (jqItemTemplate != 'undefined') {
			this.itemTemplate = jqItemTemplate.clone();
		}

		this.findLastItem = findLastItem;
		this.removeItem = removeItem;
		this.clear = attachementClear;
		this.appendItem = appendItem;

		return this;
	};


	
	return $.fn.multiFileupload;
});