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
define(["jquery.squash.bindviewformdialog","squash.translator"],
		function(BindViewFormDialog, translator) {

	var View = BindViewFormDialog.extend({
		el : "#add-template-from-project-dialog-tpl",
		popupSelector : "#add-template-from-project-dialog",

		initialize : function(){
			this.activateCheckBox();
			this.templateWithProjectName();
			this.templateWithProjectDescAndLabel();
		},

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
		},

		inactivateCheckBox : function(){
			this.$el.find("input:checkbox").prop("disabled",true);
			this.$el.find("input:checkbox").prop("checked",true);
		},

		activateCheckBox : function(){
			this.$el.find("input:checkbox").prop("disabled",false);
			this.$el.find("input:checkbox").prop("checked",true);
		},

		templateWithProjectName : function () {
			var sentence = translator.get("dialog.message.templateFromProject");
			sentence = sentence + " " + this.model.originalProjectName;
			this.$el.find("#templateFromProjectMessage").html(sentence);
		},

		templateWithProjectDescAndLabel : function () {
			var desc = this.model.get("description");
			if (this.validString(desc)) {
				this.model.set("description",this.concatWithTemplatePrefix(desc));
				this.$el.find("#add-template-from-project-description").val(this.model.get("description"));
			}

			var label = this.model.get("label");
			if (this.validString(label)) {
				this.model.set("label",this.concatWithTemplatePrefix(label));
				this.$el.find("#add-template-from-project-label").val(this.model.get("label"));
			}
		},

		concatWithTemplatePrefix : function (message) {
			if (this.prefix===undefined) {
				this.prefix=translator.get("dialog.templateFromProject.prefix");
			}
			return this.prefix + " " + message;
		},

		validString : function (str) {
			return str!==undefined && str!==null && str.length!==0;
		}

	});

	return View;
});
