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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'app/ws/squashtm.notification', "workspace.storage", "../../automation-table/sort", "../../automation-table/filter", "squash.configmanager", "squashtable", "jeditable"],
    function ($, _, Backbone, Handlebars, translator, notification, storage, sortmode, filtermode, confman) {
        "use strict";

        var View = Backbone.View.extend({
            el: "#contextual-content-wrapper",
            key: "checkbox-traitment",
            storage: storage,
            selected: 0,
            initialize: function () {
                this.render();
                var self = this;
                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-workspace/automation-requests/traitment",
                    "aaSorting": [[8, 'desc'], [7, 'desc'], [9, 'desc']],
                    "bDeferRender": true,
                    "iDisplayLength": 25,
                    "aoColumnDefs": [{
                        "bSortable": false,
                        "aTargets": [0],
                        "sClass": 'centered no-select-handle',
                        "mDataProp": "entity-index",
                        "sWidth": "2.5em"
                    },
                    {
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
                        "mRender": function (data, type, row, meta) {
                            if (data === null) { return '-'; }
                            return data;
                        }
                    },{
                        "bSortable": true,
                        "aTargets": [8],
                        "mDataProp": "status"
                    }, {
                        "bSortable": true,
                        "aTargets": [9],
                        "mDataProp": "transmitted-on"
                    }, {
                        "bSortable": false,
                        "aTargets": [10],
                        "mDataProp": "tc-id",
                        "sClass": "centered",
                        "sWidth": "2.5em",
                        "mRender": function (data, type, row, meta) {
                            return '<a href="' + squashtm.app.contextRoot + 'test-cases/' + data + '/info"><img src="/squash/images/icon-lib/eye.png" /></a>';
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
                                $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight")
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

                        })
                        self.changeNumberSelectedRows(self.selected);
                    },
                };
                var $table = $("#automation-table");
                datatableSettings.customKey = "traitment";
                datatableSettings.testers = squashtm.app.traitmentUsers;
                datatableSettings.statuses = squashtm.app.autoReqStatusesTraitment;
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

                    if (sqtable.getSelectedRows().length > self.selected) {
                        self.selected = self.selected + 1;
                    } else if (sqtable.getSelectedRows().length < self.selected && self.selected !== 0) {
                        self.selected = self.selected - 1;
                    }

                    self.changeNumberSelectedRows(self.selected);
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
                    ids.push(tcId);
                    var $row = $(row);
                    var checkbox = $row.find("input[type=checkbox]");
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

                this.storage.remove(this.key)
                this.changeNumberSelectedRows(table.getSelectedRows().length);
            },

            getSelectedTcIds: function (table) {
                var selectedRows = table.getSelectedRows();
                var datas = table.fnGetData();
                var ids = [];
                $(selectedRows).each(function (index, data) {
                    var idx = data._DT_RowIndex;
                    var tcId = datas[idx]["entity-id"]
                    ids.push(tcId);
                })
                return ids;
            },

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-traitment").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            assigned: function (table, url) {
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
                    this.deselectAll(table);
                }
            },

            bindButtons: function () {

                var self = this;
                var domtable = $("#automation-table").squashTable();
                $("#select-traitment-button").on("click", function () {
                    self.selectAll(domtable);
                });

                $("#deselect-traitment-button").on("click", function () {
                    self.deselectAll(domtable);
                });

                $("#filter-traitment-button").on("click", function () {
                    domtable.toggleFiltering();
                });
                $("#assigned-traitment-button").on("click", function () {
                    self.assigned(domtable, "automation-requests/assignee");
                    self.storage.remove(self.key);
                });
            }


        });

        return View;
    });
