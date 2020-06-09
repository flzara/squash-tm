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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<s:url var="showExecutionUrl" value="/executions"/>


<f:message var="labelNoDataset" key="automated-suite.execution.no-dataset"/>

<%--

Note : below we define colspan and width for the columns of the nested table, that must match the columns of
the host test plan table. Please refer to test-suite-automated-suites-panel.tag to check those definitions.

We can set totalColspan arbitrarily high to be sure that this td will be as long as one row whatever the actual
number of columns.
--%>

<c:set var="totalColspan" value="26"/>


<td colspan="${totalColspan}">
  <table class="executions-table" id="automated-suite-${automatedSuite.id}">
    <thead>
    <tr class="executions-table-header">
      <th></th>
      <th class="width-tenperc"></th>
      <th class="width-tenperc"></th>
      <th class="width-tenperc"></th>
      <th class="width-tenperc"></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${ executions }" var="execution" varStatus="status">
      <tr>
        <td>
          <a href="${showExecutionUrl}/${execution.id}">
            <span style="font-weight:bold;">Exec. ${status.index + 1} :</span>
            <span> <c:out value="${ execution.name }"/> </span>
            <span> <c:out value="(${(execution.datasetLabel == null || fn:length(execution.datasetLabel) == 0) ? labelNoDataset : execution.datasetLabel})"/> </span>
          </a>
        </td>
        <td>
          <span class="exec-status-label exec-status-${fn:toLowerCase(execution.executionStatus)}">
            <f:message key="execution.execution-status.${execution.executionStatus}"/>
          </span>
        </td>
        <td></td>
        <td></td>
        <td style="text-align: center">
          <c:choose>
            <c:when test="${ execution.lastExecutedOn != null }">
              <f:message var="dateFormat" key="squashtm.dateformat"/>
              <em><f:formatDate value="${ execution.lastExecutedOn }"
                                pattern="${dateFormat}"/> </em>
            </c:when>
            <c:otherwise>
              <em><f:message key="squashtm.nodata"/> </em>
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
</td>
