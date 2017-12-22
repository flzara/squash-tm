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
 *	settings : {
 *		master : a css selector that identifies the whole section that need initialization,
 *		workspace : one of "test-case", "campaign", "requirement" (can be read from dom)
 *		rendering : one of "toggle-panel", "plain". This is a hint that tells how to render the dashboard container (can be read from dom),
 *		model : a javascript object, workspace-dependent, containing the data that will be plotted (optional, may be undefined)
 *		cacheKey : if defined, will use the model cache using the specified key.
 *		listeTree : if true, the model will listen to tree selection.
 *	}
 * 
 */

define([ "require", "dashboard/basic-objects/model", "dashboard/basic-objects/timestamp-label",
		"dashboard/SuperMasterView","./summary", "./bound-test-cases-pie", "./status-pie", "./criticality-pie",
		"./bound-description-pie", "./coverage-donut", "./validation-donut", "squash.translator" ], function(require, StatModel, Timestamp, SuperMasterView, Summary,
		BoundTestCasePie, StatusPie, CriticalityPie, BoundDescriptionPie, CoverageDonut, ValidationDonut, translator) {

	function doInit(settings) {
		new SuperMasterView({
			el : "#dashboard-master",
			modelSettings : settings,
			initCharts : initCharts
		});
	}

	function initCharts() {
		
		var tcPie = new BoundTestCasePie({
			el : this.$("#dashboard-item-bound-tcs"),
			model : this.model
		});
		
		var statPie = new StatusPie({
			el : this.$("#dashboard-item-requirements-status"),
			model : this.model
		});
		
		var critPie = new CriticalityPie({
			el : this.$("#dashboard-item-requirements-criticality"),
			model : this.model
		});
		
		var descPie = new BoundDescriptionPie({
			el : this.$("#dashboard-item-bound-desc"),
			model : this.model
		});
				
		var covDonut = new CoverageDonut({
			el : this.$("#dashboard-item-coverage"),
			model : this.model,
			fetchStats: function(model) {
				return model.get("coverageStatistics");
			}
		});
		
		var validDonut = new ValidationDonut({
			el : this.$("#dashboard-item-validation"),
			model : this.model,
			fetchStats: function(model) {
				return model.get("validationStatistics");
			}
		});
		
		var summary = new Summary({
			el : this.$(".dashboard-summary"),
			model : this.model
		});

		addClickSearchEvent($("#dashboard-item-bound-tcs"), tcPie, "testcase");
		addClickSearchEvent($("#dashboard-item-requirements-status"), statPie, "status");
		addClickSearchEvent($("#dashboard-item-requirements-criticality"), critPie, "criticality");
		addClickSearchEvent($("#dashboard-item-bound-desc"), descPie, "description");
		addClickSearchEvent($("#dashboard-item-coverage"), covDonut, "coverage");
		addValidationToSearch($("#dashboard-item-validation"), validDonut);
		
		return [ summary, tcPie, statPie, critPie, descPie, covDonut, validDonut ];
	}
	
	function _initializeSearch() {
		var search = {
				fields : {
					"requirement.id" : {
						type : "LIST",
						values : "",
						ignoreBridge : "false"
					}
				}
			};
			
			search.fields.isCurrentVersion = {};
			search.fields.isCurrentVersion.type = "SINGLE";
			search.fields.isCurrentVersion.value = "1";
			search.fields.isCurrentVersion.ignoreBridge = "false";

			return search;
	}

	function _bindHighlightUnhighlight(item) {

		item.bind('jqplotDataHighlight', function(ev, seriesIndex, pointIndex, data) {
			var $this = $(this);
			$this.attr('title', translator.get("dashboard.test-cases.search"));
			//add pointer because IE don't support  zoom-in. Put pointer before zoom-in, so zoom-in is used if the brower support it
			$this.css('cursor', 'pointer');
			$this.css('cursor', 'zoom-in');
		});

		item.bind('jqplotDataUnhighlight', function(ev, seriesIndex, pointIndex, data) {
			var $this = $(this);
			$this.attr('title', "");
			$this.css('cursor', 'auto');
		});
	}

	function addClickSearchEvent(item, pie, type) {
				
		_bindHighlightUnhighlight(item);

		item.bind('jqplotDataClick', function(ev, seriesIndex, pointIndex, data) {
			/* Since the dashboard has only pies and donuts, they both implements getData() function. 
			One can add an existence check if needed (if another chart type is added for example). */
			if(pie.getData()[0].length > 1) {
				// If it is a donut
				// Special case for empty circle in the donut.
				if(pie.getData()[seriesIndex].isEmpty) {
					return;
				}
			} else {
				// If it is a pie
				// Special case for full pie.
				if (pie.getData().isFull) {
					pointIndex = pie.getData().nonzeroindex;
				}
				// Special case for empty pie.
				if (pie.getData().isEmpty){
					return;
				}
			}			

			var ids = pie.model.get('selectedIds');

			var search = _initializeSearch();
			
			search.fields["requirement.id"].values = ids.toString().split(",");

			switch (type) {
			case "testcase":
				addTestCasesToSearch(search, pointIndex, pie);
				break;
			case "status":
				addStatusToSearch(search, pointIndex, pie);
				break;
			case "criticality":
				addCriticalityToSearch(search, pointIndex, pie);
				break;
			case "description":
				addDescriptionToSearch(search, pointIndex, pie);
				break;
			case "coverage":
				addCoverageToSearch(search, pointIndex, seriesIndex, pie);
				break;
			}

			var queryString = "searchModel=" + encodeURIComponent(JSON.stringify(search));
			document.location.href = squashtm.app.contextRoot + "/advanced-search/results?requirement&" + queryString;

		});
	}

	function addTestCasesToSearch(search, pointIndex, pie) {
		search.fields.testcases = {};
		search.fields.testcases.type = "RANGE";
		
		switch (pointIndex) {
			case 0:
				search.fields.testcases.minValue = "0";
				search.fields.testcases.maxValue = "0";
				break;
			case 1:
				search.fields.testcases.minValue = "1";
				search.fields.testcases.maxValue = "1";
				break;

			case 2:
				search.fields.testcases.minValue = "2";
				search.fields.testcases.maxValue = "";
				break;
		}
	}
	
	function addStatusToSearch(search, pointIndex, pie) {
		search.fields.status = {};
		search.fields.status.type = "LIST";

		switch (pointIndex) {
			case 0:
				search.fields.status.values = [ "1-WORK_IN_PROGRESS" ];
				break;
			case 1:
				search.fields.status.values = [ "2-UNDER_REVIEW" ];
				break;
			case 2:
				search.fields.status.values = [ "3-APPROVED" ];
				break;
			case 3:
				search.fields.status.values = [ "4-OBSOLETE" ];
				break;
			}		
	}
	
	function addCriticalityToSearch(search, pointIndex, pie) {
		search.fields.criticality = {};
		search.fields.criticality.type = "LIST";

		switch (pointIndex) {
			case 0:
				search.fields.criticality.values = [ "3-UNDEFINED" ];
				break;
			case 1:
				search.fields.criticality.values = [ "2-MINOR" ];
				break;
			case 2:
				search.fields.criticality.values = [ "1-MAJOR" ];
				break;
			case 3:
				search.fields.criticality.values = [ "0-CRITICAL" ];
				break;
		}
	}
	
	function addDescriptionToSearch(search, pointIndex, pie) {
		search.fields.hasDescription = {};
		search.fields.hasDescription.type = "RANGE";
		
		switch (pointIndex) {
			case 0:
				search.fields.hasDescription.minValue = "";
				search.fields.hasDescription.maxValue = "0";
				break;
			case 1:
				search.fields.hasDescription.minValue = "1";
				search.fields.hasDescription.maxValue = "";
				break;
		}
	}

	function addCoverageToSearch(search, pointIndex, seriesIndex, chart) {
		
		search.fields.criticality = {};
		search.fields.criticality.type = "LIST";
		
		switch(seriesIndex) {
			case 0:
				search.fields.criticality.values = ["0-CRITICAL"];
			break;
			case 1:
				search.fields.criticality.values = ["1-MAJOR"];
			break;
			case 2:
				search.fields.criticality.values = ["2-MINOR"];
			break;
			case 3:
				search.fields.criticality.values = ["3-UNDEFINED"];
			break;
		}

		search.fields.testcases = {};
		search.fields.testcases.type = "RANGE";

		switch(pointIndex) {
			case 0:
				search.fields.testcases.minValue = "1";
				search.fields.testcases.maxValue = "";
			break;
			case 1:
				search.fields.testcases.minValue = "0";
				search.fields.testcases.maxValue = "0";
			break;
		}

		
	}
	
	function addValidationToSearch(item, chart) {
		
		_bindHighlightUnhighlight(item);
		
		item.bind('jqplotDataClick', function(ev, seriesIndex, pointIndex, data) {
			
			if(chart.getData()[seriesIndex].isEmpty) {
				return;
			}

			var ids = chart.model.get('selectedIds');
			
			var criticality;
			switch(seriesIndex) {
			case 0:
				criticality = "CRITICAL";
				break;
			case 1:
				criticality = "MAJOR";
				break;
			case 2:
				criticality = "MINOR";
				break;
			case 3:
				criticality = "UNDEFINED";
				break;
			}
			
			// validation must be an array to perform the split() for the ajax request
			var validation;
			switch(pointIndex) {
			case 0:
				validation = ["SUCCESS"];
				break;
			case 1:
				validation = ["FAILURE"];
				break;
			default:
				validation = ["READY", "RUNNING", "UNTESTABLE", "BLOCKED", "NOT_FOUND", "NOT_RUN", "ERROR", "WARNING", "SETTLED"];
			}
			
			var search = _initializeSearch();
			
			$.ajax({
				url: squashtm.app.contextRoot + "/requirement-browser/validation-statistics",
				type: "POST",
				dataType: "json",
				data : { 
					'selectedIds' : ids.join(","),
					'criticality' : criticality,
					'validation' : validation.join(",")
				}
			}).success(function(requirementIdsFromValidation) {
				search.fields["requirement.id"].values = requirementIdsFromValidation.toString().split(",");
				var queryString = "searchModel=" + encodeURIComponent(JSON.stringify(search));
				document.location.href = squashtm.app.contextRoot + "/advanced-search/results?requirement&" + queryString;
			});

		});
	}
	
	return {
		init : function(settings) {
			doInit(settings);
		}
	};

});