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
define([ "jquery", "./NewCustomFieldOptionDialog", "backbone", "underscore",
	"jeditable.simpleJEditable", "jeditable.selectJEditable", "app/util/StringUtil", "app/lnf/Forms", 
	"jquery.squash.oneshotdialog", "squash.configmanager", "app/ws/squashtm.notification", "squash.translator", 
	"jquery.squash", "jqueryui",
	"jquery.squash.togglepanel", "squashtable", "jquery.squash.confirmdialog", "jeditable.datepicker", 
	"jquery.squash.formdialog", "jquery.squash.tagit"  ],
	function($, NewCustomFieldOptionDialog, Backbone, _, SimpleJEditable,SelectJEditable, StringUtil, Forms, oneshot, confman, notification, translator) {
		var cfMod = squashtm.app.cfMod;
		/*
		 * Defines the controller for the custom fields table.
		 */
		var CustomFieldModificationView = Backbone.View.extend({
			el : "#information-content",
			initialize : function() {
				this.inputType = $("#cuf-inputType").data("type");

				this.optionalCheckbox = this.$("#cf-optional").get(0);

				this.configureEditables();
				this.configureRenamePopup();
				this.configureRenameOptionPopup();
				this.configureChangeOptionCodePopup();
				this.configureOptionTable();
				this.configureButtons();
				// this line below is here because toggle panel
				// buttons
				// cannot be bound with the 'events' property of
				// Backbone.View.
				// my guess is that the event is bound to the button
				// before it is moved from it's "span.not-displayed"
				// to the toggle panel header.
				// TODO change our way to make toggle panels buttons
				this.$("#add-cuf-option-button").on("click",
						$.proxy(this.openAddOptionPopup, this));

				// dialog is moved from DOM when widgetized => we
				// need to store it
				this.confirmDeletionDialog = this.$(
						"#delete-warning-pane").confirmDialog();
				// ...and we cannot use the events hash
				this.confirmDeletionDialog.on(
						"confirmdialogconfirm", $.proxy(
								this.deleteCustomField, this));
			},

			events : {
				"click #cf-optional" : "confirmOptional",
				"click .is-default>input:checkbox" : "changeDefaultOption",
				"click .opt-label" : "openRenameOptionPopup",
				"click .opt-code" : "openChangeOptionCodePopup",
				"click #delete-cuf-button" : "confirmCustomFieldDeletion"
			},

			confirmCustomFieldDeletion : function(event) {
				this.confirmDeletionDialog.confirmDialog("open");
			},

			deleteCustomField : function(event) {
				var self = this;

				$.ajax({
					type : "delete",
					url : document.location.href
				}).done(function() {
					self.trigger("customfield.delete");
				});

			},

			confirmOptional : function(event) {
				var self = this;
				var checked = event.target.checked;

				if (checked) {
					self.sendOptional(checked);

				} else {
					var defaultValue = self.findDefaultValue();
					if (StringUtil.isBlank(defaultValue) ||
						defaultValue === cfMod.richEditPlaceHolder ||
						defaultValue === cfMod.noDateLabel) {
							notification.showError(cfMod.mandatoryNeedsDefaultMessage);
							event.target.checked = true;
							return;
					}
					var message = cfMod.confirmMandatoryMessage;
					message = self.replacePlaceHolderByValue(0,
							message, defaultValue);
					oneshot.show(cfMod.confirmMandatoryTitle,
							message, { width : '500px'} )
							.done(function() {
								self.sendOptional(checked);
							})
							.fail(function() {
								event.target.checked = true;
							});
				}
			},

			findDefaultValue : function() {
				var defaultValueDiv = this.$('#cuf-default-value');

				if (defaultValueDiv && defaultValueDiv.length > 0) {
					return $(defaultValueDiv[0]).text();

				} else if (this.optionsTable) {
					var checkedDefault = this.optionsTable
							.find('td.is-default input:checked');
					if (checkedDefault) {
						return checkedDefault.val();
					}
				}
				return "";
			},

			replacePlaceHolderByValue : function(index, message,
					replaceValue) {
				var pattern = /\{[\d,\w,\s]*\}/;
				var match = pattern.exec(message);
				var pHolder = match[index];
				return message.replace(pHolder, replaceValue);
			},

			sendOptional : function(optional) {
				var self = this;
				return $.ajax({
					url : cfMod.customFieldUrl + "/optional",
					type : "post",
					data : {
						'value' : optional
					},
					dataType : "json"
				})
				.fail(function(){
					self.cancelOptionalChange();
				});

			},

			changeDefaultOption : function(event) {
				var self = this;
				var checkbox = event.currentTarget;
				var option = checkbox.value;
				var defaultValue = checkbox.checked ? option : "";
				if (defaultValue === "" && this.isFieldMandatory()) {
					checkbox.checked = true;
					notification.showError(cfMod.defaultOptionMandatoryMessage);
					return;
				}
				var uncheckSelector = ".is-default>input:checkbox" + (checkbox.checked ? "[value!='" + option + "']" : "");

				
				$.ajax({
					url : cfMod.customFieldUrl + "/defaultValue",
					type : 'POST',
					data : {
						'value' : defaultValue
					}
				}).done(function() {
					self.optionsTable.find(uncheckSelector).attr("checked", false);
				}).fail(function() {
					checkbox.checked = !checkbox.checked;
				});
			},

			
			openRenameOptionPopup : function(event) {
				var self = this;
				var labelCell = event.currentTarget;
				var previousValue = $(labelCell).text();

				self.renameCufOptionPopup.find(
						"#rename-cuf-option-previous").text(
						previousValue);
				self.renameCufOptionPopup.find(
						"#rename-cuf-option-label").val(
						previousValue);
				self.renameCufOptionPopup.formDialog("open");
			},

			openChangeOptionCodePopup : function(event) {
				var self = this;
				var codeCell = event.currentTarget;
				var previousValue = $(codeCell).text();
				var label = $(codeCell).parent("tr").find(
						"td.opt-label").text();
				self.changeOptionCodePopup.find(
						"#change-cuf-option-code-label")
						.text(label);
				self.changeOptionCodePopup.find(
						"#change-cuf-option-code").val(
						previousValue);
				self.changeOptionCodePopup.formDialog("open");
			},

			renameOption : function() {
				var self = this;
				var previousValue = self.renameCufOptionPopup.find(
						"#rename-cuf-option-previous").text();
				var newValue = self.renameCufOptionPopup.find(
						"#rename-cuf-option-label").val();
				
				if (newValue.trim() === ""){
					// Error
					var errormsg = translator.get("squashtm.domain.exception.option.pattern");
					document.getElementById("nospace").innerHTML = errormsg;
				}
				else {
				$.ajax({
					type : 'POST',
					data : {
						'value' : newValue
					},
					dataType : "json",
					url : cfMod.optionsTable.ajaxSource	+ "/" + previousValue + "/label"
				}).done(function(data) {
					self.optionsTable.refresh();
					self.renameCufOptionPopup.formDialog('close');
				});
				}

			},

			changeOptionCode : function() {
				var self = this;
				var label = self.changeOptionCodePopup.find(
						"#change-cuf-option-code-label").text();
				var newValue = self.changeOptionCodePopup.find(
						"#change-cuf-option-code").val();
				$.ajax({
					type : 'POST',
					data : {
						'value' : newValue
					},
					dataType : "json",
					url : cfMod.optionsTable.ajaxSource	+ "/" + label + "/code"
				}).done(function(data) {
					self.optionsTable.refresh();
					self.changeOptionCodePopup.formDialog('close');
				});

			},

			configureButtons : function() {
				$.squash.decorateButtons();
			},



			configureEditables : function() {
				var self = this;
				this.makeSimpleJEditable("cuf-label");
				this.makeSimpleJEditable("cuf-code");

				if (this.inputType === "PLAIN_TEXT" || this.inputType === "NUMERIC") {
					this.makeDefaultSimpleJEditable();
					$("#cuf-default-value").click(
							self.disableOptionalChange);
				} 
				else if (this.inputType === "CHECKBOX") {
					this.makeDefaultSelectJEditable();
					$("#cuf-default-value").click(
							self.disableOptionalChange);
				} else if (this.inputType === "DATE_PICKER") {
					this.makeDefaultDatePickerEditable();
				}
				else if (this.inputType === "TAG"){
					this.makeDefaultTagsEditable();
				}
				else if (this.inputType === "RICH_TEXT"){
					this.makeDefaultRichTextEditable();
				}

			},
			makeDefaultSimpleJEditable : function() {
				var self = this;
				new SimpleJEditable({
					targetUrl : cfMod.customFieldUrl + "/defaultValue",
					componentId : "cuf-default-value",
					jeditableSettings : {
						name : "value",
						callback : self.enableOptionalChange,
						onsubmit : function(){
							return self._validate(this);
						}
					}
				});
			},
			makeDefaultSelectJEditable : function() {
				var self = this;
				new SelectJEditable({
					language : {
						richEditPlaceHolder : cfMod.richEditPlaceHolder,
						okLabel : cfMod.okLabel,
						cancelLabel : cfMod.cancelLabel
					},
					target : cfMod.customFieldUrl,
					componentId : "cuf-default-value",
					jeditableSettings : {
						callback : self.enableOptionalChange,
						data : JSON.stringify(cfMod.checkboxJsonDefaultValues),
						name : "value",
						onsubmit : function(){
							return self._validate(this);
						}
					}
				});
			},
			makeDefaultDatePickerEditable : function(inputId) {
				var self = this;
				var datepick = this.$("#cuf-default-value");

				// configure editable datepicker settings :
				var dateSettings = confman.getStdDatepicker();

				// we need a custom post function
				var postfn = function(value){
					var revert = this.revert;
					var localizedDate = value;
					var postDateFormat = $.datepicker.ATOM;
					var date = $.datepicker.parseDate(cfMod.dateFormat, localizedDate);
					var postDate = $.datepicker.formatDate(postDateFormat, date);
				
					return $.ajax({
						url : cfMod.customFieldUrl + "/defaultValue",
						type : 'POST',
						data : { value : postDate }
					})
					.done(function(){
						$("#cuf-default-value").text(value);
					})
					.fail(function(){
						$("#cuf-default-value").text(revert);
					});
					
				};

				
				// make editable
				datepick.editable(postfn, {
					type : 'datepicker',
					tooltip : cfMod.richEditPlaceHolder,
					datepicker : dateSettings,
					name : "value",
					onsubmit : function(){
						return self._validate(this);
					}
				});

				
			},
			
			makeDefaultTagsEditable : function(){
				var ul = this.$("#cuf-default-value"),
					self = this;
				
				var conf = confman.getStdTagit();
				ul.squashTagit(conf);
				
				ul.on('squashtagitaftertagadded squashtagitaftertagremoved', function(event, ui){
					// Contrary to Custom Field Values, the default value of a Custom Field
					// is stored as a semicolumn separated string instead of a collection of labels.
					
					if (! ul.squashTagit("validate", event, ui)){
						return;
					}
					
					var values = ul.squashTagit('assignedTags').join('|');
					$.ajax({
						url : cfMod.customFieldUrl + '/defaultValue',
						type : 'post',
						data : {
							'id' : 'cuf-default-value',
							'value' : values 
						}
					});
				});
				
			},
			
			makeDefaultRichTextEditable : function(){
				
				var area = this.$("#cuf-default-value"),
					self = this;
				
				var conf = confman.getJeditableCkeditor();
				conf.onsubmit = function(settings, ed){
					var cked = CKEDITOR.instances[$("textarea", ed).attr('id')];
					return self._validate(cked.getData());
				};
				conf.name = "value";
				
				area.editable(cfMod.customFieldUrl + "/defaultValue",conf);
				
			},
			
			// accepts a string or the editor itself
			_validate : function(arg){
				var value = (typeof arg === "string") ? arg : $(arg).val().value; 
				var validated = true;
				
				if (this.isFieldMandatory()	&& StringUtil.isBlank(value)) {
					notification.showError(cfMod.defaultValueMandatoryMessage);
					validated = false;
				}	
				return validated;
			},
			
			disableOptionalChange : function() {
				$("#cf-optional").attr("disabled", true);
			},

			enableOptionalChange : function() {
				$("#cf-optional").removeAttr("disabled");
			},

			cancelOptionalChange : function(){
				var opt = $("#cf-optional"),
					active = opt.prop("checked");

				opt.prop("checked", ! active);
			},

			isFieldMandatory : function() {
				if (!! this.optionalCheckbox){
					return !this.optionalCheckbox.checked;
				}
				else{
					return false;
				}
			},

			renameCuf : function() {
				var newNameVal = $("#rename-cuf-input").val();
				$.ajax({
					type : 'POST',
					data : {
						'value' : newNameVal
					},
					dataType : "json",
					url : cfMod.customFieldUrl + "/name"

				}).done(function(data) {
					$('#cuf-name-header').html(data.newName);
					$('#rename-cuf-popup').formDialog('close');
				});
			},

			configureRenamePopup : function() {
				
				var dialog = $("#rename-cuf-popup");
				
				dialog.formDialog();
				
				dialog.on('formdialogconfirm', this.renameCuf);
				
				dialog.on('formdialogcancel', this.closePopup);
				
				dialog.on('formdialogopen', function(event, ui) {
					var name = $.trim($('#cuf-name-header').text());
					$("#rename-cuf-input").val($.trim(name));
				});	
		
				$("#rename-cuf-button").on('click', function(){
					dialog.formDialog('open');
				});
				
	

			},

			closePopup : function() {
				$(this).formDialog('close');
			},

			makeSimpleJEditable : function(inputId) {
				var self = this;

				var onerror = function(settings, original, xhr) {
					xhr.errorIsHandled = true;
					var errormsg = notification.getErrorMessage(xhr);
					Forms.input(self.$("#" + inputId)).setState("error", errormsg);					
					return ($.editable.types[settings.type].reset || $.editable.types.defaults.reset).apply(this, arguments);
				};

				new SimpleJEditable({
					targetUrl : cfMod.customFieldUrl,
					componentId : inputId,
					jeditableSettings : {
						onerror: onerror,
						onsubmit: function() { Forms.input(self.$("#" + inputId)).clearState(); }
					}
				});
			},

			configureOptionTable : function() {
				var self = this;
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}
				var config = $
						.extend(
								{
									"oLanguage" : {
										"sUrl" : cfMod.optionsTable.languageUrl
									},
									"bJQueryUI" : true,
									"bAutoWidth" : false,
									"bFilter" : false,
									"bPaginate" : true,
									"sPaginationType" : "squash",
									"iDisplayLength" : cfMod.optionsTable.displayLength,
									"bServerSide" : true,
									"sAjaxSource" : cfMod.optionsTable.ajaxSource,
									"bDeferRender" : true,
									"bRetrieve" : true,
									"sDom" : 't<"dataTables_footer"lp>',
									"iDeferLoading" : 0,
									"fnRowCallback" : function() {
									},
									"aoColumnDefs" : [
											{
												'bSortable' : false,
												'sWidth' : '2em',
												'sClass' : 'centered ui-state-default drag-handle select-handle',
												'aTargets' : [ 0 ],
												'mDataProp' : 'entity-index'
											},
											{
												'bSortable' : false,
												"aTargets" : [ 1 ],
												"sClass" : "opt-label linkWise",
												"mDataProp" : "opt-label"
											},
											{
												'bSortable' : false,
												"aTargets" : [ 2 ],
												"sClass" : "opt-code linkWise",
												"mDataProp" : "opt-code"
											},
											{
												'bSortable' : false,
												'aTargets' : [ 3 ],
												'sClass' : "is-default",
												'mDataProp' : 'opt-default'
											},
											{
												'bSortable' : false,
												'sWidth' : '2em',
												'sClass' : 'delete-button',
												'aTargets' : [ 4 ],
												'mDataProp' : 'empty-delete-holder'
											} ]
								}, squashtm.datatable.defaults);

				var squashSettings = {
					enableHover : true,
					enableDnD : true,
					confirmPopup : {
						oklabel : cfMod.confirmLabel,
						cancellabel : cfMod.cancelLabel
					},

					deleteButtons : {
						url : cfMod.optionsTable.ajaxSource	+ "/{opt-label}",
						popupmessage : "<div class='display-table-row'><div class='display-table-cell warning-cell'><div class='generic-error-signal'></div></div><div class='display-table-cell'>"+cfMod.optionsTable.deleteConfirmMessage+"</span></div></div>",
						tooltip : cfMod.optionsTable.deleteTooltip,
						success : function(data) {
							self.optionsTable.refresh();
						},
						dataType : "json"
					},

					functions : {
						dropHandler : function(dropData) {
							var url = cfMod.optionsTable.ajaxSource + '/positions';
							$.post(url,	dropData, function() {
								self.optionsTable.refresh();
							});
						},
						getODataId : function(arg) {
							return this.fnGetData(arg)['opt-label'];
						}
					}

				};

				this.optionsTable = this.$("table");
				this.optionsTable.squashTable(config,
						squashSettings);
			},

			openAddOptionPopup : function() {
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}
				var self = this;

				function discard() {
					self.newOptionDialog
							.off("newOption.cancel newOption.confirm");
					self.newOptionDialog.undelegateEvents();
					self.newOptionDialog = null;
				}

				function discardAndRefresh() {
					discard();
					self.optionsTable.refresh();
				}

				self.newOptionDialog = new NewCustomFieldOptionDialog(
						{
							model : {
								label : "",
								code : ""
							}
						});

				self.newOptionDialog
						.on("newOption.cancel", discard);
				self.newOptionDialog.on("newOption.confirm",
						discardAndRefresh);
			},

			configureRenameOptionPopup : function() {
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}
				var self = this;
				
				
				var dialog = $("#rename-cuf-option-popup");
				this.renameCufOptionPopup = dialog;
				
				dialog.formDialog();
				
				dialog.on('formdialogconfirm', function(){
					self.renameOption.call(self);
				});
				
				dialog.on('formdialogcancel', this.closePopup);
				
				$("#rename-cuf-option-popup").on('click', function(){
					dialog.formDialog('open');
				});
			},

			configureChangeOptionCodePopup : function() {
				if (this.inputType !== "DROPDOWN_LIST") {
					return;
				}

				var self = this;
				
				
				var dialog = $("#change-cuf-option-code-popup");
				this.changeOptionCodePopup = dialog;
				
				dialog.formDialog();
				dialog.on('formdialogconfirm', function(){
					self.changeOptionCode.call(self);
				});
				
				dialog.on('formdialogcancel', this.closePopup);
				
				$("#change-cuf-option-code-popup").on('click', function(){
					dialog.formDialog('open');
				});
				
			}

	});
	return CustomFieldModificationView;
});