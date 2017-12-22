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
define([ "jquery", "squash.translator", "app/ws/squashtm.notification", "squashtable",
		"jquery.squash.formdialog", "jquery.squash.confirmdialog" ], function($, translator, notification) {
	"use strict";

	translator.load(['message.EmptyTableSelection']);

	$(function() {
		var squashSettings = {
			functions : {
				drawDeleteButton : function(template, cells) {

					$.each(cells, function(index, cell) {
						var row = cell.parentNode; // should be the tr
						var id = clientTable.getODataId(row);
						var $cell = $(cell);

						$cell.html(template);
						$cell.find('a').button({
							text : false,
							icons : {
								primary : "ui-icon-trash"
							}
						});

					});
				},
				getODataId : function(arg) {
					var key = clientTable.squashSettings.dataKeys.entityId;
					var id = clientTable.fnGetData(arg)[key];
					if (!!id) {
						return id;
					} else {
						return null;
					}
				}
			}
		};

		var clientTable = $("#client-table").squashTable({
			"bServerSide" : false
		}, squashSettings);
	});

	$("#delete-client-popup").confirmDialog().on('confirmdialogconfirm', function(event) {

		var $this = $(this);
		var id = $this.data('entity-id');
		var ids = (!!id) ? [ id ] : id;
		var url = squashtm.app.contextRoot + '/administration/config/clients/' + ids.join(",");
		var table = $("#client-table").squashTable();
		var selectedRow = table.getRowsByIds(ids);

		$.ajax({
			url : url,
			type : 'delete'
		}).done(function() {
			table._fnAjaxUpdate();
		});

	});

	$("#delete-client-button").on('click', function() {

		var ids = $("#client-table").squashTable().getSelectedIds();

		if (ids.length > 0) {
			var popup = $("#delete-client-popup");
			popup.data('entity-id', ids);
			popup.confirmDialog('open');
		} else {
			notification.showWarning(translator.get('message.EmptyTableSelection'));
		}
	});

	var addClientDialog = $("#add-client-dialog");

	addClientDialog.formDialog();

	addClientDialog.on('formdialogconfirm', function() {
		var url = squashtm.app.contextRoot + '/administration/config/clients/';
		var params = {
			clientId : $('#add-client-name').val(),
			clientSecret : $('#add-client-secret').val(),
			registeredRedirectUri : $('#add-client-uri').val()
		};
		$.ajax({
			url : url,
			type : 'POST',
			dataType : 'json',
			data : params
		}).success(function(data) {
			$('#client-table').squashTable()._fnAjaxUpdate();
			addClientDialog.formDialog('close');
		});

	});

	addClientDialog.on('formdialogcancel', function() {
		addClientDialog.formDialog('close');
	});

	$('#new-client-button').on('click', function() {
		addClientDialog.formDialog('open');
	});

});
