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
 * Here is a good template your could use for your own milestone panels. Please pay attention to the classes
 * milestone-panel-X.
 * 
 * <div class="milestone-panel-master"> <div class="toolbar"> <input type="button" class="sq-btn
 * milestone-panel-bind-button" value="+"/> <input type="button" class="sq-btn milestone-panel-unbind-button"
 * value="-"/> </div>
 * 
 * <div class="table-tab-wrap">
 * 
 * <table class="milestone-panel-table" data-def="..."> table definition goes here </table>
 * 
 * </div>
 * 
 * <div class="bind-milestone-dialog popup-dialog not-displayed"> add popup definition goes here, see file
 * jquery.squash.milestoneDialog.js </div>
 * 
 * <div class="unbind-milestone-dialog popup-dialog not-displayed"> add popup definition goes here, see file
 * jquery.squash.milestoneDialog.js </div>
 * 
 * <script>
 * 
 * var conf = { see documentation below }
 * 
 * require(["milestones/milestone-panel"], function(panel){ panel.init(conf); });
 * 
 * </script>
 * 
 * </div>
 * 
 * 
 * 
 * configuration :
 *  {
 * 
 * element : the jquery selector for the panel (optional), rootPath : the root path for the entity of which we 're
 * managing the milestones. Note that we don't want the root context (eg not '/squash') identity : { resid : the id of
 * the test-case/whatever this panel sits in restype : the type of entity : 'testcases etc' }, currentModel : if
 * provided, the table will be initialized with it, editable : boolean, whether the user can or not edit the table
 * content }
 * 
 * 
 * Events :
 *  - node.unbindmilestones : the entity was removed from the scope of one or several milestones. The event comes with a
 * companion data : { identity : identity, milestones : [array, of, milestoneids] }
 * 
 * 
 */
