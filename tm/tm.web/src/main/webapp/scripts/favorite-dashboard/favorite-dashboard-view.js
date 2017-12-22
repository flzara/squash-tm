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
define(["backbone","custom-report-workspace/views/dashboardView","home-workspace/default-dashboard-view","user-account/user-prefs","app/AclModel","tree","custom-report-workspace/utils"], 
    function(Backbone,DashboardView,CantShowView,userPrefs,AclModel,zetree, chartUtils) {
    'use strict';
     var View = Backbone.View.extend({
            el: "#favorite-dashboard-wrapper",

            initialize : function(options) {
                this.canShowDashboard = squashtm.workspace.canShowFavoriteDashboard;
                this.isMilestoneDashboard = options.isMilestoneDashboard || false;
                this.initializeEvents();
                this.tree = zetree.get();
                this.initView();
                this.model = Backbone.Model.extend({timestamp : new Date()});
                
            },
            events : {
                "click .favorite-dashboard-refresh-button": "triggerRefresh",
                "click .show-default-dashboard-button": "showDefault"
            },

            initView : function() {
                //setting a global param so the workspace can keep trace of view all ready loaded
                squashtm.workspace.favoriteViewLoaded = true;
                 var selected =  this.tree.jstree("get_selected");
                    if(selected.length > 1){
                        squashtm.workspace.multipleSelectionDashboard = true;
                    } else {
                        squashtm.workspace.multipleSelectionDashboard = false;
                    }
                if(this.canShowDashboard){
                    this.showDashboard();
                } else {
                    this.activeView = new CantShowView();
                }
            },

            showDashboard : function() {
                var id = userPrefs.getFavoriteDashboardIdInWorkspace();
                var dynamicScopeModel = this.generateDynamicScopeModel();

                if(id){
                    id = Number(id);
                    var modelDef = Backbone.Model.extend({
                        defaults: {
                            id: id,
                            showInClassicWorkspace :true,
                            dynamicScopeModel : dynamicScopeModel
                        }
                    });

                    var activeModel = new modelDef();
                    var acls = new AclModel({type: "custom-report-library-node", id: id});

                    this.activeView = new DashboardView({
                        model: activeModel,
                        acls: acls
                    });
                }
        
            },

            generateDynamicScopeModel : function() {
                var selected =  this.tree.jstree("get_selected");
                var self = this;
                var dynamicScopeModel = {
                    testCaseLibraryIds : self.filterByType(selected,"test-case-libraries"),
                    testCaseFolderIds : self.filterByType(selected,"test-case-folders"),
                    testCaseIds : self.filterByType(selected,"test-cases"),
                    requirementLibraryIds : self.filterByType(selected,"requirement-libraries"),
                    requirementFolderIds : self.filterByType(selected,"requirement-folders"),
                    requirementIds : self.filterByType(selected,"requirements"),
                    campaignFolderIds : self.filterByType(selected,"campaign-folders"),
                    campaignIds : self.filterByType(selected,"campaigns"),
                    iterationIds : self.filterByType(selected,"iterations"),
                    milestoneDashboard : self.isMilestoneDashboard,
                    workspaceName : self.getCurrentWorkspace()
                };
                return dynamicScopeModel;
            },

            filterByType : function(selectedNodes,type) {
                var selector = "[restype='" + type + "']";
                var nodeIds = selectedNodes.filter(selector).map(function(i,e){
					return $(e).attr("resid");
				}).get();

                return _.map(nodeIds,function(id) {
                    return parseInt(id);
                });
            },

            initializeEvents : function() {
                var wreqr = squashtm.app.wreqr;
                var self = this;
			    wreqr.on("favoriteDashboard.reload", function () {
                    //removing the active view and reinitialize a dashboard with new selection
                    self.activeView.remove();
                    self.$el.html('<div id="contextual-content-wrapper" class="dashboard-grid-in-classic-workspace ui-corner-all"> </div>');
                    self.initView();
                });

                //removing the favorite dashboard backbone view when a loadWith event is fired by contextualContent.js in each workspace
                wreqr.on("contextualContent.loadWith", function () {
                    self.remove();
                });
            },

            triggerRefresh : function() {
               var wreqr = squashtm.app.wreqr;
               wreqr.trigger("favoriteDashboard.reload");
            },

            showDefault : function() {
                var wreqr = squashtm.app.wreqr;
                var self = this;

                var callback = function() {
                    if(!!self.isMilestoneDashboard){
                        wreqr.trigger("favoriteDashboard.milestone.showDefault");
                    } else {
                        wreqr.trigger("favoriteDashboard.showDefault");
                    }
                    
                    //destroying the backbone view
                    self.remove();
                };

                userPrefs.chooseDefaultContentInWorkspace(callback);
              
            },

            remove :  function() {
                squashtm.workspace.favoriteViewLoaded = false;
                 //removing listener on event bus
                var wreqr = squashtm.app.wreqr;
                wreqr.off("favoriteDashboard.reload");
                wreqr.off("contextualContent.loadWith");
                this.activeView.remove();
                Backbone.View.prototype.remove.call(this);
            },

            getCurrentWorkspace : function() {
               return chartUtils.getCurrentWorkspace();
            }

           
    });

    return View;
});