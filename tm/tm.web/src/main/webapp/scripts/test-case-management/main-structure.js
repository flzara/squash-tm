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
define(["jquery",
        "app/pubsub",
		"squash.basicwidgets",
		"contextual-content-handlers",
		"jquery.squash.fragmenttabs",
		"workspace.event-bus",
		"workspace.routing",
		"milestones/milestone-panel",
		"milestones/entity-milestone-count-notifier",
		"jqueryui",
		"jquery.squash.formdialog"],
		function($, pubsub, basic, contentHandlers, Frag, eventBus, routing, milestonePanel, milestoneNotifier){

	
	function initRenameDialog(settings){

		var identity = { resid : settings.testCaseId, restype : "test-cases"  },
			url = settings.urls.testCaseUrl,
			dialog = $("#rename-test-case-dialog");


		dialog.formDialog();

		
		$("#rename-test-case-button").on('click', function(){
			dialog.formDialog('open');
		});

		
		dialog.on( "formdialogopen", function(event, ui) {
			var hiddenRawName = $('#test-case-raw-name');
			var name = $.trim(hiddenRawName.text());
			$("#rename-test-case-input").val(name);
		});
		

		dialog.on('formdialogconfirm', function(){

			var newName = $("#rename-test-case-input").val();

			$.ajax({
				url : url,
				type : "POST",
				dataType : "json",
				data : { 'newName' : newName}
			}).success(function(){
				dialog.formDialog('close');
				eventBus.trigger('node.rename', { identity : identity, newName : newName});
			});

		});

		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});

	}
	
	function initNewVersionDialog(settings){
		
		var url = settings.urls.testCaseUrl+'/new-version';
		
		var dialog = $("#create-test-case-version-dialog").formDialog();
		
		dialog.on('formdialogopen', function(){
			
			dialog.formDialog('setState', 'wait');
			
			$.ajax({
				url : url,
				dataType : 'json'
			})
			.success(function(json){
				var name = json.name,
					ref = json.ref,
					description = json.description;
				
				var fullname = name;
				if (!! settings.milestone){
					fullname+='-'+settings.milestone.label;
				}
				
				$("#new-version-test-case-name").val(fullname);		
				$("#new-version-test-case-reference").val(ref);
				CKEDITOR.instances['new-version-test-case-description'].setData(json.description);
				
				dialog.formDialog('setState','confirm');
			});
			
		});
		
		dialog.on('formdialogconfirm', function(){
			
			var params = {
				name : dialog.find('#new-version-test-case-name').val(),
				reference : dialog.find('#new-version-test-case-reference').val(),
				description : dialog.find('#new-version-test-case-description').val()
			};
			
			$.ajax({
				url : url,
				type : 'post',
				data : JSON.stringify(params),
				contentType : 'application/json',
				dataType : 'json'
			})
			.success(function(jsonTestCase){
				dialog.formDialog('close');
				eventBus.trigger('test-case.new-version', jsonTestCase);
			});
			
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
	}

	function initRenameListener(settings){

		var nameHandler = contentHandlers.getNameAndReferenceHandler();
		nameHandler.identity = { resid : settings.testCaseId, restype : "test-cases" };
		nameHandler.nameDisplay = "#test-case-name";
		nameHandler.nameHidden = "#test-case-raw-name";
		nameHandler.referenceHidden = "#test-case-raw-reference";

	}

	function initFragmentTab(){

		var fragConf = {
			/*cookie : {
				name : "testcase-tab-cookie",
				path : routing.buildURL('testcases.base')
			}*/
		};
		Frag.init(fragConf);
	}

	function initButtons(settings){
		
		$("#print-test-case-button").on('click', function(){
			window.open(settings.urls.testCaseUrl+"?format=printable", "_blank");
		});
		
		$("#create-test-case-version-button").on('click', function(){
			$("#create-test-case-version-dialog").formDialog('open');
		});
	}

	
	function initMilestonesCountNotifier(settings){
		milestoneNotifier.newHandler({
			restype : 'test-cases',
			resid : settings.testCaseId
		});
	}

	// defines which actions should be made when a test step has been edited
	// in full screen page : reload the verified requirements table and the 
	// steps table
	function initReloadSteps(settings){	
		squashtm.app.reloadSteps = function() { 
			var reqTable = $("#verified-requirements-table"),
				stepTable = $("#test-steps-table-"+settings.testCaseId);
			
			reqTable.squashTable().refresh();
			
			// the steps table might not be present yet, hence the test 
			if (stepTable.length>0){
				stepTable.squashTable().refresh(); 
			}
		};
	}
	
	function init(settings){
		basic.init();
		initButtons(settings);
		initMilestonesCountNotifier(settings);
		initRenameDialog(settings);
		initRenameListener(settings);
		initNewVersionDialog(settings);
		initFragmentTab();
		initReloadSteps(settings);
	}

	return {
		init : init
	};

});