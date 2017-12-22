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
define(["jquery", "backbone", "squash.translator"], function($, Backbone, translator){

	return Backbone.View.extend({
		
		initialize : function(){
			
			var template = this.$el.text().split('|');

			this.hasItemsMsg = template[0];	
			this.zeroItemsMsg = template[1];		
			
			this.render();
			this.listenTo(this.model, 'change:boundTestCasesStatistics', this.render);
		},
		
		// dirty but effective way to know how many requirements we have here.
		render : function(){
			
			if (! this.model.isAvailable()){
				return;
			}
			
			var stats = this.model.get('boundTestCasesStatistics');
			var nbtc = stats.zeroTestCases + stats.oneTestCase + stats.manyTestCases;
		
			var ids = this.model.get('selectedIds');
				
			var search = {
					fields: {
						"requirement.id": {
							type:"LIST",
							values:""
						}	 
					}
			};
			
			search.fields.isCurrentVersion = {};
			search.fields.isCurrentVersion.type = "SINGLE";
			search.fields.isCurrentVersion.value = "1";
			search.fields.isCurrentVersion.ignoreBridge = "false";
				 
			search.fields["requirement.id"].values = ids.toString().split(",");
				    
			var queryString = "searchModel=" + encodeURIComponent(JSON.stringify(search));
			var urlSearch = squashtm.app.contextRoot + "/advanced-search/results?requirement&" + queryString;

			var todisplay;
			if (nbtc === 0) {
				todisplay = this.zeroItemsMsg;
			} else {
				todisplay = this.hasItemsMsg.replace('{placeholder}', '<span style="font-weight:bold;color:black;">'+nbtc+'</span>').replace('{details}', '<span><b><a href=' + urlSearch + '>'+ translator.get("dashboard.test-cases.detail")+ '</b></a></span>');
			
			}
			
			this.$el.html(todisplay);
		}
		
	});
});