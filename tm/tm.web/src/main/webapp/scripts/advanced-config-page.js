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
	require(["jquery", "squash.translator", "jeditable.simpleJEditable", "app/lnf/Forms", "app/ws/squashtm.workspace", "jquery.switchButton"],
			function($, msg, SimpleJEditable, Forms) {
		"use strict";
				$.ajaxPrefilter(function (options, originalOptions, jqXHR) {
					var token = $("meta[name='_csrf']").attr("content");
					var header = $("meta[name='_csrf_header']").attr("content");
					jqXHR.setRequestHeader(header, token);
				});
				msg.load(["label.insensitive", "label.sensitive", "label.Activate", "label.Deactivate", "label.filesystem", "label.database"]);

		$(function() {
			$("#case-insensitive-login").switchButton({
				on_label: msg.get("label.insensitive"),
				off_label: msg.get("label.sensitive")
			});

			var callbackUrlInput = new SimpleJEditable({
				targetUrl: squashtm.appRoot + "administration/config",
				componentId: "callbackUrl"
			});

			$("#callbackUrl").on("click", function() {
				Forms.input($(this)).clearState();
			});

			$(document).on("change", "#case-insensitive-login", function onChangeCase(event) {
				if (!!onChangeCase.running) {
					return;
				}
				onChangeCase.running = true;

				var enabled = $(event.currentTarget).prop("checked");

				$.ajax({
					url: squashtm.appRoot + "features/case-insensitive-login",
					method: "post",
					data: { enabled: enabled }
				}).fail(function() {
					$(event.currentTarget).switchButton("option", "checked", !enabled);
					onChangeCase.running = false;
				}).done(function() { onChangeCase.running = false; });
			});



			$("#stack-trace").switchButton({
				on_label: msg.get("label.Activate"),
				off_label: msg.get("label.Deactivate")
			});

			$("#autoconnect-on-connection").switchButton({
				on_label: msg.get("label.Activate"),
				off_label: msg.get("label.Deactivate")
			});

			$("#toggle-storage-checkbox").switchButton({
				on_label: msg.get("label.filesystem"),
				off_label: msg.get("label.database")
			});

			$(document).on("change", "#stack-trace", function onChangeCase(event) {
				if (!!onChangeCase.running) {
					return;
				}
				onChangeCase.running = true;

				var enabled = $(event.currentTarget).prop("checked");

				$.ajax({
					url: squashtm.appRoot + "features/stack-trace",
					method: "post",
					data: { enabled: enabled }
				}).fail(function() {
					$(event.currentTarget).switchButton("option", "checked", !enabled);
					onChangeCase.running = false;
				}).done(function() { onChangeCase.running = false; });
			});

			$(document).on("change", "#autoconnect-on-connection", function onChangeCase(event) {
				if (!!onChangeCase.running) {
					return;
				}
				onChangeCase.running = true;

				var enabled = $(event.currentTarget).prop("checked");

				$.ajax({
					url: squashtm.appRoot + "features/autoconnect-on-connection",
					method: "post",
					data: { enabled: enabled }
				}).fail(function() {
					$(event.currentTarget).switchButton("option", "checked", !enabled);
					onChangeCase.running = false;
				}).done(function() { onChangeCase.running = false; });
			});

			$(document).on("change", "#toggle-storage-checkbox", function onChangeCase(event) {
				if (!!onChangeCase.running) {
					return;
				}
				onChangeCase.running = true;

				var enabled = $(event.currentTarget).prop("checked");

				$.ajax({
					url: squashtm.appRoot + "features/file-repository",
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
