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
 * That page doesn't have the whole init code for templates/execute-execution-preview.html.
 * Some code was left behind, do that when time 
 */

require(["common"], function () {
	require(["jquery", "squash.basicwidgets", "custom-field-values","workspace.routing", "jquery.squash"],
		function ($, basic, cfieldVal,routing) {
			var squashtm = window.squashtm;
			var page = window.squashtm.page;
			var conf = squashtm.page.config;

			var clickHandlers = {
				stop: function () {
					if (conf.optimized) {
						parent.squashtm.ieomanager.closeWindow();
					} else {
						window.close();
					}
				},

				begin: function (event) {
					if (conf.optimized) {
						event.preventDefault();
						parent.squashtm.ieomanager.navigateNext();
						return false;
					} else {
						// nothing special
					}
				},

				links: function (event) {
					if (conf.optimized) {
						event.preventDefault();
						var url = $(this).attr('href');
						parent.squashtm.ieomanager.fillRightPane(url);
						return false;
					} else {
						// nothing special
					}
				}
			};

			var $doc = $(document);
			$doc.on("click", "#execute-stop-button", clickHandlers.stop);
			$doc.on("click", "#execute-begin-button", clickHandlers.begin);

			// when OER
			$doc.on("click", ".load-links-right-frame a", clickHandlers.links);

			// issue #2069
			$.noBackspaceNavigation();

			$doc.on("click", "#edit-tc", function () {
				var currentUrl = window.location.href;
				var myRegexp = /optimized=([a-zA-Z]*)/;
				var match = myRegexp.exec(currentUrl);
				var optimized = match[1];
				var winDef = {};
				var url = squashtm.app.contextRoot + "/test-cases/" + page.refTestCaseId + "/edit-from-exec/" + page.executionId + "?optimized=" + optimized;
				window.open(url);
				window.close();
				parent.squashtm.ieomanager.closeWindow();
			});

			$(function () {
				basic.init();

				var selector = "#test-case-attribute-panel > div.display-table";

				if (squashtm.page.hasFields) {
					$.getJSON(page.fieldsUrl)
						.success(function (jsonCufs) {
							cfieldVal.infoSupport.init(selector, jsonCufs, "jeditable");
						});
				}

				if (squashtm.page.hasDenormFields) {
					$.getJSON(page.denormsUrl).success(function (jsonDenorm) {
						cfieldVal.infoSupport.init(selector, jsonDenorm, "jeditable");
					});
				}

			});
		});
});
