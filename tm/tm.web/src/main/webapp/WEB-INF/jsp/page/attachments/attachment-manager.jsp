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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="sq"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>

<%-----------------

@params :
	workspace : test-case | campaign | requirement
	attachListId : id de la attachmentList qu'on va traiter. Est utilisé par le fragment appelé ci dessous
  
 --%>
 <c:set var="workspaceColor" value="blue" />
 <c:choose>
 	<c:when test="${workspace =='test-case'}">
 		<c:set var="workspaceColor" value="green" />
 	</c:when>
	<c:when test="${workspace =='campaign'}">
 		<c:set var="workspaceColor" value="purple" />
 	</c:when>
	<c:when test="${workspace =='requirement'}">
 		<c:set var="workspaceColor" value="blue" />
 	</c:when>
    <c:when test="${workspace =='administration'}">
      <c:set var="workspaceColor" value="grey" />
    </c:when>
 </c:choose>

<layout:info-page-layout titleKey="squashtm.attachments.manager.title" highlightedWorkspace="${workspace}">
	<jsp:attribute name="head">	
		<comp:sq-css name="squash.${workspaceColor}.css" />
	</jsp:attribute>
	<jsp:attribute name="titlePane">
		<h2><f:message key="squashtm.attachments.manager.title" /></h2>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">	
		<jsp:include page="/WEB-INF/jsp/fragment/attachments/attachment-manager.jsp" />
	</jsp:attribute>

</layout:info-page-layout>