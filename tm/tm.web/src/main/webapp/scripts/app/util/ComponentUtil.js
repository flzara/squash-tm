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
define([ "jquery" ], function($) {
	/**
	 * will update the icon of the combobox on change
	 */
	function updateStatusCboxIconOnChange(cbox) {
		cbox.bind("change", function() {
			// reset the classes
			updateStatusCboxIcon(cbox);
		});
	}
	function updateStatusCboxIcon(cbox){
		cbox.attr("class", "");

		cbox.addClass("execution-status-combo-class");

		// find and set the new class
		var selectedIndex = cbox.get(0).selectedIndex;
		var selector = "option:eq(" + selectedIndex + ")";

		var className = cbox.find(selector).attr("class");

		cbox.addClass(className);
	}
	return {
		updateStatusCboxIconOnChange : updateStatusCboxIconOnChange,
		updateStatusCboxIcon :updateStatusCboxIcon
	};
});