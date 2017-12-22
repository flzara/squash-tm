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
define(["jquery", "underscore", "jqueryui"], function($, _){

	var searchwidget = $.widget("search.searchMultiAutocompleteWidget", {
		options : {
			fieldId : "",
			options : []
		},
		
		split : function( val ) {
			return val.split( /,\s*/ );
		},
		
		extractLast : function ( term ) {
			return this.split( term ).pop();
		},
		
		_create : function(){
			var self = this;
			this._super();
			self.availableTags = _.pluck(self.options.options, 'value');

			self.input = $( "#"+this.options.fieldId+" input" );
			
			// don't navigate away from the field on tab when selecting an item
			self.input.bind( "keydown", function( event ) {
					if ( event.keyCode === $.ui.keyCode.TAB &&
							$( this ).data( "ui-autocomplete" ).menu.active ) {
						event.preventDefault();
					}
			})
			.autocomplete({
				minLength: 0,
				source: function( request, response ) {
					// delegate back to autocomplete, but extract the last term
					var terms = self.split( request.term);
					terms.pop();
					var nonSelectedTags = _.difference(self.availableTags, terms ) ;
					response( $.ui.autocomplete.filter(
							nonSelectedTags, self.extractLast( request.term ) ) );
					},
			focus: function() {
			// prevent value inserted on focus
			return false;
			},
			select: function( event, ui ) {
					var terms = self.split( this.value );
					// remove the current input
					terms.pop();
					// add the selected item
					terms.push( ui.item.value );
					// make the terms an array of unique values
					terms = _.uniq(terms);
					// add placeholder to get the comma-and-space at the end
					terms.push( "" );
					this.value = terms.join( ", " );
					return false;
			}
		});},
		
		fieldvalue : function(value){
			var self = this;
			if(!value){//it's a get
				var text = self.input.val();
				var terms = self.split(text);
				var values = _.difference(terms, [""]);
				if(values.length){
					return {"type" : "LIST", "values" : terms};
				}
			} else { //it's a set
				self.input.val("");
				if (!!value.values){
					self.input.val(value.values.join( ", " ));
				}
			}
		}
	});
	return searchwidget;
});
