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
<%@ attribute name="auditableEntity" required="true" type="java.lang.Object" description="The entity which general information we want to show" %>
<%@ attribute name="entityUrl" description="REST url representing the entity. If set, this component will pull itself from entityUrl/general" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<f:message var="rawDateFormat" key="squashtm.dateformat.iso"/>
<f:message var="displayDateFormat" key="squashtm.dateformat" />
<f:message var="neverLabel" key="label.lower.Never"/>

<div id="general-information-panel" class="information-panel" data-def="url=${entityUrl}, never=${neverLabel}, format=${displayDateFormat}">
			
	<span ><f:message key="label.CreatedOn" />&nbsp;:&nbsp;</span>
	
	<span id="created-on">
		<span class="datetime"><f:formatDate value="${ auditableEntity.createdOn }" pattern="${rawDateFormat}" timeZone="UTC"/></span> 
		<span class="author">${ auditableEntity.createdBy }</span>
	</span>
	
	<br />
	
	<span><f:message key="label.UpdatedOn" />&nbsp;:&nbsp;</span>
		
	<span id="last-modified-on">	
		<span class="datetime"><f:formatDate value="${ auditableEntity.lastModifiedOn }" pattern="${rawDateFormat}"  timeZone="UTC"/></span> 
		<span class="author">${ auditableEntity.lastModifiedBy }</span>
	</span>
	
	<br />
	
</div>
	<script type="text/javascript">
	require(["common"], function() {
			require(["jquery", "page-components/general-information-panel"], function($, general){
				general.init();
			});
		});
	</script>