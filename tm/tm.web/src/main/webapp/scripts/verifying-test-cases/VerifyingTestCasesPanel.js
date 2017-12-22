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
define([ "jquery", "app/squash.wreqr.init", "backbone", "handlebars", "underscore", "workspace.routing", "squash.translator", "jquery.squash.formdialog" ],
	function($, squash, Backbone, Handlebars, _, api, msg) {
	"use strict";

	msg.load([
			"popup.title.error",
			"message.EmptyTableSelection"
	]);

	/**
	 * Creates an binding action functino with the given configuration
	 *
	 * @param conf
	 *          the configuration for the created function
	 * @param method
	 *
	 * @return function which posts an unbind request for the given id(s)
	 * @param ids
	 *          either an id or an array of ids
	 * @return a promise (the xhr's)
	 */
	function bindingActionCallback(apiUrl, method) {
		return function(ids) {
			var url = apiUrl + "/" + (_.isArray(ids) ? ids.join(',') : ids);

			return $.ajax({
				url : url,
				type : method,
				dataType : 'json'
			});
		};
	}

	function unbindDialogSucceed(self) {
		return function(ids) {
			table().refresh(); // meh, should be triggered by event
			squash.vent.trigger("verifyingtestcasespanel:unbound", {
				model : ids
			});
		};
	}

	/**
	 * returns the datatable (well, squashtable) object for verifying TCs
	 */
	function table() {
		return $("#verifying-test-cases-table").squashTable();
	}

	var View = Backbone.View.extend({
		el: "#verifying-test-cases-table",

		initialize : function(options) {
			this.apiUrl = options.apiUrl;

			// slaps unbind dialogs in dom
			var tpl = Handlebars.compile($("#unbind-dialog-tpl").html());
			var dlgs = tpl({
				dialogId : "unbind-selected-rows-dialog"
			}) + tpl({
				dialogId : "unbind-active-row-dialog"
			});
			this.$el.append(dlgs);

			var unbind = bindingActionCallback(this.apiUrl, "delete");

			// unbind multiple items dialog
			var $batch = this.$("#unbind-selected-rows-dialog");
			$batch.formDialog();

			$batch.on("formdialogopen", this.onOpenBatch);
			$batch.on("formdialogconfirm", this.onConfirmBatchCallback(unbind, unbindDialogSucceed($batch)));
			$batch.on("formdialogcancel", this.onCloseBatch);

			// unbind single item dialog
			var $single = this.$("#unbind-active-row-dialog");
			$single.formDialog();

			$single.on("formdialogopen", this.onOpenSingle);
			$single.on('formdialogconfirm', this.onConfirmSingleCallback(unbind, unbindDialogSucceed($single)));
			$single.on('formdialogcancel', this.onCloseSingle);

			this.listenTo(squash.vent, "verifying-test-cases:unbind-selected", function(event) {
				$("#unbind-selected-rows-dialog").formDialog("open");
			});
		},

		remove : function() {
			this.$("#unbind-selected-rows-dialog").off();
			this.$("#unbind-selected-rows-dialog").formDialog("destroy");
			this.$("#unbind-active-rows-dialog").off();
			this.$("#unbind-active-rows-dialog").formDialog("destroy");
			Backbone.View.prototype.remove.apply(this, arguments);
		},

		onOpenBatch : function() {
			// read the ids from the table selection
			var ids = table().getSelectedIds();

			if (ids.length === 0) {
				$(this).formDialog('close');
				$.squash.openMessage(msg.get("popup.title.error"), msg.get("message.EmptyTableSelection"));

			} else if (ids.length === 1) {
				$(this).formDialog("setState", "confirm-deletion");

			} else {
				$(this).formDialog("setState", "multiple-tp");
			}
		},

		onConfirmBatchCallback : function(unbind, batchSucceed) {
			return function() {
				var ids = table().getSelectedIds();
				if (ids.length > 0) {
          $(this).formDialog("close");
					unbind(ids).done(unbindDialogSucceed(ids));
				}
			};
		},

		onCloseBatch : function() {
			$(this).formDialog('close');
		},

		onOpenSingle : function() {
			var id = $(this).data("entity-id");

			if (id === undefined) {
				$(this).formDialog("close");
				notification.showError(translator.get('message.EmptyTableSelection'));
			} else {
				$(this).formDialog("setState", "confirm-deletion");
			}
		},

		onConfirmSingleCallback : function(unbind, singleSucceed) {
			return function() {
				var self = this;
				var id = $(this).data('entity-id');

				if (id !== undefined) {
					unbind(id).done(function() {
						singleSucceed([ id ]);
						$(self).data('entity-id', null);
					});
				}
        $(this).formDialog("close");
			};
		},

		onCloseSingle : function() {
			$(this).formDialog('close');
			$(this).data('entity-id', null);
		}

	});

	View.bindingActionCallback = bindingActionCallback;

	return View;
});
