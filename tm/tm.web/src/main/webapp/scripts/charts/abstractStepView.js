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
define([ "jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "squash.translator","../custom-report-workspace/utils", "squash.dateutils" ], function($,
		backbone, _, Handlebars, translator,chartUtils,dateutils) {
	"use strict";

	var abstractStepView = Backbone.View.extend({
		el : "#current-step",

		_initialize : function(data, wizrouter) {
			this.router = wizrouter;
			this.registerHelper();
			this.steps = this.model.get("steps");
			var currStep = _.findWhere(this.steps, {name : data.name});
			this.next = currStep.nextStep;
			this.previous = currStep.prevStep;
			this.showViewTitle(currStep.viewTitle, currStep.stepNumber);
			this.initButtons(currStep.buttons);
			var missingStepNames = this.findMissingSteps(data, currStep.neededStep);
			this.missingStepNames = missingStepNames;

			if (_.isEmpty(missingStepNames)){
				this.render(data, $(this.tmpl));
			} else {

				var missingSteps = _.chain(this.steps)
				.filter(function(step){
					return _.contains(missingStepNames, step.name);
				})
				.sortBy("stepNumber")
				.value();

				var model = {steps : missingSteps, totalStep : this.steps.length};
				this.render(model, $("#missing-step-tpl"));
			}


		},

		registerHelper : function(){

			var genericCufLabel = translator.get("label.customField");
			Handlebars.registerHelper("cuf-label", function(prototype){
				var cufLabel = prototype.cufLabel;
				var html = cufLabel  + "<span class='small txt-discreet'> ("+ genericCufLabel +")</span>";

				return new Handlebars.SafeString(html);
			});

		},

		findMissingSteps : function (data, neededStep) {
			var self = this;
			return  _.filter(neededStep, function (step) {
				var param = _.chain(self.model.get("validation"))
				.find(function (val) {return val.name == step;})
				.result("validationParam")
				.value();

				return _.isEmpty(_.result(data.attributes, param));
			});


		},
		initButtons : function (buttons){

			var allButtons = ["previous", "next", "save", "generate"];

			_.each(buttons, function(button) {
				var select = $("#" + button);
				select.show();
			});

			_.chain(allButtons).difference(buttons).each(function(button) {
				var select = $("#" + button);
				select.hide();
			}
			);

		},



		navigateNext : function() {
			this.updateModel();
			this.router.navigate(this.next, {
				trigger : true
			});

		},

		updateModel : function() {
			// do in superclass
		},

		showViewTitle : function(title, stepNumber) {


			var text = "[" + translator.get("wizard.steps.label") +" " + stepNumber + "/" + this.steps.length + "] " + translator.get(title);
			$("#step-title").text(text);
		},

		navigatePrevious : function() {
			this.router.navigate(this.previous, {
				trigger : true
			});
		},

		render : function(data, tmpl) {
			var src = tmpl.html();
			this.template = Handlebars.compile(src);

			this.$el.append(this.template(data));

			return this;
		},

		destroy_view : function() {

			this.undelegateEvents();
			this.$el.removeData().unbind();
			this.remove();
			Backbone.View.prototype.remove.call(this);
		},

		/**
		 * Check if we are modidying an existing chartDef
		 * @return {Boolean} [description]
		 */
		isModify : function () {
		return this.model.get("chartDef") !== null;
		},

		previousStepAreValid : function() {
			return _.isEmpty(this.missingStepNames)
		},

		//as in database we have not a real column prototype for each cu, we need to create synthetic prototype client side.
		//the prototypes for cuf in database are generic for one data type and one entity type. The cuf id isn't stored in prototype but directly in axis, filter or measure.
		computeColumnsPrototypes : function () {
			var initialColumnsPrototypes = this.model.get('columnPrototypes');

			//1 creating synthetics prototypes and merging with natural
			var mergedProto = this.mergeProtoypes(initialColumnsPrototypes);
			
			//2 reorder to follow the squashtm workspace order
			var orderedProtos = _.pick(mergedProto,["REQUIREMENT","REQUIREMENT_VERSION","TEST_CASE","CAMPAIGN","ITERATION","ITEM_TEST_PLAN","EXECUTION"]);

			return orderedProtos;
		},
		
		getSelectedProject : function () {
			var projectsScope = this.model.get('projectsScope');
			return _.filter(squashtm.workspace.projects,function (project) {
				return _.contains(projectsScope,project.id);
			});
		},

		//This function will return a map with synthetic column proto for cuf merged into the original map of prototypes
		mergeProtoypes : function (selectedEntitiesColumnsPrototypes) {
			var cufPrototypes = [];
			var mapOfNaturalPrototypes = {};

			//first we separate all generic cuf column prototypes from initial list of column prototype from the other one (attributes and calculated)
			_.each(selectedEntitiesColumnsPrototypes, function (prototypes, key) {
				//grouping by column type
				var groupedcolumnsPrototype = _.groupBy(prototypes,function(prototype){
					return prototype.columnType;
				});
				//extracting cuf prototype for this entity type and put in array of all cuf column proto
				var cufPrototypesForOneEntityType = groupedcolumnsPrototype["CUF"];
				if(cufPrototypesForOneEntityType){
					cufPrototypes = cufPrototypes.concat(cufPrototypesForOneEntityType);
				}
				//now inject into computedColumnsPrototypes all the natural column prototypes
				var naturalPrototypes = groupedcolumnsPrototype["ATTRIBUTE"];
				naturalPrototypes = naturalPrototypes.concat(groupedcolumnsPrototype["CALCULATED"]);
				mapOfNaturalPrototypes[key] = naturalPrototypes;
			});

			//now we create the map of synthetic column proto
			//first we create a map of all cuf binding for projects in perimeter
			var cufBindingMap = this.getCufProjectMap();
			//now we generate the synthetics columns prototypes
			var syntheticColumnPrototypes = this.getCufProtoForBindings(cufBindingMap,cufPrototypes);

			//finally we merge the the two maps and return the result
			var mergedPrototypes = chartUtils.getEmptyCufMap();
			_.each(mapOfNaturalPrototypes,function (values,key) {
				var syntheticColumnPrototypesForEntity = syntheticColumnPrototypes[key];
				if(syntheticColumnPrototypesForEntity && syntheticColumnPrototypesForEntity.length > 0){
					var allProto = values.concat(syntheticColumnPrototypesForEntity);
					mergedPrototypes[key] = allProto;
				}
				else{
					mergedPrototypes[key] = values;
				}
			});

			return mergedPrototypes;
		},

		//return a map with cuf bindings by entitity type : {"CAMPAIGN":[{cufBinding1},{cufBinding2}],"ITERATION":[{cufBinding1},{cufBinding2}]...}
		getCufProjectMap : function () {
			var selectedProjects = this.getSelectedProject();
			var scopeType = this.model.get('scopeType');
			var self = this;
			var cufMap = _.reduce(selectedProjects,function (memo, project) {
				_.each(project.customFieldBindings,function (values,key) {
					values = _.filter(values, function (binding) {
									return binding.customField.inputType.enumName !== "RICH_TEXT";
					});
					if(values.length > 0 && memo.hasOwnProperty(key)){
							memo[key] = memo[key].concat(values);
					}
				});
				return memo;
			},chartUtils.getEmptyCufMap());

			// if the perimeter type is default or selected project, we want only cuf in the project scope
			// but if the perimeter type is is custom, we want only the project scope for the specified entity and all cuf of the database for the others entities 
			// as we can't infer the joins that can be made between projects entities (eg a requirement can be linked to any TC so the cufs for TC must cover everything)
			// An alternative could be to make an ajax request to find all linked entities and adjust cuf but it will be far too complex for a small gain, and will introduce issues for custom reports in workspaces
			if (scopeType === "CUSTOM") {
				this.appendAdditionnalCufBinding(cufMap);
			}
			//Now we filter out duplicates induced by selected several project with the same cuf binded to same entity type
			//we only want one instance of each cuf-entityType pair
			cufMap = _.mapObject(cufMap,function(bindings,entityType) {
				return _.uniq(bindings,function(binding) {
					return binding.customField.id;
				});
			});

			return cufMap;
		},

		appendAdditionnalCufBinding : function(cufBindingMap) {
			var entityPerimeter = this.model.get("scopeEntity");
			var emptyCufBindingsMap = this.getEmptycufBindingMapFilterd(entityPerimeter);
			var allcufBindingMap = chartUtils.extractCufsBindingMapFromWorkspace();
			_.each(emptyCufBindingsMap,function(value,entityType) {
				var additionnalCuf =  allcufBindingMap[entityType];
				if(cufBindingMap[entityType]){
					cufBindingMap[entityType] = cufBindingMap[entityType].concat(additionnalCuf);
				}
				else {
					cufBindingMap[entityType] = additionnalCuf;
				}
				
			});
		},

		getEmptyCufMap : function () {
			return {
				"REQUIREMENT_VERSION":[],
				"TEST_CASE":[],
				"CAMPAIGN":[],
				"ITERATION":[],
				"EXECUTION":[]
			};
		},

		getEmptycufBindingMapFilterd :function(entityPerimeter) {
			switch (entityPerimeter) {
				case 'REQUIREMENT':
					return {
						"TEST_CASE":[],
						"CAMPAIGN":[],
						"ITERATION":[],
						"EXECUTION":[]
					};
				case 'TEST_CASE':
					return {
						"REQUIREMENT_VERSION":[],
						"CAMPAIGN":[],
						"ITERATION":[],
						"EXECUTION":[]
					};
				case 'CAMPAIGN':
					return {
						"REQUIREMENT_VERSION":[],
						"TEST_CASE":[]
					};
			}
		},

		getCufProtoForBindings : function (bindingMap,cufPrototypes) {
			var protoForCufBinding = chartUtils.getEmptyCufMap();
			var self = this;
			_.each(bindingMap,function (values,key) {
				var generatedPrototypes = _.map(values,function (cufBinding) {
					//1 find the proto name
					var protoLabel = key + "_" + self.getProtoSuffix(cufBinding);
					//2 find the prototype and upgrade it with cufCode and label
					var cufPrototype = _.find(cufPrototypes,function (proto) {
						return proto.label === protoLabel;
					});
					if (cufPrototype) {
						cufPrototype = _.clone(cufPrototype);
						cufPrototype.code = cufBinding.customField.code;
						cufPrototype.cufLabel = cufBinding.customField.label;
						cufPrototype.cufName = cufBinding.customField.name;
						cufPrototype.cufId = cufBinding.customField.id;
						cufPrototype.isCuf = true;
						cufPrototype.originalPrototypeId = cufPrototype.id;
						cufPrototype.id = cufPrototype.id + "-" + cufBinding.customField.id;
						cufPrototype.cufType = cufBinding.customField.inputType.enumName;
						cufPrototype.cufTypeFriendly = cufBinding.customField.inputType.friendlyName;
						if (cufPrototype.cufType === "DROPDOWN_LIST" || cufPrototype.cufType === "TAG") {
							cufPrototype.cufListOptions = cufBinding.customField.options;
						}
						return cufPrototype;
					}
				});
				if(generatedPrototypes){
					protoForCufBinding[key] = generatedPrototypes;
				}
			});
			return protoForCufBinding;
		},

		getProtoSuffix : function (value) {
			var suffix;
			switch (value.customField.inputType.enumName) {
				case "PLAIN_TEXT":
					suffix = "CUF_TEXT";
					break;
				case "CHECKBOX":
					suffix = "CUF_CHECKBOX";
					break;				
				case "DROPDOWN_LIST":
					suffix = "CUF_LIST";
					break;				
				case "DATE_PICKER":
					suffix = "CUF_DATE";
					break;				
				case "TAG":
					suffix = "CUF_TAG";
					break;
				case "NUMERIC":
					suffix = "CUF_NUMERIC";
					break;
			}
			return suffix;
		},

		i18nFormatDate : function(date) {
			return dateutils.format(date, translator.get('squashtm.dateformatShort'));
		}

	});

	abstractStepView.extend = function(child) {
		var view = Backbone.View.extend.apply(this, arguments);
		view.prototype.events = _.extend({}, this.prototype.events, child.events);
		return view;
	};

	return abstractStepView;

});
