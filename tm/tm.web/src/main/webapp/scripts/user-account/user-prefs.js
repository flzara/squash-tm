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
define(["underscore","workspace.routing"], function(_,urlBuilder) {
    'use strict';
    var FAVORITE_DASHBOARD_KEY = "squash.core.favorite.dashboard.";
    var WORKSPACE_CONTENT_KEY = "squash.core.dashboard.content.";
    var WORKSPACE_CONTENT_DASHBOARD_VALUE = "dashboard";
    var WORKSPACE_CONTENT_DEFAULT_VALUE = "default";
    var VALID_WORKSPACE = ["home","tc","requirement","campaign"];
		var BUGTRACKER_MODE_KEY = ["squash.bug.tracker.mode"];
     //***************** GET PREF METHODS *****************************

    function getAllPrefs(){
        return squashtm.app.userPrefs || null;
    }

    function getPref(key) {
        var prefs = getAllPrefs();
        if (prefs) {
            return prefs[key] || null;
        } else {
            return {};
        }
    }

	function getBugtrackerMode(){
		var	key = BUGTRACKER_MODE_KEY;
		return getPref(key);
	}

	function getFavoriteDashboardIdInWorkspace(){
        var workspace = getWorkspace();
        return getFavoriteDashboardId(workspace);
    }

    function getFavoriteDashboardId(workspace) {
       var key = getWorkspaceKey(workspace, FAVORITE_DASHBOARD_KEY);
       return getPref(key);
    }

    function shouldShowFavoriteDashboardInWorkspace (){
        var workspace = getWorkspace();
        var key = getWorkspaceKey(workspace, WORKSPACE_CONTENT_KEY);
        return getPref(key) === WORKSPACE_CONTENT_DASHBOARD_VALUE;
    }

    //***************** SET PREF METHODS *****************************

    function getWorkspaceContentPreferenceKey (workspace){
      return getWorkspaceKey(workspace, WORKSPACE_CONTENT_KEY);
    }

    function getWorkspaceKey(workspace, root){
        if(_.contains(VALID_WORKSPACE,workspace)){
            return root + workspace;
        }
        throw ("Unknown workspace type " + workspace);
    }

    function chooseDefaultContentInWorkspace(callback){
        var workspace = getWorkspace();
        updateFavoriteContentInWorkspace(workspace, WORKSPACE_CONTENT_DEFAULT_VALUE, callback);
    }

    function chooseFavoriteDashboardInWorkspace(callback){
        var workspace = getWorkspace();
        updateFavoriteContentInWorkspace(workspace, WORKSPACE_CONTENT_DASHBOARD_VALUE, callback);
    }

    function updateFavoriteContentInWorkspace(workspace,value,callback){
        var key = getWorkspaceContentPreferenceKey(workspace);
        updateUserPreference(key,value,callback);
    }

    //ajax reqwuest to update user preference.
    //callback should be a function called after request completed.
    function updateUserPreference (key, value, callback){
        var url = urlBuilder.buildURL("user-pref-update");
               var data = {
                   key : key,
                   value : value
               };

               $.ajax({
                   'type' : 'POST',
					'contentType' : 'application/json',
					'url':url,
					'data': JSON.stringify(data)
               }).success(function(response) {
                   //updating the pref client side also
                   if(squashtm.app.userPrefs){
                       squashtm.app.userPrefs[data.key] = data.value;
                   }
                   //calling callback if callback provided
                   if(callback && typeof callback === "function"){
                        callback();
                   }
               });
    }

    //***************** UTIL METHODS *****************************

    function getWorkspace(){
        if(squashtm.app.testCaseWorkspaceConf){return "tc";}

         else if(squashtm.app.requirementWorkspaceConf){return "requirement";}

         else if(squashtm.app.campaignWorkspaceConf){return "campaign";}

         throw ("you must call this function inside a workspace. All workspace conf are undefined in your context");
    }





    return {
        getAllPrefs : getAllPrefs,
        getPref : getPref,
        getFavoriteDashboardIdInWorkspace : getFavoriteDashboardIdInWorkspace,
        getWorkspaceContentPreferenceKey : getWorkspaceContentPreferenceKey,
				getBugtrackerMode :getBugtrackerMode,
        chooseDefaultContentInWorkspace : chooseDefaultContentInWorkspace,
        chooseFavoriteDashboardInWorkspace : chooseFavoriteDashboardInWorkspace,
				shouldShowFavoriteDashboardInWorkspace : shouldShowFavoriteDashboardInWorkspace
    };
});
