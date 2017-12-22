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
define(['module',
        'app/pubsub',
        'workspace.routing',
        'jquery',
        'squash.basicwidgets',
        'page-components/step-information-panel',
        'workspace.event-bus',
        'squash.translator',
        'app/util/ComponentUtil',
        'app/util/ButtonUtil',
        "jquery.squash.oneshotdialog" ,
        'custom-field-values',
        'app/ws/squashtm.notification',
        "file-upload",
        'bugtracker/bugtracker-panel',
        'jquery.squash'],
         function(module, pubsub, routing, $, basicwidg, infopanel, eventBus,
        		 translator,
        		 ComponentUtil, buttonUtil, oneshot, cufValues, notification,
        		 upload, bugtracker) {

	// ************* event subscription ****************

	pubsub.subscribe("reload.executedialog.toolbar", initToolbar);

	pubsub.subscribe("reload.executedialog.cufs", initCufs);

	pubsub.subscribe("reload.executedialog.attachments", initAttachments);

	pubsub.subscribe("reload.executedialog.issues", initBugtracker);

	pubsub.subscribe("reload.executedialog.complete", initComplete);


	// ****************** library code *****************

	function initToolbar(){

		var config = module.config();


		var cbox = $("#execution-status-combo");
		if (cbox.length>0){
			ComponentUtil.updateStatusCboxIcon(cbox);
		}

		// 6796 : cbox too small with Firefox and IE11
		var ua = navigator.userAgent.toLowerCase();
    var ie = ua.indexOf('trident');
    var firefox = ua.indexOf('firefox');
    var chrome = ua.indexOf('chrome');
    var opera = ua.indexOf('opr');
		if (firefox > -1 || ie > -1) {
			cbox.css("height", "20px");
		}
		// to center the cbox for Firefox
		if (firefox > -1) {
			cbox.css("position", "relative").css("top", "3px");
		}
		// differences of 1px between navigators for "previous" and "next" buttons div...
		if (firefox > -1 || chrome > -1) {
			$("#execution-previous-next").css("top", "-3px");
		}
		if (opera > -1) {
			$("#execution-previous-next").css("top", "-1px");
		}

		var lang = translator.get({
			infoTitle : 'popup.title.info',
			singleComplete : 'execute.alert.test.complete',
			suiteComplete : 'squashtm.action.exception.testsuite.end'
		});


		function refreshParent(){
			if (!!window.opener) {
				var openerSize = Object.keys(window.opener).length;
	      if (openerSize > 0 && window.opener.squashtm.execution){
					window.opener.squashtm.execution.refresh();
					if (!!window.opener.config && window.opener.config.identity.restype === "test-suites") {
						window.opener.squashtm.execution.refreshTestSuiteInfo();
					}
				}
				if (openerSize > 0 && window.opener.progressWindow) {
					window.opener.progressWindow.close();
				}
	    }
		}


		function postStatus(status){
			var xhr = $.ajax({
				url : config.urls.executeStatus,
				type : 'post',
				data : {executionStatus : status }
			});

			xhr.success(function(){
				infopanel.refresh();

				refreshParent();

				var cbox = $("#execution-status-combo");
				cbox.val(status);
				ComponentUtil.updateStatusCboxIcon(cbox);

			});

			return xhr;
		}

		function statusButtonClick(evt){
			var button = $(evt.currentTarget);
			var status = button.data('status');
			postStatus(status).success(navigateNext);
		}

		function navigateNext(){
			if (config.basic.hasNextStep) {
				document.location.href= config.urls.executeNext;
			} else {
				if (config.basic.mode === "single-mode") {
					oneshot.show(lang.infoTitle, lang.singleComplete).done(function() {
						window.close();
					});
				} else if (config.basic.hasNextTestCase) {
					$('#execute-next-test-case').click();
				} else {
					oneshot.show(lang.infoTitle, lang.suiteComplete).done(function() {
						window.close();
					});
				}
			}
		}

		function navigatePrevious(){
			document.location.href= config.urls.executePrevious;
		}

		// buttons :
		if (config.permissions.editable){
			$(".control-button").on('click', function(){
				$("#execution-comment-panel").find("button[type=submit]").click();
			});
			$(".status-button").on('click', statusButtonClick);

		}


		$("#edit-tc").on('click', function(){
			var url = routing.buildURL('teststeps.fromExec', config.basic.stepId, false);
			localStorage.setItem("squashtm.execModification.index", config.basic.index);
			var newWindow = window.open(url);
			newWindow.opener = window.opener;
			window.close();
		});

		$("#execute-next-button").on('click', navigateNext);

		$("#execute-previous-button").on('click', navigatePrevious );

		$("#execute-stop-button").on('click', function(){
			window.close();
		});


		// combo :

		if (config.permissions.editable){
			cbox.change(function(){
				postStatus(cbox.val());
			});
		}

		// the window
		$(window).unload( refreshParent );

	}

	function initCufs(){
		var config = module.config();

		if (config.basic.hasCufs){
			var cufurl =  routing.buildURL('customfield.values.get', config.basic.id, 'EXECUTION_STEP'),
				mode = (config.permissions.editable) ? 'jeditable' : 'static';
			$.getJSON(cufurl)
			.success(function(jsonCufs){
				cufValues.infoSupport.init("#cuf-information-table", jsonCufs, mode);
			});
		}

		if (config.basic.hasDenormCufs){
			var denocufurl =  routing.buildURL('denormalizefield.values.get', config.basic.id, 'EXECUTION_STEP'),
				mode = (config.permissions.editable) ? 'jeditable' : 'static';
			$.getJSON(denocufurl)
			.success(function(denojsonCufs){
				cufValues.infoSupport.init("#dfv-information-table", denojsonCufs, mode);
			});
		}
	}

	function initAttachments(){

		var config = module.config();

		// attachments
		upload.initAttachmentsBloc({
			baseURL : config.urls.attachments,
			workspace : "campaign"
		});


	}

	function initBugtracker(){
		var config = module.config();

		if (config.basic.hasBugtracker){

			var conf = {
				url : config.urls.bugtracker
			};
			bugtracker.setBugtrackerMode(config.basic.bugtrackerMode);
			bugtracker.load(conf);

			//issue 3083 : propagate the information to the parent context
			eventBus.onContextual('context.bug-reported', function(event, json){
				window.opener.squashtm.workspace.eventBus.trigger(event, json );
			});

		}

	}

	function initComplete(){

		basicwidg.init();

		notification.init();

		//issue #2069
		$.noBackspaceNavigation();

	}


});
