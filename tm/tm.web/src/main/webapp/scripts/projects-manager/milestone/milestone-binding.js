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
define([ 'module', "jquery", "squash.basicwidgets", "workspace.routing", "squash.translator", "squash.configmanager", 
         "squashtable", "jquery.squash.formdialog", "jeditable.datepicker", "jqueryui" ], 
         function(module, $, basic, routing, translator, confman ) {


	var trans = translator.get({
		chooseDate : "milestone.chooseDate"
		});
	
	
			basic.init();
			var config = module.config();
			var emptyDate = "-";
			var squashSettings = {
					functions:{
						computeSelectionRange : function(row) {
							var baseRow = this.data("lastSelectedRow");
							var baseIndex = baseRow ? baseRow.rowIndex -1 : 0;
							var currentIndex = row.rowIndex - 1;
							var rangeMin = Math.min(baseIndex, currentIndex);

							var rangeMax = Math.max(baseIndex, currentIndex);
							var rows = this.$("tr");

							return [ rangeMin , rangeMax ];
						}
					}
			};
			$(function() {
				
				$("#bind-milestone-dialog-tabs").tabs({
					cache : true,
					active: 0
				});
				
				$("#binded-milestone-table").squashTable({"bServerSide":false}, squashSettings);
				$("#global-milestone-table").squashTable({sAjaxSource:config.urls.globalMilestones, "bServerSide":false},{});
				$("#personal-milestone-table").squashTable({sAjaxSource:config.urls.myMilestones, "bServerSide":false},{});
				$("#other-milestone-table").squashTable({sAjaxSource:config.urls.otherMilestones, "bServerSide":false},{});
			
			
				var table = $("#binded-milestone-table").squashTable();
				
				table.on('order.dt search.dt', function () {
			
					 $.each(table.fnGetNodes(), function(index, cell){
						 cell.firstChild.innerHTML = index + 1;
					 });
				});
						
			});

			function refreshAllTables(){
				$("#binded-milestone-table").squashTable()._fnAjaxUpdate();
				$("#global-milestone-table").squashTable()._fnAjaxUpdate();
				$("#personal-milestone-table").squashTable()._fnAjaxUpdate();
				$("#other-milestone-table").squashTable()._fnAjaxUpdate();
			}
			
			
			//Check functions
			function uncheck(table) {
				table.find(":checkbox").prop('checked', false);
			}
			
			function checkAll(table) {
				table.find(":checkbox").prop('checked', true);
			}

			function invertCheck(table) {
				var checked = table.find(":checkbox").filter(":checked");
				var unchecked = table.find(":checkbox").filter(":not(:checked)");
				checked.each(function() {
					$(this).prop('checked', false);
				});
				unchecked.each(function() {
					$(this).prop('checked', true);
				});
			}
			
			//Add check/unchuck functions to panel
			var initButtons = function initButtons(table){
				table.find(".checkAll").on('click', function(){checkAll(table);});
				table.find(".uncheckAll").on('click', function(){uncheck(table);});
				table.find(".invertSelect").on('click', function(){invertCheck(table);});
			};
		
			var pers = $("#personal-milestone");
			var other = $("#other-milestone");
			var global = $("#global-milestone");
			
			initButtons(pers);	
			initButtons(other);	
			initButtons(global);	
			
			//PANEL show/hide
			var globalPanel = $("#global-milestone-panel");
			var persPanel = $("#personal-milestone-panel");
			var otherPanel = $("#other-milestone-panel");
			
			/*
			var showGlobalPanel = function () {
				global.attr("hidden", false);
				pers.attr("hidden", true);
				other.attr("hidden", true);
			};
			
			var showPersPanel = function () {
				global.attr("hidden", true);
				pers.attr("hidden", false);
				other.attr("hidden", true);
			};
			
			var showOtherPanel = function () {
				global.attr("hidden", true);
				pers.attr("hidden", true);
				other.attr("hidden", false);
			};
			
			globalPanel.on('click', function(){ showGlobalPanel();});
			persPanel.on('click', function(){ showPersPanel();});
			otherPanel.on('click',function(){  showOtherPanel();});
			*/
		
			// Get ids from all panels
			function getAllCheckedIds(){
				var tableGlobal = $("#global-milestone-table");
				var tablePersonal = $("#personal-milestone-table");
				var tableOther = $("#other-milestone-table");
				
				var idsGlobal = getCheckedId(tableGlobal);
				var idsPersonal = getCheckedId(tablePersonal);
				var idsOther =  getCheckedId(tableOther);
				var total = [];
		
				Array.prototype.push.apply(total, idsGlobal);
				Array.prototype.push.apply(total, idsPersonal);
				Array.prototype.push.apply(total, idsOther);
			
				return total;
			}

			function getCheckedId(table) {
				table.find(":checkbox:checked").parent("td").parent("tr").addClass(
						'ui-state-row-selected');
				var ids = table.squashTable().getSelectedIds();
				table.squashTable().deselectRows();
				return ids;
			}	
			
			//config date picker
			var dateSettings = confman.getStdDatepicker(); 
			$("#add-milestone-end-date").editable(function(value){				
				$("#add-milestone-end-date").text(value);
			},{
				type : 'datepicker',
				datepicker : dateSettings,
				name : "value"
	
			});
			
			function emptyDatePicker(){
				$("#add-milestone-end-date").text(translator.get("milestone.chooseDate"));
			}

			
				//config RTE
			this.$textAreas = $("textarea");
				function decorateArea() {
					$(this).ckeditor(function() {
					}, {
						customConfig : squashtm.app.contextRoot + "/styles/ckeditor/ckeditor-config.js",
						language : squashtm.app.ckeditorLanguage
					});
				}
				this.$textAreas.each(decorateArea);	
				
			
			//Bind milestone
			$("#bind-milestone-button").on('click', function() {
				bindMilestoneDialog.formDialog('open');
			});

			var bindMilestoneDialog = $("#bind-milestone-dialog");

			bindMilestoneDialog.formDialog();

			bindMilestoneDialog.on('formdialogcancel', function() {
				bindMilestoneDialog.formDialog('close');
			});
			
			bindMilestoneDialog.on('formdialogconfirm', function() {
				var ids = getAllCheckedIds();
				var url = routing.buildURL('milestone.bind-milestones-to-project', config.data.project.id); 
				
				if ( ids.length === 0 ) {
					bindMilestoneDialog.formDialog('close');
				}
				else {
				$.ajax({
					url : url,
					type : 'POST',
					data : {Ids : ids}
				}).success(function() {
					refreshAllTables();
					bindMilestoneDialog.formDialog('close');
				});	
				}
			});
			
			
			$("#unbind-milestone-popup").confirmDialog().on("confirmdialogopen", function(){
				var $this = $(this);
				var id = $this.data('entity-id');
				var ids = ! $.isArray(id) ? [id] : id ;
				var table =  $("#binded-milestone-table").squashTable();
				
			
			
				// if the status don't allow unbind kill the popup and show another popup
				if (oneStatusIsNotEditable(table, ids)){	
					$("#unbind-milestone-popup").confirmDialog("close");
					displayStatusForbidUnbind();
				}	

				if (config.data.project.isTemplate){
				
					unbindMilestone(ids);
					$("#unbind-milestone-popup").confirmDialog("close");
				}
			
			});
			
			var translatedStatus = translator.get({
				locked :"milestone.status.LOCKED"		
			});
			
			function oneStatusIsNotEditable(table, ids){
				
				for(var i= 0; i < ids.length; i++)
				{
				var curStat = table.getDataById(ids[i])["status"]; 
				if (curStat == translatedStatus.locked){
					return true;
				}	
				}
				return false;

			}
			
			function displayStatusForbidUnbind(){
				var warn = translator.get({
					errorTitle : 'popup.title.Info',
					errorMessage : 'dialog.milestone.unbind.statusforbid'
				});
				$.squash.openMessage(warn.errorTitle, warn.errorMessage);
			}
			
			
			
			
			
			//Unbind milestone
			$("#unbind-milestone-popup").confirmDialog().on('confirmdialogconfirm', function(){
						
						var $this = $(this);
						var id = $this.data('entity-id');
						var ids = ( !! id) ? [id] : id ;
						setDefaultWarnMessage();
						unbindMilestone(ids);
						
					});
			
			var unbindMilestone = function unbindMilestone(ids) {
				var url = routing.buildURL('milestone.bind-milestones-to-project', config.data.project.id) + "/" + ids.join(',');

				$.ajax({
					url : url,
					type : 'delete'
				})
				.done(function(){
					refreshAllTables();
				});	
			};
			

			$("#unbind-milestone-popup").confirmDialog().on('confirmdialogcancel', function(){
			setDefaultWarnMessage();
			});
			
			
			var warn = translator.get({
				errorSingle : 'dialog.milestone.unbind.milestone.warning.single',
				errorMulti : 'dialog.milestone.unbind.milestone.warning.multi'
			});
			
			function setDefaultWarnMessage(){
				$("#warning-unbind-message").text(warn.errorSingle);	
			}
			
			$("#unbind-milestone-button").on('click', function(){
				var ids = $("#binded-milestone-table").squashTable().getSelectedIds();

				//$("#warning-unbind-message").text(warn.errorMulti);
				
				if (ids.length>0){
					var popup = $("#unbind-milestone-popup");
					popup.data('entity-id', ids);
					popup.confirmDialog('open');
				}
				else{
					displayNothingSelected();
				}
			});
			
			function displayNothingSelected(){
				var warn = translator.get({
					errorTitle : 'popup.title.Info',
					errorMessage : 'message.EmptyTableSelection'
				});
				$.squash.openMessage(warn.errorTitle, warn.errorMessage);
			}
					
			//create and bind milestone to the project
			$("#create-and-bind-milestone-button").on('click', function() {
				emptyDatePicker();
				createAndBindMilestoneDialog.formDialog('open');
			});

			var createAndBindMilestoneDialog = $("#create-and-bind-milestone-dialog");

			createAndBindMilestoneDialog.formDialog();

			createAndBindMilestoneDialog.on('formdialogcancel', function() {				
				createAndBindMilestoneDialog.formDialog('close');
			});			
					
		   function getPostDate(localizedDate){
				try{
				var postDateFormat = $.datepicker.ATOM;   
				var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
				var postDate = $.datepicker.formatDate(postDateFormat, date);
				return postDate;
				} catch(err){ return null;}
			}

			//created, bind to project and bind to all object in that project
			createAndBindMilestoneDialog.on('formdialogbind', function(){
				createMilestone(bindMilestoneToProjectAndAllObject, "IN_PROGRESS");  
			});
			
			   //create and bind to project
			createAndBindMilestoneDialog.on('formdialogconfirm', function() {	
				createMilestone(bindMilestoneToProject, "PLANNED");
			});
			
			var bindMilestoneToProject = function bindMilestoneToProject(milestoneId){
				var id = [milestoneId];		
				var urlBind = routing.buildURL('milestone.bind-milestones-to-project', config.data.project.id); 
				$.ajax({
					url : urlBind,
					type : 'POST',
					data : {Ids : id}
				}).success(function() {
					refreshAllTables();
					createAndBindMilestoneDialog.formDialog('close');
				});	
			};
			
			var bindMilestoneToProjectAndAllObject = function bindMilestoneToProjectAndAllObject(milestoneId){
				var id = [milestoneId];		
				var urlBind = routing.buildURL('milestone.bind-milestones-to-project', config.data.project.id); 
				$.ajax({
					url : urlBind,
					type : 'POST',
					data : {Ids : id, bindObjects: ""}
				}).success(function() {
					refreshAllTables();
					createAndBindMilestoneDialog.formDialog('close');
				});	
			};
			
			function createMilestone(callback, milestone_status){
				var urlCreate = routing.buildURL('administration.milestones');
				var params = {
					label: $( '#add-milestone-label' ).val().trim(),
					status: milestone_status,
					endDate: getPostDate($( '#add-milestone-end-date').text()),
					description: $( '#add-milestone-description' ).val()
				};
				
				// issue 4691 : clear the error messages
				createAndBindMilestoneDialog.find('.error-message').text('');
				
				$.ajax({
					url : urlCreate,
					type : 'POST',
					dataType : 'json',
					data : params				
				}).success(function(milestoneId){
					callback(milestoneId);});
			}	
			
			
			
		});

