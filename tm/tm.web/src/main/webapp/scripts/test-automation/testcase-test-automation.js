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
/*
 settings :
 - canModify : a boolean telling if the associated script can be changed or not
 - testAutomationURL : the url where to GET - POST - DELETE things.
 */
define([ "jquery", "workspace.event-bus", "squash.translator", "squash.configmanager", "tree/plugins/plugin-factory",
		"jquery.squash.formdialog", "jeditable" ], function($, eventBus, translator, confman, treefactory) {


	// ************* specific jeditable plugin ***************

	/*
	 * We need a specific plugin because we need the buttons panel
	 * to have a third button.
	 *
	 * We base our plugin on the 'text' builtin plugin.
	 *
	 */

	var edObj = $.extend(true, {},$.editable.types.text);
	var edFnButtons = $.editable.types.defaults.buttons;
	var edFnElements = $.editable.types.text.element;

	edObj.buttons = function(settings, original){
		//var form = this;
		//first apply the original function
		edFnButtons.call(this, settings, original);

		// now add our own button
		var btnChoose = $("<button/>",{
			'text' : translator.get('label.dot.pick'),
			'id' : 'ta-script-picker-button'
		});

		var btnRemove = $("<button/>",{
			'text' : translator.get('label.Remove'),
			'id' : 'ta-script-remove-button'
		});

		this.append(btnChoose)
			.append(btnRemove);
	};

	// this is overriden so as to enforce the width.
	edObj.element = function(settings, original){
		var input = edFnElements.call(this, settings, original);
		input.css('width', '70%');
		input.css('height', '16px');
		return input;
	} ;

	$.editable.addInputType('ta-picker', edObj );


	// ****************** init function ********************

	function init(settings){


		// simple case first
		if (! settings.canModify){
			return;
		}

		// else we must init the special edit in place and the popups
		_initEditable(settings);
		_initPickerPopup(settings);
		_initRemovePopup(settings);

	}

	function _initEditable(settings){

		var elt = $("#ta-script-picker-span");

		var conf = confman.getStdJeditable();
		conf.type = 'ta-picker';
		conf.name = 'path';
		conf.width = '70%';


		// now make it editable
		elt.editable(settings.testAutomationURL, conf);

		// more events
		elt.on('click', '#ta-script-picker-button', function(){
			$("#ta-picker-popup").formDialog('open');
			return false;//for some reason jeditable would trigger 'submit' if we let go
		});

		elt.on('click', '#ta-script-remove-button', function(){
			$("#ta-remove-popup").formDialog('open');
			return false;// see comment above
		});

	}

	function _initRemovePopup(settings){
		var dialog = $("#ta-remove-popup");

		dialog.formDialog();

		dialog.on('formdialogconfirm', function(){
			dialog.formDialog('close');
			var form = $("#ta-script-picker-span>form");
			form.find('input').val('');
			form.submit();
		});

		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
	}

	function _initPickerPopup(settings){

		var dialog = $("#ta-picker-popup");

		testAutomationTree = dialog.find(".structure-tree");

		// init

		dialog.formDialog({
			height : 500
		});

		// cache
		dialog.data('model-cache', undefined);



		// ************ model loading *************************

		var initDialogCache = function() {

			dialog.formDialog('setState', 'pleasewait');

			return $.ajax({
				url : settings.testAutomationURL,
				type : 'GET',
				dataType : 'json'
			})
			.done(function(json) {
				dialog.data('model-cache', json);
				createTree();
				dialog.formDialog('setState', 'main');
			})
			.fail(function(jsonError) {
				dialog.formDialog('close');
			});
		};

		var createTree = function() {

			treefactory.configure('simple-tree'); // will add the 'squash' plugin if doesn't exist yet
			instanceTree = testAutomationTree.jstree({
				"json_data" : {
					"data" : dialog.data('model-cache')
				},

				"types" : {
          "max_depth" : -2, // unlimited without check
          "max_children" : -2, // unlimited w/o check
          "valid_children" : [ "drive" ],
					"types" : {
						"drive" : {
							"valid_children" : [ "ta-test", "folder" ],
							"select_node" : true
						},
						"ta-test" : {
							"valid_chidlren" : "none",
							"select_node" : true
						},
						"folder" : {
							"valid_children" : [ "ta-test", "folder" ],
							"select_node" : true
						}
					}
				},

				"ui" : {
					"select_multiple_modifier" : false
				},

				"themes" : {
					"theme" : "squashtest",
					"dots" : true,
					"icons" : true,
					"url" : squashtm.app.contextRoot + "/styles/squash.tree.css"
				},

				"core" : {
					"animation" : 0
				},

        conditionalselect : function () {
          return true;
        },

				"plugins" : [ "json_data", "types", "ui", "themes", "squash",'conditionalselect']

			});

      $(window).bind('select_node.jstree', function () {
        console.log('select_node.jstree');
      });
      $(window).bind('click.jstree', function () {
        console.log('click.jstree');
      });

		};

		var reset = function() {
			if (testAutomationTree.jstree('get_selected').length > 0) {
				testAutomationTree.jstree('get_selected').deselect();
			}
		};

		// ****************** transaction ************


		var submit = function() {

			try {

				var node = testAutomationTree.jstree('get_selected');

				if (node.length < 1) {
					throw "no-selection";
				}

				var nodePath = node.getPath();

				$("#ta-script-picker-span").find('form input[name="path"]').val(nodePath);
				dialog.formDialog('close');

			} catch (exception) {
				var errmsg = exception;
				if (exception == "no-selection") {
					errmsg = translator.get('test-case.testautomation.popup.error.noselect');
				}
				dialog.formDialog('showError', errmsg);
			}

		};

		// ************ events *********************

		dialog.on('formdialogconfirm', submit);

		dialog.on('formdialogcancel', function() {
			dialog.formDialog('close');
		});

		dialog.on("formdialogopen", function() {
			if (dialog.data('model-cache') === undefined) {
				dialog.initAjax = initDialogCache();
			} else {
				reset();
			}
		});

		dialog.on('formdialogclose', function() {
			if (dialog.initAjax) {
				dialog.initAjax.abort();
			}

		});
	}

	return {
		init : init
	};
});
