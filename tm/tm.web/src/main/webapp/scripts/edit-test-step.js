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
	require([ "domReady", "jquery", "workspace.routing", "squash.basicwidgets", "test-step-editor/TestStepModificationView",
			"app/ws/squashtm.workspace",  "./execution-helper" ],
			function(domReady, $, routing, basic, TestStepModificationView, WS, execHelper) {
		
		        var fromExec = squashtm.app.fromExec;
		        var isIEO = squashtm.app.isIEO;
		
				var closeWindow = function() {
					/* Allow the parent to be refreshed */
					/* We can now access from informations where there's no table to refresh */
					if (window.opener.squashtm.app.reloadSteps() !== undefined) {
						window.opener.squashtm.app.reloadSteps();
					}
					window.close(); 
				};
				
				var returnToExec = function (){
					
					$.ajax({
						method:"POST",
						url: routing.buildURL('execution.updateExecStep', fromExec)					
					}).done(function(result){
				
						var index;
						var newWindow;
						
						if (result >= 0){
							 index = result;	
						} else {
							index = localStorage.getItem("squashtm.execModification.index") || 0;
						}
						var url = routing.buildURL('execute.stepbyindex', fromExec, index) + "?optimized=" +  isIEO;
						
						if(isIEO){
							url = routing.buildURL('executions.runner', fromExec) + "/" + index + "?optimized=" +  isIEO; 
								newWindow = window.open(url);
							
						} else {
						
						var winDef = {
								name : "classicExecutionRunner",
								features : "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised"
							};
						
						newWindow = window.open(url, winDef.name, winDef.features);
						}
						
						newWindow.opener = window.opener;
						window.close();
					});
					
				};

				domReady(function() {
					basic.init();
					WS.init();
					new TestStepModificationView();
					var returnFn = fromExec ? returnToExec : closeWindow;
					$("#close").button().on("click", returnFn);

				});

			});
});
