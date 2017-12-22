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
define([ "jquery", "backbone", "underscore", "workspace.routing", "squash.translator", "jquery.squash.formdialog" ], 
		function($, Backbone, _, routing, translator) {
	"use strict";
	
	
	var View = Backbone.View.extend({
		el : ".bind-milestone-dialog",
		initialize : function() {

			var self = this;
			this.dialog = this.$el.formDialog({
				autoOpen : false,
				width : 800
			});
			this.initTable();
			this.initBlanketSelectors();

		},
		
		events : {
			"formdialogcancel" : "cancel",
			"formdialogconfirm" : "confirm",
			"formdialogselection" : "selection"
		},
		
		open : function(options){
			this.options = options;
			this.initData();
				

		},
		_afterDataInit : function(){
			var self = this;
			this.initWarning();
			this.updateTable();
			var table = this.$el.find('.bind-milestone-dialog-table');
			table.one('draw.dt', function(){
				table.find('>tbody>tr>td.bind-milestone-dialog-check').each(function(){
					$(this).html('<input type="checkbox"/>');
				});
				table.find('>tbody>tr').addClass('cursor-pointer');
				self.checkBindedMilestone();
				table.trigger('refresh.squashTable' );
			});

			table.one('refresh.squashTable', function(){
				self.refreshTable();
			});
			this.dialog = this.$el.formDialog({
				autoOpen : false,
				width : 800
			});


			this._openDialog();
		},
		_openDialog : function (){
			var data = this.data;
			var workspace = this.options.workspace;
			
			if (data.hasData) {
				this.dialog.formDialog('setState', 'selection');
				this.dialog.formDialog('open');	
			} else {
				var errorMessageKey = 'message.search.mass-modif.milestone.wrongperim.'+workspace;
				var warn = translator.get({
					errorTitle : 'popup.title.Info',
					errorMessage : errorMessageKey
				});
				$.squash.openMessage(warn.errorTitle, warn.errorMessage);	
			}
		},
		
		initTable : function(){
			var table = this.$el.find('.bind-milestone-dialog-table');
			var tblCnf = {
					bServerSide : false
				},
			squashCnf = {};	
			table.squashTable(tblCnf, squashCnf);
			this.table = table;

		},
		updateTable : function(){
			var table = this.$el.find('.bind-milestone-dialog-table');
			table.squashTable().fnSettings().sAjaxSource  = this.options.tableSource;	
			table.squashTable()._fnAjaxUpdate();
		},
		
		initWarning : function(){

			var data = this.data;
		
			if (!data.samePerimeter){
				var warning = translator.get('message.search.mass-modif.milestone.warn');
				this.$el.find('#warning-mass-modif').text(warning);
			}
			

		},
		initData : function(){
			var self = this;
			$.ajax({
				type: "GET",
				url : this.options.dataURL
			}).done(function(data) {
				self.data = data;
				self._afterDataInit();
			});
		},
	
		cancel : function(event) {
			this.cleanup();
		},
		
		selection : function(){
			this.dialog.formDialog('setState', 'confirm');
		},
		
		confirm : function() {
			this.cleanup();
			var self = this;
			
			var checks = this.table.find('>tbody>tr>td.bind-milestone-dialog-check input:checked');
			var ids = [];
			
			checks.each(function(){
				var r = this.parentNode.parentNode;
				var id = self.table.fnGetData(r)['entity-id'];
				ids.push(id);
			});
			
			var data =  ids.length !== 0 ? $.param({"ids" : ids}) : 'ids[]';
			
			
		
			$.ajax({
				url : this.options.milestonesURL,
				type : 'POST',
				data : data
			}).success(function(oneVersionAlreadyBind){
				self.refreshSearchTable();
				if (oneVersionAlreadyBind){
					var warn = translator.get({
						errorTitle : 'popup.title.Info',
						errorMessage : 'message.search.mass-modif.milestone.requirement-version-already-bind'
					});
					$.squash.openMessage(warn.errorTitle, warn.errorMessage);	
					
				}
			});	
			
		},
		
		refreshSearchTable : function(){
			this.options.searchTableCallback();
		},
		refreshTable : function(){
			this.table.squashTable().refresh();
		},
         checkBindedMilestone : function(){
			this._check(this.data.checkedIds);
         },
         
         _check: function(ids){
			var tab = $('.bind-milestone-dialog-table').squashTable();
			var checks = tab.find('>tbody>tr>td.bind-milestone-dialog-check input');
			checks.each(function(){
				var r = this.parentNode.parentNode;
				var id = tab.fnGetData(r)['entity-id'];
				if (_.contains(ids, id)){
					$(this).prop('checked', true);
					}
				});
			},
	
		cleanup : function() {
			this.$el.addClass("not-displayed");
			this.$el.find('#warning-mass-modif').text("");
			// if we destroy twice, jqui blows up
			if (this.$el.hasClass("ui-dialog-content")){
			this.$el.formDialog("destroy");
			}
		},

		remove : function() {
			this.cleanup();
			this.undelegateEvents();
		},
	
		initBlanketSelectors : function(){
			

		   var table = this.$el.find('.bind-milestone-dialog-table');
			
			
		   this.$el.on('click', '.bind-milestone-dialog-selectall', function(){
					table.find('>tbody>tr>td.bind-milestone-dialog-check input').prop('checked', true);
				});			
				
		   this.$el.on('click', '.bind-milestone-dialog-selectnone', function(){
					table.find('>tbody>tr>td.bind-milestone-dialog-check input').prop('checked', false);				
				});			
				
		   this.$el.on('click', '.bind-milestone-dialog-invertselect', function(){
					table.find('>tbody>tr>td.bind-milestone-dialog-check input').each(function(){
						this.checked = ! this.checked;					
					});				
				});
			}

		
		});

	return View;
});
	