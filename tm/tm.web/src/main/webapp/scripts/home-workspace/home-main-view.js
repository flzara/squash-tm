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
define(["jquery", "underscore", "backbone","./welcome-message-view","custom-report-workspace/views/dashboardView","./default-dashboard-view", "workspace.routing","squash.attributeparser","app/AclModel","jquery.switchButton"],
	function ($, _, Backbone,messageView,DashboardView,DefaultDashboardView,urlBuilder,attrparser,AclModel) {
		"use strict";

	    var View = Backbone.View.extend({
            el: "#home-workspace",
            initialize :  function () {
                this.clearView();
                if (squashtm.app.homeWorkspaceConf.shouldShowDashboard) {
                    this.showDashboard();
                } else {
                    this.showMessage();
                }
            },
            
            events: {
                "click #show-favorite-dashboard": "chooseFavoriteDarshboard",
                "click #show-welcome-message" : "chooseWelcomeMessage"
            },
            
            chooseWelcomeMessage : function () {
                this.clearView();
                var url = urlBuilder.buildURL("home.content.message");
                var self = this;
                $.ajax({
                    url: url,
					type: 'post'
                }).success(
                    self.showMessage
                );
            },
            
            chooseFavoriteDarshboard : function () {
            	this.clearView();
                var url = urlBuilder.buildURL("home.content.dashboard");
                var self = this;
                $.ajax({
                    url: url,
					type: 'post'
                }).success(
                    self.showDashboard
                );
            },
            
            showMessage : function () {
                this.activeView = new messageView();
            },
            
            showDashboard : function () {
                var id = squashtm.app.userPrefs["squash.core.favorite.dashboard.home"];
                if(id && squashtm.app.homeWorkspaceConf.canShowDashboard){
                    id = Number(id);
                    var modelDef = Backbone.Model.extend({
                        defaults: {
                            id: id
                        }
                    });

                    var activeModel = new modelDef();
                    var acls = new AclModel({type: "custom-report-library-node", id: id});

                    this.activeView = new DashboardView({
                        model: activeModel,
                        acls: acls
                    });
                } else {
                    this.activeView = new DefaultDashboardView({});
                }
            },
            
            clearView : function () {
                this.$('#home-content').html("<div id='contextual-content-wrapper' style='height: 90%; width:98%; overflow: auto; position:absolute'></div>");
            }
        });
       
        return View;
    });