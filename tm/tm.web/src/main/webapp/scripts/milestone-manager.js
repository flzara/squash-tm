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
require(["common"], function(){
	require(["app/pubsub", "app/squash.wreqr.init", "jquery", "squash.translator", "workspace.routing","squash.configmanager","squash.dateutils", "milestone-manager/MilestoneFeatureSwitch",
	         "milestone-manager/milestone-activation", "jeditable.datepicker",  "squashtable", "app/ws/squashtm.workspace", "jquery.squash.formdialog", "jquery.squash.confirmdialog"],
			function(ps, squashtm, $, translator, routing, confman, dateutils, MilestoneFeatureSwitch, MilestoneActivation){
		"use strict";

		var trans = translator.get({
			rangeGlobal : "milestone.range.GLOBAL",
			statusPlanned : "milestone.status.PLANNED",
			unionStatusForbid : "message.milestone.synchronize.warn.union.status-forbid",
			unionRangeForbid : "message.milestone.synchronize.warn.union.range-forbid",
			statusForbid : "message.milestone.synchronize.warn.status-forbid",
			rangeForbid : "message.milestone.synchronize.warn.range-forbid",
			chooseDate : "milestone.chooseDate"
			});

		function getPostDate(localizedDate) {
			try {
				var postDateFormat = $.datepicker.ATOM;
				var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
				var postDate = $.datepicker.formatDate(postDateFormat, date);
				return postDate;

			} catch (err) {
				return null;
			}
		}

		function setActionsEnabled(enabled) {
			$(".milestone-dep").prop("disabled", !enabled);
		}

		function getFormValues(){
			return	{
					label: $( '#add-milestone-label' ).val().trim(),
					status: $( '#add-milestone-status' ).val(),
					endDate: getPostDate($( '#add-milestone-end-date' ).text()),
					description: $( '#add-milestone-description' ).val()
			}
		}

	ps.subscribe("loaded.milestoneFeatureSwitch", function() {
		console.log("loaded.milestoneFeatureSwitch");
		new MilestoneFeatureSwitch();
	});

	squashtm.vent.on("milestonefeatureswitch:activating milestonefeatureswitch:deactivating", function(event) {
		// prevents performing ops on milestones while the feature state is being changed
		setActionsEnabled(false);
	});

	squashtm.vent.on("milestonefeatureswitch:activated", function(event) {
		setActionsEnabled(true);
	});
	squashtm.vent.on("milestonefeatureswitch:deactivated", function(event) {
		setActionsEnabled(false);
		MilestoneActivation.deleteCookie();
		$table()._fnAjaxUpdate();
	});

	function $table() {
		return $("#milestones-table").squashTable();
	}

	$(function() {
		console.log("domready");
		// squashtm cannot be read before because it might be inited after this module is loaded
		var config = squashtm.milestoneManager;

		function canEditMilestone(id){
			return _.contains(config.data.editableMilestoneIds, id);
		}

		function canEditAllMilestones(ids){
			for (var i = 0; i < ids.length; i++){
				if (!canEditMilestone(ids[i])){
					return false;
				}
			}
			return true;
		}


		function selectionHasProjectBinded(ids){
			for (var i = 0; i < ids.length; i++){
				if ($table().getDataById(ids[i]).nbOfProjects > 0 ) {
					return true;
				}
			}
			return false;
		}

		// When you open the dialog, change the message in it (with or without associated project)
		$("#delete-milestone-popup").confirmDialog().on('confirmdialogopen', function(){
			var ids = $table().getSelectedIds();
			if (!canEditAllMilestones(ids)){
				$("#delete-milestone-popup").confirmDialog("close");
				warningWithTranslation("dialog.delete-milestone.not-allowed");
			}

		if (ids.length == 1){
			if (!selectionHasProjectBinded(ids)){
					$("#errorMessageDeleteMilestone").text(translator.get("dialog.delete-milestone.message"));
				}
				else {
					$("#errorMessageDeleteMilestone").text(translator.get("dialog.delete-milestone.messageproject"));
				}
			}
			if (ids.length > 1){

				if (!selectionHasProjectBinded(ids)){
					$("#errorMessageDeleteMilestone").text(translator.get("dialog.delete-milestone.messagemulti"));
				}
				else {
					$("#errorMessageDeleteMilestone").text(translator.get("dialog.deleteMilestone.warning.multiple.linked"));
				}
			}
		});

		// When you click, choose dialog if a milestone is selected
		$(document).on("click", "#delete-milestone-button", function() {
			var ids = $table().getSelectedIds();
			if (ids.length>0){
				var popup = $("#delete-milestone-popup");
				popup.data('entity-id', ids);
				popup.confirmDialog('open');
			} else {
				var errorPopup = $("#milestone-noselection-error-dialog").messageDialog();
				errorPopup.messageDialog('open');
			}
		});

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
						},
						drawDeleteButton: function(template, cells){

							$.each(cells, function(index, cell) {
								var row = cell.parentNode; // should be the tr
								var id = milestoneTable.getODataId(row);
								var $cell = $(cell);

								if (_.contains(config.data.editableMilestoneIds, id)){
									$cell.html(template);
									$cell.find('a').button({
										text : false,
										icons : {
											primary : "ui-icon-trash"
										}
									});
								}
							});
						}
					}
			};

			var milestoneTable = $("#milestones-table").squashTable({"bServerSide":false},squashSettings);

			//fix order
			milestoneTable.on('order.dt search.dt', function () {

				 $.each(milestoneTable.fnGetNodes(), function(index, cell){
					 cell.firstChild.innerHTML = index + 1;
				 });
			});

		var dateSettings = confman.getStdDatepicker();

		$("#add-milestone-end-date").editable(function(value){
			$("#add-milestone-end-date").text(value);
	    }, {
			type : 'datepicker',
			datepicker : dateSettings,
			name : "value"
		});

		$("#clone-milestone-end-date").editable(function(value){
			$("#clone-milestone-end-date").text(value);
	    }, {
			type : 'datepicker',
			datepicker : dateSettings,
			name : "value"
		});

		var $textAreas = $("textarea");

		$("#delete-milestone-popup").confirmDialog().on('confirmdialogconfirm', function(){
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = squashtm.app.contextRoot+'/administration/milestones/'+ ids.join(",");
			var table = $table();
			//var selectedRow = table.getRowsByIds(ids);

			$.ajax({
				url : url,
				type : 'delete'
			}).done(function(){
				table._fnAjaxUpdate();
			});

		});

		//Add milestone

	var addMilestoneDialog = $("#add-milestone-dialog");
	addMilestoneDialog.formDialog();

	function formatDate(date) {
		var format = translator.get("squashtm.dateformatShort");
		var formatedDate = dateutils.format(date, format);
		return dateutils.dateExists(formatedDate, format) ? formatedDate :"";
	}

	addMilestoneDialog.on('formdialogopen', function(){
		$( '#add-milestone-end-date' ).text(translator.get("milestone.chooseDate"));
	});

	addMilestoneDialog.on('formdialogconfirm', function(){
		var url = routing.buildURL('administration.milestones');
		var params = getFormValues();
		$.ajax({
			url : url,
			type : 'POST',
			dataType : 'json',
			data : params
		}).success(function(id){
			config.data.editableMilestoneIds.push(id);
			$('#milestones-table').squashTable()._fnAjaxUpdate();
			addMilestoneDialog.formDialog('close');
		});

	});

	addMilestoneDialog.on('formdialogaddanother', function(){
		var url = routing.buildURL('administration.milestones');
		var params = getFormValues();
		$.ajax({
			url : url,
			type : 'POST',
			dataType : 'json',
			data : params
		}).success(function(id){
			config.data.editableMilestoneIds.push(id);
			$('#milestones-table').squashTable()._fnAjaxUpdate();
			addMilestoneDialog.formDialog('cleanup');
			$( '#add-milestone-end-date' ).text(translator.get("milestone.chooseDate"));
		});

	});

	addMilestoneDialog.on('formdialogcancel', function(){
		addMilestoneDialog.formDialog('close');
		});

	$('#new-milestone-button').on('click', function(){
		addMilestoneDialog.formDialog('open');
	});

	//Clone milestone
	var cloneMilestoneDialog = $("#clone-milestone-dialog");

	cloneMilestoneDialog.formDialog();


	$('#clone-milestone-button').on('click', function(){

		var ids = $table().getSelectedIds();
		if (ids.length>1){
			warningWithTranslation ('message.milestone.cantclonemultiple');
		} else if (ids.length == 1) {

			var mil = $table().getDataById(ids[0]);

			if (mil.status != trans.statusPlanned){
			cloneMilestoneDialog.data('entity-id', ids);
			cloneMilestoneDialog.formDialog('open');
			} else {
				warningWithTranslation('message.milestone.invalidclonestatus');
			}


		} else {
			warningWithTranslation ('message.milestone.cantclonenothing');
		}
	});


	function warningWithTranslation(errorKey){
		var warn = translator.get({
			errorTitle : 'popup.title.Info',
			errorMessage : errorKey
		});
		$.squash.openMessage(warn.errorTitle, warn.errorMessage);
	}

	cloneMilestoneDialog.on('formdialogopen', function(){
		$( '#clone-milestone-end-date' ).text(translator.get("milestone.chooseDate"));
	});

	cloneMilestoneDialog.on('formdialogcancel', function(){
		cloneMilestoneDialog.formDialog('close');
		});

	cloneMilestoneDialog.on('formdialogconfirm', function(){
		var $this = $(this);
		var motherId  = $this.data('entity-id');
		var url = routing.buildURL('administration.milestones.clone', motherId);
		var params = {
				label: $( '#clone-milestone-label' ).val().trim(),
				status: $( '#clone-milestone-status' ).val(),
				endDate: getPostDate($( '#clone-milestone-end-date' ).text()),
				description: $( '#clone-milestone-description' ).val(),
				bindToRequirements : cloneMilestoneDialog.find("input:checkbox[name='bindToRequirements']").prop("checked"),
				bindToTestCases : cloneMilestoneDialog.find("input:checkbox[name='bindToTestCases']").prop("checked")

			};
		$.ajax({
			url : url,
			type : 'POST',
			data : params
		}).success(function(id){
			config.data.editableMilestoneIds.push(id);
			$('#milestones-table').squashTable()._fnAjaxUpdate();
			cloneMilestoneDialog.formDialog('close');
		});
		});


	var uncheckCloneParam = function() {
		cloneMilestoneDialog.find(":checkbox").prop('checked', false);
	};
	var checkAllCloneParam = function() {
		cloneMilestoneDialog.find(":checkbox").prop('checked', true);
	};

	$("#checkAll").on('click', checkAllCloneParam);
	$("#uncheckAll").on('click', uncheckCloneParam);

	//Synchronize
	$("#synchronize-milestone-button").on('click', function(){
		var table = $table();
		var ids = table.getSelectedIds();
		//BEWARE lot's of check incoming
		if (ids.length < 2) {
			//error can't select less than 2
			warningWithTranslation('message.milestone.synchronize.notenought');
		} else if (ids.length > 2){
			//error can't select more than 2
			warningWithTranslation('message.milestone.synchronize.toomuch');
		} else {
			//maybe it's ok... let's see
			var mil1 = table.getDataById(ids[0]);
			var mil2 = table.getDataById(ids[1]);
			synchronizeMilestoneDialog.data('mil1', mil1);
			synchronizeMilestoneDialog.data('mil2', mil2);

			if (mil1.status != trans.statusPlanned && mil2.status != trans.statusPlanned && (mil1.bindableToObject || mil2.bindableToObject)){
				// you need at least one milestone bindable to object to synchronize
				//and no planned milestone

				if (config.data.isAdmin){
					//ok you're admin you can skip some additional check
					configAdminSynchroPopup();
					checkFirstRadio();
					synchronizeMilestoneDialog.formDialog('open');

				} else {
					//too bad you're not admin, you have to pass some more check...
					if (mil1.range == trans.rangeGlobal && mil2.range == trans.rangeGlobal){
						//you loose you're not admin and want to synchronize 2 global milestone
						warningWithTranslation('message.milestone.synchronize.wrongrange');
					} else if (mil1.range == trans.rangeGlobal && !mil2.bindableToObject || mil2.range == trans.rangeGlobal && !mil1.bindableToObject ) {
						//you have selected one global and a restricted non bindable to object milestone...too bad you loose again !
						warningWithTranslation('message.milestone.synchronize.wrongstatus');
					} else {
						//You're still here ?? ok you can now have your pop up !
						configNonAdminSynchroPopup();
						checkFirstRadio();
						allowPerimeterOrNot();
						synchronizeMilestoneDialog.formDialog('open');

					}
				}

			} else {
				// 2 milestone not bindable to object, you loose again
				// or 1 of the milestone is PLANNED...so you loose (unfair isn't it ?)
				warningWithTranslation('message.milestone.synchronize.wrongstatus');
			}

		}

		function configAdminSynchroPopup(){
			var mil1 = synchronizeMilestoneDialog.data('mil1');
			var mil2 = synchronizeMilestoneDialog.data('mil2');
			$("#mil1").attr("disabled", !mil1.bindableToObject);
			$("#mil2").attr("disabled", !mil2.bindableToObject);
			$("#union").attr("disabled", !mil1.bindableToObject || !mil2.bindableToObject);
			$("#perim").attr("disabled", true);
			$("#perim").hide();
			$("#perimtxt").hide();

			writeMilestonesLabel();
			writeMilestonesWarning(mil1, mil2, true);


		}

		function configNonAdminSynchroPopup(){
			var mil1 = synchronizeMilestoneDialog.data('mil1');
			var mil2 = synchronizeMilestoneDialog.data('mil2');
			var mil1CantBeTarget = !mil1.bindableToObject ||  mil1.range == trans.rangeGlobal;
			var mil2CantBeTarget = !mil2.bindableToObject ||  mil2.range == trans.rangeGlobal;
			$("#mil1").attr("disabled", mil1CantBeTarget);
			$("#mil2").attr("disabled", mil2CantBeTarget);
			$("#union").attr("disabled", mil1CantBeTarget || mil2CantBeTarget);
			writeMilestonesLabel();
			writeMilestonesWarning(mil1, mil2, false);

		}

		function writeMilestonesWarning(mil1, mil2, isAdmin){

			$("#mil1warn").text("");
			$("#mil2warn").text("");
			$("#unionwarn").text("");

	    	milestoneWarn(mil1, $("#mil1Label") , $("#mil1warn"), isAdmin);
	    	milestoneWarn(mil2, $("#mil2Label"), $("#mil2warn"), isAdmin);

		}

		function milestoneWarn(milestone, $milestone, $text, isAdmin){
		 	if (!isAdmin && milestone.range == trans.rangeGlobal){
		 		$milestone.text($milestone.text().split("(")[0]);
		 		$text.text(trans.rangeForbid);
		 		$("#unionLabel").text($("#unionLabel").text().split("(")[0]);
				$("#unionwarn").text(trans.unionRangeForbid);
			} else if (!milestone.bindableToObject){
				$milestone.text($milestone.text().split("(")[0]);
				$text.text(trans.statusForbid);
				$("#unionwarn").text(trans.unionStatusForbid);
			}
		}

		function writeMilestonesLabel(){
			var msg = translator.get({
				mil:"label.milestone.synchronize.target",
				union:"label.milestone.synchronize.union"
			});


			$("#mil1Label").text(msg.mil.split('"{0}"').join(mil1.label).split('"{1}"').join(mil2.label));
			$("#mil2Label").text(msg.mil.split('"{0}"').join(mil2.label).split('"{1}"').join(mil1.label));
			$("#unionLabel").text(msg.union.split('"{0}"').join(mil1.label).split('"{1}"').join(mil2.label));
			greyTextForDisabledLabel($("#mil1"), $("#mil1Label"));
			greyTextForDisabledLabel($("#mil2"), $("#mil2Label"));
			greyTextForDisabledLabel($("#union"),$("#unionLabel"));

		}

		function greyTextForDisabledLabel( radioButtonSelector,labelSelector){
			var $radioButtonSelector = $(radioButtonSelector);
			var $labelSelector = $(labelSelector);
			if ($radioButtonSelector.attr("disabled")){
				$labelSelector.addClass("nota-bene");
			} else {
				$labelSelector.removeClass("nota-bene");
			}

		}

		$("#mil1").on('change', allowPerimeterOrNot);
		$("#mil2").on('change', allowPerimeterOrNot);
		$("#union").on('change', allowPerimeterOrNot);

		function allowPerimeterOrNot(){
			var mil1 = synchronizeMilestoneDialog.data('mil1');
			var mil2 = synchronizeMilestoneDialog.data('mil2');

			if (!config.data.isAdmin){
				//admin don't have the perimeter checkbox
			$("#perim").attr("disabled", false);

			if ($("#union").prop('checked')){
				$("#perim").attr("disabled", true);
			}

			if ($("#mil1").prop('checked') && config.data.currentUser != mil1.owner){
				$("#perim").attr("disabled", true);
			}

			if ($("#mil2").prop('checked') && config.data.currentUser != mil2.owner){
				$("#perim").attr("disabled", true);
			}

			}
		}

		function checkFirstRadio(){

			if ($("#mil1").attr("disabled")){
				$("#mil2").prop('checked', true);
			} else {
				$("#mil1").prop('checked', true);
			}
		}


	});

	var synchronizeMilestoneDialog = $("#synchronize-milestone-dialog");
	synchronizeMilestoneDialog.formDialog();

	synchronizeMilestoneDialog.on('formdialogcancel', function(){
		synchronizeMilestoneDialog.formDialog('close');
		});

	synchronizeMilestoneDialog.on('formdialogconfirm', function(){
		var mil1 = synchronizeMilestoneDialog.data('mil1');
		var mil2 = synchronizeMilestoneDialog.data('mil2');

		synchronizeMilestoneConfirmDialog.data("mil1",mil1);
		synchronizeMilestoneConfirmDialog.data("mil2",mil2);
		synchronizeMilestoneConfirmDialog.confirmDialog('open');
		synchronizeMilestoneDialog.formDialog('close');
		});

	var synchronizeMilestoneConfirmDialog = $("#synchronize-milestone-dialog-confirm");
	synchronizeMilestoneConfirmDialog.confirmDialog();


	synchronizeMilestoneConfirmDialog.on('confirmdialogconfirm', function(){
		var mil1 = synchronizeMilestoneConfirmDialog.data('mil1');
		var mil2 = synchronizeMilestoneConfirmDialog.data('mil2');

        var source = $("#mil1").prop('checked') ? mil2["entity-id"] : mil1["entity-id"];
        var target = $("#mil1").prop('checked') ? mil1["entity-id"] : mil2["entity-id"];

		$.ajax({
			url : routing.buildURL("milestone.synchronize", source, target),
			type : 'POST',
			data : {extendPerimeter: $("#perim").prop("checked"),
				isUnion:$("#union").prop("checked")}
		});

	});

	});
	});
});
