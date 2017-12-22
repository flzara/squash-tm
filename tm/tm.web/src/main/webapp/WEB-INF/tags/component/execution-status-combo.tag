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
<%@ tag body-content="empty" description="Outputs a combobox of execution statuses with no selection" %>
<%@ attribute name="id" required="true" description="The html id of the combo" %>
<%@ attribute name="name" required="true" description="The name attribute of the combo" %>
<%@ attribute name="selected" required="true" description="The status currently selected" %>
<%@ attribute name="allowsUntestable" required="true" description="Wether the status untestable is allowed" %>
<%@ attribute name="allowsSettled" required="true" description="Wether the status settled is allowed" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<select id="${ id }" name="${ name }" class="execution-status-combo-class">
	<c:if test="${allowsUntestable}">
		<option ${'UNTESTABLE' == selected ? 'selected="selected"' : ''}
            value="UNTESTABLE" class="exec-status-option exec-status-untestable" >
          <f:message key="execution.execution-status.UNTESTABLE" />
        </option>
	</c:if>
	<option ${'BLOCKED' == selected ? 'selected="selected"' : ''}
            value="BLOCKED" class="exec-status-option exec-status-blocked">
        <f:message key="execution.execution-status.BLOCKED" />
    </option>
	<option ${'FAILURE' == selected ? 'selected="selected"' : ''}
            value="FAILURE" class="exec-status-option exec-status-failure">
      <f:message key="execution.execution-status.FAILURE" />
     </option>
	 <c:if test="${allowsSettled}">
   	<option ${'SETTLED' == selected ? 'selected="selected"' : ''}
            value="SETTLED" class="exec-status-option exec-status-settled">
        <f:message key="execution.execution-status.SETTLED" />
    </option>
	</c:if>
	<option ${'SUCCESS' == selected ? 'selected="selected"' : ''}
            value="SUCCESS" class="exec-status-option exec-status-success">
        <f:message key="execution.execution-status.SUCCESS" />
    </option>
	<option ${'READY' == selected ? 'selected="selected"' : ''}
            value="READY" class="exec-status-option exec-status-ready">
      <f:message key="execution.execution-status.READY" />
    </option>
</select >				
