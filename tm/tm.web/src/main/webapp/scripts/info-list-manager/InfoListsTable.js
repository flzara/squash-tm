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
define(
	[ "jquery", "backbone", "underscore", "../squashtable/squashtable.options", "jquery.squash.oneshotdialog", "squash.translator", "app/ws/squashtm.notification","handlebars"],
	function($, Backbone, _, SquashTable, oneshot, messages, notif, Handlebars) {
		"use strict";

		messages.load([
			"message.infoList.remove.first",
			"message.infoList.remove.second",
			"message.infoList.remove.third",
			"message.infoList.remove.fourth",
			"message.infoList.bound.remove.first",
			"message.infoList.bound.remove.second",
			"message.infoList.bound.remove.third",
			"message.infoList.bound.remove.fourth",
			"message.infoList.batchRemove.first",
			"message.infoList.batchRemove.second",
			"message.infoList.batchRemove.third",
			"message.infoList.batchRemove.fourth",
			"message.infoList.batchRemove.first",
			"message.infoList.bound.batchRemove.second",
			"message.infoList.bound.batchRemove.third",
			"message.infoList.bound.batchRemove.fourth",
			"message.noListSelected",
			"dialog.info-list.warning.reindex.before",
			"label.GotoIndex"
		]);

		// TODO revert that crap back to be58f1969577
		var oLanguage = messages.get({
			'sLengthMenu' : 'generics.datatable.lengthMenu',
			'sZeroRecords' : 'generics.datatable.zeroRecords',
			'sInfo' : 'generics.datatable.info',
			'sInfoEmpty' : 'generics.datatable.infoEmpty',
			'sInfoFiltered' : 'generics.datatable.infoFiltered',
			'sSearch' : 'generics.datatable.search',
			'oPaginage' : {
				'sFirst' : 'generics.datatable.paginate.first',
				'sPrevious' : 'generics.datatable.paginate.previous',
				'sNext' : 'generics.datatable.paginate.next',
				'sLast' : 'generics.datatable.paginate.last'
			}
		});

		var itemsTableConf = window.squashtm.app.itemsTable;
		console.log("table conf" ,itemsTableConf);

		function buggerOffReindex(event) {
			$(this).dialog("close").dialog("destroy");
			this.remove();
		}

		var gotoIndexButton = {
				'text' : messages.get("label.GotoIndex"),
				'click' : function() {
					buggerOffReindex.apply(this, arguments);
					document.location.href=  squashtm.app.contextRoot + "/administration/indexes";
				}
			};

		var closeButton = {
				'text' : messages.get("label.Close"),
				'click' : buggerOffReindex
			};

		var isAdmin = window.squashtm.app.isAdmin;

		function selectTr($tr) {
			$tr.removeClass("ui-state-highlight").addClass("ui-state-row-selected");
		}

		function itemIdMapper(data) {
			return data.id;
		}

		function removeTemplate() {
			removeTemplate.tpl = removeTemplate.tpl || Handlebars.compile($("#confirm-remove-tpl").html());
			return removeTemplate.tpl;
		}

		function reindexTemplate() {
			reindexTemplate.tpl = reindexTemplate.tpl || Handlebars.compile($("#confirm-remove-reindex").html());
			return reindexTemplate.tpl;
		}

		function popupReindex() {
			var buttonConf = [];
			isAdmin && buttonConf.push(gotoIndexButton);
			buttonConf.push(closeButton);
			oneshot.show(messages.get("label.Delete"), reindexTemplate()(), { buttons : buttonConf });
		}

		function removeProps(batch) {
			var flavor = batch ? "batchRemove." : "remove.";

			return function(bound) {
				var binding = bound ? "bound." : "";
				var memo = {"warn-index":  (bound ? messages.get("dialog.info-list.warning.reindex.before") : "") };

				return ["first", "second", "third", "fourth"].reduce(function(memo, item) {
					memo[item] = messages.get("message.infoList." + binding + flavor + item);
					return memo;
				}, memo);
			};
		}

		/*
		 * Defines the controller for the custom fields table.
		 */
		var View = Backbone.View.extend({
			el : "#items-table",
			initialize : function() {
				var self = this;

				_.bindAll(this, "refresh", "onInitTable", "removeSelectedItems");

				this.apiRoot = this.$el.data("api-url");
				this.viewUrl = this.$el.data("view-url");
				this.selectedIds = [];

				this.listenTo(squashtm.vent, "newinfolist:confirmed", function(event) {
					self.refresh();
				});

				this.$el.on("init.dt", function(event) { self.onInitTable(event); });

				var remove = SquashTable.renderer($("#remove-cell-tpl").html())(function(data, type, row) {
					return { value: row.id, name: "list-delete" };
				});

				var editLink = SquashTable.renderer($("#name-cell-tpl").html())(function(data, type, row) {
					return { url:  self.viewUrl + "/" + row.id, text: data };
				});

				var colDefs = SquashTable.colDefs()
					.hidden(0, "id")
					.index(1)
					.std({ targets: 2, data: "label", render: editLink })
					.std(3, "description")
					.std(4, "defaultLabel")
					.std(5, "code")
					.calendar(6, "createdOn")
					.std(7, "createdBy")
					.datetime(8, "lastModifiedOn")
					.std(9, "lastModifiedBy")
					.button({ targets: 10, render: remove })
					.hidden(11, "bound")
					.build();

				var tableConfig = {
						jQueryUI: true,
						searching: true,
						pagingType: "squash",
						pageLength: this.$el.data("pageLength"),
						dom: '<"dataTables_header"fr>t<"dataTables_footer"lp>',
						order: [ [ 2, "asc" ] ],
						columnDefs: colDefs,
						language :  oLanguage,
						//	url: this.$el.data("language-url")
						// },
						// we cannot init ajax with deferred fetch and client-side processing,
						// so ajax is configured later on "init.dt" event
//							ajax: this.viewUrl,
						deferLoading: this.$("tbody > tr").size()
				};

				this.$el.DataTable(tableConfig);
			},

			events: {
				"click button[name='list-delete']": "onClickListDelete",
				"click td.select-handle": "onClickSelectHandle",
				"draw.dt": "onDrawTable",
				"order.dt" : "fixorder",
				"search.dt" : "fixorder",
			},

			fixorder : function(){
				//fix order
				var self = this;

				self.$el.DataTable().rows().data().each(function(rowdata, index){

				var row =	self.$el.DataTable().row(function ( idx, data, node ) {return data.id === rowdata.id;});
				row.data()['1'] = index+1;
				self.$el.DataTable().cell(row.node(), 1).node().innerHTML = index+1;
				});



			},
			/*
			 * that method programatically remove the highlight due to native range selection.
			 */
			clearRangeSelection : function clearRangeSelection() {
				if (window.getSelection) {
					window.getSelection().removeAllRanges();
				} else if (document.selection) { // should come last; Opera!
					document.selection.empty();
				}
			},
			onInitTable: function onInitTable(event) {
				this.$el.DataTable().ajax.url(this.viewUrl);
			},

			onDrawTable: function(event) {
				var self = this;

				var selector = function(idx, data, node) {
					return self.selectedIds.indexOf(data.id) > -1;
				};

				var $trs = self.$el.DataTable().rows(selector).nodes().to$();
				selectTr($trs);
			},

			/**
			 * refreshes this view / refetches table content. Context is bound to this object.
			 * @param event
			 */
			refresh: function() {
				this.$el.DataTable().ajax.reload();
			},

			onClickSelectHandle: function(event) {
				// had to copy from function buried inside squashtable-main.
				// this is sort of generic code, it should be factored out somewhere reachable
				var c = event.ctrlKey;
				var s = event.shiftKey;
				var $tr = $(event.currentTarget).closest("tr");
				var self = this;

				var toggleSelection = function($tr) {
					var $trs = self.$("tr");
					$trs.removeClass("ui-state-row-selected");
					self.selectedIds = [];
					addToSelection($tr);
				};

				var addToSelection = function($tr) {
					selectTr($tr);
					var row = self.$el.DataTable().row($tr);
					self.lastSelectedRow = row.data()['1'];
					self.selectedIds.push(row.data().id);
					self.clearRangeSelection();
				};

				var growSelection = function($tr) {
					var range = computeSelectionRange($tr);

					self.$el.DataTable().rows(function ( idx, data, node ) {return  _.contains(range, parseInt(data['1']));}).nodes().each(function(row) {
						addToSelection($(row));
					});
				};

				var computeSelectionRange = function($tr) {
					var base = self.lastSelectedRow || 0;
					var current = self.$el.DataTable().row($tr).data()['1'];

					var min = Math.min(base, current);
					min = Math.max(min, 0);

					var max = Math.max(base, current);
					max = Math.min(max, self.$("tr").length - 1);

					return _.range(min, max + 1);
				};

				if (!c && !s) {
					toggleSelection($tr);
				} else if (c & !s) {
					addToSelection($tr);
				} else if (!c & s) {
					growSelection($tr);
				} else {
					growSelection($tr);
				}
			},

			onClickListDelete: function onClickListDelete(event) {
				var self = this;
				var tgt =  event.currentTarget;
				var $tr = $(tgt).closest("tr");
				var isBound = $.parseJSON(this.$el.DataTable().row($tr).data().bound) === true;
				var props = removeProps(false /* not batch */);
				var tpl = removeTemplate()(props(isBound));
				oneshot.show(messages.get("label.Delete"), tpl, { width: "50%" }).done(function() {
					isBound && popupReindex();
					$.ajax(self.apiRoot + "/" + $(tgt).data("value"), { type: "DELETE" })
						.done(self.refresh);
				});
			},

			removeSelectedItems: function removeSelectedItems() {
				var self = this;
				var $sel = this.$(".ui-state-row-selected");
				var rows = this.$el.DataTable().rows($sel);

				if (rows.data().length === 0) {
					notif.showWarning(messages.get("message.noListSelected"));
					return;
				}

				var hasBound = _.some(rows.data(), function(data) { return $.parseJSON(data.bound) === true; });
				var props = removeProps(true /* batch */);
				var tpl = removeTemplate()(props(hasBound));

				oneshot.show(messages.get("label.Delete"), tpl, { width: "50%" }).done(function() {
					hasBound && popupReindex();
					var ids = rows.data().map(itemIdMapper).join(",");
					$.ajax(self.apiRoot + "/" + ids, { type: "DELETE" })
						.done(self.refresh);
				});
			}
		});

		return View;
	});
