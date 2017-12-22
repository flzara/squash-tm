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
<%@ tag description="general information panel for an auditable entity. Client can add more info in the body of this tag" body-content="scriptless" %>
<%@ attribute name="statisticsEntity" required="true" type="java.lang.Object" description="The entity which general information we want to show" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<c:if test="${not empty statisticsEntity}">
				<span ><f:message key="label.progress" />&nbsp;:&nbsp;<strong>${ statisticsEntity.progression }</strong>%&nbsp;(&nbsp;${ statisticsEntity.nbDone }&nbsp;/&nbsp;${ statisticsEntity.nbTestCases }&nbsp;)&nbsp;&nbsp;&nbsp;</span>
				<br />


				<br />
				<div id="table-div" class="dataTables_wrapper">
					<table id="stats-table" class="is-contextual center">
						<thead>
							<tr>
								<th class="ui-state-default"><f:message key="label.numberOfTestCases" /></th>
								<th class="ui-state-default"><f:message key="label.successful" /></th>
								<c:if test="${allowsSettled}">
								<th class="ui-state-default"><f:message key="label.Settled" /></th>
								</c:if>
								<th class="ui-state-default"><f:message key="label.plur.failed" /></th>
								<th class="ui-state-default"><f:message key="label.Running" /></th>
								<th class="ui-state-default"><f:message key="label.plur.blocked" /></th>
								<c:if test="${allowsUntestable}">
								<th class="ui-state-default"><f:message key="label.plur.untestable" /></th>
								</c:if>
								<th class="ui-state-default"><f:message key="label.Ready" /></th>
							</tr>
						</thead>
						<tbody>
							<tr id="stats:1" class="odd ui-state-highlight">
								<td>${ statisticsEntity.nbTestCases }</td>
								<td>${ statisticsEntity.nbSuccess }</td>
								<c:if test="${allowsSettled}">
								<td>${ statisticsEntity.nbSettled }</td>
								</c:if>
								<td>${ statisticsEntity.nbFailure }</td>
								<td>${ statisticsEntity.nbRunning }</td>
								<td>${ statisticsEntity.nbBlocked }</td>
								<c:if test="${allowsUntestable}">
								<td>${ statisticsEntity.nbUntestable }</td>
								</c:if>
								<td>${ statisticsEntity.nbReady }</td>
							</tr>
						</tbody>
					</table>
				</div>
</c:if>
