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
define([ "jquery","underscore", "backbone","handlebars", "./ProjectsTable", "./NewProjectFromTemplateDialog","./NewTemplateDialog","./NewTemplateFromProjectDialog","workspace.routing", "jqueryui","jquery.squash", "jquery.squash.buttonmenu","jquery.squash.formdialog" ],
		function($,_, Backbone, Handlebars, ProjectsTable, NewProjectFromTemplateDialog, NewTemplateDialog, NewTemplateFromProjectDialog, router) {
		"use strict";

			var View = Backbone.View.extend({
				el : ".fragment-body",

				initialize : function() {
					this.projectsTable = new ProjectsTable();
					this.templates = new Backbone.Collection([], {
						comparator : function(template) {
							return template.get("name");
						}
					});
					this.templates.url = router.buildURL("template");
					this.$("#add-template-button").buttonmenu();
					_.bindAll(this, 'getProjectDescription','showNewTemplateFromProjectDialog');
				},

				events : {
					"click #new-project-button" : "showNewProjectFromTemplateDialog",
					"click #new-template-button" : "showNewTemplateDialog",
					"click #new-template-from-project-button" : "getProjectDescription",
					"click #projects-table tr": "updateNewTemplateFromProjectButton"
				},


				showNewTemplateDialog : function(event) {
					this.newTemplateDialog = new NewTemplateDialog();
					this.listenTo(this.newTemplateDialog, "newtemplate.confirm", this.projectsTable.refresh);
				},

				showNewProjectFromTemplateDialog : function() {
					this.newProjectFromTemplateDialog = new NewProjectFromTemplateDialog({
						collection : this.templates
					});
					this.listenTo(this.newProjectFromTemplateDialog, "newproject.confirm", this.projectsTable.refresh);
				},

				showNewTemplateFromProjectDialog : function(response) {
					var projectTable = this.$("#projects-table").squashTable();
					var idSelected = projectTable.getSelectedIds()[0];
					var projectName = projectTable.getDataById(idSelected).name;
					var projectLabel = projectTable.getDataById(idSelected).label;

					var CustomModel = Backbone.Model.extend({
						defaults : function () {
							return {
								templateId : idSelected,
								label : projectLabel,
								description : response,
								name : "",
								copyPermissions :true,
								copyCUF:true,
								copyBugtrackerBinding: true,
								copyAutomatedProjects: true,
								copyInfolists:true,
								copyMilestone:true,
								copyAllowTcModifFromExec:true
							};
						},
						url : function () {
							return router.buildURL("template.new");
						},
						originalProjectName : projectName
					});

					var newModel = new CustomModel();

					this.newTemplateFromProjectDialog = new NewTemplateFromProjectDialog({
						model : newModel
					});
					this.listenTo(this.newTemplateFromProjectDialog, "newtemplate.confirm", this.projectsTable.refresh);
				},

				updateNewTemplateFromProjectButton : function() {
					if (this.$("#projects-table").squashTable().getSelectedIds().length == 1) {
						this.$("#new-template-from-project-button").removeClass("disabled ui-state-disabled");
					}
					else{
						this.$("#new-template-from-project-button").addClass("disabled ui-state-disabled");
					}
				},
				/*
				This method send an ajax request to get the project description not included in datatable
				but needed in NewTemplateFromProjectDialog
				*/
				getProjectDescription :  function (){
					var urlCtrl = router.buildURL("generic.project.description",this.$("#projects-table").squashTable().getSelectedIds()[0]);
					var self = this;

					$.ajax({
						url : urlCtrl,
            dataType :"html",
						success : function (response) {
							self.showNewTemplateFromProjectDialog(response);
						}
					});
				}
			});

			return View;
		});
