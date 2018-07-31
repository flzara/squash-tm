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
define([ 'module', "jquery", "squash.basicwidgets", "jeditable.selectJEditable", "workspace.routing",
	"squash.translator", "jquery.squash.formdialog" ],
	function(module, $, basic, SelectJEditable, routing, translator) {

	var config = module.config();

	basic.init();

	var changeListDialog = $("#change-list-popup");

	changeListDialog.formDialog();

	var changeListDialogAfter = $("#change-list-popup-after");

	changeListDialogAfter.formDialog();

	changeListDialog.on('formdialogcancel', function() {
		var $this = $(this);
		var selectJEdit = $this.data('selectJEdit');
		$(selectJEdit.component).text(selectJEdit.settings.jeditableSettings.oldValue);
		changeListDialog.formDialog('close');
	});

	changeListDialog.on('formdialogconfirm', function() {

		var $this = $(this);
		var projectId = $this.data('projectId');
		var listId = $this.data('infoListId');
		var infoListType = $this.data('infoListType');
		var url = routing.buildURL('info-list.bind-to-project', projectId, infoListType);
		var selectJEdit = $this.data('selectJEdit');

		$.ajax({
			url : url,
			type : 'POST',
			data : {value:listId}
		}).success(function() {
			selectJEdit.settings.jeditableSettings.oldValue = selectJEdit.component.text();
		});

        changeListDialog.formDialog('close');
        changeListDialogAfter.formDialog('open');
	});

	changeListDialogAfter.on('formdialogcancel', function() {
		changeListDialogAfter.formDialog('close');
	});

	changeListDialogAfter.on('formdialogconfirm', function() {
		document.location.href=  squashtm.app.contextRoot + "administration/indexes";
	});

	var submitFn = function (value, settings, self){
		changeListDialog.data("projectId", config.data.project.id);
		changeListDialog.data("infoListType", settings.infoListType);
		changeListDialog.data("infoListId", value.substring(1)); //remove the _ used to make sure jeditable doesn't reorder our ordered list !
		 changeListDialog.data("selectJEdit", self);
		changeListDialog.formDialog('open');
	    var val = JSON.parse(settings.data);
        return val[value];
	};

		var categoryEditable = new SelectJEditable(
			 {
			target : function(value, settings) {return submitFn(value, settings,categoryEditable);},
			componentId : "info-list-category",
			jeditableSettings : {
				data : config.data.lists.category,
				infoListType: 'category',
				oldValue: $("#info-list-category").text()
			}
		});

		var typeEditable = new SelectJEditable(
					 {
					target : function(value, settings) {return submitFn(value, settings,typeEditable);},
					componentId : "info-list-type",
					jeditableSettings : {
						data : config.data.lists.type,
						infoListType: 'type',
						oldValue: $("#info-list-type").text()
					}
				});

		var natureEditable = new SelectJEditable(
					 {
					target :  function(value, settings) {return submitFn(value, settings,natureEditable);},
					componentId : "info-list-nature",
					jeditableSettings : {
						data : config.data.lists.nature,
						infoListType: 'nature',
						oldValue: $("#info-list-nature").text()
					}
				});

	/* If Project is bound to a Template, infoLists can't be modified. */
	if(!config.data.project.infoListsAreModifiable) {

		displayLockedParameterDialog = function() {
			$.squash.openMessage(
      	translator.get('title.project.lockedParameter'),
        translator.get('message.project.lockedParameter'));
		};

		var categoryList = $("#info-list-category");
		var typeList = $("#info-list-type");
		var natureList = $("#info-list-nature");

		categoryList.editable("disable");
		typeList.editable("disable");
		natureList.editable("disable");

		categoryList.on("click", displayLockedParameterDialog);
		typeList.on("click", displayLockedParameterDialog);
		natureList.on("click", displayLockedParameterDialog);

	}

});
