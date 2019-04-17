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
 define(["jquery", "backbone", 'handlebars', 'squash.translator', 'jquery.squash.confirmdialog'],
 	function($, Backbone, Handlebars, translator) {
 	"use strict";

 	var View = Backbone.View.extend({

 		el: '#cuf-popup',

 		initialize: function(options) {
 			this.model = options;
 			this.render();
 			this.$el.confirmDialog({
 				autoOpen: true
 			});
 		},

 		events: {
 			'confirmdialogcancel': 'remove',
 			'confirmdialogconfirm': 'confirm'
 		},

 		render: function() {
 			this.$el.html("");
 			var src = $('#cuf-popup-tpl').html();
 			this.template = Handlebars.compile(src);
 			this.$el.append(this.template(this.model));
 			return this;
 		},

 		remove: function() {
 			Backbone.View.prototype.remove.apply(this, arguments);
 			$('#cuf-popup-container').html(
 				"<div id='cuf-popup' class='not-displayed popup-dialog'"
 				+ translator.get('generics.customfieldvalues.title') + '" />');
 		},

 		confirm: function(event) {
 			var selectedCufAttributes = this.model.get('selectedCufAttributes') || [];
 			var checkedCufAttributes = this.getSelectedCheckboxes();
 			var currentDisplayedCufIds = _.pluck(this.model.get('cufToDisplay'), 'id');
 			// remove the cuf related to this entity
 			selectedCufAttributes = _.reject(selectedCufAttributes, function(id) {
 				return _.contains(currentDisplayedCufIds, id);
 			});
 			// now add the checked cufs
 			if(checkedCufAttributes.length > 0) {
 				selectedCufAttributes = selectedCufAttributes.concat(checkedCufAttributes);
 			}
 			this.model.set('selectedCufAttributes', selectedCufAttributes);
 			this.remove();
 		},

 		getSelectedCheckboxes: function() {
 			return $('.cuf-checkbox:checked').map(function() {
 				return $(this).val();
 			}).toArray();
 		}

 	});

 	return View;
 });
