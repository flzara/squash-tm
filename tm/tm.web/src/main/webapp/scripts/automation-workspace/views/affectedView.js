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
define(["jquery", "underscore", "backbone", "handlebars", "squashtable", "squash.translator"],
    function ($, _, Backbone, Handlebars) {
        "use strict";

        var View = Backbone.View.extend({

            el: "#contextual-content-wrapper",
            initialize: function () {
                console.log(this)
                var self = this;
                this.render();
                var table = self.getAffectedTable();

                table.squashTable(self.getDatatableSettings());

                self.bindButtons();
            },

            getAffectedTable: function () {
                return this.$el.find("#affected-table");
            },

            getDatatableSettings: function () {
                console.log(this.loadData());
                
                var datatableSettings = {
                    //TODO récupéter la liste des demandes dynamiquement
                    aaData: [{
                        "entity-index": "2",
                        "project-name": "Projet",
                        "entity-id": "4",
                        "reference": "refe",
                        "name": "titre",
                        "format": "gherkin",
                        "created-by": "admin",
                        "transmitted-by": "testeur",
                        "transmitted-on": "10/02/2015",
                        "priority": "2",
                        "status": "En cours",
                        "assigned-to": "admin",
                        "assigned-on": "22/10/2018",
                        "image": "image"
                    }],
                    
                    bServerSide: false,
                    bFilter: true
                };
                console.log(datatableSettings)
                return datatableSettings;
            },

            loadData: function() {

                return $.ajax({
                    method: "GET",
                    contentType: "application/json",
                    url: squashtm.app.contextRoot + "automation-workspace/automation-request",
                    succes: function(response) {
                        console.log("La réponse" + response);
                    }
                })
            },

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-affected").html();
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
