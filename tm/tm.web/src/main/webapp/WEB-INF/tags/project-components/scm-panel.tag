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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="availableScmServers" type="java.util.Collection" required="true"
	description="The Collection of the available Scm Servers"%>
<%@ attribute name="project" type="java.lang.Object" required="true" description="The TM Project"%>

<c:set var="scmServerId" value="${(not empty project.scmRepository) ? project.scmRepository.scmServer.id : 0}"/>
<c:set var="scmRepositoryId" value="${(not empty project.scmRepository) ? project.scmRepository.id : 0}"/>

<f:message var="noServerLabel" key="label.NoServer" />
<f:message var="noRepositoryLabel" key="label.None" />

<div id="scm-panel">

		<fieldset id="scm-fieldset">
			<legend>
				<f:message key="label.ScmServer" />
			</legend>
			<br />
			<%---- ComboBox with SCM Severs ----%>
			<div id="scm-server-div">
				<label for="selected-scm-server">
					<f:message key="label.Server" />
				</label>
				<div id="selected-scm-server" style="display: inline">
					<c:out value="${noServerlabel}" escapeXml="true" />
				</div>

			</div>
			<br/>
			<%---- ComboBox with Repositories contained in the selected Server ----%>
			<c:if test="${ empty project.scmRepository}">
				<c:set var="repositoriesDisplay" value="display: none"/>
			</c:if>
			<div id="scm-repositories-div" style="${repositoriesDisplay}">
				<label for="selected-scm-repository">
					<f:message key="label.ScmRepository" />
				</label>
				<c:set var="scmRepositoryName"
								value="${ (not empty project.scmRepository) ? project.scmRepository.name : noRepositoryLabel}"/>
				<div id="selected-scm-repository" style="display: inline">
					<c:out value="${scmRepositoryName}" escapeXml="true" />
				</div>
			</div>
		</fieldset>

</div>


<%----- JS Initialization -----%>

<script type="text/javascript">

require(["common"], function() {
	require(["jquery", "projects-manager/project-info/scm-panel"], function($, ScmPanel) {
		let scmServersJson = ${json:serialize(availableScmServers)};
		let projectId = ${project.id};
		let boundServerId = ${scmServerId};
		let boundRepositoryId = ${scmRepositoryId};
		new ScmPanel(projectId, boundServerId, boundRepositoryId, scmServersJson);
	});
});

</script>
