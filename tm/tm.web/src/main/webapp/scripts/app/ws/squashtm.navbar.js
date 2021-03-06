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
var squashtm = squashtm || {};

define([ "jquery", "jquery.squash.buttonmenu" ], function($) {
	squashtm.navbar = {
		init : function() {
			var linkName = $("#navigation").data("highlight");
			$("#"+linkName+"-link").addClass('navigation-selected');
			$("#bugtracker-link").buttonmenu({display:"block"});
			$("#automation-link-ul").buttonmenu({display:"block"});
			$("#automation-link-ul").on("click", function() {
				var ul = $("#bugtracker-link").buttonmenu("close");

			});
			$("#bugtracker-link").on("click", function() {
				var ul = $("#automation-link-ul").buttonmenu("close");
			});
			var bugTrackerList = $("#bugtracker-link").next('ul');
			if (bugTrackerList.children().length > 7){
				bugTrackerList.css("overflow-y", "scroll");
			}
		}
	};

	return squashtm.navbar;
});
