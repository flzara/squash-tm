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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>

<s:url var="administrationUrl" value="/administration"/>

<layout:info-page-layout titleKey="label.Cleaning" isSubPaged="true" main="cleaning-page">

  <jsp:attribute name="head">
    <script type="text/javascript">
      squashtm = squashtm || {};
      squashtm.appRoot = '<c:url value="/" />';
    </script>
    <comp:sq-css name="squash.grey.css"/>
  </jsp:attribute>

  <jsp:attribute name="titlePane">
    <h2 class="admin">
      <f:message key="label.administration"/>
    </h2>
  </jsp:attribute>

  <jsp:attribute name="subPageTitle">
    <h2>
      <f:message key="label.Cleaning"/>
    </h2>
  </jsp:attribute>

  <jsp:attribute name="subPageButtons">
    <a class="sq-btn" href="${administrationUrl}">
      <f:message key="label.Back"/>
    </a>
  </jsp:attribute>

  <jsp:attribute name="footer"/>

  <jsp:attribute name="informationContent">
    <div id="cleaning-page-content" class="admin-message-page-content">

      <div id="cleaning-panel" class="sq-tg expand">
        <div class="tg-head">
          <h3><f:message key="label.CleaningAutomatedSuites"/></h3>
        </div>
        <div class="tg-body">
          <div class="display-table">
            <div class="display-table-row">
              <label for="delete-automated-suites-and-executions">
                <f:message key="label.DeleteAutomatedSuitesAndExecutions"/>
              </label>
              <div id="delete-automated-suites-and-executions" class="display-table-cell">
                <a class="sq-btn">
                  <f:message key="label.Delete"/>
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </jsp:attribute>
</layout:info-page-layout>

<f:message var="warningPopupTitle" key="label.CleaningAutomatedSuites" />
<f:message var="warningContent" key="dialog.clean-automated-suites.warning" />
<f:message var="informationContent" key="dialog.clean-automated-suites.information" />
<f:message var="automatedSuites" key="tabs.label.automated-suites" />
<f:message var="automatedExecutions" key="label.automatedExecution" />
<f:message var="confirmQuestion" key="dialog.label.delete-node.label.confirm" />
<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="cancelLabel" key="label.Cancel" />
<div id="clean-automated-suites-popup" class="popup-dialog not-displayed" title="${warningPopupTitle}">

  <div class="display-table-row">
    <div class="display-table-cell warning-cell">
      <div class="generic-warning-signal"></div>
    </div>
      <div class="generic-warning-main display-table-cell" style="padding-top:20px">
        <span><c:out value="${warningContent}" /></span>
        <br/>
        <br/>
        <span><c:out value="${informationContent}" /></span>
        <br/>
        <br/>
        <span><c:out value="${automatedSuites}" /> : </span><span id="automated-suites-count"></span>
        <br/>
        <span><c:out value="${automatedExecutions}" /> : </span><span id="automated-executions-count"></span>
        <br/>
        <br/>
        <span><c:out value="${confirmQuestion}" /></span>
      </div>
  </div>

  <div class="popup-dialog-buttonpane">
    <input class="confirm" type="button" value="${confirmLabel}" />
    <input class="cancel" type="button" value="${cancelLabel}" />
  </div>

</div>
