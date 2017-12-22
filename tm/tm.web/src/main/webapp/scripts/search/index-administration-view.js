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
define([ 'jquery', 'backbone', 'underscore', 'app/util/ButtonUtil', 'jquery.squash.confirmdialog' ], function($,
		Backbone, _, ButtonUtil) {

	var IndexAdministrationView = Backbone.View.extend({

		el : "#index-administration-content",

		initialize : function() {
			this.confirmIndexAll = $.proxy(this._confirmIndexAll, this);
			this.configurePopups.call(this);
			ButtonUtil.disable($("#refresh-index-button"));
		},

		events : {
			"click #index-all-button" : "confirmIndexAll",
			"click #requirement-index-button" : "indexRequirements",
			"click #testcase-index-button" : "indexTestcases",
			"click #campaign-index-button" : "indexCampaigns",
			"click #refresh-index-button" : "refreshPage"
		},

		configurePopups : function() {
			this.confirmIndexAllDialog = $("#confirm-index-all-dialog").confirmDialog();
			this.confirmIndexAllDialog.on("confirmdialogconfirm", $.proxy(this.indexAll, this));
		},

		_confirmIndexAll : function() {
			this.confirmIndexAllDialog.confirmDialog("open");
		},

		indexAll : function() {
			ButtonUtil.disable($("#index-all-button"));
			ButtonUtil.disable($("#requirement-index-button"));
			ButtonUtil.disable($("#testcase-index-button"));
			ButtonUtil.disable($("#campaign-index-button"));
			ButtonUtil.enable($("#refresh-index-button"));

			$("#should-reindex-message").addClass("not-displayed");
			$("#monitor-percentage").removeClass("not-displayed");
			$("#monitor-message").removeClass("not-displayed");

			$.ajax({
				type : "POST",
				url : squashtm.app.contextRoot + "advanced-search/index-all",
				data : "nodata"
			});
		},

		indexRequirements : function() {
			ButtonUtil.disable($("#index-all-button"));
			ButtonUtil.disable($("#requirement-index-button"));
			ButtonUtil.enable($("#refresh-index-button"));

			$("#requirement-monitor-percentage").removeClass("not-displayed");
			$("#requirement-monitor-message").removeClass("not-displayed");

			$.ajax({
				type : "POST",
				url : squashtm.app.contextRoot + "advanced-search/index-requirements",
				data : "nodata"
			});
		},

		indexTestcases : function() {

			ButtonUtil.disable($("#index-all-button"));
			ButtonUtil.disable($("#testcase-index-button"));
			ButtonUtil.enable($("#refresh-index-button"));

			$("#testcase-monitor-percentage").removeClass("not-displayed");
			$("#testcase-monitor-message").removeClass("not-displayed");

			$.ajax({
				type : "POST",
				url : squashtm.app.contextRoot + "advanced-search/index-testcases",
				data : "nodata"
			});
		},

		indexCampaigns : function() {
			ButtonUtil.disable($("#index-all-button"));
			ButtonUtil.disable($("#campaign-index-button"));
			ButtonUtil.enable($("#refresh-index-button"));

			$("#campaign-monitor-percentage").removeClass("not-displayed");
			$("#campaign-monitor-message").removeClass("not-displayed");

			$.ajax({
				type : "POST",
				url : squashtm.app.contextRoot + "advanced-search/index-campaigns",
				data : "nodata"
			});
		},



		refreshPage : function() {
			$.ajax({
				type : "POST",
				url : squashtm.app.contextRoot + "advanced-search/refresh",
				data : "nodata"
			}).success(
					function(json) {
						$("#monitor-percentage").html(
							addCommas(json.writtenEntities) + " / " + addCommas(json.totalEntities) + " (" + json.progressPercentage +
										"%) ");

						$("#requirement-monitor-percentage").html(
							addCommas(json.writtenEntitiesForRequirementVersions) + " / " +
							addCommas(json.totalEntitiesForRequirementVersions) + " (" +
							addCommas(json.totalEntitiesForRequirementVersions) + " (" +
							json.progressPercentageForRequirementVersions + "%) ");
						$("#testcase-monitor-percentage").html(
							addCommas(json.writtenEntitiesForTestcases) + " / " + addCommas(json.totalEntitiesForTestcases) + " (" +
							json.progressPercentageForTestcases + "%) ");
						$("#campaign-monitor-percentage").html(
							addCommas(json.writtenEntitiesForCampaigns) + " / " +
							addCommas(json.totalEntitiesForCampaigns) + " (" +
							json.progressPercentageForCampaigns + "%) ");
					});
		}
	});

	return IndexAdministrationView;
	function addCommas(nStr){
		nStr += '';
		x = nStr.split('.');
		x1 = x[0];
		x2 = x.length > 1 ? '.' + x[1] : '';
		var rgx = /(\d+)(\d{3})/;
		while (rgx.test(x1)) {
			x1 = x1.replace(rgx, '$1' + ' ' + '$2');
		}
		return x1 + x2;
	}
});
