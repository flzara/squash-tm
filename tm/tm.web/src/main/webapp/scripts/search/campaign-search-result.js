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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil","workspace.routing","workspace.event-bus", "squash.dateutils", 
         'tree', './execution-treemenu',
        "./CampaignSearchResultTable", "squash.translator", "app/ws/squashtm.notification",
        "workspace.projects", "./milestone-mass-modif-popup", 
        "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog",
		"jquery.squash.formdialog", "jquery.squash.milestoneDialog" ], 
		function($, Backbone, _, StringUtil, routing, eventBus, dateutils, tree, treemenu, CampaignSearchResultTable, 
				translator, notification, projects, milestoneMassModif) {
	
	// locale-dependant data for the creation of new iterations (see far below)
	var newIterationLabels = translator.get({
		name : 'label.generatedIT.name',
		desc : 'label.generatedIT.description'
	});
	
	
	// main view	
	var CampaignSearchResultPanel = Backbone.View.extend({

		expanded : false,
		el : "#sub-page",

		initialize : function() {
		  this.configureModifyResultsDialog();
		  this.configureExecutionDialog();
			this.getIdsOfSelectedTableRowList =  $.proxy(this._getIdsOfSelectedTableRowList, this);
			
			this.getIdsOfEditableSelectedTableRowList = $.proxy(this._getIdsOfEditableSelectedTableRowList, this);
			this.updateDisplayedValueInColumn =  $.proxy(this._updateDisplayedValueInColumn, this);
			var model = JSON.parse($("#searchModel").text());
			this.model = model;
			new CampaignSearchResultTable(model, this.isAssociation, this.associationType, this.associationId);
			this.initTableCallback();
			
		},

		events : {
			"click #select-all-button" : "selectAllForAssocation",
			"click #deselect-all-button" : "deselectAll",			
			"click #modify-search-result-button" : "editResults",			
			"click #new-search-button" : "newSearch",
			"click #modify-search-button" : "modifySearch",		
			"click #add-search-result-button" : "addITPI"
		},

	
		initTableCallback : function(){
			//little hack to select only previously selected campaigns (should be like requirements)
			var self = this;
			var table = $('#campaign-search-result-table').squashTable();
			table.drawcallbacks.push(function() {  
				table.deselectRows();
			self._restoreSelect();}
			);
		},
		
		selectAllForAssocation : function(){
			var table = $('#campaign-search-result-table').dataTable();
			var rows = table.fnGetNodes();
			var ids = [];
			$(rows).each(function(index, row) {
				ids.push(parseInt($(".element_id", row).text(),10));
			});
			
			// Should do that but it doesn't work, need to debug it
			// table.squashTable().selectRows(ids);
			
			$(rows).addClass('ui-state-row-selected');
			
		},
		
		deselectAll : function(){
			var table = $('#campaign-search-result-table').dataTable();
			table.squashTable().deselectRows();
		},
		
		modifySearch : function(){

				this.post(squashtm.app.contextRoot + "/advanced-search?searchDomain=campaign", {
					searchModel : JSON.stringify(this.model) 
				});	

		},

		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
			temp.acceptCharset="UTF-8";
			for(var x in PARAMS) {
				var opt=document.createElement("textarea");
				opt.name=x;
				opt.value=PARAMS[x];
				temp.appendChild(opt);
			}
			document.body.appendChild(temp);
			temp.submit();
			return temp;
		},
		
		newSearch : function(){
				document.location.href= squashtm.app.contextRoot +"/advanced-search?searchDomain=campaign";

		},
		

		 
		editResults : function(){
			this.addModifyResultDialog.confirmDialog("open");
		},
		
		addITPI: function(){
			this.addITPIDialog.formDialog("open");
		},
		
		_restoreSelect : function restoreSelect(){
			
			var selectedIds = this.selectedIds;
			var table = $('#campaign-search-result-table').squashTable();
			
			if ((selectedIds instanceof Array) && (selectedIds.length > 0)) {
				var rows = table.fnGetNodes();
				$(rows).filter(function() {		
					var rId = table.fnGetData(this)["itpi-id"];
					return $.inArray(rId, selectedIds) != -1;
				}).addClass('ui-state-row-selected');
			}
			
		},

		
		_getIdsOfSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					ids.push(data["itpi-id"]);
				}
			});
			
			return ids;
		},
		

		
		_getIdsOfEditableSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					if(data["editable"]){
						ids.push(data["itpi-id"]);
					} 
				}
			});
			
			return ids;
		},
		
		
	
		
		_updateDisplayedValueInColumn : function(dataTable, column) {
			var rows = dataTable.fnGetNodes();
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1  && dataTable.fnGetData(row)["editable"]) {
					var value = $("#"+column+"-combo").find('option:selected').text();
					$(".editable_"+column, row).text(value);
				}
			});

		},
				
	
		
		 
		configureModifyResultsDialog : function() {
			var self = this;
			var addModifyResultDialog = $("#modify-search-result-dialog").confirmDialog();

			
			function loadCombos(comboname){
				$.ajax({
					url : squashtm.app.contextRoot + "/executions/"+ comboname +"-data",
					dataType : 'json'
				})
				.success(function(json) {
					var combo = $("<select/>"),
						comboCell = $("#"+comboname);
						
					 $.each(json, function(key, value){ 
						var option = $("<option/>",{
							value : key,
							html : value
						});
						combo.append(option);
					 });
					 comboCell.append(combo);
				});
			}
			

			/*
			 * configure the comboboxes. Note that the category combos cannot 
			 * be initialized before we know which requirements were selected.  
			 */
			loadCombos("status-combo");
			
			addModifyResultDialog.on('change', ':checkbox', function(evt){
				var cbx = $(evt.currentTarget),
					state = cbx.prop('checked'),
					select = cbx.parent().siblings().last().find('select');
				
				select.prop('disabled', !state);
			});
			
			addModifyResultDialog.on("confirmdialogvalidate",function() {});

			addModifyResultDialog.on("confirmdialogconfirm",function() {
				var table = $('#campaign-search-result-table').dataTable();
				var ids = self.getIdsOfSelectedTableRowList(table);
				var editableIds = self.getIdsOfEditableSelectedTableRowList(table);
				var index = 0;
				
			
					if($("#status-checkbox").prop('checked')){
						self.updateDisplayedValueInColumn(table, "status");
						var value = $("#status-combo").find('option:selected').val();
					
							var urlPOST = routing.buildURL('iterations.testplan.changestatus', editableIds);
							$.post(urlPOST, {
								status : value,
								id : "status"	
							}).success(function(){
								$('#campaign-search-result-table').squashTable()._fnAjaxUpdate();
							});
						
					}
				
			});
			
			addModifyResultDialog.on('confirmdialogopen',function() {
				addModifyResultDialog.find(':checkbox').prop('checked', false);
				addModifyResultDialog.find('select').prop('disabled', true);
			
				var table = $('#campaign-search-result-table').squashTable();
				var ids = self.getIdsOfSelectedTableRowList(table);
				var editableIds = self.getIdsOfEditableSelectedTableRowList(table);
				if(ids.length === 0) {							
					notification.showError(translator.get('message.noLinesSelected'));
					$(this).confirmDialog('close');
				}else if (editableIds.length === 0){
					notification.showError(translator.get('message.search.modify.noLineWithWritingRightsOrWrongStatus'));
					$(this).confirmDialog('close');
				}else if (editableIds.length < ids.length){							
					notification.showError(translator.get('message.search.modify.noWritingRightsOrWrongStatus'));
				}
				
	

			});

			addModifyResultDialog.activate = function(arg) {};

			this.addModifyResultDialog = addModifyResultDialog;
		},
		
		loadTree : function (){
			$.ajax({
				url : squashtm.app.contextRoot + "/campaign-workspace/tree/''",
				datatype : 'json' 
			})
			.success(function(json) {
			 // Add tree in the dialog > rootModel is supposed to be given thanks to the controller
				squashtm.app.campaignWorkspaceConf.tree.model = json;
				tree.initWorkspaceTree(squashtm.app.campaignWorkspaceConf.tree);
			});
			
		},
		
		configureExecutionDialog : function() {
			var self = this;
			var addITPIDialog = $("#test-plan-dialog").formDialog({
				height : 400
			});

 
			addITPIDialog.activate = function(arg) {};

			addITPIDialog.on('formdialogopen',function() {
				addITPIDialog.formDialog('open');
				var selectedIds = $("#campaign-search-result-table").squashTable().getSelectedIds();
				if (selectedIds.length === 0){
					addITPIDialog.formDialog('close');
				  notification.showError(translator.get('message.noLinesSelected'));
					
				} 

				else {
				// get the execution id, give it to the controller which gives back the rootmodel for the tree
				self.loadTree();
				}
				
			});
			
			addITPIDialog.on('formdialogconfirm',function() {
				
				// Get all executions we want to add
				var selectedIds = $("#campaign-search-result-table").squashTable().getSelectedIds();

			
				// Get the place where we want to put the executions 
				var nodes = $("#tp-dialog-tree").jstree('get_selected');
				
				// Node must be an iteration (and only one for now)
				if (nodes.getResType() !== "iterations") {
					 notification.showError(translator.get('message.SelectIteration'));
				}
				else if(nodes.length > 1){
					 notification.showError(translator.get('message.SelectOneIteration'));
				}
				else {
					$.ajax({
						url : squashtm.app.contextRoot + "/iterations/" +  nodes.getResId() + "/test-plan"  ,
						type : 'POST',
						data : {
							itpiIds : selectedIds 
						}
					})
					.success(function(json) {
						addITPIDialog.formDialog('close');
					});
				}

				
				
				
			});
			
			function createIteration(){
				// create the data for the new iteration
				// note that by convention the date format is statically set to US standards because 
				// iteration names themselves are not locale-dependant anyway
				var now = dateutils.format(new Date(), "yyyy/MM/dd HH:mm:ss");
				return {
					name : newIterationLabels.name + " " + now,
					description : newIterationLabels.description,
					reference : "",
					copyTestPlan : false
				};			
			}
			
			addITPIDialog.on('formdialogadd',function() {

				var tree = $("#tp-dialog-tree");
				// Get the place where we want to add the iteration
				var nodes = tree.jstree('get_selected');

				// Node must be a campaign (and only one for now)
				if (nodes.getResType() !== "campaigns") {
					 notification.showError(translator.get('message.SelectCampaign'));
				}
				else if(nodes.length > 1){
					 notification.showError(translator.get('message.SelectOneCampaign'));
				}
				else {					
					var postITParams = createIteration();
					tree.jstree('postNewNode', 'new-iteration', postITParams, true);
				}
			});
			
			
			
			addITPIDialog.on('formdialogcancel',function() {
				addITPIDialog.formDialog('close');
			});
			
			addITPIDialog.on('formdialogclose',function() {
				addITPIDialog.formDialog('close');
			});
			
			
			this.addITPIDialog = addITPIDialog; 
			
		}
		
	});
	return CampaignSearchResultPanel;
});






