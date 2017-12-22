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
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<%@ attribute name="verifiedRequirementsUrl" required="true" description="URL to manipulate the verified requirements" %>
<%@ attribute name="containerId" required="true" description="if of dom container that will hold the table events" %>
<%@ attribute name="verifiedRequirementsTableUrl" required="true" description="URL for the verified requirements table" %>
<%@ attribute name="linkable" required="true" description=" boolean that says if the concerned test case is viewed by a user who has LINK rights on this entity" %>
<%@ attribute name="includeIndirectlyVerified" required="true" description="boolean that says if the table must include indirectly verified requirements" %>
<%@ attribute name="milestoneConf"  required="true" description="an instance of MilestoneFeatureConfiguration"  type="java.lang.Object"%>


<s:url var="tableLanguageUrl" value="/datatables/messages" />
<s:url var="requirementVersionsUrl" value="/requirement-versions"/>
<s:url var="root" value="/" />


<%-- Attention ! si vous refactorez cette page vous pouvez utiliser la version thymeleaf de la table des test-steps : templates/verified-requirements-bloc.frag.html --%>

	<c:if test="${not includeIndirectlyVerified}">
	<script type="text/javascript" th:inline="javascript">
	require([ "common" ], function(common) {
		require([ "jquery",  "domReady","verified-requirements/TestCaseDirectVerifiedRequirementsTable" ], function($, domReady, TestCaseDirectVerifiedRequirementsTable) {
			domReady(function() {
				squashtm.verifiedRequirementsTable = new TestCaseDirectVerifiedRequirementsTable();
			});
		});
	});
		</script>
	</c:if>
	<script type="text/javascript" th:inline="javascript">
			if (!squashtm) {
				var squashtm = {};
			}
			if (!squashtm.app) {
				squashtm.app = {
					contextRoot : "${root}",
					locale : "<f:message key='squashtm.locale'/>"
				};
			}
			squashtm.app.verifiedRequirementsTableSettings = {
				containerId : "${containerId}",
				linkable : ${linkable},
				url :"${verifiedRequirementsUrl}",
				messages : {
					cancel : "<f:message key='label.Cancel' />",
					ok : "<f:message key='rich-edit.button.ok.label' />",
					remove : "<f:message key='label.Delete'/>",
				},
			};
		</script>


<table id="verified-requirements-table" class="unstyled-table"
data-def='datakeys-id=entity-id ,ajaxsource=${ verifiedRequirementsTableUrl }'>
	<thead>
		<tr>
			<th data-def="select, map=entity-index">#</th>
			<th data-def="sortable, map=project"><f:message key="label.project" /></th>
			<th data-def="sortable, map=entity-id"><f:message key="label.versionId"/></th>
            <c:if test="${milestoneConf.milestoneDatesColumnVisible}">
            <th data-def="sortable, map=milestone-dates, tooltip-target=milestone"><f:message key="label.Milestones"/></th>
            </c:if>
          	<th data-def="sortable, map=reference"><f:message key="label.Reference"/></th>
			<th data-def="sortable, map=name, sClass=verif-req-description, link=${requirementVersionsUrl}/{entity-id}/info"><f:message key="requirement.name.label" /></th>
			<th data-def="sClass=versionNumber, sortable, map=versionNumber"><f:message key="requirement-version.version-number.label" /></th>
			<th data-def="sortable, map=criticality"><f:message key="requirement.criticality.label"/></th>
			<th data-def="sortable, map=category"><f:message key="requirement.category.label"/></th>
			<th data-def='map=verifyingSteps'><f:message key="label.test-step.short"/></th>
			<th data-def='unbind-button,narrow,  map=empty-delete-holder'>&nbsp;</th>
			  <th data-def="map=milestone, invisible"></th>
		</tr>
	</thead>
	<tbody>
		<%-- Will be populated through ajax --%>
	</tbody>
</table>

<div id="remove-verified-requirement-version-dialog" class="popup-dialog not-displayed" title="<f:message key='label.Confirm'/>">
<div><f:message key='dialog.remove-requirement-version-association.message' /></div>
<div class="popup-dialog-buttonpane">
			<input class="confirm" type="button" value="<f:message key='label.Confirm'/>" />
			 <input class="cancel" type="button" value="<f:message key='label.Cancel'/>" />
		</div>
</div>

<div id="remove-obsolete-verified-requirement-version-dialog" class="popup-dialog not-displayed" title="<f:message key='dialog.obsolete.requirement.version.removal.confirm.title'/>">
<div><f:message key='dialog.obsolete.requirement.version.removal.confirm.text' /></div>
<div class="popup-dialog-buttonpane">
			<input class="confirm" type="button" value="<f:message key='label.Confirm'/>" />
			 <input class="cancel" type="button" value="<f:message key='label.Cancel'/>" />
		</div>
</div>


<c:if test="${includeIndirectlyVerified }">
<div id="no-selected-direct-requirement-dialog" class="popup-dialog not-displayed"
		title="<f:message key='popup.title.error' />">
		<span><f:message key="verified-requirements.table.indirectverifiedrequirements.removalattemptsforbidden.label"/></span>
</div>
</c:if>
