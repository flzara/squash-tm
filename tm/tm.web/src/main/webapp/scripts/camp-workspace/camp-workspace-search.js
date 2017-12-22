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
define([ 'tree', './camp-treemenu', './popups/init-all', './init-actions', 'squash/squash.tree-page-resizer' , 'app/ws/squashtm.toggleworkspace'],
		function(tree, treemenu, popups, actions, resizer, ToggleWorkspace) {

			function initResizer() {
				var conf = {
					leftSelector : "#tree-panel-left",
					rightSelector : "#contextual-content"
				};
				resizer.init(conf);
			}

			function initTabbedPane() {
				/**
				 * Here we define which tab is currently used with selectedTab. selectedTab is used in
				 * show-iteration-test-plan-manager, search-panel*...
				 */
				// The selected pane number. Always the first one (0) by default
				selectedTab = 0;

				$("#tabbed-pane").tabs();

				$("#tree-panel-left").on("tabsselect", "#tabbed-pane", function(event, ui) {
					// change the number of the selected pane
					selectedTab = ui.index;
				});
			}
			
			function initSelectionCampaign() {
				
				$("#getAllTheCampaigns").on("click", function(){
						if($("#getAllTheCampaigns").prop("checked") ) {
							$("#tree").find('li').filter(':library').treeNode().deselect();
							$("#tree").jstree('findNodes', { restype : 'campaign-libraries'}).select();
						}
						else {
							$("#tree").find('li').filter(':library').treeNode().deselect();
						}
				});
				}	
			
			function init(settings) {
				initResizer();
				initTabbedPane();
				ToggleWorkspace.init(settings.toggleWS);
				tree.initSearchTree(settings.tree);
				initSelectionCampaign();
			}

			return {
				init : init
			};

		});
