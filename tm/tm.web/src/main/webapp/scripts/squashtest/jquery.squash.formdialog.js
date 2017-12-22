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
 * Documentation  :
 *
 * ======== structure ===========
 *
 * Contrary to regular jQuery dialogs, the button pane is declared explicitely. Here is a sample formdialog, note
 * the use of css classes and data-def :
 *
 * <div id="mydialog" class="popup-dialog">
 *
 *		<div>
 *			<p>this is my main content. Because it isn't a 'state' div (read below), it will always be displayed.
 *				It is basically equivalent state=default
 *			</p>
 *
 *			<textarea data-def="isrich">will be turned into rich editable</textarea>
 *
 *			<textarea>will remain a regular textarea</textarea>
 *		</div>
 *
 *		<div data-def="state=content-1">
 *			<p>either this is displayed</p>
 *		</div>
 *
 *		<div data-def="state=content-2">
 *			<p>or that</p>
 *		</div>
 *
 *		<div data-def="error-pane">
 *			<span>
 *				Must always be a panel having data-def=error-pane, and
 *				the first <span/> element will receive the error messages.
 *
 *				This will be jqueryfied as a jquery.squash.popupError.
 *				Also, it's optional : if this pane is undefined the formDialog
 *				will create one on the fly if needed whenever the method "showError"
 *				is invoked.
 *			</span>
 *		</div>
 *
 *		<div class="popup-dialog-buttonpane">
 *			<input type="button" value="ok"						data-def="evt=confirm, mainbtn"/>
 *			<input type="button" value="cancel"					data-def="evt=cancel" />
 *			<input type="button" value="specific to content1"	data-def="state=content-1" />
 *			<input type="button" value="specific to content2"	data-def="state=content-2" />
 *		</div>
 *
 *	</div>
 *
 *
 *	========== behaviour ================
 *
 *	1/ the buttons aren't created like regular popup : the same dom objects are litteraly reused. This can help you
 *	with the logic of the popup. HOWEVER, they are moved around, which may lead to the loss of callbacks on those buttons
 *	if they were bound before the dialog was initialized.
 *
 *	2/ the popup will be automatically destroyed and removed whenever the container it was initially declared in
 *	is removed. Not more 'is-contextual' bullshit !
 *
 *	3/ all the inputs defined in this dialog will be cleaned up automatically whenever the dialog is opened again.
 *     If you don't want to clean, you can  activate use the nocleanup option.
 *	4/ a popup can define several alternative content that are displayed one at a time, representing a state.
 *	Displaying one will automatically hide the other alternatives.
 *	Those elements are declared using data-def="state=<state-id>", or directly using class="popup-dialog-state-<state-id>".
 *	See the API for details (setState()) and configuration for details.
 *
 *	============= API ====================
 *
 *	1/ cleanup : force the cleanup of the controls
 *
 *	2/ setState(id) : will show anything configured 'state=<state id>' and hide the other ones.
 *
 *  3/ onOwnBtn(evtname, handler) : for inheritance purposes. This allows subclasses of this dialog
 *					to listen to their own button event defined using evt=<eventname> (see DOM configuration).
 *
 *  4/ showError(message) :  displays inside a jquery.squash.popupError widget
 *		an error message. By default such error panel will be created if none is declared in the DOM.
 *		You can also declare one in the DOM, that you can then css or structure as you wish, provided that :
 *		- it is tagged as a data-def="error-pane",
 *		- it contains at least a span.
 *
 *	========= configuration ==============
 *
 *	1/ basic : all basic options of jQuery dialog are valid here, EXCEPT for the buttons.
 *
 *
 *	2/ DOM conf : reads parts of the conf from the datatable, see the handlers at the end of the document for details.
 *	for now, supports :
 *  -nocleanup : for main div, don't cleanup input when the dialog is reopened
 *	- isrich : for textarea. If set, will be turned into a ckeditor.
 *	- evt=<eventname> : for buttons. If set, clicking on that button will trigger <eventname> on the dialog.
 *	- state=<state id>[ <stat id> ...] : for any elements in the popup. Multiple elements can declare the same <state-id> and they'll
 *						be logically bound when setState(<state-id>) is invoked. Note that a single element can belong
 *						to multiple state either by a space-separated list of states,  either declaring this 'state' clause multiple times.
 *
 *	- mainbtn[=<state-id>] : for buttons. If set, pressing <ENTER> inside the dialog will trigger 'click' on that button if the popup is in that
 *						current state. the <state-id> is optional : if left blank, the button will be triggered if the popup is in the default
 *						state.
 *  - maininput : an input defined as maininput will get the focus back when the cleanup function is called. This is used for dialogs that enable
 *  					'add-another' feature and are not closed but only cleaned-up. A maininput must be unique in a dialog.
 *	- error-pane : a div defined as an error-pane will be turned into a jquery.squash.popuperror. It will be used and shown when the method showError
 *					is invoked (see API), instead of the default one.
 *
 */
