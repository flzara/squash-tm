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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView"],
	function($, backbone, _, Handlebars, AbstractStepView) {
	"use strict";

	var axisStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#axis-step-tpl";
			this.model = data;
			data.name = "axis";
			this._initialize(data, wizrouter);
			this.reloadData();

		},
		
	
		
		reloadData : function() {
			
			var operations = this.model.get("operations");
			
			_.each(operations, function (op){
				
				$("#operations-operation-select-"+ op.column.id).val(op.operation); 
				
			});
			
		},
		
		
		updateModel : function() {
			
			var ids = _.pluck($(".operations-operation-select"), "name");
			
			var operations = this.getVals(ids);
			
			
			// #5761
			
			var axes = this.updateColumnsForRole('axis', operations);
			var measures = this.updateColumnsForRole('measures', operations);
			
			

			this.model.set({
				operations : operations,
				axis : axes,
				measures : measures
			}); 
			
		},
		
		getVals : function (ids) {
		
			var self = this;
			
			return _.map(ids, function(id){
				return {column : self.findColumnById(id),
					operation : $("#operations-operation-select-" + id).val() ,
				};
			});
			
			
		},
		
		findColumnById : function (id){
			return _.chain(this.model.get("computedColumnsPrototypes"))
			.values()
			.flatten()
			.find(function(col){return col.id == id; })
			.value();		
		},

		
		/* *************************************************
		 * 
		 * #5761 : must ensure that the content of attributes 'axis' and 'measures' are
		 * adjusted according to what operations the user applied to the columns
		 * 
		 * *************************************************/
		
		// 'role' should be 'axis' or 'measures'
		updateColumnsForRole : function(role, columns){
			var operations = this.model.get('columnRoles')[(role === 'axis') ? 'AXIS' : 'MEASURE'];
			
			// find which columns define an operation compatible with the role and copy them
			// with a default label
			var newCols = _.chain(columns)
							.filter(function(col){ return _.contains(operations , col.operation); })
							.map(function(col){ return _.extend({label : ""}, col); })
							.value();
			
			// get the old columns for that role
			var oldCols = this.model.get(role);
			
			// the updated columns for that role are the new columns, updated with label if any where defined prior to this 
			var updatedCols = _.chain(newCols)
								.each(function(col){
									var label = _.chain(oldCols).find(function(co){return co.column.id === col.column.id}).result('label').value() || "";
									col.label = label;
								})
								.value();
			
			return updatedCols;
		}
		
		
	});

	return axisStepView;

});