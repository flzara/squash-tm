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
define([ "jquery" ], function($) {

	return {
		// uses a well known trick on the internet
		extractPath : function(rawurl){
			var url = document.createElement('a');
			url.href = rawurl;

			//Issue 4998 IE don't put / at the start of the url so the match fail. Add / at first if it not the first char
			return url.pathname.charAt(0) != '/' ? '/' + url.pathname : url.pathname;			
		},
		
		extractParameters : function(rawurl){
			var url = document.createElement('a');
			url.href = rawurl;
			
			var querystr = url.search;
			var pairs = querystr.replace(/^\?/,'').split('&').map(function(e){return e.split('=');});
			
			var ret = {};
			pairs.forEach(function(p){
				ret[p[0]]=p[1];
			});
			
			return ret;
		}
	};
	
});
