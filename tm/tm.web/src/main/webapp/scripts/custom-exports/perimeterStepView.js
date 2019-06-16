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
define(["jquery", "backbone", "underscore", "workspace.routing", "app/squash.handlebars.helpers", "workspace.projects", "./abstractStepView", "tree", "squash.translator", "../app/util/StringUtil", "is", "./treePopup", "jquery.squash.confirmdialog", "jquery.squash.buttonmenu"],
	function ($, backbone, _, router, Handlebars, projects, AbstractStepView, tree, translator, StringUtil, is, TreePopup) {

		"use strict";

		var perimeterStepView = AbstractStepView.extend({

			initialize: function (data, wizrouter) {
				this.tmpl = "#perimeter-step-tpl";
				this.model = data;
				data.name = "perimeter";
				this._initialize(data, wizrouter);

				this.updateDisplayWithPerimeter();
			},

			events: {
				"click #select-perimeter-button" : "openPerimeterPopup"
			},

			updateModel: function () {
				// update was done when the scope was selected
			},

			openPerimeterPopup: function() {
				var self = this;
				var type = 'CAMPAIGN';
				var treePopup = new TreePopup({
					model: self.model,
					name: type,
					nodes: self.model.get('selectedTreeNodes') || []
				});
				self.addTreePopupConfirmEvent(treePopup, self, type);
			},

			addTreePopupConfirmEvent: function (popup, self, name) {

				popup.on('treePopup.confirm', function () {

					var scope = _.map($("#tree").jstree('get_selected'), function (selected) {
						return {
							id: $(selected).attr("resid"),
							type: $(selected).attr("restype").split("-").join("_").slice(0, -1).toUpperCase(),
							name: $(selected).attr("name")
						};
					});

					// Check if it is a campaign
					if(scope[0].type === 'CAMPAIGN') {
						// Store the perimeter
						self.model.set({scope: scope});

						// Fetch the corresponding projects data (used to get the custom fields)
						self.doFetchCufData(scope[0].id).then(function(cufMap) {
								var entityWithCuf = self.model.get('entityWithCuf');
								var availableCustomFields = _.chain(cufMap).pick(entityWithCuf).mapObject(function(cufList) {
									return _.map(cufList, function(cufBinding) {
										return {
											id: cufBinding.boundEntity.enumName + "_CUF-" + cufBinding.customField.id,
											label: cufBinding.customField.label,
											code: cufBinding.customField.code,
											type: cufBinding.customField.inputType.friendlyName
										};
									});
								}).value();
								self.model.set({ availableCustomFields: availableCustomFields });
						});

						// Store the selected node to reselect it if the tree is opened later
						var selecteTreedNodes = _.map($('#tree').jstree('get_selected'), function(selected) {
							return {
								id: $(selected).attr("id")
							};
						});
						self.model.set({ selectedTreeNodes: selecteTreedNodes });

						self.updateDisplayWithPerimeter();
					}
				});

			},

			updateDisplayWithPerimeter: function () {

				var scope = this.model.get('scope');
				var selectedPerimeterSpan = $('#selected-perimeter');

				if(scope) {
					var campaignName = scope[0].name;
					selectedPerimeterSpan.text(StringUtil.unescape(campaignName));
				} else {
					selectedPerimeterSpan.text(translator.get('wizard.perimeter.msg.perimeter.choose'));
				}
			},

			doFetchCufData: function(campaignId) {
				return $.ajax({
					method: 'GET',
					url: router.buildURL('custom-report.custom-export.cufs'),
					data: { campaignId: campaignId }
				});
			}

		});

		return perimeterStepView;

	});
