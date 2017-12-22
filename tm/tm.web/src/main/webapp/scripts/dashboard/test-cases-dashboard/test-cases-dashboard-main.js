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
		"dashboard/SuperMasterView", "./summary", "./bound-requirements-pie", "./status-pie", "./importance-pie",
		"./size-pie", "squash.translator" ], function(require, StatModel, Timestamp, SuperMasterView, Summary,
		BoundReqPie, StatusPie, ImportancePie, SizePie, translator) {

	function doInit(settings) {
		new SuperMasterView({
			el : "#dashboard-master",
			modelSettings : settings,
			initCharts : initCharts
		});
	}

	function initCharts() {
		var reqPie = new BoundReqPie({
			el : this.$("#dashboard-item-bound-reqs"),
			model : this.model
		});

		var statPie = new StatusPie({
			el : this.$("#dashboard-item-test-case-status"),
			model : this.model
		});
		var impPie = new ImportancePie({
			el : this.$("#dashboard-item-test-case-importance"),
			model : this.model
		});
		var sizePie = new SizePie({
			el : this.$("#dashboard-item-test-case-size"),
			model : this.model
		});
		var summary = new Summary({
			el : this.$(".dashboard-summary"),
			model : this.model
		});

		addClickSearchEvent($("#dashboard-item-bound-reqs"), reqPie, "requirement");
		addClickSearchEvent($("#dashboard-item-test-case-status"), statPie, "status");
		addClickSearchEvent($("#dashboard-item-test-case-importance"), impPie, "importance");
		addClickSearchEvent($("#dashboard-item-test-case-size"), sizePie, "size");
		summarySearch($(".dashboard-summary"), summary);
		return [ summary, reqPie, statPie, impPie, sizePie ];
	}

	function addRequirementsToSearch(search, pointIndex) {
		search.fields.requirements = {};
		search.fields.requirements.type = "RANGE";
		switch (pointIndex) {
		case 0:
			search.fields.requirements.minValue = "0";
			search.fields.requirements.maxValue = "0";
			break;
		case 1:
			search.fields.requirements.minValue = "1";
			search.fields.requirements.maxValue = "1";
			break;

		case 2:
			search.fields.requirements.minValue = "2";
			search.fields.requirements.maxValue = "";
			break;
		}
	}

	function addStatusToSearch(search, pointIndex) {
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
			search.fields.status.values = [ "5-TO_BE_UPDATED" ];
			break;
		case 4:
			search.fields.status.values = [ "4-OBSOLETE" ];
			break;
		}
	}

	function addImportanceToSearch(search, pointIndex) {
		search.fields.importance = {};
		search.fields.importance.type = "LIST";
		switch (pointIndex) {
		case 0:
			search.fields.importance.values = [ "4-LOW" ];
			break;
		case 1:
			search.fields.importance.values = [ "3-MEDIUM" ];
			break;
		case 2:
			search.fields.importance.values = [ "2-HIGH" ];
			break;
		case 3:
			search.fields.importance.values = [ "1-VERY_HIGH" ];
			break;
		}
	}

	function addSizeToSearch(search, pointIndex) {
		search.fields.steps = {};
		search.fields.steps.type = "RANGE";
		switch (pointIndex) {
		case 0:
			search.fields.steps.minValue = "0";
			search.fields.steps.maxValue = "0";
			break;
		case 1:
			search.fields.steps.minValue = "1";
			search.fields.steps.maxValue = "10";
			break;
		case 2:
			search.fields.steps.minValue = "11";
			search.fields.steps.maxValue = "20";
			break;
		case 3:
			search.fields.steps.minValue = "21";
			search.fields.steps.maxValue = "";
			break;
		}
	}

	function summarySearch(item, summary) {

		/*
		
		item.bind('click', function(){
			var ids = summary.model.get('selectedIds');
			
			 var search = {fields:{
				 id:{type:"LIST", values:"" }	 
			 }};
			 
			 search.fields.id.values = ids.toString().split(",");
			    
			var queryString = "searchModel=" + encodeURIComponent(JSON.stringify(search));
			 document.location.href = squashtm.app.contextRoot + "/advanced-search/results?test-case&" + queryString;
			
		});*/

	}
	function addClickSearchEvent(item, pie, type) {

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

		item.bind('jqplotDataClick', function(ev, seriesIndex, pointIndex, data) {
			var ids = pie.model.get('selectedIds');

			var search = {
				fields : {
					id : {
						type : "LIST",
						values : ""
					}
				}
			};

			search.fields.id.values = ids.toString().split(",");

			switch (type) {
			case "requirement":
				addRequirementsToSearch(search, pointIndex);
				break;
			case "status":
				addStatusToSearch(search, pointIndex);
				break;
			case "importance":
				addImportanceToSearch(search, pointIndex);
				break;
			case "size":
				addSizeToSearch(search, pointIndex);
				break;
			}

			var queryString = "searchModel=" + encodeURIComponent(JSON.stringify(search));
			document.location.href = squashtm.app.contextRoot + "/advanced-search/results?test-case&" + queryString;

		});
	}

	return {
		init : function(settings) {
			doInit(settings);
		}
	};

});