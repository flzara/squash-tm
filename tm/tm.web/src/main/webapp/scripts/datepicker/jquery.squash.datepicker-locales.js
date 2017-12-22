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
/*
 * Got only french for now. Locale en remains the default locale until someone manually sets the default locale to 
 * one of the locales defined below.
 * 
 * created because of issue #1535
 */

define(["jquery","jqueryui"], function($){
	/* French initialisation for the jQuery UI date picker plugin. */
	/* Written by Keith Wood (kbwood{at}iinet.com.au) and Stephane Nahmani (sholby@sholby.net). */	
		
	$.datepicker.regional.fr = {
		closeText: 'Fermer',
		prevText: 'Pr\u00e9c',
		nextText: 'Suiv',
		currentText: 'Courant',
		timeText: 'Temps',
		hourText: 'Heures',
		minuteText: 'Minutes',
		secondText: 'Secondes',
		monthNames: ['Janvier','F\u00e9vrier','Mars','Avril','Mai','Juin',
		'Juillet','Ao\u00fbt','Septembre','Octobre','Novembre','D\u00e9cembre'],
		monthNamesShort: ['Jan','F\u00e9v','Mar','Avr','Mai','Jun',
		'Jul','Ao\u00fb','Sep','Oct','Nov','D\u00e9c'],
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Jeu','Ven','Sam'],
		dayNamesMin: ['Di','Lu','Ma','Me','Je','Ve','Sa'],
		weekHeader: 'Sm',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''
	};
	
	$.datepicker.regional.de = {
		closeText: 'schlie\u00dfen',
		prevText: 'Zur\u00fcck',
		nextText: 'Vor',
		currentText: 'Heute',
		timeText: 'Zeit',
		hourText: 'Stunden',
		minuteText: 'Minuten',
		secondText: 'Sekunden',		
		monthNames: ['Januar','Februar','M\u00e4rz','April','Mai','Juni',
	    'Juli','August','September','Oktober','November','Dezember'],
		monthNamesShort: ['Jan','Feb','M\u00e4r','Apr','Mai','Jun',
	    'Jul','Aug','Sep','Okt','Nov','Dez'],
		dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
		dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		weekHeader: 'W',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''
	};

	return $.datepicker.regional;	
});