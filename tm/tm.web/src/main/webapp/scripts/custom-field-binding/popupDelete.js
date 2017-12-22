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
 *conf : 
 *  {
 *      projectId : the projectId,
 *      bindableEntity : the bindable entity type,
 *      getURL : the URL from where the available custom fields are fetched,
 *      selector : the selector for the popup,
 *      title : title of that popup,
 *      oklabel : localized label for 'ok',
 *      cancellabel : localized label for 'cancel',
 *      
 *  }
 * 
 * 
 */
define([ "require", "./models", "app/util/ButtonUtil", "jquery.squash", "jquery.squash.formdialog"], function(require, Model, ButtonUtil) {

	return function(settings) {

		// save the reference now, before the DOM is moved around
		var popup = $(settings.selector);
		
		popup.formDialog();
		
		popup.on('formdialogconfirm', submit);
		
		popup.on('formdialogcancel', function(){
			popup.formDialog('close');
		});

		popup.postSuccessListeners = [];


		// ************* private methods ***************************
		
	
		function submit(event) {
			
				var instance = $(settings.selectorMainPage);
				var table = instance.find('.cuf-binding-table');
				var ids = table.squashTable().getSelectedIds() ; 
				var url = settings.deleteUrl + "/" + ids.join(',');

				$.ajax({
					url : url,
					type : 'delete',
					dataType : 'json'
				}).done(function() {
					table.squashTable().refresh();
				});

				$(this).formDialog('close');
		}
			

			popup.addPostSuccessListener = function(listener) {
			popup.postSuccessListeners.push(listener);
		};

		return popup;
	};

});