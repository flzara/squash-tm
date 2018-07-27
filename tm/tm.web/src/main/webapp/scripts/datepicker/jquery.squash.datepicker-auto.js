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

 manager for the datepicker-auto.tag - B. Siri

 require jQuery and jQuery ui.

 Note : the calling pages must define :

 <style>
 .date-hidden {display:none};
 </style>


 This control define two sets of two modes
 - automatic or manual
 - display or edit.

 All combinations exists except edit-auto, 
 since editing the date means the user is not 
 requesting an automatic setting.


 ************************************************/

/*******************************************************************************
 * initialization
 * 
 * require an object containing :
 * 
 * control : object having - datepick : the jQuery datepicker - datelabel : the
 * jQuery label displaying the date - checkbx : the jQuery checkbox managing the
 * state of the control
 * 
 * params : object having - paramName : the string containing the label of the
 * POST parameter - url : the url to post to - initialDate : a string containing
 * the number of milliseconds since the 1st january 1970 - isAuto : a boolean
 * telling if the control is in auto state (true), or manual state (false); -
 * callback : a callback for the ajax post date success handler
 * 
 * 
 ******************************************************************************/

function DatePickerAuto(controls, params) {
	// attributes
	this.controls = controls;
	this.params = params;
	this.formerState = {
		wasAuto : false,
		formerDate : null
	};

	// methods
	this.initState = dpa_initState;
	this.setAutoMode = dpa_setAutoMode;
	this.setManualMode = dpa_setManualMode;
	this.enterEditMode = dpa_enterEditMode;
	this.enterDisplayMode = dpa_enterDisplayMode;
	this.cancelEditMode = dpa_cancelEditMode;
	this.inputAndExitEditMode = dpa_inputAndExitEditMode;
	this.setDate = dpa_setDate;
	this.postDate = dpa_postDate;
	this.postDateSuccess = dpa_postDateSuccess;
	this.postDateFailed = dpa_postDateFailed;
	this.postState = dpa_postState;
	this.postStateSuccess = dpa_postStateSuccess;
	this.postStateFailed = dpa_postStateFailed;
	this.refreshAutoDate = dpa_refreshAutoDate;
	// pseudo constructors
	this.initialize = dpa_initialize;

	this.initialize(params);

}

function dpa_initialize(options) {
	// init the datepicker
	$(this.controls.datepick).datepicker();
	var me = this;
	if(this.params.dateFormat){//[Issue 3435]
		$(this.controls.datepick).datepicker( "option", "dateFormat", this.params.dateFormat );
	}
	$(this.controls.datepick).datepicker("option", "onClose", function(submittedDateText) {
		try {
			var currentDate = $(me.controls.datepick).datepicker("getDate");
		} catch (damnit) {
			alert(damnit.toString());
		}

		var formerdate = me.formerState.formerDate;

		// if null, we set the control to automode
		// if (currentDate==null) me.setAutoMode();

		// Check validity of submitted date if a validator exists
		if(!!options.validator) {
			if(options.validator.isValid(submittedDateText)) {
				me.inputAndExitEditMode();
			} else {
				me.cancelEditMode();
				squashtm.notification.showError(options.validator.errorMessage);
				return false;
			}
		} else {
			me.inputAndExitEditMode();
		}

	});

	// initialize the date
	var newDate;

	if (this.params.initialDate.length > 0) {
		newDate = parseInt(this.params.initialDate,10);
	} else {
		newDate = -1;
	}
	this.setDate(newDate);

	// initialize the control state
	this.initState();

	// initialize the checkbox events
	var $me = this;
	this.controls.checkbx.change(function() {
		var isAuto = $(this).is(':checked');
		if (isAuto) {
			$me.setAutoMode();
			$me.postState();
		} else {
			$me.setManualMode();
			$me.enterEditMode();
			$me.postState();
		}
	});

}

function dpa_initState() {

	var checkbx = this.controls.checkbx;
	var isAuto = this.params.isAuto;

	checkbx.prop('checked', isAuto);

	if (isAuto) {
		this.setAutoMode();
	} else {
		this.setManualMode();
	}

}

