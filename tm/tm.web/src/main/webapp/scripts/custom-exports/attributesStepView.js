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
define(["jquery", "backbone", "underscore", "workspace.routing", "app/squash.handlebars.helpers", "workspace.projects", "./abstractStepView", "tree", "squash.translator", "../app/util/StringUtil", "is", "jquery.squash.confirmdialog", "jquery.squash.buttonmenu"],
	function ($, backbone, _, router, Handlebars, projects, AbstractStepView, tree, translator, StringUtil, is) {

		"use strict";

		var attributesStepView = AbstractStepView.extend({

			initialize: function (data, wizrouter) {
				this.tmpl = "#attributes-step-tpl";
				this.model = data;
				data.name = "attributes";
				this._initialize(data, wizrouter);
				this.reloadModelInView();
			},

			events: {
				"click input[name='entity']" : 'toggleEntityPanelVisibility'
			},

			reloadModelInView: function() {
				var selectedEntites = this.model.get('selectedEntities') || [];
				var selectedAttributes = this.model.get('selectedAttributes') || [];
				// Entities
				_.each(selectedEntites, function(entity) {
					$("#" + entity).prop('checked', true);
					$("#" + entity + "-panel").removeClass("not-displayed");
				});
				// Attributes
				_.each(selectedAttributes, function(attr) {
						$('#' + attr).prop('checked', true);
				})
			},

			updateModel: function () {
				// Store selected entities (only saved for the current wizard)
				var selectedEntities = _.pluck($("[name='entity']:checked"), 'id');
				// Store attributes
				var selectedAttributes = _.pluck($("input[type=checkbox][name!='entity']:checked"), 'id');

				this.model.set("selectedEntities", selectedEntities);
				this.model.set("selectedAttributes", selectedAttributes);
			},

			toggleEntityPanelVisibility: function(event) {
				var entityClicked = event.target.id;
				var entityPanelToToggle = $("#" + entityClicked + "-panel");
				entityPanelToToggle.toggleClass("not-displayed");
		}

		});

		return attributesStepView;

	});
