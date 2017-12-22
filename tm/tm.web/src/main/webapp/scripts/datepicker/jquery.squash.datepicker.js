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
/* *********************************************

 manager for the datepicker.tag - B. Siri

 require jQuery and jQuery ui.

 Note : the calling pages must define :

 <style>
 .date-hidden {display:none};
 </style>


 This simple control is behaving just the same 
 than the jquery datepicker, except that we're
 managing a label for display and text input
 for edit instead of using the later for both.

 ************************************************/

/*******************************************************************************
 * initialization
 * 
 * require an object containing :
 * 
 * control : object having - datepick : the jQuery datepicker - datelabel : the
 * jQuery label displaying the date
 * 
 * params : object having - paramName : the string containing the label of the
 * POST parameter - url : the url to post to - initialDate : a string containing
 * the number of milliseconds since the 1st january 1970 - callback : a callback
 * for the ajax post date success handler
 * 
 * 
 ******************************************************************************/

function SquashDatePicker(controls, params) {
	// attributes
	this.controls = controls;
	this.params = params;

	// note : the formerState is unused now, it exists just in case you need to
	// extend the behavior with a cancel control
	this.formerState = {
		formerDate : null
	};

	// methods
	this.enterEditMode = dp_enterEditMode;
	this.enterDisplayMode = dp_enterDisplayMode;
	this.cancelEditMode = dp_cancelEditMode;
	this.inputAndExitEditMode = dp_inputAndExitEditMode;
	this.setDate = dp_setDate;
	this.postDate = dp_postDate;
	this.postDateSuccess = dp_postDateSuccess;
	this.postDateFailed = dp_postDateFailed;

	// pseudo constructors
	this.initialize = dp_initialize;

	this.initialize(params);

}

function dp_initialize(options) {
	// init the datepicker
	$(this.controls.datepick).datepicker();
	if(this.params.dateFormat){//[Issue 3435]
		$(this.controls.datepick).datepicker( "option", "dateFormat", this.params.dateFormat );
	}
	$(this.controls.datelabel).addClass("editable");
	var me = this;

	$(this.controls.datepick).datepicker("option", "onClose", function(submittedDateText) {
		// the following check happens to prevent a weird recursion
		if (!$(this).hasClass("date-hidden")) {
			
			// Check validity of submitted date if a validator exists
			if(!!options.validator) {
				if (options.validator.isValid(submittedDateText)) {
					me.inputAndExitEditMode();
				}
				else{
					me.cancelEditMode();
					squashtm.notification.showError(options.validator.errorMessage);
					return false;
				}
			} else {
				me.inputAndExitEditMode();
			}
		}
	});

	$(this.controls.datelabel).click(function() {
		me.enterEditMode();
	});

	// initialize the date
	var newDate;

	if (/*!!this.params.initialDate*/this.params.initialDate.length > 0) {
		newDate = parseInt(this.params.initialDate,10);
	} else {
		newDate = -1;
	}
	this.setDate(newDate);

}

/*
 * *** manage the entering in edit mode
 * 
 * controls as usual
 * 
 * note : the caller should save the former state of the control
 * 
 * *****
 */
function dp_enterEditMode() {
	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;

	this.formerState.formerDate = $(datepick).datepicker('getDate');

	$(datepick).removeClass("date-hidden");
	$(datelabel).addClass("date-hidden");
	$(datepick).datepicker('show');

}

/*
 * this mode resume the control back to a normal display mode
 * 
 */
function dp_enterDisplayMode() {
	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;

	$(datepick).addClass("date-hidden");
	$(datelabel).removeClass("date-hidden");
	$(datepick).datepicker('hide'); // comment/uncomment as necessary
}

/*
 * ** manage the exit of edit mode via cancel
 * 
 * controls as usual
 * 
 * formerState : - wasAuto : boolean, telling is the control was automode or not -
 * formerDate : Date, to restore here
 * 
 */
function dp_cancelEditMode() {

	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;

	this.setDate(this.formerState.formerDate);

	this.enterDisplayMode();
}

/*
 * ** manage the exit of edit mode via cancel, and posts the new informations
 * 
 * controls as usual
 * 
 * formerState : - wasAuto : boolean, telling is the control was automode or not -
 * formerDate : Date, to restore here
 * 
 * see definition of params in the init function
 * 
 */
function dp_inputAndExitEditMode() {
	this.postDate();
}

/** params : integer, the number of milliseconds since blablabla ** */
function dp_setDate(iDate) {
	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;

	// While calling cancelEditMode, iDate = null if formerState is null
	if (!!iDate && iDate >= 0) {

		var myDate = new Date();

		myDate.setTime(iDate);

		$(datepick).datepicker("setDate", myDate);

		var dateformat = $(datepick).datepicker("option", "dateFormat");
		var stringdate = $.datepicker.formatDate(dateformat, myDate);

		$(datelabel).html(stringdate);
	} else {
		$(datepick).datepicker("setDate", null);
		$(datelabel).html("-");
	}

}

/**
 * 
 * params here is the same, plus a callback in case of success - callback :
 * pointer to a function with no arguments;
 * 
 */

function dp_postDate() {
	var datepick = this.controls.datepick;

	var paramName = this.params.paramName;
	var url = this.params.url;
	var updateFunction = this.params.updateFunction;
	var callback = this.params.callback;

	var myDate = $(datepick).datepicker("getDate");

	var strMillisec;

	if (myDate === null) {
		strMillisec = "";
	} else {
		strMillisec = myDate.getTime().toString();
	}

	var me = this;

	// post if an url is provided, else call a javascript function that will
	// handle that.
	if (typeof url != "undefined") {

		$.ajax({
			type : 'POST',

			// data : { paramName : millisec },
			data : paramName + "=" + strMillisec,
			success : function(strDate) {
				me.postDateSuccess(strDate, callback);
			},
			error : function(xhr) {
				me.postDateFailed(xhr);
			},

			dataType : "text",
			url : url
		});
	} else if (typeof updateFunction != "undefined") {
		updateFunction(paramName, strMillisec);
		me.postDateSuccess(strMillisec, callback);
	}

}

// ajax succes handler for posting a manually set date
function dp_postDateSuccess(strDate, callback) {
	var newDate;
	if (strDate.length > 0) {
		newDate = parseInt(strDate,10);
	} else {
		newDate = -1;
	}

	this.setDate(newDate);

	this.enterDisplayMode();

	if (!!callback){
		callback();
	}
}

function dp_postDateFailed(/* add params later if needed */xhr) {
	xhr.errorIsHandled = true;
	squashtm.notification.showXhrInDialog(xhr);
	this.cancelEditMode();
}
