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

                table.squashTable(self.getDatatableSettings());

                self.bindButtons();
            },

            getAffectedTable: function () {
                return this.$el.find("#assigned-table");
            },

            getDatatableSettings: function () {
                var data = this.loadData().aaData;
                data = this.convertDate(data);
                var datatableSettings = {
                    aaData: data,
                    bServerSide: false,
                    bFilter: true
                };
                return datatableSettings;
            },

            loadData: function () {
                var rep;
                $.ajax({
                    method: "GET",
                    contentType: "application/json",
                    url: squashtm.app.contextRoot + "automation-workspace/automation-request",
                    async: false,
                    success: function (data) {
                        rep = data
                    }
                })
                return rep;
            },

            convertDate: function (data) {
                var format = translator.get('squashtm.dateformat');
                for (var e = 0; e < data.length; e++) {
                    var element = data[e];
                    element["assigned-on"] = dateutils.format(element["assigned-on"], format);
                    element["transmitted-on"] = dateutils.format(element["transmitted-on"], format);
                }
                return data;
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