define([ "jquery", "workspace.event-bus", "app/ws/squashtm.notification", "squash.translator", "squashtable",
		"./jquery.squash.milestoneDialog", "jquery.squash.formdialog" ],
		function($, eventBus, notification, translator) {

			function isLocked(table) {

				var data = table.fnGetData();
				if (data.length === 0) {
					return false;
				}

				var locked = false;

				data.forEach(function(val) {
					if (val['isStatusAllowUnbind'] === false) {
						locked = true;
					}
					;
				});

				return locked;
			}

			function init(conf) {

				// stuff the configuration with the last bits
				var rootContext = squashtm.app.contextRoot + conf.rootPath;
				conf.currentTableSource = rootContext + "/milestones";
				conf.bindTableSource = rootContext + "/milestones/associables";
				conf.milestonesURL = rootContext + "/milestones";

				// now we begin
				var element = $(conf.element || ".milestone-panel-master");

				/*
				 * Here we want our table to use a local model for initialization, perform sort and filter operations
				 * locally, yet be able to reload by ajax.
				 * 
				 * Configuring all of this at once doesn't work because the table initialize the content once with the
				 * model and a second time with the ajax source.
				 * 
				 * So we need to trick it by initializing it with no ajax source specified, then we supply it when it's
				 * complete.
				 * 
				 */
				var tblCnf = {
					aaData : conf.currentModel,
					iDeferLoading : conf.currentModel.length,
					aaSorting : [ [ 3, 'asc' ] ],
					bServerSide : false,
					bDeferLoading : true,
					fnRowCallback : function(nRow, aData) {

						if (conf.editable) {

							var row = $(nRow);

							var isStatusAllowUnbind = aData['isStatusAllowUnbind'];
							if (isStatusAllowUnbind === false) {
								row.find('.unbind-button').removeClass('unbind-button');
							}

							// this callback is necessary only for test case milestones
							var tcDirectMember = aData['directMember'];
							if (tcDirectMember === false) {
								row.addClass('milestone-indirect-membership');
								row.find('.unbind-button').removeClass('unbind-button');

							}
						}

					}
				}, squashCnf = {

				};

				var currentTable = element.find('.milestone-panel-table').squashTable(tblCnf, squashCnf);

				// now we can set the ajax source
				currentTable.fnSettings().sAjaxSource = conf.currentTableSource;

				// fix order
				currentTable.on('order.dt search.dt', function() {

					$.each(currentTable.fnGetNodes(), function(index, cell) {
						cell.firstChild.innerHTML = index + 1;
					});
				});

				currentTable.on('processing.dt', function(event) {
					// reapply sorting after processing, so order is correct after _fnAjaxUpdate()
					currentTable.fnSort(currentTable.fnSettings().aaSorting); 
				})

				// editable features :
				if (conf.editable) {

					// add milestones dialog
					var dialogOptions = {
						tableSource : conf.bindTableSource,
						milestonesURL : conf.milestonesURL,
						identity : conf.identity,
						multilines : conf.multilines
					};

					var bindDialog = element.find('.bind-milestone-dialog');
					bindDialog.milestoneDialog(dialogOptions);

					$(".milestone-panel-bind-button").on('click', function() {
						bindDialog.milestoneDialog('open');
					});

					bindDialog.on('milestonedialogopen', function() {

						// maybe I'll need that flag later :
						var locked = isLocked(currentTable);

						// if there's not at least one milestone in project or only planned and locked status
						if (!conf.milestoneInProject) {
							bindDialog.milestoneDialog('setState', 'no-available-milestone');
						}

						// Campaign with locked milestone can't bind another milestone because that would remove locked
						// milestone.
						else if (conf.nodeType === "campaign" && locked) {
							bindDialog.milestoneDialog('setState', 'forbidden');
						}

						// else, we're fine
						else bindDialog.milestoneDialog('setState', 'select-milestone');

					});

					// remove milestones
					var unbindDialog = $(".unbind-milestone-dialog");

					unbindDialog.formDialog();

					unbindDialog.on('formdialogopen', function() {
						var $this = $(this);
						var id = $this.data('entity-id');
						var ids = !$.isArray(id) ? [ id ] : id;
						var state;

						// if the original selection is empty, bail out
						if (ids.length === 0) {
							unbindDialog.formDialog('setState', "none-selected");
							return;
						}

						// from the original set of ids, we must filter out ids that
						// can't be unbound because their status doesn't allow it,
						// or - specifically for test cases - because they are inherited from verified requirements.

						var unbindableIds = $.grep(ids, function(id) {
							var data = currentTable.getDataById(id);
							// for non test cases tables, directMember will be undefined and allways count as 'true'
							return data['isStatusAllowUnbind'] && (data['directMember'] !== false);
						});

						switch (unbindableIds.length) {
						case 0:
							state = "none-can-be-removed";
							break;
						case 1:
							state = "one-selected";
							break;
						default:
							state = "more-selected";
						}

						// needs details ?
						var details = unbindDialog.find('.dialog-details');
						if (ids.length > unbindableIds.length) {
							details.show();
						} else {
							details.hide();
						}

						// now show the panel
						unbindDialog.formDialog('setState', state);
					});

					/*
					 * Here we filter out the milestones that weren't direct milestones
					 */
					unbindDialog.on('formdialogconfirm', function() {

						var ids = [];

						var rows = currentTable.getSelectedRows();
						rows.each(function() {

							var data = currentTable.fnGetData(this);

							// the test is a success if 'directMember'
							// is true or undefined (that is intended).
							if (data['directMember'] !== false && data['isStatusAllowUnbind'] !== false) {
								ids.push(data['entity-id']);
							}
						});

						// abort if empty
						if (ids.length === 0) {
							unbindDialog.formDialog('close');
						}
						// else post
						else {
							var url = conf.milestonesURL + '/' + ids.join(',');

							$.ajax({
								url : url,
								type : 'DELETE'
							}).success(function() {
								eventBus.trigger('node.unbindmilestones', {
									identity : conf.identity,
									milestones : ids
								});
								unbindDialog.formDialog('close');
							});
						}

					});

					unbindDialog.on('formdialogcancel', function() {
						unbindDialog.formDialog('close');
					});

					$(".milestone-panel-unbind-button").on('click', function() {
						var ids = currentTable.getSelectedIds();
						unbindDialog.data('entity-id', ids);
						unbindDialog.formDialog('open');
					});

					// event subscription

					eventBus.onContextual('node.bindmilestones node.unbindmilestones', function() {
						currentTable._fnAjaxUpdate();
					});

				}
			}

			return {
				init : init
			};

		});
