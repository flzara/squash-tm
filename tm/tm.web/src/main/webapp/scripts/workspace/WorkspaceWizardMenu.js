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
/**
 * This is a template for a backbone module
 */
define([ "jquery", "backbone", "handlebars", "underscore", "app/util/StringUtil", "jqueryui", "jquery.squash.squashbutton" ], function($,
		Backbone, Handlebars, _, util) {
	var View = Backbone.View.extend({
		el : "#wizard-tree-pane",

		events : {
			"click #ws-wizard-tree-menu li" : "_onMenuClicked"
		},
		
		
		initialize : function() {
		
			_.each(this.collection, function(wiz) {
				// better when no dots in html ids
				wiz.name = wiz.id.replace(/\./g, "-");
			});
			
			this.menu = this.$("#wizard-tree-button");
			this.formContainer = this.$("#start-ws-wizard-container");
			
			this.render();

		},

		render : function() {
			
			var source = this.$("#ws-wizard-tree-menu-template").html();
			var template = Handlebars.compile(source);
			
			this.$("#wizard-tree-button").after(template({
				wizards : this.collection
			}));
			
			this.menu.buttonmenu();
			
			if (this.collection.length === 0){
				this.menu.prop("disabled", true);
			}
			

			return this;
		},

		/**
		 * Notifies the menu of new nodes selection and refreshes the menu access.
		 */
		refreshSelection : function(selectedNodes) {
			this.selectedNodes = selectedNodes;
			this.refreshAccess(selectedNodes);
		},

		/**
		 * Refreshes the access to the menu items
		 */
		refreshAccess : function(selectedNodes) {
			var refreshItemAccess = this._refreshItemAccessHandler(selectedNodes);
			_.each(this.collection, refreshItemAccess);
		},

		/**
		 * Returns a handler for refreshing a menu item in the given node context.
		 */
		_refreshItemAccessHandler : function(selectedNodes) {
			var self = this;
			return function(wizard) {
				var accessRule = wizard.accessRule;
				var enabled = self._checkSelectionMode(selectedNodes, accessRule) &&
						self._checkPermission(selectedNodes, accessRule) &&
						self._checkWizardActivation(selectedNodes, wizard);

				if (enabled) {
					self.$('#'+wizard.name).removeClass('ui-state-disabled');

				} else {
					self.$('#'+wizard.name).addClass('ui-state-disabled');
				}
			};
		},

		/**
		 * Checks that the node context matches the selection mode defined in access rule.
		 */
		_checkSelectionMode : function(selectedNodes, accessRule) {
			var res;
			switch (accessRule.selectionMode['$name']) {
			case "SINGLE_SELECTION":
				res = selectedNodes.length === 1;
				break;
			case "MULTIPLE_SELECTION":
				res = selectedNodes.length > 0;
				break;
			}
			return res;
		},

		/**
		 * Checks that the current node context matches the required permissions for wizard execution.
		 */
		_checkPermission : function(selectedNodes, accessRule) {
			var self = this;
			var reducePermission = function(node) {
				return function(reduced, rule) {
					return reduced || self._nodeMatchesRule(node, rule);
				};
			};

			return _.reduce(selectedNodes, function(reduced, node) {
				return reduced && _.reduce(accessRule.rules, reducePermission(node), false);
			}, true);
		},

		_nodeMatchesRule : function(node, rule) {
			var $node = $(node).treeNode();
			return $node.isAuthorized(rule.permission['$name']) && $node.is(":" + rule.nodeType['$name'].toLowerCase());
		},

		/**
		 * Checks that the given wizard is activated for the project of selected nodes
		 */
		_checkWizardActivation : function(selectedNodes, wizard) {
			return _.reduce(selectedNodes, function(reduced, node) {
				return reduced && $(node).treeNode().isWorkspaceWizardEnabled(wizard);
			}, true);
		},

		/**
		 * Event handler triggered when menu item is clicked. Posts the selected nodes to the wizard's url
		 */
		_onMenuClicked : function(event, data) {
			var wizard = _.find(this.collection, function(wizard) {
				return wizard.name === event.currentTarget.id;
			});
			
			if (! util.isBlank(wizard.url)){

				var postData = {
					url : squashtm.app.contextRoot + "/" + wizard.url
				};
				postData.nodes = _.map(this.selectedNodes, function(node) {
					var $node = $(node).treeNode();
					return {
						type : $node.getResType(),
						id : $node.getResId()
					};
				});
	
				var source = this.$("#start-ws-wizard-form-template").html();
				var template = Handlebars.compile(source);
				this.formContainer.html(template(postData));
				this.formContainer.find("form").submit();
				
			}
			else{
				return true;
			}
		}
	});

	return View;
});