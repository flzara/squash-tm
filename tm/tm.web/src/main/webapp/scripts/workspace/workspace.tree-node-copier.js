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
 * create a singleton instance if needed, 
 * then returns it to the client. 
 * 
 * 
 */

define([ 'jquery', 'underscore', 'squash.translator', "jquery.squash.oneshotdialog", "workspace.projects" ], 
		function($, _, translator, oneshot, projects) {

	squashtm = squashtm || {};
	squashtm.workspace = squashtm.workspace || {};

	if (squashtm.workspace.treenodecopier !== undefined) {
		return squashtm.workspace.treenodecopier;
	} else {
		squashtm.workspace.treenodecopier = new TreeNodeCopier();
		return squashtm.workspace.treenodecopier;
	}

	function TreeNodeCopier() {

		this.tree = $("#tree"); // default that should work 99% of the time.

		this.setTree = function(tree) {
			this.tree = tree;
		};

		// ***************** private methods *********************

	

		var reset = function() {
			$.cookie('squash-copy-nodes', null);
		};

		var retrieve = function() {
			var data = $.cookie('squash-copy-nodes');
			return JSON.parse(data);
		};

		var store = function(data) {

			var jsonData = JSON.stringify(data);

			$.cookie('squash-copy-nodes', jsonData);
		};

		var readNodesData = function(tree) {
			var nodes = tree.jstree('get_selected');

			var nodesData = nodes.toData();
			var projectIds = _.unique(nodes.all('getProjectId'));

			return {
				projects : projectIds,
				nodes : nodesData
			};

		};

		// ****************** public methods **********************

		// public version of 'retrieve'
		this.bufferedNodes = function() {
			var data = retrieve();
			if (data === null) {
				return $();
			} else {
				return this.tree.jstree('findNodes', data.nodes);
			}
		};

		// assumes that all checks are green according to the rules of this workspace.
		this.copyNodesToCookie = function() {

			reset();

			var data = readNodesData(this.tree);

			store(data);
		};

		// assumes that the operation is ok according to the rules of this workspace.
		this.pasteNodesFromCookie = function() {

			var self = this;
			var tree = this.tree;

			var data = retrieve();
			var target = tree.jstree('get_selected');

			// warn user if not same libraries
			warnIfisCrossProjectOperation.call(this, target, data).done(function() {
				doPaste(tree, target, data);
			});

		};

		// assumes that the operation is ok according to the rules of this workspace.
		this.pasteNodesFromTree = function() {

			var self = this, tree = this.tree;

			var data = readNodesData(tree), move = tree.jstree('get_instance')._get_move();
			target = $(move.np).treeNode();

			// warn user if not same libraries
			warnIfisCrossProjectOperation.call(this, target, data).done(function() {
				doPaste(tree, target, data);
			});

		};

		var warnIfisCrossProjectOperation = function(target, data) {

			var defer = $.Deferred();

			var targetProject = target.getProjectId(), 
				isCrossProject = false;
			
			// convert the dom-id to a res-id
			var srcProjects = data.projects;

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
					if (target.getName() === 'RequirementLibrary'){
						addendum = translator.get('message.warnCopyToDifferentLibrary.milestonesDiffer.requirement');
						msg = msg.replace('</ul>', addendum + '</ul>');
					}
					else if (target.getName() === 'TestCaseLibrary'){
					addendum = translator.get('message.warnCopyToDifferentLibrary.milestonesDiffer.testcase');
					msg = msg.replace('</ul>', addendum + '</ul>');
					}
					else {
						addendum = translator.get('message.warnCopyToDifferentLibrary.milestonesDiffer.campaign');
						msg = msg.replace('</ul>', addendum + '</ul>');
					}
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

		var doPaste = function(tree, target, data) {

			var nodes = tree.jstree('findNodes', data.nodes);

			target.open();

			// now we can proceed
			tree.jstree('copyNodes', nodes, target).fail(function(json) {
				tree.jstree('refresh');
			});
		};

	}

});
