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
define(['jquery', 'backbone', 'workspace.routing', './views/affectedView', './views/globalView', './views/traitmentView'],
	function ($, Backbone, urlBuilder, affectedView, globalView, traitmentView) {
		"use strict";


		var router = Backbone.Router.extend({
			activeView: null,
			initialize: function () {

			},

			routes: {
				"": "affected",
				"affected": "affected",
				"global": "global",
				"traitment": "traitment",
			},

			affected: function () {
				this.activeView = new affectedView();
			},

			global: function () {
				this.activeView = new globalView();
			},

			traitment: function () {
				this.activeView = new traitmentView();
			}

		});

		function init() {
			return new router;
		}
		return {
			init: init
		};
	})
