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
<%@ tag body-content="empty"
	description="all denormalized field label + value as display-table-row"%>
<%@ attribute name="customFieldValues" required="true"
	description="list of DenormalizedFieldValue" type="java.lang.Object"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:forEach var="cfv" items="${ customFieldValues }">
	<div class="display-table-row">
		<label class="display-table-cell" for="cfv-${ cfv.binding.customField.id }">${ cfv.binding.customField.label }</label>
		<div class="display-table-cell">${ cfv.value }</div>
	</div>
</c:forEach>