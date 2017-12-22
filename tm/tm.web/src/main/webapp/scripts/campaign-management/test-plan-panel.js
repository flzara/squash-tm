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
 * accepts as basic configuration :
 * {
 *	data : {
 *		campaignId : the id of the campaign
 *	},
 *	features : {
 *		reorderable : can the test plan be reordered by the user ?
 *		editable : is the test plan editable by the user ?
 *		linkable : can one add more test cases to the test plan ?
 *	}
 *
 * }
 *
 * Note that this code is incomplete, see for instance the iteration-management for an example of what we
 * are aiming to.
 *
 */

define(['squash.translator', './test-plan-panel/table', './test-plan-panel/popups', 'app/util/ButtonUtil' ], function(translator, table, popups, ButtonUtil ) {

	function enhanceConfiguration(origconf){

		var conf = $.extend(true, {}, origconf);

		var baseURL = squashtm.app.contextRoot;

		conf.messages = translator.get({
			allLabel : "label.All",
			automatedExecutionTooltip : "label.automatedExecution"
		});

		conf.urls = {
			testplanUrl : baseURL + '/campaigns/'+conf.data.campaignId+'/test-plan/'
		};

		// because of the filtermode in the table we have to alias some properties of the conf :
		// 'data' -> 'basic',
		// 'features' -> 'permissions'

		conf.basic = conf.data;
		conf.permissions = conf.features;

		return conf;

	}

	function _bindButtons(conf){
		if (conf.features.editable){
			$("#assign-users-button").on('click', function(){
				$("#camp-test-plan-batch-assign").formDialog('open');
			});
		}

		if (conf.features.reorderable){
			$("#reorder-test-plan-button").on('click', function(){
				$("#camp-test-plan-reorder-dialog").confirmDialog('open');
			});
		}

		if (conf.features.linkable){ 
			$("#add-test-case-button").on('click', function(){
				document.location.href=conf.urls.testplanUrl + "/manager";
			});


			$("#remove-test-plan-button").on('click', function(){
				$("#delete-multiple-test-cases-dialog").formDialog('open');
			}); 
		}


		$("#filter-test-plan-button").on('click', function(){
			var domtable =  $("#campaign-test-plans-table").squashTable();
			domtable.toggleFiltering();	// see the initialization module table#init()
		});
	}

	return {
		init : function(origconf){
			var conf = enhanceConfiguration(origconf);
			_bindButtons(conf);
			table.init(conf);
			popups.init(conf);
			filterOn = false;
		}
	};


});