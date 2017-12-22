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
define([ "jquery", "backbone", "underscore", "./DatasetsTable", "./NewDatasetDialog", 
		"jquery.squash.confirmdialog", "jquery.squash.togglepanel" ],
		function($, Backbone, _, DatasetsTable, NewDatasetDialog) {
			var DatasetsPanel = Backbone.View.extend({
				
				el : "#datasets-panel-container",
				
				initialize : function(options) {
				this.settings = options.settings;
					this.language = this.settings.language;
					
					_.bindAll(this, "showNewDialog", "refresh",
							"refreshDataSetParameterName", 
							"refreshDataSetParameterDescription");
					
					this.table = new DatasetsTable({settings : options.settings, parentTab : options.parentTab});
					this.configureButtons();
				},
				
				events : {
					
				},
				
				configureButtons : function() {
					var self = this;
					// ===============toogle buttons=================
					// this line below is here because toggle panel
					// buttons cannot be bound with the 'events'
					// property of Backbone.View.
					// my guess is that the event is bound to the button
					// before it is moved from it's "span.not-displayed"
					// to the toggle panel header.
					// TODO change our way to make toggle panels buttons
					// =============/toogle buttons===================
					this.$("#add-dataset-button").on('click', self.showNewDialog);
				},
				
				showNewDialog : function(event) {
					var self = this;

					function discard() {
						self.newDatasetDialog.destroy();
						self.newDatasetDialog.off("newDataset.cancel newDataset.confirm");
						self.newDatasetDialog.undelegateEvents();
						self.newDatasetDialog = null;
					}

					function discardAndRefresh() {
						discard();
						self.table.refresh();
					}

					self.newDatasetDialog = new NewDatasetDialog({settings : self.settings});
					self.newDatasetDialog.on("newDataset.cancel", discard);
					self.newDatasetDialog.on("newDataset.confirm", discardAndRefresh);
				}, 
				
				refresh: function() {
					this.table.reDraw();
				},
				
				refreshDataSetParameterName : function(parameters){
					this.table.refreshDataSetParameterName(parameters['id'], parameters['name']);
				},
				
				refreshDataSetParameterDescription : function(parameters){
					this.table.refreshDataSetParameterDescription(
							parameters['id'],
							parameters['description']);
				}
			});
			return DatasetsPanel;
});