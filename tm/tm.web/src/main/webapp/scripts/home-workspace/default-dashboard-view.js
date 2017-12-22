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
/**
 * view used if user haven't defined a favorite dashboard or if the defined dashboard doesn't exist anymore
 */
define(["jquery", "underscore", "backbone", "handlebars"],
	function ($, _, Backbone,Handlebars) {
		"use strict";

	    var View = Backbone.View.extend({
            el: "#contextual-content-wrapper",
            initialize :  function () {
                this.render();
            },
            
            events: {
               
            },
            
            render : function () {
                this.$el.html("");
                var source = $("#tpl-default-dashboard").html();
				var template = Handlebars.compile(source);
                this.$el.append(template());
            }
        
        });
       
        return View;
    });