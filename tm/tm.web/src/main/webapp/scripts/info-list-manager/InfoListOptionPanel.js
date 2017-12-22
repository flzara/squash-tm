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
define([ "underscore", "app/BindView", "handlebars", "backbone.validation", "squash.translator", "info-list/IconSelectDialog" ],
	function(_, BindView, Handlebars, Validation, messages, IconPicker) {
	"use strict";

	/**
	 * Renders the icon picker if necessary
	 */
	function renderIconPicker() {
		if (IconPicker.template === undefined) {
			var src = $("#icon-picker-dialog-tpl").html();
			IconPicker.template = Handlebars.compile(src);
		}
		if ($("#icon-picker-dialog").length === 0) {
			$("body").append($(IconPicker.template({})));
		}
	}

	var validationOptions = {
		valid : function(view, prop) {
		// not "success" state because we do not want green fields. yet.
			view.boundControl(prop).setState("xsuccess");
		},
		invalid : function(view, prop, err) {
			console.log(view);
			view.boundControl(prop).setState("error", err);
		}
	};

	var InfoListOptionPanel = BindView.extend({
		viewName: "option",
		wrapper: "#new-option-pane",

		events : {
			"click #add-option" : "onClickAdd",
			"click #choose-sel-opt-icon": "onClickChooseIcon",
		},

		initialize : function() {
			Backbone.Validation.bind(this, validationOptions);
			$(this.wrapper).html(this.render().$el);

			this.listenTo(squashtm.vent, "iconselectdialog:cancelled", this.onIconPickingCancelled);
			this.listenTo(squashtm.vent, "iconselectdialog:confirmed", this.onIconPicked);
		},

		render : function() {
			if (this.template === undefined) {
				var src = $("#new-option-pane-tpl").html();
				InfoListOptionPanel.prototype.template = Handlebars.compile(src);
			}
			this.$el.append(this.template({}));

			return this;
		},

		remove : function() {
			Validation.unbind(this);
			BindView.prototype.remove.apply(this, arguments);
		},

		onClickAdd : function(event) {
			if (this.model.isValid(true)) {
				// Go to InfoListOptionCollection  : add (check default) then NewInfoListPanel : onAddListOption
				squashtm.vent.trigger("list-option:add", {
				  	model : this.model,
				  	source : event,
				  	view : this
				});
			}
		},
		/**
		 * handler of the "choose icon" button (in the "selected" icon panel)
		 * @param event
		 */
		onClickChooseIcon: function(event) {
			renderIconPicker();
			this.iconPicker = this.iconPicker || new IconPicker({ el: "#icon-picker-dialog", model: { icon: this.model.get("iconName") } });
			this.iconPicker.open();
		},

		onIconPicked: function(event) {
			if (event.view !== this.iconPicker) {
				return; // bail out
			}
			var icon = event.model.icon;
			this.model.set("iconName", icon);

			var $optIcon = this.$("#sel-opt-icon");
			$optIcon.attr("class", "");

			if (_.isEmpty(icon)) {
				$optIcon.text($optIcon.data("none"));
			} else {
				$optIcon.addClass("sq-icon sq-icon-" + icon).text("");
			}
			this.onIconPickingCancelled(event);
		},

		onIconPickingCancelled: function(event) {
			if (event.view !== this.iconPicker) {
				return; // bail out
			}
			event.view.remove();
			this.iconPicker = undefined;
		}
	});

	return InfoListOptionPanel;
});