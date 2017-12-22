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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "./abstractStepView", "squash.configmanager", "squash.translator", "jeditable.datepicker", "jquery.squash.togglepanel",],
	function($, backbone, _, Handlebars, AbstractStepView, confman, translator) {
	"use strict";

	var filterStepView = AbstractStepView.extend({

		initialize : function(data, wizrouter) {
			this.dateISOFormat = $.datepicker.ISO_8601;
			this.datePickerFormat = translator.get("squashtm.dateformatShort.datepicker");
			this.tmpl = "#filter-step-tpl";
			this.model = data;
			data.name = "filter";
			this._initialize(data, wizrouter);

			var infoListSrc = $("#info-list-tpl").html();
			this.infoListTemplate = Handlebars.compile(infoListSrc);
			var infoListItemSrc = $("#info-list-item-tpl").html();
			this.infoListItemTemplate = Handlebars.compile(infoListItemSrc);

			var cufListSrc = $("#chart-wizard-cuf-list-tpl").html();
			this.cufListTemplate = Handlebars.compile(cufListSrc);

			var pickerconf = confman.getStdDatepicker();
			$(".date-picker").datepicker(pickerconf);
			this.initInfoListValues();
			this.initDropDownCufValues("DROPDOWN_LIST");
			this.initDropDownCufValues("TAG");
			this.reloadPreviousValues();
			this.initOperationValues();
			this.removeStatusDependingOnProjectConf();

		},

		events : {
			"change .filter-operation-select" : "changeOperation",
			"change .info-lists" : "changeInfoList"
		},

		initDropDownCufValues : function (cufType) {

			var self = this;

			var cufLists = this.getCufList(cufType);

			_.each(cufLists, function(liste){

				var $val = $("#list-filter-container-" + liste.id);

				var items = _.filter(liste.cufListOptions, function(cufOption) {
					return cufOption.label && cufOption.label !== "";
				});

				$val.html(self.cufListTemplate({id : liste.id,items : items}));

			});

		},

		getCufList : function(cufType){
			var self = this;

			var cufLists = _.chain(self.model.get('computedColumnsPrototypes'))
			.values()
			.flatten()
			.where({columnType : "CUF", cufType : cufType})
			.value();

			return cufLists;
		},

		initInfoListValues : function() {

			var self = this;
			var ids = self.getInfoListSelectorIds();

			var scope = _.size(self.model.get("projectsScope")) > 0 ? self.model.get("projectsScope") : "default";

			var scopedInfoLists = self.getInfoListForScope(scope);
			var infoLists = self.getAllInfoList();

			var scopedEntity = self.model.get("scopeEntity");

		_.each(ids, function(id){
			var container = $("#info-list-filter-container-" + id);
			var name = container.attr("name");


			var lists;
			if (scopedEntity == self.findTypeFromColumnId(id) || scopedEntity == "default"){
				lists = _(scopedInfoLists[name]);
			} else {
				lists = _(infoLists[name]);
			}
			lists = lists.uniq(false, function (val) {return val.id;});


			var infoListHtml = self.infoListTemplate({id :id, infolists : lists});
			container.html(infoListHtml);
			self.loadInfoListItems(id);
		});

		},

		getInfoListSelectorIds : function() {

			var self = this;

			return _.chain(self.model.get("columnPrototypes"))
		.reduce(function(memo, val) {return memo.concat(val);}, [])
		.where({dataType : "INFO_LIST_ITEM"})
		.pluck("id")
		.value();

		},

		getInfoListsFromModel : function(infoLists){

			return _.chain(infoLists)
			.map(_.pairs)
			.reduce(function(memo, val){ return memo.concat(val);}, [])
            .reduce(function(memo, val) {
            	if(memo[val[0]] === undefined){
            	memo[val[0]] = [];}
            	memo[val[0]] = memo[val[0]].concat(val[1]);
            	return memo;}, {})
			.value();
		},

		getInfoListForScope : function (scope){
			return this.getInfoListsFromModel(_(this.model.get("projectInfoList")).pick(scope));
		},

		getAllInfoList : function (){
			return this.getInfoListsFromModel(this.model.get("projectInfoList"));
		},

		loadInfoListItems : function (id) {

			var self = this;

			var selectedList = $("#info-list-" + id).val();

			if (!_.isArray(selectedList)){
				selectedList = [selectedList];
			}

			var infoListItems = _.chain(self.model.get("projectInfoList"))
			.reduce(function(memo, val){ return memo.concat(_.values(val));}, [])
			.filter(function(obj) {return _.contains(selectedList, obj.code);})
			.uniq(false, function(obj){return obj.code;})
			.reduce(function(memo, val){
				return memo.concat(_.map(val.items, function (item){
					item.isSystem = val.createdBy == "system";
					return item;}));}, [])
			.value();

			var container = $("#info-list-item-container-" + id);
			var infoListItemHtml = self.infoListItemTemplate({items : infoListItems, id : id});
			container.html(infoListItemHtml);

		},

		changeInfoList : function (event){
			this.loadInfoListItems(event.target.name);
			this.initOperationValues();
		},

		initOperationValues : function (){

			var self = this;
			$(".filter-operation-select").each(function(indx, operation) {
				self.showFilterValues(operation.name , operation.value, operation.getAttribute("data-cuf-binding-id"));
			});

		},

		reloadPreviousValues : function (){

			var self = this;
			var filters = this.model.get("filters");

			if (filters !== undefined){

				_.each(filters, function(filter){
					self.applyPreviousValues(filter);
				});
			}

		},
		applyPreviousValues : function (filter){
			var self = this;
			var id = filter.column.id;


			$("#filter-selection-" + id).attr("checked", "true");
			$("#filter-operation-select-" + id).val(filter.operation);

			self.reloadInfoList(filter);
			self.showFilterValues(id, filter.operation);
			self.reloadCufValues(filter);

			$("#first-filter-value-" + id).val(self.getValueFromFilter(filter, 0));
			$("#second-filter-value-" + id).val(self.getValueFromFilter(filter, 1));
		},
		getValueFromFilter : function (filter, pos){
			var self = this;
			var datatype = filter.column.dataType;

			var result = filter.values[pos];

			if ((datatype === "DATE" || datatype === "DATE_AS_STRING") && result !== undefined){
			var date = $.datepicker.parseDate(self.dateISOFormat, result);
			result = $.datepicker.formatDate(self.datePickerFormat, date);
			}

			return  result;
		},

		reloadInfoList : function (filter){

			var self = this;
			var datatype = filter.column.dataType;

			if (datatype == "INFO_LIST_ITEM") {

			var id = filter.column.id;
			var value = filter.values[0];

			if (!_.isArray(value)){
				value = [value];
			}

			var selectedInfoList = _.chain(self.model.get("projectInfoList"))
			.reduce(function(memo, val){ return memo.concat(_.values(val));}, [])
			.uniq(false, function(val) {return val.id;})
		    .reduce(function(memo, val) {
		    	memo[val.code] = _.map(val.items, function (item){return item.code;})
		    	;return memo;}, {})
		    .pairs()
		    .filter(function(val) {return !_.isEmpty(_.intersection(val[1], value));})
		    .map(_.first)
			.value();
			self.showFilterValues(id, filter.operation);
			$("#info-list-" + id).val(selectedInfoList);
			self.loadInfoListItems(id);
			}
		},

		reloadCufValues : function(filter) {
			var self = this;
			var datatype = filter.column.dataType;
			if(datatype === "TAG"){
				var id = filter.column.id;
				var value = filter.values[0];
				$("#first-filter-value-" + id).val(value);
			}
		},

		updateModel : function() {
			//get ids of selecteds columns
			var ids = _.pluck($('[id^="filter-selection-"]').filter(":checked"), "name");
			var self = this;
			var filters = ids.map(function (id){
				return {
					column : self.findColumnById(id),
					operation : $("#filter-operation-select-" + id).val(),
					values : self.getFilterValues(id) };
				});

			//filtering filters
			//a filter is valid only if his values are :
			//	- not empty
			//	- none of the values are undefined, null or equals to empty string
			filters = _.chain(filters)
				.filter(function(filter){
					var filterValuesAreValid = true;
					if( _.isEmpty(filter.values)){
						filterValuesAreValid = false;
					} else {
						_.each(filter.values,function(value) {
							if(value === null || value === "" )	{
								filterValuesAreValid = false;
							}
						});
					}
					return filterValuesAreValid;
				})
				.value();
			this.model.set({ filters : filters });
			this.model.set({filtered : [true]});


		},
		getFilterValues : function (id){
			var self = this;
			var datatype = self.findColumnById(id)["dataType"];
			var result = [$("#first-filter-value-" + id).val(), $("#second-filter-value-" + id).val()];


			if (datatype == "DATE" || datatype == "DATE_AS_STRING"){
				result = _.map(result, function(elem){
					var date = $.datepicker.parseDate(self.datePickerFormat, elem);
					var result = $.datepicker.formatDate(self.dateISOFormat, date);
					return result;
				});
			}

			if (datatype == "TAG"){
				var cufList = this.getCufList("TAG");
				var cufListTagValues = _.chain(cufList)
					.pluck("cufListOptions")
					.flatten()
					.value();

				var cufTagValue =  _.find(cufListTagValues, function (value) {
						return value.code === result[0];
					});

				if(cufTagValue){
					result = [cufTagValue.label];
				}
			}

			return self.removeEmpty(result);

		},
		removeEmpty : function(tab){
			return _.filter(tab, function(elem){return elem !== undefined && elem !== "";});

		},
		findColumnById : function (id){
			return _.chain(this.model.get("computedColumnsPrototypes"))
			.values()
			.flatten()
			.find(function(col){return col.id == id; })
			.value();
		},

		changeOperation : function(event){
			this.showFilterValues(event.target.name, event.target.value, event.target.getAttribute("data-cuf-binding-id"));
		},

		findTypeFromColumnId : function(id){
			return _.chain(this.model.get("columnPrototypes"))
			.pairs()
			.find(function(val){ var ids =_.pluck(val[1], "id"); return _.contains(ids,id); })
			.first()
			.value();
		},

		showFilterValues : function (id, val, cufBindingId){

			var selector = this.getSecondFilterSelector(id, val, cufBindingId);
      		var selectorLabel = this.getSecondFilterLabelSelector(id, val, cufBindingId);

			if (val == "BETWEEN") {
				selector.show();
       			selectorLabel.show();
			} else {
				selector.hide();
				selectorLabel.hide();
				selector.val('');
			}

			var select = $("select[name=" + id + "]").not(".filter-operation-select");

			select.attr("MULTIPLE", val == "IN");


		},

		getSecondFilterSelector : function (id) {
			return $("#second-filter-value-" + id);
		},

		getSecondFilterLabelSelector : function (id) {
			return $("#second-filter-value-label-" + id);
		},

		removeStatusDependingOnProjectConf : function() {

			var projectScope = this.model.get('projectsScope');
			var disabledStatusByProject = this.model.get('disabledStatusByProject');

			var disabledStatus = _.intersection
			.apply(_, _.chain(disabledStatusByProject)
					.pick(projectScope)
					.values()
					.value());

			_.each(disabledStatus, function(status){

				$(".exec-status").filter("option[value='" + status + "']").remove();

			});


		}

	});

	return filterStepView;

});
