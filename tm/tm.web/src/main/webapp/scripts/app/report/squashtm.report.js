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
 * Controller for the report panel
 *
 * depends on : jquery jquery ui jquery.jeditable.js jquery.jeditable.datepicker.js jquery.squash.plugin.js
 * jquery.squash.linkabletree.js squash.reportworkspace.js
 *
 * @author Gregory Fouquet
 */
define([ "jquery", "app/report/squashtm.reportworkspace", "tree", "underscore", "./ProjectsPickerPopup", "./SingleProjectPickerPopup","app/util/ButtonUtil", "./ReportInformationPanel", "./ReportCriteriaPanel", "./ConciseFormModel",
		"is","workspace.routing", "jqueryui", "jeditable", "jeditable.datepicker", "jquery.squash", "jquery.cookie", "datepicker/jquery.squash.datepicker-locales", "jquery.squash.jeditable"],
	function($, RWS, treebuilder, _, ProjectsPickerPopup, SingleProjectPickerPopup, ButtonUtil, ReportInformationPanel, ReportCriteriaPanel, FormModel,
					 is, router) {
	"use strict";

	var config = {
		contextPath : "",
		dateFormat : "dd/mm/yy",
		noDateLabel : "-",
		okLabel : "OK",
		cancelLabel : "Cancel"
	};

	var formState = {
		restore: function() {
			var stored = sessionStorage[config.reportUrl + "-formerState"];
			var reportDef = config.reportDef;
			if(reportDef !== null & reportDef !== undefined){

				var currentNamespace = config.reportUrl.split(/[/]+/).pop();
				var storedNamespace = JSON.parse(reportDef).pluginNamespace;

				if(currentNamespace.localeCompare(storedNamespace) === 0){

					var parmeters = JSON.parse(reportDef).parameters;
					return  JSON.parse(parmeters);

				}
			}
			if (!!stored) {
				return JSON.parse(stored);
			}
			return {};
		},

		save: function() {
			sessionStorage[config.reportUrl + "-formerState"] = stringModel();
		}
	};

	var selectedTab = false;
	var formModel, criteriaPanel, reportInfomationPanel;

	function resetState() {
		selectedTab = false;
	}

	function stringModel() {
		return JSON.stringify(formModel.toJSON());
	}

	function buildViewUrl(index, format) {
		// see [Issue 1205] for why "document.location.protocol"
		return document.location.protocol + "//" + document.location.host + config.reportUrl + "/views/" + index +
				"/formats/" + format;
	}

	function loadTab(tab) {
		var url = config.isDocx ? buildViewUrl(0, "docx") : buildViewUrl(tab.newTab.index(), "html");

		$.ajax({
			type : "get",
			url : url,
			dataType : "html",
			data : { json : stringModel() }
		}).done(function(html) {
			tab.newPanel.html(html);
		});
	}

	function buildPerimeterCheckerByControl(selectedPicker) {
		function hasProjectPicked(pp) {
			return undefined !== _.find(pp, function(element) {
				return element.selected === true;
			});
		}

		function hasEverythingSelected(rg) {
			return undefined !== _.find(rg, function(element) {
				return element.selected === true && element.value == "EVERYTHING";
			});
		}

		function hasNodePicked(tp) {
			return undefined !== _.find(tp, function(element) {
				return !!element.value;
			});
		}



		var checkerByControl = {
				"PROJECT_PICKER" : hasProjectPicked,
				"RADIO_BUTTONS_GROUP" : hasEverythingSelected,
				"TREE_PICKER" : hasNodePicked
		};

		var res = checkerByControl;

		if (!!selectedPicker) {
			var actualChecker = checkerByControl[selectedPicker];

			if (!!actualChecker) {
				res = {};
				res[selectedPicker] = actualChecker;
			}
		} // when selectedPicker is gibberish, we ignore it

		return res;
	}

	function generateView() {


		if (formModel.hasBoundary()) {
			formState.save();

			// collapses the form
			$("#report-criteria-panel.expand .tg-head").click();

			var tabPanel = $("#view-tabed-panel");

			if (!selectedTab) {
				tabPanel.tabs("option", "active", 0);
				// tab is inited, we dont need collapsible anymore,
				// otherwise click on active tab will trigger an event
				tabPanel.tabs("option", "collapsible", false);
			} else {
				loadTab(selectedTab);
			}

			$("#view-tabed-panel:hidden").show("blind", {}, 500);

		} else {
			var invalidPerimeterDialog = $("#invalid-perimeter").messageDialog();
			invalidPerimeterDialog.messageDialog("open");
		}

	}

	/*jshint validthis: true */
	function onViewTabSelected(event, ui) {
		/*jshint validthis: true */
		selectedTab = ui;
		var tabs = $(this); // js hint shit bricks here in strict mode because "this" is possibly not bound
		tabs.find(".view-format-cmb").addClass("not-displayed");
		tabs.find("#view-format-cmb-" + ui.newTab.index()).removeClass("not-displayed");

		loadTab(ui);
	}

	function doExport() {
		var viewIndex = selectedTab.newTab.index();
		var format = $("#view-format-cmb-" + viewIndex).val();

		var url = buildViewUrl(viewIndex, format);
		var data = stringModel();

		/* Issue #6752: One must encode datas before writing it in the URL, in case of special characters. */
		var encodedData = encodeURIComponent(data);

		window.open(url+"?json="+encodedData, "_blank", "resizable=yes, scrollbars=yes");
	}

	function initViewTabs() {
		$("#view-tabed-panel").tabs({
			active : false,
			collapsible : true, // we need collapsible for first init of
			// first tab
			activate : onViewTabSelected
		});
	}

	function init(settings) {
		resetState();
		config = $.extend(config, settings);

		formModel = new FormModel();
		criteriaPanel = new ReportCriteriaPanel({el: "#report-criteria-panel", model: formModel }, { formerState: formState.restore(), config: config });
		reportInfomationPanel = new ReportInformationPanel({el: "#report-information-panel", model: formModel }, config);

		initViewTabs();

		$("#generate-view").click(generateView); // perfect world -> in ReportCritPanel
		$("#export").click(doExport);

		$("#generate-report-button").click(generateView);

	}

	squashtm.report = {
		init : init
	};

	return squashtm.report;
});
