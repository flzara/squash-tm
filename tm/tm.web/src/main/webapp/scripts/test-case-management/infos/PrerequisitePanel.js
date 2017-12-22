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
define([ "jquery", "backbone", "underscore", "squash.configmanager", "jquery.squash.jeditable"],
		function($, Backbone, _, confman ) {
	
			var PrerequisitePanel = Backbone.View.extend({
				
				el : "#test-case-prerequisite-panel",
				
				initialize : function(options) {
					this.settings = options.settings;
						
					if(this.settings.writable){
						
						var richEditSettings = confman.getJeditableCkeditor();
						richEditSettings.url = this.settings.urls.testCaseUrl;

						$('#test-case-prerequisite').richEditable(richEditSettings).addClass("editable");
					}
						
				},
				
				events : {
					
				}

			});
			return PrerequisitePanel;
});