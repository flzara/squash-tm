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
define(["jquery", "backbone", "handlebars", "../domain/BTEntity", "text!./default-view-template.html!strip","jqueryui"], function($, Backbone, Handlebars, BTEntity,  source){
//define(["jquery", "backbone", "handlebars", "../domain/BTEntity", "jqueryui"], function($, Backbone, Handlebars, BTEntity, source){


	
	var DefaultFieldControl = Backbone.View.extend({
		
		initialize : function(options){
			this.options = options;
		},
		
		updatemodel : function(){
			var attribute = this.options.attribute;
			var selection = this.get();
			var newValue = {};
			newValue[attribute] = selection;
			
			this.model.set(newValue);
		},
		
		updatecontrol : function(){
			var attribute = this.options.attribute;
			var value = this.model.get(attribute);			
			this.set(value);			
		},
		
		get : function(){
			return this.$el.val();
		},
		
		set : function(newv){
			this.$el.val(newv);
		},
		
		disable : function(){
			this.$el.attr('disabled', 'disabled');
		},
		
		enable : function(){
			this.$el.removeAttr('disabled');
		}		
	});
	
	var ComboBox = DefaultFieldControl.extend({
		
		initialize : function(options){
			this.options = options;
			this.empty = (this.$el.find('option.issue-control-empty').length!==0);
			if (!!this.empty){
				this.disable();
			}
		},
		
		updatecontrol : function(){
			if (! this.empty){
				DefaultFieldControl.prototype.updatecontrol.apply(this, arguments);
			}
		},
		
		get : function(){
			var $el = this.$el;
			var id = $el.val();
			var name = $el.find("option:selected").text();
			return new BTEntity(id,name);
		},
		
		set : function(newv){
			var id = (newv===null || newv===undefined) ? this.$('option:first').val() : newv.id;
			this.$el.val(id);
		},
		
		enable : function(){
			if (! this.empty){
				this.$el.removeAttr('disabled');
			}
		}
		
	});

	
	var DefaultFieldView = Backbone.View.extend({
		
		
		// ****************** controls*******************
		
		remapControls : function(){
			
			//var labels = this.options.labels;	//TODO : use them some day ?
			
			this.controls = [
				new ComboBox({
					el : this.$(".priority-select").get(0),
					model : this.model,
					attribute : 'priority'
				}),
				
				new ComboBox({
					el : this.$(".category-select").get(0),
					model : this.model,
					attribute : 'category'
				}),
				
				new ComboBox({
					el : this.$(".version-select").get(0),
					model : this.model,
					attribute : 'version'
				}),
				
				new ComboBox({
					el : this.$(".assignee-select").get(0),
					model : this.model,
					attribute : 'assignee'
				}),
				
				new DefaultFieldControl({
					el : this.$(".summary-text").get(0),
					model : this.model,
					attribute : 'summary'
				}),
				
				new DefaultFieldControl({
					el : this.$(".description-text").get(0),
					model : this.model, 
					attribute : 'description'
				}),
				
				new DefaultFieldControl({
					el : this.$(".comment-text").get(0),
					model : this.model, 
					attribute : 'comment'
				})
			];
		},
		
		enableControls : function(){			
			var controls = this.controls;
			for (var i=0,l = controls.length;i<l;i++){
				controls[i].enable();
			}
				
		},
		
		disableControls : function(){				
			var controls = this.controls;
			for (var i=0,l = controls.length;i<l;i++){
				controls[i].disable();
			}	
		},
		
		readIn : function(){
			var controls = this.controls;
			for (var i=0,l = controls.length;i<l;i++){
				controls[i].updatecontrol();
			}				
		},
		
		readOut : function(){
			var controls = this.controls;
			for (var i=0,l = controls.length;i<l;i++){
				controls[i].updatemodel();
			}				
		},

		//we must prevent keypress=enter event inside a textarea to bubble out and reach 
		//the submit button
		abortEnter : function(evt){
			if (evt.which == '13'){
				$.Event(evt).stopPropagation();
			}			
		},

		// *********************** events *******************
		
		events : {
			"keypress .text-options" : "abortEnter"
		},
		
		
		// *********************** life cycle ***************

		
		initialize : function(options){
			
			this.options = options;
			
			var template = Handlebars.compile(source);
			
			var data = {
				issue : this.model.attributes,
				labels : this.options.labels
			};
			
			var html = template(data);			
			
			this.$el.html(html);
			
			this.remapControls();
			
			return this;

		}
		
	});
	
	return DefaultFieldView;
	
});
