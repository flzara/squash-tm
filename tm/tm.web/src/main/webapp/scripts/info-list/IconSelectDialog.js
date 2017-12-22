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
define([ "jquery", "backbone", "handlebars", "underscore", "workspace.routing", "jquery.squash.formdialog" ], function($, Backbone, Handlebars, _, routing) {
	"use strict";
	var ICON_PREFIX = "sq-icon-";

	var View = Backbone.View.extend({
		initialize : function() {

			if (!_.isEmpty(this.model.icon) && this.model.icon.indexOf(ICON_PREFIX) !== 0) {
				// i wanna be consistent with output model which is icon name, not icon class
				// yet i dont change existing code coz "no refactoring BS"
				this.model.icon = ICON_PREFIX + this.model.icon;
			}

			this.initIcon();
			this.$el.formDialog({
				autoOpen : true,
				width : 800
			});
		},

		events : {
			"click .sq-icon" : "selectIcon",
			"formdialogcancel" : "cancel",
			"formdialogconfirm" : "confirm",
			"formdialogclose" : "close"
		},

		initIcon : function(){
			var $icons = this.$icons();
			//clean the style

			$icons.removeClass("info-list-item-icon-selected")
				.removeClass("low-opacity");

			//if icon is selected add correct style
			if (!_.isEmpty(this.model.icon) && this.model.icon !== "sq-icon-noicon") {
				$icons.addClass("low-opacity");
				var selected = this.$("." + this.model.icon);
				selected.addClass("info-list-item-icon-selected");
				selected.removeClass("low-opacity");
			}

		},

		"$icons": function() {
			return this.$(".sq-icon");
		},

		selectIcon : function(event){
			var $clicked = $(event.currentTarget);
			$clicked.toggleClass("info-list-item-icon-selected");

			if ($clicked.hasClass("info-list-item-icon-selected")) {
				$clicked.removeClass("low-opacity");
				this.$icons().not($clicked).removeClass("info-list-item-icon-selected").addClass("low-opacity");
			} else {
				this.$icons().removeClass("low-opacity");
			}
		},


		open : function(){
			this.$el.formDialog("open");
		},

		close : function(event){
			this.cancel(event);
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("selectIcon.cancel");
			if (!!window.squashtm.vent) {
				window.squashtm.vent.trigger("iconselectdialog:cancelled", { model: this.model, view: this, source: event });
			}
		},

/**
 * Upon confirmation, will trigger a "iconselectdialog:confirmed" event using squashtm.vent if it exists.
 * event is { model: { icon: <icon name> }, view: this, source: domEvent }
 * @param event
 */
		confirm : function(event) {
			this.cleanup();

			var icon;
			var classes = this.$(".info-list-item-icon-selected").attr("class");

			if (classes !== undefined) {
				var iconClass = _.find(classes.split(" "), function(it) {
					return it.indexOf(ICON_PREFIX) === 0;
				});

				if (iconClass !== undefined) {
					icon = iconClass.substring(ICON_PREFIX.length);
				}
			}

			this.model.icon = icon;
			this.trigger("selectIcon.confirm", icon === undefined ? "noicon" : icon);

			if (!!window.squashtm.vent) {
				window.squashtm.vent.trigger("iconselectdialog:confirmed", {
					model : this.model,
					view : this,
					source : event
				});
			}
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			// if we destroy twice, jqui blows up
			this.$el.hasClass("ui-dialog-content") && this.$el.formDialog("destroy");
		},

		remove : function() {
			this.cleanup();
			this.undelegateEvents();
			Backbone.View.prototype.remove.apply(this, arguments);
		}

	});

	return View;
});
