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
define([ "jquery", "backbone", "underscore", "./ParametersTable", "./NewParameterDialog", "jquery.squash.confirmdialog",
		"jquery.squash.togglepanel" ], function($, Backbone, _, ParametersTable, NewParameterDialog) {
	var teamMod = squashtm.app.teamMod;
	var ParametersPanel = Backbone.View.extend({

		el : "#parameters-panel-container",

		initialize : function(options) {
			this.settings = options.settings;
			this.language = this.settings.language;
			
			_.bindAll(this, "showNewParameterDialog", "_onNewParameterConfirmed", 
					"_onParameterRemoved", "refresh", "refreshDataSetParameterName", 
					"refreshDataSetParameterDescription");

			this.table = new ParametersTable({
				settings : this.settings
			});

			this.newParameterDialog = new NewParameterDialog({
				settings : this.settings,
				model : {
					name : "",
					description : ""
				}
			});
			

			this.configureButtons();
			this.listenTo(this.newParameterDialog, "newparameterdialog.confirm", this._onNewParameterConfirmed);
			this.listenTo(this.table, "parameterstable.removed", this._onParameterRemoved);
			this.listenTo(this.table, "parameter.name.update", this.refreshDataSetParameterName);
			this.listenTo(this.table, "parameter.description.update", this.refreshDataSetParameterDescription);
		},

		events : {

		},

		configureButtons : function() {
			// ===============toogle buttons=================
			// this line below is here because toggle panel
			// buttons cannot be bound with the 'events'
			// property of Backbone.View.
			// my guess is that the event is bound to the button
			// before it is moved from it's "span.not-displayed"
			// to the toggle panel header.
			// TODO change our way to make toggle panels buttons
			// =============/toogle buttons===================
			this.$("#add-parameter-button").on('click', this.showNewParameterDialog);
		},

		showNewParameterDialog : function(event) {
			this.newParameterDialog.show();
		},

		/**
		 * handles new parameter confirmed events. refreshes table and triggers an event.
		 */
		_onNewParameterConfirmed : function() {
			this.table.refresh();
			this.trigger("parameter.created");
		}, 
		
		/**
		 * handles parameter removed events. triggers an event.
		 */
		_onParameterRemoved : function() {
			this.table.refresh();
			this.trigger("parameter.removed");
		}, 
		
		refresh: function() {
			this.table.refresh();
		},
		
		/**
		 * handles parameter update events. triggers an event.
		 */
		refreshDataSetParameterName : function(parameters){
			this.trigger("parameter.name.update", parameters);
		},
		
		refreshDataSetParameterDescription : function(parameters){
			this.trigger("parameter.description.update", parameters);
		}
	});
	return ParametersPanel;
});