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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>


<c:set var="executable" value="${false}"/>

<c:if test="${not milestoneConf.locked}">
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ iteration }">
	<c:set var="executable" value="${ true }" />
</authz:authorized>
</c:if>

<s:url var="newExecutionUrl"
	value="/iterations/{iterId}/test-plan/{tpId}/executions/new">
	<s:param name="iterId" value="${iterationId}" />
	<s:param name="tpId" value="${testPlanItem.id}" />
</s:url>

<s:url var="showExecutionUrl" value="/executions" />


<f:message var="labelNodata" key="squashtm.nodata" />
<f:message var="labelNone" key="label.None" />

<%--

Note : below we define colspan and width for the columns of the nested table, that must match the columns of
the host test plan table. Please refer to iteration-test-plan-panel.tag to check those definitions.

We can set totalColspan arbitrarily high to be sure that this td will be as long as one row whatever the actual
number of columns.
--%>

<c:set var="totalColspan" value="26"/>



<td colspan="${totalColspan}">
	<table class="executions-table" id="item-test-plan-${testPlanItem.id}">
      <thead>
        <tr class="executions-table-header">
          <th></th>
          <th class="tp-row-dataset width-tenperc"></th>
          <th class="width-tenperc"></th>
          <th class="width-tenperc"></th>
          <th class="width-tenperc"></th>
          <th class="width-tenperc"></th>
          <th class="width-tenperc"></th>
          <th class="narrow"></th>
          <th class="narrow"></th>
        </tr>
      </thead>
      <tbody>
		<c:forEach items="${ executions }" var="execution" varStatus="status">
			<tr>
				<td >
					<a href="${showExecutionUrl}/${execution.id}">
						<span style="font-weight:bold;">Exec. ${status.index + 1} :</span>
                        <span> ${ execution.name }</span>
					</a>
				</td>
				<td class="tp-row-dataset">
					<span >
                      <c:out value="${(execution.datasetLabel == null) ? labelNodata :
                                      (fn:length(execution.datasetLabel) == 0) ? labelNone : execution.datasetLabel}" />
					</span>
				</td>
				<td></td>
				<td>
                    <f:message key="execution.execution-status.${execution.executionStatus}" />
				</td>
				<td></td>
				<td>
					<span>
					<c:choose>
						<c:when test="${ execution.lastExecutedBy != null }">
							<em>${ execution.lastExecutedBy }</em>
						</c:when>
						<c:otherwise>
							<em><f:message key="squashtm.nodata" /> </em>
						</c:otherwise>
					</c:choose>
					</span>
				</td>
				<td>
				<c:choose>
					<c:when test="${ execution.lastExecutedOn != null }">
						<f:message var="dateFormat" key="squashtm.dateformat" />
						<em><f:formatDate value="${ execution.lastExecutedOn }"
								pattern="${dateFormat}" /> </em>
					</c:when>
					<c:otherwise>
						<em><f:message key="squashtm.nodata" /> </em>
					</c:otherwise>
				</c:choose>
				</td>
                <td></td>
				<td style="text-align:center;">
					<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ execution }">
					<f:message var="labelRemoveExec" key="label.removeExecution"/>
					<a id="delete-execution-table-button-${execution.id}"  class="delete-execution-table-button" title="${labelRemoveExec}"></a>
					</authz:authorized>
				</td>
			</tr>
		</c:forEach>
		<c:if test="${ executable && !testPlanItem.testCaseDeleted }">
			<tr>
				<td colspan="${totalColspan}" style="color:white; font-style:normal;">
					<strong>
						<a class="button new-exec" style="font-size:0.8em;" id="new-exec-${ testPlanItem.id }"  data-new-exec="${ newExecutionUrl }">
							<f:message key="execution.iteration-test-plan-row.new" />
						</a>
						<c:if test="${testPlanItem.automated}">
						<a	class="button new-auto-exec" style="font-size:0.8em;" id="new-auto-exec-${ testPlanItem.id }"  data-tpi-id="${ testPlanItem.id }">
							<f:message key="execution.iteration-test-plan-row.new.auto" />
						</a>
						</c:if>
					</strong>
				</td>

			</tr>

		</c:if>
    </tbody>
	</table>
</td>
