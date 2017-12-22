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
 * options : {
 * 	entityUrl : if set, the component will update its content itself whenever a POST ajax request is made
 * 				in the document.
 * 	format : the date format
 * 	never : the label 'never' displayed when no modification ever happend on that entity.
 * }
 *
 */
define([ "jquery", "squash.dateutils", "./general-information-panel-controller", "squash.statusfactory" ], function($,
		dateutils, Controller, statusfactory) {
	"use strict";
	function updateDateInformations(options, infos) {
		infos = infos || defaults();

		// update the dates
		var newExecutedOn = (infos.executedOn !== null && infos.executedOn.length > 0) ? dateutils.format(infos.executedOn,
				options.format) : "";
		var newExecutedBy = (infos.executedBy !== null && infos.executedBy.length > 0) ? '(' + infos.executedBy + ')'
				: options.never;

		$("#last-executed-on  .datetime").text(newExecutedOn);
		$("#last-executed-on  .author").text(newExecutedBy);

	}

	function defaults() {
		return {
			executedOn : $("#last-executed-on .datetime").text(),
			executedBy : $("#last-executed-on .author").text()
		};
	}

	return new Controller(updateDateInformations);
});