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
 * Initialize the ajax spinner.
 */
var squashtm = squashtm || {};

define([ "jquery", "app/ws/squashtm.notification" ], function($, notification) {
    
	var _spinner = "#ajax-processing-indicator";
	var _spinnerInitialized = false;

	function initSpinner() {

		if (_spinnerInitialized){
			return;
		}
		
		_spinnerInitialized = true;
		
                
		var $doc = $(document);

		/*
		 * Does not work with narrowed down selectors. see http://bugs.jquery.com/ticket/6161
		 */
		$doc.on('ajaxError', function(event, request, settings, ex) {

			// nothing to notify if the request was aborted, or was treated elsewhere
			if (request.status === 0 || request.errorIsHandled === true) {
				return;
			}

			// Check if we get an Unauthorized access response, then
			// redirect to login page
			else if (401 == request.status) {
				window.parent.location.reload();
			} else {
				try {
					notification.handleJsonResponseError(request);
				} catch (wtf) {
					notification.handleGenericResponseError(request);
				}
			}
		});

		$.ajaxPrefilter(function(options, _, jqXHR) {
			$(_spinner).addClass("processing").removeClass("not-processing");

			jqXHR.always(function() {
				$(_spinner).removeClass("processing").addClass("not-processing");
			});
		});
                
                
                // styling 
                $(".unstyled-notification-pane").addClass("notification-pane").removeClass("unstyled-notification-pane");
		$(_spinner).addClass("not-processing").removeClass("processing");

	}
        
        return {
            init : initSpinner
        };
});
