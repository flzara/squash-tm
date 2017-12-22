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
 define([ "jquery", "backbone", "underscore", "squashtable", "jqueryui", "jquery.squash.confirmdialog"], function($, Backbone, _) {
	 "use strict";

	//TODO mutualize what can be with ProjectFilterPopup and ProjectsPickerPopup
	var ProjectFilterPopup = Backbone.View.extend({
		events : {
			"confirmdialogcancel" : "cancel",
			"confirmdialogconfirm" : "confirm",
			"click .project-checkbox" : "setSelected"
		},

		initialize :function(){
			_.bindAll(this, "filterTable", "updateResult", "updateFormState", "updateChecked");

			var self = this;
			this.name = self.$el.attr("id");
			this.$result = $("#" + this.$el.data("idresult"));
			this.selectedId = this.model.get(this.name).val[0];

			this.$("table tbody tr .project-checkbox").each(function() {
				var $checkbox = $(this);
				var id = $checkbox.val();
				var checked = self.selectedId == id;
				$checkbox.data("previous-checked", checked);
				$checkbox.prop("checked", checked);
			});

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
				"sDom" : "<\"H\"lfr>t",
				"fnDrawCallback" : self.updateChecked
			});
			self.updateFormState();
			self.updateResult();
		},

		open : function(){
			this.$el.confirmDialog("open");
			this.table.fnAdjustColumnSizing();
		},

		filterTable : function(event){
			var self = this;
			var warning = this.$(".filter-warning");
			var filterText = this.$("div.dataTables_filter input").val();
			if(filterText){
				warning.show();
			} else {
				warning.hide();
			}
		},

		updateChecked : function(){
			var self = this;
			//To fix problem when check another radio button while previously checked was hidden.
			this.$("table tbody tr .project-checkbox").each(function() {
				var checkbox = this;
				var id = checkbox.value;
				var checked = self.selectedId == id;
				checkbox.checked = checked;
			});
		},

		confirm : function(){
			this.table.fnFilter("");
			this.updateFormState();
			this.updateResult();
		},

		/*
		 * Will update the state of the report form
		 */
		updateFormState : function(){
			var val = _.isUndefined(this.selectedId) ? [] : [ this.selectedId ];
			this.model.setVal(this.name, val);
		},

		/*
		 * Will update the name of the selected project next to the button that opens the popup
		 */
		updateResult : function(){
			if(!!this.selectedId) {
				var projectName = this.$("table td .project-checkbox[value=" + this.selectedId + "]").parent().parent().find(".project-name").text();
				this.$result.text(projectName);
			} else {
				this.$result.text("");
			}
		},

		cancel : function(){
			this.table.fnFilter( "" );
			this.$(".project-checkbox").each(function() {
				var previous = $(this).data("previous-checked");
				this.checked = previous;
			});
		},

		setSelected : function(event){
			var radio = event.currentTarget;
			this.selectedId = radio.value;
		}
	});

	return ProjectFilterPopup;
});
