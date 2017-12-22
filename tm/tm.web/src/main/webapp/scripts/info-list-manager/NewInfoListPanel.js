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
		[ "jquery", "app/BindView", "handlebars", "backbone.validation", "./InfoListOptionPanel", "./InfoListOptionModel", "./InfoListOptionCollection", "app/lnf/Forms", "squashtable/squashtable.options", "squash.configmanager", "info-list/IconSelectDialog", "squash.translator", "jquery.squash.confirmdialog" ],
		function InfoListPanel($, BindView, Handlebars, Validation, InfoListOptionPanel, InfoListOptionModel, InfoListOptionCollection, Forms, SquashTable, confman, IconPicker, messages) {
			"use strict";

			var validationOptions = {
				valid : function(view, prop) {
					if (prop === "items") {
						Forms.input(view.$("#options-table")).setState("xsuccess");
					} else {
						// not "success" state because we do not want green fields. yet.
						view.boundControl(prop).setState("xsuccess");
					}
				},
				invalid : function(view, prop, err) {
					console.log(view, prop, err);
					// find something better when there is more not-bound props
					if (prop === "items") {
						Forms.input(view.$("#options-table")).setState("error", err);
					} else {
						view.boundControl(prop).setState("error", err);
					}
				}
			};
			
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

			function initOptionsTable(view) {
				var radio = SquashTable.renderer($("#default-cell-tpl").html())(function(data, type, row) {
					return { checked: data, option: row.code };
				});

				var remove = SquashTable.renderer($("#remove-cell-tpl").html())(function(data, type, row) {
					return { value: row.code, name: "option-delete" };
				});

				// renderer is a composition of 2 handlebars renderers.
				// this is wrapped into self exec fn so that composed renderers are in the closure
				// instead of being resolved at each exec
				var icon = (function() {
					var mapper = function(data, type, row) {
						return { code: row.code, icon: row.iconName };
					};

					var notEmpty = SquashTable.renderer($("#icon-cell-tpl").html())(mapper);
					var empty = SquashTable.renderer($("#noicon-cell-tpl").html())(mapper);

					return function(data, type, row) {
						return (_.isEmpty(data) ? empty : notEmpty).apply(this, arguments);
					};
				})();

				var colDefs = SquashTable.colDefs()
					.std(0, "label")
					.std(1, "code")
					.std({ targets: 2, render: icon, data: "iconName" })
					.radio({ targets: 3, render: radio, data: "isDefault" })
					.button({ targets: 4, render: remove, data: "" })
					.build();

				view.$optionsTable = view.$("#options-table");
				view.$optionsTable.DataTable({
					//"oLanguage" : {
					//	"sUrl" : squashtm.app.cfTable.languageUrl
					//},
					//"bAutoWidth" : false,
					jQueryUI : true,
					filter : false,
					paginate : false,
					columnDefs: colDefs,
					language :  oLanguage,
					data: []
				});
			}

			/**
			 * triggers confirmation of option deletion
			 * @param opt the option to delete
			 * @returns a promise
			 */
			function confirmDeleteDefaultOpt(opt) {
				// a minima implementation. it theory it should notify user to pee off
				return confirmDeleteStdOpt(opt);
			}

		/**
		 * triggers confirmation of option deletion
		 * @param opt the option to delete
		 * @returns a promise
		 */
			function confirmDeleteStdOpt(opt) {
				// a minima implementation. confirm popup as per spec seems useless anyway
				var def = $.Deferred();
				def.resolve();
				return def.promise();
			}

			/**
			 * Renders the icon picker if necessary
			 */
			function renderIconPicker() {
				if (IconPicker.template === undefined) {
					var src = $("#icon-picker-dialog-tpl").html();
					IconPicker.template = Handlebars.compile(src);
				}
				if ($("#icon-picker-dialog").length === 0) {
					$("body").append($(IconPicker.template({})));
				}
			}

			/*
			 * Defines the controller for the new custom field panel.
			 *
			 * DESIGN NOTE : confimdialog would not correctly bubble its events when it is not the view's $el.
			 * On the other hand, this.$el is
			 *
			 */
			var NewInfoListPanel = BindView.extend({
				wrapper: "#new-item-pane",
				defaultWidth : 600,
				richWidth : 1000,

				initialize : function() {
					var self = this;
					this.apiRoot = $(this.wrapper).data("api-url");

					this.options = new InfoListOptionCollection([], { apiRoot: this.apiRoot });

					this.listenTo(squashtm.vent, "list-option:add", this.onAddListOption);
					this.listenTo(squashtm.vent, "iconselectdialog:cancelled", this.onIconPickingCancelled);
					this.listenTo(squashtm.vent, "iconselectdialog:confirmed", this.onIconPicked);
					this.listenTo(this.options, "add remove change", this.onOptionsChanged);

					Validation.bind(this, validationOptions);

					this.render();
					initOptionsTable(this);

					this.dialog = this.$el.confirmDialog({
						autoOpen : true,
//						close : function() {
//							self.cancel.call(self);
//						}
					});

					this._resize();
				},

				render : function() {
					if (this.template === undefined) {
						var src = $("#new-item-pane-tpl").html();
						NewInfoListPanel.prototype.template = Handlebars.compile(src);
					}
					this.$el.append($(this.template({})));

					var $wrapper = $(this.wrapper);
					this.$el.attr("title", $wrapper.attr("title"));
					this.$el.addClass($wrapper.attr("class"));
					/*var conf = confman.getStdCkeditor();
					this.$("#description").ckeditor(function(){}, conf);

					CKEDITOR.instances["description"].on('change', function(){
						$("#description").trigger('change');
					});*/
					
					$wrapper.html(this.$el);

					return this.renderItemPanel();
				},

				renderItemPanel: function() {
					this.itemPanel = new InfoListOptionPanel({model: new InfoListOptionModel()});
					return this;
				},

				_resize : function(){
					if (this.$el.data().confirmDialog !== undefined){
						var type = this.model.get("inputType");
						var width = (type === "RICH_TEXT") ? this.richWidth : this.defaultWidth;
						this.$el.confirmDialog("option", "width", width);
					}
				},

				remove : function() {
					Validation.unbind(this);
					this.undelegateEvents();
					this.itemPanel.remove();
					this.iconPicker && this.iconPicker.remove();
					BindView.prototype.remove.apply(this, arguments);
				},

				events : {
					"confirmdialogcancel" : "cancel",
					"confirmdialogvalidate" : "validate",
					"confirmdialogconfirm" : "confirm",
					"draw.dt": "onDrawOptionsTable",
					"change input:radio[name='option-default']": "onChangeDefaultOption",
					"click button[name='option-delete']": "onClickOptionDelete",
					"click .option-icon": "onClickListOptionIcon"
				},

				cancel : function(event) {
					window.squashtm.vent.trigger("newinfolist:cancelled", { model: this.model, view: this, source: event });
				},

				confirm : function(event) {
					window.squashtm.vent.trigger("newinfolist:confirmed", { model: this.model, view: this, source: event });
				},

				validate : function(event) {
					var sserr = false;
					var csok = this.model.save(null, {

						url: this.apiRoot + "/new",
						async: false,
						wait: true, // that's a sync request
						error : function() {
							console.log("save error", arguments);
							sserr = true;
							event.preventDefault();
						}
					});

					return csok && !sserr;
				},

				onChangeDefaultOption: function(event) {
					var tgt = event.target;
					var code = tgt.value;
					this.options.forEach(function(opt) {
						(opt.get("code") === code) ? opt.set("isDefault", true) : opt.set("isDefault", false);
					});
				},

				onClickOptionDelete: function(event) {
					var tgt = event.currentTarget;
					var code = $(tgt).data("value");
					var opt = this.options.findWhere({ "code": String(code) });
					var self = this;

					var confirm = opt.get("isDefault") === true ? confirmDeleteDefaultOpt : confirmDeleteStdOpt;

					confirm(opt).done(function() {
						if (!!self.options.remove(opt)) {
							self.$optionsTable.DataTable()
							.row($(tgt).closest("tr")).remove()
							.draw();
						}
					});
				},
				/**
				 * Creates a function which changes the icon of the row that was clicked
				 * @param event
				 * @returns {Function}
				 */
				changeRowIconCallback: function(event) {
					var tgt = event.currentTarget;
					var code = $(tgt).data("code");
					var opt = this.options.findWhere({ code: code });
					var $tr = $(tgt).closest("tr");
					var self = this;

					return function(icon) {
						opt.set("iconName", icon);
						self.$optionsTable.DataTable().row($tr).data(opt.attributes).draw();
					};
				},

				onOptionsChanged: function(event) {
					this.model.set("items", this.options.toJSON());
				},

				onAddListOption: function(event) {
					this.options.add(event.model);
					this.$optionsTable.DataTable()
						.row.add(event.model.attributes)
						.draw();
					this.itemPanel.remove();
					this.renderItemPanel();
				},

				/**
				 * handler of the icon in the options table
				 * @param event
				 */
				onClickListOptionIcon: function(event) {
					renderIconPicker();
					this.iconPickedCallback = this.changeRowIconCallback(event);
					var tgt = event.currentTarget;
					var opt = this.options.findWhere({ code: $(tgt).data("code").toString() });
					this.iconPicker = this.iconPicker || new IconPicker({ el: "#icon-picker-dialog", model: { icon: opt.get("iconName") } });
					this.iconPicker.open();
				},

				onIconPicked: function(event) {
					if (event.view !== this.iconPicker) {
						return; // bail out
					}
					if (!!this.iconPickedCallback) {
						this.iconPickedCallback(event.model.icon);
					}
					this.onIconPickingCancelled(event);
				},

				onIconPickingCancelled: function(event) {
					if (event.view !== this.iconPicker) {
						return; // bail out
					}
					event.view.remove();
					this.iconPicker = undefined;
					this.iconPickedCallback = undefined;
				}
			});

			return NewInfoListPanel;
		});