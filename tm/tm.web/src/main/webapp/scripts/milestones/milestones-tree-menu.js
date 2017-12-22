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
define(['jquery', 'milestone-manager/milestone-activation', 'workspace.contextual-content',
        'squash.translator', 'app/ws/squashtm.notification',
        'jquery.squash.formdialog' ], function($, milestones, ctxContent, translator, notification){
	
	
	function configureTable(){
		
		var table = $("#tree-milestone-dialog-table");
		
		// we want to deselect all lines except the one currently selected
		table.on('click', '>tbody>tr', function(evt){
			
			// don't trigger if the clicked element is 
			// the checkbox itself 
			if (! $(evt.target).is('input')){
				var chk = $(evt.currentTarget).find('.select-milestone-dialog-check input');								
				var newstate = ! chk.is(':checked');
				chk.prop('checked', newstate);
				table.find('.select-milestone-dialog-check input').not(chk).prop('checked', false);
			}
			else{
				table.find('.select-milestone-dialog-check input').not(evt.target).prop('checked', false);		
			}
		});
		
		
		var tblCnf = {
				sAjaxSource : squashtm.app.contextRoot + '/milestones?selectable', 
				bServerSide : false,
				fnDrawCallback : function(){
					table.find('>tbody>tr>td.select-milestone-dialog-check').each(function(){
						$(this).html('<input type="radio"/>');
					});
					table.find('>tbody>tr').addClass('cursor-pointer');
				}
			},

			squashCnf = {
				
			};
		
		table.squashTable(tblCnf, squashCnf);
		
		table.squashTable().refresh();
	}
	
	
	function init(){
		
		// the white down arrow menu
		var dialog = $("#tree-milestone-dialog").formDialog(),
			table = $("#tree-milestone-dialog-table");
		
		dialog.on('formdialogopen', function(){		
			
			if (!! table.data('squashtableInstance')){
				table.squashTable()._fnAjaxUpdate();
			}
			else{
				configureTable();
			}
			
		});		
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
		dialog.on('formdialogconfirm', function(){
			var row = table.find('input:checked').parents('tr').get(0);
			
			if (row === undefined){
				notification.showError(translator.get('message.EmptyTableSelection'));				
				return;
			}
			
			var data = table.squashTable().fnGetData(row);		
		
			var milestone = {
				id : data['entity-id'],
				label : data['label'] 
			}
			milestones.setActiveMilestone(milestone);
			dialog.formDialog('close');
			location.reload();
		
		});
		
		$("#tree-milestone-selector").on('click', function(){
			dialog.formDialog('open');
		});
		
		// the requirement dashboard button
		$("#requirementDashboardMilestone").on('click', function(){
			ctxContent.loadWith(squashtm.app.contextRoot+"/requirement-browser/dashboard");
		});
		
		// the test-case dashboard button
		$("#testCaseDashboardMilestone").on('click', function(){
	  		ctxContent.loadWith(squashtm.app.contextRoot+"/test-case-browser/dashboard");
		});
		
		// the campaign dashboard button
		$("#campaignDashboardMilestone").on('click', function(){
	  		ctxContent.loadWith(squashtm.app.contextRoot+"/campaign-browser/dashboard-milestones");
		});
		
	}
	
	return {
		init : init
	};
	
});

