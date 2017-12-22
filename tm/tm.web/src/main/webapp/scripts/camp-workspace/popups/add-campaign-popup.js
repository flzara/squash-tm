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
define(['jquery', 'tree', 'custom-field-values', 'workspace.projects', '../permissions-rules', 'jquery.squash.formdialog'], 
		function($, zetree, cufValuesManager, projects, rules){
	
	function postNode(dialog, tree){

		var params = {
			name : dialog.find('#add-campaign-name').val(),
			reference : dialog.find('#add-campaign-reference').val(),
			description : dialog.find('#add-campaign-description').val()
		};
		
		var cufParams = dialog.data('cuf-values-support').readValues();
		
		$.extend(params, cufParams);
		
		return tree.jstree('postNewNode', 'new-campaign', params, true);
	}
	
	
	function addCufHandler(dialog, tree){
		var table = dialog.find('table.add-node-attributes');
		var cufHandler = cufValuesManager.newCreationPopupCUFHandler({table : table});
		
		dialog.on('formdialogopen', function(){
			var projectId = tree.jstree('get_selected').getProjectId();
			var bindings = projects.findProject(projectId).customFieldBindings['CAMPAIGN'];
			var cufs = $.map(bindings, function(b){return b.customField;});
			
			cufHandler.loadPanel(cufs);		
		});
		
		
		dialog.on('formdialogcleanup', function(){
			cufHandler.reset();
		});
		
		dialog.on('formdialogclose', function(){
			cufHandler.destroy();
		});
		
		dialog.data('cuf-values-support', cufHandler);
	}
	
	function init(){
		
		var dialog = $("#add-campaign-dialog").formDialog();
		var tree = zetree.get();
		
		// Added to cancel the open if no rights
		dialog.on('formdialogopen', function(){
			var node = tree.jstree('get_selected');
			
			if (! rules.canCreateCampaign(node)){
				/* Acknowledged by Safi, David and Gregory : 
				 * Inactivated buttons and item-menu should not be clickable. 
				 * No error popup should be visible despite the fact a lot of these error popup have been implemented since 1.11.
				 * Due to event flow bug in jQuery, we cant't prevent the popup 'onopen' event to be triggered,
				 * so it was decided to close the popup immediately if rules check function return false,
				 * and inactivate the code dedicated to the error message.
				 * See Issue 4972
				 * */
				dialog.formDialog('close');
//				var errorState;
//				switch (rules.whyCantCreate(node)){
//				case 'milestone-denied' : errorState = 'milestone-denied'; break;
//				case 'permission-denied': errorState = 'permission-denied';break;
//				default : throw "there is a bug : permission-rules said you can't create a node but you actually can";
//				}
//				dialog.formDialog('setState', errorState);
			}
			else{
				dialog.formDialog('setState','confirm');
				var name = node.getName();
				dialog.find("#new-campaign-tree-button").val(name);				
			}			
		});

		// end
	
		dialog.on('formdialogadd-close', function(){
			postNode(dialog,tree).then(function(){
				dialog.formDialog('close');
			});			
		});
		
		dialog.on('formdialogadd-another', function(){
			postNode(dialog, tree).then(function(){
				dialog.formDialog('cleanup');
			}) ;		
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
		
		addCufHandler(dialog, tree);
		
	}
	
	return {
		init : init
	};

});