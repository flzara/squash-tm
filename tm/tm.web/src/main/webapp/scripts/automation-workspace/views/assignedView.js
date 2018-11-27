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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'app/ws/squashtm.notification', "workspace.storage", "./sort", "./filter", "squash.configmanager", "tree/plugins/plugin-factory", "squashtable", "jeditable", "jqueryui", "jquery.squash.formdialog"],
    function ($, _, Backbone, Handlebars, translator, notification, storage, sortmode, filtermode, confman, treefactory) {
        "use strict";

        var View = Backbone.View.extend({
            el: "#contextual-content-wrapper",
            key: "checkbox-assigned",
            storage: storage,
            selected: 0,
            initialize: function () {
                this.render();
                var self = this;
                $.ajax({
                    url: squashtm.app.contextRoot + "automation-workspace/count",
                    method: "GET"
                }).success(function (data) {
                    if (data !== 0) {
                        squashtm.app.visible = true;
                        $("#divTable").show();
                        $("#divBtn").hide();
                    } else {
                        squashtm.app.visible = false;
                        $("#divTable").hide();
                        $("#divBtn").show();
                    }
                })

                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-workspace/automation-requests",
                    "aaSorting": [[7, 'desc'], [8, 'asc']],
                    "bDeferRender": true,
                    "iDisplayLength": 25,
                    "aoColumnDefs": [{
                        "bSortable": false,
                        "aTargets": [0],
                        "sClass": 'centered select-handle',
                        "mDataProp": "entity-index"
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
                        "mDataProp": "priority"
                    }, {
                        "bSortable": true,
                        "aTargets": [8],
                        "mDataProp": "transmitted-on"
                    }, {
                        "bSortable": true,
                        "aTargets": [9],
                        "mDataProp": "assigned-on"
                    }, {
                        "bSortable": true,
                        "aTargets": [10],
                        "mDataProp": "script",
                        "sClass": "assigned-script"
                    }, {
                        "bSortable": false,
                        "aTargets": [11],
                        "mDataProp": "tc-id",
                        "sClass": "centered",
                        "sWidth": "2.5em",
                        "mRender": function (data, type, row, meta) {
                            return '<a href="' + squashtm.app.contextRoot + 'test-cases/' + data + '/info" class="table-button edit-pencil"></a>';
                        }
                    }, {
                        "bSortable": false,
                        "aTargets": [12],
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
                        "aTargets": [13]
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
                                $row.addClass("ui-state-row-selected").removeClass("ui-state-highlight");

                            }
                        })
                        edObj.buttons = function (settings, original) {
                            //first apply the original function
                            edFnButtons.call(this, settings, original);

                            // now add our own button
                            var btnChoose = $("<button/>", {
                                'text': translator.get('label.dot.pick'),
                                'id': 'ta-script-picker-button' + data["tc-id"]
                            });

                            var btnRemove = $("<button/>", {
                                'text': translator.get('label.Remove'),
                                'id': 'ta-script-remove-button'
                            });

                            this.append(btnRemove);
                            this.append(btnChoose);

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
                        cell.css({ "font-style": "italic"});
                        var cellId = "assigned-script" + data["tc-id"];
                        var testAutomationUrl = squashtm.app.contextRoot + "test-cases/" + data["tc-id"] + "/test-automation/tests";
                        cell.attr("id", cellId);
                        var settings = {
                            url: testAutomationUrl,
                            id: cellId
                        }
                        cell.on('click', '#ta-script-picker-button' + data['tc-id'], function () {
                            self._initPickerPopup(settings);
                            var popup = $("#ta-picker-popup").formDialog();
                            popup.formDialog('open');
                            return false;//for some reason jeditable would trigger 'submit' if we let go
                        });
                        cell.on('click', '#ta-script-remove-button', function () {
                            var popup = $("#ta-remove-popup").formDialog();
                            popup.formDialog('open');
                            return false;// see comment above
                        });
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
                datatableSettings.customKey = "assigned";
                datatableSettings.testers = squashtm.app.assignableUsers;
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
                $('.DataTables_sort_wrapper').css('height', '100%');
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

            _initPickerPopup: function (settings) {

                var dialog = $("#ta-picker-popup");

                var testAutomationTree = dialog.find(".structure-tree");

                // init

                dialog.formDialog({
                    height: 500
                });

                // cache
                dialog.data('model-cache', undefined);



                // ************ model loading *************************

                var initDialogCache = function () {

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

                    $(window).bind('select_node.jstree', function () {
                        console.log('select_node.jstree');
                    });
                    $(window).bind('click.jstree', function () {
                        console.log('click.jstree');
                    });

                };

                var reset = function () {
                    if (testAutomationTree.jstree('get_selected').length > 0) {
                        testAutomationTree.jstree('get_selected').deselect();
                    }
                };

                // ****************** transaction ************


                var submit = function () {

                    try {

                        var node = testAutomationTree.jstree('get_selected');

                        if (node.length < 1) {
                            throw "no-selection";
                        }

                        var nodePath = node.getPath();
                        $("#" + settings.id).find('form input[name="path"]').val(nodePath);
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

                dialog.on('formdialogconfirm', submit);

                dialog.on('formdialogcancel', function () {
                    dialog.formDialog('close');
                });

                dialog.on("formdialogopen", function () {
                    if (dialog.data('model-cache') === undefined) {
                        dialog.initAjax = initDialogCache();
                    } else {
                        reset();
                    }
                });

                dialog.on('formdialogclose', function () {
                    if (dialog.initAjax) {
                        dialog.initAjax.abort();
                    }

                });
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
                var source = $("#tpl-show-assigned").html();
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