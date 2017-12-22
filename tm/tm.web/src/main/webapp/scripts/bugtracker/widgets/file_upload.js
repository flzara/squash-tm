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
define(["jquery", "../domain/FieldValue", "squash.translator", "handlebars", "file-upload"], function($, FieldValue, translator, Handlebars){


	//TODO : move this to an external template and load it via "text!" loader
	var itemTemplate = '<div class="attachment-templates" style="display:none;">'+
							'<div class="attachment-item"> <input type="file" size="40"/> <input type="button" value="{{removeLabel}}"></div>'+
						'</div>';

	return {

		options : {
			rendering : {
				inputType : {
					name : "file_upload"
				}

			}
		},

		_generateId : function(){
			return "_fileupload-"+Math.random().toString().substr(2,3);
		},


		_create : function(){

			var delegate = this._createDelegate();

			var itemTemplate = this.element.find('div.attachment-templates div.attachment-item');

			//this is NOT a jquery widget, fetching and caching the instance of the object created that way is important because
			//we cannot query for it later.
			this._delegateWidget = delegate.multiFileupload(itemTemplate);
			this._delegateWidget.clear();

		},

		_getDelegate : function(){
			return this.element.find('form.bt-delegate');
		},

		_createDelegate : function(frameId){
			var elt = $('<form class="bt-delegate"/>');
			this.element.append(elt);
			return elt;
		},

		fieldvalue : function(fieldvalue){
			//weeeeell, very special case here.
		},

		disable : function(){
			this._getDelegate().find("input").prop('disabled', true);
		},

		getForm : function(){
			return this._getDelegate();
		},

		createDom : function(field){

			var removeButtonLabel = translator.get("dialog.attachment.add.button.remove.label");

			var compiledTemplate = Handlebars.compile(itemTemplate);
			var processedTemplate = compiledTemplate({removeLabel : removeButtonLabel});

			var mainElt = $('<div/>', {
				'data-widgetname' : 'file_upload',
				'data-fieldid' : field.id,
				'style' : 'border : 1px solid lightgray; display : inline-block'
			});

			var templateElt = $(processedTemplate);

			mainElt.append(templateElt);

			return mainElt;
		}
	};
});
