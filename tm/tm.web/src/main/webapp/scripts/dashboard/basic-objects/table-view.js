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
/*
 * This is your plain table. When its model is updated, will render again. Note that 
 * it must be subclassed and implement getData. 
 * 
 * DOM configuration :
 * 
 * 1/ on the <table> tag :
 * use the usual 'data-def' attributes : 
 * - model-attribute : (optional) If set, will consider only this attribute of the model and not the whole model.
 * 
 * 2/ on the <tbody>
 * 
 * one can define sereval rows here, that will be interpreted as templates.
 * - <tr class="dashboard-table-template-emptyrow"> : 
 *  that row will be used when there are no data to display. 
 *  Default  : empty row 
 *  
 * <tr class="dashboard-table-template-datarow"> : 
 *  this is a handlebar template, which context will be a row data (an array). Useful if you want custom css for your tds. 
 *  Default : will generate rows and tds that work, with no css.
 * 
 */

define(["jquery", "backbone", "squash.attributeparser", "handlebars"], 
		function($, Backbone, attrparser, Handlebars){
	
	
	return Backbone.View.extend({
		
		DEFAULT_DATAROW : "<tr> {{#each this}}<td>{{this}}</td>{{/each}} </tr>" ,

		
		getData : function(){
			throw "must be override. Must return [][] (array of array).";
		},
		
		initialize : function(){
			this._readDOM();
			this.render();
			this._bindEvents();
		},
		
		render : function(){
			
			if (! this.model.isAvailable()){
				return;
			}
			
			var body = this.$el.find('tbody');
			body.empty();
			
			var data = this.getData();
			
			if (data.length===0){
				body.append(this.emptyrowTemplate.clone());
			}
			else{
				var i=0,
					len=data.length;
				for (i=0;i<len;i++){
					var r = this.datarowTemplate(data[i]);
					body.append(r);
				}
				
			}
		},
		
		_readDOM : function(){
			var body = this.$el.find('tbody');
			
			// empty row template
			var emptyrow = body.find('tr.dashboard-table-template-emptyrow');
			this.emptyrowTemplate = (emptyrow.length>0) ? emptyrow : $('<tr></tr>');

			// data row template
			var datarow = body.find("tr.dashboard-table-template-datarow");
			var dtrtpl = (datarow.length>0) ? "<tr>"+datarow.html()+"</tr>" : this.DEFAULT_DATAROW;		
				
			this.datarowTemplate = Handlebars.compile(dtrtpl);
			
			var strconf = this.$el.data('def');
			var conf = attrparser.parse(strconf);
			if (conf['model-attribute']!==undefined){
				this.modelAttribute = conf['model-attribute'];
			}
		},
		
		_bindEvents : function(){
			var self = this;
			var modelchangeevt = "change";
			if (this.modelAttribute!==undefined){
				modelchangeevt+=":"+this.modelAttribute;
			}
			this.listenTo(this.model, modelchangeevt, this.render);
		}
		
	});
	
	
});