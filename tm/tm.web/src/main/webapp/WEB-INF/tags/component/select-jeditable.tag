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
<%-- call rich-editable-init first --%>
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ attribute name="targetUrl" required="false" description="URL where to post the informations. Either this, or 'targetFunction' must be defined." %>
<%@ attribute name="targetFunction" required="false" description="a function that will post the informations. Either this, or 'targetUrl' must be defined." %>

<%@ attribute name="componentId" required="true" %>
<%@ attribute name="submitCallback" required="false" %>
<%@ attribute name="jsonData" required="false" description="string representing the json formated data displayed in the select. Either jsonData or 
															jsonUrl must be defined." %>
<%@ attribute name="jsonUrl" required="false" description="url where to fetch json formated data displayed in the select. Either jsonData or 
															jsonUrl must be defined." %>
															
<%@ attribute name="onSubmit" required="false" description="javascript function that will be executed before the data are submitted. 
															Accepts : settings, original. Returns : true, false. this : the widget." %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="target" value="'${targetUrl}'"/>
<c:if test="${not empty targetFunction }">
<c:set var="target" value="${targetFunction}"/>
</c:if>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<script type="text/javascript">
//TODO remove this tag. Init in js file instead.
require(["common"], function() {
  require(["jquery", "jeditable"], function($) {
    $( '#${ componentId }' ).editable( ${target}, {
      type: 'select',  
      placeholder: '<f:message key="rich-edit.placeholder" />',
      submit: '<f:message key="rich-edit.button.ok.label" />',
      cancel: '<f:message key="label.Cancel" />',  
      onblur : function() {}, <%-- prevents the widget to return to unediting state on blur event --%>           
      <c:if test="${ not empty submitCallback }" >callback : function(value, settings){${submitCallback}(value, settings);},</c:if>
      <c:if test="${not empty jsonData}">data : JSON.stringify(${jsonData}),</c:if>
      <c:if test="${not empty jsonUrl}">loadurl : '${jsonUrl}',</c:if>
      <c:if test="${not empty onSubmit}">onsubmit : ${onSubmit},</c:if>
      indicator : '<span class="processing-indicator" />'
    }).addClass("editable");
  });
});
</script>