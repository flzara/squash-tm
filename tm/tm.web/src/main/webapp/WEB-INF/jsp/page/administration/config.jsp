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
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url var="administrationUrl" value="/administration"/>

<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="addLabel" key="label.Add"/>
<f:message var="cancelLabel" key="label.Cancel"/>

<layout:info-page-layout titleKey="label.ModifyConfig" isSubPaged="true" main="advanced-config-page">
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
    <f:message key="label.ModifyConfig"/>
  </h2>
  </jsp:attribute>

  <jsp:attribute name="subPageButtons">
  <a class="sq-btn" href="${administrationUrl}"><f:message key="label.Back"/></a>
  </jsp:attribute>

  <jsp:attribute name="footer"/>
  <jsp:attribute name="informationContent">
  <c:url var="clientsUrl" value="/administration/config/clients/list"/>
  <c:url var="addClientUrl" value="/administration/config/clients"/>

  <div id="config-page-content" class="admin-message-page-content">

    <div id="config-info-panel" class="expand sq-tg">
      <div class="tg-head">
        <h3><f:message key="label.Attachments"/></h3>
      </div>

      <div class="tg-body">

        <div id="config-table" class="display-table">
          <div class="display-table-row">
            <label for="whiteList" class="display-table-cell"><f:message key="label.whiteList"/></label>
            <div class="display-table-cell editable text-editable large" id="whiteList">${whiteList}</div>
          </div>
        </div>

        <div id="config-table" class="display-table">
          <div class="display-table-row">
            <label for="uploadSizeLimit" class="display-table-cell"><f:message key="label.uploadSizeLimit"/></label>
            <div class="display-table-cell editable text-editable" id="uploadSizeLimit">${uploadSizeLimit}</div>
          </div>
          <span><f:message key="label.uploadSize.warning"/></span>
        </div>

        <div id="config-table" class="display-table">
          <div class="display-table-row">
            <label for="uploadImportSizeLimit" class="display-table-cell"><f:message
              key="label.uploadImportSizeLimit"/></label>
            <div class="display-table-cell editable text-editable"
                 id="uploadImportSizeLimit"> ${uploadImportSizeLimit} </div>
          </div>
          <span><f:message key="label.uploadSize.warning"/></span>
        </div>


      </div>
    </div>

    <%-- Callback URL panel --%>

    <div id="callback-url-panel" class="sq-tg expand">
      <div class="tg-head">
        <h3><f:message key="label.PublicUrl" /></h3>
      </div>
      <div class="tg-body">
        <div class="display-table">
          <div class="display-table-row control-group">
            <label for="callback-url"><f:message key="label.SquashPublicUrl"/></label>
            <div id="callbackUrl" type="text" size="50" class="display-table-cell">${callbackUrl}</div>
            <span class="help-inline" />
          </div>
        </div>
      </div>
    </div>

    <%-- // Callback URL panel --%>

    <%-- stack trace feature --%>
    <c:if test="${ shouldDisplayStackTraceControlPanel }">
      <div id="stack-trace-config-panel" class="sq-tg expand">
        <div class="tg-head">
          <h3><f:message key="title.display.error.detail"/></h3>
        </div>
        <div class="tg-body">
          <div class="controls">
            <c:set var="checked" value="${ stackTrace ? 'checked=checked' : '' }"/>
            <input id="stack-trace" type="checkbox" value="enabled" ${ checked } />
            <span class="help-inline">&nbsp;</span>

          </div>
        </div>
      </div>
    </c:if>
    <%-- stack trace feature --%>

    <%-- case sensitivity feature --%>
    <div id="client-config-panel" class="sq-tg expand">
      <div class="tg-head">
        <h3><f:message key="title.loginCaseSensitivity"/></h3>
      </div>
      <div class="tg-body">
        <div>
          <div class="control-group">
            <div class="controls">
              <c:if test="${ empty duplicateLogins }">
              <c:set var="checked" value="${ caseInsensitiveLogin ? 'checked=checked' : '' }"/>
              <input id="case-insensitive-login" type="checkbox" value="enabled" ${ checked } />
              <span class="help-inline">&nbsp;</span>
              </c:if>

              <c:if test="${ not empty duplicateLogins }">
              <span>
                <c:choose>
                <c:when test="${ caseInsensitiveLogin }">
                <f:message key="label.insensitive"/>
                </c:when>
                <c:otherwise>
                <f:message key="label.sensitive"/>
                </c:otherwise>
                </c:choose>
              </span>
              <span class="help-inline"><f:message key="message.removeDuplicateLogins"/></span>
              </c:if>
            </div>
          </div>

          <c:if test="${ not empty duplicateLogins }">
          <div class="control-group">
            <label class="control-label" for="label"><f:message key="label.duplicateLogins"/></label>
            <div class="controls">
              <ul>
                <c:forEach items="${ duplicateLogins }" var="login">
                <li>${ login }</li>
                </c:forEach>
              </ul>
            </div>
          </div>
        </div>
        </c:if>
      </div>
    </div>
    <%-- /case sensitivity feature --%>



  </jsp:attribute>
</layout:info-page-layout>
