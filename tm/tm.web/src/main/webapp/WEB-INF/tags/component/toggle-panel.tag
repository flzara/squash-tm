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
<%@ tag description="component for jquery toggle panel with buttons" %>
<%@ attribute name="title" description="Title of the panel. Alternative : set the titleKey attribute"%>
<%@ attribute name="titleKey" description="Key of the panel title. Alternative : set the title attribute" %>
<%@ attribute name="open" description="true if the panel should be opened when rendered" %>
<%@ attribute name="panelButtons" fragment="true" description="add buttons to the togglepanel" %>
<%@ attribute name="body" fragment="true" description="body of the panel" %>
<%@ attribute name="id" required="true" description="the id of the panel" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>


<c:set var="headerclass" value="${not empty open and not open ? 'collapse' : 'expand' }"/>

<c:if test="${ not empty titleKey }">
	<f:message var="title" key="${ titleKey }" />
</c:if>

<div class="${ headerclass } sq-tg">
	<div class="tg-head">
		<h3>${title}</h3>
		<div class="tg-toolbar">
			<jsp:invoke fragment="panelButtons"/>
		</div>
	</div>
	<div id="${id}" class="tg-body">
		<jsp:invoke fragment="body"/>
	</div>
</div>

