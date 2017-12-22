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
define(["jquery", "underscore", "isIE",  "jqueryui"], function($, _, isIE){
	
	var searchwidget = $.widget("search.searchMultiCascadeFlatWidget", {
		options : {},
		
		_primarySelect : function(){
			return $(".multicascadeflat-primary", this.element);
		},
		
		_secondarySelect : function(){
			return $(".multicascadeflat-secondary", this.element);	
		},
		
		_create : function(){
			this._super();
			var self = this;
			
			var primarySelect = this._primarySelect(),
				secondarySelect = this._secondarySelect();	
			
			// Issue 4362 : have to delegate show/hide to 
			// a secondary object depending on the browser ability
			// if you think : "modernizr" I say : "**** u"
			
			var isie = isIE(),
				Manager = (isie) ? RetardedOptionDisplayManager : RegularOptionDisplayManager;
			
			this.options.isIE = isie;
			this.options.primaryManager = new Manager(primarySelect);
			this.options.secondaryManager = new Manager(secondarySelect);
		
						
			// add the on change handlers on the primary select
			primarySelect.on('change', function(){
				self.update();
			});
			
			// add some dots as separators between items belonging to different lists
			for (var i=0; i< this.options.lists.length -1 ; i++){
				var items= this.options.lists[i].subInput.possibleValues;
				var lastCode = items[items.length-1].code;
				
				secondarySelect.find('option[value="'+lastCode+'"]').css('border-bottom-style', 'dotted');
			}
		},
		
		fieldvalue : function(value){
			//case : getter
			if (!value){
				var filter = (this.options.isIE) ? ':selected' : ':visible:selected';
				var values = this._secondarySelect()
								 .find('option')
								 .filter(filter)
								 .map(function(i,e){ return e.value;})
								 .get();
				
				var max = this._secondarySelect().find('option').length;
				
				if (values && values.length === max){
					values = [];
				}
				
				return {type : 'LIST', values : values};
			}
			//case : setter
			else{
				var primarySelect = this._primarySelect(),
					secondarySelect = this._secondarySelect();
				
				primarySelect.find('option').removeAttr('selected');
				secondarySelect.find('option').removeAttr('selected');
				
				if (!!value.values){
					
					_.each(this.options.lists, function(primaryOpt){				
						_.each(primaryOpt.subInput.possibleValues, function(secondaryOpt){
							if (_.contains(value.values, secondaryOpt.code)){
								secondarySelect.find('[value="'+secondaryOpt.code+'"]').prop('selected', true);	
								primarySelect.find('[value="'+primaryOpt.code+'"]').prop('selected', true);
							}					
						});
					});
				}
			}
		},
		
		// ************* show / hide option boilerplate ************
		
		update : function(){
			var select = this._primarySelect().get(0);
			for (var i=0; i < select.length; i++){
				var opt = select[i];
				if (opt.selected){
					this.showSecondaryFrom(opt.value);
				}
				else{
					this.hideSecondaryFrom(opt.value);
				}				
			}			
		},
		
		showSecondaryFrom : function(primaryCode){
			this._loopAndApplySecondaryFrom(primaryCode, "show");
		},
		
		hideSecondaryFrom : function(primaryCode){
			this._loopAndApplySecondaryFrom(primaryCode, "hide");
		},
		
		hideAll : function(){
			var self = this;
			this._primarySelect().find('option').each(function(i, elt){
				self.options.primaryManager.hide(elt.value);
			});
			this._secondarySelect().find('option').each(function(i, elt){
				self.options.secondaryManager.hide(elt.value);
			});
		},
		
		showAll : function(){
			var self = this;
			this._primarySelect().find('option').each(function(i, elt){
				self.options.primaryManager.show(elt.value);
			});
			this._secondarySelect().find('option').each(function(i, elt){
				self.options.secondaryManager.show(elt.value);
			});			
		},
		
		hidePrimary : function(code){
			this.options.primaryManager.hide(code);			
			this.hideSecondaryFrom(code);
		},
		
		showPrimary : function(code){
			this.options.primaryManager.show(code);
			
			var primarySelect = this._primarySelect();			
			var opt = primarySelect.find("option[value='"+code+"']");
			if (opt.is(':selected')){
				this.showSecondaryFrom(code);
			}

		},
		
		_loopAndApplySecondaryFrom : function(primaryCode, methodname){
			var secondarySelect = this._secondarySelect();
			
			for (var pi=0; pi<this.options.lists.length;pi++){
				var primaryOpt = this.options.lists[pi],
					primaryValues = primaryOpt.subInput.possibleValues;
				
				if (primaryOpt.code === primaryCode){
					for (var si=0; si < primaryValues.length; si++){
						var subopt = primaryValues[si];
						this.options.secondaryManager[methodname](subopt.code);
						secondarySelect.find('option[value="'+subopt.code+'"]')[methodname]();
					}
				}
			}
		} 
		
	});
	
	
	// *********************** Issue 4362 handler *************************
	
	 function RegularOptionDisplayManager(select){
		this.select = select;
		this.show = function(code){
			this.select.find('option[value="'+code+'"]').show();
		};
		this.hide = function(code){
			this.select.find('option[value="'+code+'"]').hide();
		};
	}
	 
	/*
	 * Issue 4362
	 * 
	 * Internet explorer (any version) is unable to :
	 * - change the 'display' of an option of a select, 
	 * - answer correctly to predicates on visibility about them
	 * 
	 * Because of this, we have to explicitly remove 
	 * or add the options in the select.
	 * 
	 * We also have to maintain a state for the 
	 * secondary select options too.
	 * 
	 * Also, insert unprofessional comments here 
	 * on that retarded browser.
	 * 
	 */
	function RetardedOptionDisplayManager(select){
		
		this.select = select;
		this.indexes = [];
		this.reverseindexes = {};
		this.options = {};
		
		
		// ************* init **********
		var self = this;
		this.select.find('option').each(function(i, opt){
			self.indexes[i] = opt.value;
			self.reverseindexes[opt.value] = i;
			self.options[opt.value] = opt;			
		});
		
		
		// ********** methods ************
		
		this.show = function(code){
			
			// careful : 'option' here is a native js element, not jQuery
			var option = this.options[code];
			
			var insertionPoint = this.findInsertionPoint(option.value);
			
			if (insertionPoint.length > 0){
				$(option).insertBefore(insertionPoint);
			}
			else{			
				this.select.append(option);	
			}
		};
		
		this.hide = function(code){
			var option = this.select.find('option[value="'+code+'"]');
			option.detach();
		};
		
		// returns the option where to in
		this.findInsertionPoint = function(code){
			
			var select = this.select;
			
			var idxcode = this.reverseindexes[code];
			if (idxcode === this.indexes.length-1){
				return $();
			}
			
			var higherindexcodes = this.indexes.slice(idxcode+1);
			
			// prepare a jquerystring to locate options 
			// this option should be inserted before
			var querystring = "";
			higherindexcodes.forEach(function(code, idx){
				querystring += "option[value='"+code+"'], ";
			});
			
			// remove the extra comma then select
			querystring = querystring.substr(0, querystring.length-2);

			return select.find(querystring).first();	

		};
	}
	
	
	
	return searchwidget;
	
});