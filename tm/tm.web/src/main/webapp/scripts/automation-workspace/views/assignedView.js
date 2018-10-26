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
define(["jquery", "underscore", "backbone", "handlebars", "squash.translator", 'squash.dateutils', "squashtable"],
    function ($, _, Backbone, Handlebars, translator, dateutils) {
        "use strict";

        var View = Backbone.View.extend({
        el: "#contextual-content-wrapper",
            initialize: function () {
                var self = this;
                this.render();
                var table = self.getAffectedTable();
                table.squashTable(self.getDatatableSettings(), self.t1);
                self.bindButtons();
                console.log(self.t1)
            },

            getAffectedTable: function () {
                return this.$el.find("#assigned-table");
            },

            t1: {

				buttons: [{
					tdSelector: '>tbody>tr>td.tc-ic',
					jquery: true,
					//tooltip: translator.get('dialog.unbind-testcase.tooltip'),
					uiIcon: function (row, data) {
						return (data['tc-ic'] !== null) ? 'ui-icon-trash' : 'ui-icon-minus';
					},
					/*
					 * the delete button must be drawn if
					 * - the user can delete and the item was not executed or
					 * - the user can extended delete and item was executed
					 */
					/*condition: function (row, data) {
						return (data['last-exec-on'] === null) ?
							initconf.permissions.deletable :
							initconf.permissions.extendedDeletable;
					},*/
					onClick: function (table, cell) {
						console.log("click")
					}
				}]},

            getDatatableSettings: function () {

                var datatableSettings = {
                    sAjaxSource: squashtm.app.contextRoot + "automation-workspace/automation-request",
                    fnPreDrawCallback: function (settings) {
                    },
                    fnRowCallback: function (row, data, displayIndex) {
                    },
                    aaSorting:[[7,'desc'], [8,'desc']],
                    aoColumnDefs : [ {
                        bSortable: false,
                        aTargets : [ 0 ],
                        sClass: 'centered select-handle',
                        mDataProp : "entity-index",
                        sWidth: "2.5em"
					}, {
                        bSortable: true,
						aTargets : [ 1 ],
						mDataProp : "project-name"
					},{
                        bSortable: true,
						aTargets : [ 2 ],
						mDataProp : "entity-id"
					},{
						bSortable: true,
						aTargets : [ 3 ],
						mDataProp : "reference"
					},{
                        bSortable: true,
						aTargets : [ 4 ],
						mDataProp : "name"
					},{
                        bSortable: true,
						aTargets : [ 5 ],
						mDataProp : "format"
					},{
                        bSortable: true,
						aTargets : [ 6 ],
						mDataProp : "created-by"
					},{
                        bSortable: true,
						aTargets : [ 7 ],
                        mDataProp : "transmitted-on",
                        sWidth: "13em"
					},{
                        bSortable: true,
						aTargets : [ 8 ],
						mDataProp : "priority"
					},{
                        bSortable: true,
						aTargets : [ 9 ],
                        mDataProp : "assigned-on",
                        sWidth: "12em"
					},{
                        bSortable: true,
						aTargets : [ 10 ],
						mDataProp : "script"
					},{
                        bSortable: false,
						aTargets : [ 11 ],
                        mDataProp : "tc-id",
                        render: function(data, type, row, meta) {
                            return "";
                        }
					},{
                        bSortable: false,
						aTargets : [ 12 ],
                        mDataProp : "checkbox",
                        render: function ( data, type, row, meta ) {
                            return '<input type="checkbox" />';
                        },
                        sClass: 'centered',
                        sWidth: "2.5em"
					}],
                    bFilter: true
                };
                return datatableSettings;
            },

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-assigned").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            bindButtons: function () {
                $("#filter-affected-button").on("click", function () {
                    console.log();
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
