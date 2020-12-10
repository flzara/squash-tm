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
/*
 settings :
 - canModify : a boolean telling if the associated script can be changed or not
 - testAutomationURL : the url where to GET - POST - DELETE things.
 */
define([ "jquery", "squash.configmanager", "jeditable" ], function($, confman) {


	// ****************** init function ********************

	function init(settings){


		// simple case first
		if (! settings.canModify){
			return;
		}

		// else we must init editable fields
		_initAutomatedTestReferenceCell(settings);
		_initScmUrlCell(settings);
		_initAutomatedTestTechnologyCell(settings);

	}

	function _initScmUrlCell (settings) {
		var scmUrlCell = $("#test-case-source-code-repository-url");

		var scmUrlCellEditable = confman.getStdJeditable();
		scmUrlCellEditable.params = {
			"id": "test-case-source-code-repository-url"
		};
		scmUrlCellEditable.maxlength = 255;

		scmUrlCell.editable(settings.testCaseUrl, scmUrlCellEditable);

		scmUrlCell.on('keyup', function (event) {
			// not perform autocomplete if arrows are pressed
			if (!_.contains([37, 38, 39, 40], event.which)) {
				var searchInput = $(event.currentTarget).find('input');
				searchInput.autocomplete();
				performAutocomplete(searchInput);
			}
		});
	}

	function performAutocomplete (searchInput) {
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

	function _initAutomatedTestReferenceCell (settings) {
		var automatedTestReferenceCell = $("#test-case-automated-test-reference");
		var automatedTestReferenceCellEditable = confman.getStdJeditable();
		automatedTestReferenceCellEditable.params = {
			"id": "test-case-automated-test-reference"
		};
		automatedTestReferenceCellEditable.maxlength = 255;

		automatedTestReferenceCell.editable(settings.testCaseUrl, automatedTestReferenceCellEditable);
	}

	function _initAutomatedTestTechnologyCell (settings) {
		var automatedTestTechnologyCell = $("#test-case-automated-test-technology");

		var automatedTestTechnologyCellEditable = confman.getJeditableSelect();

		automatedTestTechnologyCellEditable.params = {
			"id": "test-case-automated-test-technology"
		};
		automatedTestTechnologyCellEditable.data = confman.toJeditableSelectFormat(settings.automatedTestTechnologies);

		automatedTestTechnologyCell.editable(settings.testCaseUrl, automatedTestTechnologyCellEditable);
	}

	return {
		init : init
	};
});
