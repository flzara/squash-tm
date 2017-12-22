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
 * A fig leaf view is mostly used to hide the charts when there are no model to back them up. Basically it is something that will display one content 
 * until the model triggers an event, then it will switch to the other content and stay that way.  
 * 
 * The alternate contents are tagged with classes. The actual content has a class "dashboard-figleaf-figures". The fig leaf that will be displayed instead until 
 * a model is available has class "dashboard-figleaf-notready".
 */

define(["jquery", "backbone"], function($, Backbone){
	return Backbone.View.extend({
		
		initialize : function(){
			
			if (this.model.isAvailable()){
				this.show();
			}
			else{
				this.hide();
				this.listenToOnce(this.model, 'change', this.show);
			}
		
		},
		
		show : function(){
			this.$el.find('.dashboard-figleaf-figures').removeClass('not-displayed');
			this.$el.find('.dashboard-figleaf-notready').addClass('not-displayed');
		},
		
		hide : function(){
			this.$el.find('.dashboard-figleaf-figures').addClass('not-displayed');
			this.$el.find('.dashboard-figleaf-notready').removeClass('not-displayed');			
		}
		
	});
});