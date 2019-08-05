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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'app/ws/squashtm.notification', "workspace.storage", "../../automation-table/sort", "../../automation-table/filter", "squash.configmanager", "tree/plugins/plugin-factory", "squashtable", "jeditable"],
    function ($, _, Backbone, Handlebars, translator, notification, storage, sortmode, filtermode, confman, treefactory) {
        "use strict";

        var View = Backbone.View.extend({
            el: "#contextual-content-wrapper",
            key: "checkbox-global",
            storage: storage,
            selected: 0,
            initialize: function () {
                this.render();
                var self = this;
                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-workspace/automation-requests/global",
                    "aaSorting": [[7, 'desc'], [8, 'asc'], [12, 'desc']],
                    "bDeferRender": true,
                    "iDisplayLength": 25,
                    "aoColumnDefs": [{
                        "bSortable": false,
                        "aTargets": [0],
                        "sClass": 'centered no-select-handle',
                        "mDataProp": "entity-index",
                        "sWidth": "2.5em"
                    }, {
                        "bSortable": true,
                        "aTargets": [1],
                        "mDataProp": "project-name"
                    }, {
                        "bSortable": true,
                        "aTargets": [2],
                        "mDataProp": "entity-id",
                        "sClass": "entity_id"
                    }, {
                        "bSortable": true,
                        "aTargets": [3],
                        "mDataProp": "reference"
                    }, {
                        "bSortable": true,
                        "aTargets": [4],
                        "mDataProp": "name"
                    }, {
                        "bSortable": true,
                        "aTargets": [5],
                        "mDataProp": "format"
                    }, {
                        "bSortable": true,
                        "aTargets": [6],
                        "mDataProp": "created-by"
                    }, {
                        "bSortable": true,
                        "aTargets": [7],
                        "mDataProp": "priority",
                        "sClass": "priority"
                    }, {
                        "bSortable": true,
                        "aTargets": [8],
                        "mDataProp": "status"
                    }, {
                        "bSortable": true,
                        "aTargets": [9],
                        "mDataProp": "assigned-to"
                    }, {
                        "bSortable": true,
                        "aTargets": [10],
                        "mDataProp": "script",
                        "sClass": "assigned-script",
						"mRender": function (data, type, row) {
							var hrefScript="";
							var title = translator.get('test-case.automation-btn-conflict');
							if (row['listScriptConflict'].length!==1) {
								hrefScript='<a href="" class="tf-sm script-conflict" id="list-script-conflict" >'+ title +'</a>';

							}else{
								hrefScript = data;
							}

							return hrefScript;
						}
                    }, {
                        "bSortable": false,
                        "aTargets": [11],
                        "mDataProp": "uuid"
                    },{
                        "bSortable": true,
                        "aTargets": [12],
                        "mDataProp": "transmitted-on"
                    }, {
                        "bSortable": true,
                        "aTargets": [13],
                        "mDataProp": "assigned-on"
                    }, {
                        "bSortable": false,
                        "aTargets": [14],
                        "mDataProp": "tc-id",
                        "sClass": "centered",
                        "sWidth": "2.5em",
                        "mRender": function (data, type, row, meta) {
                            return '<a href="' + squashtm.app.contextRoot + 'test-cases/' + data + '/info" class="table-button view-eye"></a>';
                        }
                    }, {
                        "mDataProp": "writableAutom",
                        "bVisible": false,
                        "aTargets": [15]
                    }, {
                        "bSortable": false,
                        "aTargets": [16],
                        "mDataProp": "checkbox",
                        "sClass": "centered",
                        "mRender": function (data, type, row) {
                            var store = self.storage.get(self.key);
                            var checked = false;
                            if (_.contains(store, row["tc-id"])) {
                                checked = true;
                            }
                            var input = "";
                            var $row = $(row);
                            if (row['writableAutom']) {
                                if (checked) {
                                    input = '<input type="checkbox" class="editor-active" checked>';
                                    $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight");
                                } else {
                                    input = '<input type="checkbox" class="editor-active">';
                                }
                            }
                            return input;
                        },
                        "sWidth": "2.5em"
                    }, {
                        "mDataProp": "requestId",
                        "bVisible": false,
                        "aTargets": [17]
                    }, {
						"mDataProp": "listScriptConflict",
						"bVisible": false,
						"aTargets": [18]
					}],
                    "bFilter": true,

                    fnRowCallback: function (row, data, displayIndex) {
                        var $row = $(row);
                        var edObj = $.extend(true, {}, $.editable.types.text);
                        var edFnButtons = $.editable.types.defaults.buttons;
                        var edFnElements = $.editable.types.text.element;
                        var checkbox = $row.find("input[type=checkbox]");
                        if (checkbox !== undefined && checkbox[0].checked) {
                            $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight");
                        }

                        $row.on("change", "input[type=checkbox]", function () {

                            if (this.checked) {
                                $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight");
                            } else {
                                $row.removeClass("ui-state-row-selected").addClass("ui-state-highlight");
                            }
                            var store = self.storage.get(self.key);
                            if (store === undefined) {
                                var tab = [];
                                tab.push(data["tc-id"]);
                                self.storage.set(self.key, tab);
                            } else {
                                if (this.checked) {
                                    store.push(data["tc-id"]);

                                } else {
                                    var idx = store.indexOf(data["tc-id"]);
                                    store.splice(idx, 1);
                                }
                                self.storage.set(self.key, store);
                            }
                        });

                        $row.on("click", "td.select-handle", function () {
                            if (!$row.hasClass("ui-state-row-selected")) {
                                $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight");
                            }

                        });

                        var priority = $row.find('.priority');
                        if (priority.text() === '') {
                            priority.text('-');
                        }

                        edObj.buttons = function (settings, original) {
                            //first apply the original function
                            edFnButtons.call(this, settings, original);

                            // now add our own button
                            var btnChoose = $("<button/>", {
                                'text': translator.get('label.dot.pick'),
                                'id': 'ta-script-picker-button'
                            });

                            var btnRemove = $("<button/>", {
                                'text': translator.get('label.Remove'),
                                'id': 'ta-script-remove-button'
                            });
                            this.append(btnChoose)
                                .append(btnRemove);

                        };

                        // this is overriden so as to enforce the width.
                        edObj.element = function (settings, original) {
                            var input = edFnElements.call(this, settings, original);
                            input.css('width', '70%');
                            input.css('height', '16px');
                            return input;
                        };


                        $.editable.addInputType('ta-picker', edObj);
                        var cellId = "assigned-script" + data["entity-id"];
                        var editable = confman.getStdJeditable();
                        editable.type = 'ta-picker';
                        editable.name = "path";
                        var cell = $row.find('.assigned-script');
                        var entityId = data["entity-id"];

                        var url = squashtm.app.contextRoot + 'automation-requests/' + entityId + '/tests';
                        var isGherkin = data['format'].toLowerCase() === translator.get('test-case.format.gherkin').toLowerCase();
                        if (data['script'] !== '-' && (data['isManual'] || data['script'] === null) && !isGherkin && (data['listScriptConflict']===null || data['listScriptConflict'].length===1)) {
                            cell.editable(url, editable);
                            cell.css({ "font-style": "italic" });

                            cell.attr("id", cellId);

                            var urlTa = squashtm.app.contextRoot + 'automation-requests/' + entityId + '/tests';
                            var settings = {
                                url: urlTa,
                                id: cellId
                            };

                            cell.on("click", function () {
                                $("td[id!=" + cellId + "]").find("form button[type=cancel]").click();
                            });

                            cell.on('click', '#ta-script-picker-button', function () {
                                self._initPickerPopup(settings);
                                var popup = $("#ta-picker-popup").formDialog();
                                popup.formDialog('open');
                                return false;//for some reason jeditable would trigger 'submit' if we let go
                            });
                            cell.on('click', '#ta-script-remove-button', function () {
                                self._initRemovePopup(settings);
                                var input = $(cell).find("input");
                                if (input.val() !== "") {
                                    var popup = $("#ta-remove-popup").formDialog();
                                    popup.formDialog('open');
                                } else {
                                    input.val('');
                                }
                                return false;// see comment above
                            });
                        } else if (isGherkin && data['script'] !== '-' || data['script']!==null) {
                            cell.css({ 'color': 'gray', 'font-style': 'italic' });
                        } else if (data['listScriptConflict'] !== null && data['listScriptConflict'].length!==1) {
							/*TM-13: list of script in conflict*/
							cell.css({ 'color': 'gray', 'font-style': 'italic' });
							cell.on('click', '.script-conflict', function(evt){
							event.preventDefault();
							var $btn = $(evt.currentTarget);
							var $row = $btn.parents('tr');
							var rowmodel = sqtable.fnGetData($row);
							var title =  translator.get('test-case.automation-btn-conflict') + ":";
              var msg =  translator.get('test-case.automation-conflict-message');

							var list = '<ul>' + rowmodel.listScriptConflict.map(function(scr){return '<li>'+scr+'</li>';}) + '</ul>';
							var listScript = list.replace(',', '');
							notification.showInfo(title + "\n" + listScript + "\n" + msg);

							evt.stopPropagation();

						    });
						}
                    },

                    fnDrawCallback: function () {
                        self.selected = 0;
                        this.data("sortmode").update();
                        var rows = this.fnGetNodes();
                        $(rows).each(function (index, row) {
                            var $row = $(row);
                            if ($row.hasClass("ui-state-row-selected")) {
                                self.selected = self.selected + 1;
                            }

                        });
                        self.changeNumberSelectedRows(self.selected);
                    }
                };

                var $table = $("#automation-table");
                datatableSettings.customKey = "global";
                datatableSettings.testers = squashtm.app.globalUsers;
                datatableSettings.statuses = squashtm.app.autoReqStatuses;
                var fmode = filtermode.newInst(datatableSettings);
                var smode = sortmode.newInst(datatableSettings);
                datatableSettings.searchCols = fmode.loadSearchCols();
                datatableSettings.aaSorting = smode.loadaaSorting();
                $table.data('filtermode', fmode);
                $table.data('sortmode', smode);
                var sqtable = $table.squashTable(datatableSettings);
                sqtable.toggleFiltering = function () {
                    fmode.toggleFilter();
                };
                this.bindButtons();
                sqtable.on('change', function () {

                    self.changeNumberSelectedRows($("#automation-table").squashTable().getSelectedRows().length);
                });
            },

            _initRemovePopup: function (settings) {
                var dialog = $("#ta-remove-popup");

                dialog.formDialog();

                dialog.on('formdialogconfirm', function () {
                    dialog.formDialog('close');
                    var form = $(".assigned-script>form");
                    form.find('input').val('');
                    form.submit();
                });

                dialog.on('formdialogcancel', function () {
                    dialog.formDialog('close');
                });
            },

            _initPickerPopup: function (settings) {
                var dialog = $("#ta-picker-popup");

                var testAutomationTree = dialog.find(".structure-tree");

                // init

                dialog.formDialog({
                    height: 500
                });

                // ************ model loading *************************

                var initDialogCache = function () {
                    // cache
                    dialog.data('model-cache', undefined);
                    dialog.formDialog('setState', 'pleasewait');

                    return $.ajax({
                        url: settings.url,
                        type: 'GET',
                        dataType: 'json'
                    })
                        .done(function (json) {
                            dialog.data('model-cache', json);
                            createTree();
                            dialog.formDialog('setState', 'main');
                        })
                        .fail(function (jsonError) {
                            dialog.formDialog('close');
                        });
                };

                var createTree = function () {

                    treefactory.configure('simple-tree'); // will add the 'squash' plugin if doesn't exist yet
                    var instanceTree = testAutomationTree.jstree({
                        "json_data": {
                            "data": dialog.data('model-cache')
                        },

                        "types": {
                            "max_depth": -2, // unlimited without check
                            "max_children": -2, // unlimited w/o check
                            "valid_children": ["drive"],
                            "types": {
                                "drive": {
                                    "valid_children": ["ta-test", "folder"],
                                    "select_node": true
                                },
                                "ta-test": {
                                    "valid_chidlren": "none",
                                    "select_node": true
                                },
                                "folder": {
                                    "valid_children": ["ta-test", "folder"],
                                    "select_node": true
                                }
                            }
                        },

                        "ui": {
                            "select_multiple_modifier": false
                        },

                        "themes": {
                            "theme": "squashtest",
                            "dots": true,
                            "icons": true,
                            "url": squashtm.app.contextRoot + "styles/squash.tree.css"
                        },

                        "core": {
                            "animation": 0
                        },

                        conditionalselect: function () {
                            return true;
                        },

                        "plugins": ["json_data", "types", "ui", "themes", "squash", 'conditionalselect']

                    });
                };

                // ****************** transaction ************


                var submit = function () {
                    try {

                        var node = testAutomationTree.jstree('get_selected');

                        if (node.length < 1) {
                            throw "no-selection";
                        }

                        var nodePath = node.getPath();
                        $("#" + settings.id).find('form input[name=path]').val(nodePath);
                        dialog.formDialog('close');

                    } catch (exception) {
                        var errmsg = exception;
                        if (exception == "no-selection") {
                            errmsg = translator.get('test-case.testautomation.popup.error.noselect');
                        }
                        dialog.formDialog('showError', errmsg);
                    }
                };

                // ************ events *********************

                $('.assigned-script').on('click',function(evt){

                                	var $toto = $(evt.currentTarget);
                                	console.log("evt" +  $toto);

                                });

                dialog.on('formdialogconfirm', submit);

                dialog.on('formdialogcancel', function () {
                    dialog.formDialog('close');
                });

                dialog.on("formdialogopen", function () {
					dialog.initAjax = initDialogCache();

                });

                dialog.on('formdialogclose', function () {
                    if (dialog.initAjax) {
                        dialog.initAjax.abort();
                    }
                });
            },

            changeNumberSelectedRows: function (number) {
                $("#selectedRows").text(number);
            },

            selectAll: function (table) {
                var rows = table.fnGetNodes();
                var ids = [];
                var self = this;
                $(rows).each(function (index, row) {
                    var tcId = parseInt($('.entity_id', row).text(), 10);

                    var $row = $(row);
                    var checkbox = $row.find("input[type=checkbox]");
                    if (checkbox[0] !== undefined) {
                        ids.push(tcId);
                        checkbox[0].checked = true;
                        var store = self.storage.get(self.key);
                        if (store === undefined) {
                            var tab = [];
                            tab.push(tcId);
                            self.storage.set(self.key, tab);
                        } else {
                            if (checkbox[0].checked) {
                                store.push(tcId);

                            } else {
                                var idx = store.indexOf(tcId);
                                store.splice(idx, 1);
                            }
                            self.storage.set(self.key, store);
                        }

                    }

                });
                table.selectRows(ids);
                this.changeNumberSelectedRows(table.getSelectedRows().length);
            },

            deselectAll: function (table) {
                table.deselectRows();
                var rows = table.fnGetNodes();
                $(rows).each(function (index, row) {
                    var $row = $(row);
                    var checkbox = $row.find("input[type=checkbox]");
                    if (checkbox[0] !== undefined) {
                        checkbox[0].checked = false;
                    }

                });

                this.storage.remove(this.key);
                this.changeNumberSelectedRows(table.getSelectedRows().length);
            },

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-global").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            getSelectedTcIds: function (table) {
                var selectedRows = table.getSelectedRows();
                var datas = table.fnGetData();
                var ids = [];
                $(selectedRows).each(function (index, data) {
                    var idx = data._DT_RowIndex;
                    var tcId = datas[idx]["entity-id"];
                    ids.push(tcId);
                });
                return ids;
            },

            checkScriptAutoIsAbsent: function (table) {
                var count = 0;
                var selectedRows = table.getSelectedRows();
                var datas = table.fnGetData();
                var self = this;
                $(selectedRows).each(function (index, data) {
                    var idx = data._DT_RowIndex;
                    var script = data.cells[10].lastChild.nodeValue;
                    var status = datas[idx]["status"];
                    var format = datas[idx]["format"];
                    if (self.noScriptAutoForAllowedStatus(script, status, format)) {
                        count = count + 1;
                    }
                });


                return count;
            },

            noScriptAutoForAllowedStatus: function (script, status, format) {
                var allowedStatusForAutomation = [translator.get('automation-request.request_status.TRANSMITTED'),
                translator.get('automation-request.request_status.AUTOMATION_IN_PROGRESS'),
                translator.get('automation-request.request_status.AUTOMATED')];
                return _.contains(allowedStatusForAutomation, status)
                    && (script === null || script === "-" || script === " ")
                    && format.toLowerCase() !== translator.get('test-case.format.gherkin').toLowerCase();
            },

            updateStatus: function (table, status) {
                var tcIds = this.getSelectedTcIds(table);
                if (tcIds.length === 0 || tcIds === undefined) {
                    notification.showWarning(translator.get("automation.notification.selectedRow.none"));
                } else {

                    $.ajax({
                        url: squashtm.app.contextRoot + 'automation-requests/' + tcIds,
                        method: 'POST',
                        data: {
                            "id": "automation-request-status",
                            "value": status
                        }
                    }).success(function () {
                        table.refresh();
                    });
                }
                this.storage.remove(this.key);
                this.deselectAll(table);
            },

			trySquashTAScriptAssociation : function (tcIds) {
				return $.ajax({
							url: squashtm.app.contextRoot + 'automation-requests/associate-TA-script',
							method: 'POST',
							data: {
								"tcIds": tcIds
							}
						});
			},

            actions: function (table, url) {
                var tcIds = this.getSelectedTcIds(table);

                if (tcIds.length === 0 || tcIds === undefined) {
                    notification.showWarning(translator.get("automation.notification.selectedRow.none"));
                } else {
                    $.ajax({
                        url: squashtm.app.contextRoot + url,
                        method: 'POST',
                        data: {
                            "tcIds": tcIds
                        }
                    }).success(function () {
                        table.refresh();
                    });
                }
                this.storage.remove(this.key);
                this.deselectAll(table);
            },

            bindButtons: function () {
                var self = this;
                var domtable = $("#automation-table").squashTable();

                $("#filter-global-button").on("click", function () {
                    domtable.toggleFiltering();
                });

                $("#select-global-button").on("click", function () {
                    self.selectAll(domtable);
                });

                $("#deselect-global-button").on("click", function () {
                    self.deselectAll(domtable);
                });

                $("#progress-automation-button").on("click", function () {
                    self.updateStatus(domtable, "AUTOMATION_IN_PROGRESS");
                });

                $("#automated-automation-button").on("click", function () {
                    var tcIds = self.getSelectedTcIds(domtable);
                    if (tcIds.length === 0 || tcIds === undefined) {
                        notification.showWarning(translator.get("automation.notification.selectedRow.none"));
                    } else {
											self.trySquashTAScriptAssociation(tcIds).done(function(map){
												if(Object.keys(map).length === 0){
													self.updateStatus(domtable, "AUTOMATED");
												} else {
													domtable.refresh();
													notification.showWarning(translator.get("automation.notification.script.none"));
												}
											});
										}
                });

                $("#rejected-automation-button").on("click", function () {
                    self.updateStatus(domtable, "REJECTED");
                });

                $("#assigned-automation-button").on("click", function () {
                    self.actions(domtable, "automation-requests/assignee");
                });

                $("#unassigned-automation-button").on("click", function () {
                    self.actions(domtable, "automation-requests/unassigned");
                });
            }
        });

        return View;
    });

