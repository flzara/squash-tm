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
define(['jquery', 'squash.attributeparser',	'handlebars', 'squash.configmanager',
        'squash.dateutils', "squash.translator", 'jquery.squash.formdialog', 'jeditable.datepicker'],
		        function($, attrparser, handlebars, confman, dateutils, translator) {
	"use strict";

  // registers an iterationplanning dialog as a jq widget
	if ($.squash.iterplanningDialog === undefined || $.squash.iterplanningDialog === null){
		$.widget("squash.iterplanningDialog", $.squash.formDialog, {

			options : {
				loaded : false,
				template : handlebars.compile(
						'{{#each this}}'+
							'<tr data-iterid="{{this.id}}" class="centered picker-item">'+
								'<td>{{this.name}}</td>'+
								'<td><span class="picker-start cursor-pointer">{{this.scheduledStartDate}}</span></td>'+
								'<td><span class="picker-end cursor-pointer">{{this.scheduledEndDate}}</span></td>'+
							'</tr>' +
						'{{/each}}')
			},

			// *************** CREATION ************************

			_create : function(){
				this._super();
				var self=this;

				this._configure();

			},

			_configure : function(){
				var strconf = this.element.data('def');
				var conf = attrparser.parse(strconf);
				$.extend(this.options, conf);

				var self = this;

				this.onOwnBtn('cancel', function(){
					self.close();
				});

				this.onOwnBtn('confirm', function(){
					self.setState('loading');
					self.commitThenClose();
				});
			},



			// **************** LOAD / OPEN **********************************

			open : function(){
				this._super();
				var self = this;

				if (this.options.loaded){
					self.setState('loading');
					self.reset();
					self.render();
					self.setState('edit');
				}
				else{
					self.setState('loading');
					this._loadThenOpen();
				}
			},


			_loadThenOpen : function(){
				var self =this;
				var url = window.squashtm.app.contextRoot+'/campaigns/'+this.options.campaignId+'/iterations';
				$.getJSON(url, function(json){
					self._createModel(json);
					self.reset();
					self.render();
					self.setState('edit');
				});
			},

			// ******************** backbone view ****************************

			// this is not a Backbone.View render
			render : function(){
				this._createDOM();
				this._createWidgets();
			},

			_createDOM : function(){

				var model = this.options.model;

				// transform the model
				var _formated = [],
					dateformat = this.options.dateformat;

				$.each(model, function(){
					// convert the dates from ATOM to the datepicker format
					var dSchedStart = (this.scheduledStartDate!==null) ? dateutils.format(this.scheduledStartDate, dateformat) : '--';
					var dSchedEnd  = (this.scheduledEndDate!==null) ? dateutils.format(this.scheduledEndDate, dateformat) : '--';
					_formated.push({id : this.id, name : this.name,	scheduledStartDate : dSchedStart, scheduledEndDate : dSchedEnd });
				});

				// create the dom
				var body = this.uiDialog.find('tbody');
				body.empty();
				body.append(this.options.template(_formated));
			},

			_createWidgets : function(){

				var dialogview = this,
					format= this.options.dateformat,
					body = this.uiDialog.find('tbody');

				var conf ={
					type : 'datepicker',
					placeholder : window.squashtm.message.placeholder,
					datepicker  : confman.getStdDatepicker()
				};

				// post to the model
				var postFunction = function(value){
					var $this = $(this),
						id = $this.parents('.picker-item:first').data('iterid'),
						date = dateutils.format(value, dateutils.ISO_8601, format),
						attribute = ($this.hasClass('picker-start')) ? 'scheduledStartDate' : 'scheduledEndDate';

					var item = $.grep(dialogview.options.model, function(elt){return elt.id === id; })[0];
					item[attribute] = date;

					return value;
				};



				body.find('.picker-start').editable(postFunction, conf);
				body.find('.picker-end').editable(postFunction, conf);

			},

			// ********************* model management **********************


			_createModel : function(data){
				this.options._savemodel = data;
				this.options.loaded = true;
			},

			commitThenClose : function(){
				var url = window.squashtm.app.contextRoot + '/campaigns/'+this.options.campaignId+'/iterations/planning',
					self = this;
				
				var dateFormat = this.options.dateformat;
				var allPeriodsConsistent = true;
				
				var scheduledTimePeriodValidator = function(key, value) {
					// Value can be null or undefined
					if(!value) {
						return null;
					}
					var scheduledEnd = value.scheduledEndDate;
					var scheduledStart = value.scheduledStartDate;
					
					// Check validity of the dates
					if(!!scheduledEnd) {
						scheduledEnd = dateutils.parse(scheduledEnd);
						if(scheduledEnd.getTime() <= 0) {
							scheduledEnd = undefined;
							value.scheduledEndDate = null;
						}
					}
					if(!!scheduledStart) {
						scheduledStart = dateutils.parse(scheduledStart);
						if(scheduledStart.getTime() <= 0) {
							scheduledStart = undefined;
							value.scheduledStartDate = null;
						}
					}
					
					if(!! scheduledStart && !! scheduledEnd) {
						// Check consistency of the time Period
						if(scheduledStart > scheduledEnd) {
							// We reset scheduledStart & scheduledEnd
							value.scheduledEndDate = self.options._savemodel[key].scheduledEndDate;
							value.scheduledStartDate = self.options._savemodel[key].scheduledStartDate;
							// Notification of the error
							squashtm.notification.showError(translator.get("message.exception.planning.notConsistentPeriods"));
							allPeriodsConsistent = false;
						}
					}
					return value;
				};
				
				$.ajax({
					url : url,
					async : false,		// we don't want the user to interact with the system while the request is being processed
					type : "POST",
					contentType : 'application/json',
					data : JSON.stringify(self.options.model, scheduledTimePeriodValidator)
				})
				.done(function(){
					self.options._savemodel = self.options.model;
					if(allPeriodsConsistent) {
						self.close();
					} else {
						self._loadThenOpen();
					}
				});
			},

			reset : function(){
				this.options.model = $.extend(true, [], this.options._savemodel);	// deep copy of the saved model
			}


		});
	}

	return {
		init : function(conf){
			$("#iteration-planning-popup").iterplanningDialog();
			$("#iteration-planning-button").click(function(){
				$("#iteration-planning-popup").iterplanningDialog('open');
			});
		}
	};
});