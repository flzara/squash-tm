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
 * This module exists because there are no satifsying jeditable password plugin.
 * 
 * The constructor accepts as argument an url and a regular jeditable configuration.
 * Of course not all options are supported.
 *  
 */


define(["jquery", "squash.translator"], function($, translator){
	
	if ($.fn.jedpassword !== undefined){
		return;
	}
	
	
	// bare init code
	$.fn.jedpassword = function(url, edconf){
		
		var $this = $(this);
		
		var password = $this.text();
		$this.data('password', password);

		
		$.extend($this, skeletton, {jedp_url : url, jedp_conf : edconf});
		
		$this.jedp_init();
		
	};

	
	/*
	 *  now the main thing
	 *  
	 *  Note that each and every properties of this object has a prefix 'jedp_' that I hope 
	 *  wont conflict with other properties of the raw jQuery object we attempt to merge with
	 *  in the constructor 'jedpassword' above.
	 */
	var skeletton = {
			
		jedp_url : null,
		jedp_conf : {
			name : "value"
		},
		jedp_inputblock : null,
		jedp_input : null,
			
		
		
		jedp_init : function(){
			var self = this;
			
			this.jedp_initControls();
			this.jedp_displayMode();
			
			this.on('click', function(){
				self.jedp_editMode();
			});
		},
		
		jedp_displayMode : function(){
			var pass = this.data('password') || "";
			var stars = new Array(pass.length+1).join('*');
			this.text(stars);
			
			this.show();
			this.jedp_inputblock.hide();
		},
		
		jedp_editMode : function(){
			var pass = this.data('password');
			this.jedp_input.val(pass);
			
			this.hide();
			this.jedp_inputblock.show();
		},
		
		jedp_initControls : function(){
			var self = this;
			var _lang = translator.get({
				confirmLabel : 'label.Confirm',
				cancelLabel : 'label.Cancel'
			});
			
			this.jedp_inputblock = $("<div/>");
			
			var input = $('<input type="password">');
			var confirm = $('<input type="button" value="' + _lang.confirmLabel + '">');
			var cancel = $('<input type="button" value="' + _lang.cancelLabel + '">');
			
			input.on('keydown', function(e){				
				var code = e.keyCode || e.which;
				
				 if(code == 13) { 
					 self.jedp_fnConfirm();
				 }
			});
			
			confirm.on('click', function(){
				self.jedp_fnConfirm();
			});

			cancel.on('click', function(){
				self.jedp_displayMode();
			});
			
			this.jedp_inputblock.append(input, confirm, cancel);
			this.jedp_input = input;
			
			this.jedp_inputblock.on('blur', function(){
				self.jedp_fnConfirm();
			});
			
			this.jedp_inputblock.hide().insertBefore(this);
			
		},
		
		jedp_fnConfirm : function(){
			var self = this;
			var newPassword = self.jedp_input.val();
			
			var params = {};
			params[self.jedp_conf.name] = newPassword;
		
			$.ajax({
				url : self.jedp_url,
				type : 'post',
				data : params
			})
			.success(function(newP){
				self.data('password', newPassword);
				self.jedp_displayMode();
			})
			.error(function(){
				self.jedp_displayMode();
			});			
		}
		
			
	}; 
	
	
});