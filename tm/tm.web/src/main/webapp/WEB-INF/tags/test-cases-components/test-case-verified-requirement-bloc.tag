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
<%@ tag body-content="empty" description="inserts the html table of verified resquirements" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>


<%@ attribute name="containerId"    required="true" description="if of dom container that will hold the table events" %>
<%@ attribute name="linkable" 	    required="true" description="boolean that says if the concerned test case is viewed by a user who has LINK rights on this entity" %>
<%@ attribute name="testCase" 	    required="true" description="the test case" type="java.lang.Object"%>
<%@ attribute name="milestoneConf"  required="true" description="an instance of MilestoneFeatureConfiguration"   type="java.lang.Object"%>


<c:url var="root" value="/" />
<c:url var="verifiedRequirementsTableUrl"	value="/test-cases/${testCase.id}/verified-requirement-versions?includeCallSteps=true" />
<c:url var="verifiedRequirementsUrl" 		value="/test-cases/${testCase.id }/verified-requirement-versions"/>

<script type="text/javascript">
			if (!squashtm) {
				var squashtm = {};
			}
			if (!squashtm.app) {
				squashtm.app = {
					locale : "<f:message key='squashtm.locale'/>",
					contextRoot : "${root}"
				};
			}
			squashtm.app.verifiedRequirementsBlocSettings = {
				containerId : "${containerId}",
				linkable : "${linkable}",
				url :"${verifiedRequirementsUrl}"
			};
</script>

<f:message var="panelTitle" key="label.verifiedRequirements.test-cases"/>

<div id="verified-requirements-bloc-frag">
	<comp:toggle-panel id="verified-requirements-panel" title="${panelTitle}" >
		<jsp:attribute name="panelButtons">
			<c:if test="${ linkable }">					
			<f:message var="associateLabel"	key="label.associateRequirements" />
				<button id="add-verified-requirements-button" class="sq-icon-btn btn-sm" type="submit" title="${associateLabel}" >
                	<span class="ui-icon ui-icon-plus squared-icons">+</span>
                </button>
			
			<f:message var="removeLabel" key="label.desassociationRequirements" />
				<button id="remove-verified-requirements-button" class="sq-icon-btn btn-sm" type="submit" title="${removeLabel}" >
                	<span class="ui-icon ui-icon-minus squared-icons">-</span>
          	  	</button>					
			</c:if>		
		</jsp:attribute>
		<jsp:attribute name="body">
			<tc:verified-requirements-table includeIndirectlyVerified="${ true }" 
                                            linkable="${ linkable }" 
                                            verifiedRequirementsTableUrl="${ verifiedRequirementsTableUrl }" 
                                            verifiedRequirementsUrl="${verifiedRequirementsUrl }" 
                                            containerId="contextual-content" 
                                            milestoneConf="${milestoneConf}"/>
		</jsp:attribute>
	</comp:toggle-panel>
</div>
