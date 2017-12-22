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
<%@ taglib prefix="lay" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="highlightedWorkspace" required="false" description="the highlighted workspace in the navigation bar." %>
<%@ attribute name="main" required="false" %>
<%-- the declaration oder does matter --%>

<script type="text/javascript">
var require = require || {};
require.baseUrl = "${pageContext.servletContext.contextPath}/scripts";
	var squashtm = {};
	squashtm.app = {
		contextRoot: "${pageContext.servletContext.contextPath}",
		locale : "<f:message key='squashtm.locale'/>",
		ckeditorLanguage: "<f:message key='rich-edit.language.value' />"
    	};
	
<lay:_common-lang/>
	
</script>
<script type="text/javascript" src="<c:url value='/scripts/pubsub-boot.js' />"></script>
<c:choose>
  <c:when test="${ not empty main }">
<script  charset="utf-8" src="<c:url value='/scripts/require-min.js' />" data-main="${ main }"></script>
  </c:when>
  <c:otherwise>
<script  charset="utf-8" src="<c:url value='/scripts/require-min.js' />"  data-main="legacy-ws-page"></script>
  </c:otherwise>
</c:choose>
