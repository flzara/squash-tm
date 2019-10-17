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
require([ "common" ], function() {
	require(["jquery", "app/ws/squashtm.workspace", "jeditable.simpleJEditable", "./scm-server/RenameScmServerDialog", 
		"./scm-server/ScmRepositoriesTableView", "third-party-server/credentials-manager", "jquery.squash.togglepanel"],
	function($, WS, SimpleJEditable, RenameScmServerDialog, ScmRepositoriesTableView, CredentialManagerView) {

		WS.init();

		var nameLabel = $('#scm-server-name-header');
		var renameScmServerDialog = new RenameScmServerDialog(nameLabel);
		new ScmRepositoriesTableView();

		$('#rename-scm-server-button').click(function() {
			renameScmServerDialog.open();
		});

		/* JEditable for Url modification. */
		new SimpleJEditable({
			// targetUrl: The target Url is the current Url.
			componentId: "scm-server-url",
			jeditableSettings: {
				name: 'url',
				callback: function(value) {
					$(this).siblings('#scm-server-url-link').attr('href', value);
				}
			}
		});

		$('#scm-server-url').click(function() {
			$(this).siblings('.error-message').text('');
		});

		/* JEditable for Committer email modification. */
		new SimpleJEditable({
			// targetUrl: The target Url is the current Url.
			componentId: "scm-server-committer-mail",
			jeditableSettings: {
				name: 'committerMail'
			}
		});

		$('#scm-server-committer-mail').click(function() {
			$(this).siblings('.error-message').text('');
		});

		// ******* authentication ***********
		 var authConf = $.extend({}, squashtm.pageConfiguration.authConf);
		 authConf.entityUrl = squashtm.pageConfiguration.url;

		 new CredentialManagerView({ conf: authConf });

	});
});
