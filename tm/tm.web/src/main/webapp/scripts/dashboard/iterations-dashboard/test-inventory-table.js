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
define([ "../basic-objects/table-view", "squash.translator" ], function(TableView, translator) {

	return TableView.extend({

		render : function() {

			if (!this.model.isAvailable()) {
				return;
			}

			var body = this.$el.find('tbody');
			body.empty();

			var data = this.getData();

			if (data.length === 0) {
				body.append(this.emptyrowTemplate.clone());
			} else {
				var i = 0, len = data.length;
				for (i = 0; i < len; i++) {
					var r = this.datarowTemplate(data[i]);
					body.append(r);
				}
				this.applySpecificStyle();
			}
		},
		applySpecificStyle : function() {
			var table = $("#test-suite-statistics");
			table.find("tr:last").addClass("iteration-dashbord-total-ligne");			
		},
		getData : function() {

			var inventory = this.model.get('testsuiteTestInventoryStatisticsList');

			var data = [], i = 0, len = inventory.length;

			if (len > 0) {
				var totals = [ translator.get('dashboard.meta.labels.total'), 0, // total
				0, // to execute
				0, // executed
				0, // ready
				0, // running
				0, // success
				0, // settled
				0, // failure
				0, // blocked
				0, // untestable
				0, // progress
				0, // pc success
				0, // pc failure
				0, // pc prev
				0, // very high
				0, // high
				0, // medium
				0 // low
				];

				for (i = 0; i < len; i++) {
					var m = inventory[i];
					var rowdata = [ m.testsuiteName, m.nbTotal, m.nbToExecute, m.nbExecuted, m.nbReady, m.nbRunning,
							m.nbSuccess, m.nbSettled, m.nbFailure, m.nbBlocked, m.nbUntestable, m.pcProgress,
							m.pcSuccess, m.pcFailure, m.pcPrevProgress, m.nbVeryHigh, m.nbHigh,
							m.nbMedium, m.nbLow ];
					data.push(rowdata);

					// update th totals
					for (var j = 1; j < 20; j++) {
						totals[j] += rowdata[j];
					}

				}

				// finalize the totals and add them to the data, mostly the percentages
				var total = totals[1];
				var totalExecuted = totals[3];
				var totalSucces = totals[6] + totals[7];

				totals[11] = (total > 0) ? ((totalExecuted / total) * 100).toFixed(0) : 0.0;
				totals[12] = (totalExecuted > 0) ? ((totalSucces / totalExecuted) * 100).toFixed(0) : 0.0;
				totals[13] = (totalExecuted > 0) ? ((totals[8] / totalExecuted) * 100).toFixed(0) : 0.0;
				totals[14] = (total > 0) ? (totals[14] / i).toFixed(0) : 0.0;

				data.push(totals);
			}

			return data;
		}
	});

});
