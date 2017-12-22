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
//the 'tree-node' plugin will be automatically applied when required
define(['jquery', './jstree-dnd-override', './continuous-shift-select-plugin','./squash-plugin', './workspace-tree-plugin', './tree-picker-plugin' , './conditional-select-plugin','./jstree-dnd-customreport-override', './tree-node', 'jstree'], function($, applyDndOverride, applyContinuousShiftSelectPlugin, applySquashPlugin, applyWorkspacePlugin, applyTreePickerPlugin, applyConditionalSelectPlugin, applyCustomReportDnD){

    return {

        configure : function(type, settings){
            switch(type){
      case 'custom-report-workspace-tree' :
        applyDndOverride(settings);
            applySquashPlugin();
            applyContinuousShiftSelectPlugin();
            applyWorkspacePlugin();
            applyConditionalSelectPlugin();
            applyCustomReportDnD();
            break;

            case 'workspace-tree' :
        applyDndOverride(settings);
                applySquashPlugin();
                applyContinuousShiftSelectPlugin();
                applyWorkspacePlugin();
                applyConditionalSelectPlugin();
                break;

            case 'tree-picker' :
                applySquashPlugin();
                applyContinuousShiftSelectPlugin();
                applyTreePickerPlugin();
                applyConditionalSelectPlugin();
                break;

            case 'simple-tree' :
                applySquashPlugin();

                break;
            case 'search-tree' :
                 applySquashPlugin();
                 applyContinuousShiftSelectPlugin();
                 applyWorkspacePlugin();
                 applyConditionalSelectPlugin();

            break;
            default :
                throw "'"+type+"' is not a valid tree profile";
            }
        }

    };

});
