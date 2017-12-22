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
define(['jquery', 'tree', 
        'squash.attributeparser',
        'squash.dateutils',
        'jquery.squash.formdialog'], 
		function($, zetree, attrparser, dateutils){
	
	$.widget("squash.exportDialog", $.squash.formDialog, {
		
		_create : function(){
			this._super();
	
			var self = this;
			
			this.onOwnBtn('cancel', function(){
				self.close();
			});
			
			this.onOwnBtn('confirm', function(){
				self.confirm();
			});			
		}, 
		
		open : function(){
			this._super();			
			
			var selection = this.options.tree.jstree('get_selected');
			if (selection.length>0){
				var name = this._createName();
				$('#export-test-case-filename').val(name);
				this.setState('main');
			}
			else{
				this.setState('nonodeserror');
			}
		},
			
		
		_createName : function(){
			return this.options.nameprefix+"_"+ dateutils.format(new Date(), this.options.dateformat);
		},
		
		_createUrl : function(nodes, type, filename, includeCalls, keepRteFormat){
			
			var url = squashtm.app.contextRoot+'/test-case-browser/content/'+type;
			
			var libIds = nodes.filter(':library').map(function(){
				return $(this).attr('resid');
			}).get().join(',');
			var nodeIds = nodes.not(':library').map(function(){
				return $(this).attr('resid');
			}).get().join(',');
			
			var params = {
				'filename' : filename,
				'libraries' : libIds,
				'nodes' : nodeIds,
				'calls' : includeCalls,
				'keep-rte-format' : keepRteFormat
			};
			
			return url+"?"+$.param(params);
		},
		
		
		confirm : function(){
			var nodes = this.options.tree.jstree('get_selected');
			if ((nodes.length>0) ){
				var filename = $("#export-test-case-filename").val();
				var includeCalls = $("#export-test-case-includecalls").prop('checked');
				var keepRteFormat = $("#export-test-case-keepRteFormat").prop('checked');
				var type = this.element.find('input[name="format"]:checked').data('val');
				
				var url = this._createUrl(nodes, type, filename, includeCalls, keepRteFormat);
				document.location.href = url;
				this.close();
			}
			else{
				this.setState('nonodeserror');
			}
		}
	});
	
	
	function init(){
		
		var dialog = $("#export-test-case-dialog").exportDialog({
			tree : zetree.get()
		});

	}
	
	
	return {
		init : init
	};

});