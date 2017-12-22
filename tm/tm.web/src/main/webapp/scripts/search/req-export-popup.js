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
define([ "jquery", "backbone", "underscore", "workspace.routing", "squash.translator","squash.dateutils", "jquery.squash.formdialog"],
		function($, Backbone, _, routing, translator, dateutils) {
	"use strict";


	var View = Backbone.View.extend({
		el : ".export-requirement-dialog",
		initialize : function() {

			var self = this;
			this.dialog = this.$el.formDialog({
				autoOpen : false,
				width : 800
			});

		},

		events : {
			"formdialogcancel" : "cancel",
			"formdialogconfirm" : "confirm"
		},

		open : function(options){
			this.dialog.formDialog('open');
			this._init();
		},

		cancel : function(event) {
			this.cleanup();
		},

		confirm : function() {
			var table = $('#requirement-search-result-table').squashTable();
			var ids = table.getSelectedIds();

			if (ids.length === 0) {
				var data =  table.fnGetData();
				ids = _.map(data,function (req) {
					return req["requirement-id"];
				});
			}

			var url = this._createUrl(ids);
			document.location.href = url;

			this.cleanup();
		},

		cleanup : function() {
			this.dialog.formDialog('close');
		},

		remove : function() {
			this.cleanup();
			this.undelegateEvents();
		},

		//REQUIREMENT EXPORT URL
		_createUrl : function(nodes){
			var url = window.squashtm.app.contextRoot+'/requirement-browser/searchExports';
			var filename = this.$el.find("#export-name-input").val();
			var keepRte = this.$el.find("#export-keepRteFormat").prop('checked');

			var params = {
				'filename' : filename,
				'nodes' : nodes.join(),
				'keep-rte-format' : keepRte
			};

			return url+"?"+$.param(params);

		},

		_init : function () {
			this.$el.find("#export-name-input").val(this._createName());
			this.$el.find("#export-keepRteFormat").prop('checked', true);
		},

		_createName : function () {
			var prefix =  translator.get("label.lower.dash.exportRequirements");
			var date =  dateutils.format(new Date(), "yyyyMMdd_HHmmss");
			return prefix + "_" + date;
		}


	});

	return View;
});
