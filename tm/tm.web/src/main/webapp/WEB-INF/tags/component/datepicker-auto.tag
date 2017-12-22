<%--

        This file is part of the Squashtest platform.
        Copyright (C) Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%-- Use with datepicker-manager, same tag lib --%>

<%@ attribute name="datePickerId" required="true"
	description="this Id refers to the main div"%>
<%@ attribute name="fmtLabel" required="true"
	description="message key for the label to be displayed"%>
<%@ attribute name="url" required="true" description="url to post to"%>
<%@ attribute name="isContextual"
	description="if should be displayed in the contextual content, set to true. Do it."%>
<%@ attribute name="paramName" required="true"
	description="the name of the parameter being posted"%>
<%@ attribute name="initialDate" required="true"
	description="the date to set the datepicker to when loading the page"%>
<%@ attribute name="isAuto" required="true"
	description="tells whether the user manually set the date or note. true or false."%>
<%@ attribute name="autosetParamName" required="true"
	description="name of the flag being posted to the server, telling if the user is setting this date manually or not"%>
<%@ attribute name="postCallback" required="false"
	description="sets the callback once posted"%>
<%@ attribute name="editable" type="java.lang.Boolean" required="false"
	description="if specified, will tell whether the component is editable or not. Default is true."%>
<%@ attribute name="jsVarName" required="false"
	description="set the name of the javascript DatepickerAuto object to be able to use it outside this tag"%>

<f:message var="dateFormatDp" key="squashtm.dateformatShort.datepicker" />
<c:if test="${ (empty editable) or editable }">
	<script type="text/javascript">
	//TODO remove this js. Init in .js file instead.
require(["common"], function() {
	require(["jquery", "jquery.squash.datepicker-auto"], function() {
<c:choose>
<c:when test="${ not empty jsVarName }">
	var ${jsVarName};
</c:when>
<c:otherwise>
var myDatePicker;
</c:otherwise>
</c:choose>
	$(function(){
		var controls ={
			datepick : $('#${datePickerId}'),
			datelabel : $('#${datePickerId}-label'),
			checkbx : $('#${datePickerId}-auto')
		};
		
		var params ={
			paramName : "${paramName}",
			dateFormat : "${dateFormatDp}",
			modeParamName : "${autosetParamName}",
			url :  "${url}",
			initialDate : "${initialDate}",
			isAuto :${isAuto}
			<c:if test="${not empty postCallback}">,callback : ${postCallback}</c:if>
		};
		<c:choose>
		<c:when test="${ not empty jsVarName }">
		window.${jsVarName} = new DatePickerAuto(controls, params);
		</c:when>
		<c:otherwise>
		var myDatePicker = new DatePickerAuto(controls, params);
		</c:otherwise>
		</c:choose>
		
	});		
	});
});
</script>
</c:if>

<f:message var="label" key="${fmtLabel}" />
<f:message var="dateFormat" key="squashtm.dateformatShort" />
<f:message var="dateFormatDp" key="squashtm.dateformatShort.datepicker" />
<div>
	<div class="datepicker-caption">
		<label>${label}</label>&nbsp;
	</div>
	<div class="datepicker-date">


		<c:choose>
			<c:when test="${ (empty editable) or editable }">
				<input id="${datePickerId}" type="text" class="date-hidden" />
			</c:when>
		</c:choose>

		<c:choose>
			<c:when test="${ not empty initialDate }">
				<jsp:useBean id="rawDate" class="java.util.Date" />
				<jsp:setProperty property="time" name="rawDate"
					value="${ initialDate }" />
				<f:formatDate value="${ rawDate }" var="dateToDisplay"
					pattern="${dateFormat}" />
			</c:when>
			<c:otherwise>
				<f:message key="squashtm.nodata" var="dateToDisplay" />
			</c:otherwise>
		</c:choose>

		<span id="${datePickerId}-label"
			style="color: #4297d7; font-weight: bold">${dateToDisplay}</span>

		<c:choose>
			<c:when test="${ (empty editable) or editable }">
				<input type="checkbox" id="${datePickerId}-auto"
					style="vertical-align: top; position: relative;" /><label for="${datePickerId}-auto" class="afterDisabled">auto</label>
		</c:when>
		</c:choose>

	</div>
	<div class="unsnap"></div>

</div>
