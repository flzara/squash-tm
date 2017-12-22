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
define([ "jquery", "backbone", "jeditable.simpleJEditable", "app/ws/squashtm.notification", "squash.translator", "jquery.squash.confirmdialog",
		"jquery.squash.messagedialog", "squashtable" ], function($, Backbone, SimpleJEditable, notification, translator) {
	var ParametersTable = Backbone.View.extend({

		el : "#parameters-table",

		initialize : function(options) {
			this.settings = options.settings;
			this.removeRowParameter = $.proxy(this._removeRowParameter, this);
			this.parametersTableRowCallback = $.proxy(this._parametersTableRowCallback, this);
			this.parametersTableDrawCallback = $.proxy(this._parametersTableDrawCallback, this);
			this.confirmRemoveParameter = $.proxy(this._confirmRemoveParameter, this);
			this.addSimpleJEditableToName = $.proxy(this.addSimpleJEditableToName, this);
			this.updateParameterDescription = $.proxy(this._updateParameterDescription, this);
			this.refresh = $.proxy(this._refresh, this);
			this._configureTable.call(this);
			this._configureRemoveParametersDialogs.call(this);
			
			this.table.on("parameter.description.update", this.updateParameterDescription);
		},

		events : {

		},

		_dataTableSettings : function(self) {
			return {
				// has Dom configuration
				"bPaginate" : false,
				"aaSorting" : [ [ 3, 'asc' ] ],
				"fnRowCallback" : self.parametersTableRowCallback,
				"fnDrawCallback" : self.parametersTableDrawCallback
			};
		},

		_squashSettings : function(self) {

			var squashSettings = {};

			if (self.settings.permissions.isWritable) {
				squashSettings = {
					buttons : [ {
						tooltip : self.settings.language.remove,
						tdSelector : "td.delete-button",
						uiIcon : "ui-icon-trash",
						jquery : true,
						onClick : this.removeRowParameter,
						condition : function(row, data) {
							return data["directly-associated"];
						}
					} ],

					richEditables : {
						'parameter-description' : {
							'url' : self.settings.basic.parametersUrl + '/{entity-id}/description',
							'oncomplete' : 'parameter.description.update'
						}
					}

				};
			}

			return squashSettings;

		},

		discriminateInheritedVerifications : function(row, data, displayIndex) {
			if (!data["directly-associated"]) {
				$(row).addClass("inherited-parameter-verification");
				$('td.delete-button', row).html(''); // remove the delete button
			}
		},
		
		_configureTable : function() {
			var self = this;
			$(this.el).squashTable(self._dataTableSettings(self), self._squashSettings(self));
			this.table = $(this.el).squashTable();
		},

		_parametersTableRowCallback : function(row, data, displayIndex) {
			if (data["directly-associated"] && this.settings.permissions.isWritable) {
				this.addSimpleJEditableToName(row, data);
			}
			this.discriminateInheritedVerifications(row, data, displayIndex);
			return row;
		},
		
		_parametersTableDrawCallback : function(oSettings){
			var table = $("#"+oSettings.sInstance);
			// prevent parameter-description cells to turn into editable
			// if they map to an inherited parameter
			table.find('tr.inherited-parameter-verification td.parameter-description').removeClass('parameter-description');
		},

		_removeRowParameter : function(table, cell) {
			var row = cell.parentNode.parentNode;
			this.confirmRemoveParameter(row);
		},

		_confirmRemoveParameter : function(row) {
			var self = this;
			var paramId = self.table.getODataId(row);

			self._isUsed.call(self, paramId).done(function(isUsed) {
				if (isUsed) {
					self.cannotRemoveUsedParamDialog.openMessage();
				} else {
					self.toDeleteId = paramId;
					self.confirmRemoveParameterDialog.confirmDialog("open");
				}
			});
		},

		_isUsed : function(paramId) {
			var self = this;
			return $.ajax({
				url : self.settings.basic.parametersUrl + "/" + paramId + "/used",
				type : "get"
			});
		},

		_removeParameter : function() {
			var self = this;
			var id = this.toDeleteId;
			$.ajax({
				url : self.settings.basic.parametersUrl + '/' + id,
				type : 'delete'
			}).done(function() {
				self.refresh();
				self.trigger("parameterstable.removed");
			});
		},

		_configureRemoveParametersDialogs : function() {
			var self = this;
			this.confirmRemoveParameterDialog = $("#remove-parameter-confirm-dialog").confirmDialog();

			this.confirmRemoveParameterDialog.on("confirmdialogconfirm", $.proxy(self._removeParameter, self));
			this.confirmRemoveParameterDialog.on("close", $.proxy(function() {
				this.toDeleteId = null;
			}, this));

			this.cannotRemoveUsedParamDialog = $("#remove-parameter-used-dialog").messageDialog();
		},

		// =====================================================

		addSimpleJEditableToName : function(row, data) {
			var self = this;
			var urlPOST = self.settings.basic.parametersUrl + '/' + data["entity-id"] + "/name";
			var component = $('td.parameter-name', row);
			var validate = function(value, settings){
				settings.oldName = settings.oldName === undefined ? data['name'] : settings.oldName;
				var pattern = /^[A-Za-z0-9_\-]{1,255}$/ ;
				if (!pattern.test(value)){
					notification.showError(translator.get("message.parameterInvalidPattern"));
					return settings.oldName;
				
				} else {
					
					$.ajax({
						url : urlPOST,
						data : {"value" :value },
						method : "POST"
					});
					settings.oldName = value;
					return value;
				}
			};
			
			new SimpleJEditable({
				targetUrl : validate,
				component : component,
				jeditableSettings : {
					callback : function(newname){
						self.trigger("parameter.name.update",{
							id : data['entity-id'],
							name : newname
						});						
					},
					onerror : function(settings, original, xhr){
						console.log(original);
						xhr.errorIsHandled = true;
						notification.showXhrInDialog(xhr);
						original.reset(this);
					}
				}
			});
		},

		_refresh : function() {
			this.table.fnDraw(false);
		},
		
		_updateParameterDescription : function(event, result){
			var id = result['id'];
			
			// get parameter description (richEditable) from the squashTable and converts it to a simple String
			var description = $.trim(this.table.getRowsByIds([id]).eq(0).find('td.parameter-description').text());
			
			this.trigger('parameter.description.update', {
				id : id, 
				description : description
				});
		}

	});

	return ParametersTable;

});
