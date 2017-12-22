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
define(["jquery", "./jquery.squash.attachmentsDialog"], function($){
	
	function reloadAttachments(settings){
		var container =$("#attachment-container"); 
		
		container.load(settings.baseURL+"/display");
	}
	


	function init(settings){
		
		// init the dialog
		var dialog = $("#add-attachments-dialog").attachmentsDialog({
			url : settings.baseURL+"/upload"
		});
		
		dialog.on('attachmentsdialogdone', function(){
			reloadAttachments(settings);
		});
		
		
		// bind the buttons
		$("#manage-attachment-bloc-button").on('click', function(){
			document.location.href = settings.baseURL+"/manager?workspace="+settings.workspace;
		});
		
	
		$("#upload-attachment-button").on('click', function(){
			$("#add-attachments-dialog").attachmentsDialog('open');
		});	
	}
	
	return {
		init : init
	};
});