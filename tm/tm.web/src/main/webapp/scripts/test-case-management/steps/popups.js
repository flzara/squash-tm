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
define(['jquery', 'workspace.event-bus', 'squash.translator','underscore', 'jqueryui', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog' ], 
		function($, eventBus, translator, _) {


	function _initDeleteStep(conf){
		
		var deleteStepDialog = $("#delete-test-step-dialog");
		
		deleteStepDialog.formDialog();
		
		deleteStepDialog.on('formdialogopen', function(){

			var entityId = $("#delete-test-step-dialog").data("entity-id");
			$("#delete-test-step-dialog").data("entity-id", null);
			
			var selIds = [];
						
			if(!entityId){
				selIds = $("#test-steps-table-"+conf.testCaseId).squashTable().getSelectedIds();
			} 
						
			if(!!entityId){
				selIds.push(entityId);
			}
			
			switch (selIds.length){			
				case 0 : $(this).formDialog('setState','empty-selec'); break;
				case 1 : $(this).formDialog('setState','single-tp'); break;
				default : $(this).formDialog('setState','multiple-tp'); break;					
			}
			
			this.selIds = selIds;
		});
		
		deleteStepDialog.on('formdialogconfirm', function(){
			var table = $("#test-steps-table-"+ conf.testCaseId).squashTable();
			var ids = this.selIds;
			var calledStepsDeleted	= stepIdsContainCalledSteps(table, ids);
			var url = conf.urls.testCaseStepsUrl +"/"+ ids.join(',');
			
			$.ajax({
				url : url,
				type : 'delete',
				dataType : 'json'
			})
			.done(function(testStepsSize){
				if(calledStepsDeleted){
					eventBus.trigger("testStepsTable.deletedCallSteps");
				}
				eventBus.trigger("testStepsTable.removedSteps");
				conf.stepsTablePanel.refreshTable();
				if(testStepsSize == "0"){
					eventBus.trigger("testStepsTable.noMoreSteps");
				}
			});
			
			$(this).formDialog('close');
		});
		
		deleteStepDialog.on('formdialogcancel', function(){
			$(this).formDialog('close');
		});
		
	}
	function stepIdsContainCalledSteps(table, ids){
		for(var i in ids){
			var calledId = table.getDataById(ids[i])["called-tc-id"];
			if(calledId){
				return true;
			}
		}
		return false;
	}
	
	
	
	
	// ************************* call step dataset dialog 
	// *************************
	
	function _initCallStepDatasetDialog(conf){
		
		var dialog = $("#pick-call-step-dataset-dialog"),
			table = $("#test-steps-table-"+conf.testCaseId).squashTable();
		
		
		dialog.formDialog();
		
		dialog.on('formdialogopen', function(){
			
			dialog.formDialog('setState', 'loading');
			
			// init variables
			var openerId = dialog.data('opener-id'),
				tblrow = table.getRowsByIds([openerId]),
				rowdata = table.fnGetData(tblrow.get(0)),
				stepInfo = rowdata['call-step-info'],
				thisTcName = $.trim($("#test-case-name").text()); // oooh that's ugly
				
				
			// fetch the dataset available for this test case. We'll then 
			// proceed to the initialization of the popup

			var fetchDatasetsUrl = squashtm.app.contextRoot + '/test-cases/'+stepInfo.calledTcId+'/datasets';
				
			$.getJSON(fetchDatasetsUrl).success(function(json){	
				
				/*
				 * Populate the dataset combo box.
				 * 
				 *  Note that the "none" option is always important, even when no dataset is available
				 *  (ie the span pick-call-step-dataset-nonavailable) : in that later case the select should 
				 *  return the "none" option.
				 *  
				 */
				// add the none option
				var select = $("#pick-call-step-dataset-select");
				select.empty();
				var noneOption = $('<option value="0" selected>'+translator.get('label.None')+'</option>');
				select.append(noneOption);		
				
				// add the datasets fetched from the server
				if (json.length === 0){
					$("#pick-call-step-dataset-nonavailable").show();
					select.hide();
				}
				else{					
					select.show();
					$("#pick-call-step-dataset-nonavailable").hide();
					//issue 6148 ordering dataset
					var sortDataset = _.sortBy(json,'name');
					$.each(sortDataset, function(idx, ds){
						var opt = $('<option value="'+ds.id+'">'+ds.name+'</option>');
						select.append(opt);
					});
				}
				
			
				// create the content of pick-call-step-dataset-consequence
				var spanConsequence = $("#pick-call-step-dataset-consequence"),
					template = spanConsequence.data('template');
				
				var txtConsequence = template.replace('{0}', stepInfo.calledTcName)
											.replace('{1}', thisTcName);
				
				spanConsequence.text(txtConsequence);
				
				// last, we can configure the initial state of the popup according to stepInfo
				switch (stepInfo.paramMode){
				case 'DELEGATE' :
					dialog.find('input[name="param-mode"][value="choice2"]').prop('checked', true);
					select.val(0);
					break;
				case 'CALLED_DATASET' :
					dialog.find('input[name="param-mode"][value="choice1"]').prop('checked', true);
					select.val(stepInfo.calledDatasetId);
					break;
				case 'NOTHING' :
					dialog.find('input[name="param-mode"][value="choice1"]').prop('checked', true);
					select.val(0);
				}
				
				// now we can unveil the popup
				dialog.formDialog('setState', 'main');
				
			});
			
		});

		dialog.on('formdialogconfirm', function(){
			
			var select = dialog.find('select');
			var stepId = dialog.data('opener-id'),
				choice = dialog.find('input[name="param-mode"]:checked').val(),
				mode,
				datasetId,
				dsName;


			if (choice === "choice2"){
				mode="DELEGATE";
				datasetId = null;
				dsName = null;
			}
			else{
				var ds = select.val();
				datasetId = (ds === "0") ? null : ds; 
				mode = (ds === "0") ? 'NOTHING' : 'CALLED_DATASET';
				dsName = (ds === "0") ? null : select.find('option:selected').text();
			}
			
			// now we can post
			var postURL = squashtm.app.contextRoot + '/test-cases/'+conf.testCaseId+'/steps/'+stepId+'/parameter-assignation-mode';
			$.ajax({
				url : postURL,
				type : 'POST', 
				data : { mode : mode, datasetId : datasetId}
			}).success(function(){
				dialog.formDialog('close');
				var evt = jQuery.Event("testStepsTable.changedCallStepParamMode");
				eventBus.trigger(evt, {
					stepId : stepId, 
					mode : mode, 
					datasetId : datasetId,
					datasetName : dsName
				});
			});
		});		
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
		// last hooks for convenience only : clicking a label or select would trigger a click on the radio button next to it
		dialog.on('click', 'label', function(evt){
			$(evt.currentTarget).parent().prev().click();
		});
		
		dialog.on('click', 'select', function(){
			dialog.find('input[name="param-mode"][value="choice1"]').prop('checked', true);
		});
		
	}
	
	
	
	/*
	 * needs :
	 * 
	 * conf.permissions.writable
	 * conf.urls.testCaseStepsUrl
	 * conf.testCaseId
	 * conf.stepsTablePanel
	 */
	return {
		init : function(conf){
			if (conf.permissions.writable){
				_initDeleteStep(conf);
				_initCallStepDatasetDialog(conf);
			}
		}
	};
	
});