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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'squash.dateutils', "./filter", "squash.configmanager", "squashtable", "jeditable"],
    function ($, _, Backbone, Handlebars, translator, dateutils, filtermode, confman) {
        "use strict";

        var View = Backbone.View.extend({
            el: "#contextual-content-wrapper",

            initialize: function () {
                this.render();

                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-workspace/automation-request",
                    "aaSorting": [[7, 'desc'], [8, 'asc']],
                    "bDeferRender": true,
                    "aoColumnDefs": [{
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
                    }, {
                        "bSortable": true,
                        "aTargets": [8],
                        "mDataProp": "transmitted-on",
                        "sWidth": "13em"
                    }, {
                        "bSortable": true,
                        "aTargets": [9],
                        "mDataProp": "assigned-on",
                        "sWidth": "12em"
                    }, {
                        "bSortable": true,
                        "aTargets": [10],
                        "mDataProp": "script",
                        "sClass": "assigned-script"
                    }, {
                        "bSortable": false,
                        "aTargets": [11],
                        "mDataProp": "tc-id",
                        "mRender": function (data, type, row, meta) {
                            return `<a href="${squashtm.app.contextRoot}test-cases/${data}/info">Lien</a>`;
                        }
                    }, {
                        "bSortable": false,
                        "aTargets": [12],
                        "mDataProp": "checkbox",
                        "mRender": function (data, type, row, meta) {
                            return '<input type="checkbox" />';
                        },
                        "sClass": 'centered',
                        "sWidth": "2.5em"
                    }],
                    "bFilter": true,

                    fnRowCallback: function (row, data, displayIndex) {
                        var $row = $(row);
                        var edObj = $.extend(true, {}, $.editable.types.text);
                        var edFnButtons = $.editable.types.defaults.buttons;
                        var edFnElements = $.editable.types.text.element;

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
                        var url = `${squashtm.app.contextRoot}test-cases/${entityId}/test-automation/tests`;
                        cell.editable(url, editable);
                    }

                };

                var $table = $("#assigned-table");
                var fmode = filtermode.newInst(datatableSettings);

                $table.data('filtermode', fmode);
                var sqtable = $table.squashTable(datatableSettings);


                function toggleSortmode(locked) {
					/*if (locked) {
						sortmode.disableReorder();
					}
					else {
						sortmode.enableReorder();
					}*/
                }

                toggleSortmode(fmode.isFiltering());

                sqtable.toggleFiltering = function () {
                    var isFiltering = fmode.toggleFilter();
                    toggleSortmode(isFiltering);
                };

                this.bindButtons();
            },

            selectAll: function (table) {
                var rows = table.fnGetNodes();
                var ids = [];
                $(rows).each(function (index, row) {
                    ids.push(parseInt($('.entity_id', row).text(), 10));
                })
                table.selectRows(ids);
            },

            deselectAll: function (table) {
                table.deselectRows();
            },

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-assigned").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            bindButtons: function () {
                var self = this;
                var domtable = $("#assigned-table").squashTable();
                $("#filter-affected-button").on("click", function () {
                    domtable.toggleFiltering();
                });
                $("#select-affected-button").on("click", function () {
                    self.selectAll(domtable);
                });
                $("#deselect-affected-button").on("click", function () {
                    self.deselectAll(domtable);
                });
                $("#start-affected-button").on("click", function () {
                    console.log("Affected start");
                });
                $("#automated-affected-button").on("click", function () {
                        var selectedRows = domtable.getSelectedRows();
                        
                        $(selectedRows).each(function(index, data) {
                            $.ajax({
                                url: `${squashtm.app.contextRoot}automation-request/${parseInt(data.children[2].textContent)}`,
                                method: 'POST'
                            }).success(function() {
                                domtable.refresh();
                            });
                        });
                });
            }


        });

        return View;
    });
