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
 * This tick formatter is meant for use with $.jqplot.DateAxisRenderer. 
 * 
 * When used, it will ensure that the dates displayed are stripped of time informations. This 
 * is needed because when the DateAxisRenderer autocomputes its tick interval (ie its scale), 
 * when the interval is too short ( <19days ) it will  include the time in the calculus. This 
 * leads to weird axis labels where you could have your dates labeled by, say, increments of 
 * 2 days, 6 hours and 21 minutes. 
 * 
 * To solve this, this renderer will make sure that dates are stripped of the time component. 
 * It just overrides the method #setTick, that wraps attempts to modify the value if this tick. 
 * If this setter is bypassed by a direct call to tick.value = ..., then we're screwed. Not perfect 
 * but it should work for most our needs.
 *
 */


define(["jquery", "jqplot-core"], function($){
	
	$.jqplot.StrippedTimeDateTickRenderer = function(options){
		$.jqplot.AxisTickRenderer.call(this, options);
	};
	
	$.jqplot.StrippedTimeDateTickRenderer.prototype = new $.jqplot.AxisTickRenderer();
	$.jqplot.StrippedTimeDateTickRenderer.prototype.constructor = $.jqplot.StrippedTimeDateTickRenderer;
	
	$.jqplot.StrippedTimeDateTickRenderer.prototype.setTick = function(value, axisName, isMinor){
		var strippedValue = new Date(value).setHours(0,0,0,0);
		$.jqplot.AxisTickRenderer.prototype.setTick.call(this, strippedValue, axisName, isMinor);
	};
});
