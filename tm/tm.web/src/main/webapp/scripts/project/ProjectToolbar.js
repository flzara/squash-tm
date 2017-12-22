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
 * This is a template for a backbone module
 */
define([ "jquery", "backbone", "handlebars", "jquery.squash.confirmdialog" ], function($, Backbone, Handlebars) {
	function proceedTemplateCoertion(templateId) {
		return function() {
			$.ajax({
				type : "put",
				url : squashtm.app.contextRoot + "/projects/" + templateId,
				data : JSON.stringify({
					mode : "coerce-template",
					templateId : templateId
				}),
				dataType : "json",
				contentType : "application/json"
			}).done(function() {
				document.location.href = squashtm.app.contextRoot + "/administration/projects/" + templateId;
			});
		};
	}

	var View = Backbone.View.extend({
		el : "#project-toolbar",
		initialize : function() {
			var templateId = $("#coerce").data("template-id");

			// we need to store the dialog because jquery stores
			// it somewhere else in the dom.
			this.coerceDialog = this.$el.find("#coerce-warning-dialog").confirmDialog();
			// because it's somewhere else, we cannot bind
			// events using the backbone event property
			this.coerceDialog.on("confirmdialogconfirm", proceedTemplateCoertion(templateId));
		},

		events : {
			"click #coerce" : "confirmTemplateCoertion"
		},

		confirmTemplateCoertion : function(event) {
			var dom = event.source, button = $(dom), templateId = button.data("template-id");

			this.coerceDialog.confirmDialog("open");
		}
	});

	return View;
});