function dpa_setAutoMode() {
	var datelabel = this.controls.datelabel;
	var checkbx = this.controls.checkbx;
	$(this.controls.datelabel).removeClass("editable");
	checkbx.prop('checked', true);
	$(datelabel).unbind();

}

/*
 * same parameter than above
 * 
 * 
 */
function dpa_setManualMode() {
	var datelabel = this.controls.datelabel;
	var checkbx = this.controls.checkbx;
	$(this.controls.datelabel).addClass("editable");
	var me = this;

	checkbx.prop('checked', false);
	$(datelabel).click(function() {
		me.enterEditMode();
	});

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
function dpa_enterEditMode() {
	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;
	var checkbx = this.controls.checkbx;

	this.formerState.wasAuto = checkbx.is(':checked');
	this.formerState.formerDate = $(datepick).datepicker('getDate');

	$(datepick).removeClass("date-hidden");
	$(datelabel).addClass("date-hidden");
	$(datepick).datepicker('show');
	// $(datepick).datepicker("setDate",null);
}

/*
 * this mode resume the control back to a normal display mode
 * 
 */
function dpa_enterDisplayMode() {
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
function dpa_cancelEditMode() {

	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;

	this.setDate(this.formerState.formerDate);

	if (this.formerState.wasAuto) {
		this.setAutoMode();
		this.postState(); // tell the server we're back in autostate
	}

	// no need to reset to manual mode, since entering the edit mode implied to
	// be in manual mode already
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
function dpa_inputAndExitEditMode() {
	this.postDate();
	this.postState();

}

/** params : integer, the number of milliseconds since blablabla ** */
function dpa_setDate(iDate) {
	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;

	if (iDate >= 0) {

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
 * params here is the same, plus a callback in case of success
 *  - callback : pointer to a function with no arguments;
 * 
 */

function dpa_postDate() {
	var datepick = this.controls.datepick;

	var paramName = this.params.paramName;
	var url = this.params.url;
	var callback = this.params.callback;

	var myDate = $(datepick).datepicker("getDate");

	var strMillisec;

	if (myDate === null) {
		strMillisec = "";
	} else {
		strMillisec = myDate.getTime().toString();
	}

	var me = this;

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

}

// ajax succes handler for posting a manually set date
function dpa_postDateSuccess(strDate, callback) {
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

function dpa_postDateFailed(/* add params later if needed */xhr) {
	xhr.errorIsHandled = true;
	squashtm.notification.showXhrInDialog(xhr);
	this.cancelEditMode();
}

function dpa_postState() {

	var checkbx = this.controls.checkbx;

	var modeParamName = this.params.modeParamName;
	var url = this.params.url;
	var callback = this.params.callback;
	var isChecked = checkbx.is(':checked');

	var me = this;

	$.ajax({
		type : 'POST',
		data : modeParamName + "=" + isChecked.toString(),
		success : function(strDate) {
			me.postStateSuccess(strDate, callback);
		},
		error : function(xhr) {
			me.postStateFailed(xhr);
		},
		dataType : "text",
		url : url
	});

}

function dpa_postStateSuccess(strDate, callback) {
	var datepick = this.controls.datepick;
	var datelabel = this.controls.datelabel;
	var checkbx = this.controls.checkbx;

	var isChecked = checkbx.is(':checked');

	// if checked we must set the automatic date and leave (pretty much the only
	// possible case
	if (isChecked) {
		this.postDateSuccess(strDate, callback);
	}

}

function dpa_postStateFailed(xhr) {
	/*alert("error while posting state");*/
	xhr.errorIsHandled = true;
	squashtm.notification.showXhrInDialog(xhr);
	this.cancelEditMode();
}
function dpa_refreshAutoDate(newDateToPut) {
	var checkbx = this.controls.checkbx;
	if (checkbx.is(':checked')) {
		var newDate;

		if (newDateToPut !== null) {
			newDate = parseInt(newDateToPut,10);
		} else {
			newDate = -1;
		}
		this.setDate(newDate);
	}
}
