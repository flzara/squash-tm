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
define(["require", 'squash.translator', "./iteration-progression-view", "./nonexecuted-testcase-importance-pie", "./testcase-status-pie",
		"../basic-objects/success-rate-view", "./test-inventory-table", "dashboard/SuperMasterView"],
	function (require, translator, ProgressionPlot, ImportancePie, StatusPie, SuccessRateDonut, InventoryTable, SuperMasterView) {

		function doInit(settings) {

			new SuperMasterView({
				el: "#dashboard-master",
				modelSettings: settings,
				initCharts: initCharts
			});
		}

		function initCharts() {

			var res = [];


			var progress = new ProgressionPlot({
				el: this.$("#dashboard-cumulative-progression"),
				model: this.model
			});

			var impPie = new ImportancePie({
				el: this.$("#dashboard-nonexecuted-testcase-importance"),
				model: this.model
			});

			var statusPie = new StatusPie({
				el: this.$("#dashboard-testcase-status"),
				model: this.model
			});

			var succesDonut = new SuccessRateDonut({
				el: this.$("#dashboard-success-rate"),
				model: this.model,
				fetchStats: function (model) {
					return model.get("iterationTestCaseSuccessRateStatistics");
				}
			});

			var inventoryTable = new InventoryTable({
				el: this.$("#dashboard-test-inventory"),
				model: this.model
			});

			res.push(impPie);
			res.push(statusPie);
			res.push(succesDonut);
			res.push(inventoryTable);
			res.push(progress);

			addClickSearchEvent(impPie, "nonexecuted-testcase-importance");
			addClickSearchEvent(statusPie, "status");
			addClickSearchEvent(succesDonut, "succesDonut");

			return res;
		}

		function addClickSearchEvent(pie, type) {

			var item = $(pie.el);

			item.bind('jqplotDataHighlight', function (ev, seriesIndex, pointIndex, data) {
				var $this = $(this);
				$this.attr('title', translator.get("dashboard.test-cases.search"));
				//add pointer because IE don't support  zoom-in. Put pointer before zoom-in, so zoom-in is used if the brower support it
				$this.css('cursor', 'pointer');
				$this.css('cursor', 'zoom-in');

			});

			item.bind('jqplotDataUnhighlight', function (ev, seriesIndex, pointIndex, data) {
				var $this = $(this);
				$this.attr('title', "");
				$this.css('cursor', 'auto');
			});

			item.bind('jqplotDataClick', function (ev, seriesIndex, pointIndex, data) {

				//Special case for full pie.
				if (pie.getData().isFull) {
					pointIndex = pie.getData().nonzeroindex;
				}
				//Special case for empty pie.
				if (pie.getData().isEmpty) {
					return;
				}

				var id = pie.model.get('selectedId');
				var search = {fields: {}};

				search.fields['iteration.id'] = {type: 'LIST', values: [id]};

				switch (type) {
					case "nonexecuted-testcase-importance":
						addNonexcTcImportanceToSearch(search, pointIndex);
						break;


					case "status" :
						addStatusToSearch(search, pointIndex);
						break;

					case "succesDonut" :
						addSuccesDonutToSearch(search, seriesIndex, pointIndex);
						break;
				}

				var queryString = "searchModel=" + encodeURIComponent(JSON.stringify(search));
				document.location.href = squashtm.app.contextRoot + "/advanced-search/results?campaign&" + queryString;

			});
		}

		function addSuccesDonutToSearch(search, seriesIndex, pointIndex) {

			var importanceType = "LIST";
			var statusType = "LIST";

			var statusValues;
			var importanceValues;

			var importance = seriesIndex;
			var status = pointIndex;

			switch (importance) {
				case 0 :
					importanceValues = ["1-VERY_HIGH"];
					break;
				case 1 :
					importanceValues = ["2-HIGH"];
					break;
				case 2 :
					importanceValues = ["3-MEDIUM"];
					break;
				case 3 :
					importanceValues = ["4-LOW"];
					break;
			}


			switch (status) {

				case 0 :
					statusValues = ["3-SUCCESS", "4-WARNING", "10-SETTLED"];
					break;

				case 1 :
					statusValues = ["5-FAILURE", "7-ERROR"];
					break;

				case 2 :
					statusValues = ["6-BLOCKED", "8-NOT_RUN", "9-UNTESTABLE", "9-NOT_FOUND"];
					break;

			}

			search.fields['referencedTestCase.importance'] = {type: importanceType, values: importanceValues};
			search.fields['executionStatus'] = {type: statusType, values: statusValues};
		}

		function addStatusToSearch(search, pointIndex) {


			var type = "LIST";
			var values;

			switch (pointIndex) {
				case 0:
					values = ["1-READY"];
					break;
				case 1:
					values = ["2-RUNNING"];
					break;
				case 2:
					values = ["3-SUCCESS"];
					break;
				case 3:
					values = ["10-SETTLED"];
					break;

				case 4:
					values = ["5-FAILURE"];
					break;

				case 5:
					values = ["6-BLOCKED"];
					break;

				case 6:
					values = ["9-UNTESTABLE"];
					break;

			}

			search.fields['executionStatus'] = {type: type, values: values};

		}

		function addNonexcTcImportanceToSearch(search, pointIndex) {

			search.fields.executionStatus = {};
			search.fields.executionStatus.type = "LIST";
			search.fields.executionStatus.values = ["1-READY", "2-RUNNING"];

			var type = "LIST";
			var values;

			switch (pointIndex) {
				case 0:
					values = ["4-LOW"];
					break;
				case 1:
					values = ["3-MEDIUM"];
					break;
				case 2:
					values = ["2-HIGH"];
					break;
				case 3:
					values = ["1-VERY_HIGH"];
					break;
			}
			search.fields['referencedTestCase.importance'] = {type: type, values: values};
		}


		return {
			init: function (settings) {
				doInit(settings);
			}
		};

	});
