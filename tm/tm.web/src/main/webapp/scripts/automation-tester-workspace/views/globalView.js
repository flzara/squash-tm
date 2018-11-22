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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'app/ws/squashtm.notification', "workspace.storage", "./sort", "./filter", "squash.configmanager", "squashtable", "jeditable", "jquery.squash.formdialog"],
    function ($, _, Backbone, Handlebars, translator, notification, storage, sortmode, filtermode, confman) {
        "use strict";
        
        var View = Backbone.View.extend({
            el: "#contextual-content-wrapper",
            key: "checkbox-tester-global",
            storage: storage,
            selected: 0,
            initialize: function () {
                this.render();
                var self = this;
                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-tester-workspace/automation-request/global",
                    "aaSorting": [[4, 'asc']],
                    "bDeferRender": true,
                    "iDisplayLength": 25,
                    "aoColumns": [{
                        "bSortable": false,
                        "aTargets": [0],
                        "sClass": 'centered select-handle',
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
                        "sWidth": "4em",
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
                        "mDataProp": "format",
                        "sWidth": "7em"
                    }, {
                        "bSortable": true,
                        "aTargets": [6],
                        "mDataProp": "created-by"
                    }, {
                        "bSortable": true,
                        "aTargets": [7],
                        "mDataProp": "priority",
                        "sWidth": "6em"
                    },{
                        "bSortable": true,
                        "aTargets": [8],
                        "mDataProp": "status",
                        "sWidth": "6em"
                    }, {
                        "bSortable": true,
                        "aTargets": [9],
                        "mDataProp": "transmitted-on",
                        "sWidth": "13em"
                    }, {
                        "bSortable": false,
                        "aTargets": [10],
                        "mDataProp": "writable",
                        "sClass": "centered",
                        "sWidth": "2.5em",
                        "mRender": function (data, type, row) {

                            var render = "";
                            if(data) {
                                render = "<a class='table-button edit-pencil'></a>"
                            } else {
                                render = '<a href="' + squashtm.app.contextRoot + 'test-cases/' + row["entity-id"] + '/info"><img src="/squash/images/icon-lib/eye.png"></a>'
                            }

                            return render;
                        }
                    }, {
                        "bSortable": false,
                        "aTargets": [11],
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
                            if (checked) {
                                input = '<input type="checkbox" class="editor-active" checked>';
                                $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight")
                            } else {
                                input = '<input type="checkbox" class="editor-active">';
                            }
                            return input;
                        },
                        "sWidth": "2.5em"
                    }, {
                        "mDataProp": "requestId",
                        "bVisible": false,
                        "aTargets": [12]
                    }],
                    "bFilter": true,
                    fnRowCallback: function (row, data, displayIndex) {
                        var $row = $(row);
                        if ($row.find("input[type=checkbox]")[0].checked) {
                            $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight")
                        }
                        $row.on("change", "input[type=checkbox]", function () {

                            if (this.checked) {
                                $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight")
                            } else {
                                $row.removeClass("ui-state-row-selected").addClass("ui-state-highlight")
                            }
                            var store = self.storage.get(self.key);
                            if (store === undefined) {
                                var tab = [];
                                tab.push(data["tc-id"])
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
                        })

                        $row.on("click", "td.select-handle", function () {
                            if (!$row.hasClass("ui-state-row-selected")) {
                                $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight");

                            }
                        })
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
                    },
                }

                var $table = $("#automation-table");
                datatableSettings.customKey = "tester-global";
                datatableSettings.testers = squashtm.app.testerTransmitted;
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
                sqtable.on('change', function () {

                    if (sqtable.getSelectedRows().length > self.selected) {
                        self.selected = self.selected + 1;
                    } else if (sqtable.getSelectedRows().length < self.selected && self.selected !== 0) {
                        self.selected = self.selected - 1;
                    }
                    self.changeNumberSelectedRows(self.selected);
                });
                self.bindButtons();

                
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
                    ids.push(tcId);
                    var $row = $(row);
                    var checkbox = $row.find("input[type=checkbox]")
                    checkbox[0].checked = true
                    var store = self.storage.get(self.key);
                    if (store === undefined) {
                        var tab = [];
                        tab.push(tcId)
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

                })
                table.selectRows(ids);
                this.changeNumberSelectedRows(table.getSelectedRows().length);
            },

            deselectAll: function (table) {
                table.deselectRows();
                var rows = table.fnGetNodes();
                $(rows).each(function (index, row) {
                    var $row = $(row);
                    var checkbox = $row.find("input[type=checkbox]");
                    checkbox[0].checked = false

                })

                this.storage.remove(this.key);
                this.changeNumberSelectedRows(table.getSelectedRows().length);
            },

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-global").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            getSelectedRequestIds: function (table) {
                var selectedRows = table.getSelectedRows();
                var datas = table.fnGetData();
                var ids = [];
                $(selectedRows).each(function (index, data) {
                    var idx = data._DT_RowIndex;
                    var requestId = datas[idx].requestId
                    ids.push(requestId);
                })
                return ids;
            },

            checkScriptAutoIsPresent: function (table) {
                var selectedRows = table.getSelectedRows();
                var datas = table.fnGetData();
                var scripts = [];
                $(selectedRows).each(function (index, data) {
                    var idx = data._DT_RowIndex;
                    var script = datas[idx].script;
                    if (script == null) {
                        scripts.push(script);
                    }

                })
                return scripts;
            },
            bindButtons: function () {
                var self = this;
                var domtable = $("#automation-table").squashTable();
                $("#filter-affected-button").on("click", function () {
                    domtable.toggleFiltering();
                });
                $("#select-affected-button").on("click", function () {
                    self.selectAll(domtable);
                });
                $("#deselect-affected-button").on("click", function () {
                    self.deselectAll(domtable);
                });
                $("#desassigned-affected-button").on("click", function () {
                    var requestIds = self.getSelectedRequestIds(domtable);
                    if (requestIds.length === 0 || requestIds === undefined) {
                        notification.showWarning(translator.get("automation.notification.selectedRow.none"));
                    } else {

                        $.ajax({
                            url: squashtm.app.contextRoot + 'automation-requests/desassigned/' + requestIds,
                            method: 'POST'
                        }).success(function () {
                            domtable.refresh();
                        });
                    }
                    self.storage.remove(self.key);
                });
                $("#automated-affected-button").on("click", function () {
                    var requestIds = self.getSelectedRequestIds(domtable);
                    var scripts = self.checkScriptAutoIsPresent(domtable);
                    if (requestIds.length === 0 || requestIds === undefined) {
                        notification.showWarning(translator.get("automation.notification.selectedRow.none"));
                    } else if (scripts.length !== 0) {
                        notification.showWarning(translator.get("automation.notification.script.none"));
                    } else {
                        $.ajax({
                            url: squashtm.app.contextRoot + 'automation-requests/' + requestIds,
                            method: 'POST',
                            data: {
                                "id": "automation-request-status",
                                "value": "EXECUTABLE"
                            }
                        }).success(function () {
                            domtable.refresh();
                        });
                    }
                    self.storage.remove(self.key);

                });

                $("#btn-no-assigned").on("click", function () {
                    location.href = "#traitment";
                    $("#tf-traitment-tab a").addClass("tf-selected");
                    $("#tf-assigned-tab a").removeClass("tf-selected");
                });
            }


        });

        return View;
    });
