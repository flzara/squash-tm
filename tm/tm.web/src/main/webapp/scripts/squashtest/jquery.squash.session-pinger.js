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
define([ "jquery", "jquery.ckeditor" ], function($) {
	
	function documentHasCkeditorInstancesVisibles() {
		for (var ckeInstance in CKEDITOR.instances) {
			if (CKEDITOR.instances[ckeInstance].container.isVisible()) {
				return true;
			}
		}
	}
	
	//This object will check the document state regularly to see if an editor is opened.
	//If so, will send a ping to keep the session opened.
	return function() {
		//==============================ATTRIBUTES
		var needToPing = false;
		var pingIntervalTime = 600000;//10 min
		var checkIntervalTime = 180000;//3 min

		var checkInterval = setInterval(function() {
			doForEachCheck();
		}, checkIntervalTime);
		
		//console.log("Pinger initialized");
		//================================FUNCTIONS
		
		//-------------------PING FUNCTIONS
		
		function doPing() {
			//console.log("Ping");
			$.ajax({
				url : squashtm.app.contextRoot + "/ping",
				method : "GET"
			});
		}
		
		//-------------------CHECK FUNCTIONS
		function adaptCheckTime() {
			clearInterval(checkInterval);
			var checkTime = checkIntervalTime;
			if(needToPing){
				checkTime = pingIntervalTime;
			}
			checkInterval = setInterval(function() {
				doForEachCheck();
			}, checkTime);
		}
		
		// at each check we ping if needed and if state changed, adapt frequency of checks.
		function doForEachCheck(){
		//console.log("check");
			var previousNeedToPing = needToPing;
			checkIfNeedToPing();
			if(needToPing){
				doPing();
			}
			if(previousNeedToPing !== needToPing){
				adaptCheckTime();
			}
		}
		
		//checks if the state of the document is requesting to ping the session
		function checkIfNeedToPing() {
			needToPing = documentHasCkeditorInstancesVisibles();
		}		
	};

	
});