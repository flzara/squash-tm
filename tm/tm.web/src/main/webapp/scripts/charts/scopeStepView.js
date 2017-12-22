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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "./abstractStepView", "tree", "jquery.squash.confirmdialog"],
	function($, backbone, _, Handlebars, AbstractStepView, tree) {
	"use strict";

	var scopeStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#scope-step-tpl";
			this.model = data;
			data.name = "scope";
			this._initialize(data, wizrouter);
			
			var treePopup = $("#tree-popup-tpl").html();
			this.treePopupTemplate = Handlebars.compile(treePopup);

		},
		
		events : {
			"click .perimeter-select" : "openPerimeterPopup"
			
		},
		
		openPerimeterPopup : function (event) {
		
			
			$("#tree-popup-container").html(this.treePopupTemplate());
			
			$("#tree-dialog").confirmDialog();
			$("#tree-dialog").confirmDialog('open');
			this.initTree(event.target.id);
		},
		
		updateModel : function() {
			
			var scope = _.map($("#tree").jstree('get_selected'), function (sel) { return {type : $(sel).attr("restype").split("-").join("_").slice(0,-1).toUpperCase(), id:$(sel).attr("resid")}; } );
			
			this.model.set({scope : scope});
			this.model.set({projectsScope : _.uniq(_.map($("#tree").jstree('get_selected'), function(obj){return $(obj.closest("[project]")).attr("project"); }))});
		},
		
		
		initTree : function (workspaceName){
		

			var ids = _.pluck(this.model.get("scope"), "id");
			ids = ids.length > 0 ? ids : 0;
			
			
			
			$.ajax({
				url : squashtm.app.contextRoot + "/" + workspaceName + '-workspace/tree/' + ids,
				datatype : 'json' 
				
				
			}).done(function(model){
				
				var treeConfig = {
						model : model,
						treeselector: "#tree",
						workspace: workspaceName,	
						canSelectProject:true
				};
				tree.initLinkableTree(treeConfig);
					
			});

		}
	});

	return scopeStepView;

});