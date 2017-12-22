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
define(["jquery", "squash.basicwidgets", "contextual-content-handlers", "jquery.squash.fragmenttabs", "bugtracker/bugtracker-panel",
        "dashboard/campaigns-dashboard/campaigns-dashboard-main","favorite-dashboard"], 
		function($, basic, ContentHandlers, Frag, Bugtracker, dashboard, favoriteView){
	
	
	function init(conf){
		
		// basic content
		basic.init();
		
		// name handler
		var nameHandler = ContentHandlers.getSimpleNameHandler();
		nameHandler.identity = conf.basic.identity;
		nameHandler.displayName = "#folder-name";
		
		// tabs
		Frag.init();
		
		// bugtracker
		if (conf.bugtracker.hasBugtracker){
			Bugtracker.load(conf.bugtracker);
		}
		
		// dashboard
		var shouldShowFavoriteDashboard = squashtm.workspace.shouldShowFavoriteDashboard;
		if(shouldShowFavoriteDashboard){
			favoriteView.init();
		} else {
			dashboard.init({
				master : '#dashboard-master',
				cacheKey : 'dashboard-cfold'+conf.basic.identity.resid
			});
		}
		
	}
	
	return {
		init : init,
		initDashboardPanel : function(conf){
			var shouldShowFavoriteDashboard = squashtm.workspace.shouldShowFavoriteDashboard;
			if(shouldShowFavoriteDashboard){
				favoriteView.init();
			} else {
				dashboard.init(conf);
			}
		}
	};
	
});