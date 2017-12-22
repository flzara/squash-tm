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
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<layout:info-page-layout titleKey="squashtm.library.test-case.title" highlightedWorkspace="campaign" isSubPaged="true" main="test-suite-page">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.purple.css" />
	</jsp:attribute>
	<jsp:attribute name="titlePane">
		<h2><f:message key="squashtm.library.campaign.title" /></h2>	
	</jsp:attribute>
	
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="subpage.test-suite.info.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="sq-btn" id="back" value="${backButtonLabel}" 
              onClick="document.location.href=squashtm.workspace.backurl;" /> 	
	</jsp:attribute>
		
	<jsp:attribute name="informationContent">	
		<jsp:include page="/WEB-INF/jsp/fragment/test-suites/test-suite.jsp">
			<jsp:param name="isInfoPage" value="true" />
		</jsp:include>
	</jsp:attribute>
	
</layout:info-page-layout>