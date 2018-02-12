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
define(["handlebars","jquery.squash.bindviewformdialog","./NewProjectFromTemplateDialogModel","squash.translator"],
		function(Handlebars,BindViewFormDialog, NewProjectFromTemplateDialogModel, translator) {
	var View = BindViewFormDialog.extend({
		el : "#add-project-from-template-dialog-tpl",
		popupSelector : "#add-project-from-template-dialog",
		model : new NewProjectFromTemplateDialogModel(),

		initialize : function(){
			this.inactivateCheckBox();
		},

		compileFormDialog : function(){
			this._fetchTemplateList();
			var source = this.$el.html();
			var template =  Handlebars.compile(source);
			return template({
				items : this.collection.toJSON()
			});
		},

		events : {
			"change #add-project-from-template-template" : "changeTemplateSelection"
		},

		_fetchTemplateList : function(){
			this.collection.fetch({
				async : false,
				success : function(collection, response, options) {
						//adding the "no template" option, id=0 as no project can have the id 0 on server
						collection.add({id : "0" ,name : translator.get("label.noneDS")});
				}
			});
		},

		inactivateCheckBox : function(){
			this.$el.find("input:checkbox").prop("disabled",true);
			this.$el.find("input:checkbox").prop("checked",true);
		},

		activateCheckBox : function(){
			this.$el.find("input:checkbox").prop("disabled",false);
			this.$el.find("input:checkbox").prop("checked",true);
		},

		changeTemplateSelection : function(event){
			if (this.model.get("templateId")==="0") {
				this.inactivateCheckBox();
				this.model.set({fromTemplate : false});
			} else {
				this.activateCheckBox();
				this.model.set({fromTemplate : true});
			}
		},

		propagateSyncToProjectManager : function(){
			this.trigger("newproject.confirm");
		},

		onConfirmSuccess : function(){
			this.trigger("newproject.confirm");
			BindViewFormDialog.prototype.onConfirmSuccess.call(this);
		},

		onConfirmSuccessAndResetDialog : function(){
			this.trigger("newproject.confirm");
			BindViewFormDialog.prototype.onConfirmSuccessAndResetDialog.call(this);
		}
	});

	return View;
});
