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
define(["jquery", "backbone", "underscore", "workspace.routing", "app/squash.handlebars.helpers", "workspace.projects", "./abstractStepView", "tree", "squash.translator", "../app/util/StringUtil", "is", "./customFieldPopup", "jquery.squash.confirmdialog", "jquery.squash.buttonmenu"],
	function ($, backbone, _, router, Handlebars, projects, AbstractStepView, tree, translator, StringUtil, is, CustomFieldPopup) {

		"use strict";

		var attributesStepView = AbstractStepView.extend({

			initialize: function (data, wizrouter) {
				this.tmpl = "#attributes-step-tpl";
				this.model = data;
				data.name = "attributes";
				this._initialize(data, wizrouter);
				this.reloadModelInView();
				this.listenTo(this.model, 'change:selectedCufAttributes', this.updateSelectedAttributesWithCuf);
			},

			events: {
				"click input[name='entity']" : 'toggleEntityPanelVisibility',
				"click .wizard-cuf-btn" : "openCufPopup"
			},

			reloadModelInView: function() {
				var self = this;
				var selectedEntities = this.model.get('selectedEntities') || [];
				var selectedAttributes = this.model.get('selectedAttributes') || [];
				var selectedCufAttributes = this.model.get('selectedCufAttributes') || [];
				// Entities
				_.each(selectedEntities, function(entity) {
					$("#" + entity).prop('checked', true);
					$("#" + entity + "-panel").removeClass("not-displayed");
				});
				// Attributes
				_.each(selectedAttributes, function(attr) {
						$('#' + attr).prop('checked', true);
				})
				// Show Cuf attributes
				_.each(selectedCufAttributes, function(cufAttrId) {
					self.showCufCheckBox(cufAttrId);
				});
			},

			updateModel: function () {
				// Store selected entities (only saved for the current wizard)
				var selectedEntities = _.pluck($("[name='entity']:checked"), 'id');
				// Store attributes
				var selectedAttributes = _.pluck($("input[type=checkbox][name!='entity']:not([data-cuf]):checked"), 'id');
				// Update selectedCufAttributes
				var selectedCufAttributes = _.pluck($("input[type=checkbox][name!='entity'][data-cuf]:checked"), 'id');

				this.model.set("selectedEntities", selectedEntities);
				this.model.set("selectedAttributes", selectedAttributes);
				this.model.set("selectedCufAttributes", selectedCufAttributes);
			},

			toggleEntityPanelVisibility: function(event) {
				var entityClicked = event.target.id;
				var entityPanelToToggle = $("#" + entityClicked + "-panel");
				entityPanelToToggle.toggleClass("not-displayed");
			},

			openCufPopup: function(event) {
				var entityType = event.target.getAttribute("data-entity");
				var cufToDisplay = this.model.get('availableCustomFields')[entityType];
				this.model.set({ cufToDisplay: cufToDisplay });

				var cufPopup = new CustomFieldPopup(this.model);
			},

			// callback when selectedCufAttributes changes
			updateSelectedAttributesWithCuf: function(model, newSelectedIds) {
				var self = this;
				var previousSelectedIds = model.previous("selectedCufAttributes");
				var idsToHide = _.difference(previousSelectedIds, newSelectedIds);
				_.each(idsToHide, function(id) {
					self.hideCufCheckBox(id);
				});
				var idsToShow = _.difference(newSelectedIds, previousSelectedIds);
				_.each(idsToShow, function(id) {
					self.showCufCheckBox(id);
				});
			},

			showCufCheckBox: function(id) {
				var checkBoxSelector = '[id="' + id + '"]';
				var checkBox = this.$el.find(checkBoxSelector);
				checkBox.prop('checked', true);

				var checkBoxWrapperSelector = '[id="wrapper-' + id + '"]';
				var wrapper = this.$el.find(checkBoxWrapperSelector);
				wrapper.addClass("chart-wizard-visible");
				wrapper.removeClass("chart-wizard-hidden");
			},

			hideCufCheckBox: function(id) {
				var checkBoxSelector = '[id="' + id + '"]';
				var checkBox = this.$el.find(checkBoxSelector);
				checkBox.prop('checked', false);

				var checkBoxWrapperSelector = '[id="wrapper-' + id + '"]';
				var wrapper = this.$el.find(checkBoxWrapperSelector);
				wrapper.removeClass("chart-wizard-visible");
				wrapper.addClass("chart-wizard-hidden");
			}

		});

		return attributesStepView;

	});
