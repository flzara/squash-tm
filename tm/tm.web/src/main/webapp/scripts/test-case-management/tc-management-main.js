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
define([ "jquery", "./main-structure", "./steps/step-table-panel", "./parameters/ParametersTab", "./infos/InfosTab"], 
		function($, structure, stepTablePanel, ParametersTab, InfosTab) {

	function initStructure(settings){
		structure.init(settings);
	}
	
	var initStepTablePanel = function(settings) {
		stepTablePanel.init(settings);
	};	
	
	var initParametersTab = function(settings) {
		new ParametersTab({settings : settings});
	};
	
	var initInfosTab = function(settings){
		new InfosTab({settings : settings});
	};

	return {
		initStructure : initStructure,
		initStepTablePanel : initStepTablePanel,
		initParametersTab: initParametersTab,
		initInfosTab : initInfosTab
	};
});
