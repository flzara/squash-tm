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
/*
 * Implements the dnd api 
 * 
 */

define(['jquery', 'underscore', 'workspace.tree-node-copier', 'workspace.permissions-rules-broker', 'squash.translator', 
        "jquery.squash.oneshotdialog", 'workspace.projects',  'jstree'], 
        function($, _, nodecopier, rulesbroker, translator, oneshot, projects){
	
	
	/* *******************************************************************************
					Library part	
	******************************************************************************** */
	

	/*
	 * *************************** post new nodes operations **********************************************
	 */
	/**
	 * Post new contents to the url determined by the selected node of a tree and creates a new node with returned JSON
	 * data.
	 * 
	 * @param treeId
	 *          html id of the tree
	 * @param contentDiscriminator
	 *          discriminator to append to post url (determines content to be created)
	 * @param postParameters
	 *          map of post params
	 * @param selectNewNode
	 *          optional, default = true
	 */

	function postNewNode(contentDiscriminator, postParameters, selectNewNode) {
		if (selectNewNode === undefined) {
			selectNewNode = true;
		}
		// **************** variables init ******************

		var origNode = this.get_selected();
		var targetNode;

		if ((origNode.is(':library')) || (origNode.is(':folder')) ||
				((origNode.is(':campaign')) && (contentDiscriminator == "new-iteration")) ||
				((origNode.is(':requirement')) && (contentDiscriminator == "new-requirement")) ) {
			targetNode = origNode;
		} else {
			targetNode = origNode.getParent();
		}

		var url = targetNode.getContentUrl() + '/' + contentDiscriminator;
		var newNode = null;

		// ***************** function init ********************

		var postNode = function() {
			return $.ajax({
				url : url,
				data : JSON.stringify(postParameters),
				type : 'POST',
				dataType : 'json',
				contentType : "application/json;charset=UTF-8"
			});
		};

		var addNode = function(data) {
			var res = targetNode.appendNode(data);
			newNode = res[0];
			return res[1];
		};

		var selectNode = function() {
			targetNode.deselect();
			origNode.deselect();

			newNode.select();
			return targetNode.open();
		};

		var createNode = function() {
			if (selectNewNode) {
				return postNode().then(addNode).then(selectNode);
			} else {
				return postNode().then(addNode);
			}
		};

		// ********** actual code. ******************

		var isOpen = targetNode.isOpen();
		if (!isOpen) {
			return targetNode.open() // first call will make the node load if necessary.
			.then(createNode);
		} else {
			return createNode();
		}

	}
	

	/*
	 * **************************** dnd check section ****************************************
	 */

		
	/*
	 * Will check if a dnd move is legal. Note that this check is preemptive, contrarily to checkMoveIsAuthorized which
	 * needs to post-check.
	 * This method check only for the move in the tree.
	 * 
	 * NB : this method is called by the configuration of plugin "crrm" in the initialization object.
	 * 
	 */
	function check_move() {
		
		// apply the basic rules
		if (! this.__call_old()){
			return false;
		}
		
		
		//now apply our own.
		var rules = this._getRules();
		
		try{
			
			var move = this._get_move();
			
			//this simple test will cut short useless tests.	
			if (! move.np.is('li')){
				return false;
			}
			
			var	movednodes = $(move.o).treeNode(),
				newparent = $(move.np).treeNode();
			
			return rules.canDnD(movednodes, newparent);
			
		} catch (invalid_node) {
			return false;
		}

	}

	/*
	 * This method checks if we can move the object is the dest folder returns true if it's ok to move the object. Note
	 * that contrary to checkDnd(moveObject), that code is called only for "move", not "copy" operations, and thus is
	 * not part of the aforementioned function.
	 * 
	 * A second reasons is that we don't want to forbid the operation a-priori : we cancel it a-posteriori. Thus, the user
	 * will know why the operation could not be performed instead of wondering why he cannot move the  nodes.
	 */
	function check_name_available(data) {
		var dest = data.rslt.np;
		var object = data.rslt.o;

		// here are the names of all destination children, and the names of the
		// moved objects
		destNames = dest.children("ul").children("li").not(object).collect(function(elt) {
			return $(elt).attr('name');
		});
		movedNames = object.collect(function(elt) {
			return $(elt).attr('name');
		});

		var okay = true;
		for ( var i in movedNames) {
			if ($.inArray(movedNames[i], destNames) >= 0) {
				okay = false;
				break;
			}
		}
		return okay;
	}

	
	var warnIfisCrossProjectOperation = function(moveObject){
		
		var defer = $.Deferred();
		
		var targetProject = $(moveObject.np).treeNode().getProjectId(),
		libName = $(moveObject.np).treeNode().getName(),
			srcProjects = _.unique($(moveObject.op).treeNode().all('getProjectId')),
			isCrossProject = false;
		
		var moved = moveObject.o;
		

		// check if cross project
		for ( var i = 0; i < srcProjects.length; i++) {
			if (targetProject != srcProjects[i]) {
				isCrossProject = true;
				break;
			}
		}

		if (isCrossProject) {
			var msg = translator.get('message.warnCopyToDifferentLibrary');

			// if cross-project, also check whether
			// the nature/type/category settings are different				
			var areInfoListsDifferent = projects.haveDifferentInfolists(srcProjects.concat(targetProject));
			var addendum;
			
			if (areInfoListsDifferent){
				addendum = translator.get('message.warnCopyToDifferentLibrary.infolistsDiffer');
				// we append the addendum by manipulating the html directly
				// it is so because first creating the js element then appending 
				// will give poor results
				msg = msg.replace('</ul>', addendum + '</ul>');
			}
			
			var lostMilestones = projects.willMilestonesBeLost(targetProject, srcProjects);
			if (lostMilestones){
				if (libName === 'RequirementLibrary'){
					addendum = translator.get('message.warnCopyToDifferentLibrary.milestonesDiffer.requirement');
					msg = msg.replace('</ul>', addendum + '</ul>');
				}
				else if (libName === 'TestCaseLibrary'){
				addendum = translator.get('message.warnCopyToDifferentLibrary.milestonesDiffer.testcase');
				msg = msg.replace('</ul>', addendum + '</ul>');
				}
				else {
					addendum = translator.get('message.warnCopyToDifferentLibrary.milestonesDiffer.campaign');
					msg = msg.replace('</ul>', addendum + '</ul>');
				}
			}
			
			// check if synchronized requirements might lose synchronization
			// note : if there are subnodes that are synchronized requirements 
			// and not yet loaded, this selector will not find them. 
			var syncreqs = moved.treeNode().getFlatSubtree().add(moved).is(':requirement:synchronized');
			if (syncreqs === true){
				addendum = translator.get('message.warnCopyToDifferentLibrary.syncreqlost');
				msg = msg.replace('</ul>', addendum + '</ul>');
			}
			
			oneshot.show('Info', msg)
			.done(function() {
				defer.resolve();
			}).fail(function() {
				defer.reject();
			});
		} else {
			defer.resolve();
		}	
		
		return defer.promise();
	};

	
	// ******************************* node move operations ****************************
	
	/*
	 *
	 * @param data : the move_node object @param url : the url to post to.
	 */
	function moveNodes(data) {

		var tree = data.inst,
			nodeData = data.rslt,
			nodes = nodeData.o,
			target = nodeData.np,
			targetTreeNode = $(target).treeNode(),
			action = nodeData.p;

				
		// first check if we don't need to perform an
		// operation
		if (nodeData.o.length === 0) {
			return;
		}
		
		// we also reject testsuites
		var firstNode = nodeData.o[0];
		if ($(firstNode).is(":test-suite")) {
			return;
		}

		var rawurl = targetTreeNode.getMoveUrl();
		var nodeIds = $(nodes).treeNode().all('getResId').join(',');
		var url;
		
		if(action === "inside"){
			url = rawurl.replace('{nodeIds}', nodeIds).replace('/{position}', "");
		} else {
			/* 
			 * There is a quirk with the position computed by jstree : it doesn't 
			 * exclude the moved nodes from the computation.
			 * 
			 * So we need to ensure a correct calculus by removing the moved nodes 
			 * from the list of children to that we can safely compute the real index. 
			 */
			var childrenUpToPosition = targetTreeNode.getChildren().slice(0, nodeData.cp);
			var position = childrenUpToPosition.not(nodes).length;
			
			url = rawurl.replace('{nodeIds}', nodeIds).replace('{position}', position);
		}
		
		tree.open_node(target);

		return $.ajax({
			type : 'PUT',
			url : url,
			dataType : 'json'
		})
		.success(function(){
			nodes.treeNode().afterMove(target, nodeData.op);
		})
		.fail(function(){
			$.jstree.rollback(data.rlbk);
		});
	}



	/*
	 * ***************************** node copy section ****************************************
	 */
	
    /*
     * jstree inserts dumb copies when we ask for copies. We need to destroy them before inserting the correct ones
     * incoming from the server.
     * 
     * @param object : the move_object returned as part of the data of the event mode_node.jstree.
     * 
     */
    function destroyJTreeCopies(object, tree) {
            object.oc.each(function(index, elt) {
                    tree.delete_node(elt);
            });
    }

	/*
	 * will batch-insert nodes incoming from the server.
	 * 
	 * @param jsonResponse : the node formatted in json coming from the server.
	 * 
	 * @param currentNode : the node where we want them to be inserted.
	 * 
	 * @param tree : the tree instance.
	 */
	function insertCopiedNodes(jsonResponse, currentNode, tree) {
		for ( var i = 0; i < jsonResponse.length; i++) {
			tree.create_node(currentNode, 'last', jsonResponse[i], false, true);
		}
	}


	/*
	 * will erase fake copies in the tree, send the copied node data to the server, and insert the returned nodes.
	 * Note that this method is invoked by the tree-node-copier.
	 * 
	 * @param data : nodesIds
	 * 
	 * @param url : the url where to send the data.
	 * 
	 * @returns : a promise
	 * 
	 */
	function copyNodes(nodes, target) {

		var deferred = $.Deferred();

		var tree = this;
		
		var url = target.getCopyUrl();
		var nodeIds = nodes.all('getResId');
		
		var params = {
			'nodeIds[]' : nodeIds 
		};
		
		//special delivery for pasting iterations to campaigns
		if (target.is(':campaign')){
			params['next-iteration-index'] = (target.getChildren().length);
		}
		
		$.when(tree.open_node(target)).then(function() {
			
			$.post(url, params, 'json')
			.done(function(jsonData) {
				insertCopiedNodes(jsonData, target, tree);
				tree.open_node(target, deferred.resolve);
				if (typeof (refreshStatistics) == "function") {
					refreshStatistics();
				}
				/* Issue #6438: We have to refresh the test-plan table 
				 * if we just copied a test-suite in an iteration. */
				if(target.getDomType() === "iteration") {
					$("#iteration-test-plans-table").squashTable().refresh();
				}
			})
			.error(deferred.reject);

			
		});

		return deferred.promise();
	}


	/* *******************************************************************************
							// Library part	
	 ******************************************************************************** */
	
	/* *******************************************************************************
							Plugin definition
	 ******************************************************************************** */	

	return function(){
		

		$.jstree.plugin('workspace_tree', {
			defaults : {
				
			},

			__init : function() {


				var container = this.get_container();

				var self = this;

				container.bind("select_node.jstree", function(event, data) {
					data.rslt.obj.treeNode().deselectChildren();
					return true;
					
				})
				
				/*
				 * This event is triggered after the movement was performed. Some checks regarding the validity of this operation
				 * were not performed, we will perform them now. If the drop operation is invalid we will cancel it now and notify the 
				 * user of the specific reasons.
				 * 
				 * Note : the event 'before.jstree' was too buggy to use so we won't use it.
				 */
				.bind("move_node.jstree", function(event, data) {

					var moveObject = data.args[0];
					
					if (moveObject === null || moveObject === undefined || moveObject.cr === undefined) {
						return; //abort !
					}
					var rules = data.inst._getRules();
					
					
					//case dnd-copy
					if (squashtm.keyEventListener.ctrl) {
						destroyJTreeCopies(moveObject, data.inst);
						nodecopier.pasteNodesFromTree();
						return;
					} 
					
					//case dnd-move
					if (check_name_available(data)) {
						
						warnIfisCrossProjectOperation(moveObject)
						.done(function(){
							moveNodes(data);
						})
						.fail(function(){
							$.jstree.rollback(data.rlbk);
						});

					} 
					else {
						$.squash.openMessage('', translator.get('squashtm.action.exception.cannotmovenode.label')).done(function() {
							$.jstree.rollback(data.rlbk);
						});
					}
				});
		
			
				
			},

			_fn : {
				
				check_move : check_move,
				
				postNewNode : postNewNode, 
				
				copyNodes : copyNodes,

				refresh_selected : function() {
					var self = this;
					var selected = this.get_selected();
					if(selected.length > 0){ 
						selected.all('refresh');
					}
				},
				
				_getRules : function(){				
					//this code handle lazy initialisation for permissions-rules. 
					var settings = this._get_settings(); 
					
					if (settings.workspace_tree.rules === undefined){
						settings.workspace_tree.rules = rulesbroker.get();
					}
					
					return settings.workspace_tree.rules;
				},
				
				/*
				* A example command object is of the form : 
				* 
				* {
				*	removed : [ Node, ...],
				*	renamed : [ NodeRenaming, ... ],
				*	moved : [ NodeMovement, ... ] 
				* }
				* 
				* With the following definitions :
				* 
				* Node : {
				*	//any properties that might help identify your node, eg { resid : 13, rel : 'test-case' }
				* }
				* 
				* NodeRenaming : {
				*	node : Node,
				*	name : String
				* }
				* 
				* NodeMovement : {
				*	dest : Node,
				*	moved : [ Node, ... ]
				* }
				*/
				apply_commands : function(commandObject){
				
					if (commandObject === null || commandObject === undefined){return;}
										
					//first, node renamings.
					var renamed = commandObject.renamed;
					if (renamed !== null && renamed !== undefined &&  renamed instanceof Array){
						var len = renamed.length;
						for (var i=0; i<len; i++){
							var node = this.findNodes(renamed[i].node);
							if (node.length>0){
								node.setName(renamed[i].name);
							}
						}
					}
					
					//second, node movements. Note that if the destination node was loaded we will move the nodes there, while if it 
					//hasn't loaded yet we simply need to remove the children.
					var moved = commandObject.moved;
					if (moved !== null && moved !== undefined &&  moved instanceof Array){
						var len2 = moved.length;
						for (var j=0;j<len2;j++){
							var movement = moved[j];
							
							var target = this.findNodes(movement.dest);
							var children = this.findNodes(movement.moved);
							
							// if the children exist somewhere in the tree, move them
							if (children.length>0){
								children.moveTo(target);
							}
							// if they don't, but the target exist, reload it to make the children appear. 
							else if (target.length>0){
								target.refresh();
							}
							
						}
					}
					
					// third, the nodes that were deleted.
					var removed = commandObject.removed;
					if (removed !== null && removed !== undefined &&  removed instanceof Array && removed.length>0){
						var nodes = this.findNodes(removed);
						if (nodes.length>0){
							nodes.removeMe();
						}
					}
				}
			}

		});
		
	};	
});
