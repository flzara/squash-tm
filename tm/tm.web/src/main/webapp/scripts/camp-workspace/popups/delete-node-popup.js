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
define(['jquery', 'tree', '../permissions-rules', 'workspace/workspace.delnode-popup'], function($, zetree, rules){
	
	
	function _collectId(node){
		if (node.getAttributeNode){
			return node.getAttributeNode('resid').value;
		}
		else{
			return node.getAttribute('resid');
		}
	}
	
	function _loopOver(nodes, callback){
		
		var allXhrs = [];
		
		var selectors = [":folder, :campaign", ":iteration", ":test-suite"];
		
		for (var i=0;i<3;i++){
			var filterednodes = nodes.filter(selectors[i]);
			callback(allXhrs, filterednodes);
		}
		
		return allXhrs;
		
	}
	
	//subclassing the deletion dialog because this is a special case
	$.widget("squash.delcampDialog", $.squash.delnodeDialog, {
		getSimulXhr : function(nodes){
			return _loopOver(nodes, function(aXhrs, n){
				if (n.length>0){
					var nodes = n.treeNode();
					var ids = $.map(nodes.get(), _collectId).join(',');
					var rawUrl = nodes.getDeleteUrl();
					var url = rawUrl.replace('{nodeIds}', ids) + '/deletion-simulation';
					url = url.replace('?remove_from_iter={remove_from_iter}', '');
					aXhrs.push($.getJSON(url));
				}
				else{
					aXhrs.push(null);
				}
			});
		}, 
		
		getConfirmXhr : function(nodes){
			return _loopOver(nodes, function(aXhrs, n){
				if (n.length>0){
					var nodes = n.treeNode();
					var ids = $.map(nodes.get(), _collectId).join(',');
					var rawUrl = nodes.getDeleteUrl();
					var url = rawUrl.replace('{nodeIds}', ids);
					url = url.replace('{remove_from_iter}', $("#remove-tc-from-iter").prop("checked"));
					aXhrs.push($.ajax({
						url : url,
						type : 'delete'
					}));
				}
				else{
					aXhrs.push( null);	
					//pushing null is important here because the success callback will make
					//assumptions on the order of the response.
				}
			});				
		}	
	});
	
	function init(){
		
		var tree = zetree.get();
		var dialog = $("#delete-node-dialog").delcampDialog({
			tree : tree,
			rules : rules
		});
		

		dialog.on('delcampdialogconfirm', function(){
			dialog.delcampDialog('performDeletion');
		});
		
		dialog.on('delcampdialogcancel', function(){
			dialog.delcampDialog('close');
		});
		
	}
	
	return {
		init : init
	};

});