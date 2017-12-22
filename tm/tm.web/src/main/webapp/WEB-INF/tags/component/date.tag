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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ attribute name="value" required="true" type="java.util.Date" %>
<%@ attribute name="noValueKey" required="false" type="java.lang.String" description="optional message key to use when there is no date to print" %>
<c:choose>
  <c:when test="${ empty value }">
    <f:message key="${ not empty noValueKey ? noValueKey : 'squashtm.dateformat' }" />
  </c:when>
  <c:otherwise>
    <f:message var="pattern" key="squashtm.dateformat" />
    <f:formatDate value="${ value }" pattern="${ pattern }" />
  </c:otherwise>
</c:choose>
