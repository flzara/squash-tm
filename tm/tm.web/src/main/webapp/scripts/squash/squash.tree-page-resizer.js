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
define(
		[ "jquery", "jqueryui" ],
		function($) {

			// needed only if there's an iframe on the right panel (it captures
			// the mouse movements and messes with the resizing)
			function Overlay(panel) {

				this.destroy = function() {
				};

				if (panel.find('iframe').length > 0) {
					this.shadowedPanel = panel;
					this.formerZIndex = panel.css('z-index');

					this.shadowedPanel.css('z-index', -10);

					this.overlay = $(
							'<div style="position : absolute; width : 100%; height : 100%; opacity : 0.0; filter:alpha(opacity=00);"/>')
							.prependTo(this.shadowedPanel);

					this.destroy = function() {
						this.overlay.remove();
						this.shadowedPanel.css('z-index', this.formerZIndex);
					};
				}

			}

			function makePanelResizable(confObj) {

				var conf = {
					minWidth : 270,
					helper : "ui-resizable-helper",
					handles : "e",

					start : function() {
						confObj.helper = $(".ui-resizable-helper");
						confObj.overlayRight = new Overlay(confObj.rightPanel);
						// confObj.overlayLeft = new Overlay(confObj.leftPanel);
					},

					stop : function() {
						confObj.leftPanel.css('height', '');

						confObj.overlayRight.destroy();
						// confObj.overlayLeft.destroy();

						delete confObj.overlayRight;
						// delete confObj.overlayLeft;
						delete confObj.helper;
					}

				};

				/**
				 * we will be using different resizing strategies, that depend
				 * on the css 'position' attribute of the right panel.
				 *
				 */
				var position = confObj.rightPanel.css('position');

				if (position === 'absolute') {
					$.extend(conf, {
						resize : function() {
							var pos = confObj.helper.width();

							confObj.leftPanel.width(pos - 10);
							confObj.rightPanel.css('left', pos + 10 + "px");
							localStorage.setItem('leftWidth',pos);
						}
					});
				} else /* if (position==='relative' || position==='static') */{

					confObj.leftPanel.css('float : left;');
					confObj.rightPanel.css('overflow', 'hidden');
					confObj.rightPanel.css('width', 'auto');
					if(confObj.helper) {
						localStorage.setItem('leftWidth', confObj.helper.width());
					}
				}

				confObj.leftPanel.resizable(conf);

				// now that the resizebar exists, let configure it
				confObj.resizeBar = confObj.leftPanel.find(".ui-resizable-e");

			}

			var resizer = {

				defaultSettings : {
					leftSelector : "#tree-panel-left",
					rightSelector : "#contextual-content"
				},

				init : function(settings) {

					var effective = (arguments.length > 0) ? $.extend({},
							this.defaultSettings, settings)
							: this.defaultSettings;

					var confObj = {
						leftPanel : $(effective.leftSelector),
						rightPanel : $(effective.rightSelector)
					};

					makePanelResizable(confObj);

				}

			};

			return resizer;
		});
