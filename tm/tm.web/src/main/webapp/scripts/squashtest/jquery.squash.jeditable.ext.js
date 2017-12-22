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
define(['jquery', 'squash.configmanager', 'squash.attributeparser', "jeditable", "jeditable.ckeditor"],
		function($, confman, attrparser){


	// Adding maxlength attribute for text
	// thanks to
	// http://blogpad-online.blogspot.com/2010/10/jeditable-maxlength_12.html
	$.editable.types.text.element = function(settings, original) {
		var input = $('<input />');
		if (settings.width != 'none') {
			input.width(settings.width);
		}
		if (settings.height != 'none') {
			input.height(settings.height);
		}
		if (settings.maxlength != 'none') {
			input.attr('maxlength', settings.maxlength);
		}
		input.attr('autocomplete', 'off');
		$(this).append(input);
		return (input);
	};

	// [Issue 3830] : need to html decode the content provided by jeditable core for input text (see jeditable.authored, line 176)
	$.editable.types.text.content = function(string, settings, original) {
		 var decoded = $("<div/>").html(string).text();
			$(':input:first', this).val(decoded);
	};

	/**
	 * custom rich jeditable for the type 'ckeditor'. The plugin
	 * jquery.jeditable.ckeditor.js must have been called beforehand. The
	 * purpose of it is that we hook the object with additional handlers that
	 * will enable or disable hyperlinks with respect to the state of the
	 * editable (edit-mode or display-mode).
	 *
	 * It accepts one object for argument, with the regular options of a
	 * jeditable. : - this : a dom element javascript object. Not part of the
	 * settings. - url : the url where to post. - ckeditor : the config for the
	 * nested ckeditor instance - placeholder : message displayed when the
	 * content is empty - submit : text for the submit button - cancel : text
	 * for the cancel button
	 *
	 * Also accepts (simple) options passed as 'data-def' on the dom element.
	 * Note : options 'cols' and 'rows' can be set to 'auto', such dimensions
	 * will then be unbounded.
	 *
	 */

	$.widget('squash.richEditable', {

		options : confman.getJeditableCkeditor(),

		_init : function() {
			var defoptions = this.options;

			this.element.each(function(){
				var $this = $(this);
				var stropt = $this.data('def');
				var options = (!! stropt) ? attrparser.parse(stropt) : {};

				var finaloptions = $.extend(true, {}, defoptions, options);

				if (options.cols === "auto"){
					delete finaloptions.cols;
				}
				if (options.rows === "auto"){
					delete finaloptions.rows;
				}

				$this.editable(finaloptions.url, finaloptions);
			});
		}

	});

	// ripped from the now defunct-and-removed simple-editable.tag (see version 1.11 and prior)
	// if you like archeology)
	$.widget('squash.textEditable', {

		options : confman.getStdJeditable(),

		_init : function(){
			var defoptions = this.options;

			this.element.each(function(){
				var $this = $(this);

				// fix the text
				var txt = $this.text();
				$this.text( $.trim(txt) );

				// configure
				var stropt = $this.data('def');
				var options = (!! stropt) ? attrparser.parse(stropt) : {};
				var inbetweenoptions = {};
				if ($this.hasClass("large")) {
					inbetweenoptions.width = "100%";
				}

				var finaloptions = $.extend(true, inbetweenoptions, defoptions, options);

				finaloptions.onerror =  function(settings, self, xhr){
					var spanError = $("<span/>" ,{
						'class':'error-message'
					});
					self.reset();
					self.click();
					$(self).append(spanError);
					xhr.label = spanError;
					$(spanError).on("mouseover",function(){ spanError.fadeOut('slow').remove(); });
				};

				// enhance the callback if needed
				if (finaloptions.callback !== undefined){
					var oldc = finaloptions.callback;
					finaloptions.callback = function(value, settings){
						var fixedvalue = $("<span/>").html(value).text();
						// sometimes the callback can be passed as string representing,
						// the function name.
						// we must then look for this function.
						var call = (typeof oldc === "string") ? window[oldc] : oldc;
						window[oldc](fixedvalue, settings);
					};
				}

				// invoke
				$this.editable(finaloptions.url, finaloptions);

			});
		}
	});

});

