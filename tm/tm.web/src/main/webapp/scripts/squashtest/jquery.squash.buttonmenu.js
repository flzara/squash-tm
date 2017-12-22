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
 *	this combines a jquery button and a jquery menu. The DOM of the button of that of the menu must be adjacent. The button is the main
 *  object. The configuration is :
 *
 *  conf : {
 *		menu : {  menu configuration },
 *		zindex : a user-defined z-index value, to make sure your menu will be displayed above any other elements.
 *					default is 3000,
 *		blur : "hide", "nothing" or another custom function that will receive no args. Default is "hide".
 *		anchor : one of ["left", "right"]. Default is "left" : this means that the menu is anchored to the button via its top-left corner.
 *				When set to "right", it would be the top-right,
 *		"no-auto-hide" : default is false. If true,  the menu will not automatically hide when an element is clicked (see behaviour below)
 *	}
 *
 * Behaviour :
 *
 * - When the button is clicked, the menu will be toggled on/off.
 * - When a <li> element of the menu is clicked, the menu will be toggled off (closed) automatically
 * - This <li> default behaviour can be overriden in two ways :
 *		1/ the element <li> has a css class "no-auto-hide"
 *		3/ the global flag "no-auto-hide" is true.
 *
 *
 *	@author bsiri
 *
 */

define([ "jquery", "jqueryui", "jquery.squash" ], function($) {
	"use strict";

	// prevent double loading
	if ($.squash && $.squash.buttonmenu) {
		return;
	}

	$.widget("squash.buttonmenu", {
		options : {
			menu : {
				zindex : 3000
			},
			blur : "hide",
			anchor : "left",
			"no-auto-hide" : false,

			// private
			_firstInvokation : true,

      display : "inline-block"
		},

		// will wrap both elements in an enclosing div, that will help adjust their relative positionning later.
		_createStructure : function(settings) {

			var btn = this.element, menu = this.element.next(), components = btn.add(menu);

			btn.addClass("buttonmenu-button");
			btn.attr('role', 'buttonmenu');
			menu.addClass("buttonmenu-menu");

			var div = $("<div/>", {
				style : "display:"+ settings.display +"; position:relative;",
				"class" : "buttonmenu-wrapper"
			});

			components.wrapAll(div);

		},

		_create : function() {

			var self = this;
			var settings = this.options;

			var button = this.element;
			var menu = button.next();

			// basics
			this._createStructure(settings);
			this._menuCssTweak();
			this._bindLi();

			menu.menu(settings.menu);

			// events
			button.on("click", function() {
				if (button.hasClass('ui-state-disabled')){
					return false;
				}

				if (menu.hasClass("expand")) {
					self.close();
				} else {
					self.open();
				}

				if (settings._firstInvokation) {
					self._fixRender(menu);
				}

				return false;
			});

			return this;
		},

		blur : function(evt) {
			var blurhandler = this.options.blur;
			if (blurhandler === "hide") {
				this.close();
			} else if ($.isFunction(blurhandler)) {
				try {
					blurhandler.call(this, evt);
				} catch (wtf) {
					if (window.console && window.console.log) {
						window.console.log("buttonmenu : problem while bluring menu " + this.selector);
					}
				}
			}
		},

		enable : function() {
			this.element.removeClass('ui-state-disabled');
		},

		disable : function(selector) {
			this.close();
			this.element.addClass('ui-state-disabled');
		},

		open : function() {
			this.element.next().addClass("expand").removeClass("collapse").show();
		},

		close : function() {
			this.element.next().removeClass("expand").addClass("collapse").hide();
		},

		/*
		 * The goal here is to prevents the juggling effect when hovering the items.
		 *
		 * The following cannot be invoked before the menu has appeared at least once. It is so because we need the browser
		 * to compute its final width before we can execute some adjustments based on it (in some cases the width might had
		 * been 0).
		 *
		 */
		_fixRender : function(menu) {
			var settings = this.options;

			var width = menu.width();
			menu.width(width + 10);

			settings._firstInvokation = false;
		},

		_menuCssTweak : function() {
			var menu = this.element.next();
			menu.hide();
			menu.removeClass("not-displayed");
			menu.addClass("squash-buttonmenu");
			menu.css("position", "absolute");
			menu.css("overflow", "hidden");
			menu.css("white-space", "nowrap");
			menu.css("z-index", this.options.menu.zindex);
			if (this.options.anchor === "right") {
				menu.css("right", 0);
			}
		},

		_bindLi : function() {
			var self = this, settings = this.options, menu = this.element.next();

			// item disabled ? event prevented !
			menu.find('li').each(function(){
				$(this).bindFirst('click', function(evt){
					if ($(this).hasClass('ui-state-disabled')){
						evt.stopImmediatePropagation();
						return false;
					}
				});
			});

			menu.on("click", "li", function(evt) {
				var $li = $(this);

				// if no policy was set to prevent the menu from closing, let the menu close
				if (!($li.hasClass("no-auto-hide") || settings["no-auto-hide"])) {
					self.close();
				}
			});

		},

		_destroy : function() {
			this.element.next().menu("destroy");
			this._super();
		}

	});

	$(document).on("click", function(evt) {
		var $target = $(evt.target);
		var $menus = $(":data('squash-buttonmenu')");
		var $blurTarget;

		if ($target.is(".buttonmenu-menu.expand")) {
			$blurTarget = $menus.not($target.prev());

		} else {
			var $parent = $target.parents(".buttonmenu-menu.expand");

			if ($parent.length > 0) {
				$blurTarget = $menus.not($parent.prev());

			} else {
				$blurTarget = $menus;

			}
		}

		$blurTarget.buttonmenu("blur");
	});
});
