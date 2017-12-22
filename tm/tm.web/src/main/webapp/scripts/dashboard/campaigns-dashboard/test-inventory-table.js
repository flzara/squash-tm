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
define(["../basic-objects/table-view", "squash.translator", "squash.attributeparser"], 
		function(TableView, translator, attrparser){
	
	return TableView.extend({

		getData : function(){
			var inventory = this.model.get(this.modelAttribute);
			
			var data = [],
				i = 0,
				len = inventory.length;
			
			if (len > 0){
				var totals = [
					translator.get('dashboard.meta.labels.total'),	//name
					0, // ready
					0, // running
					0, // success
					0, // settled
					0, // failure
					0, // blocked
					0, // untestable
					0, // total
					"" // progress
				],
				total_nbterm = 0,
				total_total = 0;
				
				for (i=0;i<len;i++){
					
					// compute stats
					var m = inventory[i];
					var _nbterm = m.nbSuccess + m.nbSettled + m.nbFailure + m.nbBlocked + m.nbUntestable + m.nbWarning + m.nbError;
					var total = _nbterm + m.nbReady + m.nbRunning;
					var progress = (total>0) ? (_nbterm * 100 / total).toFixed(0) + ' %' : '0%';
					var rowdata = [
					              (m.iterationName || m.campaignName),	// sorry, too lazy to make it right -_-
					               m.nbReady,
					               m.nbRunning,
					               m.nbSuccess + m.nbWarning,
					               m.nbSettled,
					               m.nbFailure,
					               m.nbBlocked + m.nbError + m.nbNotRun,
					               m.nbUntestable,
					               total,
					               progress
					];
					data.push(rowdata);
					
					// save the totals
					total_nbterm += _nbterm;
					total_total += total;
					for (var j=1; j<9; j++){
						totals[j]+=rowdata[j];
					}
				}
				
				// finalize the totals and add them to the data
				totals[9] = (total_total>0) ? (total_nbterm * 100 / total_total).toFixed(0) + ' %' : '0%';
				data.push(totals);
			}
			
			return data;
		}
	});
	
});
