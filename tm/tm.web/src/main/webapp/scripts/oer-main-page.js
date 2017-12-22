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
/* TODO move that in a folder */
define(["module",
        "app/pubsub",
        "jquery",
        "squash.resizer",
        "app/oer-library/oer-manager",
        "app/oer-library/jquery.oer-control",
        "jquery.squash.formdialog",
        "jquery.squash.messagedialog"],
		function(module, pubsub, $, resizer, Manager){


	// ************* events subscription *********

	pubsub.subscribe("reload.oer.panelsready", initPanels);

	pubsub.subscribe("reload.oer.control", initControl);

	pubsub.subscribe("reload.oer.urldialog", initUrldialog);

	pubsub.subscribe("reload.oer.genericerrordialog", initErrordialog);

	pubsub.subscribe("reload.oer.complete", initComplete);

	// ************** library code ***************

	function initPanels(){
		resizer.init({
			leftSelector : "#ieo-left-panel",
			rightSelector : "#ieo-right-panel"
		});
	}


	function initControl(){
		$("#ieo-control").ieoControl();
	}

	function initUrldialog(){
		var openurlDialog = $("#open-address-dialog");
		openurlDialog.formDialog();

		openurlDialog.on('formdialogconfirm', function(){
			var url = $('#address-input').val();
			squashtm.ieomanager.fillRightPane(url);
			openurlDialog.formDialog('close');
		});

		$("#open-address-dialog-button").on('click', function(){
			openurlDialog.formDialog('open');
		});
	}

	function initErrordialog(){
		$("#generic-error-dialog").messageDialog();
	}
	function resizeLeftPanel(){
		$("#tree-panel-left").css('width',localStorage.getItem("leftWidth"));
		var pos = parseInt(localStorage.getItem("leftWidth"))+ 10;
		$("#contextual-content").css('left',pos+"px");
	}

	function initComplete(){
		resizeLeftPanel();
		var settings = module.config();
		var manager = new Manager(settings);

		manager.setControl($("#ieo-control"));
		manager.setRightPane($("#ieo-right-panel"));

		squashtm = squashtm ||  {};
		squashtm.ieomanager = manager;
	}

});
