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
define(["jquery", "squash.translator", 'squash.dateutils', "jquery.squash.datepicker"], function($, translator, dateutils) {
	
	return {
		init: function(conf) {
			var myStartDatePicker;
			var myEndDatePicker;

			var dateFormatShort = translator.get('squashtm.dateformatShort');
			var dateFormatTimePicker = translator.get("squashtm.dateformatShort.datepicker");
			
			var startControls ={
					datepick : $('#actual-start'),
					datelabel : $('#actual-start-label'),
					checkbx: $('#actual-start-checkbox')
			};
			var startParams ={
					paramName : "actualStart",
					initialDate : conf.data.initialActualStartDate,
					dateFormat : dateFormatTimePicker,
					modeParamName: "setActualStartAuto",
					isAuto: conf.data.initialActualStartAuto
			};
			var endControls ={
					datepick : $('#actual-end'),
					datelabel : $('#actual-end-label'),
					checkbx: $('#actual-end-checkbox')
					
			};
			var endParams ={
					paramName : "actualEnd",
					initialDate : conf.data.initialActualEndDate,
					dateFormat : dateFormatTimePicker,
					modeParamName: "setActualEndAuto",
					isAuto: conf.data.initialActualEndAuto
			};
			if(!!conf.data.planningUrl) {
				startParams.url = conf.data.planningUrl;
				endParams.url = conf.data.planningUrl;
			}

			var startValidator = {
					isValid : function(txtSubmittedStartDate) {
						var txtEndDate = $('#actual-end-label').text();
						var result = true;

						if(txtSubmittedStartDate !== "" && txtEndDate !== "-") {
							var endDate = dateutils.parse(txtEndDate, dateFormatShort);
							var startDate = dateutils.parse(txtSubmittedStartDate, dateFormatShort);

							result = (endDate >= startDate);
						}
						return result;
					},
					errorMessage : translator.get('message.exception.timePeriodNotConsistent')
			};
			var endValidator = {
					isValid : function(txtSubmittedEndDate) {
						var txtStartDate = $('#actual-start-label').text();
						var result = true;

						if(txtSubmittedEndDate !== "" && txtStartDate !== "-") {
							var endDate = dateutils.parse(txtSubmittedEndDate, dateFormatShort);
							var startDate = dateutils.parse(txtStartDate, dateFormatShort);

							result = (endDate >= startDate);
						}
						return result;
					},
					errorMessage : translator.get('message.exception.timePeriodNotConsistent')
			};
			startParams.validator = startValidator;
			endParams.validator = endValidator;
			
			myStartDatePicker = new DatePickerAuto(startControls, startParams);
			myEndDatePicker = new DatePickerAuto(endControls, endParams);

		}
	};
});
