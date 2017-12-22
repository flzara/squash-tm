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
<f:message var="dateFormat" key="squashtm.dateformat" />
<f:message var="status" key="${milestone.status.i18nKey}" />

<div class="ui-widget-header ui-corner-all ui-state-default fragment-header" >
	<h2><span><f:message key="title.Dashboard" /></span></h2>
</div>


<div class="fragment-body">


	<%-- description panel --%>
	<comp:toggle-panel id="test-case-milestone-description-panel" titleKey="label.Information"  open="true">
		<jsp:attribute name="body">
		<div id="test-case-attribut-table" class="display-table">
			<div class="display-table-row">
				<label class="display-table-cell"><f:message key="label.Status" /></label>
				<span>${status}</span>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell"><f:message key="label.EndDate" /></label>
				<span><f:formatDate value="${milestone.endDate}" pattern="${dateFormat}" /></span>
			</div>
		</div>
		</jsp:attribute>
	</comp:toggle-panel>

	<%-- statistics panel --%>
	<c:if test="${shouldShowDashboard}">
      <dashboard:favorite-dashboard />
  </c:if>

  <c:if test="${not shouldShowDashboard}">
  	<dashboard:test-cases-dashboard-panel url="${statsUrl}"/>
  </c:if>

<script type="text/javascript">

var shouldShowDashboard = ${shouldShowDashboard};

require(["common"], function() {

	require(["jquery","squash.basicwidgets","test-case-library-management", "milestone-manager/milestone-activation","favorite-dashboard"], function($,basicwidg, TCLM, milestones, favoriteMain){
		$(function(){
		basicwidg.init();

    //init the default dashboard
    if(shouldShowDashboard){
      squashtm.workspace.canShowFavoriteDashboard = ${canShowDashboard};
      squashtm.workspace.shouldShowFavoriteDashboard = shouldShowDashboard;

      var options = {};
      options.isMilestoneDashboard = ${isMilestoneDashboard};

      favoriteMain.init(options);
    }
    else {
    	var mId = milestones.getActiveMilestone();
      TCLM.initDashboardPanel({
        master : '#dashboard-master',
        cacheKey : "dashboard-tcmilestone"+mId
      });
		}
	});
});
});

</script>
