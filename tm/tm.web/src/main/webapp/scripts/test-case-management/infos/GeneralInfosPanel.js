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
define([ "jquery", "backbone", "underscore", "workspace.event-bus", "squash.configmanager", "jeditable.simpleJEditable", "jeditable.selectJEditable", 
         "jeditable.selectJEditableAuto", "jquery.squash.jeditable"],
		function($, Backbone, _ , eventBus, confman, SimpleJEditable, SelectJEditable, SelectJEditableAuto) {
	
			var GeneralInfosPanel = Backbone.View.extend({
				
				el : "#test-case-description-panel",
				
				initialize : function(options) {
					var self = this;
					this.settings = options.settings;
					this.updateReferenceInTree = $.proxy(this._updateReferenceInTree, this);
					this.postImportance = $.proxy(this._postImportance, this);
					this.postStatus = $.proxy(this._postStatus, this);
					this.updateStatusInTree = $.proxy(this._updateStatusInTree, this);
					this.refreshImportanceIfAuto = $.proxy(this._refreshImportanceIfAuto,this);
					this.updateImportanceInTree = $.proxy(this._updateImportanceInTree, this);
					this.onRefreshImportance = $.proxy(this._onRefreshImportance, this);
					this.updateIcon = $.proxy(this._updateIcon, this);
					
					if(this.settings.writable){
						
						var richEditSettings = confman.getJeditableCkeditor();
						richEditSettings.url = this.settings.urls.testCaseUrl;
	
						$('#test-case-description').richEditable(richEditSettings).addClass("editable");
						
						this.referenceEditable = new SimpleJEditable({
							targetUrl :this.settings.urls.testCaseUrl,
							componentId : "test-case-reference",
							submitCallback : this.updateReferenceInTree,
							jeditableSettings : {
								maxLength : 50
							}
						});	
					
						this.importanceEditable = new SelectJEditable({
							target : this.postImportance,
							getUrl : this.settings.urls.testCaseUrl+"/importance",
							componentId : "test-case-importance",
							jeditableSettings : {
								data : this.settings.testCaseImportanceComboJson
							}
						});
		
						this.natureEditable = new SelectJEditable({
							target : this.settings.urls.testCaseUrl,
							componentId : "test-case-nature",
							jeditableSettings : {
								data : confman.toJeditableSelectFormat(this.settings.testCaseNatures.items, {"code" : "friendlyLabel"}),
								onsubmit : function(settings, original){
									self.updateIcon(this, self.settings.testCaseNatures.items, "#test-case-nature-icon");
									}
							}
						});
						
						this.typeEditable = new SelectJEditable({
							target : this.settings.urls.testCaseUrl,
							componentId : "test-case-type",
							jeditableSettings : {
								data : confman.toJeditableSelectFormat(this.settings.testCaseTypes.items, {"code" : "friendlyLabel"}),
								onsubmit : function(){
									self.updateIcon(this, self.settings.testCaseTypes.items, "#test-case-type-icon");
								}
							}
						});

						
						this.statusEditable = new SelectJEditable({
							target : this.postStatus,
							componentId : "test-case-status",
							jeditableSettings : {
								data : this.settings.testCaseStatusComboJson
							}
						});
						
					
						this.importanceEditableAuto = new SelectJEditableAuto({
							associatedSelectJeditableId:"test-case-importance",
							url: this.settings.urls.importanceAutoUrl,
							isAuto: this.settings.importanceAuto,
							paramName:"importanceAuto" ,
							autoCallBack: this.updateImportanceInTree
						});
						
						$(this.importanceEditable).on("selectJEditable.refresh", this.onRefreshImportance);
								
					}
					
					this.identity = { resid : this.settings.testCaseId, restype : "test-cases"  };
					
				},
				
				events : {
					
				},
				
				_refreshImportanceIfAuto : function(){
					if(this.importanceEditableAuto.isAuto()){
						this.importanceEditable.refresh();
					}
				},
				
				_onRefreshImportance : function(){
					var option  = this.importanceEditable.getSelectedOption();
					this.updateImportanceInTree(option);
				},
				
				_updateIcon : function(form, data, receiver){
					var value = form.find('select').val();
					var icon = $.grep(data, function(e){return e.code === value;})[0].iconName;
					$(receiver).attr('class', '').addClass('sq-icon sq-icon-'+icon);					
				},
					
				_postStatus : function (value, settings){
					var self = this;
					$.post(this.settings.urls.testCaseUrl, {id:"test-case-status", value : value})
					.done(function(response){
						self.updateStatusInTree(value);
					});

					// in the mean time, must return immediately
					return settings.data[value];
				},
				
				_updateStatusIcon : function (value){

					var status = $("#test-case-status-icon");
					status.attr("class", ""); //reset
					status.addClass("sq-icon test-case-status-" + value);
				},
				
				_postImportance : function (value, settings){
					var self = this;
					$.post(this.settings.urls.testCaseUrl, {id:"test-case-importance", value : value})
					.done(function(response){
						self.updateImportanceInTree(value);
					});
					// in the mean time, must return immediately
					return settings.data[value];
				},
				
				_updateImportanceIcon : function (value){

					var status = $("#test-case-importance-icon");
					status.attr("class", ""); // reset
					status.addClass("sq-icon test-case-importance-" + value);
				},
				
				_updateStatusInTree : function(value){
					var self = this;
					self._updateStatusIcon(value);
					eventBus.trigger('node.attribute-changed', {identity : self.identity, attribute : 'status', value : value.toLowerCase()});
					
				},
				
				_updateImportanceInTree : function(value){
					var self = this;
					self._updateImportanceIcon(value);
					eventBus.trigger('node.attribute-changed', {identity : self.identity, attribute : 'importance', value : value.toLowerCase()});
				},
				
				_updateReferenceInTree : function (newRef){
					var self = this;
					eventBus.trigger('node.update-reference', {identity : self.identity, newRef : newRef});		
				}

			});
						
			return GeneralInfosPanel;
});