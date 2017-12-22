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
if (window.console === undefined) {
	window.console = {};
}
if (window.console.log === undefined) {
	window.console.log = function() {/*NOOP*/};
}
if (window.console.trace === undefined){
	window.console.trace = function() {/*NOOP*/};
}

var contextRoot;
if (window.squashtm !== undefined && window.squashtm.app !== undefined && window.squashtm.app.contextRoot !== undefined) {
	contextRoot = squashtm.app.contextRoot;
	if (contextRoot.lastIndexOf("/") < 1) {
		contextRoot = contextRoot + "/";
	}
} else {
	contextRoot = "/squash/";
	window.console.log("WARN could not find squashtm.app.contextRoot, context root set to 'squash'");
}
var CKEDITOR_BASEPATH = contextRoot + "scripts/ckeditor/";

requirejs.config({
    packages: [
        {
            name: 'contextual-content-handlers',
            main: 'ctxt-handlers-main'
        },
        {
            name: 'file-upload',
            main: 'file-upload-main'
        },
        {
            name: 'squashtable',
            main: 'squashtable-main'
        },
        {
            name: 'tree',
            main: 'tree-main'
        },
        //cufs
        {
            name: 'custom-field-binding',
            main: 'cuf-binding-main'
        },
        {
            name: 'custom-field-values',
            main: 'cuf-values-main'
        },
        //entities management
        {
            name: 'projects-manager',
            main: 'projects-manager-main'
        },
        {	name: 'requirement-folder-management',
        	main: 'rf-management-main'
        },
        {	name: 'requirement-library-management',
        	main:'rl-management-main'
        },
        {
            name: 'test-case-management',
            main: 'tc-management-main'
        },
        {
            name: 'test-case-folder-management',
            main: 'tcf-management-main'
        },
        {
            name: 'test-case-library-management',
            main: 'tcl-management-main'
        },
        {
            name: 'iteration-management',
            main: 'iter-management-main'
        },
        {
            name: 'test-suite-management',
            main: 'ts-management-main'
        },
        {
            name: 'campaign-management',
            main: 'campaign-management-main'
        },
        {
            name: 'campaign-folder-management',
            main: 'cf-management-main'
        },

        //workspaces
        {
            name: 'tc-workspace',
            main: 'tc-workspace-main'
        },
        {
            name: 'req-workspace',
            main: 'req-workspace-main'
        },
        {
            name: 'camp-workspace',
            main: 'camp-workspace-main'
        },
        // charts rendering
        {
            name: 'charts-rendering',
            main: 'chart-render-main',
            location: 'charts/rendering'
        },

        {
            name : 'favorite-dashboard',
            main : 'favorite-dashboard-main'
        }

    ],
    /*
     * rules for paths naming:
     * * third party lib: unversionned lib name
     * * non AMD squash lib: replace "squashtm" by "squash" in js file name and remove any unrequired "ext" suffix.
     */
    paths: {
        /*
         * CAVEAT: as we defined a "jquery" path, any module named
         * "jquery/my.module" will be interpolated as
         * "/lib/jquery/../my.module"
         */

        "worker": "lib/requirejsWorker/worker",
        "workerWithoutFake": "lib/requirejsWorker/workerWithoutFake",
        "worker-fake": "lib/requirejsWorker/fake-worker",
//				"datatables" : "datatables/jquery.dataTables",
        "datatables": "datatables/jquery.dataTables.min",
        //lib
        "linq": "lib/openXML/linq",
        "ltxml": "lib/openXML/ltxml",
        "ltxml-extensions": "lib/openXML/ltxml-extensions",
        "jszip": "lib/openXML/jszip",
        "jszip-ie9-support": "lib/openXML/jszip-ie9-support",
        "jszip-utils": "lib/openXML/jszip-utils",
        "jszip-deflate": "lib/openXML/jszip-deflate",
        "jszip-inflate": "lib/openXML/jszip-inflate",
        "jszip-load": "lib/openXML/jszip-load",
        "openxml": "lib/openXML/openxml",
        "docxgen": "lib/docxgen/docxgen.min",
        "FileSaver": "lib/docxgen/FileSaver.min",
        "domReady": "lib/require/domReady",
//				"jquery" : "lib/jquery/jquery-2.1.1",
        "jquery": "lib/jquery/jquery-2.1.1.min",
//				"jqueryui" : "lib/jquery/jquery-ui-1.9.2.custom",
        "jqueryui": "lib/jquery/jquery-ui-1.9.2.custom.min",
        "handlebars": "lib/handlebars/handlebars-v4.0.2",
//				"underscore" : "lib/underscore/underscore-1.7.0",
        "underscore": "lib/underscore/underscore-1.8.3-min",
//				"backbone" : "lib/backbone/backbone",
        "backbone": "lib/backbone/backbone-min",
//				"backbone.validation" : "lib/backbone/backbone-validation-amd",
        "backbone.validation": "lib/backbone/backbone-validation-amd-min",
//				"backbone.wreqr" : "lib/backbone/backbone.wreqr",
        "backbone.wreqr": "lib/backbone/backbone.wreqr.min",
        "jqplot-core": "lib/jqplot/jquery.jqplot",
        // "jqplot-core" : "lib/jqplot/jquery.jqplot.min",
//				"jqplot-pie" : "lib/jqplot/plugins/jqplot.pieRenderer",
        "jqplot-pie": "lib/jqplot/plugins/jqplot.pieRenderer.min",
//				"jqplot-donut" : "lib/jqplot/plugins/jqplot.donutRenderer",
        "jqplot-donut": "lib/jqplot/plugins/jqplot.donutRenderer.min",
//				"jqplot-dates" : "lib/jqplot/plugins/jqplot.dateAxisRenderer",
        "jqplot-dates": "lib/jqplot/plugins/jqplot.dateAxisRenderer.min",
//				"jqplot-highlight": "lib/jqplot/plugins/jqplot.highlighter",
        "jqplot-highlight": "lib/jqplot/plugins/jqplot.highlighter.min",
//				"jqplot-category" : "lib/jqplot/plugins/jqplot.categoryAxisRenderer",
        "jqplot-category": "lib/jqplot/plugins/jqplot.categoryAxisRenderer.min",
        //"jqplot-canvas-fonts" : "lib/jqplot/plugins/jqplot.canvasTextRenderer",
        "jqplot-canvas-fonts": "lib/jqplot/plugins/jqplot.canvasTextRenderer.min",
        //"jqplot-canvas-label" : "lib/jqplot/plugins/jqplot.canvasAxisLabelRenderer",
        "jqplot-canvas-label": "lib/jqplot/plugins/jqplot.canvasAxisLabelRenderer.min",
        //"jqplot-canvas-ticks" : "lib/jqplot/plugins/jqplot.canvasAxisTickRenderer",
        "jqplot-canvas-ticks": "lib/jqplot/plugins/jqplot.canvasAxisTickRenderer.min",
//			"jqplot-bar" : "lib/jqplot/plugins/jqplot.barRenderer",
        "jqplot-bar": "lib/jqplot/plugins/jqplot.barRenderer.min",
        //"jqplot-legend" : "lib/jqplot/plugins/jqplot.enhancedLegendRenderer",
        "jqplot-legend": "lib/jqplot/plugins/jqplot.enhancedLegendRenderer.min",
        // "jqplot-point-labels" : "lib/jqplot/plugins/jqplot.pointLabels",
        "jqplot-point-labels": "lib/jqplot/plugins/jqplot.pointLabels.min",
//				"moment" : "lib/momentjs/moment-with-locales.min",
        "moment": "lib/momentjs/moment-with-locales.min",
        "is": "lib/is/is.min",
        //cke
        "ckeditor": "ckeditor/ckeditor",
        "jquery.ckeditor": "ckeditor/adapters/jquery",
        //jeditable
//				"jeditable" : "jquery/jquery.jeditable.authored",
        "jeditable": "jquery/jquery.jeditable.mini.authored",
        "jeditable.ckeditor": "jquery/jquery.jeditable.ckeditor",
        "jeditable.datepicker": "jquery/jquery.jeditable.datepicker",
        "jeditable.simpleJEditable": "squashtest/classes/SimpleJEditable",
        "jeditable.selectJEditable": "squashtest/classes/SelectJEditable",
        "jeditable.selectJEditableAuto": "squashtest/classes/SelectJEditableAuto",
        //jquery
        "jstree": "jquery/jquery.jstree",
        "jform": "jquery/jquery.form",
//				"jform" : "jquery/jquery.form.min",
        "jquery.squash.milestoneDialog": "milestones/jquery.squash.milestoneDialog",
        "jquery.dialog-patch": "jquery/jquery.dialog-patch",
        "jquery.generateId": "jquery/jquery.generateId",
        "jquery.hotkeys": "jquery/jquery.hotkeys-0.8",
        "jquery.timepicker": "jquery/jquery-ui-timepicker-addon",
        "jquery.cookie": "jquery/jquery.cookie",
//				"jquery.tagit" : "jquery/tag-it",
        "jquery.tagit": "jquery/tag-it.min",
        //"jquery.gridster" : "jquery/jquery.gridster",
        "jquery.gridster": "jquery/jquery.gridster",
        "jstree-dnd-customreport-override": "tree/workspace-tree-conf/jstree-dnd-customreport-override",
        //squashtest
        "jquery.squash": "squashtest/jquery.squash.plugin",
        "jquery.squash.rangedatepicker": "squashtest/jquery.squash.rangedatepicker",
        "jquery.squash.togglepanel": "squashtest/jquery.squash.togglepanels",
        "jquery.squash.messagedialog": "squashtest/jquery.squash.messagedialog",
        "jquery.squash.confirmdialog": "squashtest/jquery.squash.confirmdialog",
        "jquery.squash.oneshotdialog": "squashtest/jquery.squash.oneshotdialog",
        "jquery.squash.formdialog": "squashtest/jquery.squash.formdialog",
        "jquery.squash.bindviewformdialog": "squashtest/jquery.squash.bindviewformdialog",
        "jquery.squash.squashbutton": "squashtest/jquery.squash.squashbutton",
        "jquery.squash.jedpassword": "squashtest/jquery.squash.jedpassword",
        "jquery.squash.jeditable": "squashtest/jquery.squash.jeditable.ext",
        "squash.session-pinger": "squashtest/jquery.squash.session-pinger",
        "jquery.squash.tagit": "squashtest/jquery.squash.tagit",
        "jquery.squash.buttonmenu": "squashtest/jquery.squash.buttonmenu",
        "jquery.switchButton": "jquery/jquery.switchButton",
        "jquery.squash.add-attachment-popup": "squashtest/add-attachment-popup",
        "jquery.squash.datepicker": "datepicker/jquery.squash.datepicker",
        "jquery.squash.datepicker-auto": "datepicker/jquery.squash.datepicker-auto",
        "jquery.squash.fragmenttabs": "squash/squash.fragmenttabs",
        //squash
        "squash.cssloader": "squash/squash.cssloader",
        "squash.translator": "squash/squash.translator",
        "squash.resizer": "squash/squash.tree-page-resizer",
        "squash.basicwidgets": "squash/squash.basicwidgets",
        "squash.attributeparser": "squash/squash.attributeparser",
        "squash.configmanager": "squash/squash.configmanager",
        "squash.dateutils": "squash/squash.dateutils",
        "squash.statusfactory": "squash/squash.statusfactory",
        //workspace
        "workspace.tree-node-copier": "workspace/workspace.tree-node-copier",
        "workspace.tree-event-handler": "workspace/workspace.tree-event-handler",
        "workspace.permissions-rules-broker": "workspace/workspace.permissions-rules-broker",
        "workspace.contextual-content": "workspace/workspace.contextual-content",
        "workspace.event-bus": "workspace/workspace.event-bus",
        "workspace.storage": "workspace/workspace.storage",
        "workspace.sessionStorage": "workspace/workspace.sessionStorage",
        "workspace.routing": "workspace/workspace.routing",
        "workspace.breadcrumb": "workspace/workspace.breadcrumb",
        "workspace.projects": "workspace/workspace.projects",
        //for plugin compatibility purposes
        "jquery.squash.datatables": "plugin-compatibility/jquery.squash.datatable",
        // contextual content
        "squash.KeyEventListener": "squashtest/classes/KeyEventListener"
    },
    shim: {

        "openxml": {
            deps: ["jszip-load", "jszip-inflate", "jszip-deflate"]
        },

        "jszip": {
            deps: ['jszip-ie9-support']
        },

        "jszip-load": {
            deps: ["jszip"]
        },
        "jszip-inflate": {
            deps: ["jszip"]
        },
        "jszip-deflate": {
            deps: ["jszip"]
        },
        "ckeditor": {
            exports: "CKEDITOR"
        },
        "jquery.ckeditor": {
            deps: ["jquery", "jqueryui", "jquery.dialog-patch", "ckeditor"],
            exports: "jqueryCkeditor"
        },
        "jeditable": {
            deps: ["jquery", "jqueryui"],
            exports: "jeditable"
        },
        "jeditable.ckeditor": {
            deps: ["jeditable", "jquery.ckeditor",
                "jquery.generateId"],
            exports: "jeditableCkeditor"
        },
        "jeditable.datepicker": {
            deps: ["jeditable"],
            exports: "jeditableDatepicker"
        },
        "jeditable.simpleJEditable": {
            deps: ["jquery.squash.jeditable"],
            exports: "SimpleJEditable"
        },
        "jeditable.selectJEditable": {
            deps: ["jquery.squash.jeditable"],
            exports: "SelectJEditable"
        },
        "jstree": {
            deps: ["jquery", "jqueryui", "jquery.hotkeys",
                "jquery.cookie"],
            exports: "jqueryui"
        },
        "jform": ["jquery"],
        "jqueryui": ["jquery"],
        "jquery.generateId": ["jquery"],
        "datatables": ["jqueryui"],
        "jquery.dialog-patch": ["jqueryui"],
        "jquery.squash": {
            deps: ["jquery", "jqueryui"],
            exports: "squashtm.popup"
        },
        "jquery.squash.datepicker": {
            deps: ["jquery", "jqueryui"],
            exports: "SquashDatePicker" // this is a constructor
        },
        "jquery.squash.datepicker-auto": {
            deps: ["jquery", "jqueryui"],
            exports: "DatePickerAuto" // this is a constructor
        },
        "jquery.timepicker": ["jquery", "jqueryui"],
        "jquery.squash.togglepanel": {
            deps: ["jquery", "jqueryui", "jquery.squash.squashbutton"],
            exports: "jquerySquashtmTogglepanel"
        },
        "jquery.squash.messagedialog": {
            deps: ["jquery", "jqueryui"],
            exports: "$.squash"
        },
        "jquery.squash.confirmdialog": {
            deps: ["jquery", "jqueryui"],
            exports: "jquerySquashConfirmDialog"
        },
        "jquery.squash.jeditable": {
            deps: ["jquery", "jeditable", "jeditable.ckeditor"],
            exports: "jquerySquashtmJeditable"
        },
        "jquery.squash.squashbutton": {
            deps: ["jquery", "jqueryui"],
            exports: "$.squash"
        },
        "jquery.cookie": {
            deps: ["jquery"],
            exports: "jQuery.cookie"
        },
        "underscore": {
            exports: "_"
        },
        "jquery.tagit": {
            deps: ["jquery", "jqueryui"],
            exports: "tagit"
        },
        "jquery.switchButton": ["jquery", "jqueryui"],
        // jqplot
        "jqplot-core": ["jquery"],
        "jqplot-pie": ["jquery", "jqplot-core"],
        "jqplot-donut": ["jquery", "jqplot-core"],
        "jqplot-dates": ["jquery", "jqplot-core"],
        "jqplot-category": ["jquery", "jqplot-core"],
        "jqplot-bar": ["jquery", "jqplot-core"],
        "jqplot-legend": ["jquery", "jqplot-core"],
        "jqplot-point-labels": ["jquery", "jqplot-core"],
        "jqplot-canvas-fonts": ["jquery", "jqplot-core"],
        "jqplot-canvas-label": ["jquery", "jqplot-core", "jqplot-canvas-fonts"],
        "jqplot-canvas-ticks": ["jquery", "jqplot-core", "jqplot-canvas-fonts"],
        "jqplot-highlight": {
            deps: ["jquery", "jqplot-core"],
            init: function ($) {
                $.jqplot.config.enablePlugins = true;
                return $;
            }
        },
        "squash.KeyEventListener": {
            deps: ["jquery"],
            exports: "KeyEventListener" // this is a constructor
        }
    }
});
