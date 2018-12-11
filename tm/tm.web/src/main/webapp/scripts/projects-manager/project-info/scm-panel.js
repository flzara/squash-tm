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
define(["jquery", "backbone", "jeditable.selectJEditable", "workspace.routing", "squash.translator", "jquery.squash.formdialog"],
		function($, Backbone, SelectJEditable, routing, translator) {

		var ScmPanel = Backbone.View.extend({

			el: '#scm-panel',

			repoFieldset: $('#scm-repositories-fieldset'),

			initialize: function(projectId, boundServerId, boundRepositoryId, availableScmServers) {
				var self = this;
				self.projectId = projectId;
				self.currentServer = boundServerId;
				self.currentRepository = boundRepositoryId;

				self.serverCombo = self.initServerComboBox(availableScmServers);
				self.serverCombo.setValue("" + self.currentServer);
				// If a repository is bound, initRepositoryCombo
				if(boundServerId !== 0) {
					self.repositoryCombo = self.initRepositoryComboBox(boundServerId);
				}
			},
			/*
			* Initialize the ComboBox of the Servers with the given collection.
			* @param scmServers: The Collection of ScmServers.
			*/
			initServerComboBox: function(scmServers) {
				var self = this;
				var formattedServers = self.formatScmServers(scmServers);
				return new SelectJEditable({
					componentId: 'selected-scm-server',
					jeditableSettings: {
						data: formattedServers
					},
					target: function(value) {
						// Value changed ?
						if(self.currentServer === value) {
							// No -> Do Nothing
						} else {
							// Yes ->
							if(value ==='0') {
								// Is it 0 ? -> Unbind
								self.repoFieldset.hide();
								self.doUnbindRepositoryToProject().error(function(xhr, error) {
									console.log(error);
								});
								self.reforgeRepositoryComboBox();
							} else {
							// Otherwise -> Load Repositories
								self.initRepositoryComboBox(value);
							}
							self.currentServer = value;
						}
						return value;
					}
				});
			},
			/*
			* Format the ScmServers Collection to fill the SelectJEditable.
			* @param scmServers: The ScmServers Collection.
			*	@return formattedServers: {
			*		serverId1: serverName1,
			*		serverId2: serverName2,
			*		...
			* }
			*/
			formatScmServers: function(scmServers) {
				var result = {
					'0' : translator.get('label.NoServer'),
					'selected': '0'
				};
				for(var n in scmServers) {
					var server = scmServers[n];
					result[server.id] = server.name;
				}
				return result;
			},
			/*
			* Initialize the ComboBox of the Repositories corresponding to the given ScmServer.
			* @param serverId: The id of the server which Repositories are to load.
			*/
			initRepositoryComboBox: function(serverId) {
				var self = this;
				self.doLoadRepositories(serverId).success(function(scmRepositories) {
						self.reforgeRepositoryComboBox();
						var formattedScmRepositories = self.formatScmRepos(scmRepositories);
						self.repositoryCombo = new SelectJEditable({
							componentId: 'selected-scm-repository',
							jeditableSettings: {
								data: formattedScmRepositories
							},
							target: function(value) {
								// Value changed ?
								if(self.currentRepository === value) {
									// No -> Do Nothing
								} else {
									// Yes ->
									// Is it 0 ? -->
									if(value === '0') {
										// Unbind Repository
										self.doUnbindRepositoryToProject().error(function(xhr, error) {
											console.log(error);
										});
									} else {
										// Otherwise -> Bind repository
										self.doBindRepositoryToProject(value).error(function(xhr, error) {
											console.log(error);
										});
									}
									self.currentRepository = value;
								}
								return value;
							}
						});
						self.repositoryCombo.setValue("" + self.currentRepository);
						self.repoFieldset.show();
				});
			},
			/*
			* Format the ScmRepositories Collection to fill the SelectJEditable.
			*/
			formatScmRepos: function(scmRepositories) {
				var result = {
					'0' : translator.get('label.none'),
					'selected': '0'
				};
				for(var n in scmRepositories) {
					var repo = scmRepositories[n];
					result[repo.id] = repo.name;
				}
				return result;
			},
			/*
			* Create the GET Request to retrieve all the ScmRepositories contained in the given ScmServer.
			* @param scmServerId: The id of the ScmServer which Repositories are to load.
			* @return The Promise of the GET Request.
			*/
			doLoadRepositories: function(scmServerId) {
				return $.ajax({
					method: 'GET',
					url: routing.buildURL('administration.scm-repositories.list'),
					data: {
						scmServerId: scmServerId
					}
				});
			},
			/*
			* Reforge the Repository ComboBox. This is the only solution found to reset the values of Select.
			*/
			reforgeRepositoryComboBox: function() {
				var self = this;
				self.repositoryComboBox = null;
				$('#selected-scm-repository').remove();
				var newDiv = $("<div id='selected-scm-repository'>" + translator.get('label.None') + "</div>");
				$('#scm-repositories-fieldset').append(newDiv);
			},
			/*
			* Create the POST Request to bind a ScmRepository to the current Project.
			* @param repositoryId: The id of the ScmRepository to bind to the Project.
			* @return The Promise of the POST Request.
			*/
			doBindRepositoryToProject: function(repositoryId) {
				var projectId = this.projectId;
				return $.ajax({
					method: 'POST',
					url: routing.buildURL('generic-projects.scm-repository', projectId),
					data: {
						scmRepositoryId: repositoryId
					}
				});
			},
			/*
			* Create the DELETE Request to unbind a ScmRepository from the current Project.
			* @return The Promise of the DELETE Request.
			*/
			doUnbindRepositoryToProject : function() {
				var projectId = this.projectId;
				return $.ajax({
					method: 'DELETE',
					url: routing.buildURL('generic-projects.scm-repository', projectId)
				});
			},

		});

		return ScmPanel;
});
