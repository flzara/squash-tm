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

<%@ attribute name="adminproject" type="java.lang.Object" required="true" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>

<div class="display-table-row">
	<f:message var="active" key="project.active.label" />
	<f:message var="activate" key="project.activate.label" />
	<f:message var="inactive" key="project.inactive.label" />
	<f:message var="inactivate" key="project.inactivate.label" />
	<label for="project-active" class="display-table-cell "><f:message
			key="project.state.label" /> </label>
	<div class="display-table-cell" id="project-active">
		<c:if test="${adminproject.project.active}">
			<span class="projectActive">${active} </span>
			<sec:authorize access=" hasRole('ROLE_ADMIN')">
				<a id="activateProject" >[${inactivate}]</a>
			</sec:authorize>
		</c:if>
		<c:if test="${!adminproject.project.active}">
			<span class="projectInactive">${inactive} </span>
			<sec:authorize access=" hasRole('ROLE_ADMIN')">
				<a id="activateProject" >[${activate}]</a>
			</sec:authorize>
		</c:if>
	</div>
</div>
<sec:authorize access=" hasRole('ROLE_ADMIN')">
<script>

	var changeActive = ${!adminproject.project.active};

	$(function() {

		$('#activateProject').click(function() {
			changeActiveProject(changeActive);
		});
	});
	function changeActiveProject(active) {

		requestProjectActivation(active).done(function(data) {
			refreshProjectActivationSuccess(data);
		});
	}

	function requestProjectActivation(active) {
		return $.ajax({
			type : 'post',
			data : {
				'isActive' : active
			},
			dataType : "json",
			url : "${ projectUrl }"
		});
	}

	function refreshProjectActivationSuccess(data) {
		var isNowActive = data.active;
		if (isNowActive) {
			var labelInactive = $('#project-description-table .projectInactive');
			labelInactive.removeClass('projectInactive');
			labelInactive.addClass('projectActive');
			labelInactive.text("${active}");

			var linkActivate = $('#project-description-table a#activateProject');
			linkActivate.text("[${inactivate}]");
			changeActive = !isNowActive;
		} else {
			var labelInactive = $('#project-description-table .projectActive');
			labelInactive.removeClass('projectActive');
			labelInactive.addClass('projectInactive');
			labelInactive.text("${inactive}");

			var linkActivate = $('#project-description-table a#activateProject');
			linkActivate.text("[${activate}]");
			changeActive = !isNowActive;
		}
	}
</script>
</sec:authorize>