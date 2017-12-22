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
/* French initialisation for the jQuery UI date picker plugin. */
/* Written by Keith Wood (kbwood{at}iinet.com.au) and Stéphane Nahmani (sholby@sholby.net). */
jQuery(function($) {
	$.datepicker.regional.fr = {
		closeText : 'Fermer',
		prevText : 'Pr\u00e9c',
		nextText : 'Suiv',
		currentText : 'Courant',
		monthNames : [ 'Janvier', 'F\u00e9vrier', 'Mars', 'Avril', 'Mai',
				'Juin', 'Juillet', 'Ao\u00fbt', 'Septembre', 'Octobre',
				'Novembre', 'D\u00e9cembre' ],
		monthNamesShort : [ 'Jan', 'F\u00e9v', 'Mar', 'Avr', 'Mai', 'Jun',
				'Jul', 'Ao\u00fb', 'Sep', 'Oct', 'Nov', 'D\u00e9c' ],
		dayNames : [ 'Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi',
				'Vendredi', 'Samedi' ],
		dayNamesShort : [ 'Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam' ],
		dayNamesMin : [ 'Di', 'Lu', 'Ma', 'Me', 'Je', 'Ve', 'Sa' ],
		weekHeader : 'Sm',
		dateFormat : 'dd/mm/yy',
		firstDay : 1,
		isRTL : false,
		showMonthAfterYear : false,
		yearSuffix : ''
	};
	$.datepicker.setDefaults($.datepicker.regional.fr);
});