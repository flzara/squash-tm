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

	var ColoursEnum = {
		// RequirementStatus / TestCaseStatus
		WORK_IN_PROGRESS: "#C9E8AA",
		UNDER_REVIEW: "#A3D86E",
		APPROVED: "#56AD25",
		OBSOLETE: "#D9D9D9",
		TO_BE_UPDATED: "#FFFF00",

		// RequirementCriticality
		MINOR: "#FBD329",
		MAJOR: "#FDA627",
		CRITICAL: "#FD7927",
		UNDEFINED: "#FCEDB6",

		// TestCaseExecutionMode
		AUTOMATED: "#258123",
		MANUAL: "#284BEE",

		// TestCaseImportance
		VERY_HIGH: "#FD7927",
		HIGH: "#FDA627",
		MEDIUM: "#FBD329",
		LOW: "#FCEDB6",

		// CampaignStatus / IterationStatus
		// UNDEFINED: "#E0E0E0",
		PLANNED: "#E9C63E",
		IN_PROGRESS: "#5BBAA8",
		FINISHED: "#599949",
		ARCHIVED: "#F38D3D",

		//ExecutionStatus
		SETTLED: "#99FF99",
		UNTESTABLE: "#969696",
		BLOCKED: "#FFCC00",
		FAILURE: "#FF3300",
		SUCCESS: "#99CC00",
		RUNNING: "#6699FF",
		READY: "#BDD3FF"
		// WARNING: "#536dec",
		// ERROR: "#536dec",
		// NOT_RUN: "#536dec",
		// NOT_FOUND: "#536dec"

	};

	var colourFiller = [
		"#f44336",
		"#e91e63",
		"#9c27b0",
		"#673ab7",
		"#3f51b5",
		"#2196f3",
		"#03a9f4",
		"#00bcd4",
		"#009688",
		"#4caf50",
		"#8bc34a",
		"#cddc39",
		"#ffeb3b",
		"#ffc107",
		"#ff9800",
		"#ff5722",
		"#ba000d",
		"#b0003a",
		"#6a0080",
		"#320b86",
		"#002984",
		"#0069c0",
		"#007ac1",
		"#008ba3",
		"#00675b",
		"#087f23",
		"#5a9216",
		"#99aa00",
		"#c8b900",
		"#c79100",
		"#c66900",
		"#c41c00"
	];

	function getAssociatedColours(legends) {
		var flatLegends = _.flatten(legends);
		var colours = [];

		try {
			for (var i = 0; i < flatLegends.length; i++) {
				colours.push(ColoursEnum[flatLegends[i]])
			}
		} catch (e) {
			// if a color is missing in the enum (or if there is any error), we reset the colors,
			// jqplot will use its standard colors
			colours = [];
		}

		return colours;
	}

	function completeColourArray(colourArray) {
		var newColour,
			emptyElementIndex,
			emptyElementNumber = 0;

		do {
			emptyElementIndex = colourArray.findIndex(function (element) {
				return !element;
			});
			newColour = colourFiller[emptyElementNumber % colourFiller.length];
			colourArray[emptyElementIndex] = newColour;
			emptyElementNumber++;
		} while (_.contains(colourArray, "") || _.contains(colourArray, null));
		return colourArray;
	}

	return {
		getAssociatedColours: getAssociatedColours,
		completeColourArray: completeColourArray
	};

});
