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

define([ "jquery", "./ProjectFilterPopup" ],
		function($, ProjectFilterPopup) {

			var popupSelector = "#project-filter-popup";
			var popupOpener = "#menu-project-filter-link";

			function init() {
				var projectFilterPopup = new ProjectFilterPopup({el :"#project-filter-popup"});

				$(popupOpener).click(function() {
					projectFilterPopup.open();
				});

				$("#menu-toggle-filter-ckbox").click(function(){

					function postStatus(enabled){
						$.post(squashtm.app.contextRoot+'/global-filter/filter-status', { isEnabled : enabled })
						.done(function(){
							window.location.reload();
						});
					}

					if ($(this).is(':checked')){
						postStatus(true);
					}
					else{
						postStatus(false);
					}
				});
			}


			/**
			 * public module
			 */
			return  {
				init : init
			};


		});
