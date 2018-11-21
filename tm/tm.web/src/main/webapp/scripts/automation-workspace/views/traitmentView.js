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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'app/ws/squashtm.notification', "workspace.storage", "./sort", "./filter", "squash.configmanager", "squashtable", "jeditable"],
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
                var requestStatus = ["TRANSMITTED"];
                $.ajax({
                    url: squashtm.app.contextRoot + "automation-workspace/testers/" + requestStatus,
                    method: "GET",
                    // data: {
                    //     requestStatus : JSON.stringify(requestStatus)
                    // }
                }).success(function (data) {
                    //console.log(data)
                })
                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-workspace/automation-requests/traitment",
                    "aaSorting": [[7, 'desc'], [8, 'asc']],
                    "bDeferRender": true,
                    "iDisplayLength": 25,
                    "aoColumnDefs": [{
                        "bSortable": false,
                        "aTargets": [0],
                        "sClass": 'centered select-handle',
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
                        "sWidth": "3em",
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
                    }, {
                        "bSortable": true,
                        "aTargets": [8],
                        "mDataProp": "transmitted-on",
                        "sWidth": "14em"
                    }, {
                        "bSortable": false,
                        "aTargets": [9],
                        "mDataProp": "tc-id",
                        "sClass": "centered",
                        "sWidth": "2.5em",
                        "mRender": function (data, type, row, meta) {
                            return '<a href="' + squashtm.app.contextRoot + 'test-cases/' + data + '/info"><img src="/squash/images/icon-lib/eye.png"></a>';
                        }
                    }, {
                        "bSortable": false,
                        "aTargets": [10],
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
                        "aTargets": [11]
                    }],
                    "bFilter": true,
                    fnRowCallback: function (row, data, displayIndex) {
                        var $row = $(row);
                        var edObj = $.extend(true, {}, $.editable.types.text);
                        var edFnButtons = $.editable.types.defaults.buttons;
                        var edFnElements = $.editable.types.text.element;

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
                        var editable = confman.getStdJeditable();
                        editable.type = 'ta-picker';
                        editable.name = "path";
                        var cell = $row.find('.assigned-script');
                        var entityId = data["entity-id"];
                        var url = squashtm.app.contextRoot + 'test-cases/' + entityId + '/test-automation/tests';
                        cell.editable(url, editable);
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
                    } else {
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

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-traitment").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            actions: function (table, url, status) {
                var requestIds = this.getSelectedRequestIds(table);

                if (requestIds.length === 0 || requestIds === undefined) {
                    notification.showWarning(translator.get("automation.notification.selectedRow.none"));
                } else {
                        $.ajax({
                            url: squashtm.app.contextRoot + url + requestIds,
                            method: 'POST',
                            data: {
                                "id": "automation-request-status",
                                "value": status
                            }
                        }).success(function () {
                            table.refresh();
                        });
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
                    self.actions(domtable, "automation-requests/", "WORK_IN_PROGRESS");
                    self.storage.remove(self.key);
                });
                $("#no-automation-traitment-button").on("click", function () {
                    self.actions(domtable, "automation-requests/", "NOT_AUTOMATABLE");
                    self.storage.remove(self.key);
                });
            }


        });

        return View;
    });
