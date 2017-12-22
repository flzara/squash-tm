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
define([ "jquery", "app/ws/squashtm.notification", "squash.translator", "jquery.squash.togglepanel", "jquery.squash.formdialog" ], function($, notification, translator) {

	return function(settings) {

		var instance = $(settings.selector);
		var button = instance.prev().find("button[value='+']");
		var buttonDelete = instance.prev().find("button[value='-']");

		// *********** decorate the attributes ******

		button.setPopup = function(popup) {
			button.click(function() {
			popup.formDialog("open");
			});
		};

		instance.getButton = function() {
			return button;
		};
		
		instance.getButtonDelete = function() {
			return buttonDelete;
		};
		
			buttonDelete.setPopup = function(popupDelete) {
			buttonDelete.click(function() {
				
				var table = instance.find('.cuf-binding-table');
				
				var hasPermission = (table.squashTable().getSelectedIds().length > 0);
			if (hasPermission) {
				panel = instance.find('.cuf-binding-table').closest('.sq-tg');
				popupDelete.formDialog("open");
			}
		 else {
			 notification.showError(translator.get('message.NoCUFSelected'));
		 }
			
			});
		};

		return instance;
	};

});