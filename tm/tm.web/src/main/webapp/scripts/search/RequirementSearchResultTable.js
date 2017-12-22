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
define([ "jquery", "backbone", "squash.translator","jeditable.simpleJEditable", "workspace.projects",
         "squash.configmanager", "workspace.routing", "app/ws/squashtm.notification", "squashtable",
         "jqueryui", "jquery.squash.jeditable", "jquery.cookie" ],
         function($, Backbone, translator, SimpleJEditable, projects, confman, routing, notification) {

	var RequirementSearchResultTable = Backbone.View.extend({
		el : "#requirement-search-result-table",
		initialize : function(model, isAssociation, associateType, associateId) {
			this.model = model;
			this.isAssociation = isAssociation;
			this.associateType = associateType;
			this.associateId = associateId;
			this.addSelectEditableToCriticality = $.proxy(this._addSelectEditableToCriticality, this);
			this.addSelectEditableToCategory = $.proxy(this._addSelectEditableToCategory, this);
			this.addSelectEditableToStatus = $.proxy(this._addSelectEditableToStatus, this);
			this.addSimpleEditableToReference = $.proxy(this._addSimpleEditableToReference, this);
			this.addSimpleEditableToLabel = $.proxy(this._addSimpleEditableToLabel, this);
			this.addInterfaceLevel2Link = $.proxy(this._addInterfaceLevel2Link, this);
			this.addIconToAssociatedToColumn = $.proxy(this._addIconToAssociatedToColumn, this);
			this.addTreeLink = $.proxy(this._addTreeLink, this);
			this.getTableRowId = $.proxy(this._getTableRowId, this);
			this.tableRowCallback = $.proxy(this._tableRowCallback, this);

			var self = this;
			var tableConf ;
			var squashConf;
			if(isAssociation){

				tableConf = {
						"oLanguage" : {
							"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
						},
						"bServerSide": true,
						"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
						"fnServerParams": function ( aoData )
							{
								aoData.push( { "name": "model", "value": JSON.stringify(model) } );
								aoData.push( { "name": "associateResultWithType", "value": associateType } );
								aoData.push( { "name": "id", "value":  associateId } );
								aoData.push( { "name": "requirement", "value": "requirement" } );
							},
						"sServerMethod": "POST",
						"bDeferRender" : true,
						"bFilter" : false,
						"fnRowCallback" : this.tableRowCallback,
						"fnDrawCallback" : this.tableDrawCallback,
						"aaSorting" : [ [ 2, "asc" ],  [4, "asc"], [6, "asc"], [7, "asc"], [8, "asc"], [5, "asc"] ],
						"aoColumnDefs" : [ {
							"bSortable" : false,
							"aTargets" : [ 0 ],
							"mDataProp" : "entity-index",
							"sClass" : "select-handle centered"
						}, {
							"aTargets" : [ 1 ],
							"mDataProp" : "empty-is-associated-holder",
							"bSortable" : false,
							"sWidth" : "2em",
							"sClass" : "is-associated centered"
						}, {
							"aTargets" : [ 2 ],
							"mDataProp" : "project-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 3 ],
							"mDataProp" : "requirement-id",
							"bSortable" : true,
							"sClass" : "centered element_id"
						}, {
							"aTargets" : [ 4 ],
							"mDataProp" : "requirement-reference",
							"bSortable" : true,
							"sClass" : "editable editable_ref"
						}, {
							"aTargets" : [ 5 ],
							"mDataProp" : "requirement-label",
							"bSortable" : true,
							"sClass" : "editable editable_label"
						}, {
							"aTargets" : [ 6 ],
							"mDataProp" : "requirement-criticality",
							"bSortable" : true,
							"sClass" : "editable editable_criticality"
						}, {
							"aTargets" : [ 7 ],
							"mDataProp" : "requirement-category",
							"bSortable" : true,
							"sClass" : "editable editable_category"
						}, {
							"aTargets" : [ 8 ],
							"mDataProp" : "requirement-status",
							"bSortable" : true,
							"sClass" : "editable editable_status"
						}, {
							"aTargets" : [ 9 ],
							"mDataProp" : "requirement-milestone-nb",
							"bSortable" : true,
							"sClass" : "centered"
						},{
							"aTargets" : [ 10 ],
							"mDataProp" : "requirement-version",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 11 ],
							"mDataProp" : "requirement-version-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 12 ],
							"mDataProp" : "requirement-testcase-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 13 ],
							"mDataProp" : "requirement-attachment-nb",
							"bSortable" : true
						}, {
							"aTargets" : [ 14 ],
							"mDataProp" : "requirement-created-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 15 ],
							"mDataProp" : "requirement-modified-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 16 ],
							"mDataProp" : "empty-openinterface2-holder",
							"sClass" : "centered search-open-interface2-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 17 ],
							"mDataProp" : "editable",
							"bVisible" : false,
							"bSortable" : false
						} ],
						"sDom" : 'ft<"dataTables_footer"lip>'
					};

				squashConf = {
						enableHover : true
					};

				this.$el.squashTable(tableConf, squashConf);
			} else {
				tableConf = {
						"oLanguage" : {
							"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
						},
						"bServerSide": true,
						"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
						"fnServerParams": function ( aoData )
							{
								aoData.push( { "name": "model", "value": JSON.stringify(model) } );
								aoData.push( { "name": "requirement", "value": "requirement" } );
							},
						"sServerMethod": "POST",
						"bDeferRender" : true,
						"bFilter" : false,
						"fnRowCallback" : this.tableRowCallback,
						"fnDrawCallback" : this.tableDrawCallback,
						"aaSorting" : [ [ 1, "asc" ], [ 3, "asc" ], [ 5, "asc" ], [ 6, "asc" ], [ 7, "asc" ], [ 4, "asc" ] ],
						"aoColumnDefs" : [ {
							"bSortable" : false,
							"aTargets" : [ 0 ],
							"mDataProp" : "entity-index",
							"sClass" : "select-handle centered"
						}, {
							"aTargets" : [ 1 ],
							"mDataProp" : "project-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 2 ],
							"mDataProp" : "requirement-id",
							"bSortable" : true,
							"sClass" : "centered element_id"
						}, {
							"aTargets" : [ 3 ],
							"mDataProp" : "requirement-reference",
							"bSortable" : true,
							"sClass" : "editable editable_ref"
						}, {
							"aTargets" : [ 4 ],
							"mDataProp" : "requirement-label",
							"bSortable" : true,
							"sClass" : "editable editable_label"
						}, {
							"aTargets" : [ 5 ],
							"mDataProp" : "requirement-criticality",
							"bSortable" : true,
							"sClass" : "editable editable_criticality"
						}, {
							"aTargets" : [ 6 ],
							"mDataProp" : "requirement-category",
							"bSortable" : true,
							"sClass" : "editable editable_category"
						}, {
							"aTargets" : [ 7 ],
							"mDataProp" : "requirement-status",
							"bSortable" : true,
							"sClass" : "editable editable_status"
						}, {
							"aTargets" : [ 8 ],
							"mDataProp" : "requirement-milestone-nb",
							"bSortable" : true,
							"sClass" : "centered"
						},
						{
							"aTargets" : [ 9 ],
							"mDataProp" : "requirement-version",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 10 ],
							"mDataProp" : "requirement-version-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 11 ],
							"mDataProp" : "requirement-testcase-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 12 ],
							"mDataProp" : "requirement-attachment-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 13 ],
							"mDataProp" : "requirement-created-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 14 ],
							"mDataProp" : "requirement-modified-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 15 ],
							"mDataProp" : "empty-openinterface2-holder",
							"sClass" : "centered search-open-interface2-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 16 ],
							"mDataProp" : "empty-opentree-holder",
							"sClass" : "centered search-open-tree-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 17 ],
							"mDataProp" : "editable",
							"bVisible" : false,
							"bSortable" : false
						} ],
						"sDom" : 'ft<"dataTables_footer"lip>'
					};

				squashConf = {
					enableHover : true
				};

				this.$el.squashTable(tableConf, squashConf);
			}


		},

		_getTableRowId : function(rowData) {
			return rowData[2];
		},

		_addSelectEditableToCriticality : function(row, data) {
			var self = this;
			var urlPOST = routing.buildURL('requirementversions', data['requirement-version-id']);
			var urlGET = squashtm.app.contextRoot + "/requirements/criticality-combo-data";
			var confirm = translator.get("rich-edit.button.confirm.label");
			var cancel = translator.get("label.Cancel");
			var component =$('.editable_criticality', row);
			//TODO use SelectJEditable obj
			component.editable(urlPOST, {
						type : 'select',
						submit : confirm,
						cancel : cancel,
						loadurl : urlGET,
						"submitdata" : function(value, settings) {
							return {"id": "requirement-criticality"};
						}
					});
			return component;
		},

		_addSelectEditableToCategory : function(row, data) {
			var urlPOST = routing.buildURL('requirementversions', data['requirement-version-id']);
			var projectCategories = projects.findProject(data['project-id']).requirementCategories;
			var catsData = confman.toJeditableSelectFormat(projectCategories.items, {'code' : 'friendlyLabel'});
			var confirm = translator.get("rich-edit.button.confirm.label");
			var cancel = translator.get("label.Cancel");
			var component = $('.editable_category', row);
			//TODO use SelectJEditable obj
			component.editable(urlPOST, {
				type : 'select',
				submit : confirm,
				cancel : cancel,
				data : catsData,
				"submitdata" : function(value, settings) {
					return {"id": "requirement-category"};
				}
			});
			return component;
		},

		_addSelectEditableToStatus : function(row, data) {
			var self = this;
			var urlPOST = routing.buildURL('requirementversions', data['requirement-version-id']);
			var urlGET = squashtm.app.contextRoot + "/requirements/status-combo-data";
			var confirm = translator.get("rich-edit.button.confirm.label");
			var cancel = translator.get("label.Cancel");
			var component = $('.editable_status', row);
			//TODO use SelectJEditable obj
			component.editable(urlPOST, {
				type : 'select',
				submit : confirm,
				cancel : cancel,
				loadurl : urlGET,
				"submitdata" : function(value, settings) {
					return {"id": "requirement-status"};
				}
			});
			return component;
		},

		_addSimpleEditableToReference : function(row, data) {
			var url = routing.buildURL('requirementversions', data['requirement-version-id']);
			var component = $("td.editable_ref", row);
			new SimpleJEditable({
				targetUrl : url,
				component : component,
				jeditableSettings : {
					"submitdata" : function(value, settings) {
						return {"id": "requirement-reference"};
					}
				}
			});
			return component;

		},

		_addSimpleEditableToLabel : function(row, data) {
			var url = routing.buildURL('requirementversions', data['requirement-version-id']);
			var component = $("td.editable_label", row);
			new SimpleJEditable({
				targetUrl : url,
				component : component,
				jeditableSettings : {
					"submitdata" : function(value, settings) {
						return {"id": "requirement-name"};
					},
					'onerror' : function(settings, self, xhr){
						xhr.errorIsHandled=true;
						notification.showXhrInDialog(xhr);
						self.reset();
					}
				}
			});
			return component;
		},

		_tableRowCallback : function(row, data, displayIndex) {
			if(data.editable){
				this.addSimpleEditableToReference(row,data);
				this.addSimpleEditableToLabel(row,data);
				this.addSelectEditableToCriticality(row,data);
				this.addSelectEditableToCategory(row,data);
				this.addSelectEditableToStatus(row,data);
			}else{
				$(row).addClass("nonEditable");
				$(row).attr('title', squashtm.app.requirementSearchResultConf.messages.nonEditableTooltip);
			}
			this.addInterfaceLevel2Link(row,data);
			this.addTreeLink(row,data);

			if(this.isAssociation){
				this.addIconToAssociatedToColumn(row,data);
			}
		},

		_addInterfaceLevel2Link : function(row, data) {
			var id = data["requirement-id"];
			var $cell = $(".search-open-interface2-holder",row);
			$cell.append('<span class="ui-icon ui-icon-pencil"></span>')
			.click(function(){
				window.location = squashtm.app.contextRoot + "/requirements/" + id + "/info";
			});
		},

		_addIconToAssociatedToColumn : function(row, data) {

			var associatedTo = data["is-associated"];

			if(associatedTo){
				if(this.associateType == "requirement"){
					$(".is-associated",row).append('<span class="associated-icon-requirement"></span>');
				} else if(this.associateType == "testcase"){
					$(".is-associated",row).append('<span class="associated-icon-testcase" title="'+translator.get('search.associatedwith.testcase.image.tooltip')+'"></span>');
				} else {
					$(".is-associated",row).append('<span class="associated-icon-campaign"></span>');
				}
			}
		},

		_addTreeLink : function(row, data){
			var self = this;
			var id = data["requirement-id"];
			var $cell = $(".search-open-tree-holder", row);
			$cell.append('<span class="search-open-tree"></span>')
				.click(function(){
					$.cookie("workspace-prefs", "REQUIREMENT-"+id, {path : "/"});
					window.location = squashtm.app.contextRoot + "requirement-workspace/";
			});
		},

		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return RequirementSearchResultTable;
});
