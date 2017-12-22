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
define(["jquery", "milestone-manager/milestone-activation", "squash.attributeparser", 
        "workspace.routing", "workspace.event-bus", "jquery.switchButton", 
        "milestones/jquery.squash.milestoneDialog"], 
		function($, milestoneDao, attrparser, routing, eventBus){
	
	return {
		init : function(){
			
			// ************ variables ********************
			
			var checkbox = $("#toggle-milestone-checkbox"),
				dialog = $(".bind-milestone-dialog"),
				label = $("#toggle-milestone-label");
			
			var enabled = milestoneDao.isEnabled(),
				milestone = milestoneDao.getActiveMilestone();
			
			
			// ****** util functions ********************
			
			
			function updateLabel(state){
				if (state){
					label.removeClass('disabled-transparent')
						.addClass('clickable-item')
						.on('click', labelClick);
				}
				else{
					label.removeClass('clickable-item')
						.addClass('disabled-transparent')
						.off('click', labelClick);
				}

			}
			
			function updateStatus(){
				
				var checked = checkbox.is(':checked');
				if (checked){
					milestoneDao.activateStatus();
					var m = milestoneDao.getActiveMilestone();
					// force the user to choose if no milestone is defined
					if (m.id === ''){
						dialog.milestoneDialog('open');
					}
				}
				else{
					milestoneDao.deactivateStatus();
				}

				updateLabel(checked);
			}
			
			
			// ****** init the checkbox *****************
			
			var checkConf = attrparser.parse(checkbox.data('def'));
			checkConf.checked = enabled;
			
			checkbox.switchButton(checkConf);
			
			checkbox.siblings('.switch-button-background').css({position : 'relative', top : '5px'});
			
			// ****** init the label ***********************
			
			var currMilestone = milestoneDao.getActiveMilestone();
			if (currMilestone.label !== ""){
				label.text(currMilestone.label);
			}
			
			function labelClick(){
				dialog.milestoneDialog('open'); 				
			}

			updateLabel(enabled);
			
			// ***** init the dialog **********
			
			var dialogOptions = {
				multilines : false,
				mustChoose : true,
				identity : "dontcare",
				tableSource : routing.buildURL('milestones.selectable')
			}
			
			dialog.milestoneDialog(dialogOptions);
			
			
			// ***** events ********************
			
			$.ajaxSetup({
		    complete: function () {
		        if ($(".bind-milestone-dialog-table").length>0) 
		        	$(".bind-milestone-dialog-table tr input:first").attr('checked', true ); 
		    }});
			
			checkbox.on('change', updateStatus);
			
			// persist what the user chose to do
			eventBus.on('node.bindmilestones', function(evt, data){
				var table = dialog.find('table').squashTable(),
					id = data.milestones[0];
				
				// we must find the label of the milestone from the table
				var row = table.find('input:checked').parents('tr');
				var data = table.fnGetData(row);
				var lbl = data['label'];
				
				var newMilestone = {
					id : id,
					label : lbl
				}
				
				milestoneDao.setActiveMilestone(newMilestone);
				label.text(lbl);
			});

			// disable the whole thing if no active milestone 
			// was selected and no active milestone was set previously
			
			dialog.on('milestonedialogclose', function(){
				var m = milestoneDao.getActiveMilestone();
				if (m.id === ''){
					// sorry for the slacky selector
					checkbox.siblings('.switch-button-label.off').click();
				}
			});
		}		
	}
	
});