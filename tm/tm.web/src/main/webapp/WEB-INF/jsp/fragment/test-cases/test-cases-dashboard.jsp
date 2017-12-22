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
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<s:url var="statsUrl" value="/test-case-browser/statistics"/>


<div class="ui-widget-header ui-corner-all ui-state-default fragment-header" >
	<div id="right-frame-button">
		<f:message var="toggleLibraryTooltip" key="tooltip.toggleLibraryDisplay" />
		<input type="button" class="sq-btn btn-sm" id="toggle-expand-left-frame-button" title="${toggleLibraryTooltip}"/>
  	</div>
	<h2><span><f:message key="title.Dashboard" /></span></h2>
</div>


<div class="fragment-body">

	<dashboard:test-cases-dashboard-panel url="${statsUrl}"/>

</div>


<script type="text/javascript">
require(["common"], function() {
		require(["jquery","test-case-folder-management"], function($,TCF){
	$(function(){
			TCF.initDashboardPanel({
				master : '#dashboard-master',
				model : ${json:serialize(statistics)},
				listenTree : true
			});
		});
	});
});
</script>
