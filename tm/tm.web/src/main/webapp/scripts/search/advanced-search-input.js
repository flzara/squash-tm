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
define([ "jquery", "backbone", "app/squash.handlebars.helpers", "squash.translator", "app/ws/squashtm.notification", "underscore", "workspace.projects",
         "squash.configmanager", "./SearchDateWidget", "./SearchRangeWidget","./SearchNumericRangeWidget",
		"./SearchExistsWidget","./SearchMultiAutocompleteWidget", "./SearchMultiSelectWidget", "./SearchMultiSelectProjectWidget",  "./SearchCheckboxWidget",
		"./SearchComboMultiselectWidget", "./SearchRadioWidget", "./SearchTagsWidget", "./SearchMultiCascadeFlatWidget", "./SearchDateCustomFieldWidget",
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, translator, notification, _, projects) {

	function fieldValue(fieldType, value) {
		if (!value) {
			var text = $(this.element.children()[0]).val();
			var id = $(this.element).attr("id");
			return {
				"type" : fieldType,
				"value" : text,
				"ignoreBridge" : this.options.ignoreBridge
			};
		} else {
			$(this.element.children()[0]).val(value.value);
		}
	}

	// text area widget
	var searchTextAreaWidget = $.widget("search.searchTextAreaWidget", {
		options : {
			ignoreBridge : false
		},

		_create : function() {
			this._super();
		},

		fieldvalue : function(value) {
			return fieldValue.call(this, "TEXT", value);
		}
	});

	// text field widget
	var searchTextFieldWidget = $.widget("search.searchTextFieldWidget", {
		options : {
			ignoreBridge : false
		},

		_create : function() {
		},

		fieldvalue : function(value) {
			return fieldValue.call(this,"SINGLE", value);
		}
	});





	var TestCaseSearchInputPanel = Backbone.View.extend({

		el : "#advanced-search-input-panel",

		initialize : function(options) {
			this.options = options;
			this.model = {fields : []};
			// init templates cache
			this.templates = {};
			this.getInputInterfaceModel();

			// templates are no longer needed
			this.templates = {};

			resizePerimeter = function(event) {
				var sizeWithPadding =  $('#perimeter-panel-id').css('width');
				var sizeWithoutPadding = parseInt(sizeWithPadding, 10) - 20;
				$("#perimeter-multiple-custom").css('width', sizeWithoutPadding);
				};

			resizePerimeter();
			$( window ).on('resize', resizePerimeter);
			window.onresize = resizePerimeter;

		},

		events : {
			"click #advanced-search-button" : "showResults"

		},

		getInputInterfaceModel : function() {
			var self = this;

			var formBuilder = function(formModel) {
				$.each(formModel.panels || {}, function(index, panel) {
					var context = {
						"toggle-panel-id": panel.id+"-panel-id",
						"toggle-panel-table-id": panel.id+"-panel-table-id",
						"toggle-panel-icon" : panel.cssClasses,
						"toggle-panel-title" : panel.title,
						"toggle-panel-state" : (panel.open) ? "expand" : "collapse"
					};
					var tableid = panel.id+"-panel-table-id";
					var source ;

					// First A

					var panelName = panel.id;
					// compiles the panel template
					if ( panelName == "perimeter" ) {
						source = self.$("#toggle-panel-perimeter-template").html();
					}
					else if (panelName == "general-information") {
						source = self.$("#toggle-panel-informations-template").html();
					}
					else if (panelName == "general-information-fullsize") {
						source = self.$("#toggle-panel-informationsfull-template").html();
					}
					else {
						source = self.$("#toggle-panel-template").html();
					}
					/* Add another source if specified */

					if (!source) { // could this really happen without being a bug ?
						return;
					}

					var template = Handlebars.compile(source);

					// parses the search model if any
					var marshalledSearchModel = self.$("#searchModel").text();
					var searchModel = {};

					if(marshalledSearchModel){
						searchModel = JSON.parse(marshalledSearchModel).fields;
					}

					var searchDomain = self.$("#searchDomain").text();

					var html = template(context);
					self.$("#advanced-search-input-form-panel-"+panel.location).append(html);
					self.$("#advanced-search-input-form-panel-"+panel.location).addClass(searchDomain);

		 // First C
					for (var i = 0, field; i < panel.fields.length; i++){
						field = panel.fields[i];
						var inputType = field.inputType.toLowerCase();
						switch(inputType)
						{
							case "textfield" :
								self.makeTextField(tableid, field.id, field.title, searchModel[field.id], field.ignoreBridge);
								break;
							case "textfieldid" :
								self.makeTextFieldId(tableid, field.id, field.title, searchModel[field.id], field.ignoreBridge);
								break;
							case "textfieldreference" :
								self.makeTextFieldReference(tableid, field.id, field.title, searchModel[field.id], field.ignoreBridge);
								break;
							case "textarea":
								self.makeTextArea(tableid, field.id, field.title, searchModel[field.id]);
								break;
							case "multiselect" :
								self.makeMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "multiselectperimeter" :
								self.makeMultiselectPerimeter(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "multiautocomplete":
								self.makeMultiAutocomplete(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "combomultiselect":
								self.makeComboMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "multicascadeflat" :
								self.makeMultiCascadeFlat(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "range" :
								self.makeRangeField(tableid, field.id, field.title, searchModel[field.id]);
								break;
							case "exists" :
								self.makeExistsField(tableid, field.id, field.title, field.possibleValues,searchModel[field.id]);
								break;
							case "existsbefore" :
								self.makeExistsBeforeField(tableid, field.id, field.title, field.possibleValues,searchModel[field.id]);
								break;
							case "date":
								self.makeDateField(tableid, field.id, field.title, searchModel[field.id]);
								break;
							case "cf_time_interval":
								self.makeCustomFieldDateSearch(tableid, field.id, field.title, searchModel[field.id]);
								break;
							case "checkbox":
								self.makeCheckboxField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case  "radiobutton":
								self.makeRadioField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id], field.ignoreBridge);
								break;
							case "tags":
								self.makeTagsField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								break;
							case "numericrange":
								self.makeNumericRangeField(tableid, field.id, field.title, searchModel[field.id]);
								break;
						}

					}
					// End C

					// End A
				});

			};

			this._processModel(formBuilder);


		},

		_processModel : function(formBuilder) {
			if (!!squashtm.app.searchFormModel) {

				formBuilder(squashtm.app.searchFormModel);

				// last detail, we must also hook the project selector with the nature an type selectors
				var self = this;
				$("#perimeter-multiple-custom").on('change', function(){
					self._updateAvailableInfolists();
				});
				// also, update immediately
				self._updateAvailableInfolists();

			} else { // TODO legacy, remove that in Squash 1.9
				$.ajax({
					url : squashtm.app.contextRoot + "/advanced-search/input?"+this.$("#searchDomain").text(),
					data : "nodata",
					dataType : "json"
				}).success(function() {
					formBuilder(squashtm.app.searchFormModel);

					// last detail, we must also hook the project selector with the nature an type selectors
					var self = this;
					$("#perimeter-multiple-custom").on('change', function(){
						self._updateAvailableInfolists();
					});
					// also, update immediately
					self._updateAvailableInfolists();
				});
			}
		},

		/**
		 * returns the html of a compiled template
		 * @param selector jq selector to find the template
		 * @param context the params given to the template
		 * @returns
		 */
		_compileTemplate : function(selector, context) {
				var template = this.templates[selector];

				if (!template) {
					var source = this.$(selector).html();
					template = Handlebars.compile(source);
					this.templates[selector] = template;
				}

				return template(context);
		},

		_appendFieldDom : function(tableId, fieldId, fieldHtml) {
			this.$("#"+tableId).append(fieldHtml);
			var escapedId = fieldId.replace(/\./g, "\\.");
			return this.$("#" + escapedId);
		},

		makeRadioField : function(tableId, fieldId, fieldTitle, options, enteredValue, ignoreBridge) {
			var context = {"text-radio-id": fieldId, "text-radio-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#radio-button-template", context));

			$fieldDom.searchRadioWidget({"ignoreBridge" : ignoreBridge});
			$fieldDom.searchRadioWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchRadioWidget("fieldvalue", enteredValue);

		},

		makeRangeField : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {"text-range-id": fieldId, "text-range-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#range-template", context));

			$fieldDom.searchRangeWidget();
			$fieldDom.searchRangeWidget("fieldvalue", enteredValue);

		},

		makeNumericRangeField : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {"text-range-id": fieldId, "text-range-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#numeric-range-template", context));

			$fieldDom.searchNumericRangeWidget();
			$fieldDom.searchNumericRangeWidget("fieldvalue", enteredValue);

		},

		makeExistsField : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"text-exists-id": fieldId, "text-exists-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#exists-template", context));
			$fieldDom.searchExistsWidget();
			$fieldDom.searchExistsWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchExistsWidget("fieldvalue", enteredValue);
		},

		makeExistsBeforeField : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"text-exists-id": fieldId, "text-exists-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#existsbefore-template", context));
			$fieldDom.searchExistsWidget();
			$fieldDom.searchExistsWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchExistsWidget("fieldvalue", enteredValue);
		},

		makeDateField : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {"text-date-id": fieldId, "text-date-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#date-template", context));
			$fieldDom.searchDateWidget();
			$fieldDom.searchDateWidget("createDom", "F"+fieldId);
			$fieldDom.searchDateWidget("fieldvalue", enteredValue);
		},

		makeCustomFieldDateSearch : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {"text-date-id": fieldId, "text-date-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#cf-date-template", context));
			$fieldDom.searchDateCustomFieldWidget();
			$fieldDom.searchDateCustomFieldWidget("createDom", "F"+fieldId);
			$fieldDom.searchDateCustomFieldWidget("fieldvalue", enteredValue);
		},

		makeCheckboxField : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"text-checkbox-id": fieldId, "text-checkbox-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#checkbox-template", context));
			$fieldDom.searchCheckboxWidget();
			$fieldDom.searchCheckboxWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchCheckboxWidget("fieldvalue", (enteredValue !== undefined)? enteredValue.value : false);

		},

		makeTextField : function(tableId, fieldId, fieldTitle, enteredValue, ignoreBridge) {
			var context = {
				"text-field-id": fieldId,
				"text-field-title": fieldTitle,
				fieldValue : !!enteredValue ? enteredValue.value : ""
			};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#textfield-template", context));
			$fieldDom.searchTextFieldWidget({"ignoreBridge" : ignoreBridge});
		},

		makeTextFieldId : function(tableId, fieldId, fieldTitle, enteredValue, ignoreBridge) {
			var context = {
				"text-field-id": fieldId,
				"text-field-title": fieldTitle,
				fieldValue : !!enteredValue ? enteredValue.value : ""
			};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#textfield-id-template", context));
			$fieldDom.searchTextFieldWidget({"ignoreBridge" : ignoreBridge});
		},

		makeTextFieldReference : function(tableId, fieldId, fieldTitle, enteredValue, ignoreBridge) {
			var context = {
				"text-field-id": fieldId,
				"text-field-title": fieldTitle,
				fieldValue : !!enteredValue ? enteredValue.value : ""
			};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#textfield-reference-template", context));
			$fieldDom.searchTextFieldWidget({"ignoreBridge" : ignoreBridge});
		},

		makeTextArea : function(tableId, fieldId, fieldTitle, enteredValue) {
			var context = {
				"text-area-id": fieldId,
				"text-area-title": fieldTitle,
				fieldValue : !!enteredValue ? enteredValue.value : ""
			};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#textarea-template", context));
			$fieldDom.searchTextAreaWidget();
		},

		makeMultiselect : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			// adds a "selected" property to options
			enteredValue = enteredValue || {};
			// no enteredValue.values means 'select everything'
			_.each(options, function(option) {
				option.selected = (!enteredValue.values) || (enteredValue.values.length === 0)|| _.contains(enteredValue.values, option.code);
			});
			var context = {"multiselect-id": fieldId, "multiselect-title": fieldTitle, options: options};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#multiselect-template", context));
			$fieldDom.searchMultiSelectWidget();
		},

		makeMultiselectPerimeter : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			// adds a "selected" property to options
			enteredValue = enteredValue || {};
			// no enteredValue.values means 'select everything', empty array mean everything
			_.each(options, function(option) {
				// Issue 4970 : _.contains failed because tried to find a string in a collection of int
				// the check on isNaN is not necessary but rather be safe than sorry
				var code = parseInt(option.code, 10);
				code = (isNaN(code)) ? option.code : code;
				option.selected = (!enteredValue.values) || (enteredValue.values.length === 0) || _.contains(enteredValue.values, code);
			});
			var context = {"multiselect-id": fieldId, "multiselect-title": fieldTitle, options: options};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#multiselect-perimeter-template", context));
			$fieldDom.searchMultiSelectProjectWidget();


		},

		makeMultiCascadeFlat : function(tableId, fieldId, fieldTitle, unsortedOptions, enteredValue){
			enteredValue = enteredValue || {};

			// issue 4597 : sort the lists alphabetically
			var options = unsortedOptions.sort(function(l1, l2){
				return l1.value.localeCompare(l2.value);
			});

			/*
			 *  Setting the initial state.
			 *  Remember that the enteredValue contains the values of the secondary select only.
			 *	So, setting the state of the primary select must be induced from the secondary selected values
			 */

			_.each(options, function(primaryOpt){
				_.each(primaryOpt.subInput.possibleValues, function(secondaryOpt){
					secondaryOpt.selected = (! enteredValue.values) || (enteredValue.values.length === 0)|| _.contains(enteredValue.values, secondaryOpt.code);
				});

				primaryOpt.selected = _.some(primaryOpt.subInput.possibleValues, function(sub){return (sub.selected === true);});
			});

			var context = {"multicascadeflat-id" : fieldId, "multicascadeflat-title" : fieldTitle, options : options};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#multicascadeflat-template", context));
			$fieldDom.searchMultiCascadeFlatWidget({ lists : options });
		},

		makeMultiAutocomplete : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"multiautocomplete-id": fieldId, "multiautocomplete-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#multiautocomplete-template", context));
			$fieldDom.searchMultiAutocompleteWidget({fieldId : fieldId, options : options});
			$fieldDom.searchMultiAutocompleteWidget("fieldvalue", enteredValue);

		},

		makeComboMultiselect : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"combomultiselect-id": fieldId, "combomultiselect-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#combomultiselect-template", context));
			$fieldDom.searchComboMultiSelectWidget();
			$fieldDom.searchComboMultiSelectWidget("createDom", "F"+fieldId, options);
			$fieldDom.searchComboMultiSelectWidget("fieldvalue", enteredValue);
		},

		makeTagsField : function(tableId, fieldId, fieldTitle, options, enteredValue) {
			var context = {"tags-id": fieldId, "tags-title": fieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, fieldId, this._compileTemplate("#tags-template", context));
			$fieldDom.searchTagsWidget({
				available : options,
				state : enteredValue
			});
		},

		extractSearchModel : function(){

			var fields = this.$el.find("div.search-input");
			var jsonVariable = {};

			// A little hack for the jstree if it exists (campaign only on july 2015)
			// We need to get the id value and stuff from the project from the tree and put them on the jsonVariable
			// This method extractModel puts them in the model and it's been sent to the controller (check showResults to see the url)

			if( !!$("#tree").attr("id") ){
				var key;
				var selectedInTree = $("#tree").jstree('get_selected');

				//because we don't have time, we can select only one type of node in the tree, so the following will work fine.
				// BE CAREFULL : This won't work anymore if multiple node type can be selected.
				var type = selectedInTree.attr('restype');

				switch (type){

				case 'campaign-libraries' :
					key =  "project.id";
					break;
				case 'campaign-folders' :
					selectedInTree = selectedInTree.find("[restype=campaigns]"); //select sub campaign for folders
					key =  "campaign.id";
					break;
				case 'campaigns' :
					key =  "campaign.id";
					break;
				case 'iterations' :
					key =  "iteration.id";
					break;
				case 'test-suites' :
					key =  "testSuites.id";
					break;
				}

				var attrName = key == 'project.id' ? 'project' : 'resid';

				var ids =_.map(selectedInTree, function(node){return $(node).attr(attrName);});
                if (ids!==undefined&&ids.length>0) {
                    jsonVariable[key] = {type : "LIST", values : ids};
                }
                else {
                    jsonVariable["project.id"] = {type : "LIST", values : []};
                }


			}


			// Looking for informations in all the widgets to check if there's something to add to the model

			for (var k = 0, $field; k < fields.length; k++) {
				$field = $(fields[k]);
				var wtype = $($field.children()[0]).attr("data-widgetname");
				var newKey = $field.attr("id");
				var escapedKey = newKey.replace(/\./g, "\\.");
				var field = $("#"+escapedKey).data("search"+wtype+"Widget");
				if(field && !!field.fieldvalue()){
					var value = field.fieldvalue();
					if( value ) {
						jsonVariable[newKey] = value;
					}
				}
			}

			this.model = {fields : jsonVariable};
		},

		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
			temp.acceptCharset="UTF-8";

			for ( var x in PARAMS) {
				var opt = document.createElement("textarea");
				opt.name = x;
				opt.value = PARAMS[x];
				temp.appendChild(opt);
			}

			document.body.appendChild(temp);
			temp.submit();
			return temp;
		},

		showResults : function() {

			this.extractSearchModel();

			if (this.emptyCriteria()){
				var message = translator.get('search.validate.empty.label');
				notification.showInfo(message);
				return;
			}

			var searchModel = JSON.stringify(this.model);
			var queryString = "searchModel=" + encodeURIComponent(searchModel);

			if(!!$("#associationType").length){
				var associateResultWithType = $("#associationType").text();
				queryString += "&associateResultWithType=" + encodeURIComponent(associateResultWithType);

				var id = $("#associationId").text();
				queryString += "&id=" + encodeURIComponent(id);

			}

			document.location.href = squashtm.app.contextRoot + "advanced-search/results?"+$("#searchDomain").text() + "&" + queryString;
		},

		emptyCriteria : function(){
			var hasCriteria = false;
			$.each(this.model.fields, function(namename,field){
				// we must distinguish singlevalued and multivalued fields
				// singlevalued fields define a property 'value', while multivalued fields define a property 'values'.
				// a singlevalued field is empty if the property 'value' is empty,
				// a multivalued field is empty if the property 'values' is null.
				//
				if ( field.value !== undefined && field.value !== "" ){
					hasCriteria = true;
				} else if (field.values !== undefined && field.values !== null){
					hasCriteria = true;
				}
			});
			return !hasCriteria;
		},

		// that method knows about fields 'projects', 'nature', 'type' and 'category'
		_updateAvailableInfolists : function(){

			var selectedProjectIds = $("#perimeter-multiple-custom").val(),
				allProjects = projects.getAll(),
				nature = $("#nature"),
				type = $("#type"),
				category = $("#category");

			nature.searchMultiCascadeFlatWidget("hideAll");
			type.searchMultiCascadeFlatWidget("hideAll");
			category.searchMultiCascadeFlatWidget("hideAll");

			var natListCodes = [],
				typListCodes = [],
				catListCodes = [];

			// TODO : check that
			if (allProjects !== null) {
			for (var i=0; i < allProjects.length; i++){
				var project = allProjects[i],
					pId = project.id;


				// collecting which info lists are used by the current selection
				// use of  ""+ dirty trick to cast that int to string
				if ( $.inArray(""+pId, selectedProjectIds) !== -1) {
					natListCodes.push( project.testCaseNatures.code );
					typListCodes.push( project.testCaseTypes.code );
					catListCodes.push (project.requirementCategories.code);
				}

			}
			}
			// now remove the duplicates
			natListCodes = _.uniq(natListCodes, true);
			typListCodes = _.uniq(typListCodes, true);
			catListCodes = _.uniq(catListCodes, true);

			// finally update the info lists
			_.each(natListCodes, function(code) {nature.searchMultiCascadeFlatWidget("showPrimary", code);});
			_.each(typListCodes, function(code) {type.searchMultiCascadeFlatWidget("showPrimary", code);});
			_.each(catListCodes, function(code) {category.searchMultiCascadeFlatWidget("showPrimary", code);});


		}


	});

	$(document).keyup(function(event) {
		if (event.keyCode == 13) {
			$("#advanced-search-button").click();
		}
	});

	return TestCaseSearchInputPanel;
});
