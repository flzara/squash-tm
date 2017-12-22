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
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<layout:common-import-outer-frame-layout titleKey="workspace.home.title" highlightedWorkspace="requirement">
	<jsp:attribute name="head" >	
		<%-- css override is needed in case of a sub page. --%>
		<comp:sq-css name="squash.blue.css" />	
		<comp:sq-css name="structure.subpageoverride.css" />
			
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">	
		<h2><f:message key="access-denied.title.label" /></h2>
	</jsp:attribute>		
	
	<jsp:attribute name="content">
		<div id="sub-page" class="sub-page " >
					
			<div id="sub-page-content" class="access-denied-page-content ui-corner-all">
				<f:message var="backButtonLabel" key="label.Back" />
				<input type="button" class="button snap-right" value="${backButtonLabel}" onClick="history.back();"/>
				<br/>
				<br/>
				<br/>
				<br/>
				<h2 style="text-align: center; color: white;"><f:message key="access-denied.label" /></h2>
			</div>	
			
		</div>
	
	</jsp:attribute>

</layout:common-import-outer-frame-layout>
