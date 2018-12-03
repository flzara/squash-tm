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

<c:set var="scmServerId" value="${(not empty project.scmRepository) ? project.scmRepository.scmServerId : 0}"/>
<c:set var="scmRepositoryId" value="${(not empty project.scmRepository) ? project.scmRepository.id : 0}"/>

<f:message var="noServerLabel" key="label.NoServer" />
<f:message var="noRepositoryLabel" key="label.None" />

<comp:toggle-panel id="scm-panel" titleKey="title.scmPanel" open="true">
	<jsp:attribute name="body">

		<%---- ComboBox avec les Serveurs de Source ----%>
		<fieldset id="scm-server-fieldset">
			<legend>
				<f:message key="label.ScmServer" />
			</legend>
			<div id="selected-scm-server">
				<c:out value="${noServerlabel}" escapeXml="true" />
			</div>
		</fieldset>

		<br/>

		<%---- ComboBox avec les Dépôts du Serveur sélectionné ----%>
		<c:if test="${ empty project.scmRepository}">
			<c:set var="repositoriesDisplay" value="display: none"/>
		</c:if>
		<fieldset id="scm-repositories-fieldset" style="${repositoriesDisplay}">
			<legend>
				<f:message key="label.ScmRepository" />
			</legend>
			<c:set var="scmRepositoryName"
            				value="${ (not empty project.scmRepository) ? project.scmRepository.repositoryPath : noRepositoryLabel}"/>
			<div id="selected-scm-repository">
				<c:out value="${scmRepositoryName}" escapeXml="true" />
			</div>
		</fieldset>

	</jsp:attribute>
</comp:toggle-panel>


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
