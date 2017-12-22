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
<%@ tag
  description="general information panel for an auditable entity. Client can add more info in the body of this tag"
  body-content="scriptless"%>
<%@ attribute name="auditableEntity" required="true" type="java.lang.Object"
  description="The entity which general information we want to show"%>
<%@ attribute name="entityUrl"
  description="REST url representing the entity. If set, this component will pull itself from entityUrl/general"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="wu" uri="http://org.squashtest.tm/taglib/workspace-utils" %>

<f:message var="rawDateFormat" key="squashtm.dateformat.iso" />
<f:message var="displayDateFormat" key="squashtm.dateformat" />
<f:message var="neverLabel" key="label.lower.Never" />

<f:message var="entityStatus" key="${auditableEntity.executionStatus.canonicalStatus.i18nKey}" />
<c:set var="statusClass" value="exec-status-${fn:toLowerCase(auditableEntity.executionStatus.canonicalStatus)}" />

<c:if test="${ auditableEntity.automated }">
  <f:message var="autoEntityStatus" key="${auditableEntity.executionStatus.i18nKey}" />
  <c:set var="autoStatusClass" value="exec-status-${fn:toLowerCase(auditableEntity.executionStatus)}" />
</c:if>

<div id="general-information-panel" class="information-panel"
  data-def="url=${entityUrl}, never=${neverLabel}, format=${displayDateFormat}">



  <div id="general-info-execmode" style="display: inline-block; margin-right: 2em; vertical-align: top">
    <label>
      <f:message key="label.ExecutionMode" />
    </label>
    <span id="execmode-label"><f:message key="${ auditableEntity.executionMode.i18nKey }" /></span>
  </div>

  <div id="general-info-execstatus" style="display: inline-block; margin-right: 2em; vertical-align: top">
    <label>
      <f:message key="label.Status" />
    </label>
    <span id="execstatus-label"> <span class="exec-status-label ${statusClass}"
      style="white-space: nowrap; display: inline-block;">${entityStatus}</span>
    </span>

    <c:if test="${ auditableEntity.automated }">
      <br>
      <label>
        <f:message key="label.AutomatedTestStatus" />
      </label>
      <span id="autostatus-label"> <span class="exec-status-label ${autoStatusClass}"
        style="white-space: nowrap; display: inline-block;">${autoEntityStatus}</span>
      </span>
    </c:if>
  </div>

  <div id="general-info-executed-on" style="display: inline-block; margin-right: 2em; vertical-align: top">
    <label for="last-executed-on">
      <f:message key="label.LastExecutionOn" />
    </label>
    <span id="last-executed-on"> <span class="datetime"><f:formatDate
          value="${ auditableEntity.lastModifiedOn }" pattern="${rawDateFormat}" timeZone="UTC" /></span> <span class="author">${
        auditableEntity.lastModifiedBy }</span>
    </span>

  </div>


  <c:if test="${auditableEntity.automated}">
    <c:set var="taDisassociated" value="${execution.automatedExecutionExtender.projectDisassociated}" />
    <c:set var="taNotOverYet" value="${execution.automatedExecutionExtender.notOverYet}" />
    <c:set var="jobURL" value="${ wu:getAutomatedJobURL(pageContext.servletContext, execution.id) }"/>
    
    <f:message var="taNotOverYetLabel" key="url.resultNotAvailable" />
    <f:message var="taDisassociatedLabel" key="url.resultObsolete" />
    
    <div id="general-info-resulturl" style="display: inline-block; margin-right: 2em; vertical-align: top">
      <div>
      <label for="resulturl-link">
        <f:message key="label.resultURL" />
      </label>
      <a id="resulturl-link" href="${execution.resultURL}" target="_blank">
        <%-- the following reads : if the project was dissociated display 'disassociated', else if it's still running display 'still running', else display the url --%>
        <c:out value="${taDisassociated ? taDisassociatedLabel : taNotOverYet ? taNotOverYetLabel : execution.resultURL}" />
       </a>
       </div>
       
       <div>
       <label><f:message key="label.job.url"/></label><a href="${jobURL}" target="_blank"><c:out value="${jobURL}" default="${taDisassociatedLabel}" /></a>
       </div>
    </div>
  </c:if>


</div>