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
define(["jquery", "backbone", "underscore", "ace/ace", "workspace.routing"], function ($, Backbone, _, ace, urlBuilder) {
	var ScriptEditorTab = Backbone.View.extend({

		el: "#tab-tc-script-editor",

		//This view is initialized in test-case.jsp, the server model is directly injected in the page.
		initialize: function (options) {
			var serverModel = options.settings;
			this._initializeEditor(serverModel);
			this._initializeModel(serverModel);
		},

		_initializeEditor: function (serverModel) {
			var editor = ace.edit("tc-script-editor");
			editor.setTheme("ace/theme/twilight");
			editor.session.setMode("ace/mode/gherkin");
			editor.session.setValue(serverModel.scriptExender.script)
			this.editor = editor;
		},

		_initializeModel: function (options) {
			var ScriptedTestCseModel = Backbone.Model.extend({
				urlRoot: urlBuilder.buildURL("testcases.scripted", options.testCaseId)
			});

			this.model = new ScriptedTestCseModel();
		},

		events: {
			"click #tc-script-save-button": "saveScript"
		},

		saveScript: function () {
			var tcScript = this.editor.session.getValue();
			this.model.set('script', tcScript);
			this.model.save();
		}

	});

	return ScriptEditorTab;
});
