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
define(['jquery', 'workspace.contextual-content', 'workspace.routing'],
	function ($, ctxcontent, urlBuilder) {


		function _initTabs() {
			var url = $(location).attr("href");
			if (url.indexOf("#validate") != -1) {
				addSelectedTabClass("#tf-validate-tab a");
			} else if (url.indexOf("#global") != -1) {
				addSelectedTabClass("#tf-global-tab a");
			} else {
				addSelectedTabClass("#tf-transmitted-tab a");
			}

			$("#tf-automation-tabs").find("a").on("click", function () {
				var model = squashtm.app;
				var url = model.contextRoot;
				var requestStatus = [];
				var href = $(this).attr("href");
				if (href === "#transmitted") {
					requestStatus = ["READY_TO_TRANSMIT"];
				} else if (href === "#validate") {
					requestStatus = ["WORK_IN_PROGRESS"];
				} else {
					requestStatus = ["AUTOMATION_IN_PROGRESS", "READY_TO_TRANSMIT", "TRANSMITTED", "WORK_IN_PROGRESS", "AUTOMATED", "SUSPENDED", "REJECTED"];
					url = url + "automation-workspace/testers/";
				}
				var self = this;
				$.ajax({
					url: model.contextRoot + "automation-tester-workspace/lastModifiedBy/" + requestStatus,
					method: "GET",
				}).success(function (data) {
					switch (href) {
						case "#transmitted":
							model.testerTransmitted = data;
							console.log(model.testerTransmitted)
							break;
						case "#validate":
							model.testerValidate = data;
							break;
						case "#global":
							model.testerGlobalView = data;
							break;
						default:
							break;

					}					
				});
				if (!$(self).hasClass('tf-selected')) {
					selectTab(self);
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
			init: init
		};
	}
)
