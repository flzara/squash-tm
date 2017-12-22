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
define([ "jquery", "tree", "handlebars", "underscore", "workspace/workspace.import-popup" ], function($, zetree, Handlebars, _) {
	"use strict";

	var recapBuilder = {};

	/**
	 * builds recap for xls import
	 *
	 * @param json
	 * @returns recap as an html string
	 */
	recapBuilder.xls = function(json) {
			var templateOk =  Handlebars.compile($("#xls-import-recap-tpl").html()); // caching
			return templateOk(json);
	};

	$.widget("squash.reqImportDialog", $.squash.importDialog, {

		importType : 'xls',

		createSummary : function(json) {
			this.element.find(".import-summary").html(recapBuilder[this.importType].call(this, json));
		},
		createFormatErrorsSummary : function(json){
			this.templateKoMiss = Handlebars.compile($("#xls-import-recap-ko").html());
			var htmlTpl = this.templateKoMiss(json);
			this.element.find(".error-format").html(htmlTpl);
		},

		bindEvents : function() {
			this._super();
			var self = this;

			this.onOwnBtn("ok", function() {
				var tree = zetree.get();
				tree.find('[restype="requirement-libraries"]').each(
						function(idx, elt){
							tree.jstree("refresh", this);
							});
			});

			this.onOwnBtn("simulate", function() {
				if (self.validate() === true) {
					self.simulate();
				} else {
					self.setState("error-type");
				}
			});

			this.element.on("change", "input[type='file']", function() {
				var filename = /([^\\]+)$/.exec(this.value)[1];
				self.element.find(".confirm-file").text(filename);
			});
		},

		simulate : function() {
			this.setState("progression");
			this.doSubmit({
				urlPostfix : "/" + this.importType,
				queryParams : {
					"dry-run" : true
				}
			});
		},

		submit : function() {
			this.setState("progression");
			this.doSubmit({
				urlPostfix : "/" + this.importType
			});
		}

	});

	function init() {
		$("#import-excel-dialog").reqImportDialog({
			formats : [ "xls", "xlsx", "xlsm" ],
			typeFormats : {
				xls : [ "xls", "xlsx", "xlsm" ]
			}
		});
	}

	return {
		init : init
	};

});
