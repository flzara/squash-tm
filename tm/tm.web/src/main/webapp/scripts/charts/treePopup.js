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
define(["jquery", "backbone", "underscore", "handlebars", "tree", "squash.translator", "jquery.squash.confirmdialog"],
	function ($, Backbone, _, Handlebars, tree, translator) {
		"use strict";

	

		var View = Backbone.View.extend({
			el: "#tree-dialog",

			initialize: function (model) {
		
				this.model = model.model;
				this.nodes = model.nodes;
				this.render();
				
				$("#tree").on('reselect.jstree', function(event, data) {
					data.inst.findNodes(model.nodes).select();
				});
				
				this.initTree(model.name);
				
				this.$el.confirmDialog({
					autoOpen: true,
					height: 800,
					//issue 6503 hiding the close cross that is completly bugged. And if i use comfirmdialogclose event, it
					//prevent the confirm event to be fired. ConfirmDialog was a bad choice, FormDialog would have been better for this complex popup... 
					open: function(event, ui) { $("#tree-dialog").prev().children(".ui-dialog-titlebar-close").hide(); }
				});
		
				this.checkFilterChangeImpact();
			
			},
			
			checkFilterChangeImpact : function (){			
				if (this.hasInfoListFilter()){					
					$("#warning-info-list").text(translator.get("wizard.perimeter.warning.info-list-filter"));		
				}	
			},
			
			hasInfoListFilter : function (){				
				return ! _.chain(this.model.get("filters"))
				.filter(function(val) {return val.column.dataType == "INFO_LIST_ITEM";})
				.isEmpty()
				.value();	
			},
			
			render: function () {
				var treePopup = $("#tree-popup-tpl").html();
				this.treePopupTemplate = Handlebars.compile(treePopup);
				this.$el.append(this.treePopupTemplate());
				return this;
			},

			events: {
				"confirmdialogcancel": "cancel",
				"confirmdialogconfirm": "confirm",
				"confirmdialogvalidate" : "validate"
			},

			cancel: function (event) {
				this.remove();

			},
			
			validate : function(event){
				
				var nbSelect = $("#tree").jstree('get_selected').size();
				if (nbSelect === 0){
					var title = translator.get('wizard.perimeter.select.title');
					var msg = translator.get('wizard.perimeter.select.msg');				
					$.squash.openMessage(title, msg); 
					return false;
				}
				return true;
			},

			confirm: function (event) {
				var self = this;

				self.trigger("treePopup.confirm");
				this.remove();

			},
			
			

			initTree : function (name){
				
				
				var workspaceName = name.split("_").join("-").toLowerCase();

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

			},
			

			remove: function () {
				Backbone.View.prototype.remove.apply(this, arguments);
				$("#tree-dialog-container").html('<div id="tree-dialog" style="height: 800px!important" class="not-displayed popup-dialog search-minimal-height" title="' + translator.get('report.form.tree-picker.dialog.title') + '" />');
			}
		});

		return View;
	});
