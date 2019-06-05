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
require([ "common" ], function(common) {
	require([ "jquery", "squash.translator", "underscore", "app/pubsub", "jquery.squash.formdialog" ], function($, translator, _, ps) {

		ps.subscribe("load.informationContent", init);

		function init() {
			configureInformationDialog();
		}

		function configureInformationDialog() {
				var dialog = $('#information-dialog');
				dialog.formDialog();

				var targetUrl = retrieveTargetUrl();
				var information = squashtm.app.information;
				if (_.size(information) != 0) {
					dialog.formDialog('open');
					retrieveMessages(information);
				} else {
					window.location.href = squashtm.app.contextRoot + 'home-workspace';
				}
				dialog.on('formdialogclose', function () {
					close(dialog, targetUrl);
				});
				dialog.on('formdialogcancel', function () {
					close(dialog, targetUrl);
				});
		}

		function retrieveTargetUrl() {
			var url = window.location.href;
			var baseUrl = squashtm.app.contextRoot;
			var homeUrl = baseUrl + 'home-workspace';

			if (url.indexOf('targetUrl=') != -1) {
				targetUrl = url.split('targetUrl=')[1];
				if (targetUrl === '/home-workspace' || targetUrl === '/automation-workspace') {
					targetUrl = baseUrl + targetUrl.substring(1);
				}
			} else {
				targetUrl = homeUrl;
			}

			return targetUrl;
		}

		function retrieveMessages(information) {
			var messageDate = retrieveMessageDate(information);
			var messageUser = retrieveMessageUser(information);
			if (messageDate !== '') { $('#information-message-date').text(messageDate); }
			if (messageUser !== '') { $('#information-message-user').text(messageUser); }
			if (messageDate === '' || messageUser === '') {
				if (messageDate === '') { $('#information-message-date').hide(); }
				if (messageUser === '') { $('#information-message-user').hide(); }
				$('#information-divider').hide();
			}
		}

		function retrieveMessageDate(information) {
			var message = '';
			var messageDate = information.messageDate;
			if (messageDate != undefined && messageDate !== '') {
				message += translator.get('information.expirationDate.' + messageDate).replace('{0}', formateDueDate(information.dueDate));
			}
			return message;
		}

		function formateDueDate(date) {
			var dates = date.split('-');
			return dates[2] + '/' + dates[1] + '/' + dates[0];
		}

		function retrieveMessageUser(information) {
			var message = '';
			var messageUser = information.messageUser;
			if (messageUser != undefined && messageUser !== '') {
				message = translator.get('information.userExcess.' + messageUser).replace('{0}', information.maxUserNb).replace('{1}', information.currentUserNb);
			}
			return message;
		}

		function close(dialog, targetUrl) {
			dialog.formDialog('close');
			window.location.href = targetUrl;
		}

	});
});
