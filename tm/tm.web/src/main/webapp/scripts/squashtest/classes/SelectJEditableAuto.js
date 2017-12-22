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
define([ "jquery", "jquery.squash.jeditable" ], function($) {
/*
 * to use with webapp/WEB-INF/tags/component/select-jeditable-auto.tag
 * 
 * exp : 
 * ----
 * 
 * settings = {
 *	associatedSelectJeditableId:"test-case-importance"
 *	url:settings.urls.importanceAutoUrl,
 *	isAuto: settings.importanceAuto,
 *	paramName:"importanceAuto" ,(the name of the parameter being posted)
 *	autoCallBack: sendUpdateImportanceEvent  (the function to call when changed to auto (param = value : the new value for the associated input)
 *}
 */
	var SelectJEditableAuto = function(settings) {
		var sel_checkbx = $('#'+settings.associatedSelectJeditableId+'-auto');
		
		sel_checkbx.prop('checked', settings.isAuto);
		
		if (sel_checkbx.prop('checked')){
			sel_setAutoMode();
		}
		
		sel_checkbx.change(function(){
			var sel_isAuto = $(this).prop('checked');
			if (sel_isAuto){
				$('#'+settings.associatedSelectJeditableId).find("[type='cancel']").trigger('click');
				sel_setAutoMode();
			}else{
				sel_setManualMode();
				$('#'+settings.associatedSelectJeditableId).trigger('click');
			}
			sel_postState(this,sel_isAuto);
		});		
		
		function sel_setAutoMode(){
			$('#'+settings.associatedSelectJeditableId).editable('disable');
		}
		function sel_setManualMode(){
			$('#'+settings.associatedSelectJeditableId).editable('enable');
		}
		
		function sel_postState(checkbx, isAuto){
			$.ajax({
				type : 'POST',
				data : settings.paramName+"="+isAuto.toString(),
				success : function(jsonEnumValue){sel_postStateSuccess(jsonEnumValue,  isAuto);},
				error : function(){sel_postStateFailed();},
				dataType : "json",
				url : settings.url	
			});		
		}
		function sel_postStateSuccess(jsonEnumValue, isAuto){
			if (isAuto){
				sel_postStateIsAutoSuccess(jsonEnumValue);
			}
		}
		function sel_postStateIsAutoSuccess(jsonEnumValue){
			$("#"+settings.associatedSelectJeditableId).html(jsonEnumValue.localizedValue);
			if(settings.autoCallBack){
				settings.autoCallBack(jsonEnumValue.value);
			}
		}
		function sel_postStateFailed(){
			$.squash.openMessage(squashtm.message.errorTitle, squashtm.message.cache['error.generic.label']);			
		}
		
		this.isAuto = function (){
			return $(sel_checkbx).prop('checked');
		};

	};
	return SelectJEditableAuto;
});
