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
/**
 * @since 1.12.0.RC2 This module uses Wreqr to listen to these events and react accordingly :
 * * `verifying-test-cases:unbind-row` unbinds the TC matching the table row which was clicked
 * * `verifying-test-cases:unbind-selected` unbinds the TCs matching the selected rows of the table
 */
require(["common"], function () {
	require(["jquery", "app/squash.wreqr.init", "workspace.event-bus", "workspace.tree-event-handler",
		"squash.translator", "app/ws/squashtm.notification", "req-workspace/linked-requirements-panel",
		"jqueryui", "jquery.squash.messagedialog", "squashtable", "app/ws/squashtm.workspace", "jquery.squash.formdialog"],
		function ($, squash, eventBus, treehandler, msg, notification, LinkedReqVersionsPanel) {
			"use strict";

			msg.load([
				"requirement-version.linked-requirement-versions.rejection.already-linked-rejection",
				"requirement-version.linked-requirement-versions.rejection.not-linkable-rejection",
				"requirement-version.linked-requirement-versions.rejection.same-requirement-rejection",
				"label.Unbind",
				"message.SelectOneRequirement"
			]);

			function lock() {
				$('#add-items-button').button('disable');
				$('#remove-items-button').button('disable');
			}

			function unlock() {
				$('#add-items-button').button('enable');
				$('#remove-items-button').button('enable');
			}

			function sendUpdateTree(ids) {
				eventBus.trigger("node.update-reqCoverage", { targetIds: ids });
			}

			function showAddSummary(summary) {
				if (summary) {
					var summaryMessages = {
						alreadyLinkedRejections: msg.get("requirement-version.linked-requirement-versions.rejection.already-linked-rejection"),
						notLinkableRejections: msg.get("requirement-version.linked-requirement-versions.rejection.not-linkable-rejection"),
						sameRequirementRejections: msg.get("requirement-version.linked-requirement-versions.rejection.same-requirement-rejection")
					};

					var summaryRoot = $("#add-summary-dialog > ul");
					summaryRoot.empty();

					for (var rejectionType in summary) {
						if (summary.hasOwnProperty(rejectionType)) {
							var message = summaryMessages[rejectionType];
							if (message) {
								summaryRoot.append('<span>' + message + '</span>');
							}
						}
					}

					if (summaryRoot.children().length > 0) {
						$("#add-summary-dialog").messageDialog("open");
					}
				}
			}

			/**
			 * returns the datatable (well, squashtable) object for linked ReqVersions
			 */
			function table() {
				return $("#linked-requirement-versions-table").squashTable();
			}

			$(document).on("click", "#remove-items-button", function (event) {
				squash.vent.trigger("linkedrequirementversions:unbind-selected", { source: event });
			});

			/**
			*	Get the id of the Requirement Version currently selected in the tree.
			* @return
			* 	- An array containing the id of the unique requirementVersion selected in the tree
			* 	- An empty array if:
			* 		- More than one node is selected
			* 		- The selected node is a library Node or a folder Node
			*/
			function getReqVersionsIdFromTree() {

				var ids = [];
				var node = 0;
				var selectedNodes = $('#linkable-requirements-tree').jstree('get_selected');
				if (selectedNodes.length > 0 && selectedNodes.not(':library').length === selectedNodes.length) {
					node = selectedNodes.treeNode();
					ids = node.all('getResId');
				}
				return $.map(ids, function (id) { return parseInt(id); });
			}

			$(function () {

				// init the table
				$("#linked-requirement-versions-table").squashTable(
					{
						aaData: window.squashtm.bindingsManager.model
					},
					{
						unbindButtons: {
							delegate: "#unbind-active-linked-reqs-row-dialog",
							tooltip: msg.get('label.Unbind')
						},
						buttons: [{
							tooltip: msg.get('requirement-version.link.type.modify.tooltip'),
							tdSelector: "td.edit-link-type-button",
							uiIcon: "edit-pencil",
							onClick: function (table, cell) {
								var row = cell.parentNode.parentNode;
								var modifiedVersionId = table.getODataId(row);
								openChooseTypeDialog(window.squashtm.bindingsManager.requirementVersion.id, modifiedVersionId, false);
							}
						}]
					});

				var openChooseTypeDialog = function (reqVersionId, relatedId, isRelatedIdANodeId) {
					var linkTypeDialog = $("#choose-link-type-dialog");
					linkTypeDialog.data('reqVersionId', reqVersionId);
					linkTypeDialog.data('relatedReqNodeId', relatedId);
					linkTypeDialog.data('isRelatedIdANodeId', isRelatedIdANodeId);
					linkTypeDialog.formDialog('open');
				};

				var openCreateTypeDialog = function (reqVersionId, relatedId, isRelatedIdANodeId) {
					var linkTypeDialog = $("#create-link-type-dialog");
					linkTypeDialog.data('reqVersionId', reqVersionId);
					linkTypeDialog.data('relatedReqNodeId', relatedId);
					linkTypeDialog.data('isRelatedIdANodeId', isRelatedIdANodeId);
					linkTypeDialog.formDialog('open');
				};

				var deselectTree = function () {
					var tree = $('#linkable-requirements-tree');
					tree.jstree('deselect_all');
				};

				var apiUrl = window.squashtm.bindingsManager.bindingsUrl;

				// init the panel
				new LinkedReqVersionsPanel({ apiUrl: apiUrl })

				// init the popups
				var chooseLinkTypeDialog = $("#choose-link-type-dialog").formDialog();
				var createLinkTypeDialog = $("#create-link-type-dialog").formDialog();
				$("#add-summary-dialog").messageDialog();

				var bind = LinkedReqVersionsPanel.bindingActionCallback(apiUrl, "POST");
				var getAndDisplayRelatedReqVersionInfos = LinkedReqVersionsPanel.bindingActionCallback(apiUrl, "GET");

				var onAddItemBtnClick = function () {
					lock();
					var ids = getReqVersionsIdFromTree();
					if (ids.length === 0) {
						notification.showError(msg.get('message.SelectOneRequirement'));
						unlock();
						deselectTree();
						return;
					} else if (ids.length > 0) {
						openCreateTypeDialog(window.squashtm.bindingsManager.requirementVersion.id, ids, true);
					}
				};
				$("#add-items-button").on("click", onAddItemBtnClick);

				var onLinkTypeDialogOpen = function () {

					var self = chooseLinkTypeDialog;
					self.formDialog('setState', 'wait');
					var relatedId = self.data('relatedReqNodeId');
					var isRelatedIdANodeId = self.data('isRelatedIdANodeId');

					/* Fetching related RequirementVersion attributes in order to display them in the popup. */
					getAndDisplayRelatedReqVersionInfos(relatedId, { "isRelatedIdANodeId": isRelatedIdANodeId }).success(
						function (relatedReqVersionInfos) {
							var relatedVersionName = relatedReqVersionInfos.versionName;
							self.find("#relatedRequirementName").html(relatedVersionName);
							$.ajax({
								url: apiUrl + "/requirement-versions-link-types",
								method: 'GET',
								datatype: 'json'
							}).success(function (typesList) {
								displayCombo(self, typesList);
							});
						});
				};

				var onCreateLinkTypeDialogOpen = function () {

					var self = createLinkTypeDialog;
					self.formDialog('setState', 'wait');
					var relatedId = self.data('relatedReqNodeId');
					var isRelatedIdANodeId = self.data('isRelatedIdANodeId');
					/* Fetching related RequirementVersion attributes in order to display them in the popup. */
					getAndDisplayRelatedReqVersionInfos(relatedId, { "isRelatedIdANodeId": isRelatedIdANodeId }).success(
						function (relatedReqVersionInfos) {
							var relatedVersionName = relatedReqVersionInfos.versionName;
							self.find("#relatedRequirementName").html(relatedVersionName);
							$.ajax({
								url: apiUrl + "/requirement-versions-link-types",
								method: 'GET',
								datatype: 'json'
							}).success(function (typesList) {
								displayCombo(self, typesList);
							});
						});
				};

				var displayCombo = function (self, typesList) {
					var comboBox = self.find("#link-types-options");
					comboBox.empty();
					var length = typesList.length;
					for (var i = 0; i < length; i++) {
						var type = typesList[i];
						var id = type.id, role1 = type.role1, role2 = type.role2;
						var optionKey_1 = id + "_" + 0;
						var optionLabel_1 = role1 + " - " + role2;
						var def = type.default;
						var o;
						if (def) {
							o = new Option(optionLabel_1, optionKey_1, true, true);
							comboBox.append(o);
						} else {
							o = new Option(optionLabel_1, optionKey_1);
							comboBox.append(o);
						}

						if (role1 !== role2) {
							var optionKey_2 = id + "_" + 1;
							var optionLabel_2 = role2 + " - " + role1;
							if (type.default) {
								o = new Option(optionLabel_2, optionKey_2, true, true);
								comboBox.append(o);
							} else {
								o = new Option(optionLabel_2, optionKey_2);
								comboBox.append(o);
							}

						}
					}
					self.formDialog('setState', 'confirm');
				}

				/* Trigger unbind-selected-versions event */
				$('#remove-linked-requirement-button').on("click", function (event) {
					squash.vent.trigger("linkedrequirementversions:unbind-selected", { source: event });
				});

				$('#modify-linked-requirement-button').on("click", function (event) {
					// read the ids from the table selection
					var ids = table().getSelectedIds();

					if (ids.length === 0) {
						$.squash.openMessage(msg.get("popup.title.error"), msg.get("message.EmptyTableSelection"));
					} else {
						openChooseTypeDialog(window.squashtm.bindingsManager.requirementVersion.id, ids, false);
					}

				});

				createLinkTypeDialog.on('formdialogopen', onCreateLinkTypeDialogOpen);
				chooseLinkTypeDialog.on('formdialogopen', onLinkTypeDialogOpen);

				var onLinkTypeDialogConfirm = function () {

					var self = chooseLinkTypeDialog;
					var relatedReqVersionId = self.data("relatedReqNodeId");
					var params = configureParams(self, true);
					bind(relatedReqVersionId, params).success(function (data) {
						self.formDialog('close');
						table().refresh();
						unlock();
					});
				};
				chooseLinkTypeDialog.on('formdialogconfirm', onLinkTypeDialogConfirm);

				var onLinkTypeDialogCancel = function () {
					chooseLinkTypeDialog.formDialog('close');
					deselectTree();
					table().refresh();
					unlock();
				};
				chooseLinkTypeDialog.on('formdialogcancel', onLinkTypeDialogCancel);

				var onCreateLinkTypeDialogConfirm = function () {

					var self = createLinkTypeDialog;
					var relatedReqVersionId = self.data("relatedReqNodeId");
					var params = configureParams(self, false);
					bind(relatedReqVersionId, params).success(function (rejections) {
						if (Object.keys(rejections).length > 0) {
							showAddSummary(rejections);
							deselectTree();
						} else {
							table().refresh();
						}
						self.formDialog('close');
						unlock();
					});
				};

				var configureParams = function (self, isRelatedIdANodeId) {
					deselectTree();
					var selectedKey = $(self).find('option:selected').val();
					var selectedTypeIdAndDirection = selectedKey.split("_");
					var selectedTypeId = parseInt(selectedTypeIdAndDirection[0]);
					var selectedTypeDirection = parseInt(selectedTypeIdAndDirection[1]);
					var params = {
						reqVersionLinkTypeId: selectedTypeId,
						reqVersionLinkTypeDirection: selectedTypeDirection
					};
					if (isRelatedIdANodeId) {
						params.isRelatedIdANodeId = self.data("isRelatedIdANodeId");
					}
					return params;
				}

				createLinkTypeDialog.on('formdialogconfirm', onCreateLinkTypeDialogConfirm);

				var onCreateLinkTypeDialogCancel = function () {
					createLinkTypeDialog.formDialog('close');
					deselectTree();
					table().refresh();
					unlock();
				};
				createLinkTypeDialog.on('formdialogcancel', onCreateLinkTypeDialogCancel);
				createLinkTypeDialog.on('formdialogclose', onCreateLinkTypeDialogCancel);

			});



		});
});
