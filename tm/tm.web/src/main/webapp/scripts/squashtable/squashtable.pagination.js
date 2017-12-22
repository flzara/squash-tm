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
define(['jquery','jqueryui', 'datatables'], function($){
	
	$.fn.dataTableExt.oPagination.iFullNumbersShowPages = 1;

	$.fn.dataTableExt.oPagination.squash = {
		/*
		 * Function: oPagination.squash.fnInit Purpose: Initalise dom elements required for pagination with a list of
		 * the pages Returns: - Inputs: object:oSettings - dataTables settings object node:nPaging - the DIV which
		 * contains this pagination control function:fnCallbackDraw - draw function which must be called on update
		 */
		"fnInit" : function(oSettings, nPaging, fnCallbackDraw) {

			var initButton = function(object, cssClass) {
				object.button({
					text : false,
					icons : {
						primary : cssClass
					}
				});
			};

			var nFirst = $('<span />', {
				'class' : 'paginate_button first'
			});
			var nPrevious = $('<span />', {
				'class' : 'paginate_button previous'
			});
			var nNext = $('<span />', {
				'class' : 'paginate_button next'
			});
			var nLast = $('<span />', {
				'class' : 'paginate_button last'
			});
			var nPageTxt = $("<span />", {
				text : parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength) / oSettings._iDisplayLength)
			});

			$(nPaging).append(nFirst).append(nPrevious).append(nPageTxt).append(nNext).append(nLast);

			// utf8 code for character "◀" = U25C0 and for "▶" = U25B6
			nFirst.text("◀◀");
			nPrevious.text("◀");
			nNext.text("▶");
			nLast.text("▶▶");

			$(nPaging).find('.paginate_button').button({
				disabled : false,
				text : true
			});

			nFirst.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "first");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength) / oSettings._iDisplayLength));
			}).bind('selectstart', function() {
				return false;
			});

			nPrevious.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "previous");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength)/ oSettings._iDisplayLength));
			}).bind('selectstart', function() {
				return false;
			});

			nNext.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "next");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength) / oSettings._iDisplayLength));
			}).bind('selectstart', function() {
				return false;
			});

			nLast.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "last");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength) / oSettings._iDisplayLength));
			}).bind('selectstart', function() {
				return false;
			});

		},

		/*
		 * Function: oPagination.extStyle.fnUpdate Purpose: Update the list of page buttons shows Returns: - Inputs:
		 * object:oSettings - dataTables settings object function:fnCallbackDraw - draw function which must be called on
		 * update
		 */
		"fnUpdate" : function(oSettings, fnCallbackDraw) {
			if (!oSettings.aanFeatures.p) {
				return;
			}

			/* Loop over each instance of the pager */
			var an = oSettings.aanFeatures.p;

			for ( var i = 0, iLen = an.length; i < iLen; i++) {
				// var buttons = an[i].getElementsByTagName('span');
				var buttons = $(an[i]).find('span.paginate_button');
				if (oSettings._iDisplayStart === 0) {
					buttons.eq(0).button("option", "disabled", true);
					buttons.eq(1).button("option", "disabled", true);
					$(an[i]).find(">span").eq(2).text("1");
				} else {
					buttons.eq(0).button("option", "disabled", false);
					buttons.eq(1).button("option", "disabled", false);
				}

				if (oSettings.fnDisplayEnd() == oSettings.fnRecordsDisplay()) {
					buttons.eq(2).button("option", "disabled", true);
					buttons.eq(3).button("option", "disabled", true);
				} else {
					buttons.eq(2).button("option", "disabled", false);
					buttons.eq(3).button("option", "disabled", false);
				}
			}
		}
	};
	
	return $.fn.dataTableExt.oPagination.squash;
});