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
define([ "jquery", "backbone", "underscore", "workspace.event-bus", "squash.translator", "squash.configmanager", "jeditable.simpleJEditable", "jeditable.selectJEditable", "app/ws/squashtm.notification", "jquery.squash.jeditable"],
		function($, Backbone, _, eventBus, translator, confman, SimpleJEditable, SelectJEditable, notification) {

			var AutomationPanel = Backbone.View.extend({

				el : "#test-case-automation-panel",

				initialize : function(options) {
					var self = this;
					this.updateAutomatableInTree = $.proxy(this._updateAutomatableInTree, this);
					self.settings = options.settings;

					self.initAutomationRequestBlock();
					var automatableRadio = $("input[type=radio][name=test-case-automatable]");

					if (self.settings.writable) {
						this.priorityEditable = new SimpleJEditable({
							targetUrl : this.settings.urls.testCaseUrl,
							componentId : "automation-request-priority",
							jeditableSettings : {
								'maxlength' : 9,
								'onerror' : function(settings, self, xhr){
                						notification.showError(xhr);
                						self.reset();
                					}
							}
						});

						this.statusEditable = new SelectJEditable({
							target : this.settings.urls.testCaseUrl,
							componentId : "automation-request-status",
							jeditableSettings : {
								data : this.settings.automReqStatusComboJson
							}
						});

						$('#transmit-test-case-autom-request-button').on('click', function() {
							$.ajax({
								url: self.settings.urls.testCaseUrl,
								method: 'POST',
								data: {
									'id': 'automation-request-status',
									'value': 'TRANSMITTED'
								}
							}).success(function() {
								self.trySquashTAScriptAssociation();
								$('#automation-request-status').text(translator.get('automation-request.request_status.TRANSMITTED'));
							});
						});

						automatableRadio.on('change', function() {
							var value = this.value;
							$.ajax({
								url:  self.settings.urls.testCaseUrl,
								method: 'POST',
								data: {
									'id': 'test-case-automatable',
									'value': this.value
								}
							}).success(function() {
									self.initAutomationRequestBlock();
									self.updateAutomatableInTree(value);
							});
						});
					} else {
						automatableRadio.attr('disabled', true);
					}

				},

				_updateAutomatableInTree : function(value){
					var self = this;
					var identity = {resid: self.settings.testCaseId, restype: "test-cases"};
					eventBus.trigger('node.attribute-changed', {identity : identity, attribute : 'automeligible', value : value.toLowerCase()});

				},

				initAutomationRequestBlock : function() {
					if ($('input[type=radio][name=test-case-automatable]:checked').val() === 'Y') {
						$('.test-case-automation-request-block').show();
					} else {
						$('.test-case-automation-request-block').hide();
					}
				},

				trySquashTAScriptAssociation : function () {
					$.ajax({
						url:  this.settings.urls.testCaseUrl + "/associate-TA-script",
						method: 'POST'
					});
			  },

				events : {

				}

			});
			return AutomationPanel;
});
