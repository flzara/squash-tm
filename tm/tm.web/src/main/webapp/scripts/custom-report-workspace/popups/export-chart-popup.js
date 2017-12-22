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
define(['jquery', 'tree', '../permissions-rules', 'jquery.squash.formdialog'], function($, zetree, rules){


	function init(){

    var minHeight = $(document).height()*0.75;
    var minWidth = $(document).width()*0.75;

		var dialog = $("#export-chart-dialog").formDialog({
						minHeight: minHeight,
						minWidth : minWidth,
		});

		// Added to cancel the open if no rights
		dialog.on('formdialogopen', function(){
		var str = '<img src='+$("#chart-display-area").jqplotToImageStr({})+' />';
			$("#exported-chart").html(str);
		});

		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});

	}

	return {
		init : init
	};

});
