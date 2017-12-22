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
define(["jquery", "jquery.squash" ], function($) {
	"use strict";

	function _dryRunStart(url){
		return $.ajax({
			url : url,
			method : "get",
			dataType : "json",
			data : {
				"dry-run" : ""
			}
		});
	}

	function _runInPopup(url){
		window.open(url+'?optimized=false', "classicExecutionRunner", "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised");
	}

	function _runInOER(url){
		var realUrl = url + '?optimized=true&suitemode=false';
		var win = window.open(realUrl, "_blank");
		win.focus();
	}

	return {

		runInPopup : function(url){
			_dryRunStart(url)
			.done(function(){
				_runInPopup(url);
			});
		},

		runInOER : function(url){
			_dryRunStart(url)
			.done(function(){
				_runInOER(url);
			});
		}
	};

});