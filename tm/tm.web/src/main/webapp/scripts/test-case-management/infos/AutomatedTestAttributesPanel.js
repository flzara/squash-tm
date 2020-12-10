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
define([ "jquery", "backbone", "underscore", "workspace.event-bus", "squash.translator", "squash.configmanager", "jeditable.simpleJEditable", "jeditable.selectJEditable", "app/ws/squashtm.notification", "app/util/StringUtil", "jquery.squash.jeditable"],
		function($, Backbone, _, eventBus, translator, confman, SimpleJEditable, SelectJEditable, notification, StringUtils ) {

			var AutomatedTestAttributesPanel = Backbone.View.extend({

				el : "#test-case-autom-attributes-panel",

				initialize : function(options) {
					var self = this;
					self.settings = options.settings;

					if (self.settings.writable) {
						this.sourceCodeRepositoryUrlEditable = new SimpleJEditable({
							targetUrl :this.settings.urls.testCaseUrl,
							componentId : "test-case-source-code-repository-url",
							jeditableSettings : {
								maxlength : 255
							}
						});

						this.sourceCodeRepositoryInput = $('#test-case-source-code-repository-url');

						this.sourceCodeRepositoryInput.on('keyup', function (event) {
							// not perform autocomplete if arrows are pressed
							if (!_.contains([37, 38, 39, 40], event.which)) {
								var searchInput = $(event.currentTarget).find('input');
								searchInput.autocomplete();
								self.performAutocomplete(searchInput);
							}
						});

						this.automatedTestReference = new SimpleJEditable({
							targetUrl :this.settings.urls.testCaseUrl,
							componentId : "test-case-automated-test-reference",
							jeditableSettings : {
								maxlength : 255
							}
						});

						this.automatedTestTechnologyEditable = new SelectJEditable({
							target : this.settings.urls.testCaseUrl,
							componentId : "test-case-automated-test-technology",
							jeditableSettings : {
								data : confman.toJeditableSelectFormat(this.settings.automatedTestTechnologies)
							}
						});
					}
				},

				performAutocomplete: function (searchInput) {
					searchInput.autocomplete('close');
					searchInput.autocomplete('disable');

					var searchInputValue = searchInput.val();

					searchInput.autocomplete({
						delay : 500,
						source: function(request, response) {
							$.ajax({
								type: 'GET',
								url: squashtm.app.contextRoot + 'scm-repositories/autocomplete',
								data: {
									searchInput: searchInputValue
								},
								success: function(data) {
									response(data);
								}
							});
						},
						minLength: 1
					});
					searchInput.autocomplete('enable');
				}

			});
			return AutomatedTestAttributesPanel;
});
