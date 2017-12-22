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
<%@ tag body-content="empty" 
description="a pane that renders like the generic warning or error dialogs, for use in different dialogs" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
  
<%@ attribute name="type" type="java.lang.String" description="choose either 'warning', 'info' or 'error'. Default is 'warning'." %>
<%@ attribute name="txtcontent" type="java.lang.String" description="a message to display in, defaults to empty string" %>
<%@ attribute name="htmlcontent" fragment="true" description="html content. Will replace the txt content div if specified." %>


<c:set var="etype" value="${not empty type ? type : 'warning'}"/>
<c:set var="etxt" value="${not empty txtcontent ? txtcontent : '' }"/>

	
<div class="display-table-row">
    <div class="display-table-cell warning-cell">
      <div class="generic-${etype}-signal"></div>
    </div>
    <c:if test="${empty htmlcontent}">               
    <div class="generic-${etype}-main display-table-cell" style="padding-top:20px"><c:out value="${etxt}" /></div>
    </c:if>
    <c:if test="${not empty htmlcontent }">
      <jsp:invoke fragment="htmlcontent"/>
    </c:if>
</div>