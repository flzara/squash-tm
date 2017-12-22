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
<%@ tag body-content="empty" %>

<%@ attribute name="requirementVersion" required="true" type="java.lang.Object" rtexprvalue="true" %>
<%@ attribute name="tableModel" required="false" type="java.lang.Object" rtexprvalue="true" 
			  description="the initial data to be displayed in the table. Will be loaded through ajax if left blank."%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="cmp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<c:url var="changesBaseUrl" value="/audit-trail/requirement-versions/fat-prop-change-events" />
<c:url var="modelUrl" value="/audit-trail/requirement-versions/${ requirementVersion.id }/events-table" />
<c:url var="tableLangUrl" value="/datatables/messages" />

<c:set var="deferloadingClause" value=""/>
<c:if test="${not empty tableModel}"><c:set var="deferloadingClause" value=", deferloading=${tableModel.iTotalRecords}"/></c:if>


<cmp:toggle-panel id="requirement-audit-trail-panel" titleKey="title.EditHistory" open="false">
	<jsp:attribute name="body">		
		<div>
			<table id="requirement-audit-trail-table" class="unstyled-table" data-def="ajaxsource=${modelUrl}, 
																datakeys-id=event-id, pagesize=50 ${deferloadingClause}" >
				<thead>
					<tr>
						<th data-def="map=event-date"><f:message key="label.Date" /></th>
						<th data-def="map=event-author"><f:message key="label.User" /></th>
						<th data-def="map=event-message, sClass=event-message-cell"><f:message key="label.Event" /></th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div> 
	</jsp:attribute>
</cmp:toggle-panel>

<span id="show-audit-event-details-template" class="not-displayed">&nbsp;<span style="text-decoration:underline;cursor:pointer" id="show-audit-event-details"><f:message key="message.property-change.show-details.label" /></span></span>

<%-- /AUDIT TRAIL --%>

<%-- AUDIT EVENT DETAILS --%>	
<f:message var="auditEventDetailsDialogTitle" key="message.property-change.show-details.title" />	
<div id="audit-event-details-dialog" class="not-displayed popup-dialog" title="${ auditEventDetailsDialogTitle }">
	<div class="display-table">
		<div>
			<label for="audit-event-old-value"><f:message key="message.property-change.old-value.label" /></label>
			<span id="audit-event-old-value">old value</span>
		</div>
		<div class="display-table-row">
			<label for="audit-event-new-value"><f:message key="message.property-change.new-value.label" /></label>
			<span id="audit-event-new-value">new value</span>
		</div>
	</div>
</div>


