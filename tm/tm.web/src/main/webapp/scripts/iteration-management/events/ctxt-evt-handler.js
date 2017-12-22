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
 * conf : {
 *	//no conf needed yet, but that may change later.
 * }
 * 
 */
define(['jquery', 'workspace.event-bus' ], function($, eventBus) {

	
	return {
		init : function(conf){
			
			eventBus.onContextual('context.content-modified', function(evt, args){	
				
				$("#iteration-test-plans-table").squashTable().refresh();           
				
                if (args && args.newDates){
	                actualStart.refreshAutoDate(args.newDates.newStartDate);
	                actualEnd.refreshAutoDate(args.newDates.newEndDate);
                }
                
			});		
			eventBus.onContextual('node.bind', function(evt, args){	
				$("#iteration-test-plans-table").squashTable().refresh();           
			});
		}		
	};
	
});