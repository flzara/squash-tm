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
define([ "jquery", "backbone", "workspace.routing", "./abstractStepView" ], function($, Backbone, router, abstractStepView) {

		
	var wizardRouter = Backbone.Router.extend({
		
		initialize : function(options) {
			this.wizardView = options.wizardView;
		},
		
		routes : {
			"" : "perimeterStep",
			"perimeter": "perimeterStep",
			"filter" : "filterStep",
			"type" : "typeStep",
			"axis" : "axisStep",
			"preview" : "previewStep",
			"attributes":"attributesStep"

		}, 

		
		perimeterStep : function() {
			this.wizardView.showPerimeterStep(this);			
		},
		filterStep :  function() {
			this.wizardView.showFilterStep(this);
		},
		typeStep :  function() {
			this.wizardView.showTypeStep(this);
		},
		axisStep :  function() {
			this.wizardView.showAxisStep(this);
		},
		previewStep : function(){
			this.wizardView.showPreviewStep(this);
		},
		attributesStep : function(){
			this.wizardView.showAttributesStep(this);
		}
		
	});
	
	return wizardRouter;

});


