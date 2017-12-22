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
 * This is an abstract popup, that is usually instancied only once per workspace.
 * Must be supplied the following options :
 *
 * a tree,
 * a permissions-rules
 *
 */
define(['jquery', 'underscore', "workspace.event-bus", 'jquery.squash.formdialog'], function ($, _, eventBus) {

	if (($.squash !== undefined) && ($.squash.delnodeDialog !== undefined)) {
		// plugin already loaded
		return;
	}

	$.widget("squash.delnodeDialog", $.squash.formDialog, {

		// ********** these options are MANDATORY **************
		options: {
			tree: null,
			rules: null,

			// that one is a private working variable and requires no initialization
			'ui-state-update': null
		},

		open: function () {
			this._super();
			this.simulateDeletion();
		},

		// ******************* creates the XHR requests ***********

		getSimulXhr: function (nodes) {
			var ids = nodes.treeNode().all('getResId').join(',');
			var rawUrl = nodes.getDeleteUrl();
			var url = rawUrl.replace('{nodeIds}', ids) + '/deletion-simulation';
			return $.getJSON(url);
		},

		getConfirmXhr: function (nodes) {
			var ids = nodes.treeNode().all('getResId').join(',');
			var rawUrl = nodes.getDeleteUrl();
			var url = rawUrl.replace('{nodeIds}', ids);
			return $.ajax({
				url: url,
				type: 'delete'
			});
		},

		// ********************* callbacks *************************************

		// expects an array of array
		deletionSuccess: function (responsesArray) {

			var tree = this.options.tree;
			var uiUpdate = this.options['ui-state-update'];

			uiUpdate.previous.deselect_all();

			var i = 0, len = responsesArray.length;
			for (i = 0; i < len; i++) {
				if (responsesArray[i] === null || responsesArray[i] === undefined) {
					continue;
				}
				var commands = responsesArray[i][0];
				tree.jstree('apply_commands', commands);
			}

			this.close();

			uiUpdate.next.select();
		},

		// expects an array of array
		simulationSuccess: function (responsesArray) {

			var htmlDetail = '';

			$.each(responsesArray, function (idx, arg) {
				if (arg !== null && arg !== undefined) {
					var messages = arg[0].messages;
					for (var i = 0, len = messages.length; i < len; i++) {
						htmlDetail += '<li>' + messages[i] + '</li>';
					}
				}
			});

			if (htmlDetail.length > 0) {
				this.element.find('.delete-node-dialog-details').removeClass('not-displayed').find('ul').html(
					htmlDetail);
			} else {
				this.element.find('.delete-node-dialog-details').addClass('not-displayed');
			}

			this.setState('confirm');
		},

		// ***************************** ajax queries *******************************

		/*
		 * because $.when(deferred(s)).done(something) is supplied with inconsistent arguments given the number of
		 * deferred in the .when() clause, we must ensure that the result of the operation will be an array of array as
		 * the callbacks expect it.
		 */
		smartAjax: function (xhrs, callback) {
			var self = this;
			// case of an array of xhr : the result will be an array of array as expected
			if (_.isArray(xhrs) && xhrs.length > 1) {
				return $.when.apply($, xhrs).done(function () {
					callback.call(self, arguments);
				}).fail(function () {
					self.close();	// other error handling mechanism should kick in
				});
			}
			// case of a single xhr : the result will be an array, that we transform in an array of array as expected
			else {
				return $.when(xhrs).done(function () {
					callback.call(self, [arguments]);
				}).fail(function () {
					self.close();	// other error handling mechanism should kick in
				});
			}
		},

		// ******************** deletion simulation *************

		simulateDeletion: function () {
			var self = this;
			var tree = this.options.tree;
			var rules = this.options.rules;

			// first, check that the operation is allowed.
			this.setState("pleasewait");

			var nodes = tree.jstree('get_selected');
			this.uiDialog.data('selected-nodes', nodes);

			if (!rules.canDelete(nodes)) {
				var why = rule.whyCantDelete(nodes);
				this.uiDialog.find('[data-def="state=rejected"]').text(why);
				this.setState('rejected');

				return;
			}

			// else we can proceed.
			var xhrs = this.getSimulXhr(nodes);

			this.smartAjax(xhrs, this.simulationSuccess).fail(function () {
				self.setState('reject');
			});
		},

		// ********************** actual deletion*********************

		_findPrevNode: function (nodes) {
			var tree = this.options.tree;
			var oknode = tree.find(':library').filter(':first');

			if (nodes.length === 0) {
				return oknode;
			}
			var ids = nodes.all('getResId');
			var ancestry = nodes.first().treeNode().getAncestors().get().reverse();

			$(ancestry).each(function () {
				var $this = $(this), $thisid = $this.attr('resid');
				if ($this.is(':library') || $.inArray($thisid, ids) == -1) {
					oknode = $this.treeNode();
					return false; // means 'beak' in .each
				}
			});

			return oknode;

		},

		performDeletion: function () {
			this.setState("pleasewait");

			var self = this;
			var tree = this.options.tree;
			var nodes = this.uiDialog.data('selected-nodes');
			var newSelection = this._findPrevNode(nodes);

			//[#6937] : the tree reselects the parent node before the deleted nodes are
			// actually deleted, which can lead to inaccurate model sometimes
			this.options['ui-state-update'] = {
				previous: nodes,
				next: newSelection
			};


			this.setState('pleasewait');

			var xhrs = this.getConfirmXhr(nodes);

			this.smartAjax(xhrs, this.deletionSuccess);

		}

	});

});
