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
define(["jquery", "backbone", "squash.dateutils"], function($, Backbone, dateutils){

	return Backbone.View.extend({
		
		initialize : function(){
			// matches the following : 'blahb...{PICKME !}blablahblah...{PICKME TOO !}
			// PICKME : dateformat, PICKME TOO = time format.
			var expr = /.*?\{(.*?)\}.*?\{(.*?)\}.*/;	
			
			var matched = expr.exec( this.$el.text() );
			if (matched.length !== 3){
				throw "dashboard : invalid format. Found : '"+this.$el.text()+"', requires format : 'blahblah {date format} blahblah {time format}'";
			}
			
			this.dateformat = matched[1];
			this.timeformat = matched[2];
			
			this.template = this.$el.text();
			
			this.render();
			
			this.listenTo(this.model, 'change:timestamp', this.render);
			
		},
		
		render : function(){
			var timestamp = this.model.get('timestamp');
			if (!! timestamp){
				
				var strdate = dateutils.format(timestamp,this.dateformat);
				var strtime = dateutils.format(timestamp,this.timeformat);
				
				var txt = this.template.replace(/\{.*?\}/, '<span style="font-weight:bold">'+strdate+'</span>')
										.replace(/\{.*?\}/, '<span style="font-weight:bold">'+strtime+'</span>');
				
				this.$el.html(txt);
				this.$el.show();
				
			}
		}
		
	});	
	
});