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
define(['jquery', 'workspace.contextual-content', 'workspace.routing', "jquery.squash.formdialog"],
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
					requestStatus = ["TRANSMITTED", "AUTOMATION_IN_PROGRESS", "SUSPENDED", "REJECTED", "AUTOMATED", "READY_TO_TRANSMIT", "WORK_IN_PROGRESS"];
					url = url + "automation-workspace/testers/";
				}
				var self = this;
				$.ajax({
					url: model.contextRoot + "automation-tester-workspace/lastModifiedBy/" + requestStatus,
					method: "GET"
				}).success(function (data) {
					switch (href) {
						case "#transmitted":
							model.testerTransmitted = data;
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


		function showWorkflow() {
			$("#icon-workflow").on("click", function () {
				var url = squashtm.app.contextRoot;
				if ($("#autom-image").length === 0) {
					if (squashtm.app.locale === "fr") {
						$("#workflow-img").append("<img id='autom-image' src='" + url+ "images/workflow_autom_fr.png' style='width:100%'/>");
					} else if (squashtm.app.locale === "de") {
						$("#workflow-img").append("<img id='autom-image' src='" + url+ "images/workflow_autom_de.png' style='width:100%'/>");
					} else {
						$("#workflow-img").append("<img id='autom-image' src='" + url+ "images/workflow_autom_en.png' style='width:100%'/>");
					}
				}
				var dialog = $("#workflow-popup").formDialog();
				dialog.formDialog("open");
			});

			$("#close-automation-workflow").on("click", function () {
				var dialog = $("#workflow-popup").formDialog();
				dialog.formDialog("close");
			});
		}

		function init() {
			_initTabs();
			showWorkflow();
		}


		return {
			init: init
		};
	}
);
