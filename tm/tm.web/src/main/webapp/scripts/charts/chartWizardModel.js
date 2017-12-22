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
define([ "jquery", "backbone", "underscore","app/util/StringUtil"], function($, Backbone, _,stringUtil) {
	"use strict";

return Backbone.Model.extend({

	initialize : function(data){

		var self = this;
		var chartDef = data.chartDef;
		this.set("projects",squashtm.workspace.projects);
		//settings the entityType who can have cuf binded in squashtm 1.15
		this.set("entityWithCuf",["REQUIREMENT_VERSION","TEST_CASE","CAMPAIGN","ITERATION","EXECUTION"]);

		if (chartDef !== null){
			this.set({
				updateId : chartDef.id,
				name : chartDef.name,
				type : chartDef.type,
				axis: chartDef.axis,
				owner : chartDef.owner,
				scope : _.map(chartDef.scope, function(val){ val.type = val.type.replace("LIBRARY", "LIBRARIE");return val;}),
				projectsScope : self.getProjectScope(chartDef.projectScope),
				scopeEntity : self.getScopeEntity(chartDef.scope),
				scopeType : chartDef.scopeType,
				measures : chartDef.measures,
				operations : self.getOperations(chartDef),
				filters : self.getFilters(chartDef),
				selectedEntity : self.getSelectedEntities(chartDef),
				selectedAttributes : self.getSelectedAttributes(chartDef),
				filtered : [true]
			});
		} else {
			this.set({scopeType : "DEFAULT"});
		}
	},

	getOperations : function (chartDef){
		return _.chain(chartDef)
		.pick('measures', 'axis')
		.values()
		.flatten()
		.map(function(val){return _(val).pick('column', 'operation');})
		.value();
	},

	getScopeEntity : function (scope){

		var val = _.chain(scope)
		.first()
		.result("type")
		.value();

		val = val.split("_")[0];

		if (val == "PROJECT") {
			val = "default";
		} else if (val == "TEST"){
			val = "TEST_CASE";
		}

		return val;

	},

	getFilters : function (chartDef){
		return _.chain(chartDef.filters)
		.map(function(filter) {
			switch(filter.operation){
				case "BETWEEN":
					break;
				default : 
					filter.values = [filter.values] ; 
			}
			return filter;
		})
		.value();
	},

	getSelectedAttributes : function (chartDef){

		var standardSelectedAttributes = _.chain(chartDef)
			.pick('filters', 'measures', 'axis')
			.values()
			.flatten()
			.filter(function(columnPrototypeInstance){//filter out the cuf prototype as we need to change ids only on cuf proto
				return columnPrototypeInstance.cufId === undefined || columnPrototypeInstance.cufId === null;
			})
			.pluck('column')
			.pluck('id')
			.uniq()
			.map(function(val) {return val.toString();})
			.value();

		var customFieldSelectedAttributes = this.getCufSelectedAttributes(chartDef);
		//convenient attribute to have easier templating in attribute step view
		this.set("selectedCufAttributes",customFieldSelectedAttributes);
		return _.union(standardSelectedAttributes,customFieldSelectedAttributes);

	},

	getCufSelectedAttributes : function(chartDef){
		var customFieldSelectedAttributes = _.chain(chartDef)
			.pick('filters', 'measures', 'axis')
			.values()
			.flatten()
			.filter(function(columnPrototypeInstance){//filter out the cuf prototype as we need to change ids only on cuf proto
				return columnPrototypeInstance.cufId != null;
			})
			.map(function (columnPrototypeInstance){
				columnPrototypeInstance.column.id = columnPrototypeInstance.column.id + "-" + columnPrototypeInstance.cufId;
				return columnPrototypeInstance;
			})
			.pluck('column')
			.pluck('id')
			.uniq()
			.map(function(val) {return val.toString();})
			.value();

			return customFieldSelectedAttributes;
	},

	getSelectedEntities : function (chartDef) {

		return _.chain(chartDef)
			.pick('filters', 'measures', 'axis')
			.values()
			.flatten()
			.pluck('column')
			.pluck('specializedType')
			.pluck('entityType')
			.uniq()
			.value();
	},

	getProjectScope : function(projectScope) {
		return _.map(projectScope, function(projectId) {
			return parseInt(projectId);
		});
	},



		toJson : function(param) {
			return JSON.stringify ({
			id : this.get('updateId') || null,
			name : this.get("name") || param,
			type : this.get("type"),
			query : {
				axis: this.extractAxisCufPrototype(),
				measures : this.extractMeasureCufPrototype(),
				filters : this.extractFiltersCufPrototype()
			},
			owner : this.get("owner") || null,
			projectScope : this.get("projectsScope"),
			scopeType : this.get("scopeType"),
			scope : _.map(this.get("scope"), function(val) {var newVal = _.clone(val); newVal.type = val.type.replace("LIBRARIE", "LIBRARY"); return newVal;})
			});
		},

		//put the real prototype column id for the cuf column prototype (ie the generic column proto that exist in database)
		extractAxisCufPrototype : function(){
			return _.map(this.get("axis"),function (axis) {
				if(axis.column.isCuf){
					var newAxis = _.clone(axis);
					var newColumn = _.clone(axis.column);
					newColumn.id = axis.column.originalPrototypeId;
					newAxis.column = newColumn;
					newAxis.cufId = axis.column.cufId;
					return newAxis;
				}
				else{
					return axis;
				}
				
			});
		},

		extractMeasureCufPrototype : function(){
			return _.map(this.get("measures"),function (measure) {
				if(measure.column.isCuf){
					var newMeasure = _.clone(measure);
					var newColumn = _.clone(measure.column);
					newColumn.id = measure.column.originalPrototypeId;
					newMeasure.column= newColumn;
					newMeasure.cufId = measure.column.cufId;
					return newMeasure;
				}
				else{
					return measure;
				}
				
			});
		},

		extractFiltersCufPrototype : function(){
			var list = _.map(this.get("filters"), function(filter) {
					var newFilter= _.clone(filter);			
					newFilter.values = _.flatten(filter.values);
					if(filter.column.isCuf){
						var newColumn = _.clone(filter.column);
						newColumn.id = filter.column.originalPrototypeId;
						newFilter.column = newColumn;
						newFilter.cufId = filter.column.cufId;
					}
					return newFilter;
			});
			return list;
		}
	});
});
