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
 * projectSelecteor JQuery ui widget with a client side templating. The render method has to provide an HTML markup compatible with the thymeleaf template (project-picker.frag.html)
 * as the AbstractProjectFilterPopup use it in it's original form.'
 */
 define(["./AbstractProjectFilterPopup", "handlebars",],
		function(AbstractProjectFilterPopup, Handlebars) {

	var ProjectSelectorPopup = AbstractProjectFilterPopup.extend({
	
		initialize :function(options){
			var self = this;
			this.options = options;
			this.render()._initialize();
		},

		render : function() {
			var templateSelector = this.options.templateSelector;
			if (templateSelector){
				var source = $(templateSelector).html();
				var template = Handlebars.compile(source);
				this.$el.append(template(this.options.initialProjectModel));
			} else {
				throw "you must specify a template selector to render this dialog with handlebar, client side";
			}
			return this;
		},
		
		confirm : function(){
			this.$el.find("table tbody tr").each(function() {
				var $checkbox = $(this).find(".project-checkbox");
				var checked = $checkbox.is(":checked");
				$checkbox.data("previous-checked", checked);
				
			});
			this.trigger("projectPopup.confirm");
		}
	});
	
	return ProjectSelectorPopup;
});
