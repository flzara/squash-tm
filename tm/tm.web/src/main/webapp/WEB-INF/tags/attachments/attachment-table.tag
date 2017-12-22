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
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ attribute name="baseURL" type="java.lang.Object"  description="the base url for attachments table"%>
<%@ attribute name="editable" type="java.lang.Boolean" description="List of attachments is editable. Defaults to false." required="true"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>



<c:set var="btnDeleteClause" value=""/>
<c:set var="prefilledClause" value=""/>

<c:if  test="${editable}"> <c:set var="btnDeleteClause" value=", delete-button"/></c:if>
<c:if test="${not empty model}"><c:set var="prefilledClause" value=", pagesize=50, deferloading=${model.iTotalRecords}"/></c:if>

<table id="attachment-detail-table" class="unstyled-table" data-def="ajaxsource=${baseURL}/details, 
													   pre-sort=1-asc 
													   ${prefilledClause}" >
	<thead>
		<tr>
			<th data-def="map=entity-index, select, narrow, center">#</th>
			<th data-def="map=hyphenated-name, sortable, center, link=${baseURL}/download/{entity-id}"><f:message key="label.Name"/></th>	
			<th data-def="map=size, center, sortable"><f:message key="label.SizeMb"/></th>
			<th data-def="map=added-on, center, sortable"><f:message key="label.AddedOn"/></th>
			<th data-def="map=empty-delete-holder${btnDeleteClause}">&nbsp;</th> 
		</tr>
	</thead>
	<tbody>
		<%-- Will be populated through ajax (if no ${model} is present) --%>
	</tbody>
</table>

