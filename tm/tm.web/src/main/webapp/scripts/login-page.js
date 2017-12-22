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
/**
 * COntroller for the login page
 */
require([ "common" ], function() {
	require([ "jquery", "app/pubsub", "app/ws/squashtm.notification", "app/ws/squashtm.ajaxspinner", "jqueryui", "jquery.squash.squashbutton" ],
			function($, ps, notification, spinner) {
				ps.subscribe("load.notification", function() {
                                        spinner.init();
					notification.init();
				});

				ps.subscribe("load.loginForm", function() {
					$("#username").focus();
				});

				$(document).on("keydown", "body", function(event) {
					var e;
					if (event.which != "") {
						e = event.which;
					} else if (event.charCode != "") {
						e = event.charCode;
					} else if (event.keyCode != "") {
						e = event.keyCode;
					}

					if (e == 13) {
						event.preventDefault();
						$("#login-form-button-set input").click();
					}
				});

			});
});
