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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url var="administrationUrl" value="/administration" />

<c:url var="editLoginMsgUrl" value="/administration/modify-login-message"/>
<c:url var="editLoginButtonsConf" value="/styles/ckeditor/welcome-message-ckeditor-config.js"/>

<layout:info-page-layout titleKey="label.ConsultModifyLoginMessage" highlightedWorkspace="requirement" isSubPaged="true">
	<jsp:attribute  name="head">
		<comp:sq-css name="squash.grey.css" />
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>	
	</jsp:attribute>
		
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="label.ConsultModifyLoginMessage" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href= '${administrationUrl}'"/>	
	</jsp:attribute>
	
	<jsp:attribute name="footer">	
		
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">
		<div id="login-page-content" class="admin-message-page-content">   
                        
			<span id="login-message" class="editable rich-editable" 
            data-def="url=${editLoginMsgUrl}, rows=auto, cols=auto, ckeditor.customConfig=${editLoginButtonsConf}" >${loginMessage}</span>	
		</div>
	</jsp:attribute>
	
</layout:info-page-layout>