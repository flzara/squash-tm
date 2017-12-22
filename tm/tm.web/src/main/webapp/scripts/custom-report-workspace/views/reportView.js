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
define(["underscore", "backbone", "squash.translator", "handlebars", "squash.dateutils",
		"workspace.projects", "workspace.routing"],
	function (_, Backbone, translator, Handlebars, dateutils, projects, urlBuilder) {
		"use strict";

		var View = Backbone.View.extend({

			el: "#contextual-content-wrapper",
			tpl: "#tpl-show-report",

			initialize: function (options) {
				this.options = options;

				this.i18nString = translator.get({
					"dateFormat": "squashtm.dateformat",
					"dateFormatShort": "squashtm.dateformatShort"
				});
				_.bindAll(this, "render");
				this.render();
			},

			events: {
				"click #refresh-btn": "refresh",
				"click #modify-report-button": "modifyReport",
				"click #rename-report-button": "rename",
				"click #export-report-button": "export"
			},

			render: function () {
				this.$el.html("");
				var self = this;
				var url = urlBuilder.buildURL('custom-report-report-server', this.model.get('id'));

				this.options.acls.fetch({})
					.then(function () {
						return $.ajax({
							'type': 'get',
							'dataType': 'json',
							'contentType': 'application/json',
							'url': url
						});

					}).then(function (json) {

					self.setBaseModelAttributes(json);

					self.template();

					url = urlBuilder.buildURL('reports') + json.pluginNamespace + '/panel/' + self.model.get('id');
					$.ajax({
						'type': 'get',
						'url': url
					}).done(function (html) {
						$("#reportDetail").html(html);
						$("#report-information-panel").hide();
						$("#report-name-div").hide();
						$("#report-criteria-panel").hide();
						$("#report-attributs").show();
						$("#view-tabed-panel").parent().removeClass("fragment-body");

					});

				});

			},


			template: function () {
				// TODO maybe template could be compiled only once -> store it someplace
				var source = $("#tpl-show-report").html();
				var template = Handlebars.compile(source);

				var props = this.model.toJSON();
				props.acls = this.options.acls.toJSON();

				this.$el.append(template(props));


			},

			setBaseModelAttributes: function (json) {
				this.model.set("name", json.name);
				this.model.set("createdBy", json.createdBy);
				this.model.set("createdOn", (this.i18nFormatDate(json.createdOn) + " " + this.i18nFormatHour(json.createdOn)));
				if (json.lastModifiedBy) {
					this.model.set("lastModifiedBy", json.lastModifiedBy);
					this.model.set("lastModifiedOn", (this.i18nFormatDate(json.lastModifiedOn) + " " + this.i18nFormatHour(json.lastModifiedOn)));
				}
			},

			i18nFormatDate: function (date) {
				return dateutils.format(date, this.i18nString.dateFormatShort);
			},

			i18nFormatHour: function (date) {
				return dateutils.format(date, "HH:mm");
			},

			getI18n: function (key) {
				return " " + translator.get(key);
			},

			modifyReport: function () {
				var nodeId = this.model.get('id');
				var url = urlBuilder.buildURL("report-workspace", nodeId);
				document.location.href = url;
			},

			rename: function () {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("renameNode");
			},

		});

		return View;
	});
