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
define([ "jquery", "backbone", "underscore", "./ParametersPanel", "./DatasetsPanel", "jquery.squash.confirmdialog" ], function($,
		Backbone, _, ParametersPanel, DatasetsPanel) {
	var ParametersTab = Backbone.View.extend({

		el : "#parameters-tabs-panel",

		initialize : function(options) {
			var self = this;
			this.settings = options.settings;

			this.parametersPanel = new ParametersPanel({
				settings : this.settings,
				parentTab : this
			});
			this.datasetsPanel = new DatasetsPanel({
				settings : this.settings,
				parentTab : this
			});

			this.listenTo(this.parametersPanel, "parameter.created parameter.removed", this.datasetsPanel.refresh);
			this.listenTo(this.parametersPanel, "parameter.name.update", this.datasetsPanel.refreshDataSetParameterName);
			this.listenTo(this.parametersPanel, "parameter.description.update", this.datasetsPanel.refreshDataSetParameterDescription);
						
			// content is refreshed where this tab becomes visible. should be in a parent view if it existed
			$("div.fragment-tabs").on("tabsshow", function(event, ui) {
				if (ui.index === self.settings.parameters.tabIndex) {
					self.parametersPanel.refresh();
					self.datasetsPanel.refresh();
				}
			});
		},
		events : {}
	});
	return ParametersTab;
});