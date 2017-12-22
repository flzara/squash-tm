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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>

<comp:toggle-panel id="test-case-prerequisite-panel" titleKey="generics.prerequisite.title" 
				   open="${ not empty testCase.prerequisite }">
	<jsp:attribute name="body">
		<div id="test-case-prerequisite-table" class="display-table">
			<div class="display-table-row">
				<div class="display-table-cell" id="test-case-prerequisite">${ testCase.prerequisite }</div>
			</div>
		</div>
	</jsp:attribute>
</comp:toggle-panel>
