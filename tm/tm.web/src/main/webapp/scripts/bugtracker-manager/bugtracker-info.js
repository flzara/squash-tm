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
/*
 * Handles the page bugtracker-info.jsp
 */

define(['module', 'jquery', 'backbone', './bugtracker-info-backbone', 'squash.basicwidgets', 'jquery.squash.formdialog'],
		function(module, $, Backbone, CredentialManagerView, basic){

	var conf = module.config();


	$(function(){

		// ************** basic inputs **************
		basic.init();

		// the silly checkbox
		$("#bugtracker-iframeFriendly-checkbx").on('change', function(){
			$.ajax({
				type: 'POST',
				data: {
					'isIframeFriendly' : $("#bugtracker-iframeFriendly-checkbx").is(":checked")
				},
				dataType: 'json',
				url: conf.btUrl
			});
		});


		// **************  deletion button **************
	  	$("#delete-bugtracker-popup").confirmDialog().on('confirmdialogconfirm', function() {
			var url = conf.btUrl;

			$.ajax({
				url : url,
				type : 'DELETE'
			}).success(function () {
		      document.location.href = conf.backUrl
			});
		});

	  	$("#delete-bugtracker-button").on('click', function() {
			var popup = $("#delete-bugtracker-popup");
			popup.confirmDialog('open');
		});


	  	// **************  rename dialog **************
		var renameDialog = $("#rename-bugtracker-dialog");
		renameDialog.formDialog();


		renameDialog.on('formdialogopen', function(){
			var name = $.trim($('#bugtracker-name-header').text());
			$("#rename-bugtracker-input").val($.trim(name));
		});


		renameDialog.on('formdialogconfirm', function(){
			var params = { newName : $("#rename-bugtracker-input").val() };
			var url = conf.btUrl;
			$.ajax({
			  url : url,
			  type : 'POST',
			  dataType : 'json',
			  data : params
			}).success(function(data){
				$('#bugtracker-name-header').html(data.newName);
				renameDialog.formDialog('close');
		    });
		  });


		 renameDialog.on('formdialogcancel', function(){
		 	renameDialog.formDialog('close');
		 });


		 $("#rename-bugtracker-button").on('click', function(){
			 renameDialog.formDialog('open');
		 });


		 // *************** authentication **************
		 var authconfModel = new Backbone.Model(conf.authConf);

		 // we don't update the data using backbone model .save() and to reflect that
		 // we set the url separetely from the model
		 new CredentialManagerView({
			 model : authconfModel,
			 btUrl : conf.btUrl
		 })

	});



});
