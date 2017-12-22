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
define(['jquery', './utils', './permissions-rules',
        'workspace/WorkspaceWizardMenu',
        'jquery.squash.buttonmenu'], function($, utils, permissions, WizardMenu){
 

	function createWidgets(){
		$("#tree-create-button").buttonmenu();
		$("#tree-import-button").buttonmenu();
	}
	
	function decorateEnablingMethods(buttons){
		var i=0, len = buttons.length;
		
		function cssenable(){
			this.removeClass("disabled ui-state-disabled");
		}
		
		function cssdisable(){
			this.addClass("disabled ui-state-disabled");
		}
		
		function menuenable(){
			this.buttonmenu('enable');
		}
		
		function menudisable(){
			this.buttonmenu('disable');
		}
		
		for (i=0;i<len;i++){
			var jqbtn = buttons[i];
			if (jqbtn.attr('role') === "buttonmenu"){
				jqbtn.enable = menuenable;
				jqbtn.disable = menudisable;
			}
			else{
				jqbtn.enable = cssenable;
				jqbtn.disable = cssdisable;
			}
		}
	}
	

	
	// the wizard menu is a bit different from the rest, hence the init code
	// is put appart
	function createWizardMenu(wizards){
		
		if (!!wizards && wizards.length>0){
			
			var wmenu = new WizardMenu({
				collection : wizards
			});
			
			var tree = $("#tree");
			
			//state init
			wmenu.refreshSelection(tree.jstree("get_selected"));
			
			//evt binding
			tree.on('select_node.jstree deselect_node.jstree deselect_all.jstree', function(evt, data){
				wmenu.refreshSelection(data.inst.get_selected());
			});
		}
	}
	
	
	function initExportPlugins(){
		var plugins = $("#tree_element_menu .export-plugin");
		var modules = plugins.map(function(idx, elt){
			var modulename = $(elt).data('module');
			return require.toUrl(modulename);
		}).get();
		var items = plugins.get();
		
		require(modules, function(){
			var i, len = modules.length;
			for (i=0;i<len;i++){
				var module = arguments[i],
					item = items[i];
				module.init(item);
			}
		});
	}
	
	function init(settings){
		createWidgets();
		createWizardMenu(settings.wizards);
		initExportPlugins();

		$("#tree_element_menu").removeClass("unstyled-pane");
	}
	
	
	
	return {
		
		init : init
		
	};
	
});