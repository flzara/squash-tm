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
 *	Settings :
 *	{
 *		target : a css selector to the target table,
 *		urls : {
 *			bugtracker : the base url of the Squash bugtracker service
 *		},
 *		language : {
 *			removeMessage : the text in the remove popup confirm,
 *			removeTooltip : the text of the tooltip of the remove button
 *		}
 *  }
 *
 *  @returns : the squashTable instance of this table.
 */
define(["jquery", "squashtable"], function($){
	var initTSTable = function(oSettings){
		var settings = $.extend({}, oSettings);	//local copy of the arguments
		var tblSelector = settings.target;
		var squashSettings = {
			deleteButtons : {
				url : settings.urls.bugtracker+'/issues/{local-id}',
				popupmessage : settings.language.removeMessage,
				tooltip : settings.language.removeTooltip,
				success : function(data) {
					$(tblSelector).squashTable().refresh();
				}
			}
		};

		return $(tblSelector).squashTable(settings.tblSettings, squashSettings);
	};

	return{
		initTestStepIssueTable : initTSTable
	};
});