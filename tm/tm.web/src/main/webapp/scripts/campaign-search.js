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
require([ "common" ], function() {
	require([ "jquery", 'camp-workspace/camp-workspace-search' ,"app/ws/squashtm.workspace", "domReady" , "search/advanced-search-input", 'jquery.cookie' ],
			function($, CampWorkspaceSearch,  WS, domReady, AdvancedSearchView) {

		domReady(function() {
			WS.init();
			
			$.cookie("workspace-prefs", null, {
				path : '/'
			});
			CampWorkspaceSearch.init(squashtm.app.campaignWorkspaceConf);
		});
		
		
		
		$(function() {
			WS.init();
			$('#tree').bind("select_node.jstree", function (e, data) { 
        data.rslt.obj.parents('.jstree-closed').each(function () { 
          data.inst.open_node(this); 
        }); 
			}); 
			var view = new AdvancedSearchView();
			
		});
	});
});


