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
define(["jquery", "underscore", "backbone", "handlebars"],
    function ($, _, Backbone, Handlebars) {
        "use strict";

        var View = Backbone.View.extend({
            el: "#contextual-content-wrapper",
            initialize: function () {
                this.render();
                this.bindButtons();
                this.getDatatable().squashTable(this.getDatatableSettings());
            },

            events: {

            },

            getDatatableSettings: function () {
                
                var datatableSettings = {
                    
                    //TODO récupéter la liste des demandes dynamiquement
                    aaData: [{
                        "id": "2",
                        "project": "Projet",
                        "reference": "refe",
                        "label": "titre",
                        "format": "gherkin",
                        "createdby": "admin",
                        "transmittedby": "testeur",
                        "transmittedon": "10/02/2015",
                        "priority": "2",
                        "status": "En cours",
                        "affectedon": "22/10/2018",
                        "image": "image"
                    }],
                    aoColumnDefs: [{
                        'bVisible': true,
                        'bSortable': false,
                        'aTargets': [0]
                    },
                    {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [1]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [2]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [3]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [4]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [5]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [6]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [7]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [8]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [9]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [10]
                    }, {
                        'bVisible': true,
                        'bSortable': true,
                        'aTargets': [11]
                    }, {
                        'bVisible': true,
                        'bSortable': false,
                        'aTargets': [12]
                    },],
                    bServerSide: false,
                    bFilter: true
                };
                return datatableSettings;
            },

            getDatatable: function() {
                return this.$el.find("#traitment-table");
            },

            render: function () {
                this.$el.html("");
                var source = $("#tpl-show-traitment").html();
                var template = Handlebars.compile(source);

                this.$el.append(template);
            },

            bindButtons: function () {
                $("#select-traitment-button").on("click", function () {
                    console.log("Traitment select");
                });
                $("#filter-traitment-button").on("click", function () {
                    console.log("Traitment filter");
                });
                $("#assigned-traitment-button").on("click", function () {
                    console.log("Traitment assigned");
                });
            }


        });

        return View;
    });
