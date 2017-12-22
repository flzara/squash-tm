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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers"],
	function($, backbone, _, Handlebars) {
	"use strict";

	
	
	var sideView = Backbone.View.extend({
		el : "#side-view",
		
		events : {

			
		},

		
		initialize : function(data) {
			this.tmpl = "#side-view-tpl";
			this.model = data;
			this.render(data);
			
			
			var validSteps = this.getValidSteps();
			
			var invalidSteps = this.getInvalidSteps(validSteps, data.name); 
			
			_.each(invalidSteps , function(step){	
				$("#step-icon-" + step).addClass("wizard-step-fail");
			});
			
			
			_.each(validSteps, function(step){	
				$("#step-icon-" + step).addClass("wizard-step-ok");
				$("#step-" + step).addClass("nota-bene");
			});
			
			$("#step-icon-" + data.name).attr('class', '');
			$("#step-icon-" + data.name).addClass("wizard-step-current");
			$("#step-" + data.name).addClass("normal-warning-message");
			
		},
		
		getInvalidSteps : function(validSteps, currStepName){
		
			
			var steps = this.model.get("steps");
			
			var currStepNumber = _.chain(steps)
			.where({name : currStepName})
			.pluck('stepNumber')
			.first()
			.value();
			
			var invalidStep = _.chain(steps)
			.filter(function(step){return step.stepNumber < currStepNumber;})
			.pluck('name')
			.difference(validSteps)
			.value();
			
			return invalidStep;
			
		},		
		getValidSteps : function (data){
			
			var self = this;
			return _.chain(self.model.get("validation"))
			.filter(function(val) {return !_.isEmpty(_.result(self.model.attributes, _.result(val, "validationParam")));   } ).pluck("name")
			.value();
			
		},

		render : function(data, tmpl) {	
				var src = $(this.tmpl).html();
				this.template = Handlebars.compile(src);

			this.$el.append(this.template(data));

			return this;
		},
		
		destroy_view: function() {

		    this.undelegateEvents();
		    this.$el.removeData().unbind(); 
		    this.remove();  
		    Backbone.View.prototype.remove.call(this);
		}
		
	});
	

	

	return sideView;

});