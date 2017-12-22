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
 * This simple uploader will create a pseudo form and iframe, put the file uploads in there, set their name to "attachment[]", and sumbit to url
 *  targetting the pseudo iframe, and finally remove all of them once the job is done. See http://www.ajaxf1.com/tutorial/ajax-file-upload-tutorial.html.
 */

define(["jquery", "jform"], function($){

	
	function _detachUnrelatedInputs(form){
		var unrelatedInputs = form.find('input').filter(function(){
			
			var unrelated = true;
			var $this = $(this);
			
			if ($this.attr('type') === 'file'){
				var content = $this.val();
				if (content !== ""){
					unrelated = false;	//this input is actually very relevant
				}
			}
			
			return unrelated;
			
		});
		
		unrelatedInputs.detach();
		
		return unrelatedInputs;
	}
	
	
	function _uploadFilesOnly(form, url){
		
		var notToBePosted = _detachUnrelatedInputs(form);

		//sets the name of the remaining inputs
		form.find('input').attr('name', 'attachment[]');
		
		//don't post if there is nothing to send
		if (form.find('input').length>0){
			form.ajaxSubmit({
				url : url,
				iframe : true,
				type : 'POST'
			});
		}
		
		//now reattach the detached inputs
		form.append(notToBePosted);

	}

	return {
		uploadFilesOnly : function(arg, url){
			
			var $arg = (arg instanceof jQuery) ? arg : $(arg);
			
			if ($arg.is('form')){
				_uploadFilesOnly($arg, url);
			}
			
			else {
				throw "argument is neither a form nor an input file";
			}
			

		}
	};
	
});