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
define([ "jquery.squash.bindviewformdialog","./NewTemplateDialogModel", "jquery.squash"],
		function(BindViewFormDialog, NewTemplateDialogModel) {
	"use strict";

	var templateFormDialog = BindViewFormDialog.extend({
		el : "#add-template-dialog-tpl",
		popupSelector : "#add-template-dialog",
		model : new NewTemplateDialogModel(),

		//overriding callConfirm method of BindViewFormDialog to have redirection after the save success
		callConfirm : function(){
			this.updateModelFromCKEditor();
			this.model.save().success(function(response, status, options){
				document.location.href = response.url;
			});
		},

		onConfirmSuccessAndResetDialog : function(){
			this.trigger("newtemplate.confirm");
			BindViewFormDialog.prototype.onConfirmSuccessAndResetDialog.call(this);
		}

	});

	return templateFormDialog;

});
