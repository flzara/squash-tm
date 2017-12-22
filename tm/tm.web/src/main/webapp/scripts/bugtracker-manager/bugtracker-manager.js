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
require(["common"], function() {
	require(["jquery", "squash.translator", "workspace.routing",'app/ws/squashtm.notification', 
	         "squashtable", 
	         "app/ws/squashtm.workspace", 
	         "jquery.squash.formdialog"], 
			function($, translator, routing, notification){					
		
		$(function() {		
		
			$("#bugtrackers-table").squashTable({},{});						
		});	
		
		
		// *************** BT ADDITION ********************
		
		var addBTDialog = $("#add-bugtracker-dialog");
		
		addBTDialog.formDialog();
		
		addBTDialog.on('formdialogcancel', function(){
			addBTDialog.formDialog('close');
		});
		
		addBTDialog.on('formdialogconfirm', function(){
			var url = routing.buildURL('administration.bugtrackers');
			var params = {
				name: $( '#add-bugtracker-name' ).val(),
				url: $( '#add-bugtracker-url' ).val(),
				kind: $( '#add-bugtracker-kind' ).val(),
				iframeFriendly: $('#add-bugtracker-iframeFriendly').is(':checked')							
			}
			$.ajax({
				url : url,
				type : 'POST',
				dataType : 'json',
				data : params				
			}).success(function(){
				$('#bugtrackers-table').squashTable().refresh();
				addBTDialog.formDialog('close');
			});
		});
		

		addBTDialog.on('formdialogaddanother', function(){
			var url = routing.buildURL('administration.bugtrackers');
			var params = {
				name: $( '#add-bugtracker-name' ).val(),
				url: $( '#add-bugtracker-url' ).val(),
				kind: $( '#add-bugtracker-kind' ).val(),
				iframeFriendly: $('#add-bugtracker-iframeFriendly').is(':checked')							
			}
			$.ajax({
				url : url,
				type : 'POST',
				dataType : 'json',
				data : params				
			}).success(function(){
				$('#bugtrackers-table').squashTable().refresh();
				addBTDialog.formDialog('open');
			});
		});
		
		
		$("#new-bugtracker-button").on('click', function(){
			addBTDialog.formDialog('open');
		});
		
		// *************** BT DELETION ********************

		$("#delete-bugtracker-popup").confirmDialog().on('confirmdialogconfirm', function(){
			
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = squashtm.app.contextRoot+'/bugtracker/'+ ids.join(",");
			var table = $("#bugtrackers-table").squashTable();
			
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table.refresh();
			});
			
			
		});

		$("#delete-bugtracker-button").on('click', function(){
			var ids = $("#bugtrackers-table").squashTable().getSelectedIds();

			if (ids.length>0){
				var popup = $("#delete-bugtracker-popup");
				popup.data('entity-id', ids);
				popup.confirmDialog('open');
			}
			else{
				notification.showWarning(translator.get('message.EmptyTableSelection'));
			}
		});

	
	});			
});		