define([ 'jquery', "underscore", 'squash.attributeparser', 'squash.configmanager', 'jqueryui', './jquery.squash.squashbutton', 'squashtest/jquery.squash.popuperror' ], function($, _, attrparser, confman) {

	if (($.squash !== undefined) && ($.squash.formDialog !== undefined)) {
		// plugin already loaded
		return;
	}

	$.widget("squash.formDialog", $.ui.dialog, {

		options : {
			nocleanup : false,
			autoOpen : false,
			resizable : false,
			modal : true,
			width : 600,
			position : [ 'center', 100 ],
			_internalEvents : {},
			_state : "default",
			_richeditors : {},
			_mainBtns : {},
			_maininput : undefined,
			_errorPane : undefined
		},

		_triggerCustom : function(event) {
			var evtname = $(event.target).data('evt');
			this._trigger(evtname);
			this._triggerInternal(evtname);
			return this;
		},

		_triggerInternal : function(evtname){
			var listeners = this.options._internalEvents[evtname];
			if (listeners!==undefined){
				for (var i=0,len = listeners.length; i<len;i++){
					listeners[i]();
				}
			}
		},

		onOwnBtn : function(evtname, handler){
			var listeners = this.options._internalEvents[evtname];
			if (listeners === undefined){
				listeners = this.options._internalEvents[evtname] = [];
			}
			listeners.push(handler);
		},

		cancel : function(event) {
			var isclose = this.close();
			if (isclose === false) {
				return;
			}

			this._trigger("cancel");
			return this;
		},

		// if the argument is unknown, will default to state "default"
		setState : function(state) {
			this.uiDialog.find('[class*="popup-dialog-state"]').hide();

			var tobedisplayed = this.uiDialog.find('.popup-dialog-state-' + state);
			tobedisplayed.show();

			this.options._state = (tobedisplayed.length === 0) ? "default" : state;
			this._triggerInternal("statechange"+state);
		},

		getState : function(){
			return this.options._state;
		},

		showError : function(msg){
			if (this.options._errorPane === undefined){
				this._createErrorpane();
			}
			var pane = this.options._errorPane;
			pane.find('span:first').text(msg);
			pane.popupError('show');
		},

		_create : function() {
			var self = this;

			var parent = this.element.eq(0).parent();

			function keyshortcuts(event) {
				switch (event.keyCode) {
				case $.ui.keyCode.ESCAPE:
					self.cancel(event);
					event.preventDefault();
					break;

				case $.ui.keyCode.ENTER:

					/*
					 * Issue 5108
					 *
					 * Due to unknown changes elsewhere in the app the following code breaks
					 * although it worked fined in previous versions
					 *
					 *  Fixing it by adding an exception on the Enter event, when the event
					 *  originates from within a textarea
					 */
					var target = $(event.target);
					if (target.is('textarea')){
						event.stopPropagation();
						return;
					}

					// the following activate the main button for the current
					// form state, unless another button was explicitly focused
					// (in which case it just let the event run).
					var button = self.uiDialog.find(".ui-dialog-buttonset .ui-button:focus")[0];
					if(!button){
						event.preventDefault();
						var state = self.options._state;
						var btn = self.options._mainBtns[state];

						if (btn !== undefined) {
							btn.focus();
							btn.click();
						}
					}
					break;

				default:
					return;
				}
			}


			// extend the conf with the domconf on the root element
			var def = this.element.data('def');
			if (!!def){
				var conf = attrparser.parse(def);
				this.options = $.extend(conf, this.options);
			}

			// extend the conf with explicit data attributes
			var datas = _.omit(this.element.data(), "def");
			this.options = _.extend(this.options, datas);

			// creates the widget
			self._super();
			
			// now read and apply dom conf from the content
			this._readDomConf();

			// declares custom events
			self._on({
				"click .ui-dialog-buttonpane :input" : self._triggerCustom
			});

			// autoremove when parent container is removed
			parent.on('remove', function() {
				self._destroy();
				self.element.remove();
			});

			this.uiDialog.keydown(keyshortcuts);

		},

		_createErrorpane : function(){
			var errorpane = $("<div><span></span></div>");
			this.uiDialog.find('.popup-dialog-buttonpane').before(errorpane);
			errorpane.popupError();
			this.options._errorPane = errorpane;
		},

		open : function() {
			if (!this.options.nocleanup){
				this.cleanup();
			}
			this._super();

		},

		// due to a bug in IE9 the input type "select" is cleanedup separately from the others
		cleanup : function() {
			var elt = this.element;

			elt.find(':input,textarea').not('select').not(':input[type=radio]').not('input[type="button"]').each(function() {
				$(this).val('');
			});

			elt.find('select').each(function(){
				$(this).find('option').first().prop('selected', true);
			});

			elt.find('.error-message').each(function(){
				$(this).text('');
			});

			this.focusMainInput();
			
			this._trigger('cleanup');
		},
		
		focusMainInput : function() {
			var maininput = this.options._maininput;
			if(maininput !== undefined) {
				if(maininput.data().ckeditorInstance !== undefined) {
					// if the main input is a rich editor
					var ckeditorInstance = CKEDITOR.instances[maininput.attr('id')];
					// setData is asynchronous
					ckeditorInstance.setData("", function() {
						ckeditorInstance.focus();
					});
				} else {
					// if it is a basic input
					maininput.focus();
				}
			}
		},
		
		_createButtons : function() {

			// ripped from jquery-ui 1.8.13. It might change some day, be careful.
			var buttonpane = this.uiDialog.find('.popup-dialog-buttonpane');
			buttonpane.addClass('ui-dialog-buttonpane ui-widget-content ui-helper-clearfix').wrapInner(
					'<div class="ui-dialog-buttonset"></div>');
			var buttons = buttonpane.find('input:button').squashButton();
			buttonpane.find('.ui-dialog-buttonset').append(buttons);

			// the following line will move the buttonpane after the body of the popup.
			buttonpane.appendTo(this.uiDialog);

		},

		// negation of the above. Untested yet.
		_destroyButtons : function() {
			var buttonpane = this.uiDialog.find('.popup-dialog-buttonpane');
			buttonpane.removeClass('ui-dialog-buttonpane ui-widget-content ui-helper-clearfix');
			buttonpane.find('input:button').squashButton('destroy').appendTo(buttonpane);
			buttonpane.find('div.ui-dialog-buttonset').remove();

			// move the buttonpane back to the body.
			this.element.append(buttonpane);
		},

		_setOption : function(key, value) {
			// In jQuery UI 1.8, you have to manually invoke the _setOption method from the base widget
			$.Widget.prototype._setOption.apply(this, arguments);
		},

		_destroyCked : function() {
			var editors = this.options._richeditors;
			for (var i in editors) {
				var domelt = editors[i];
				var ckInstance = CKEDITOR.instances[domelt.attr('id')];
				if (ckInstance) {
					ckInstance.destroy(true);
				}
			}
		},

		_destroy : function() {
			this._off($(".ui-dialog-buttonpane button"), "click");
			this._off($(".ui-dialog-titlebar-close"), "click");
			this._destroyCked();
			this._destroyButtons();
			this._super();
		},

		_readDomConf : function() {
			var $widget = this;
			var handlers = $.squash.formDialog.domconf;

			$widget.uiDialog.find('[data-def]').each(function() {

				var $elt = $(this);
				var raw = $elt.data('def');
				var conf = attrparser.parse(raw);

				var handler;
				for ( var key in conf) {

					handler = handlers[key];
					if (handler !== undefined) {
						handler.call($widget, $elt, conf[key]);
					}
				}
			});
		}

	});
	
	$.squash.formDialog.domconf = {
		'nocleanup': function($elt, value){
			this.options.nocleanup = true;
		},
		
		'isrich' : function($elt, value) {
			var randomKey = Math.random().toString().substring(3,6);
			this.options._richeditors[randomKey]=$elt;
			var conf = confman.getStdCkeditor();
			$elt.ckeditor(function() {
			}, conf);
		},

		'mainbtn' : function($elt, value) {
			if (value === true){
				this.options._mainBtns["default"] = $elt;
			}
			else{
				var values = $.trim(value).split(' ');
				for (var i=0, len = values.length; i<len;i++){
					this.options._mainBtns[values[i]] = $elt;
				}
			}
		},
		
		'maininput' : function($elt, value) {
			if(value === true) {
				this.options._maininput = $elt;
			}
		},

		'evt' : function($elt, value) {
			$elt.data('evt', value);
		},

		'state' : function($elt, value) {
			var values = $.trim(value).split(' ');
			for (var i=0,len = values.length; i<len;i++){
				$elt.addClass('popup-dialog-state-' + values[i]);
			}
		},

		'error-pane' : function($elt, value){
			$elt.popupError();
			this.options._errorPane = $elt;
		}
	};
});
