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
require(["common"], function() {
	require(["jquery", "squash.translator", "app/ws/squashtm.workspace", "client-manager/client-manager", "jquery.switchButton"],
			function($, msg) {
		"use strict";

		msg.load(["label.insensitive", "label.sensitive"]);

		$(function() {
			$("#case-insensitive-login").switchButton({
				on_label: msg.get("label.insensitive"),
				off_label: msg.get("label.sensitive")
			});

			$(document).on("change", "#case-insensitive-login", function onChangeCase(event) {
				if (!!onChangeCase.running) {
					return;
				}
				onChangeCase.running = true;

				var enabled = $(event.currentTarget).prop("checked");

				$.ajax({
					url: squashtm.appRoot + "/features/case-insensitive-login",
					method: "post",
					data: { enabled: enabled }
				}).fail(function() {
					$(event.currentTarget).switchButton("option", "checked", !enabled);
					onChangeCase.running = false;
				}).done(function() { onChangeCase.running = false; });
			});
		});
	});
});