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
 that file will compile every little random plugins/redefinitions etc we need to stuff jQuery with.

 */

var squashtm = squashtm || {};

(function($) {
	// custom selectors, eg $(tree).find(":folder") will select all the nodes
	// corresponding to folders.

	$.extend($.expr[':'], {
		library : function(a) {
			return $(a).is("[rel='drive']");
		},
		folder : function(a) {
			return $(a).is("[rel='folder']");
		},
		'test-case' : function(a) {
			return $(a).is("[rel='test-case']");
		},
		requirement : function(a) {
			return $(a).is("[rel='requirement']");
		},
		campaign : function(a) {
			return $(a).is("[rel='campaign']");
		},
		iteration : function(a) {
			return $(a).is("[rel='iteration']");
		},
		'test-suite' : function(a) {
			return $(a).is("[rel='test-suite']");
		},
		// ***************** legacy *****************
		node : function(a) {
			return $(a).is(":folder, :test-case, :requirement, :campaign");
		},
		resource : function(a) {
			return $(a).is("[rel='iteration']");
		},
		view : function(a) {
			return $(a).is("[rel='test-suite']");
		},
		// ****************** /legacy *****************
		editable : function(a) {
			return $(a).attr('editable') === 'true';
		},
		creatable : function(a) {
			return $(a).attr('creatable') === 'true';
		},
		deletable : function(a) {
			return $(a).attr('deletable') === 'true';
		},
		exportable : function(a){
			return $(a).attr('exportable') === 'true';
		},
		manageable : function(a){
			return $(a).attr('manageable') === 'true';
		},
		'milestone-creatable' : function(a){
			return $(a).attr('milestone-creatable-deletable') === 'true';
		},
		'milestone-editable' : function(a){
			return $(a).attr('milestone-editable') === 'true';
		},
		'synchronized' : function(a){
			return $(a).attr('synchronized') === 'true';
		},
		'req-version-modifiable' : function (a) {
			return $(a).attr('req-version-modifiable') === 'true';
		}
	});

	// convenient function to gather data of a jQuery object.
	$.fn.collect = function(fnArg) {
		var res = [];
		if (this.length > 0) {
			this.each(function(index, elt) {
				res.push(fnArg(elt));
			});
		}
		return res;

	};

	$.fn.contains = function(domElt) {
		var vThis = this.collect(function(e) {
			return e;
		});

		for ( var e in vThis) {
			if (vThis[e] === domElt) {
				return true;
			}
		}

		return false;

	};


	$.fn.visible = function() {
	    return this.css('visibility', 'visible');
	};

	$.fn.invisible = function() {
	    return this.css('visibility', 'hidden');
	};


	/*
	 * thanks to
	 * http://stackoverflow.com/questions/2360655/jquery-event-handlers-always-execute-in-order-they-were-bound-any-way-around-t
	 * for fixing me
	 */
	$.fn.bindFirst = function(name, selector, data, fn) {

		this.on(name, selector, data, fn);

		this.each(function() {
			var handlers = $._data(this, 'events')[name.split('.')[0]];
			var handler = handlers.pop();
			handlers.splice(0, 0, handler);
		});
	};



	/* defines functions in the jQuery namespace */
	$.extend({
		/**
		 * Opens a "popup" window containing the result of a POST. Plain window.open() can only GET
		 *
		 * @param url
		 *            the url to POST
		 * @param data
		 *            the post data as a javascript object
		 * @param windowDef
		 *            definition of the window to open : { name: "name of window", features: "features string as per
		 *            window.open" }
		 * @return reference to the new window
		 */
		open : function(url, data, windowDef) {
			var postData = '';

			for ( var attr in data) {
				postData += '<input type=\"hidden\" name=\"' + attr + '\" value=\"' + data[attr] + '\" />';
			}

			var form = '<form id=\"postForm\" style=\"display:none;\" action=\"' + url + '\" method=\"post\">' +
					'<input type=\"submit\" name=\"postFormSubmit\" value=\"\" />' + postData + '</form>';

			var win = window.open("about:blank", windowDef.name, windowDef.features);
			win.document.write(form);
			win.document.forms.postForm.submit();

			return win;
		}
	});



	$.extend({

		/*
		 * inhibits navigation to previous page when pressing backspace, as requested in issue
		 * https://ci.squashtest.org/mantis/view.php?id=2069
		 *
		 * Solution credited to erikkallen, at
		 * http://stackoverflow.com/questions/1495219/how-can-i-prevent-the-backspace-key-from-navigating-back
		 *
		 * Kudos my good sir.
		 *
		 */

		noBackspaceNavigation : function() {
			$(document).bind('keydown', function(event) {
				var doPrevent = false;
				if (event.keyCode === 8) {
					var d = event.srcElement || event.target;
					if ((d.tagName.toUpperCase() === 'INPUT' && (d.type.toUpperCase() === 'TEXT' || d.type
							.toUpperCase() === 'PASSWORD')) ||
							d.tagName.toUpperCase() === 'TEXTAREA') {
						doPrevent = d.readOnly || d.disabled;
					} else {
						doPrevent = true;
					}
				}

				if (doPrevent) {
					event.preventDefault();
				}
			});
		}
	});


})(jQuery);
