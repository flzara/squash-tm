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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "./abstractStepView","../custom-report-workspace/utils","./customFieldPopup"],
	function($, backbone, _, Handlebars, AbstractStepView,chartUtils,CustomFieldPopup) {
	"use strict";

	var attributesStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#attributes-step-tpl";
			this.model = data;
			data.name = "attributes";
			this.model.set("cufMapByEntity",chartUtils.extractCufsMapFromWorkspace());
			this.model.set("computedColumnsPrototypes",this.computeColumnsPrototypes());
			this._initialize(data, wizrouter);
			//listen to changes in cuf selected attributes
			this.listenTo(this.model, 'change:selectedCufAttributes', this.updateSelectedAttributesWithCuf);
			this.initializeCufCheckBox();
		},
		
		events : {
			"click .wizard-cuf-btn" : "openCufPopup",
			"click input[name='entity']" : "toggleEntityPanelVisibility"
		},
	
		updateModel : function() {

			var self = this;
			var ids = _.pluck($('[id^="attributes-selection-"]').filter(":checked:visible"), "name");
			this.model.set({"selectedAttributes" : ids});

			//also updating the convenient attribute selectedCufAttributes
			var selectedCuf = this.model.get("selectedCufAttributes");
			this.model.set({"selectedCufAttributes" : _.intersection(ids,selectedCuf)});

			//now retrieve the selected entities type to updated filter and operation view
			var allProtos = _.chain(self.model.get("computedColumnsPrototypes")).values().flatten().value();

			/* Finding selected entities from all the attributes check-boxes checked. */
			var selectedEntities = _.chain(ids)
				.map(function(id){
					return _.find(allProtos,function(proto){
						return proto.id === id || proto.id.toString() === id;
					});
				})
				.map(function(proto){
					return proto.specializedType.entityType;
				})
				.uniq()
				.value();
			/* Finding which entities icons were checked. */
			var checkedEntities = _.pluck($('[name="entity"]').filter(":checked"), "id");

			/* Intersection between the two variables above. 
			 * We don't want to include attributes that are checked but not visible. */
			selectedEntities = _.intersection(selectedEntities, checkedEntities);

			this.model.set({"selectedEntity" : selectedEntities});

			//now filtering out the axis, filter, measures and operations to sync them with new user selection
			var filteredAxis = this.filterWithValidIds(this.model.get("axis"));
			this.model.set({"axis" : filteredAxis});
			
			var filteredMeasures = this.filterWithValidIds(this.model.get("measures"));
			this.model.set({"measures" : filteredMeasures});

			var filteredFilters = this.filterWithValidIds(this.model.get("filters"));
			this.model.set({"filters" : filteredFilters});

			var filteredOperations = this.filterWithValidIds(this.model.get("operations"));
			this.model.set({"operations" : filteredOperations});
		},
		
		filterWithValidIds : function (col) {		
			var self = this;
			return _.chain(col)
			.filter(function(val){return _.contains(self.model.get("selectedAttributes"), "" + val.column.id);})
			.value();
			
		},

		openCufPopup : function(event) {
			var self = this;
			var entityType = event.target.getAttribute("data-entity");
			var cufToDisplay = _.mapObject(this.model.get("computedColumnsPrototypes"),function(prototypes,entityType) {
				return _.filter(prototypes,function(proto) {
					return proto.columnType === "CUF";
				});
			});
			this.model.set("cufToDisplay",cufToDisplay[entityType]);
			this.model.set("selectedCufEntity",entityType);
			var ids = _.pluck($('[id^="attributes-selection-"][data-cuf="true"]').filter(":checked"), "name");
			this.model.set("selectedCufAttributes",ids);
			var cufPopup = new CustomFieldPopup(this.model);
		},

		//callback executed when selected cuf changes
		updateSelectedAttributesWithCuf:function(model, newSelectedIds, options) {
			var self = this;
			var previousSelectedIds = model.previous("selectedCufAttributes");
			//damned, we have to play with lot's of manual change. A simple viewSate = fn(state) "a la react/redux" would be much cleaner...
			var idsToHide = _.difference(previousSelectedIds, newSelectedIds);
			_.each(idsToHide, function(id) {
				self.hideCufCheckBox(id);
			});

			var idsToShow = _.difference(newSelectedIds, previousSelectedIds);
			_.each(idsToShow, function(id) {
				self.showCufCheckBox(id);
			});
		},

		initializeCufCheckBox : function() {
			var ids = this.model.get("selectedCufAttributes") || [];
			var self = this;
			_.each(ids,function(id) {
				self.showCufCheckBox(id);
			});
		},

		showCufCheckBox : function(id) {
			var checkBoxSelector = '[id="attributes-selection-'+ id + '"]';
				var checkBox = this.$el.find(checkBoxSelector);
				checkBox.prop("checked",true);
				var checkBoxWrapperSelector = '[id="wrapper-attributes-selection-'+ id + '"]';
				var wrapper = this.$el.find(checkBoxWrapperSelector);
				wrapper.addClass("chart-wizard-visible");
				wrapper.removeClass("chart-wizard-hidden");
		},

		hideCufCheckBox : function(id) {
			var checkBoxSelector = '[id="attributes-selection-'+ id + '"]';
				var checkBox = this.$el.find(checkBoxSelector);
				checkBox.prop("checked",false);
				var checkBoxWrapperSelector = '[id="wrapper-attributes-selection-'+ id + '"]';
				var wrapper = this.$el.find(checkBoxWrapperSelector);
				wrapper.removeClass("chart-wizard-visible");
				wrapper.addClass("chart-wizard-hidden");
		},
		toggleEntityPanelVisibility : function(event) {
			var entityClicked = event.target.id;
			var entityPanelToToggle = $("#" + entityClicked + "-panel");
			entityPanelToToggle.toggleClass("not-displayed");
		}
	});
	return attributesStepView;
});