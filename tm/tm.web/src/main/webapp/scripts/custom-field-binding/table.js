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
 * settings (see the doc for jquery datatable for details about the native settings):
 *  {
 *      selector : the selector for the table,
 *      languageUrl : the url where to fetch the localization conf object,
 *      getUrl : the ajaxSource (native),
 *      deleteUrl : the url where to send DELETE request,
 *      moveUrl : the url where to notify that rows have been reordered
 *      editUrl : the url where to put or delete that a location has changed 
 *      deferLoading : the iDeferLoading (native),
 *      oklabel : text for the ok button,
 *      cancellabel : text for the cancel button,
 *      renderingLocations : an array of RenderingLocation. These are the ones supported by the BindableEntity this table is treating.  
 *  }
 */

define(
		[ "jquery", "squashtable" ],
		function($) {

			return function(settings) {

				// ******************** hook, callback etc definition
				// **********************************

				/*
				 * The data format for RenderingLocation are inconsistent
				 * between the datatable and what the server returns. Indeed,
				 * for each row : - the datatable expects the renderingLocations
				 * to be a map, mapping a location to a boolean (true | false), -
				 * while the server actually returns an array, containing only
				 * the locations used (and disabled locations aren't returned)
				 * 
				 * To cope with that we must customize the way the datatable and
				 * the server talk and make the data compatible.
				 */

				var fnServerData = function(sSource, aoData, fnCallback, oSettings) {
					oSettings.jqXHR = $.ajax({
						'dataType' : 'json',
						'type' : 'GET',
						'url' : sSource,
						'data' : aoData,
						'success' : function(allData, textStatus, jqXHR) {
							var availableLocations = settings.renderingLocations;
							var count = 0, dataLength = allData.aaData.length;

							var namecollect = function(elt){ return elt.enumName; };
							for (count = 0; count < dataLength; count++) {
								var data = allData.aaData[count];

								var actualLocations = $.map(data.renderingLocations, namecollect); 

								var result = {}, i = 0, max = renderingLocations.length;

								for (i = 0; i < max; i++) {
									var possibleLocation = renderingLocations[i];
									result[possibleLocation] = ($.inArray(possibleLocation,	actualLocations) !== -1) ? "true" : "false";
								}

								data.renderingLocations = result;
							}
							// now we can invoke the original callback
							fnCallback(allData, textStatus, jqXHR); 
						}
					});
				};

				var fnDrawCallback = function(oSettings) {
					var table = this;
					var cells = table.find('td.custom-field-location');

					var clickHandler = function() {
						var checkbx = $(this);
						var row = checkbx.parent('td').parent('tr').get(0);

						var locationName = checkbx.data('location-name'), checked = checkbx
								.prop('checked');

						var rowdata = table.fnGetData(row);
						var id = rowdata.id;

						$.ajax({
							url : settings.editUrl + "/" + id	+ "/renderingLocations/" + locationName,
							type : (checked) ? 'PUT' : 'DELETE'
						});
					};

					cells.each(function() {

						var cell = $(this);
						var row = cell.parent('tr').get(0);
						var colPosition = table.fnGetPosition(this)[2];
						// see the definition of aoColumnDefs regarding the offset (-3)
						var locationName = settings.renderingLocations[colPosition - 3]; 

						var checkbx = $('<input type="checkbox" />');
						checkbx.data('location-name', locationName);
								checkbx.prop('checked', (table
										.fnGetData(this) == "true"));
						checkbx.click(clickHandler);

						cell.empty().append(checkbx);
					});

				};

				// **************************** column definition
				// *******************************

				var aoColumnDefs = [
						{
							'bSortable' : false,
							'bVisible' : false,
							'aTargets' : [ 0 ],
							'mDataProp' : 'id'
						},
						{
							'bSortable' : false,
							'bVisible' : true,
							'aTargets' : [ 1 ],
							'mDataProp' : 'position',
							'sWidth' : '2em',
							'sClass' : 'centered ui-state-default drag-handle select-handle'
						}, {
							'bSortable' : false,
							'bVisible' : true,
							'aTargets' : [ 2 ],
							'mDataProp' : 'customField.name'
						} ];

				var i = 0, renderingLocations = settings.renderingLocations, arrayLength = renderingLocations.length;

				for (i = 0; i < arrayLength; i++) {
					var columnDef = {
						'bSortable' : false,
						'bVisible' : true,
						'aTargets' : [ 3 + i ],
						'mDataProp' : 'renderingLocations.'	+ renderingLocations[i],
						'sWidth' : '15em',
						'sClass' : 'centered custom-field-location'
					};
					aoColumnDefs.push(columnDef);
				}

				aoColumnDefs.push({
					'bSortable' : false,
					'bVisible' : true,
					'aTargets' : [ 3 + arrayLength ],
					'mDataProp' : null,
					'sWidth' : '2em',
					'sClass' : 'unbind-button centered'
				});

				// **************************** rest of the table initialization
				// ******************************
				var tableConf = {
					oLanguage : {
						sUrl : settings.languageUrl
					},
					sAjaxSource : settings.getUrl,
					iDeferLoading : settings.deferLoading,
					aoColumnDefs : aoColumnDefs,
					fnServerData : fnServerData,
					fnDrawCallback : fnDrawCallback
				};

				var squashConf = {

					dataKeys : {
						entityId : 'id',
						entityIndex : 'position'
					},

					confirmPopup : {
						oklabel : settings.oklabel,
						cancellabel : settings.cancellabel
					},

					unbindButtons : {
						url : settings.deleteUrl + "/{id}",
						delegate : settings.deletePopupSelector,
						tooltip : settings.deleteTooltip,
						success : function() {
							$(settings.selector).squashTable().refresh();
						}
					},

					enableHover : true,

					enableDnD : true,

					fixObjectDOMInit : true,

					functions : {
						dropHandler : function(moveObject) {
							$.ajax({
								url : settings.moveUrl + "/"+ moveObject.itemIds.join(',')+ "/position",
								type : 'POST',
								data : {
									'newPosition' : moveObject.newIndex
								}
							}).success(function() {
								$(settings.selector).squashTable().refresh();
							});
						}
					}

				};

				$(settings.selector).squashTable(tableConf, squashConf);

				var table = $(settings.selector).squashTable();

				return table;

			};

		});