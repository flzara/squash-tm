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
 * That module will watch for events related to milestone binding-unbinding and display/hide the relevant warnings 
 * according to the situation. 
 * 
 */
define(["jquery", "workspace.event-bus"], function($, eventBus){
	
	
	function MilestoneNotificationHandler(settings){
		this.settings = settings;
		
		eventBus.onContextual('node.bindmilestones node.unbindmilestones', function(evt, data){
			var id = data.identity,
				delta = data.milestones.length
		
			if (parseInt(id.resid) === settings.resid && id.restype === settings.restype){
				
				var notifiers = $(".milestone-count-notifier");
				
				// display or hide each notifier, 
				// also update the milestones counter.
				notifiers.each(function(idx, notifier){
					var $notifier = $(notifier);
					var nbmilestones = $notifier.data('milestones');
					
					var delta = data.milestones.length;
					delta = (evt.type+'.'+evt.namespace === "node.bindmilestones") ? delta : - delta;
					
					nbmilestones += delta;
					
					if (nbmilestones>1){
						$notifier.show();
					} 
					else{
						$notifier.hide();
					}
					
					$notifier.data('milestones', nbmilestones);
				});
			}
		});
		
	}
	
	return {
		newHandler : function(identity){
			return new MilestoneNotificationHandler(identity);
		}
	}
	
	
	
});