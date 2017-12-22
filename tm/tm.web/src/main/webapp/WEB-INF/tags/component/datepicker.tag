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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Use with datepicker-manager, same tag lib --%>

<%@ attribute name="datePickerId" required="true" description="this Id refers to the main div" %>
<%@ attribute name="fmtLabel" required="false" description="message key for the label to be displayed" %>
<%@ attribute name="url" required="false" description="url to post to"%>
<%@ attribute name="updateFunction" required="false" description="an user_defined javascript function that will handle
the data. Prototype is : function my_function(strParamName, iDate)"%>
<%@ attribute name="isContextual" description="if should be displayed in the contextual content, set to true. Do it." %>
<%@ attribute name="paramName" required="true" description="the name of the parameter being posted"%>
<%@ attribute name="initialDate" required="true" description="date in millisecondes since 1rst january 1970." %>
<%@ attribute name="postCallback" required="false" description="sets the callback once posted" %> 
<%@ attribute name="editable" type="java.lang.Boolean" required="false" description="if specified, will tell whether the component is editable or not. Default is true." %>


<c:if test="${ (not empty fmtLabel)}" >
<f:message var="label" key="${fmtLabel}" />
</c:if>
<f:message var="dateFormat" key="squashtm.dateformatShort" />
<f:message var="dateFormatDp" key="squashtm.dateformatShort.datepicker" />
<div >
	<div class="datepicker-caption">
	<c:if test="${ (not empty label)}" >	
		<label>${label}</label>&nbsp;
		</c:if>
	</div>
	<div class="datepicker-date">
	
	<c:if test="${ (empty editable) or editable }" >
			<input id="${datePickerId}" type="text" class="date-hidden"/>			
            <script type="text/javascript">
          //TODO remove this javascript. Init in js file instead.
            require(["common"], function() {
              require(["jquery", "squash.configmanager", "jquery.squash.datepicker"], function($, confman) {
                 var myDatePicker;
                
                  var controls ={
                    datepick : $('#${datePickerId}'),
                    datelabel : $('#${datePickerId}-label')
                  };
                  
                  
                  var params ={
                    paramName : "${paramName}",
                    dateFormat : "${dateFormatDp}",//[Issue 3435]
                    initialDate : "${initialDate}"
                    <c:if test="${not empty url}" >,url :  "${url}"</c:if>
                    <c:if test="${not empty updateFunction}">,updateFunction : ${updateFunction} </c:if>
                    <c:if test="${not empty postCallback}">,callback : ${postCallback}</c:if>
                  };
                  myDatePicker = new SquashDatePicker(controls, params);  
              });
            });  
            </script>
	</c:if>
	
	<c:choose>
		<c:when test="${ not empty initialDate }">
			<jsp:useBean id="rawDate" class="java.util.Date" />
			<jsp:setProperty property="time" name="rawDate" value="${ initialDate }"/>
			<f:formatDate value="${ rawDate }" var="dateToDisplay" pattern="${dateFormat}"/>
		</c:when>
		<c:otherwise>
			<f:message key="squashtm.nodata" var="dateToDisplay"/>
		</c:otherwise>
	</c:choose>		
	
	<span id="${datePickerId}-label" style="color:#4297d7;font-weight: bold ">${dateToDisplay}</span>	
	
	</div>
	<div style="clear:both;visibility:hidden;"></div>
</div> 