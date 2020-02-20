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
define(["jquery", "backbone", "underscore","squash.translator", "ace/ace", "workspace.routing", "./documentation/Scriptdocumentation","app/ws/squashtm.notification"], function ($, Backbone, _,translator, ace, urlBuilder, doc, notification) {
	var ScriptEditorTab = Backbone.View.extend({

		el: "#tab-tc-script-editor",

		//This view is initialized in test-case.jsp, the server model is directly injected in the page.
		initialize: function (options) {
			var serverModel = options.settings;
			this._initializeEditor(serverModel);
			this._initializeModel(serverModel);
			this.writable = serverModel.writable;
			this.active = false;
			this.testCaseId = serverModel.testCaseId;
			this.i18n = translator.get({
				"validate.success": "test-case.scripted.check.valid"
			});
		},

		events: {
			"click #tc-script-save-button": "saveScript",
			"click #tc-script-snippets-button": "showSnippets",
			"click #tc-script-toggle-help-panel": "toggleHelpPanel",
			"click #tc-script-editor": "activateEditor",
			"click #tc-script-validate-button": "validateScript",
			"click #tc-script-cancel": "cancel"
		},

		saveScript: function () {
			var that = this;
			var tcScript = this.editor.session.getValue();
			this.model.set('script', tcScript);
			this.model.save(null, {
				success: function (model, response) {
					that.originalScript = tcScript;
				}
			});
		},

		_initializeEditor: function (serverModel) {
			var that = this;
			//see https://github.com/ajaxorg/ace-builds/issues/35 to understand the next line...
			//so much headaches with the loading of ace extensions before finding this magic method...
			ace.config.loadModule("ace/ext/language_tools", function (langTools) {
				//modules must be loaded one by one
				ace.config.loadModule("ace/ext/split", function (splitModule) {
					var container = document.getElementById("tc-script-editor");
					var theme = "ace/theme/chrome-grey";

					//creating split before editors
					var split = new splitModule.Split(container, theme, 1);
					that.split = split;

					//now init the main editor, ie the one where user will type his script
					var editor = split.getEditor(0);
					that.editor = editor;
					//[Issue 7449]
					//temporary setting the mode to gherkin and text to empty so session and undo manager are properly initialized
					//text and mode will be set in next steps of editor initialization
					//see :
					// https://github.com/ajaxorg/ace/issues/2045
					// https://github.com/ajaxorg/ace/blob/v1.1.3/lib/ace/ace.js#L111
					var session = ace.createEditSession("","gherkin");
					editor.setSession(session);

					//disabling local auto completion as specified
					//must do it before using enableBasicAutoCompletion: true
					//because we need that auto completion is enabled to have the snippet ONLY
					langTools.setCompleters([langTools.snippetCompleter]);
					that.originalScript = serverModel.script;
					//now that editor and session are properly initialized we can init the value
					editor.session.setValue(that.originalScript);
					editor.getSession().setUseWrapMode(true);
					editor.getSession().setWrapLimitRange(160, 160);
					//now that editor and session are properly initialized we can init the mode (ie the script language that the editor will know)
					that._initialize_editor_mode(editor);
					editor.setTheme(theme);
					editor.setOptions({
						// Has to set this one to true if i want snippets, but basic auto completion is disabled above
						enableBasicAutocompletion: true,
						// Soft tabs with two spaces by tabs like required by gherkin good practices
						tabSize: 2,
						useSoftTabs: true,
						printMarginColumn: 160,
						enableSnippets: true,
						enableLiveAutocompletion: false,
						readOnly: true,
						highlightActiveLine: false,
						highlightGutterLine: false
					});

					// Adding convenient shortcut to save the script to server
					editor.commands.addCommand({
						name: 'saveGherkinScript',
						bindKey: {win: 'Ctrl-Alt-S', mac: 'Command-Alt-S'},
						exec: function (editor) {
							that.saveScript();
						},
						readOnly: false
					});
				});


			});
		},

		_initializeModel: function (options) {
			var urlRoot = urlBuilder.buildURL("testcases.scripted", options.testCaseId);
			var ScriptedTestCseModel = Backbone.Model.extend({
				urlRoot: urlRoot
			});
			this.urlRoot = urlRoot;
			this.model = new ScriptedTestCseModel();
		},

		_initialize_editor_mode: function (editor) {
			this.locale = this._findScriptLocale();

			var aceEditorMode;
			if (this.locale === "en") {
				aceEditorMode = "ace/mode/gherkin";
			} else {
				aceEditorMode = "ace/mode/gherkin-" + this.locale;
			}
			editor.session.setMode(aceEditorMode);
		},

		_findScriptLocale: function () {
			var line0 = this.editor.session.getLine(0);
			var locale = "en";
			if (line0 !== null) {
				line0 = line0.trim();
				if (line0.search("language:") !== -1) {
					var parsedLocale = line0.substring(line0.length - 2);
					switch (parsedLocale) {
						case "fr" :
							locale = "fr";
							break;
						case "de" :
							locale = "de";
							break;
						case "es" :
							locale = "es";
							break;
						default:
							console.log("Unable to find locale from parsed locale : " + parsedLocale + ". Default to en.");
					}
				}
			}
			return locale;
		},

		toggleHelpPanel: function () {
			var split = this.split;
			if (split.getSplits() === 2) {
				split.setSplits(1);
				this.editor.getSession().setWrapLimitRange(160, 160);
				this.editor.setOption("printMarginColumn", 160);
				return;
			}
			split.setSplits(2);
			this.editor.getSession().setWrapLimitRange(80, 80);
			this.editor.setOption("printMarginColumn", 80);
			var documentationEditor = split.getEditor(1);
			documentationEditor.setReadOnly(true);
			documentationEditor.setOptions({
				showPrintMargin: false,
				tabSize: 2,
				useSoftTabs: true,
				readOnly: true,
				highlightActiveLine: false,
				highlightGutterLine: false
			});
			documentationEditor.session.setValue(doc.getDocumentation(this.locale));
			documentationEditor.getSession().setUseWrapMode(true);
			documentationEditor.getSession().setWrapLimitRange(80, 80);
			this._initialize_editor_mode(documentationEditor);
		},

		activateEditor: function () {
			if (this.writable && !this.active) {
				this.editor.setTheme("ace/theme/chrome");
				this.editor.setOptions({
					readOnly: false,
					highlightActiveLine: true,
					highlightGutterLine: true
				});
				this.$el.find("#tc-script-save-button").show();
				this.$el.find("#tc-script-snippets-button").show();
				this.$el.find("#tc-script-cancel").show();
				this.$el.find("#tc-script-validate-button").show();
				this.editor.focus();
				this.active = true;
			}
		},

		cancel: function () {
			this.editor.setTheme("ace/theme/chrome-grey");
			this.editor.setOptions({
				readOnly: true,
				highlightActiveLine: false,
				highlightGutterLine: false
			});
			this.$el.find("#tc-script-save-button").hide();
			this.$el.find("#tc-script-snippets-button").hide();
			this.$el.find("#tc-script-cancel").hide();
			this.$el.find("#tc-script-validate-button").hide();
			this.editor.session.setValue(this.originalScript);
			this.active = false;
		},

		showSnippets: function () {
			this.editor.execCommand("startAutocomplete");
		},

		validateScript: function () {
			console.log("validateScript");

			var that = this;
			var script = this.editor.session.getValue();
			var validationUrl = urlBuilder.buildURL("testcases.scripted.validate", this.testCaseId);

			var xhr = $.ajax({
				url : validationUrl,
				type : 'post',
				contentType: 'application/json',
				dataType: 'json',
				data : JSON.stringify({'script' : script })
			}).success(function () {
				notification.showInfo(that.i18n["validate.success"]);
			});
		}


	});

	return ScriptEditorTab;
});
