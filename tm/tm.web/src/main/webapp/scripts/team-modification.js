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
require([ "common" ], function(common) {
	require([ "jquery", "team-editor/TeamModificationView", "app/ws/squashtm.workspace", "domReady" ], 
				function($, TeamModificationView, WS, domReady) {
		
		var goBackInHistory = function(){
			history.back();
		};
		
		var goBack = function() {
			document.location.href = squashtm.app.contextRoot + "/administration/users/list";
		};

		domReady(function() {
			WS.init();
			var view = new TeamModificationView();
			$("#back").on("click", goBackInHistory);
			view.on("team.delete", goBack);
		});

	});
});
