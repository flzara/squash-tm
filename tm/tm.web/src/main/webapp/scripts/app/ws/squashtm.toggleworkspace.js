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
/**
 * Controller for the ToggleWS workspace type page (report-workspace.html) Depends on : -
 * jquery
 */
define(["jquery", "workspace.sessionStorage"],
		function($, storage) {
			var settings = {
				expandSidebarLabel : " ",
				collapseSidebarLabel : " "
			};

			function setEditToggleWSNormalState() {
				$("#contextual-content").removeClass("expanded");
			}

			function setEditToggleWSExpandState() {
				$("#contextual-content").addClass("expanded");
			}

			function toggleEditToggleWSState() {
				$("#contextual-content").toggleClass("expanded");
			}

			function setLeftFrameNormalState() {
				$(".left-frame").removeClass("expanded");/*
				$("#toggle-expand-left-frame-button").attr("value",
						settings.collapseSidebarLabel);*/
				var pathname = window.location.pathname;
				storage.remove(pathname);
			}

			function setLeftFrameExpandState() {
				$(".left-frame").addClass("expanded");
			/*	$("#toggle-expand-left-frame-button").attr("value",
						settings.expandSidebarLabel);*/
				var pathname = window.location.pathname;
				storage.set(pathname, "leftexpanded");
			}

			function toggleLeftFrameState() {
				if ($(".left-frame").hasClass("expanded")) {
					setLeftFrameNormalState();
				} else {
					setLeftFrameExpandState();
				}
			}

			function toggleToggleWSWorkspaceState() {
				toggleLeftFrameState();
				toggleEditToggleWSState();
			}

			/**
			 * initializes the workspace. 
			 *
			 * @returns
			 */
			function init(options) {

				var defaults = settings;
				settings = $.extend(defaults, options);

				$("#contextual-content").delegate(
						"#toggle-expand-left-frame-button", "click",
						toggleToggleWSWorkspaceState);

				// settings.workspace is the pathname in every workspace html
					if(settings.workspace !== undefined) {
							var urlSession = storage.get(settings.workspace);
							if ( urlSession !== undefined ) {
								if ( urlSession == "leftexpanded" ) {								
									setLeftFrameExpandState();
									setEditToggleWSExpandState();								
								}
						}
				}
			}

			squashtm.toggleWorkspace = {
				setToggleWSWorkspaceNormalState : function() {
					setLeftFrameNormalState();
					setEditToggleWSNormalState();
				},
 
				setToggleWSWorkspaceExpandState : function() {
					setLeftFrameExpandState();
					setEditToggleWSExpandState();
				},
				 
				init : init
			};

			return squashtm.toggleWorkspace;
		});
