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
define([ 'module', "info-list/info-list-modification-information-view", "info-list/info-list-modification-table-view",  "jquery", "backbone", "underscore", "squash.basicwidgets", "jeditable.simpleJEditable",
		"workspace.routing", "squash.translator", "jquery.squash.togglepanel", "squashtable", "app/ws/squashtm.workspace"],
		function(module, InfoView, TableView  , $, backbone, _, basic, SimpleJEditable, routing, translator) {
	"use strict";

	var config = module.config();

	translator.load([
	"dialog.delete.info-list.used.message",
	"dialog.info-list.warning.reindex.before",
	"dialog.delete.info-list.unused.message"]);

	var infoListModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {
			this.basicInit();
			this.config = config;
			this.configureRenameInfoListPopup();
			this.configureDeleteInfoListPopup();

			var infoView =	new InfoView(config);
			var tableView =	new TableView(config);

		},
		basicInit : function() {
			basic.init();

		},

		events : {
			"click #rename-info-list-button" : "renameInfoListPopup",
			"click #delete-info-list-button" : "deleteInfoListPopup"
			},

		renameInfoListPopup : function(){
			var self = this;
			self.RenameInfoListPopup.formDialog("open");

			var listName = $("#info-list-name-header").text();
			$("#rename-popup-info-list-label").val(listName);
		},

		deleteInfoListPopup : function(){
			var self = this;
			var message = $("#delete-info-list-warning");

			$.ajax({
				type : 'GET',
				url : routing.buildURL("info-list.isUsed", self.config.data.infoList.id)
			}).done(function(data) {
				if (data === true){
					message.text(translator.get("dialog.delete.info-list.used.message"));
					reindexWarn.text(translator.get("dialog.info-list.warning.reindex.before"));
				}
				else {
					message.text(translator.get("dialog.delete.info-list.unused.message"));
					reindexWarn.text("");
				}

			});
			self.DeleteInfoListPopup.formDialog("open");
		},
		configureRenameInfoListPopup : function(){
			var self = this;

			var dialog = $("#rename-info-list-popup");
			this.RenameInfoListPopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function(){
				self.renameInfoList.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);

		},
		configureDeleteInfoListPopup : function(){
			var self = this;

			var dialog = $("#delete-info-list-popup");
			this.DeleteInfoListPopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function(){
				self.deleteInfoList.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);
		},
		renameInfoList : function(){
			var self = this;
			var newName = self.RenameInfoListPopup.find("#rename-popup-info-list-label").val();

			$.ajax({
				type : 'POST',
				data : {
					id:'info-list-label',
					'value' : newName
				},
				url : routing.buildURL("info-list.info", self.config.data.infoList.id)
			}).done(function(data) {
				self.$("#info-list-name-header").text(data);
				self.$("#info-list-label").text(data);
				self.RenameInfoListPopup.formDialog('close');
			});


		},
		deleteInfoList : function(){
			$.ajax({
				type : 'DELETE',
				url : routing.buildURL("info-list.info", config.data.infoList.id)
			}).done(function(data) {
				document.location.href = squashtm.app.contextRoot + "/administration/info-lists/";
			});

		},
		closePopup : function() {
			$(this).formDialog('close');
		}


	});
	return infoListModificationView;

});
