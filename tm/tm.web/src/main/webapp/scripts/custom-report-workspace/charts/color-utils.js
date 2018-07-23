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
		CRITICAL: "#d32424",
		// UNDEFINED: ""

		// TestCaseExecutionMode
		AUTOMATED: "#258123",
		MANUAL: "#284bee",

		// TestCaseImportance
		VERY_HIGH: "#d32424",
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
		FAILURE: "#d32424",
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
