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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="stru" uri="http://org.squashtest.tm/taglib/string-utils" %>


<%@ attribute name="attachmentSet" type="java.util.Set" description="Set of attachments" %>
<%@ attribute name="attachListId" type="java.lang.Long" description="id of the attachment list" %>


<?xml version="1.0" encoding="utf-8" ?>

<%@ tag language="java"  pageEncoding="utf-8"%>
<%--
	@params 
	
	attachmentSet : set of attachments

 --%>
<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<s:url var="dlUrl" value="/attach-list/${attachListId}/attachments/download"/>

<c:forEach var="attachment" items="${attachmentSet}">
	<div class="div-attachments-item" style="text-align:center;" >
		<div class="attachment-file file-${fn:toLowerCase(attachment.type)}"></div>
		<span><a  href="${dlUrl}/${attachment.id}" target="_blank" class="breakwords" >${stru:truncateAndEllipse(attachment.name, 45)}</a></span> 
	</div>
</c:forEach>

