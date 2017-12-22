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
define(['jquery', "squash.configmanager", 'jquery.squash.formdialog', "jquery.squash.tagit"], function($, confman){
	
	function init(settings){
		
		// widgets 
		
		var dialog = $(settings.selector).formDialog();

	
		dialog.data('model' , settings.model);
		
		var tagconf = confman.getStdTagit();
		tagconf.constrained=true;
		 
	
		  
		tagconf.validate = function(label){
			
			var values = $("#choose-tag").squashTagit('assignedTags');
			dialog.data('tags', values);
			return true;
		};
		
		$.ajax({
			url : dialog.data().url,
			type : 'GET'
		}).success(function(val){
			tagconf.autocomplete.source = val;
			$("#choose-tag").squashTagit(tagconf);
		});


		
		// events		
		dialog.on('formdialogconfirm', function(){
		
			
		var inputname = dialog.attr('id');
		var resultspan  = $("#"+dialog.data('idresult'));
		var values = dialog.data('tags');
			
		console.log(values);
		resultspan.text(values);
		settings.model.setVal(inputname, values);
			dialog.formDialog('close');
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
	}
	
	function setValue(){
		

	}
	
	return {
		init : init,
		setValue : setValue
	};
	
});