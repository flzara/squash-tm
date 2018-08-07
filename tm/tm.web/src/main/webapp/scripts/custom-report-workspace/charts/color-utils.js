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
		WORK_IN_PROGRESS: "#E9C63E",
		UNDER_REVIEW: "#5BBAA8",
		APPROVED: "#599949",
		OBSOLETE: "#E0E0E0",
		TO_BE_UPDATED: "#F38D2D",

		// RequirementCriticality
		MINOR: "#F9D667",
		MAJOR: "#E66400",
		CRITICAL: "#CC2C00",
		UNDEFINED: "#E0E0E0",

		// TestCaseExecutionMode
		AUTOMATED: "#258123",
		MANUAL: "#284BEE",

		// TestCaseImportance
		VERY_HIGH: "#CC2C00",
		HIGH: "#E66400",
		MEDIUM: "#FEA412",
		LOW: "#F9D667",

		// CampaignStatus / IterationStatus
		// UNDEFINED: "#c6c6c6",
		PLANNED: "#E9C63E",
		IN_PROGRESS: "#5BBAA8",
		FINISHED: "#599949",
		ARCHIVED: "#F38D3D",

		//ExecutionStatus
		SETTLED: "#2F4233",
		UNTESTABLE: "#F4F4DE",
		BLOCKED: "#F2C830",
		FAILURE: "#B22C2C",
		SUCCESS: "#2D7243",
		RUNNING: "#4288CE",
		READY: "#BAB4B9"
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
