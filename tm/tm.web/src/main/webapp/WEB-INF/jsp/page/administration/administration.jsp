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
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<c:url var="usersUrl"                 value="/administration/users/list" />
<c:url var="projectsUrl"              value="/administration/projects" />
<c:url var="bugtrackerUrl"            value="/administration/bugtrackers" />
<c:url var="loginUrl"                 value="/administration/login-message" />
<c:url var="welcomeUrl"               value="/administration/welcome-message" />
<c:url var="customFieldsUrl"          value="/administration/custom-fields" />
<c:url var="testAutomationServerUrl"  value="/administration/test-automation-servers" />
<c:url var="scmServerUrl"             value="/administration/scm-servers" />
<c:url var="milestoneUrl"             value="/administration/milestones" />
<c:url var="reqLinkTypeUrl"           value="/administration/requirement-link-types" />
<c:url var="configUrl"                value="/administration/config" />
<c:url var="cleaningUrl"              value="/administration/cleaning" />
<c:url var="logfileUrl"               value="/administration/log-file" />

<c:set var="userLicenseInformation"   value="${userLicenseInformation}" />
<c:set var="dateLicenseInformation"   value="${dateLicenseInformation}" />
<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<script src="<c:url value='/scripts/require-min.js' />" data-main="scripts/administration"></script>
<script type="text/javascript">
    requirejs.config({
        waitSeconds: 0
    });
