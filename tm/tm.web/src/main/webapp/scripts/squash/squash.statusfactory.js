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
 *API :
 *
 *	translate(statusName) -> returns the i18n version of this status name
 *	reverseTranslate(i18n) -> return the real name of the status given its translation
 *	getHtmlFor(status) -> given a statusname OR its translation, returns a html string to render it
 *	getIconFor(status) -> given a statusname OR its translation, returns the corresponding icon
 *
 */
define(["squash.translator"], function(translator){
	"use strict";
	var statusKeys = {
			UNTESTABLE : "execution.execution-status.UNTESTABLE",
			SETTLED : "execution.execution-status.SETTLED",
			BLOCKED : "execution.execution-status.BLOCKED",
			FAILURE : "execution.execution-status.FAILURE",
			SUCCESS : "execution.execution-status.SUCCESS",
			RUNNING : "execution.execution-status.RUNNING",
			READY	: "execution.execution-status.READY",
			WARNING : "execution.execution-status.WARNING",
			NOT_RUN : "execution.execution-status.NOT_RUN",
			NOT_FOUND : "execution.execution-status.NOT_FOUND",
			ERROR : "execution.execution-status.ERROR"
		};

	// async init of messages
	translator.load(statusKeys);
	


	return {
		

		/*
		 * PUBLIC API
		 *
		 */

		getHtmlFor : function(status){

			var css,
				text;

			// lets check whether the argument is a real status name or a translation
			var realStatusName = this._findRealStatusName(status);

			// process if found
			if (!! realStatusName){
				css = 'exec-status-' + realStatusName.toLowerCase();
				text = this.translate(realStatusName);

				return '<span class="exec-status-label ' + css + '">' + text + '</span>';
			} else {
				return status;
			}
		},
		
		getIconFor : function(status){

			var css,
				text;

			// lets check whether the argument is a real status name or a translation
			var realStatusName = this._findRealStatusName(status);

			// process if found
			if (!! realStatusName){
				css = 'exec-status-' + realStatusName.toLowerCase();
				return '<span class="exec-status-label ' + css + '"></span>';
			} else {
				return "NOT_FOUND";
			}
		},

		translate : function(statusName){
			return translator.get(statusKeys[statusName.toUpperCase()]);
		},

		reverseTranslate : function(translated){
			for (var ppt in statusKeys){
				if (translator.get(statusKeys[ppt])===translated){
					return ppt;
				}
			}
			return undefined;
		},
		

		/*
		 * PRIVATE
		 * 
		 */
		
		_findRealStatusName : function(status){
			return (statusKeys[status.toUpperCase()] !== undefined) ? status : this.reverseTranslate(status);
		}
	};
});