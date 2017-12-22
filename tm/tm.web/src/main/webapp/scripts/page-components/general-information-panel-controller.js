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
 * This module returns the constructor for GeneralInformationPanelController.
 * A GeneralInformationPanelController fetches its options as written below.
 * It should be given an updater function which is notified when the panel should be refreshed.
 *
 * updater signature should be : functino(opts, jsonData)
 *
 * When no json data is provided, updater should use sensible defaults
 *
 * options : {
 * 	entityUrl : if set, the component will update its content itself whenever a POST ajax request is made
 * 				in the document.
 * 	format : the date format
 * 	never : the label 'never' displayed when no modification ever happend on that entity.
 * }
 *
 */
define([ "jquery", "squash.attributeparser" ], function($, attrparser) {
	"use strict";

	function GeneralInformationPanelController(updater) {
		if (!(this instanceof GeneralInformationPanelController)) {
			return new GeneralInformationPanelController(updater);
		}
		var elt = $("#general-information-panel");
		var stropts = elt.data("def");
		this.opts = attrparser.parse(stropts);
		this.updater = updater;
	}

	GeneralInformationPanelController.prototype.refresh = function() {
		var self=this;
		if (this.opts.url) {
			$.ajax({
				type : "GET",
				url : this.opts.url + "/general",
				dataType : "json"
			}).done(function(json) {
				self.updater.call(self, self.opts, json);
			});
		}
	};

	GeneralInformationPanelController.prototype.init = function() {
		var self = this;

		this.updater.call(this, this.opts);

		if (!!this.opts.url) {
			$("#general-information-panel").ajaxSuccess(function(event, xrh, settings) {
				if (settings.type === "POST" && settings.url.indexOf(self.opts.url) > -1) {
					self.refresh();
				}
			});
		}
	};

	return GeneralInformationPanelController;
});