</script>
<layout:info-page-layout titleKey="label.administration">

  <jsp:attribute name="head">
    <script type="text/javascript">
      var squashtm = squashtm || {};
      squashtm.app.userLicenseInformation = "${userLicenseInformation}";
      squashtm.app.dateLicenseInformation = "${dateLicenseInformation}";
    </script>
    <comp:sq-css name="squash.grey.css" />
    <comp:sq-css name="squash.core.override.css" />
  </jsp:attribute>

  <jsp:attribute name="titlePane">
    <h2 class="admin">
      <f:message key="label.administration" />
    </h2>
  </jsp:attribute>

  <jsp:attribute name="informationContent">
    <div id="admin-pane">
      <sec:authorize var="isAdmin" access="hasRole('ROLE_ADMIN')" />
      <c:if test="${(isAdmin and (not empty userLicenseInformation or not empty dateLicenseInformation)) or (not empty userLicenseInformation and userLicenseInformation.contains('false')) or (not empty dateLicenseInformation and dateLicenseInformation < 0)}">
      <div id="information-block">
        <div id="information-block-wrapper" class="ui-widget ui-widget-content ui-corner-all">
          <div class="display-table-row">
            <div class="display-table-cell warning-cell">
              <div class="generic-warning-signal"></div>
            </div>
            <div id="information-block-content" class="display-table-cell"></div>
          </div>
        </div>
      </div>
      </c:if>
      <div id="admin-link-pane">

        <sec:authorize access=" hasRole('ROLE_ADMIN')">
          <a href="${ usersUrl }" class="unstyledLink">
            <span id="user-admin" class="admin-section-icon admin-user-icon"></span>
            <span class="admin-section-label"><f:message key="label.userManagement" /></span>
          </a>
        </sec:authorize>

        <a href="${ projectsUrl }" class="unstyledLink">
          <span id="project-admin" class="admin-section-icon admin-project-icon"></span>
          <span class="admin-section-label"><f:message key="label.projectManagement" /></span>
        </a>

        <sec:authorize var="isAdmin" access="hasRole('ROLE_ADMIN')" />
        <c:if test="${ isAdmin or milestoneFeatureEnabled }">
          <a href="${ milestoneUrl }" class="unstyledLink">
            <span id="milestone-admin" class="admin-section-icon admin-milestone-icon"></span>
            <span class="admin-section-label"><f:message key="label.milestoneManagement" /></span>
          </a>
        </c:if>

        <sec:authorize access=" hasRole('ROLE_ADMIN')">

        <a href="${ customFieldsUrl }" class="unstyledLink">
          <span id="custom-fields-admin" class="admin-section-icon admin-customfields-icon"></span>
          <span class="admin-section-label"><f:message key="label.customFieldsManagement" /></span>
        </a>

        <a href="<c:url value='/administration/info-lists' />" class="unstyledLink">
          <span id="admin-info-list" class="admin-section-icon admin-infolist-icon"></span>
          <span class="admin-section-label"><f:message key="label.infoListManagement" /></span>
        </a>

        <a href="${ reqLinkTypeUrl }" class="unstyledLink">
          <span id="admin-requirement-link-type" class="admin-section-icon admin-req-links-icon"></span>
          <span class="admin-section-label"><f:message key="label.reqLinkTypeManagement" /></span>
        </a>

        <a href="${ bugtrackerUrl }" class="unstyledLink">
          <span id="bug-tracker-admin" class="admin-section-icon admin-bugtracker-icon"></span>
          <span class="admin-section-label"><f:message key="label.bugtrackerManagement" /></span>
        </a>

        <a href="${testAutomationServerUrl}" class="unstyledLink">
          <span id="test-automation-servers-admin" class="admin-section-icon admin-test-automation-servers-icon"></span>
          <span class="admin-section-label"><f:message key="label.testAutomationServersManagement"/></span>
        </a>

        <a href="${scmServerUrl}" class="unstyledLink">
          <span id="scm-servers-admin" class="admin-section-icon admin-scm-servers-icon"></span>
          <span class="admin-section-label"><f:message key="label.scmServersManagement"/></span>
        </a>

        <a id="fake-link" class="unstyledLink"></a>

        </sec:authorize>

      </div>

      <div id="admin-stats">
        <div class="admin-stats-table">
          <label><f:message key="label.version" /></label><span>${sqTMversion}</span>
        </div>
        <div class="admin-stats-table">
          <label><f:message key="label.statistics" /></label>
          <div>
            <div>
              <label><f:message key="label.projects" /></label><span>${ adminStats.projectsNumber }</span>
            </div>
            <div>
              <label><f:message key="label.users" /></label><span>${ adminStats.usersNumber }</span>
            </div>
          </div>
          <div>
            <div>
              <label><f:message key="label.requirements" /></label><span>${ adminStats.requirementsNumber }</span>
            </div>
            <div>
              <label><f:message key="label.testCases" /></label><span>${ adminStats.testCasesNumber }</span>
            </div>
          </div>
          <div>
            <div>
              <label><f:message key="label.campaigns" /></label><span>${ adminStats.campaignsNumber }</span>
            </div>
            <div>
              <label><f:message key="label.iterations" /></label><span>${ adminStats.iterationsNumber }</span>
            </div>
            <div>
              <label><f:message key="label.executions" /></label><span>${ adminStats.executionsNumber }</span>
            </div>
          </div>
          <c:if test="${ adminStats.databaseSize != 0 }">
            <div>
              <div>
                <label><f:message key="label.database.size" /></label><span>${ adminStats.databaseSize } <f:message key="label.database.unit" /></span>
              </div>
            </div>
          </c:if>
        </div>
      </div>

      <div id="admin-small-link-pane">
        <div id="admin-small-link-pane-table">
          <sec:authorize access=" hasRole('ROLE_ADMIN')">
            <a href="${ loginUrl }" class="unstyledLink ">
                <span id="login-message-admin" class="admin-section-icon admin-msglogin-icon-small"></span>
                <span class="admin-section-label"><f:message key="label.consultModifyLoginMessage" /></span>
              </a>
              <a href="${ welcomeUrl }" class="unstyledLink ">
                <span id="welcome-message-admin" class="admin-section-icon admin-msghome-icon-small"></span>
                <span class="admin-section-label"><f:message key="label.consultModifyWelcomeMessage" /></span>
              </a>

            <a href="${ configUrl }" class="unstyledLink ">
              <span id="config-admin" class="admin-section-icon admin-config-icon-small"></span>
              <span class="admin-section-label"><f:message key="label.ModifyConfig" /></span>
            </a>

            <a href="${ cleaningUrl }" class="unstyledLink">
              <span id="cleaning-admin" class="admin-section-icon admin-cleaning-icon-small"></span>
              <span class="admin-section-label"><f:message key="label.Cleaning" /></span>
            </a>

            <a href="${ logfileUrl }" class="unstyledLink ">
              <span id="logfile-admin" class="admin-section-icon admin-logfile-icon-small"></span>
              <span class="admin-section-label"><f:message key="label.DownloadLogfile" /></span>
            </a>
          </sec:authorize>
        </div>
      </div>


    </div>
  </jsp:attribute>
</layout:info-page-layout>
