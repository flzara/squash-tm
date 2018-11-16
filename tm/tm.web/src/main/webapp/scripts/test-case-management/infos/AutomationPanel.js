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
define([ "jquery", "backbone", "underscore", "squash.translator", "squash.configmanager", "jeditable.simpleJEditable", "jquery.squash.jeditable"],
		function($, Backbone, _, translator, confman, SimpleJEditable) {

			var AutomationPanel = Backbone.View.extend({

				el : "#test-case-automation-panel",

				initialize : function(options) {
					var self = this;
					self.settings = options.settings;

					self.initAutomationRequestBlock();

					if(self.settings.writable){

						this.priorityEditable = new SimpleJEditable({
							targetUrl : this.settings.urls.automationRequestUrl,
							componentId : "automation-request-priority",
							jeditableSettings : {
								maxLength : 50,
							}
						});


						$('input[type=radio][name=test-case-automatable]').on('change', function() {

							$.ajax({
								url:  self.settings.urls.testCaseUrl,
								method: 'POST',
								data: {
									'id': 'test-case-automatable',
									'value': this.value
								}
							}).success(function() {
									self.initAutomationRequestBlock();
							});
						});
					}

				},

				initAutomationRequestBlock : function() {
					if ($('input[type=radio][name=test-case-automatable]:checked').val() === 'Y') {
						$('.test-case-automation-request-block').show();
					} else {
						$('.test-case-automation-request-block').hide();
					}
				},

				events : {

				}

			});
			return AutomationPanel;
});
