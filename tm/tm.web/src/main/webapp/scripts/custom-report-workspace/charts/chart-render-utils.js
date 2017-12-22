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
define(['jquery','underscore'], function($,_){


	function toChartInstance(jsonChart){

		return $.extend(true, {}, jsonChart, {

			// allows for indiscriminate reference to a serie by name or index
			getSerie : function(nameOrIndex){
				// first, discriminate if we reference the serie by name or index
				// in case the argument is a number (ie an index), look for the name in the list of measures.
				var name = (isNaN(nameOrIndex)) ? nameOrIndex : this.measures[nameOrIndex].label;
				return this.series[name];

			}
		});
	}

	return {
		toChartInstance : toChartInstance
	};

});
