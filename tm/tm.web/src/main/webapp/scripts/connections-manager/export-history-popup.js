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
define(['jquery',
        'squash.dateutils',
        'jquery.squash.formdialog'],
		function($, dateutils){

	$.widget("squash.exportDialog", $.squash.formDialog, {

		_create : function(){
			this._super();

			var self = this;

			this.onOwnBtn('cancel', function(){
				self.close();
			});

			this.onOwnBtn('confirm', function(){
				self.confirm();
			});

		},

		open : function(){
			this._super();
      var name = this._createName();
      $('#export-name-input').val(name);
			this.setState('main');

		},

		_createName : function(){
			return this.options.nameprefix+"_"+ dateutils.format(new Date(), this.options.dateformat);
		},

		//REQUIREMENT EXPORT URL
		_createUrl : function(name, loginFilter, dateFilter){
		  var table = $("#connections-table").squashTable();
		  //Drawing to be sure that oAjaxData exist (not the case if no filtering or sorting before export)
		  table.fnDraw();
			var url = squashtm.app.contextRoot + 'administration/connections/exports';

			var params = table.fnSettings().oAjaxData;
			params["filename"] = name;

			return url+"?"+$.param(params);

		},

		confirm : function(){
        var filename = $('#export-name-input').val();
        var loginFilter = $('#login_filter_input').val();
        var dateFilter = $('#date_filter_input').val();
        var url = this._createUrl(filename, loginFilter, dateFilter);
        document.location.href = url;
        this.close();
		}
	});


	function init(){

		var dialog = $("#export-connection-history-dialog").exportDialog({width : 600});

	}


	return {
		init : init
	};

});
