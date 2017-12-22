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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil", "workspace.routing","workspace.event-bus",
        "./TestCaseSearchResultTable", "squash.translator", "app/ws/squashtm.notification",
        "squash.configmanager", "workspace.projects" , "./milestone-mass-modif-popup", "./tc-export-popup", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog", "jquery.squash.milestoneDialog" ],
		function($, Backbone, _, StringUtil, routing, eventBus, TestCaseSearchResultTable, translator, notification, confman, projects, milestoneMassModif,tcExport) {

	var TestCaseSearchInputPanel = Backbone.View.extend({

		expanded : false,
		el : "#sub-page",

		initialize : function() {
			this.configureModifyResultsDialog();
			this.getIdsOfSelectedTableRowList =  $.proxy(this._getIdsOfSelectedTableRowList, this);
			this.getIdsOfEditableSelectedTableRowList = $.proxy(this._getIdsOfEditableSelectedTableRowList, this);
			this.updateDisplayedValueInColumn =  $.proxy(this._updateDisplayedValueInColumn, this);
			var model = JSON.parse($("#searchModel").text());
			this.domain = $("#searchDomain").text();
			this.isAssociation = !!$("#associationType").length;
			if(this.isAssociation){
				this.associationType = $("#associationType").text();
				this.associationId = $("#associationId").text();
			}
			this.model = model;
			new TestCaseSearchResultTable(model, this.domain, this.isAssociation, this.associationType, this.associationId);
			this.milestoneMassModif = new milestoneMassModif();
      this.tcExport = new tcExport();
		},

		events : {
			"click #export-search-result-button" : "exportResults",
			"click #modify-search-result-button" : "editResults",
			"click #new-search-button" : "newSearch",
			"click #modify-search-button" : "modifySearch",
			"click #associate-selection-button" : "associateSelection",
			"click #select-all-button" : "selectAllForAssocation",
			"click #associate-all-button" : "associateAll",
			"click #deselect-all-button" : "deselectAll",
			"click #modify-search-result-milestone-button" : "editMilestone"
		},



		associateSelection : function(){
			var table = $('#test-case-search-result-table').dataTable();
			var ids = table.squashTable().getSelectedIds();
			if (ids.length === 0){
				notification.showError(translator.get('message.noLinesSelected'));
				return;
			}
			var id = this.associationId;

			if("requirement" === this.associationType){

				var st = ids.join(',');

				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/requirement-versions/" + id + "/verifying-test-cases/"+st
				}).done(function() {
					$("#back").click();
				});

			}
			else{
				var url;

				switch(this.associationType){
				case "campaign" :  url = squashtm.app.contextRoot + "/campaigns/" + id + "/test-plan"; break;
				case "iteration" : url = squashtm.app.contextRoot + "/iterations/" + id + "/test-plan"; break;
				case "testsuite" : url = squashtm.app.contextRoot + "/test-suites/" + id + "/test-plan"; break;
				default : throw "unknown association type " +associationType;
				}

				$.ajax({
					type: "POST",
					url : url,
					data : { "testCasesIds[]" : ids }
				}).done(function() {
					$("#back").click();
				});
			}


		},

		selectAllForAssocation : function(){
			var table = $('#test-case-search-result-table').dataTable();
			var rows = table.fnGetNodes();
			var ids = [];
			$(rows).each(function(index, row) {
				ids.push(parseInt($(".element_id", row).text(),10));
			});

			table.squashTable().selectRows(ids);
		},

		deselectAll : function(){
			var table = $('#test-case-search-result-table').dataTable();
			table.squashTable().deselectRows();
		},

		associateAll : function(){
			this.selectAllForAssocation();
			this.associateSelection();
		},

		modifySearch : function(){

			if(this.isAssociation){
				this.post(squashtm.app.contextRoot + "advanced-search?searchDomain="+this.domain+"&id="+this.associationId+"&associateResultWithType="+this.associationType, {
					searchModel : JSON.stringify(this.model)
				});
			} else {
				this.post(squashtm.app.contextRoot + "advanced-search?searchDomain="+this.domain, {
					searchModel : JSON.stringify(this.model)
				});
			}
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

			if(this.isAssociation){
				document.location.href= squashtm.app.contextRoot +"advanced-search?searchDomain="+this.domain+"&id="+this.associationId+"&associateResultWithType="+this.associationType;
			} else {
				document.location.href= squashtm.app.contextRoot +"advanced-search?searchDomain="+this.domain;
			}
		},

		exportResults : function(){
      this.tcExport.open();
      },

		editResults : function(){
			this.addModifyResultDialog.confirmDialog("open");
		},

		editMilestone : function(){
			var self = this;
			var table = $('#test-case-search-result-table').squashTable();
			var ids = table.getSelectedIds();
			var dialogOptions = {
					tableSource : routing.buildURL('search-tc.mass-change.associable-milestone', ids),
					milestonesURL : routing.buildURL('search-tc.mass-change.bindmilestones', ids),
					requirementVersionIds : ids,
					dataURL : routing.buildURL('search-tc.mass-change.data', ids),
					searchTableCallback : function(){
						var table = $('#test-case-search-result-table').squashTable();
		                table._fnAjaxUpdate();
					},
					workspace : "test-case"
				};
			if (ids.length === 0){
				notification.showError(translator.get('message.noLinesSelected'));
			} else {
			this.milestoneMassModif.open(dialogOptions);
			}
		},
		enableTypeAndNatureModification : function(dialog, table){

			var rows = table.getSelectedRows();

			if (rows.length === 0){
				return;
			}

			// reset the controls
			$("#modify-search-result-dialog-project-conf-warning").hide();
			$(".mass-change-forbidden").hide();
			$(".mass-change-allowed").show();
			$(".mass-change-infolist-combo").prop('disabled', false);

			// find the selected projects unique ids
			var selectedProjects = [];
			rows.each(function(indx, row){
				selectedProjects.push(table.fnGetData(row)['project-id']);
			});
			selectedProjects = _.uniq(selectedProjects);

			// check for conflicts
			var difNat = projects.haveDifferentInfolists(selectedProjects, ["nature"]);
			var difTyp = projects.haveDifferentInfolists(selectedProjects, ["type"]);

			function populateCombo(select, infolistName){
				var p = projects.findProject(selectedProjects[0]);
				select.empty();

				for (var i=0;i<p[infolistName].items.length; i++){
					var item = p[infolistName].items[i];
					var opt = $('<option/>', {
						value : item.code,
						html : item.friendlyLabel
					});
					select.append(opt);
				}
			}

			if (difNat){
				$("#modify-search-result-dialog-project-conf-warning").show();
				$("#nature-disabled-icon").show();
				$("#nature-checkbox").hide();
			}
			else{
				// we can move on and populate the combo
				populateCombo($("#nature-combo select"), 'testCaseNatures');
			}

			if (difTyp){
				$("#modify-search-result-dialog-project-conf-warning").show();
				$("#type-disabled-icon").show();
				$("#type-checkbox").hide();
			}
			else{
				// we can move on and populate the combo
				populateCombo($("#type-combo select"), 'testCaseTypes');
			}

		},

		_getIdsOfSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];

			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
						ids.push(data["test-case-id"]);
				}
			});

			return ids;
		},

		_getIdsOfEditableSelectedTableRowList : function(dataTable){
			var rows = dataTable.fnGetNodes();
			var ids = [];

			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					if(data["editable"]){
						ids.push(data["test-case-id"]);
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
		initModifyMilestoneDialog : function() {

			var self = this;
			var table = $('#test-case-search-result-table').squashTable();
			var ids = table.getSelectedIds();

			var dialogOptions = {
					tableSource : routing.buildURL('search-tc.mass-change.associable-milestone', ids),
					milestonesURL : routing.buildURL('search-tc.mass-change.bindmilestones', ids),
					identity : ""
				};

			var addModifyMilestoneDialog = $('.bind-milestone-dialog');
            addModifyMilestoneDialog.milestoneDialog(dialogOptions);
			this.addModifyMilestoneDialog = addModifyMilestoneDialog;

			eventBus.on("node.bindmilestones", function(){
				var table = $('#test-case-search-result-table').squashTable();
				table._fnAjaxUpdate();
			});

		},

		configureModifyResultsDialog : function() {

			var self = this;
			var addModifyResultDialog = $("#modify-search-result-dialog").confirmDialog();

			function loadCombos(comboname){
				$.ajax({
					url : squashtm.app.contextRoot + "/test-cases/"+ comboname +"-data",
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
			 * configure the comboboxes. Note that the type and nature combos cannot
			 * be initialized before we know which test cases were selected.
			 */
			$("#nature-combo").append('<select/>');
			$("#type-combo").append('<select/>');
			loadCombos("importance-combo");
			loadCombos("status-combo");

			addModifyResultDialog.on('change', ':checkbox', function(evt){
				var cbx = $(evt.currentTarget),
					state = cbx.prop('checked'),
					select = cbx.parent().siblings().last().find('select');

				select.prop('disabled', !state);
			});


			addModifyResultDialog.on("confirmdialogvalidate",function() {});

			addModifyResultDialog.on("confirmdialogconfirm",function() {
				var table = $('#test-case-search-result-table').dataTable();
				var ids = self.getIdsOfEditableSelectedTableRowList(table);
				var columns = ["importance","status","type","nature"];
				var index = 0;

				for(index=0; index<columns.length; index++){
					if($("#"+columns[index]+"-checkbox").prop('checked')){
						self.updateDisplayedValueInColumn(table, columns[index]);
						var value = $("#"+columns[index]+"-combo").find('option:selected').val();
						for(var i=0; i<ids.length; i++){
							var urlPOST = squashtm.app.contextRoot + "/test-cases/" + ids[i];
							$.post(urlPOST, {
								value : value,
								id : "test-case-"+columns[index]
							});
						}
					}
				}
			});

			addModifyResultDialog.on('confirmdialogopen', function() {

				addModifyResultDialog.find(':checkbox').prop('checked', false);
				addModifyResultDialog.find('select').prop('disabled', true);
				var table = $('#test-case-search-result-table').squashTable();
				var ids = self.getIdsOfSelectedTableRowList(table);
				var editableIds = self.getIdsOfEditableSelectedTableRowList(table);

				if(ids.length === 0) {
					notification.showError(translator.get('message.noLinesSelected'));
					$(this).confirmDialog('close');
				} else if (editableIds.length === 0){
					notification.showError(translator.get('message.search.modify.noLineWithWritingRights'));
					$(this).confirmDialog('close');
				}else if (editableIds.length < ids.length){
					notification.showError(translator.get('message.search.modify.noWritingRights'));
				}



				self.enableTypeAndNatureModification(addModifyResultDialog, table);

			});

			addModifyResultDialog.activate = function(arg) {};

			this.addModifyResultDialog = addModifyResultDialog;
		}

	});
	return TestCaseSearchInputPanel;
});
