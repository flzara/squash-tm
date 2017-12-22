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
/**
 * projectPicker JQuery ui widget. Should be used with the appropriate dom
 * component (project-picker.frag.html)
 *
 * Configuration : { url: "the url where to get the projects", required confirm :
 * function() on confirm, required cancel: function() on cancel, optional,
 * defaults to close loadOnce: true|false|"never", loads projects only once,
 * defaults to false, loads on each open, "never" never loads. }
 *
 * It also forwards additional configuration to the internal popup dialog.
 *
 * Methods : open, close
 */
 define([ "jquery",  "backbone", "./ProjectsPickerModel", "underscore", "squashtable", "jqueryui", "jquery.squash.confirmdialog"],
		function($, Backbone, ProjectFilterModel, _) {
	 "use strict";

	 //TODO mutualize what can be with ProjectFilterPopup and SingleProjectPickerPopup
	function eachCheckbox($domPicker, eachCallback) {
			var $boxes = $domPicker.find("table .project-checkbox");
			$boxes.each(eachCallback);
			return _.pluck($boxes, "value");
	}


	var ProjectFilterPopup = Backbone.View.extend({

		events : {
			"confirmdialogcancel" : "cancel",
			"confirmdialogconfirm" : "confirm",
			"click .project-checkbox" : "notifyModel",
			"click .project-picker-selall" : "selectAllProjects",
			"click .project-picker-deselall" : "deselectAllProjects",
			"click .project-picker-invsel" : "invertAllProjects"
		},

		initialize :function(){
			_.bindAll(this, "filterTable", "updateResult", "updateFormState");

			var self = this;

			this.name = self.$el.attr("id");
			this.$result = $("#" + this.$el.data("idresult"));
			// process initial state
			var ids = this.model.get(this.name);

			this.$("table tbody tr .project-checkbox").each(function() {
				var $checkbox = $(this);
				var id = $checkbox.val();
				var checked = _.contains(ids.val, id);
				$checkbox.data("previous-checked", checked);
				$checkbox.prop("checked", checked);
			});

			// set model
			self.filterModel = new ProjectFilterModel({projectIds : ids});

			// init confirm dialog
			this.$el.confirmDialog({ width : 800 });

			// init datatable
			self.table = this.$("table").bind("filter", self.filterTable).squashTable({
					"sScrollY": "500px",
					"bFilter":true,
					"bPaginate" : false,
					"bServerSide" : false,
					"bScrollCollapse": true,
					"bAutoWidth" : true,
					"bRetrieve" : false,
					"sDom" : "<\"H\"lfr>t"
				});
			this.updateFormState();
			this.updateResult();

		},

		open : function(){
			this.$el.confirmDialog("open");
			this.table.fnAdjustColumnSizing();
		},

		filterTable : function(event){
			var warning = this.$(".filter-warning");
			var filterText = this.$("div.dataTables_filter input").val();
			if(filterText){
				warning.show();
			}else{
				warning.hide();
			}
		},

		confirm : function(){
			this.table.fnFilter( "" );
			this.updateFormState();
			this.updateResult();
		},

		updateResult : function(){
			var self = this;
			
			var selection = self.filterModel.get("projectIds").val;

			if (selection.length > 1) {
				self.$result.text(self.$result.data("multiplevaluetext"));
			} else if (selection.length == 1) {
				var projectId = selection[0];
				var projectName = this.$("table td .project-checkbox[value=" + projectId + "]").parent()
						.parent().find(".project-name").text();
				self.$result.text(projectName);
			} else {
				self.$result.text("");
			}
		},

		/**
		 * "this" bound to view at init
		 */
		updateFormState : function(){
			this.model.setVal(this.name, this.filterModel.get("projectIds").val);
		},

		cancel : function(){
			this.table.fnFilter("");
			this.$(".project-checkbox").each(function() {
				var previous = $(this).data("previous-checked");
				$(this).prop("checked", previous);
			});
		},

		selectAllProjects : function() {
			var ids = eachCheckbox(this.$el, function() {
				$(this).prop("checked", true);
			});
			this.filterModel.select(ids);
		},

		deselectAllProjects : function () {
			var ids = eachCheckbox(this.$el, function() {
				$(this).prop("checked", false);
			});
			this.filterModel.deselect(ids);
		},

		invertAllProjects : function () {
			var selectIds = [];
			var deselectIds = [];
			eachCheckbox(this.$el, function() {
				var $checkbox = $(this);
				if($checkbox.is(":checked")){
					$checkbox.prop("checked",false);
					deselectIds.push(this.value);
				}else{
					$checkbox.prop("checked",true);
					selectIds.push(this.value);

				}

			});
			this.filterModel.select(selectIds);
			this.filterModel.deselect(deselectIds);
		},

		notifyModel : function(event){
			var $checkbox = $(event.currentTarget);
			this.filterModel.changeProjectState($checkbox.val(), $checkbox.is(":checked"));
		}

	});

	return ProjectFilterPopup;
});
