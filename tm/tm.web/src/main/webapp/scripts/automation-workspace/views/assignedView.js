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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'squash.dateutils', "./filter", "squashtable"],
    function ($, _, Backbone, Handlebars, translator, dateutils, filtermode) {
        "use strict";

        var View = Backbone.View.extend({
        el: "#contextual-content-wrapper",
            initialize: function () {
                this.render();

                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-workspace/automation-request",
                    "aaSorting":[[8,'desc'],[7,'asc']],
                    "bDeferRender" : true,
                    "aoColumnDefs" : [ {
                        "bSortable": false,
                        "aTargets" : [ 0 ],
                        "sClass": 'centered select-handle',
                        "mDataProp" : "entity-index",
                        "sWidth": "2.5em"
					}, {
                        "bSortable": true,
						"aTargets" : [ 1 ],
						"mDataProp" : "project-name"
					},{
                        "bSortable": true,
						"aTargets" : [ 2 ],
                        "mDataProp" : "entity-id",
                        "sWidth": "4em"
					},{
						"bSortable": true,
						"aTargets" : [ 3 ],
						"mDataProp" : "reference"
					},{
                        "bSortable": true,
						"aTargets" : [ 4 ],
						"mDataProp" : "name"
					},{
                        "bSortable": true,
						"aTargets" : [ 5 ],
                        "mDataProp" : "format",
                        "sWidth": "7em"
					},{
                        "bSortable": true,
						"aTargets" : [ 6 ],
						"mDataProp" : "created-by"
					},{
                        "bSortable": true,
						"aTargets" : [ 7 ],
                        "mDataProp" : "transmitted-on",
                        "sWidth": "13em"
					},{
                        "bSortable": true,
						"aTargets" : [ 8 ],
                        "mDataProp" : "priority",
                        "sWidth": "6em"
					},{
                        "bSortable": true,
						"aTargets" : [ 9 ],
                        "mDataProp" : "assigned-on",
                        "sWidth": "12em"
					},{
                        "bSortable": true,
						"aTargets" : [ 10 ],
						"mDataProp" : "script"
					},{
                        "bSortable": false,
						"aTargets" : [ 11 ],
                        "mDataProp" : "tc-id",
                        "mRender": function(data, type, row, meta) {
                            return `<a href="${squashtm.app.contextRoot}test-cases/${data}/info">Lien</a>`;
                        }
					},{
                        "bSortable": false,
						"aTargets" : [ 12 ],
                        "mDataProp" : "checkbox",
                        "mRender": function ( data, type, row, meta ) {
                            return '<input type="checkbox" />';
                        },
                        "sClass": 'centered',
                        "sWidth": "2.5em"
					}],
                    "bFilter": true,
                    
                };
                
                
                
                var $table = $("#assigned-table");
                var fmode = filtermode.newInst(datatableSettings);

                $table.data('filtermode', fmode);
                var sqtable = $table.squashTable(datatableSettings);
                console.log(sqtable)
                
                
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

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-assigned").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            bindButtons: function () {
                var table = this.$el.find("#assigned-table");
                $("#filter-affected-button").on("click", function () {
                    var domtable =  $("#assigned-table").squashTable();
			        domtable.toggleFiltering();
                });
                $("#select-affected-button").on("click", function () {
                });
                $("#desassigned-affected-button").on("click", function () {
                    console.log("Affected desassigned");
                });
                $("#start-affected-button").on("click", function () {
                    console.log("Affected start");
                });
                $("#automated-affected-button").on("click", function () {
                    console.log("Affected automated");
                });
            }


        });

        return View;
    });
