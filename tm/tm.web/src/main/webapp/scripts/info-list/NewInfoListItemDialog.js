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
define(["jquery", "backbone", "underscore", "handlebars", "./IconSelectDialog", "squash.translator", "workspace.routing", "app/lnf/Forms",
		"info-list-manager/InfoListOptionModel", "app/squash.backbone.validation", "app/squash.wreqr.init", "jquery.squash.formdialog"],
	function ($, Backbone, _, Handlebars, IconSelectDialog, translator, routing, Forms, InfoListOptionModel, Validation, squashtm) {
		"use strict";

		translator.load(["label.infoListItems.icon.none",
			"message.optionCodeAlreadyDefined"]);


		/**
		 * Builds a handler function which checks the non-existence of a property.
		 * @param propName the name of the prop to be read from `event.model`
		 * @param urlRoot  the root of the check url. Property shall be appended
		 * @returns {Function} handler to be bound to a request-response event
		 */
		function checkNotExistsHandler(propName, urlRoot) {
			return function (event) {
				var prop = event.model.get(propName);
				var res;

				if (!_.isEmpty(prop)) {
					// we check the code is not defined in the list
					$.ajax({
						url: urlRoot + "/" + encodeURIComponent(prop),
						method: "get",
						async: false,
						data: {format: "exists"},
						success: function (data) {
							res = !data.exists;
						}
					});
				}

				return res;
			}
		}

		squashtm.reqres.setHandler("list-option:code:validate", checkNotExistsHandler("code", squashtm.app.contextRoot + "/info-lists/items/code"));

		var ICON_PREFIX = "sq-icon-";

		var View = Backbone.View.extend({
			el: "#add-info-list-item-popup",

			initialize: function () {
				squashtm.reqres.setHandler("list-option:label:validate",
					checkNotExistsHandler("label", squashtm.app.contextRoot + "/info-lists/" + this.model.listId + "/items/label"));
				this.$el.find("input:text").val("");
				this.render();
				this.$el.formDialog({
					autoOpen: true
				});

			},

			render: function () {
				this.$("#new-info-list-item-icon")
					.attr("class", "")
					.addClass("sq-icon")
					.html(translator.get("label.infoListItems.icon.none"));

				return this;
			},

			events: {
				"formdialogcancel": "cancel",
				"formdialogconfirm": "confirm",
				"formdialogaddanother": "confirmAndReset",
				"click .sq-icon": "openChangeIconPopup"
			},

			cancel: function (event) {
				this.cleanup();
				this.trigger("newOption.cancel");
				this.$el.formDialog("close");
			},

			confirm: function (event) {
				var self = this;
				var url = routing.buildURL('info-list.items', this.model.listId);

				if(this.validate(event)){
					var params = {
						"label": this.model.label,
						"code": this.model.code,
						"iconName": this.model.icon || "noicon"
					};
					$.ajax({
						url: url,
						type: 'POST',
						dataType: 'json',
						data: params
					})
					.success(function(){
						self.cleanup();
						self.trigger("newOption.confirm");
						self.$el.formDialog("close");
					});
				}
			},
			
			confirmAndReset: function (event) {
				var self = this;
				var url = routing.buildURL('info-list.items', this.model.listId);
				
				if(this.validate(event)){
					var params = {
							"label": this.model.label,
							"code": this.model.code,
							"iconName": this.model.icon || "noicon"
					};
					
					$.ajax({
						url: url,
						type: 'POST',
						dataType: 'json',
						data: params
					})
					.success(function(){
						self.cleanup();
						self.render();
						self.model.icon = "noicon";
						self.trigger("newOption.addanother");
					});
				}
			},

			openChangeIconPopup: function () {
				var self = this;

				function discard() {
					self.newIconDialog.off("selectIcon.cancel selectIcon.confirm");
					self.newIconDialog.undelegateEvents();
					self.newIconDialog = null;
				}

				function discardAndRefresh(icon) {
					discard();
					var $icon = $("#new-info-list-item-icon");

					var classList = $icon.attr('class').split(/\s+/);
					classList.forEach(function (item, index) {
						if (item.indexOf(ICON_PREFIX) > -1) {
							$icon.removeClass(item);
						}
					});

					if (icon !== "noicon") {
						$icon.addClass("sq-icon-" + icon);
						$icon.text("");
					} else {
						$icon.text(translator.get("label.infoListItems.icon.none"));
					}
					self.populateModel();
				}

				self.newIconDialog = new IconSelectDialog({
					el: "#choose-item-icon-popup",
					model: {
						icon: this.model.icon
					}
				});

				self.newIconDialog.on("selectIcon.cancel", discard);
				self.newIconDialog.on("selectIcon.confirm", discardAndRefresh);
			},

			validate: function (event) {
				var res = true;
				this.populateModel();
				var self = this;
				Forms.form(this.$el).clearState();

				// We manually use validation rules implemented for list creation - quite inelegant, we should use BB.Model to back this view
				var bbm = _.extend(new InfoListOptionModel(this.model), Backbone.Validation.mixin);
				var errs = bbm.validate();
				if (!!errs) {
					_.chain(errs) // essentially a foreach on the errs object
						.pairs()
						.each(function (pair) {
							Forms.input($("#new-info-list-item-" + pair[0])).setState("error", pair[1]);
						})
					return false;
				}
				//event.preventDefault();
				return res;
			},

			cleanup: function () {
				this.$el.addClass("not-displayed");
				Forms.form(this.$el).clearState();
				this.$el.formDialog('cleanup');
			},

			populateModel: function () {
				var $el = this.$el;
				var self = this;
				self.model.label = $el.find("#new-info-list-item-label").val();
				self.model.code = $el.find("#new-info-list-item-code").val();
				var selected = $el.find("#new-info-list-item-icon");
				var classList = selected.attr('class').split(/\s+/);

				classList.forEach(function (item, index) {
					if (item.indexOf(ICON_PREFIX) > -1) {
						self.model.icon = item.substring(ICON_PREFIX.length);
					}
				});
			},

			remove: function () {
				squashtm.reqres.removeHandler("list-option:label:validate");
				Backbone.View.prototype.remove.apply(this, arguments);
			}
		});

		return View;
	});
