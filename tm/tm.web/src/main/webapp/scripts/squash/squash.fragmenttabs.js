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
 * Controller for tabs in entity tragment (test case, requirement...)
 *
 * requires : - jquery - jqueryui - jquery cookie plugin
 */

define(["jquery", "jqueryui", "jquery.cookie"], function($){


	squashtm = squashtm || {};


	return {
		init : function() {

			var cookieConf;
			if (arguments.length > 0){
				cookieConf = arguments[0].cookie;
			}


			var args = {
					cache : true,
					active: 0
				};

			if(!!cookieConf){
				var cookieVal = $.cookie(cookieConf.name);
				if (cookieVal){
					args.active = parseInt(cookieVal,10);
				}
			}

			if (arguments.length > 0) {
				args = $.extend(args, arguments[0]);
			}

			$('.fragment-tabs').tabs(args);
		}
	};
});
