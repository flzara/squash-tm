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
/**
 * Documentation
 *
 * This module provide support to create formdialogs on top of Backbone views/model and app/BindView.
 *
 * The main benefits are :
 *-Reducing boilerplate code in dialogs
 *-Avoid bugs with dialog reuse, as the View and the model are instancied each time the popup is opened
 *-Avoid bugs with the "AddAnother" as the View is retemplated and the model is cleared.
 * As this module use jquery.squash.formdialog and BindView, see doc of these two modules for additionnal information.
 *
 * HTML Structure in a JSP page:
 *
 *<script id="my-form-dialog-tpl" type="text/x-handlebars-template">
  <div id="my-form-dialog" class="not-displayed popup-dialog form-horizontal" title="<f:message key='key' />">
    <table class="form-horizontal">
      <tr class="control-group">
        <td>
          <label class="control-label" for="my-form-name">
            <f:message key="label.Name" />
          </label>
        </td>
        <td class="controls">
          <input id="my-form-name" name="my-form-name" type="text" size="50" maxlength="255" data-prop="name" data-object="myObjet"/>
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
    </table>

    <div class="popup-dialog-buttonpane">
      <input class="confirm" type="button" value="<f:message key='label.Add' />" data-def="evt=confirm, mainbtn"/>
      <input class="cancel" type="button" value="<f:message key='label.Close' />" data-def="evt=cancel"/>
    </div>
  </div>
</script>
 *
 *
 * Note the two additional attributes in <input> :
 * data-prop -> Bind the value of the input to the data-prop value in backbone model. In the example, an attribute {name : value} will be added to the model.
 *              This value is binded to user input and will be updated each time user input change (See app/bindView for details)
 *
 * data-objet -> Class of the @RequestBody Object in spring MVC controller. Used by tm.notification to show error message.
 *              Ignore this attribute on fields who don't require server side validation.
 *
 * data-default-value -> Allow to set model with an initial value for this input. Use it for select box or other input that require a value to be set without any user action.
 *
 * ==================================
 *
 * Using this module :
 *
 * See ProjectsManager.js, NewTemplateDialog.js and NewTemplateDialogModel.js for a simple application
 * See ProjectsManager.js, NewProjectFromTemplateDialog.js and NewProjectFromTemplateDialogModel.js for a more complex customisation
 * (custom templating inside dialog, additionnal events...)
 *
 * For these two examples JSP page is show-projects.jsp
 *
 * In the view two attribute must be declared :
 * el -> initially el is the template selector
 * popupSelector -> the selector of dialog's main div
 *
 * */
define([ "jquery","backbone", "app/BindView", "backbone", "underscore", "handlebars", "app/lnf/Forms",'squash.configmanager', "jqueryui","jquery.squash","jquery.squash.formdialog",
		"jquery.squash.formdialog" ], function($, Backbone,BindView, BackBone,_, Handlebars, Forms, confman) {
	"use strict";


	var BindViewFormDialog = BindView.extend({
		initialize : function(){
			this._construct();
			this._initDefaultValues();
			_.bindAll(this, 'onConfirmSuccess','onConfirmSuccessAndResetDialog');
		},

		//Override with caution as you need to follow the same logic of templating
		//and initialization of jQuery dialog
		render : function(){

		},

		//Override if additional/other templating is required
		compileFormDialog : function(){
			var source = this.$el.html();
			return Handlebars.compile(source);
		},

		//BindViewFormDialog come with a default backbone model
		//Override with a custom Backbone.Model if required but don't change the variable name !
		model : new Backbone.Model(),

		//events for buttons.
		events : {
			"formdialogcancel" : "callRemove",
			"formdialogclose" : "callRemove",
			"formdialogconfirm" : "callConfirm",
			"formdialogaddanother" : "callConfirmAndResetDialog"
		},

		callRemove : function(){
			var defaultModel = this.model.defaults;
			if (defaultModel!==undefined && defaultModel!==null) {
				this.model.clear();
				if (typeof defaultModel === "function") {
					this.model.set(this.model.defaults());
				}
				else {
					this.model.set(this.model.defaults);
				}
			}
			else {
					this.model.clear();
			}
			this.$el.formDialog("destroy");
			this.stopListening();
			this.remove();
		},

		//Override only if data from response is needed or partial saving necessary
		//If only different behavior in view after request success is needed, override onConfirmSuccess and onConfirmAndResetPopupSuccess.
		callConfirm : function(){
			//hacking the ckeditor witch is unbindable in bind view
			//TODO case of popup with several rich editor inside
			this.updateModelFromCKEditor();
			this.model.save(null,{
				success : this.onConfirmSuccess
			});
		},

		callConfirmAndResetDialog : function(){
			this.updateModelFromCKEditor();
			this.model.save(null,{
				success : this.onConfirmSuccessAndResetDialog
			});
		},

		onConfirmSuccess : function(){
			this.callRemove();
		},

		onConfirmSuccessAndResetDialog : function(){

			this.callRemove();
			this._elOnTemplate();
			this.initialize();
		},

		updateModelFromCKEditor : function () {
			var editors  = this.$el.find("textarea[data-prop][data-def='isrich']");
			var dataProp = editors.attr("data-prop");
			var value = editors.val();
			this.model.set(dataProp,value);
		},

		//**************** PRIVATE STUFF ********************//

    _construct : function(){
      var template = this.compileFormDialog();
      //dirty because a backbone view shouldn't manipulate element outside of it's scope.
      //but with the (in)famous jquery UI dialog, witch make your HTML tags fly all over the DOM, we have no choice...
      $("body").append(template);
      this.tplSelector = this.$el.selector;
      this._elOnDialog();
      this.$el.formDialog();
      this.$el.formDialog("open");
      return this;
    },

		_elOnDialog : function(){
			this.setElement(this.popupSelector);
		},

		_elOnTemplate : function(){
			this.setElement(this.tplSelector);
		},

		_initDefaultValues : function () {
			var that = this;
			var inputsWithDefaultValues = this.$el.find("[data-prop][data-default-value]").each(function (index,input) {
				var attName = $(input).attr("data-prop");
				var defaultValue = $(input).attr("data-default-value");
				that.model.set(attName, defaultValue);
			});
		}

	});

	//now overriding BindViewFormDialog extend function to mix events in a childview derivated from newBindViewFormDialog
	BindViewFormDialog.extend = function(protoProps, staticProps){
		//call the Bindview extend function to "inherit" from BindView
		var view = BindView.extend.call(this,protoProps,staticProps);
		//overriding events prop in child view with an _extend of events already in BindViewFormDialog(event for buttons and events coming from BindView)
		//and events in child view (protoProps.events)
		view.prototype.events = _.extend({}, this.prototype.events, protoProps.events);
		//return the childView
		return view;
	};

	return BindViewFormDialog;
});
