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
define([ "jquery", "backbone", "underscore"], function($, Backbone, _) {

	var KeywordStepTablePanel = Backbone.View.extend({

		el : "#tab-tc-keyword-steps",

		initialize : function(options) {
			var self = this;
			this.settings = options.settings;
		},

		events : {
			"click #add-keyword-test-step-btn" : "addKeywordTestStep"
		},

		addKeywordTestStep: function() {
			var inputKeyword = $('#add-keyword-test-step-input').val();
			$.ajax({
				type: "POST",
				url: "/squash/test-cases/"+this.settings.testCaseId+"/steps/add-keyword",
				contentType: 'application/json',
				data: inputKeyword
			}).done(function(id){
				var displayDiv = $('#add-keyword-test-step-result');
				displayDiv.text("The keyword test step has been successfully created with id : "+id+" and name : "+inputKeyword);
			});
		}

	});
	return KeywordStepTablePanel;
});
