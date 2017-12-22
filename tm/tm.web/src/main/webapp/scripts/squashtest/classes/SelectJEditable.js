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
 * Must specify one of the following : ================================ settings.jeditableSettings.jsonData = string
 * representing the json formated data displayed in the select. Either jsonData or jsonUrl must be defined.
 * 
 * settings.jeditableSettings.jsonUrl = url where to fetch json formated data displayed in the select. Either jsonData
 * or jsonUrl must be defined.
 */

define([ "jquery", "squash.configmanager", "jquery.squash.jeditable" ], function($, confman) {
	/*
	 * settings = { target (target url or target function) componentId ...(jeditable settings) }
	 */
	var SelectJEditable = function(settings) {
		var self = this;
		this.settings = settings;
		var target = settings.target;
		var getUrl = settings.getUrl;
		var componentId = settings.componentId;
		var component = $('#' + componentId);
		this.component = component;
		var txt = component.text();
		component.text($.trim(txt));

		var defaultSettings = confman.getJeditableSelect();

		this.setValue = function(value) {
			component.html(settings.jeditableSettings.data[value]); 
			self.instance.data(value);
		};

		this.getSelectedOption = function() {
			var option = "";
			$.each(settings.jeditableSettings.data, function(key, value) {
				if ($("<span/>").html(value).text() == component.text()) {
					option = key;
				}
			});
			return option;
		};

		// init
		if (settings.getUrl) {
			this.refresh = function() {
				$.ajax({
					type : "get",
					url : settings.getUrl
				}).then(function(value) {
					component.html(value);
					$(self).trigger("selectJEditable.refresh");
				});
			};
		} else {
			this.refresh = function() {
				if (console) {
					console.log("refresh not suported because SelectJEditable.settings.getUrl undefined");
				}
				return;
			};
		}
		var effectiveSettings = $.extend(true, {}, settings.jeditableSettings, defaultSettings);
		this.instance = $(component).editable(target, effectiveSettings).addClass("editable");

	};
	return SelectJEditable;
});
