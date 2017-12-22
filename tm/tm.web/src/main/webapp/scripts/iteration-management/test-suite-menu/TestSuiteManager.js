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
 * A Control is a pair of input text / button enclosing the main view.
 *
 */

define([ 'jquery', 'jquery.squash.oneshotdialog', 
         'jqueryui', 'jquery.squash.squashbutton',
         'jquery.squash.formdialog'
         ], function($, oneshot) {

	function TestSuiteManagerControl(settings) {

		this.manager = settings.manager;
		this.confirmMessage = settings.confirmMessage;
		this.confirmTitle = settings.confirmTitle;
		this.panel = settings.panel;
		this.action = settings.action;

		this.input = $("input[type='text']", settings.panel);
		this.button = $("input[type='button']", settings.panel).squashButton();

		var self = this;

		/* *** little override here ***** */

		var oldVal = this.input.val;

		this.input.val = function() {
			if (arguments.length > 0) {
				oldVal.apply(this, arguments);
				updateBtn();
			} else {
				return oldVal.call(this);
			}
		};

		/* ************** public *********** */

		this.reset = function() {
			defaultState();
			this.input.addClass('manager-control-ready');
			this.input.removeClass('manager-control-disabled');
		};

		this.deactivate = function() {
			defaultState();
			this.input.prop('disabled', true);
			this.input.removeClass('manager-control-ready');
			this.input.addClass('manager-control-disabled');
			this.button.squashButton("disable");
		};

		this.setText = function(text) {
			this.input.val(text);
		};

		/* ************* private ******** */

		var defaultState = $.proxy(function() {
			this.input.prop('disabled', false);
			this.button.squashButton("enable");
			this.input.val("");
		}, self);

		var editState = $.proxy(function() {
			this.input.removeClass('manager-control-ready');
		}, self);

		/* ************* handlers ******** */

		this.button.click(function() {
			if (self.confirmMessage && self.confirmTitle) {
				oneshot.show(self.confirmTitle, self.confirmMessage).done(function() {
					self.action();
				});
			} else {
				self.action();
			}
		});

		// we're in competition here with the default 'enter' event
		// bound to the
		// close button
		this.input.keypress(function(evt) {
			self.manager.instance.find('.error-message').html('');
			if (evt.which == '13') {
				evt.stopImmediatePropagation();
				var disabledStatus = self.button.squashButton("option", "disabled");
				if (!disabledStatus) {
					self.button.click();
				}
			}
		});

		var updateBtn = function() {
			var button = self.button;
			if (!self.input.val().length ) {
				button.squashButton("disable");
			} else {
				button.squashButton("enable");
			}
		};

		// that one is better than change()
		this.input.keyup(function(evt) {
			updateBtn();
		});

		this.input.focus(editState);

	}

	/*
	 * 
	 * The view displays and manage the test suites (known here as items)
	 * 
	 */
	function TestSuiteManagerView(settings) {

		this.panel = settings.panel;
		this.manager = settings.manager;
		this.model = settings.model;

		var self = this;

		this.model.addView(this);

		/* ****** private ********* */

		var getAllItems = $.proxy(function() {
			return $('.suite-div', this.panel);
		}, this);

		var appendItem = $.proxy(function(data) {

			var newSuite = $("<div/>", {
				'class' : 'suite-div ui-corner-all'
			});
			var spanSuite = $("<span/>", {
				'data-suite-id' : data.id,
				'text' : data.name
			});

			newSuite.append(spanSuite);
			this.panel.append(newSuite);

		}, self);

		var sortSuiteList = $.proxy(function() {
			var allSuites = $('.suite-div', this.panel);

			var sorted = allSuites.sort(function(a, b) {
				var textA = getItemDomText(a);
				var textB = getItemDomText(b);
				return (textA < textB) ? -1 : 1;

			});
			this.panel.append(sorted);
		}, self);

		var getItemDomId = function(elt) {
			if (elt.firstElementChild !== undefined) {
				return elt.firstElementChild.getAttribute('data-suite-id');
			} else {
				return elt.firstChild.getAttribute('data-suite-id');
			}
		};

		var getItemDomText = function(elt) {
			if (elt.firstElementChild !== undefined) {
				return elt.firstElementChild.textContent;
			} else {
				return elt.firstChild.innerText;
			}
		};

		/* ********* public *********** */

		this.getSelectedIds = function() {
			var ids = this.getSelected().find('span').collect(function(elt) {
				return elt.getAttribute('data-suite-id');
			});
			return ids;
		};

		this.selectItems = function(selected) {
			getAllItems().each(function(i, elt) {
				for ( var j = 0; j < selected.length; j++) {
					var id = getItemDomId(elt);
					if (selected[j] == id) {
						$(elt).addClass("suite-selected ui-widget-header ui-state-default");
					}
				}
			});
		};

		this.getSelected = function() {
			return getAllItems().filter('.suite-selected');
		};

		this.deselectAllItems = function() {
			getAllItems().removeClass("suite-selected ui-widget-header ui-state-default");
		};
		

		this.redraw = function(evt_name) {

			// save state
			var selected = this.getSelectedIds();

			// rebuild
			var modelData = this.model.getData();
			this.panel.empty();

			for ( var i in modelData) {
				appendItem(modelData[i]);
			}

			sortSuiteList();

			// restore state
			this.selectItems(selected);

			this.manager.updatePopupState();
		
		};

		
		
		this.panel.delegate('.suite-div', 'click', function() {
			if (!self.manager.ctrlPressed) {
				self.deselectAllItems();
			}
			$(this).toggleClass('suite-selected ui-widget-header ui-state-default');
			self.manager.updatePopupState();
		});

	}

	function TestSuiteManager(settings) {

		var self = this;
		
		
		// ****** main dialog initialization ******
		
		var thismanager = this;
		
		var maindialog = settings.dialog;
		maindialog.formDialog({width : 400});
		
		maindialog.on('formdialogopen', function(){
			thismanager.resetAll();
		});
		
		maindialog.on('formdialogclosemanager', function(){
			maindialog.formDialog('close');
		});
		
		$("#manage-test-suites-button").on('click', function(){
			maindialog.formDialog('open');
		});
		
		

		/*
		 * **************** public state management methods ********************
		 */

		this.updatePopupState = function() {

			var allItems = this.view.getSelected();

			switch (allItems.size()) {
			case 0:
				this.rename.control.deactivate();
				this.remove.button.squashButton("disable");
				this.rename.control.button.squashButton("disable");
				break;
			case 1:
				this.rename.control.reset();
				var itemText = allItems.eq(0).find('span').text();
				this.rename.control.setText(itemText);
				this.remove.button.squashButton("enable");
				this.rename.control.button.squashButton("enable");
				break;
			default:
				this.rename.control.deactivate();
				break;
			}

		};

		/* ******************** actions ************************* */

		/* ----- suite creation ------- */

		var postNewSuite = $.proxy(function() {
			var name = this.create.control.input.val();

			this.model.postNew(name).success(function() {
				self.create.control.reset();
			});

		}, self);

		/* ------- suite renaming -------- */

		var postRenameSuite = $.proxy(function() {

			var suiteId = this.view.getSelectedIds()[0];
			var newName = this.rename.control.input.val();

			this.model.postRename({
				id : suiteId,
				name : newName
			});

		}, self);

		/* ------- suites removing -------- */

		var postRemoveSuites = $.proxy(function() {

			var toSend = {};
			toSend['ids[]'] = this.view.getSelectedIds();

			this.model.postRemove(toSend);

		}, self);

		/* ------- bind ctrl ------------ */

		var bindCtrl = $.proxy(function() {
			var jqDoc = $(document);
			jqDoc.keydown(function(evt) {
				if (evt.which == 17) {
					self.ctrlPressed = true;
				}
			});

			jqDoc.keyup(function(evt) {
				if (evt.which != 17) {
					self.ctrlPressed = false;
				}
			});
		}, self);
		
	

		/* ******************** init code ****************************** */


		// executed every time the popup opens
		this.resetAll = function() {
			this.view.deselectAllItems();
			this.create.control.reset();
			this.updatePopupState();
		};

		// actual init code
		this.instance = settings.instance;
		this.model = settings.model;
		this.ctrlPressed = false;

		this.create = {};
		this.rename = {};

		this.remove = {};
		this.remove.button = $(".remove-suites-section input", this.instance);

		var createControlSettings = {
			manager : this,
			panel : $(".create-suites-section", this.instance),
			action : postNewSuite
		};

		var renameControlSettings = {
			manager : this,
			panel : this.rename.panel = $(".rename-suites-section", this.instance),
			action : postRenameSuite
		};

		/*
		 * the remove control settings is special in the sense that it has no text input, just a button
		 */
		var removeControlSettings = {
			manager : this,
			confirmMessage : settings.deleteConfirmMessage,
			confirmTitle : settings.deleteConfirmTitle,
			panel : this.remove.panel = $(".remove-suites-section", this.instance),
			action : postRemoveSuites
		};

		var viewSettings = {
			manager : this,
			model : settings.model,
			panel : $(".display-suites-section", this.instance)
		};

		this.create.control = new TestSuiteManagerControl(createControlSettings);
		this.rename.control = new TestSuiteManagerControl(renameControlSettings);
		this.remove.control = new TestSuiteManagerControl(removeControlSettings);
		this.view = new TestSuiteManagerView(viewSettings);

		bindCtrl();
		this.view.redraw();

	}

	return TestSuiteManager;
});
