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

		var nameStepView = AbstractStepView.extend({

			initialize: function (data, wizrouter) {
				this.tmpl = "#name-step-tpl";
				this.model = data;
				data.name = "name";
				this._initialize(data, wizrouter);
				this.updateSaveButtonStatus();
				this.reloadModelInView();
			},

			events: {
			},

			updateSaveButtonStatus: function() {
				if(!this.previousStepsAreValid()) {
					$('#save').attr('disabled', true);
				} else {
					$('#save').attr('disabled', false);
				}
			},

			updateModel: function () {
				this.model.set("name", $("input#custom-export-name").val());
			},

			save: function() {
				/* Here we save the new Custom Export ! */
				this.updateModel();

				var parentId = this.model.get("parentId");
				var cookiePath = this.getCookiePath();

				var targetUrl;
				if(this.isUpdatingAnExistingExport()) {
					targetUrl = router.buildURL('custom-report.custom-export.update', parentId);
				} else {
					targetUrl = router.buildURL('custom-report.custom-export.new', parentId);
				}
					// Ajax POST request to save the Custom Export =)
					$.ajax({
						method: "POST",
						contentType: "application/json",
						url: targetUrl,
						data: this.model.toJson()
					}).done(function (newCustomExportId) {
						var nodeToSelect = "CustomExport-" + newCustomExportId;
						$.cookie("jstree_select", nodeToSelect, {path: cookiePath});
						window.location.href = router.buildURL("custom-report.custom-export.redirect", newCustomExportId);
					});
			},

			reloadModelInView: function() {
				$("input#custom-export-name").val(this.model.get("name"));
			},

			/**
       * IE and FF add a trailing / to cookies...
       * Chrome don't...
       * So we need to put the good path to avoid two jstree_select cookies with differents path.
       */
      getCookiePath : function () {
      	var path = "/squash/custom-report-workspace";
      	if (is.ie() || is.firefox()) {
      		path = path + "/";
      	}
      	return path;
      }

		});

		return nameStepView;

	});
