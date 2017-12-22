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
define([ "jquery", "squash.configmanager", "jquery.squash.jeditable" ], function($, confman) {
/*
 * settings = {
 *  targetUrl
 *  componentId or component
 *  submitCallback [function(value, settings)]
 *  jeditableSettings
 * }
 */
	var SimpleJEditable = function(settings) {
		var self = this;
		this.settings = settings;
		var targetUrl = settings.targetUrl;
		var component;
		if (settings.componentId) {
			var componentId = settings.componentId;
			component = $('#' + componentId);
		} else {
			component = settings.component;
		}
		var txt = component.text();
		component.text($.trim(txt));

		var defaultSettings = confman.getStdJeditable();
		
		if(!settings.jeditableSettings){
			settings.jeditableSettings = {};
		}
	
		if(settings.submitCallback){			
			settings.jeditableSettings.callback = function(value, settings2){
				value = $("<span/>").html(value).text();
				self.settings.submitCallback(value, settings2);
			};
		}
		
		this.refresh = function(){
			
		};
		var effectiveSettings = $.extend(true, {}, settings.jeditableSettings, defaultSettings);
		this.instance = $(component).editable(targetUrl, effectiveSettings).addClass("editable");

	};
	return SimpleJEditable;
});
