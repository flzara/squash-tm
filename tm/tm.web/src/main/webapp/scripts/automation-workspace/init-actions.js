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
define(['jquery', 'workspace.contextual-content'/*, 'squash.translator'*/, 'workspace.routing'],
 function($, ctxcontent/*, translator*/, urlBuilder) {


		function _initTabs() {
			var url = $(location).attr("href");
			if (url.indexOf("#traitment") != -1) {
				addSelectClass("#traitment-tab a");
			} else if (url.indexOf("#global") != -1) {
				addSelectClass("#global-tab a");
			} else {
				addSelectedTabClass("#affected-tab a");
			}

			$("#tf-affected-tabs").find("a").on("click", function() {
				if (! $(this).hasClass('tf-selected')) {
					selectTab(this);
				}
			});
		}
		function selectTab(elt) {
			var elts = $(elt).parent().parent().children();
			var i;

			for (i = 0; i < elts.length; i++) {
				$(elts[i]).find("a").removeClass("tf-selected");
			}

			addSelectedTabClass(elt);
		}
		function addSelectedTabClass(elt) {
			$(elt).addClass("tf-selected");
		}


		function init() {
			_initTabs();
		}

		return {
			init:init
		};
 }
)
