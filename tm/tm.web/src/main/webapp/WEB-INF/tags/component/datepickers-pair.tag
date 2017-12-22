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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<f:message var="scheduledStartLabel" key="dialog.label.campaign.scheduled_start.label" />
<f:message var="scheduledEndLabel" key="dialog.label.campaign.scheduled_end.label" />

<f:message var="dateFormat" key="squashtm.dateformatShort" />
<f:message var="dateFormatDp" key="squashtm.dateformatShort.datepicker" />

<div class="datepicker-pair">
	<div class="datepicker start-date">
		<div class="datepicker-caption">
			<c:if test="${ (not empty scheduledStartLabel)}">
				<label>${scheduledStartLabel}</label>&nbsp;
			</c:if>
		</div>
		<div class="datepicker-date">
			<input id="scheduled-start" type="text" class="date-hidden" />
			<span id="scheduled-start-label" style="color: #4297d7; font-weight: bold"></span>
		</div>
		<div style="clear: both; visibility: hidden;"></div>
	</div>
	<div class="datepicker end-date">
		<div class="datepicker-caption">
			<c:if test="${ (not empty scheduledEndLabel)}">
				<label>${scheduledEndLabel}</label>&nbsp;
		</c:if>
		</div>
		<div class="datepicker-date">
			<input id="scheduled-end" type="text" class="date-hidden" />
			<span id="scheduled-end-label" style="color: #4297d7; font-weight: bold"></span>
		</div>
		<div style="clear: both; visibility: hidden;"></div>
	</div>
</div>

<style>
.start-date {
	margin-bottom: 6px;
}
</style>