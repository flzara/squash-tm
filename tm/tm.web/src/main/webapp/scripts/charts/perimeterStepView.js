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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "workspace.projects", "./abstractStepView", "tree", "squash.translator", "./treePopup","../project-filter/ProjectSelectorPopup","../custom-report-workspace/utils", "jquery.squash.confirmdialog", "jquery.squash.buttonmenu"],
	function ($, backbone, _, Handlebars, projects, AbstractStepView, tree, translator, TreePopup,ProjectSelectorPopup,chartUtils) {
		"use strict";

		translator.load({
			msgdefault: 'wizard.perimeter.msg.default',
			msgcustomroot: 'wizard.perimeter.msg.custom.root',
			msgcustomsingle: 'wizard.perimeter.msg.custom.singleproject',
			msgcustommulti: 'wizard.perimeter.msg.custom.multiproject'
		});

		var perimeterStepView = AbstractStepView.extend({

			initialize: function (data, wizrouter) {
				this.tmpl = "#perimeter-step-tpl";
				this.model = data;
				data.name = "perimeter";
				this._initialize(data, wizrouter);
				$("#change-perimeter-button").buttonmenu();
				var treePopup = $("#tree-popup-tpl").html();
				this.treePopupTemplate = Handlebars.compile(treePopup);
				this.initPerimeter();
				this.initProjectPerimeterPopup();
				this.updateButtonStatus(this.model.get("scopeType"));


			},

			events: {
				"click .perimeter-select": "openPerimeterPopup",
				"click #repopen-perim": "reopenPerimeter",
				"click #repopen-projects-perim": "openProjectPerimeterPopup",
				"click #reset-perimeter": "resetPerimeter",
				"click #change-perimeter-project-button":"openProjectPerimeterPopup",
				"click .scope-type": "changeScopeType"

			},

			initPerimeter: function () {
				var scope = this.model.get("scopeEntity") || "DEFAULT";
				var scopeType = this.model.get("scopeType");

				switch (scopeType) {
					case "DEFAULT":
						this.writeDefaultPerimeter();
						break;

					case "PROJECTS":
						this.writeProjectPerimeter(scope);
						break;

					case "CUSTOM":
						this.writePerimeter(scope);
						break;

					default:
						break;
				}
			},

			initProjectPerimeterPopup : function() {
				var self = this;
				var projects = squashtm.workspace.projects;
				var isModifyMode = this.model.get('chartDef');
				var initialModel =  _.chain(projects)
					.map(function(project) {
						var checked = _.contains(self.model.get("projectsScope"),project.id);
						return {
							id: project.id,
							name: project.name,
							label: project.label,
							checked : isModifyMode ? checked : false
						};
					})
					.sortBy("name")
					.value();

				this.projectPopup = new ProjectSelectorPopup({
					el:"#project-perimeter-popup",
					frontTemplating : true,
					templateSelector : "#project-popup-tpl",
					initialProjectModel : initialModel,
					preventServerFilterUpdate : true
				});

				//setting the callback after confirm in project popup
				this.listenTo(this.projectPopup, 'projectPopup.confirm', function() {
					var selectedIds = self.projectPopup.model.get("projectIds");
					self.model.set("scope", _.map(selectedIds,function(id){
						return {id:id,type:"PROJECT"};
					}));
					self.model.set("projectsScope", _.map(selectedIds,function(id){
						return parseInt(id);
					}));
					self.writeProjectPerimeter();
				 });
			},

			changeScopeType : function () {
				var scopeType = this.$el.find("input[name='scope-type']:checked").val();
				this.model.set({
					scope : [],
					projectsScope : [],
					scopeEntity: "default"
				});
				this.model.set("scopeType",scopeType);
				this.updateButtonStatus(scopeType);
				this.initPerimeter();

			},

			updateButtonStatus : function (scopeType) {
				switch (scopeType) {
					case "DEFAULT":
						this.inactivateChooseProjectPerimeter();
						this.inactivateChooseCustomPerimeter();
						break;
					case "PROJECTS":
						this.activateChooseProjectPerimeter();
						this.inactivateChooseCustomPerimeter();
						break;
					case "CUSTOM":
						this.inactivateChooseProjectPerimeter();
						this.activateChooseCustomPerimeter();
						break;
					default:
						break;
				}
			},

			inactivateChooseProjectPerimeter : function () {
				this.$el.find("#change-perimeter-project-button").addClass("disabled");
			},

			inactivateChooseCustomPerimeter : function () {
				this.$el.find("#change-perimeter-button").addClass("disabled");
			},

			activateChooseProjectPerimeter : function () {
				this.$el.find("#change-perimeter-project-button").removeClass("disabled");
			},

			activateChooseCustomPerimeter : function () {
				this.$el.find("#change-perimeter-button").removeClass("disabled");
			},

			writeDefaultPerimeter: function () {

				var defaultId = this.model.get("defaultProject");
				var projectName = projects.findProject(defaultId).name;

				var mainmsg = translator.get("wizard.perimeter.msg.default");
				var perimmsg = " " + translator.get('label.project').toLowerCase() + " " + projectName;
				$("#selected-perim-msg").text(mainmsg);
				$("#selected-perim").text(perimmsg);

				this.model.set({scope: [{type: "PROJECT", id: defaultId}]});
				this.model.set({projectsScope: [defaultId]});
				this.model.set({scopeEntity: "default"});
			},

			writePerimeter: function (name) {

				var rootmsg = translator.get('wizard.perimeter.msg.custom.root');
				var entitynames;
				if (name && name !== "default") {
					entitynames = translator.get("wizard.perimeter." + name);
				}

				var projScope = this.model.get('projectsScope'),
					suffixmsg = null;

				var scope = this.model.get('scope');

				if (projScope.length === 1 && name!=="default") {
					var projectId = projects.findProject(projScope[0]).name;
					suffixmsg = translator.get('wizard.perimeter.msg.custom.singleproject', entitynames, projectId);
				} else {
					suffixmsg = translator.get('wizard.perimeter.msg.custom.multiproject', entitynames);
				}

				$("#selected-perim-msg").text(rootmsg);

				if (name==="default" || scope.length === 0) {
					var textHint = translator.get('wizard.perimeter.msg.perimeter.choose');
					$("#selected-perim").text(textHint);
				} else {
					var link = "<a id='repopen-perim' style='cursor:pointer' name= '" + name + "'>" + suffixmsg + "</a>";
					$("#selected-perim").html(link);
				}


			},

			writeProjectPerimeter: function() {
				var rootmsg = translator.get('wizard.perimeter.msg.projects.root');
				var projScope = this.model.get('projectsScope');
				$("#selected-perim-msg").text(rootmsg);

				if (projScope.length === 0) {
					var textHint = translator.get('wizard.perimeter.msg.perimeter.choose');
					$("#selected-perim").text(textHint);
				} else {
					var projectNames = projects.getProjectsNames(projScope);
					var link = "";
					_.each(projectNames,function(name) {
						link=link.concat(name+", ");
					});
					//removing the last comma
					link = link.slice(0, -2);
					//append html
					link = "<a id='repopen-projects-perim' style='cursor:pointer' >" + link + "</a>";
					$("#selected-perim").html(link);
				}
			},

			resetPerimeter: function () {
				this.writeDefaultPerimeter();
			},

			reopenPerimeter: function (event) {
				var self = this;

				var nodes = _.map(this.model.get("scope"), function (obj) {
					return {
						restype: obj.type.split("_").join("-").toLowerCase() + "s", //yeah that quite fucked up...change back the _ to -, lower case and add a "s"
						resid: obj.id
					};
				});

				var treePopup = new TreePopup({
					model: self.model,
					name: event.target.name,
					nodes: nodes

				});
				self.addTreePopupConfirmEvent(treePopup, self, event.target.name);

			},
			openPerimeterPopup: function (event) {

				var self = this;

				var treePopup = new TreePopup({
					model: self.model,
					name: event.target.name,
					nodes: []
				});

				self.addTreePopupConfirmEvent(treePopup, self, event.target.name);


			},

			openProjectPerimeterPopup : function(){
				 this.projectPopup.open();
			},

			addTreePopupConfirmEvent: function (popup, self, name) {

				popup.on('treePopup.confirm', function () {

					var scope = _.map($("#tree").jstree('get_selected'), function (sel) {
						return {
							type: $(sel).attr("restype").split("-").join("_").slice(0, -1).toUpperCase(),
							id: $(sel).attr("resid")
						};
					});
					self.model.set({scope: scope});
					self.model.set({
						projectsScope: _.uniq(_.map($("#tree").jstree('get_selected'), function (obj) {
							return parseInt($(obj).closest("[project]").attr("project"));
						}))
					});
					self.writePerimeter(name);
					self.model.set({scopeEntity: name});
					self.removeInfoListFilter();
				});

			},


			removeInfoListFilter: function () {
				this.model.set({
					filters: _.chain(this.model.get("filters"))
						.filter(function (val) {
							return val.column.dataType != "INFO_LIST_ITEM";
						})
						.value()
				});
			},

			updateModel: function () {
				//here we must invalidate cuf selected attributes if the perimeter change.
				//we also must clean filter and axis if some cufs are now invalids

				//1. first we recompile all computedColumnPrototypes for the new perimeter
				this.model.set("computedColumnsPrototypes",this.computeColumnsPrototypes());

				//2. exctracting all valid column prototype ids
				var validsIds = _.chain(this.model.get("computedColumnsPrototypes"))
									.values()
									.flatten()
									.pluck("id")
									.value();

				//3. now it's simple. If the column is always in computedColumnPrototypes it's valid, else it's invalid...
				this.checkValidColumnPrototypeInstance(["axis","filters","measures"],validsIds);

				//4. For the selectedAttributes and selectedCufAttribute it's a little more tricky, as all the ids are strings
				this.checkValidColumnPrototypeId(["selectedAttributes","selectedCufAttributes"],validsIds);

				//5. Recompute the selectedEntities attributes after invalids columns ids have been filtered out
				this.recomputeSelectedEntities();
			},

			checkValidColumnPrototypeInstance : function(instanceTypes,validsIds) {
				var self = this;
				_.each(instanceTypes, function(instanceType) {
					var instances = self.model.get(instanceType);
					var filtered = _.filter(instances,function(instance) {
						return _.contains(validsIds,instance.column.id);
					});
					self.model.set(instanceType,filtered);
				});
			},

			checkValidColumnPrototypeId : function(listIds,validsIds) {
				var validsStringIds = _.map(validsIds,function(id) {
					return "" + id;//yeah toString() in javascript :)
				});
				var self = this;
				_.each(listIds, function(listId) {
					var list = self.model.get(listId);
					var filtered = _.filter(list,function(id) {
						return _.contains(validsStringIds,id);
					});
					self.model.set(listId,filtered);
				});
			},

			recomputeSelectedEntities : function() {
				//get a map entityType/valids ids like {TEST_CASE:[1,2,101-4....]}
				var selectedEntities = [];
				var selectedAttributes = this.model.get("selectedAttributes");
				var protosIdsByEntityType = _.mapObject(this.model.get("computedColumnsPrototypes"),function(values, key) {
					return _.chain(values)
								.pluck("id")
								.map(function(id) {return "" + id;})
								.value();
					});

				_.each(protosIdsByEntityType,function(protos,entityType) {
					var inter = _.intersection(protos,selectedAttributes);
					if(inter.length > 0 ){
						selectedEntities.push(entityType);
					}
				});

			}


		});

		return perimeterStepView;

	});
