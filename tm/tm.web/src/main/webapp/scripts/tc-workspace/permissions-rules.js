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
define(['jquery', 'workspace.tree-node-copier', 'tree', 'milestone-manager/milestone-activation'], function($, copier, tree, milestones){

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};
	
	if (squashtm.workspace.permissions_rules === undefined){
		squashtm.workspace.permissions_rules = new TestCasePermissionsRules();
	}
	
	return squashtm.workspace.permissions_rules;
	
	
	/* ******************************************************************
	 * Predicates on the milestones 
	 * 
	 * Test test does the following : 
	 * 1 - test if the active milestone allows the operation
	 * 2 - also, if a 'nodes' argument is supplied, test if the other milestones 
	 *		to which those nodes are bound too also allow the operation
	 * 
	 * *****************************************************************/
	
	// operation : 'canCreateDelete' || 'canEdit'
	// metaattr : 'milestone-creatable' || 'milestone-editable'
	function milestonesAllowOperation(nodes, operation, metaattr){
		
		var allowed = true;

		var activeMilestone = squashtm.app.testCaseWorkspaceConf.activeMilestone; 
		
		var activeAllowed = true,
			nodesAllowed = true;
		// no nodes -> check the active milestone
		// no active milestone -> true
		activeAllowed = (!! activeMilestone) ? activeMilestone[operation] : true;
		
		if (nodes !== undefined){
			// nodes : they must all allow the operation
			nodesAllowed = (nodes.filter(':'+metaattr).length === nodes.length);
		}
		
		allowed = activeAllowed && nodesAllowed;
	
	
		return allowed;
	}
	
	function milestonesAllowCreation(nodes){
		return milestonesAllowOperation(nodes, 'canCreateDelete', 'milestone-creatable');
	}
	
	function milestonesAllowEdition(nodes){
		return milestonesAllowOperation(nodes, 'canEdit', 'milestone-editable');
	}
	
	
	/* *******************************************************
	 * Main Object 
	 *********************************************************/
	
	function TestCasePermissionsRules(){		
		
		this.milestonesAllowCreation = milestonesAllowCreation;
		
		this.milestonesAllowEdition = milestonesAllowEdition;
		

		this.canCreateButton = function(nodes){
			return milestonesAllowCreation(nodes) && nodes.filter(':creatable').length === 1;
		};
		
		this.canCreateFolder = function(nodes){
			return milestonesAllowCreation(nodes) && nodes.filter(':creatable').filter(':folder, :library').length === 1;
		};
		
		this.canCreateTestCase = function(nodes){
			return milestonesAllowCreation(nodes) && nodes.filter(':creatable').length === 1;
		};
		
		this.whyCantCreate = function(nodes){
			if (! milestonesAllowCreation()){
				return "milestone-denied";
			}
			else if (! this.canCreateButton(nodes)){
				return "permission-denied";
			}
			else {
				return "yes-you-can";
			}
		};

		// must be not empty, and not contain libraries.
		this.canCopy = function(nodes){
			return (nodes.length > 0) && (! nodes.is(':library'));
		};
		
		this.whyCantCopy = function(nodes){
			if (nodes.length===0){
				return "empty-selection";
			}
			
			if (nodes.is(':library')){
				return "no-libraries-allowed";
			}
			
			return "yes-you-can";
		};
		
		
		/*
		 * Milestone mode : the recipient must allow creation.
		 * 
		 * Rest of the rules is standard stuff.
		 */
		this.whyCantPaste = function(){
			
			var nodes = copier.bufferedNodes(); 
			
			if (nodes.length===0){
				return "empty-selection";
			}
			
			var target = nodes.tree.get_selected();
			
			if (target.length !== 1){
				return "not-unique";
			}
			
			if (! target.isCreatable()){
				return "not-creatable";
			}
			
			if (! target.acceptsAsContent(nodes)){
				return 'invalid-content';
			}
			
			if (! milestonesAllowCreation(target)){
				return "milestone-denied";
			}
			
			return "yes-you-can";
		};
			
		this.canPaste = $.proxy(function(nodes){
			return (this.whyCantPaste(nodes) === "yes-you-can");			
		}, this);
		
		this.whyCantRename = function(nodes){
			
			if (! milestonesAllowEdition(nodes)){
				return "milestone-denied";
			}
			else if (nodes.length !== 1){
				return "not-unique";
			}
			else if (nodes.filter(':editable').not(':library').length !== 1){
				return "permission-denied";
			}
			else {
				return "yes-you-can";
			}
		};

		this.canRename = $.proxy(function(nodes){
			return (this.whyCantRename(nodes) === "yes-you-can");
		}, this);
		
		this.canImport = function(nodes){
			return tree.get().data('importable');	//tree.data would lead to a different object.
		};
		
		this.canExport = function(nodes){
			return (nodes.filter(':exportable').length == nodes.length) && (nodes.length>0);
		};

		this.canSearch = function(nodes){
			return true;
		};
		
		this.canDelete = function(nodes){
			return milestonesAllowCreation(nodes) && (nodes.filter(':deletable').not(':library').length == nodes.length) && (nodes.length>0);
		};
		
		this.whyCantDelete = function(nodes){
			if (nodes.length===0){
				return "empty-selection";
			}
			
			if (nodes.not(':deletable').length>0){
				return "not-deletable";
			}
			
			if (nodes.is(':library')){
				return "no-libraries-allowed";
			}
			
			if (! milestonesAllowCreation(nodes)){
				return "milestone-denied";
			}
			
			return "yes-you-can";
		};
		
		
		this.canDnD = function(movednodes, newparent){
			
			var oldparent = movednodes.getParent();
			
			// check if the node is draggable first
			if (movednodes.is(':library')){
				return false;
			}
			
			//check that moving the node will not remove it from its original container
			if (! squashtm.keyEventListener.ctrl && ! movednodes.isDeletable()){
				return false;
			}
			
			// check that the destination type is legal
			if (! newparent.isCreatable() || ! newparent.acceptsAsContent(movednodes)) {
				return false;
			}
			
			// check that destination isn't locked by milestones
			if (! milestonesAllowCreation(newparent)){
				return false;
			}
			
			return true;
						
		};
	
		this.buttonrules = {
			'tree-create-button' : this.canCreateButton,
			'new-folder-tree-button' : this.canCreateFolder,
			'new-test-case-tree-button' : this.canCreateTestCase,
			'copy-node-tree-button' : this.canCopy,
			'paste-node-tree-button' : this.canPaste,
			'rename-node-tree-button' : this.canRename,
			'import-excel-tree-button' : this.canImport,
			'import-links-excel-tree-button' : this.canImport,
			'export-tree-button' : this.canExport,
			'delete-node-tree-button' : this.canDelete,
			'search-tree-button' : this.canSearch
		};

	}
	
	
});