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
define([ 'jquery', 'backbone', "underscore", 'app/util/ButtonUtil',  'squash.translator',
         "app/ws/squashtm.notification", "jquery.squash.confirmdialog", 'squashtable',	'jqueryui', 'jquery.squash.formdialog' ],
         function($, Backbone, _, ButtonUtil, translator, notification) {
	"use strict";

	var ScmServersTableView = Backbone.View.extend({
		el : "#scm-server-table-pane",
		initialize : function() {

			// DOM initialized table
			this.table = this.$("table");
			this.table.squashTable(squashtm.datatable.defaults, {
				deleteButtons : {
					delegate : "#",
					tooltip : translator.get('label.Remove')
				}
			});
		}
	});
	return ScmServersTableView;
});
