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
define(['jquery', 'tree', '../permissions-rules', './lolmode', 'workspace/workspace.delnode-popup'], 
		function($, zetree, rules, lolmode){
	
	function init(){

		var tree = zetree.get();
		var dialog = $("#delete-node-dialog").delnodeDialog({
			tree : tree,
			rules : rules,
			position : 'center'
		});


		dialog.on('delnodedialogconfirm', function(){
			dispatch(dialog, tree);
		});
		
		dialog.on('delnodedialogcancel', function(){
			dialog.delnodeDialog('close');
		});
		
		dialog.on('delnodedialogclose', function(){
			var opts = dialog.delnodeDialog('option');
			if (!!  opts.lol){
				opts.lol.clean();
				delete opts.lol;
			}
		});
		
		// DEBUG
		debug();
		
	}
	
	function dispatch(dialog, tree){
		var node = tree.jstree('get_selected');
		if (node.is(':folder') && node.hasClass('jstree-leaf')){
			var lol = lolmode.getNew(dialog);
			dialog.delnodeDialog('option', 'lol', lol);
		}
		else{
			dialog.delnodeDialog('performDeletion');
		}
	}
	
	
	function debug(){
		

	}
	
	
	return {
		init : init
	};

});