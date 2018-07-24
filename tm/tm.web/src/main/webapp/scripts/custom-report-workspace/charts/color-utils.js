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
define(['jquery', 'underscore'], function ($, _) {

	var ColorsEnum = {
		// RequirementStatus / TestCaseStatus
		WORK_IN_PROGRESS: "#fabf00",
		UNDER_REVIEW: "#6bbfbd",
		APPROVED: "#6bbf00",
		OBSOLETE: "#b5b5b5",
		TO_BE_UPDATED: "#ff8700",

		// RequirementCriticality
		MINOR: "#258123",
		MAJOR: "#ffd423",
		CRITICAL: "#FF0000",
		// UNDEFINED: ""

		// TestCaseExecutionMode
		AUTOMATED: "#258123",
		MANUAL: "#284bee",

		// TestCaseImportance
		VERY_HIGH: "#FF0000",
		HIGH: "#ffd423",
		MEDIUM: "#258123",
		LOW: "#284bee",

		// CampaignStatus / IterationStatus
		UNDEFINED: "#b5b5b5",
		PLANNED: "#6bbfbd",
		IN_PROGRESS: "#fabf00",
		FINISHED: "#6bbf00",
		ARCHIVED: "#536dec",

		//ExecutionStatus
		SETTLED: "#006700",
		UNTESTABLE: "#f4f4f4",
		BLOCKED: "#ffd423",
		FAILURE: "#FF0000",
		SUCCESS: "#258123",
		RUNNING: "#284bee",
		READY: "#bababb"
		// WARNING: "#536dec",
		// ERROR: "#536dec",
		// NOT_RUN: "#536dec",
		// NOT_FOUND: "#536dec"

	};

	function getAssociatedColors(legends) {
		var flatLegends = _.flatten(legends);
		var colors = [];

		try {
			for (var i = 0; i < flatLegends.length; i++) {
				colors.push(ColorsEnum[flatLegends[i]])
			}
		} catch (e) {
			// if a color is missing in the enum (or if there is any error), we reset the colors,
			// jqplot will use its standard colors
			colors = [];
		}

		return colors;
	}

	return {
		getAssociatedColors: getAssociatedColors
	};

});
