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
define([ "jquery", "app/squash.wreqr.init", "backbone", "handlebars", "underscore", "workspace.routing", "squash.translator", 'app/ws/squashtm.notification', "jquery.squash.formdialog" ],
	function($, squash, Backbone, Handlebars, _, api, msg, notification) {
	"use strict";

	msg.load([
			"popup.title.error",
			"message.EmptyTableSelection"
	]);

	/**
	 * Creates a binding action function with the given configuration.
	 *
	 * @param apiUrl
	 *					the Url of the request to send.
	 * @param method
	 *          the method for the created function.
	 *
	 * @return function which send a request for the given id(s)
	 *
	 * 		@param ids
	 *    			either an id or an array of ids which will be send as parameters
	 * 		@return a promise (the xhr's)
	 */
	function bindingActionCallback(apiUrl, method) {
		return function(ids, data) {
			var url = apiUrl + "/" + (_.isArray(ids) ? ids.join(',') : ids);

			return $.ajax({
				url : url,
				type : method,
				dataType : 'json',
				data : data
			});
		};
	}

	function unbindDialogSucceed(self) {
		return function(ids) {
			table().refresh(); // meh, should be triggered by event
			squash.vent.trigger("linkedrequirementspanel:unbound", {
				model : ids
			});
		};
	}

	/**
	 * returns the datatable (well, squashtable) object for verifying TCs
	 */
	function table() {
		return $("#linked-requirement-versions-table").squashTable();
	}

	var View = Backbone.View.extend({
		el: "#linked-requirement-versions-table",

		initialize : function(options) {
			this.apiUrl = options.apiUrl;

			// slaps unbind dialogs in dom
			var tpl = Handlebars.compile($("#unbind-linked-reqs-dialog-tpl").html());
			var dlgs = tpl({
				dialogId : "unbind-selected-linked-reqs-rows-dialog"
			}) + tpl({
				dialogId : "unbind-active-linked-reqs-row-dialog"
			});
			this.$el.append(dlgs);

			var tmplLinkType = Handlebars.compile($("#choose-link-type-dialog-tpl").html());
			var linkTypeDialog = tmplLinkType({
				dialogId : "choose-link-type-dialog"
			});
			this.$el.append(linkTypeDialog);

			var tmplSummaryDialog = Handlebars.compile($("#add-summary-dialog-tpl").html());
      var addSummaryDialog = tmplSummaryDialog();
      this.$el.append(addSummaryDialog);

			var unbind = bindingActionCallback(this.apiUrl, "delete");

			// unbind multiple items dialog
			var $batch = this.$("#unbind-selected-linked-reqs-rows-dialog");
			$batch.formDialog();

			$batch.on("formdialogopen", this.onOpenBatch);
			$batch.on("formdialogconfirm", this.onConfirmBatchCallback(unbind, unbindDialogSucceed($batch)));
			$batch.on("formdialogcancel", this.onCloseBatch);

			// unbind single item dialog
			var $single = this.$("#unbind-active-linked-reqs-row-dialog");
			$single.formDialog();

			$single.on("formdialogopen", this.onOpenBatch);
			$single.on('formdialogconfirm', this.onConfirmSingleCallback(unbind, unbindDialogSucceed($single)));
			$single.on('formdialogcancel', this.onCloseSingle);

			this.listenTo(squash.vent, "linkedrequirementversions:unbind-selected", function(event) {
				$("#unbind-selected-linked-reqs-rows-dialog").formDialog("open");
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
				notification.showError(msg.get('message.EmptyTableSelection'));
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
