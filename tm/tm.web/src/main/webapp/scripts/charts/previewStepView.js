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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "./abstractStepView", "workspace.routing", "custom-report-workspace/charts/chartFactory",'is','jquery.cookie'],
	function($, backbone, _, Handlebars, AbstractStepView, router, chart, is ) {
	"use strict";

	var previewStepView = AbstractStepView.extend({

		initialize : function(data, wizrouter) {
			this.tmpl = "#preview-step-tpl";
			this.model = data;
			data.name = "preview";
			this._initialize(data, wizrouter);
			this.initChart();
			this.initName();

		},


		initName : function (){
			//var name = this.model.get('name') || "graph" ;
			if (this.isModify()) {
        $("#chart-name").val(this.model.get("name"));
			} else {
        $("#chart-name").val("");
        this.updateModel();
			}
		},
		initChart : function (){
			var data = this.model.get("chartData");
			chart.buildChart("#chart-display-area", data);

		},
		save : function () {
			var parentId = this.model.get("parentId");
			this.updateModel();

			var targetUrl;
			var path = this.getCookiePath();

			if (this.isModify()){
				targetUrl = router.buildURL("chart.update", parentId);
			} else {
				targetUrl = router.buildURL("chart.new", parentId);
			}

			$.ajax({
				method : "POST",
				contentType: "application/json",
				url : targetUrl,
				data : this.model.toJson()

			}).done(function(id){
				var nodeToSelect = "CustomReportChart-" + id;
				$.cookie("jstree_select",nodeToSelect,{path:path});
						window.location.href = router.buildURL("custom-report-chart-redirect",id);
			});


		},


		updateModel : function() {
		    var name = $("#chart-name").val();
		    this.model.set("name",name );
		},
		
		/**
		 * IE and FF add a trailing / to cookies...
		 * Chrome don't...
		 * So we need to put the good path to avoid two jstree_select cookies with differents path.
		 */
		getCookiePath : function () {
			var path = "/squash/custom-report-workspace";
			if(is.ie()||is.firefox()){
				path = path + "/";
			}
			return path;
		}

	});

	return previewStepView;

});
