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
define([ 'jquery', 'jstree' ], function($) {

	return function() {

		/*
		 * Thanks to https://github.com/vakata/jstree/blob/master/src/misc.js
		 */
		"use strict";
		$.jstree.defaults.conditionalselect = function() {
			return true;
		};
		$.jstree.plugin("conditionalselect", {
			_fn : {
				select_node : function(obj, supress_event, prevent_open) {

					if (this.get_settings().conditionalselect.call(this, this._get_node(obj))) {
						this.__call_old("select_node", obj, supress_event, prevent_open);
					}
				}
			}
		});
	};

});