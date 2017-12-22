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
<%@ tag
	description="general information panel for an auditable entity. Client can add more info in the body of this tag"
	pageEncoding="utf-8"%>
<%@ attribute name="url" required="true"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="auto-exec-btns-panel" class="btn-group" data-suites-url="<c:url value='/automated-suites' />">
	<f:message var="autoExecLabel" key="iteration.suite.execution.auto.label" />
	<input id="execute-auto-button" class="run-menu sq-btn" type="button" value="${autoExecLabel}" />
	<ul class="not-displayed">
		<li>
			<a id="execute-auto-execute-all" >
				<f:message	key="iteration.suite.execution.auto.all.label" /> 
			</a>
		</li>
		<li>
			<a id="execute-auto-execute-selection" >
				<f:message	key='iteration.suite.execution.auto.selection.label' /> 
			</a>
		</li>
	</ul>
	
</div>
<script>
publish("reload.auto-exec-btns-panel")
</script>
