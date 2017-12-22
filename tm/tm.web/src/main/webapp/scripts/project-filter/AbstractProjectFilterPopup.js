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
 define([ "jquery",  "./ProjectFilterModel","underscore", "squashtable", "jqueryui", "jquery.squash.confirmdialog"],
		function($, ProjectFilterModel, _) {
	//TODO mutualize what can be with app/report/ProjectsPickerPopup and SingleProjectPickerPopup
	function eachCheckbox($domPicker, eachCallback) {
			var $boxes = $domPicker.find("table .project-checkbox");
			$boxes.each(eachCallback);
			return _.pluck($boxes, 'value');
	}


		var AbstractProjectFilterPopup = Backbone.View.extend({

			events : {
				"confirmdialogclose" : "close",
				"confirmdialogcancel" : "cancel",
				"confirmdialogconfirm" : "confirm",
				"click .project-checkbox" : "notifyModel",
				"click .project-picker-selall" : "selectAllProjects",
				"click .project-picker-deselall" : "deselectAllProjects",
				"click .project-picker-invsel" : "invertAllProjects"
			},

			_initialize : function() {
                var self = this;
                this.filterTable = $.proxy(this._filterTable, this);
                // process initial state
                var ids =[];
                this.$el.find("table tbody tr").each(function() {
                    var $checkbox = $(this).find(".project-checkbox");
                    var checked = $checkbox.is(":checked");
                    $checkbox.data("previous-checked", checked);
                    var id = $checkbox.val();
                    if(checked){
                        ids.push(id);
                    }
                });
                // set model
                var url  = this.$el.data("url");
                self.model = new ProjectFilterModel({projectIds : ids} );
                self.model.url = url;

                // init confirm dialog
                this.$el.confirmDialog({width : 800});

                // init datatable
                // change filter by search.dt
                self.table = this.$el.find("table").bind('search.dt', self.filterTable).squashTable({
                    "sScrollY": "500px",
                    "bFilter":true,
                    "bPaginate" : false,
                    "bServerSide" : false,
                    "bScrollCollapse": true,
                    "bAutoWidth" : true,
                    "bRetrieve" : false,
                    "sDom" : '<"H"lfr>t',
										"aoColumnDefs": [{
											"aTargets": [0],
											"bSearchable": false
										}]
                });
            },

			open : function(){
				this.$el.confirmDialog("open");
				this.table.fnAdjustColumnSizing();
			},
			_filterTable : function(event){
				var warning = this.$el.find(".filter-warning");
				var filterText = this.$el.find('div.dataTables_filter input').val();
				if(filterText){
					warning.show();
				}else{
					warning.hide();
				}
			},

			confirm : function(){
				throw "You must define confirm in objects deriving from this view";
			},

			cancel : function(){
				this.table.fnFilter( '' );
				this.$el.find(".project-checkbox").each(function() {
					var previous = $(this).data("previous-checked");
					this.checked = previous;
				});
			},

			dialogConfig : {
				autoOpen : false,
				resizable : false,
				modal : true
			},

			selectAllProjects : function() {
				var ids = eachCheckbox(this.$el, function() {
					this.checked = true;
				});
				this.model.select(ids);
			},

			deselectAllProjects : function () {
				var ids = eachCheckbox(this.$el, function() {
					this.checked = false;
				});
				this.model.deselect(ids);
			},

			invertAllProjects : function () {
				var selectIds = [];
				var deselectIds = [];
				eachCheckbox(this.$el, function() {
					if(this.checked){
						this.checked = false;
						deselectIds.push(this.value);
					}else{
						this.checked = true;
						selectIds.push(this.value);

					}

				});
				this.model.select(selectIds);
				this.model.deselect(deselectIds);
			},

			notifyModel : function(event){
				var checkbox = event.currentTarget;
				this.model.changeProjectState(checkbox.value, checkbox.checked);
			},

			// This popup is bugged as hell. It fire a close event on confirm and closing by click on the closethick
			// We need to cancel if clicked on closethick and do nothing if clicked on confirm as the confirm method will handle it
			// If we cancel on close, the confirm will be ignored...
			close : function(event) {
				if(event && event.originalEvent && event.originalEvent.target.className === "ui-icon ui-icon-closethick"){
					this.cancel();
				}
			}
		});

        //patch Backbone.View.extend to merge event hash of abstarct view and children view.
        AbstractProjectFilterPopup.extend = function(child) {
		var view = Backbone.View.extend.apply(this, arguments);
		view.prototype.events = _.extend({}, this.prototype.events, child.events);
		return view;
	};

		return AbstractProjectFilterPopup;
});
