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
 * WARNING : THIS IS A STUB. MUCH OF THE CODE STILL LIES IN THE ORIGINAL TAG FILE. TODO : MOVE IT ALL HERE !
 *
 * @see 'tags/capaigns-components/campaign-test-plan-table.tag'
 * @see 'iteration-management/test-plan-panel'
 *
 *
 *
 *
 * configuration an object as follow :
 *"
 * {
 *		permissions : {
 *			editable : boolean, is the table content editable ?
 *			reorderable : boolean, is the test plan reorderable ?
 *		},
 *		basic : {
 *			campaignId : the id of the current iteration
 *		},
 *		messages : {
 *			allLabel : label meaning 'all' in the current locale
 *		},
 *		urls : {
 *			testplanUrl : base urls for test plan items,
 *		}
 *	}
 *
 */

define(['jquery', '../../test-plan-panel/sortmode', 'squash.configmanager',
        '../../test-plan-panel/filtermode', "squash.translator", 'squashtable', 'jeditable'],
        function($, smode, confman, fmode, translator) {

	function createTableConfiguration(conf){
 
		var rowCallback = function(row, data, displayIndex) {

			var $row = $(row);

			// ********* first, treat the read-permission features *********

			var $exectd = $row.find('.exec-mode').text('');
			if (data['exec-mode'] === "M") {
				$exectd.append('<span class="exec-mode-icon exec-mode-manual"/>').attr('title', '');
			} else {
				$exectd.append('<span class="exec-mode-icon exec-mode-automated"/>').attr('title',
						conf.messages.automatedExecutionTooltip);
			}

			// assignee
			$row.find('.assignee-combo').wrapInner('<span/>');

			// dataset : we create the 'button' part of a menu, but not actual menu.
			if (data['dataset'].available.length>0){
				var $dstd = $row.find('.dataset-combo');
				$dstd.wrapInner('<span />');
			}

			// ****** now the write permission features *********************

			if (conf.features.editable){

				// assignable users
				var assignableUsers = conf.data.assignableUsers;
				var assigneeurl = conf.urls.testplanUrl + data['entity-id'];
				var $assigneeelt = $row.find('.assignee-combo').children().first();
				$assigneeelt.addClass('cursor-arrow');
				$assigneeelt.editable(
					assigneeurl,{
						type : 'select',
						data : assignableUsers,
						name : 'assignee',
						onblur : 'cancel',
						callback : function(value, settings){
							$(this).text(assignableUsers[value]);
						}
				});

				// datasets : we build here a full menu. Note that the read features
				// already ensured that a <a class="buttonmenu"> exists.
				var $dsspan = $row.find('.dataset-combo').children().first(),
					dsInfos = data['dataset'],
					dsurl = conf.urls.testplanUrl + data['entity-id'];

				if (dsInfos.available.length>0){
					$dsspan.addClass('cursor-arrow');
					$dsspan.editable(dsurl, {
						type : 'select',
						data : confman.toJeditableSelectFormat(dsInfos.available),
						name : 'dataset',
						onblur : 'cancel',
						callback : function(value, settings){
							$(this).html(settings.data[value]);
						}
					});
				}

			}


		};

		var drawCallback = function(){

			// make all <select> elements autosubmit on selection
			// change.
			this.on('change', 'select', function() {
				$(this).submit();
			});

			//sort mode
			this.data('sortmode').update();
		};

		var preDrawCallback = function(settings){
			
			/*
			 * The column dataset.selected.name is visible if : 
			 * 1/ the dataset column is being filtered (we want to see the filter) or
			 * 2/ at least one row contains a non empty dataset
			 * 
			 */
			var alldata = this.fnGetData();
			
			var dsFilterOn = this.data('filtermode').isFiltering('dataset.selected.name'),
			rowsHavingDataset = $.grep(alldata, function(model){ return model.dataset.available.length !== 0 ;});

			
			var dsColIdx = this.getColumnIndexByName('dataset.selected.name'),
				dsColVis = (dsFilterOn || rowsHavingDataset.length!==0);
			
	
			this.fnSetColumnVis(dsColIdx, dsColVis, false);
			this.data('showDatasets', dsColVis);
		};

		var tableSettings = {
			bFilter : true,
			"aLengthMenu" : [[10, 25, 50, 100, -1], [10, 25, 50, 100, conf.messages.allLabel]],
			fnDrawCallback : drawCallback,
			fnRowCallback :  rowCallback,
			fnPreDrawCallback : preDrawCallback,
			"aaSorting" : [ [ 2, "asc" ] ]
		};

		var squashSettings = {unbindButtons : {
			tooltip : translator.get('dialog.unbind-testcase.tooltip')
		}};

		if (conf.features.reorderable){
			squashSettings.enableDnD = true;
			squashSettings.functions = {
				dropHandler : function(dropData){
					var ids = dropData.itemIds.join(',');
					var url	= conf.urls.testplanUrl + '/' + ids + '/position/' + dropData.newIndex;
					$.post(url, function(){
						$("#campaign-test-plans-table").squashTable().refresh();
					});

				}
			};
		}

		return {
			tconf : tableSettings,
			sconf : squashSettings
		};
	}


	// **************** MAIN ****************

	return {
		init : function(enhconf){

			var tableconf = createTableConfiguration(enhconf);

			var sortmode = smode.newInst(enhconf);
			var filtermode = fmode.newInst(enhconf);

			tableconf.tconf.aaSorting = sortmode.loadaaSorting();
			tableconf.tconf.searchCols = filtermode.loadSearchCols();


			var table = $("#campaign-test-plans-table");
			table.data('sortmode', sortmode);
			table.data('filtermode', filtermode);
			
			table.squashTable(tableconf.tconf, tableconf.sconf);

			// glue code between the filter and the sort mode
			function toggleSortmode(locked){
				if (locked){
					sortmode.disableReorder();
				}
				else{
					sortmode.enableReorder();
				}
			}


			toggleSortmode(filtermode.isFiltering());

			table.toggleFiltering = function(){
				var isFiltering = filtermode.toggleFilter();
				toggleSortmode(isFiltering);
			};
		}
	};

});