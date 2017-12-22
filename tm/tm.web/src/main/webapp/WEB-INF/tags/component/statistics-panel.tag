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
<%@ attribute name="statisticsUrl" required="true" description="the url where get fresh statistics infos" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<c:if test="${not empty statisticsEntity}">
<comp:toggle-panel id="statistics-toggle-panel" titleKey="title.statistics" open="true"  >
	<jsp:attribute name="body">
			<div id="statistics-panel">
				<comp:statistics-panel-content statisticsEntity="${ statisticsEntity }"/>
			</div>
	</jsp:attribute>
</comp:toggle-panel>
<script>
function refreshStatistics(){
	$('#statistics-panel').load('${ statisticsUrl }');
}
</script>
</c:if>